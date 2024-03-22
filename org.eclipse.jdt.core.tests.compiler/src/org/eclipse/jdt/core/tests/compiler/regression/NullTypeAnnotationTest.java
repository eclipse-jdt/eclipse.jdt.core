/*******************************************************************************
 * Copyright (c) 2012, 2021 GK Software AG and others.
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
 *     IBM Corporation
 *     Till Brychcy - Contribution for
 *								Bug 467032 - TYPE_USE Null Annotations: IllegalStateException with annotated arrays of Enum when accessed via BinaryTypeBinding
 *								Bug 467482 - TYPE_USE null annotations: Incorrect "Redundant null check"-warning
 *								Bug 473713 - [1.8][null] Type mismatch: cannot convert from @NonNull A1 to @NonNull A1
 *								Bug 467430 - TYPE_USE Null Annotations: Confusing error message with known null value
 *     Pierre-Yves B. <pyvesdev@gmail.com> - Contribution for
 *                              Bug 559618 - No compiler warning for import from same package
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.Excuse;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class NullTypeAnnotationTest extends AbstractNullAnnotationTest {

	public NullTypeAnnotationTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which do not belong to the class are skipped...
	static {
//			TESTS_NAMES = new String[] { "testBug456584" };
//			TESTS_NUMBERS = new int[] { 561 };
//			TESTS_RANGE = new int[] { 1, 2049 };
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}

	public static Class testClass() {
		return NullTypeAnnotationTest.class;
	}

	// a list with nullable elements is used
	public void test_nonnull_list_elements_01() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				  """
					import org.eclipse.jdt.annotation.*;
					import java.util.List;
					public class X {
					    void foo(List<@Nullable Object> l) {
					        System.out.print(l.get(0).toString()); // problem: retrieved element can be null
					        l.add(null);
					    }
					    void bar(java.util.List<@Nullable Object> l) {
					        System.out.print(l.get(1).toString()); // problem: retrieved element can be null
					        l.add(null);
					    }
					}
					"""},
			"""
				----------
				1. ERROR in X.java (at line 5)
					System.out.print(l.get(0).toString()); // problem: retrieved element can be null
					                 ^^^^^^^^
				Potential null pointer access: The method get(int) may return null
				----------
				2. ERROR in X.java (at line 9)
					System.out.print(l.get(1).toString()); // problem: retrieved element can be null
					                 ^^^^^^^^
				Potential null pointer access: The method get(int) may return null
				----------
				""");
	}

	// a list with nullable elements is used, custom annotations
	public void test_nonnull_list_elements_01a() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
		customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
		runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError,
			new String[] {
				CUSTOM_NULLABLE_NAME,
				CUSTOM_NULLABLE_CONTENT_JSR308,
				CUSTOM_NONNULL_NAME,
				CUSTOM_NONNULL_CONTENT_JSR308,
				"X.java",
				  """
					import org.foo.*;
					import java.util.List;
					public class X {
					    void foo(List<@Nullable Object> l) {
					        System.out.print(l.get(0).toString()); // problem: retrieved element can be null
					        l.add(null);
					    }
					    void bar(java.util.List<@Nullable Object> l) {
					        System.out.print(l.get(1).toString()); // problem: retrieved element can be null
					        l.add(null);
					    }
					}
					"""},
			"""
				----------
				1. ERROR in X.java (at line 5)
					System.out.print(l.get(0).toString()); // problem: retrieved element can be null
					                 ^^^^^^^^
				Potential null pointer access: The method get(int) may return null
				----------
				2. ERROR in X.java (at line 9)
					System.out.print(l.get(1).toString()); // problem: retrieved element can be null
					                 ^^^^^^^^
				Potential null pointer access: The method get(int) may return null
				----------
				""",
			null,
			true, /* shouldFlush*/
			customOptions);
	}

	// a list with nullable elements is used, @Nullable is second annotation
	public void test_nonnull_list_elements_02() {
		runNegativeTestWithLibs(
			new String[] {
				"Dummy.java",
				"""
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.*;
					@Retention(RetentionPolicy.CLASS)
					@Target({METHOD,PARAMETER,LOCAL_VARIABLE,TYPE_USE})
					public @interface Dummy {
					}
					""",
				"X.java",
				  """
					import org.eclipse.jdt.annotation.*;
					import java.util.List;
					public class X {
					    void foo(List<@Dummy @Nullable Object> l) {
					        System.out.print(l.get(0).toString()); // problem: retrieved element can be null
					        l.add(null);
					    }
					    void bar(java.util.List<@Dummy @Nullable Object> l) {
					        System.out.print(l.get(1).toString()); // problem: retrieved element can be null
					        l.add(null);
					    }
					    void bar2(java.util.List<java.lang.@Dummy @Nullable Object> l2) {
					        System.out.print(l2.get(1).toString()); // problem: retrieved element can be null
					        l2.add(null);
					    }
					}
					"""},
			"""
				----------
				1. ERROR in X.java (at line 5)
					System.out.print(l.get(0).toString()); // problem: retrieved element can be null
					                 ^^^^^^^^
				Potential null pointer access: The method get(int) may return null
				----------
				2. ERROR in X.java (at line 9)
					System.out.print(l.get(1).toString()); // problem: retrieved element can be null
					                 ^^^^^^^^
				Potential null pointer access: The method get(int) may return null
				----------
				3. ERROR in X.java (at line 13)
					System.out.print(l2.get(1).toString()); // problem: retrieved element can be null
					                 ^^^^^^^^^
				Potential null pointer access: The method get(int) may return null
				----------
				""");
	}

	// a list with non-null elements is used, list itself is nullable
	public void test_nonnull_list_elements_03() {
		runNegativeTestWithLibs(
			new String[] {
				"Dummy.java",
				  """
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.*;
					@Retention(RetentionPolicy.CLASS)
					@Target({METHOD,PARAMETER,LOCAL_VARIABLE,TYPE_USE})
					public @interface Dummy {
					}
					""",
				"p/List.java",
				  """
					package p;
					public interface List<T> {
						T get(int i);
					 void add(T e);
					 void add(int i, T e);
					}
					""",
				"X.java",
				  """
					import org.eclipse.jdt.annotation.*;
					import p.List;
					public class X {
					    void foo(@Nullable List<@NonNull Object> l) {
					        System.out.print(l.get(0).toString()); // problem: l may be null
					        l.add(null); // problem: cannot insert 'null' into this list
					    }
					    void bar(@Nullable List<java.lang.@NonNull Object> l) {
					        System.out.print(l.get(0).toString()); // problem: l may be null
					        l.add(0, null); // problem: cannot insert 'null' into this list
					    }
					    void bar2(@Dummy p.@Nullable List<java.lang.@NonNull Object> l2) {
					        System.out.print(l2.get(0).toString()); // problem: l2 may be null
					        l2.add(0, null); // problem: cannot insert 'null' into this list
					    }
					}
					"""},
			"""
				----------
				1. ERROR in X.java (at line 5)
					System.out.print(l.get(0).toString()); // problem: l may be null
					                 ^
				Potential null pointer access: this expression has a \'@Nullable\' type
				----------
				2. ERROR in X.java (at line 6)
					l.add(null); // problem: cannot insert \'null\' into this list
					      ^^^^
				Null type mismatch: required \'@NonNull Object\' but the provided value is null
				----------
				3. ERROR in X.java (at line 9)
					System.out.print(l.get(0).toString()); // problem: l may be null
					                 ^
				Potential null pointer access: this expression has a \'@Nullable\' type
				----------
				4. ERROR in X.java (at line 10)
					l.add(0, null); // problem: cannot insert \'null\' into this list
					         ^^^^
				Null type mismatch: required \'@NonNull Object\' but the provided value is null
				----------
				5. ERROR in X.java (at line 13)
					System.out.print(l2.get(0).toString()); // problem: l2 may be null
					                 ^^
				Potential null pointer access: this expression has a \'@Nullable\' type
				----------
				6. ERROR in X.java (at line 14)
					l2.add(0, null); // problem: cannot insert \'null\' into this list
					          ^^^^
				Null type mismatch: required \'@NonNull Object\' but the provided value is null
				----------
				""");
	}

	// an outer and inner class both have a type parameter,
	// client instantiates with nullable/nonnull actual type arguments
	public void test_nestedType_01() {
		runNegativeTestWithLibs(
			new String[] {
				"A.java",
				  """
					import org.eclipse.jdt.annotation.*;
					public class A<X> {
					    public class I<Y> {
					        X anX;
					        public X foo(Y l) {
					            return anX;
					        }
					        public I(X x) {
					            anX = x;
					        }
					    }
					    void bar(A<@Nullable Object>.I<@NonNull Object> i) {
					        @NonNull Object o = i.foo(null); // problems: argument and assignment violate null contracts
					    }
					}
					"""},
			"""
				----------
				1. ERROR in A.java (at line 13)
					@NonNull Object o = i.foo(null); // problems: argument and assignment violate null contracts
					                    ^^^^^^^^^^^
				Null type mismatch (type annotations): required '@NonNull Object' but this expression has type '@Nullable Object'
				----------
				2. ERROR in A.java (at line 13)
					@NonNull Object o = i.foo(null); // problems: argument and assignment violate null contracts
					                          ^^^^
				Null type mismatch: required \'@NonNull Object\' but the provided value is null
				----------
				""");
	}

	// an outer and inner class both have a type parameter,
	// a subclass instantiates with nullable/nonnull actual type arguments
	// and correctly implements an abstract inherited method
	// compile errors only inside that method
	public void test_nestedType_02() {
		runNegativeTestWithLibs(
			new String[] {
				"A.java",
				  """
					public class A<X> {
					    public abstract class I<Y> {
					        public abstract X foo(Y l);
					        public X idX(X in) { return in; }
					        public Y idY(Y in) { return in; }
					    }
					}
					""",
				"B.java",
				  """
					import org.eclipse.jdt.annotation.*;
					public class B extends A<@NonNull Object> {
					    public class J extends I<@Nullable String> {
					        @Override
					        public @NonNull Object foo(@Nullable String l) {
					            System.out.print(idX(null));
					            return idY(null);
					        }
					    }
					}
					"""},
			"""
				----------
				1. ERROR in B.java (at line 6)
					System.out.print(idX(null));
					                     ^^^^
				Null type mismatch: required \'@NonNull Object\' but the provided value is null
				----------
				2. ERROR in B.java (at line 7)
					return idY(null);
					       ^^^^^^^^^
				Null type mismatch (type annotations): required '@NonNull Object' but this expression has type '@Nullable String'
				----------
				""");
	}

	// an outer and inner class both have a type parameter,
	// a subclass instantiates with nullable/nonnull actual type arguments
	// and incorrectly implements an abstract inherited method
	public void test_nestedType_03() {
		runNegativeTestWithLibs(
			new String[] {
				"A.java",
				  """
					public class A<X> {
					    public abstract class I<Y> {
					        public abstract X foo(Y l);
					    }
					}
					""",
				"B.java",
				  """
					import org.eclipse.jdt.annotation.*;
					public class B extends A<@NonNull Object> {
					    public class J extends I<@Nullable String> {
					        @Override
					        public @Nullable Object foo(@NonNull String l) {
					            return null;
					        }
					    }
					}
					"""},
			"""
				----------
				1. ERROR in B.java (at line 5)
					public @Nullable Object foo(@NonNull String l) {
					       ^^^^^^^^^^^^^^^^
				The return type is incompatible with '@NonNull Object' returned from A<Object>.I<String>.foo(String) (mismatching null constraints)
				----------
				2. ERROR in B.java (at line 5)
					public @Nullable Object foo(@NonNull String l) {
					                            ^^^^^^^^^^^^^^^
				Illegal redefinition of parameter l, inherited method from A<Object>.I<String> declares this parameter as @Nullable
				----------
				""");
	}

	// a reference to a nested type has annotations for both types
	public void test_nestedType_04() {
		runNegativeTestWithLibs(
			new String[] {
				"A.java",
				  """
					public class A<X> {
					    public abstract class I<Y> {
					        public abstract X foo(Y l);
					    }
					}
					""",
				"B.java",
				  """
					import org.eclipse.jdt.annotation.*;
					public class B {
					    public void foo(A<Object>.@Nullable I<@NonNull String> ai) {
					            ai.foo(null); // problems: ai can be null, arg must not be null
					    }
					}
					"""},
			"""
				----------
				1. ERROR in B.java (at line 4)
					ai.foo(null); // problems: ai can be null, arg must not be null
					^^
				Potential null pointer access: this expression has a \'@Nullable\' type
				----------
				2. ERROR in B.java (at line 4)
					ai.foo(null); // problems: ai can be null, arg must not be null
					       ^^^^
				Null type mismatch: required \'@NonNull String\' but the provided value is null
				----------
				""");
	}

	// a reference to a nested type has annotations for both types, mismatch in detail of outer
	public void test_nestedType_05() {
		runNegativeTestWithLibs(
			new String[] {
				"A.java",
				  """
					public class A<X> {
					    public abstract class I<Y> {
					        public abstract X foo(Y l);
					    }
					}
					""",
				"B.java",
				  """
					import org.eclipse.jdt.annotation.*;
					public class B {
					    public void foo(A<@NonNull Object>.@Nullable I<@NonNull String> ai1) {
							A<@Nullable Object>.@Nullable I<@NonNull String> ai2 = ai1;
					    }
					}
					"""},
			"""
				----------
				1. ERROR in B.java (at line 4)
					A<@Nullable Object>.@Nullable I<@NonNull String> ai2 = ai1;
					                                                       ^^^
				Null type mismatch (type annotations): required \'A<@Nullable Object>.@Nullable I<@NonNull String>\' but this expression has type \'A<@NonNull Object>.@Nullable I<@NonNull String>\'
				----------
				""");
	}

	public void testMissingAnnotationTypes_01() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public class U {}
					   @Missing1 X.@Missing2 U fU;
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					@Missing1 X.@Missing2 U fU;
					 ^^^^^^^^
				Missing1 cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 3)
					@Missing1 X.@Missing2 U fU;
					             ^^^^^^^^
				Missing2 cannot be resolved to a type
				----------
				""",
			this.LIBS,
			true/*shouldFlush*/);
	}

	// bug 392862 - [1.8][compiler][null] Evaluate null annotations on array types
	// annotation on leaf type in 1-dim array
	public void testArrayType_01() {
		runNegativeTestWithLibs(
			new String[] {
				"Wrapper.java",
				  """
					public class Wrapper<T> {
						T content;\
						public Wrapper(T t) { content = t; }
						public T content() { return content; }
					}
					""",
				"A.java",
				  """
					import org.eclipse.jdt.annotation.*;
					public class A {
					    void bar(Wrapper<@NonNull String[]> realStrings, Wrapper<@Nullable String[]> maybeStrings) {
					        System.out.println(realStrings.content()[0].toUpperCase()); // no problem
					        realStrings.content()[0] = null; // problem: cannot assign null as @NonNull element
					        System.out.println(maybeStrings.content()[0].toUpperCase()); // problem: element can be null
					        maybeStrings.content()[0] = null; // no problem
					    }
					}
					"""},
			"""
				----------
				1. ERROR in A.java (at line 5)
					realStrings.content()[0] = null; // problem: cannot assign null as @NonNull element
					                           ^^^^
				Null type mismatch: required \'@NonNull String\' but the provided value is null
				----------
				2. ERROR in A.java (at line 6)
					System.out.println(maybeStrings.content()[0].toUpperCase()); // problem: element can be null
					                   ^^^^^^^^^^^^^^^^^^^^^^^^^
				Potential null pointer access: array element may be null
				----------
				""");
	}

	// bug 392862 - [1.8][compiler][null] Evaluate null annotations on array types
	// annotation on leaf type in 2-dim array
	public void testArrayType_02() {
		runNegativeTestWithLibs(
			new String[] {
				"Wrapper.java",
				  """
					public class Wrapper<T> {
						T content;\
						public Wrapper(T t) { content = t; }
						public T content() { return content; }
					}
					""",
				"A.java",
				  """
					import org.eclipse.jdt.annotation.*;
					public class A {
					    void bar(Wrapper<@NonNull String[][]> realStrings, Wrapper<@Nullable String[][]> maybeStrings) {
					        System.out.println(realStrings.content()[0][0].toUpperCase()); // no problem
					        realStrings.content()[0][0] = null; // problem: cannot assign null as @NonNull element
					        System.out.println(maybeStrings.content()[0][0].toUpperCase()); // problem: element can be null
					        maybeStrings.content()[0][0] = null; // no problem
					    }
					}
					"""},
			"""
				----------
				1. ERROR in A.java (at line 5)
					realStrings.content()[0][0] = null; // problem: cannot assign null as @NonNull element
					                              ^^^^
				Null type mismatch: required \'@NonNull String\' but the provided value is null
				----------
				2. ERROR in A.java (at line 6)
					System.out.println(maybeStrings.content()[0][0].toUpperCase()); // problem: element can be null
					                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Potential null pointer access: array element may be null
				----------
				""");
	}

	// bug 392862 - [1.8][compiler][null] Evaluate null annotations on array types
	// annotation on array type (1-dim array)
	public void testArrayType_03() {
		runNegativeTestWithLibs(
			new String[] {
				"A.java",
				  """
					import org.eclipse.jdt.annotation.*;
					public class A {
					    void array(String @NonNull[] realStringArray, String @Nullable[] maybeStringArray) {
					        @NonNull Object array;
					        array = realStringArray;  // no problem
					        realStringArray = null; 	 // problem: cannot assign null as @NonNull array
					        array = maybeStringArray; // problem: array can be null
					        maybeStringArray = null;  // no problem
					    }
					    void leaf(String @NonNull[] realStringArray, String @Nullable[] maybeStringArray, boolean b) {
					        @NonNull String string;
					        string = realStringArray[0];  // problem: unchecked conversion
					        realStringArray[0] = null; 	 // no problem
					        if (b)
					            string = maybeStringArray[0]; // problems: indexing nullable array & unchecked conversion
					        else
					            maybeStringArray[0] = null; 	 // problem: indexing nullable array
					        maybeStringArray[0] = null; 	 // problem protected by previous dereference
					    }
					}
					"""},
		    """
				----------
				1. ERROR in A.java (at line 6)
					realStringArray = null; 	 // problem: cannot assign null as @NonNull array
					                  ^^^^
				Null type mismatch: required \'String @NonNull[]\' but the provided value is null
				----------
				2. ERROR in A.java (at line 7)
					array = maybeStringArray; // problem: array can be null
					        ^^^^^^^^^^^^^^^^
				Null type mismatch (type annotations): required '@NonNull Object' but this expression has type 'String @Nullable[]'
				----------
				3. WARNING in A.java (at line 12)
					string = realStringArray[0];  // problem: unchecked conversion
					         ^^^^^^^^^^^^^^^^^^
				Null type safety (type annotations): The expression of type 'String' needs unchecked conversion to conform to \'@NonNull String\'
				----------
				4. ERROR in A.java (at line 15)
					string = maybeStringArray[0]; // problems: indexing nullable array & unchecked conversion
					         ^^^^^^^^^^^^^^^^
				Potential null pointer access: this expression has a \'@Nullable\' type
				----------
				5. WARNING in A.java (at line 15)
					string = maybeStringArray[0]; // problems: indexing nullable array & unchecked conversion
					         ^^^^^^^^^^^^^^^^^^^
				Null type safety (type annotations): The expression of type 'String' needs unchecked conversion to conform to \'@NonNull String\'
				----------
				6. ERROR in A.java (at line 17)
					maybeStringArray[0] = null; 	 // problem: indexing nullable array
					^^^^^^^^^^^^^^^^
				Potential null pointer access: this expression has a \'@Nullable\' type
				----------
				""");
	}

	// bug 392862 - [1.8][compiler][null] Evaluate null annotations on array types
	// annotation on intermediate type in 2-dim array
	public void testArrayType_04() {
		runNegativeTestWithLibs(
			new String[] {
				"A.java",
				  """
					import org.eclipse.jdt.annotation.*;
					public class A {
					    void outer(String [] @NonNull[] realArrays, String [] @Nullable[] maybeArrays) {
					        @NonNull Object array;
					        array = realArrays; 		// problem: unchecked conversion
					        realArrays = null; 		// no problem, outer array is unspecified
					        array = maybeArrays; 	// problem: unchecked conversion
					        maybeArrays = null; 		// no problem
					    }
					    void inner(String [] @NonNull[] realArrays, String [] @Nullable[] maybeArrays) {
					        @NonNull Object array;
					        array = realArrays[0]; 	// no problem
					        realArrays[0] = null; 	// problem: cannot assign null to @NonNull array
					        array = maybeArrays[0]; 	// problem: element can be null
					        maybeArrays[0] = null; 	// no problem
					    }
					    void leaf(String [] @NonNull[] realArrays, String [] @Nullable[] maybeArrays) {
					        @NonNull Object array;
					        array = realArrays[0][0]; // problem: unchecked conversion
					        realArrays[0][0] = null;  // no problem, element type is unspecified
					        array = maybeArrays[0][0]; // problems: indexing nullable array & unchecked conversion
					        maybeArrays[0][0] = null; // problem: indexing nullable array
					    }
					}
					"""},
			"""
				----------
				1. WARNING in A.java (at line 5)
					array = realArrays; 		// problem: unchecked conversion
					        ^^^^^^^^^^
				Null type safety (type annotations): The expression of type \'String [] @NonNull[]\' needs unchecked conversion to conform to \'@NonNull Object\'
				----------
				2. WARNING in A.java (at line 7)
					array = maybeArrays; 	// problem: unchecked conversion
					        ^^^^^^^^^^^
				Null type safety (type annotations): The expression of type \'String [] @Nullable[]\' needs unchecked conversion to conform to \'@NonNull Object\'
				----------
				3. ERROR in A.java (at line 13)
					realArrays[0] = null; 	// problem: cannot assign null to @NonNull array
					                ^^^^
				Null type mismatch: required \'String @NonNull[]\' but the provided value is null
				----------
				4. ERROR in A.java (at line 14)
					array = maybeArrays[0]; 	// problem: element can be null
					        ^^^^^^^^^^^^^^
				Null type mismatch (type annotations): required \'@NonNull Object\' but this expression has type \'String @Nullable[]\'
				----------
				5. WARNING in A.java (at line 19)
					array = realArrays[0][0]; // problem: unchecked conversion
					        ^^^^^^^^^^^^^^^^
				Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'@NonNull Object\'
				----------
				6. ERROR in A.java (at line 21)
					array = maybeArrays[0][0]; // problems: indexing nullable array & unchecked conversion
					        ^^^^^^^^^^^^^^
				Potential null pointer access: array element may be null
				----------
				7. WARNING in A.java (at line 21)
					array = maybeArrays[0][0]; // problems: indexing nullable array & unchecked conversion
					        ^^^^^^^^^^^^^^^^^
				Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'@NonNull Object\'
				----------
				8. ERROR in A.java (at line 22)
					maybeArrays[0][0] = null; // problem: indexing nullable array
					^^^^^^^^^^^^^^
				Potential null pointer access: array element may be null
				----------
				""");
	}

	// bug 392862 - [1.8][compiler][null] Evaluate null annotations on array types
	// mismatches against outer array type, test display of type annotation in error messages
	public void testArrayType_05() {
		runNegativeTestWithLibs(
			new String[] {
				"A.java",
				  """
					import org.eclipse.jdt.annotation.*;
					public class A {
					    void outer(String @NonNull[] @NonNull[] realArrays, String @NonNull[] @Nullable[] maybeArrays, String @Nullable[][] unknownArrays) {
					        realArrays[0] = maybeArrays[0];		// problem: inner array can be null
					        realArrays[0] = unknownArrays[0];	// problems: inner array is unspecified, outer can be null
					    }
					    void oneDim(String @Nullable[] maybeStrings, String[] unknownStrings) {
					        String @NonNull[] s = maybeStrings;
					        s = unknownStrings;
					        consume(maybeStrings);
					        consume(unknownStrings);
					    }
					    void consume(String @NonNull[] s) {};
					}
					"""},
			"""
				----------
				1. ERROR in A.java (at line 4)
					realArrays[0] = maybeArrays[0];		// problem: inner array can be null
					                ^^^^^^^^^^^^^^
				Null type mismatch (type annotations): required \'String @NonNull[]\' but this expression has type \'String @Nullable[]\'
				----------
				2. ERROR in A.java (at line 5)
					realArrays[0] = unknownArrays[0];	// problems: inner array is unspecified, outer can be null
					                ^^^^^^^^^^^^^
				Potential null pointer access: this expression has a \'@Nullable\' type
				----------
				3. WARNING in A.java (at line 5)
					realArrays[0] = unknownArrays[0];	// problems: inner array is unspecified, outer can be null
					                ^^^^^^^^^^^^^^^^
				Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'String @NonNull[]\'
				----------
				4. ERROR in A.java (at line 8)
					String @NonNull[] s = maybeStrings;
					                      ^^^^^^^^^^^^
				Null type mismatch (type annotations): required \'String @NonNull[]\' but this expression has type \'String @Nullable[]\'
				----------
				5. WARNING in A.java (at line 9)
					s = unknownStrings;
					    ^^^^^^^^^^^^^^
				Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'String @NonNull[]\'
				----------
				6. ERROR in A.java (at line 10)
					consume(maybeStrings);
					        ^^^^^^^^^^^^
				Null type mismatch (type annotations): required \'String @NonNull[]\' but this expression has type \'String @Nullable[]\'
				----------
				7. WARNING in A.java (at line 11)
					consume(unknownStrings);
					        ^^^^^^^^^^^^^^
				Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'String @NonNull[]\'
				----------
				""");
	}

	// bug 392862 - [1.8][compiler][null] Evaluate null annotations on array types
	// more compiler messages
	public void testArrayType_10() {
		runNegativeTestWithLibs(
			new String[] {
				"A.java",
				  """
					import org.eclipse.jdt.annotation.*;
					public class A {
					    void outer(String @NonNull[] @NonNull[] realArrays, String @NonNull[] @Nullable[] maybeArrays, String @Nullable[][] unknownArrays, String @NonNull[][] mixedArrays) {
					        realArrays = maybeArrays;			// problem on inner dimension!
					        realArrays = unknownArrays; 			// problems on both dimensions
					        maybeArrays = realArrays;			// problem on inner dimension
					        unknownArrays = maybeArrays;			// no problem: outer @NonNull is compatible to expected @Nullable, inner @Nullable is compatible to inner unspecified
					        realArrays = mixedArrays;			// problem on inner
					        maybeArrays = mixedArrays;			// problem on inner
					        consume(maybeArrays, mixedArrays, maybeArrays);
					    }
					    void consume(String @NonNull[] @NonNull[] realStrings, String @NonNull[] @Nullable[] maybeArrays, String @Nullable[][] unknownArrays) {
					    }
					}
					"""},
			"""
				----------
				1. ERROR in A.java (at line 4)
					realArrays = maybeArrays;			// problem on inner dimension!
					             ^^^^^^^^^^^
				Null type mismatch (type annotations): required \'String @NonNull[] @NonNull[]\' but this expression has type \'String @NonNull[] @Nullable[]\'
				----------
				2. ERROR in A.java (at line 5)
					realArrays = unknownArrays; 			// problems on both dimensions
					             ^^^^^^^^^^^^^
				Null type mismatch (type annotations): required \'String @NonNull[] @NonNull[]\' but this expression has type \'String @Nullable[] []\'
				----------
				3. ERROR in A.java (at line 6)
					maybeArrays = realArrays;			// problem on inner dimension
					              ^^^^^^^^^^
				Null type mismatch (type annotations): required \'String @NonNull[] @Nullable[]\' but this expression has type \'String @NonNull[] @NonNull[]\'
				----------
				4. WARNING in A.java (at line 8)
					realArrays = mixedArrays;			// problem on inner
					             ^^^^^^^^^^^
				Null type safety (type annotations): The expression of type \'String @NonNull[] []\' needs unchecked conversion to conform to \'String @NonNull[] @NonNull[]\'
				----------
				5. WARNING in A.java (at line 9)
					maybeArrays = mixedArrays;			// problem on inner
					              ^^^^^^^^^^^
				Null type safety (type annotations): The expression of type \'String @NonNull[] []\' needs unchecked conversion to conform to \'String @NonNull[] @Nullable[]\'
				----------
				6. ERROR in A.java (at line 10)
					consume(maybeArrays, mixedArrays, maybeArrays);
					        ^^^^^^^^^^^
				Null type mismatch (type annotations): required \'String @NonNull[] @NonNull[]\' but this expression has type \'String @NonNull[] @Nullable[]\'
				----------
				7. WARNING in A.java (at line 10)
					consume(maybeArrays, mixedArrays, maybeArrays);
					                     ^^^^^^^^^^^
				Null type safety (type annotations): The expression of type \'String @NonNull[] []\' needs unchecked conversion to conform to \'String @NonNull[] @Nullable[]\'
				----------
				""");
	}

	// combine flow info on outer type with annotation analysis for inners
	public void testArrayType_11() {
		runNegativeTestWithLibs(
			new String[] {
				"ArrayTest.java",
				"""
					import org.eclipse.jdt.annotation.*;
					
					public class ArrayTest {
					\t
						@NonNull Object @NonNull[] test1(@NonNull Object @Nullable[] in) {
							if (in == null) throw new NullPointerException();\s
							return in; // array needs check, element is OK
						}
						@NonNull Object @NonNull[] test2(@Nullable Object @Nullable[] in) {
							if (in == null) throw new NullPointerException();\s
							return in; // array needs check, element is NOK
						}
						@NonNull Object @NonNull[]@NonNull[] test3(@NonNull Object @Nullable[][] in) {
							if (in == null) throw new NullPointerException();\s
							return in; // outer needs check, inner is unchecked, element is OK
						}
						@NonNull Object @NonNull[]@NonNull[] test4(@Nullable Object @Nullable[][] in) {
							if (in == null) throw new NullPointerException();\s
							return in; // outer needs check, inner is unchecked, element is NOK
						}
						@NonNull Object @NonNull[]@NonNull[] test5(@NonNull Object @Nullable[]@Nullable[] in) {
							if (in == null) throw new NullPointerException();\s
							return in; // outer needs check, inner is NOK, element is OK
						}
						@NonNull Object @NonNull[]@NonNull[] test6(@NonNull Object @Nullable[]@NonNull[] in) {
							if (in == null) throw new NullPointerException();\s
							return in; // outer needs check, inner is OK, element is OK
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in ArrayTest.java (at line 11)
					return in; // array needs check, element is NOK
					       ^^
				Null type mismatch (type annotations): required \'@NonNull Object @NonNull[]\' but this expression has type \'@Nullable Object @Nullable[]\'
				----------
				2. WARNING in ArrayTest.java (at line 15)
					return in; // outer needs check, inner is unchecked, element is OK
					       ^^
				Null type safety (type annotations): The expression of type \'@NonNull Object @Nullable[] []\' needs unchecked conversion to conform to \'@NonNull Object @NonNull[] @NonNull[]\'
				----------
				3. ERROR in ArrayTest.java (at line 19)
					return in; // outer needs check, inner is unchecked, element is NOK
					       ^^
				Null type mismatch (type annotations): required \'@NonNull Object @NonNull[] @NonNull[]\' but this expression has type \'@Nullable Object @Nullable[] []\'
				----------
				4. ERROR in ArrayTest.java (at line 23)
					return in; // outer needs check, inner is NOK, element is OK
					       ^^
				Null type mismatch (type annotations): required \'@NonNull Object @NonNull[] @NonNull[]\' but this expression has type \'@NonNull Object @Nullable[] @Nullable[]\'
				----------
				""");
	}

	// https://bugs.eclipse.org/403216 - [1.8][null] TypeReference#captureTypeAnnotations treats type annotations as type argument annotations
	public void testBug403216_1() {
		runConformTestWithLibs(
			new String[] {
				"Test.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					
					public class Test {}
					
					class X {
						class Y {
							public void foo( @A X. @B Y this) {}
						}
					}
					@Target(value={ElementType.TYPE_USE})
					@interface A {}
					@Target(value={ElementType.TYPE_USE})
					@interface B {}
					"""
			},
			null,
			"");
	}

	// issue from https://bugs.eclipse.org/bugs/show_bug.cgi?id=403216#c7
	public void testBug403216_2() {
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_ANNOTATED_TYPE_ARGUMENT_TO_UNANNOTATED, JavaCore.IGNORE);
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					import java.util.*;
					public class X {
					    void test(List<@NonNull String> strings) {
					        List<String> someStrings;
					        someStrings = strings;
					    }
					}
					"""
			},
			options,
			"");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=403216#c9
	public void testBug403216_3a() {
		runNegativeTestWithLibs(
			new String[] {
				"Test.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					import org.eclipse.jdt.annotation.*;
					
					public class Test {}
					
					class X {
						class Y {
							public void foo( @A X. @NonNull Y this) {}
						}
					}
					@Target(value={ElementType.TYPE_USE})
					@interface A {}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in Test.java (at line 9)
					public void foo( @A X. @NonNull Y this) {}
					                 ^^^^^^^^^^^^^^^^
				Nullness annotations are not applicable at this location\s
				----------
				""");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=403216#c9
	public void testBug403216_3b() {
		runConformTestWithLibs(
			new String[] {
				"Test.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					
					public class Test {}
					
					class X {
						class Y {
							public void foo( @A X. @A Y this) {}
						}
					}
					@Target(value={ElementType.TYPE_USE})
					@interface A {}
					"""
			},
			getCompilerOptions(),
			"");
	}

	// https://bugs.eclipse.org/403457 - [1.8][compiler] NPE in WildcardBinding.signature
	public void testBug403457_1() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					import org.eclipse.jdt.annotation.*;
					
					public class X {
						void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}
					   void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}
					}
					
					@Target(ElementType.TYPE_USE)
					@interface Marker {
					\t
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}
					         ^^^
				Map cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 7)
					void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}
					         ^^^
				Map cannot be resolved to a type
				----------
				""",
			this.LIBS,
			true/*flush*/);
	}

	// https://bugs.eclipse.org/403457 - [1.8][compiler] NPE in WildcardBinding.signature
	// variant with null annotations
	public void testBug403457_2() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					// import java.util.Map;
					import org.eclipse.jdt.annotation.*;
					
					public class X {
						void foo(Map<@Nullable ? super @Nullable Object, @Nullable ? extends @Nullable String> m){}
					   void goo(Map<@Nullable ? extends @Nullable Object, @Nullable ? super @Nullable String> m){}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					void foo(Map<@Nullable ? super @Nullable Object, @Nullable ? extends @Nullable String> m){}
					         ^^^
				Map cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 6)
					void goo(Map<@Nullable ? extends @Nullable Object, @Nullable ? super @Nullable String> m){}
					         ^^^
				Map cannot be resolved to a type
				----------
				""",
			this.LIBS,
			true/*flush*/);
	}

	// storing and decoding null-type-annotations to/from classfile: RETURN_TYPE
	public void testBinary01() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		runConformTestWithLibs(
				new String[] {
					"p/X.java",
					"""
						package p;
						import java.util.List;
						import org.eclipse.jdt.annotation.*;
						public class X {
							public List<@Nullable String> getSomeStrings() { return null; }
						}
						"""
				},
				customOptions,
				"");
		runNegativeTestWithLibs(
				new String[] {
					"Y.java",
					"""
						import p.X;
						public class Y {
							public void test(X x) {
								String s0 = x.getSomeStrings().get(0);
								System.out.println(s0.toUpperCase());
							}
						}
						"""
				},
				customOptions,
				"""
					----------
					1. ERROR in Y.java (at line 5)
						System.out.println(s0.toUpperCase());
						                   ^^
					Potential null pointer access: The variable s0 may be null at this location
					----------
					"""
				);
	}

	// storing and decoding null-type-annotations to/from classfile: METHOD_FORMAL_PARAMETER & METHOD_RECEIVER
	// Note: receiver annotation is not evaluated by the compiler, this part of the test only serves debugging purposes.
	public void testBinary02() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		runConformTestWithLibs(
				new String[] {
					"p/X.java",
					"""
						package p;
						import java.util.List;
						import org.eclipse.jdt.annotation.*;
						import static java.lang.annotation.ElementType.*;
						import java.lang.annotation.*;
						@Retention(RetentionPolicy.CLASS)
						@Target(TYPE_USE)
						@interface Immutable {}
						public class X {
							public void setAllStrings(@Immutable X this, int dummy, List<@NonNull String> ss) { }
						}
						"""
				},
				customOptions,
				"");
		runNegativeTestWithLibs(
				new String[] {
					"Y.java",
					"""
						import p.X;
						import java.util.List;
						import org.eclipse.jdt.annotation.*;
						public class Y {
							public void test(X x, List<@Nullable String> ss) {
								x.setAllStrings(-1, ss);
							}
						}
						"""
				},
				customOptions,
				"""
					----------
					1. ERROR in Y.java (at line 6)
						x.setAllStrings(-1, ss);
						                    ^^
					Null type mismatch (type annotations): required \'List<@NonNull String>\' but this expression has type \'List<@Nullable String>\'
					----------
					"""
				);
	}

	// storing and decoding null-type-annotations to/from classfile: FIELD
	public void testBinary03() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		customOptions.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.IGNORE);
		runConformTestWithLibs(
				new String[] {
					"p/X1.java",
					"""
						package p;
						import org.eclipse.jdt.annotation.*;
						public abstract class X1 {
							public static String @Nullable [] f1 = null;
							public static String [] @Nullable [] f2 = new String[] @Nullable[] { null };
						}
						"""
				},
				customOptions,
				"");
		runNegativeTestWithLibs(
				new String[] {
					"Y1.java",
					"""
						import p.X1;
						public class Y1 {
							public void test() {
								System.out.println(p.X1.f1.length);
								System.out.println(X1.f2[0].length);
							}
						}
						"""
				},
				customOptions,
				"""
					----------
					1. ERROR in Y1.java (at line 4)
						System.out.println(p.X1.f1.length);
						                        ^^
					Potential null pointer access: this expression has a \'@Nullable\' type
					----------
					2. ERROR in Y1.java (at line 5)
						System.out.println(X1.f2[0].length);
						                   ^^^^^^^^
					Potential null pointer access: array element may be null
					----------
					"""
				);
	}

	// storing and decoding null-type-annotations to/from classfile: SUPER_TYPE
	public void testBinary04() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		runConformTestWithLibs(
				new String[] {
					"p/X1.java",
					"""
						package p;
						import java.util.ArrayList;
						import org.eclipse.jdt.annotation.*;
						@SuppressWarnings("serial")
						public abstract class X1 extends ArrayList<@Nullable String> {
						}
						""",
					"p/X2.java",
					"""
						package p;
						import java.util.List;
						import org.eclipse.jdt.annotation.*;
						public abstract class X2 implements List<@Nullable String> {
						}
						"""
				},
				customOptions,
				"");
		runNegativeTestWithLibs(
				new String[] {
					"Y1.java",
					"""
						import p.X1;
						public class Y1 {
							public void test(X1 x) {
								String s0 = x.get(0);
								System.out.println(s0.toUpperCase());
							}
						}
						""",
					"Y2.java",
					"""
						import p.X2;
						public class Y2 {
							public void test(X2 x) {
								String s0 = x.get(0);
								System.out.println(s0.toUpperCase());
							}
						}
						"""
				},
				customOptions,
				"""
					----------
					1. ERROR in Y1.java (at line 5)
						System.out.println(s0.toUpperCase());
						                   ^^
					Potential null pointer access: The variable s0 may be null at this location
					----------
					----------
					1. ERROR in Y2.java (at line 5)
						System.out.println(s0.toUpperCase());
						                   ^^
					Potential null pointer access: The variable s0 may be null at this location
					----------
					"""
				);
	}

	// storing and decoding null-type-annotations to/from classfile: CLASS_TYPE_PARAMETER & METHOD_TYPE_PARAMETER
	public void testBinary05() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		runConformTestWithLibs(
				new String[] {
					"p/X1.java",
					"""
						package p;
						import java.util.ArrayList;
						import org.eclipse.jdt.annotation.*;
						@SuppressWarnings("serial")
						public abstract class X1<@NonNull T> extends ArrayList<T> {
						    public <@Nullable S> void foo(S s) {}
						}
						"""
				},
				customOptions,
				"");
		runNegativeTestWithLibs(
				new String[] {
					"Y1.java",
					"""
						import p.X1;
						import org.eclipse.jdt.annotation.*;
						public class Y1 {
							X1<@Nullable String> maybeStrings;
							void test(X1<@NonNull String> x) {
								x.<@NonNull Object>foo(new Object());
							}
						}
						"""
				},
				customOptions,
				"""
					----------
					1. ERROR in Y1.java (at line 4)
						X1<@Nullable String> maybeStrings;
						   ^^^^^^^^^^^^^^^^
					Null constraint mismatch: The type \'@Nullable String\' is not a valid substitute for the type parameter \'@NonNull T extends Object\'
					----------
					2. ERROR in Y1.java (at line 6)
						x.<@NonNull Object>foo(new Object());
						   ^^^^^^^^^^^^^^^
					Null constraint mismatch: The type \'@NonNull Object\' is not a valid substitute for the type parameter \'@Nullable S extends Object\'
					----------
					""");
	}

	// storing and decoding null-type-annotations to/from classfile: CLASS_TYPE_PARAMETER_BOUND & METHOD_TYPE_PARAMETER_BOUND
	public void testBinary06() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		runNegativeTestWithLibs(
				new String[] {
					"p/X1.java",
					"""
						package p;
						import java.util.ArrayList;
						import org.eclipse.jdt.annotation.*;
						@SuppressWarnings("serial")
						public abstract class X1<T extends @NonNull Object> extends ArrayList<T> {
						    public <U, V extends @Nullable Object> void foo(U u, V v) {}
						}
						""",
					"p/X2.java",
					"""
						package p;
						import org.eclipse.jdt.annotation.*;
						public class X2<@NonNull W extends @Nullable Object> {}
						"""
				},
				customOptions,
				"""
					----------
					1. ERROR in p\\X2.java (at line 3)
						public class X2<@NonNull W extends @Nullable Object> {}
						                                   ^^^^^^^^^
					This nullness annotation conflicts with a \'@NonNull\' annotation which is effective on the same type parameter\s
					----------
					""");
		// fix the bug:
		runConformTestWithLibs(
				new String[] {
					"p/X1.java",
					"""
						package p;
						import java.util.ArrayList;
						import org.eclipse.jdt.annotation.*;
						@SuppressWarnings("serial")
						public abstract class X1<T extends @NonNull Object> extends ArrayList<T> {
						    public <U, V extends @Nullable Object> void foo(U u, V v) {}
						}
						""",
					"p/X2.java",
					"""
						package p;
						import org.eclipse.jdt.annotation.*;
						public class X2<@Nullable W extends Object> {}
						"""
				},
				customOptions,
				"");
		runNegativeTestWithLibs(
				new String[] {
					"Y1.java",
					"""
						import p.X1;
						import p.X2;
						import org.eclipse.jdt.annotation.*;
						public class Y1 {
							X1<@Nullable String> maybeStrings;
						   X2<@NonNull String> strings;
							void test(X1<@NonNull String> x) {
								x.<Y1, @NonNull Object>foo(this, new Object());
							}
						}
						"""
				},
				customOptions,
				"""
					----------
					1. ERROR in Y1.java (at line 5)
						X1<@Nullable String> maybeStrings;
						   ^^^^^^^^^^^^^^^^
					Null constraint mismatch: The type \'@Nullable String\' is not a valid substitute for the type parameter \'T extends @NonNull Object\'
					----------
					2. ERROR in Y1.java (at line 6)
						X2<@NonNull String> strings;
						   ^^^^^^^^^^^^^^^
					Null constraint mismatch: The type \'@NonNull String\' is not a valid substitute for the type parameter \'@Nullable W extends Object\'
					----------
					"""
				);
	}

	// storing and decoding null-type-annotations to/from classfile: CLASS_TYPE_PARAMETER_BOUND & METHOD_TYPE_PARAMETER_BOUND
	// variant: qualified type references
	public void testBinary06b() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		runNegativeTestWithLibs(
				new String[] {
					"p/X1.java",
					"""
						package p;
						import java.util.ArrayList;
						import org.eclipse.jdt.annotation.*;
						@SuppressWarnings("serial")
						public abstract class X1<T extends java.lang.@NonNull Object> extends ArrayList<T> {
						    public <U, V extends java.lang.@Nullable Object> void foo(U u, V v) {}
						}
						""",
					"p/X2.java",
					"""
						package p;
						import org.eclipse.jdt.annotation.*;
						public class X2<@NonNull W extends java.lang.@Nullable Object> {}
						"""
				},
				customOptions,
				"""
					----------
					1. ERROR in p\\X2.java (at line 3)
						public class X2<@NonNull W extends java.lang.@Nullable Object> {}
						                                             ^^^^^^^^^
					This nullness annotation conflicts with a \'@NonNull\' annotation which is effective on the same type parameter\s
					----------
					""");
		// fix the bug:
		runConformTestWithLibs(
				new String[] {
					"p/X1.java",
					"""
						package p;
						import java.util.ArrayList;
						import org.eclipse.jdt.annotation.*;
						@SuppressWarnings("serial")
						public abstract class X1<T extends java.lang.@NonNull Object> extends ArrayList<T> {
						    public <U, V extends java.lang.@Nullable Object> void foo(U u, V v) {}
						}
						""",
					"p/X2.java",
					"""
						package p;
						import org.eclipse.jdt.annotation.*;
						public class X2<@Nullable W extends Object> {}
						"""
				},
				customOptions,
				"");
		runNegativeTestWithLibs(
				new String[] {
					"Y1.java",
					"""
						import org.eclipse.jdt.annotation.*;
						public class Y1 {
							p.X1<java.lang.@Nullable String> maybeStrings;
						   p.X2<java.lang.@NonNull String> strings;
							void test(p.X1<java.lang.@NonNull String> x) {
								x.<Y1, java.lang.@NonNull Object>foo(this, new Object());
							}
						}
						"""
				},
				customOptions,
				"""
					----------
					1. ERROR in Y1.java (at line 3)
						p.X1<java.lang.@Nullable String> maybeStrings;
						     ^^^^^^^^^^^^^^^^^^^^^^^^^^
					Null constraint mismatch: The type \'@Nullable String\' is not a valid substitute for the type parameter \'T extends @NonNull Object\'
					----------
					2. ERROR in Y1.java (at line 4)
						p.X2<java.lang.@NonNull String> strings;
						     ^^^^^^^^^^^^^^^^^^^^^^^^^
					Null constraint mismatch: The type \'@NonNull String\' is not a valid substitute for the type parameter \'@Nullable W extends Object\'
					----------
					"""
				);
	}

	// storing and decoding null-type-annotations to/from classfile: method with all kinds of type annotations
	public void testBinary07() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		customOptions.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.IGNORE);
		runConformTestWithLibs(
				new String[] {
					"p/List.java",
					"""
						package p;
						@org.eclipse.jdt.annotation.NonNullByDefault
						public interface List<T> {
							T get(int i);
						}
						""",
					"p/X1.java",
					"""
						package p;
						import java.util.Map;
						import org.eclipse.jdt.annotation.*;
						import static java.lang.annotation.ElementType.*;
						import java.lang.annotation.*;
						@Retention(RetentionPolicy.CLASS)
						@Target(TYPE_USE)
						@interface Immutable {}
						public abstract class X1 {
						    public <@NonNull U, V extends @Nullable Object> List<@NonNull Map<Object, @NonNull String>> foo(@Immutable X1 this, U u, V v) { return null; }
						}
						"""
				},
				customOptions,
				"");
		runNegativeTestWithLibs(
				new String[] {
					"Y1.java",
					"""
						import p.X1;
						import org.eclipse.jdt.annotation.*;
						public class Y1 {
							void test(X1 x) {
								x.<@NonNull Y1, @NonNull Object>foo(this, new Object())
									.get(0).put(null, null);
							}
						}
						"""
				},
				customOptions,
				"""
					----------
					1. ERROR in Y1.java (at line 6)
						.get(0).put(null, null);
						                  ^^^^
					Null type mismatch: required \'@NonNull String\' but the provided value is null
					----------
					""");
	}

	// storing and decoding null-type-annotations to/from classfile: details
	public void testBinary08() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		customOptions.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.IGNORE);
		runNegativeTestWithLibs(
				new String[] {
					"p/X1.java",
					"""
						package p;
						import java.util.*;
						import org.eclipse.jdt.annotation.*;
						public abstract class X1 {
						    public class Inner {}
						    public Object []@NonNull[] arrays(Object @NonNull[][] oa1) { return null; }
						    public void nesting(@NonNull Inner i1, X1.@Nullable Inner i2) { }
						    public void wildcard1(List<@Nullable ? extends @NonNull X1> l) { } // contradiction
						    public void wildcard2(List<? super @NonNull X1> l) { }
						}
						"""
				},
				customOptions,
				"""
					----------
					1. ERROR in p\\X1.java (at line 8)
						public void wildcard1(List<@Nullable ? extends @NonNull X1> l) { } // contradiction
						                                               ^^^^^^^^
					This nullness annotation conflicts with a \'@Nullable\' annotation which is effective on the same type parameter\s
					----------
					""");
		// fix the error:
		runConformTestWithLibs(
				new String[] {
					"p/X1.java",
					"""
						package p;
						import java.util.*;
						import org.eclipse.jdt.annotation.*;
						public abstract class X1 {
						    public class Inner {}
						    public Object []@NonNull[] arrays(Object @NonNull[][] oa1) { return null; }
						    public void nesting(@NonNull Inner i1, X1.@Nullable Inner i2) { }
						    public void wildcard1(List<@Nullable ? extends X1> l) { }
						    public void wildcard2(List<? super @NonNull X1> l) { }
						}
						"""
				},
				customOptions,
				"");

		runNegativeTestWithLibs(
				new String[] {
					"Y1.java",
					"""
						import p.X1;
						import org.eclipse.jdt.annotation.*;
						import java.util.*;
						public class Y1 {
							void test(X1 x) {
								Object @NonNull[][] a = new Object[0][]; // safe: new is never null
								x.arrays(a)[0] = null; // illegal
								x.nesting(null, null); // 1st null is illegal
								x.wildcard2(new ArrayList<@NonNull Object>());
								x.wildcard2(new ArrayList<@Nullable Object>()); // OK
								x.wildcard1(new ArrayList<@NonNull X1>()); // incompatible
							}
						}
						"""
				},
				customOptions,
				"""
					----------
					1. ERROR in Y1.java (at line 7)
						x.arrays(a)[0] = null; // illegal
						                 ^^^^
					Null type mismatch: required \'Object @NonNull[]\' but the provided value is null
					----------
					2. ERROR in Y1.java (at line 8)
						x.nesting(null, null); // 1st null is illegal
						          ^^^^
					Null type mismatch: required \'X1.@NonNull Inner\' but the provided value is null
					----------
					3. ERROR in Y1.java (at line 11)
						x.wildcard1(new ArrayList<@NonNull X1>()); // incompatible
						            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Null type mismatch (type annotations): required \'List<@Nullable ? extends X1>\' but this expression has type \'@NonNull ArrayList<@NonNull X1>\', corresponding supertype is \'List<@NonNull X1>\'
					----------
					""");
	}

	// storing and decoding null-type-annotations to/from classfile: details
	// variant: qualified references
	public void testBinary08b() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		customOptions.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.IGNORE);
		runNegativeTestWithLibs(
				new String[] {
					"p/X1.java",
					"""
						package p;
						import org.eclipse.jdt.annotation.*;
						public abstract class X1 {
						    public class Inner {}
						    public java.lang.Object []@NonNull[] arrays(java.lang.Object @NonNull[][] oa1) { return null; }
						    public void nesting(@NonNull Inner i1, X1.@Nullable Inner i2) { }
						    public void wildcard1(java.util.List<@Nullable ? extends p.@NonNull X1> l) { } // contradiction
						    public void wildcard2(java.util.List<? super p.@NonNull X1> l) { }
						}
						"""
				},
				customOptions,
				"""
					----------
					1. ERROR in p\\X1.java (at line 7)
						public void wildcard1(java.util.List<@Nullable ? extends p.@NonNull X1> l) { } // contradiction
						                                                           ^^^^^^^^
					This nullness annotation conflicts with a \'@Nullable\' annotation which is effective on the same type parameter\s
					----------
					""");
		// fix the error:
		runConformTestWithLibs(
				new String[] {
					"p/X1.java",
					"""
						package p;
						import java.util.*;
						import org.eclipse.jdt.annotation.*;
						public abstract class X1 {
						    public class Inner {}
						    public java.lang.Object []@NonNull[] arrays(java.lang.Object @NonNull[][] oa1) { return null; }
						    public void nesting(@NonNull Inner i1, p.X1.@Nullable Inner i2) { }
						    public void wildcard1(List<@Nullable ? extends p.X1> l) { }
						    public void wildcard2(List<? super p.@NonNull X1> l) { }
						}
						"""
				},
				customOptions,
				"");

		runNegativeTestWithLibs(
				new String[] {
					"Y1.java",
					"""
						import p.X1;
						import org.eclipse.jdt.annotation.*;
						import java.util.*;
						public class Y1 {
							void test(X1 x) {
								java.lang.Object @NonNull[][] a = new java.lang.Object[0][]; // safe: new is never null
								x.arrays(a)[0] = null; // illegal
								x.nesting(null, null); // 1st null is illegal
								x.wildcard2(new ArrayList<java.lang.@NonNull Object>());
								x.wildcard2(new ArrayList<java.lang.@Nullable Object>());
								x.wildcard1(new ArrayList<p.@NonNull X1>()); // incompatible
							}
						}
						"""
				},
				customOptions,
				"""
					----------
					1. ERROR in Y1.java (at line 7)
						x.arrays(a)[0] = null; // illegal
						                 ^^^^
					Null type mismatch: required \'Object @NonNull[]\' but the provided value is null
					----------
					2. ERROR in Y1.java (at line 8)
						x.nesting(null, null); // 1st null is illegal
						          ^^^^
					Null type mismatch: required \'X1.@NonNull Inner\' but the provided value is null
					----------
					3. ERROR in Y1.java (at line 11)
						x.wildcard1(new ArrayList<p.@NonNull X1>()); // incompatible
						            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Null type mismatch (type annotations): required \'List<@Nullable ? extends X1>\' but this expression has type \'@NonNull ArrayList<@NonNull X1>\', corresponding supertype is \'List<@NonNull X1>\'
					----------
					""");
	}

	// storing and decoding null-type-annotations to/from classfile: EXTENDED DIMENSIONS.
	public void testBinary09() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		runNegativeTestWithLibs(
				new String[] {
					"X.java",
					"""
						import org.eclipse.jdt.annotation.NonNull;
						import org.eclipse.jdt.annotation.Nullable;
						public class X {
							@NonNull String @Nullable [] f @NonNull [] = null;
							static void foo(@NonNull String @Nullable [] p @NonNull []) {
								p = null;
								@NonNull String @Nullable [] l @NonNull [] = null;
							}
						}
						"""

				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						@NonNull String @Nullable [] f @NonNull [] = null;
						                                             ^^^^
					Null type mismatch: required \'@NonNull String @NonNull[] @Nullable[]\' but the provided value is null
					----------
					2. ERROR in X.java (at line 6)
						p = null;
						    ^^^^
					Null type mismatch: required \'@NonNull String @NonNull[] @Nullable[]\' but the provided value is null
					----------
					3. ERROR in X.java (at line 7)
						@NonNull String @Nullable [] l @NonNull [] = null;
						                                             ^^^^
					Null type mismatch: required \'@NonNull String @NonNull[] @Nullable[]\' but the provided value is null
					----------
					""");
		// fix the error:
		runConformTestWithLibs(
				new String[] {
						"X.java",
						"""
							import org.eclipse.jdt.annotation.NonNull;
							import org.eclipse.jdt.annotation.Nullable;
							public class X {
								@NonNull String @Nullable [] f @NonNull [] = new @NonNull String @NonNull [0] @Nullable [];
							}
							"""
				},
				customOptions,
				"");

		runNegativeTestWithLibs(
				new String[] {
					"Y.java",
					"""
						import org.eclipse.jdt.annotation.*;
						public class Y {
							void test(X x) {
						       x.f = null;
						       x.f[0] = null;
						       x.f[0][0] = null;
							}
						}
						"""
				},
				customOptions,
				"""
					----------
					1. WARNING in Y.java (at line 1)
						import org.eclipse.jdt.annotation.*;
						       ^^^^^^^^^^^^^^^^^^^^^^^^^^
					The import org.eclipse.jdt.annotation is never used
					----------
					2. ERROR in Y.java (at line 4)
						x.f = null;
						      ^^^^
					Null type mismatch: required \'@NonNull String @NonNull[] @Nullable[]\' but the provided value is null
					----------
					3. ERROR in Y.java (at line 6)
						x.f[0][0] = null;
						^^^^^^
					Potential null pointer access: array element may be null
					----------
					4. ERROR in Y.java (at line 6)
						x.f[0][0] = null;
						            ^^^^
					Null type mismatch: required \'@NonNull String\' but the provided value is null
					----------
					""");
}

	// storing and decoding null-type-annotations to/from classfile: array annotations.
	public void testBinary10() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		runNegativeTestWithLibs(
				new String[] {
					"X.java",
					"""
						import java.util.ArrayList;
						import org.eclipse.jdt.annotation.NonNull;
						public class X  {
							void foo(ArrayList<String> @NonNull [] p) {
							}
						}
						class Y extends X {
							void foo() {
								super.foo(null);
							}
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 9)
						super.foo(null);
						          ^^^^
					Null type mismatch: required \'ArrayList<String> @NonNull[]\' but the provided value is null
					----------
					""");
		// fix the error:
		runConformTestWithLibs(
				new String[] {
						"X.java",
						"""
							import java.util.ArrayList;
							import org.eclipse.jdt.annotation.NonNull;
							public class X  {
								void foo(ArrayList<String> @NonNull [] p) {
								}
							}
							"""
				},
				customOptions,
				"");

		runNegativeTestWithLibs(
				new String[] {
					"Y.java",
					"""
						public class Y extends X {
							void foo() {
								super.foo(null);
							}
						}
						"""
				},
				customOptions,
				"""
					----------
					1. ERROR in Y.java (at line 3)
						super.foo(null);
						          ^^^^
					Null type mismatch: required \'ArrayList<String> @NonNull[]\' but the provided value is null
					----------
					""");
	}

	public void testConditional1() {
		runWarningTestWithLibs(
			true/*flush*/,
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					import java.util.*;
					public class X {
						List<@NonNull String> foo(List<@NonNull String> good, List<String> dubious, int f) {
							if (f < 2)
								return f == 0 ? good : dubious;
							if (f < 4)
								return f == 2 ? dubious : good;
							if (f < 6)
								return f == 4 ? good : good;
							return null;
						}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. WARNING in X.java (at line 6)
					return f == 0 ? good : dubious;
					                       ^^^^^^^
				Null type safety (type annotations): The expression of type \'List<String>\' needs unchecked conversion to conform to \'List<@NonNull String>\'
				----------
				2. WARNING in X.java (at line 8)
					return f == 2 ? dubious : good;
					                ^^^^^^^
				Null type safety (type annotations): The expression of type \'List<String>\' needs unchecked conversion to conform to \'List<@NonNull String>\'
				----------
				""");
	}

	public void testConditional2() {
		runWarningTestWithLibs(
			true/*flush*/,
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					import java.util.*;
					public class X {
						List<@NonNull String> foo(List<@NonNull String> good, ArrayList<String> dubious, int f) {
							if (f < 2)
								return f == 0 ? good : dubious;
							if (f < 4)
								return f == 2 ? dubious : good;
							if (f < 6)
								return f == 4 ? good : good;
							return null;
						}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. WARNING in X.java (at line 6)
					return f == 0 ? good : dubious;
					                       ^^^^^^^
				Null type safety (type annotations): The expression of type \'ArrayList<String>\' needs unchecked conversion to conform to \'List<@NonNull String>\', corresponding supertype is 'List<String>'
				----------
				2. WARNING in X.java (at line 8)
					return f == 2 ? dubious : good;
					                ^^^^^^^
				Null type safety (type annotations): The expression of type \'ArrayList<String>\' needs unchecked conversion to conform to \'List<@NonNull String>\', corresponding supertype is 'List<String>'
				----------
				""");
	}

	// conditional in argument position
	public void testConditional3() {
		runWarningTestWithLibs(
			true/*flush*/,
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					import java.util.*;
					public class X {
						void foo(List<@NonNull String> good, List<String> dubious, int f) {
							consume(f == 0 ? good : dubious);
							consume(f == 2 ? dubious : good);
							consume(f == 4 ? good : good);
						}
						void consume(List<@NonNull String> strings) {}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. WARNING in X.java (at line 5)
					consume(f == 0 ? good : dubious);
					                        ^^^^^^^
				Null type safety (type annotations): The expression of type \'List<String>\' needs unchecked conversion to conform to \'List<@NonNull String>\'
				----------
				2. WARNING in X.java (at line 6)
					consume(f == 2 ? dubious : good);
					                 ^^^^^^^
				Null type safety (type annotations): The expression of type \'List<String>\' needs unchecked conversion to conform to \'List<@NonNull String>\'
				----------
				""");
	}

	// types with null annotations on details (type parameter) are compatible to equal types
	public void testCompatibility1() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					import java.util.*;
					public class X {
						List<@NonNull String> return1(List<@NonNull String> noNulls) {
							return noNulls;
						}
						List<@Nullable String> return2(List<@Nullable String> withNulls) {
							return withNulls;
						}
						void assigns(List<@NonNull String> noNulls, List<String> dubious, List<@Nullable String> withNulls) {
							List<@NonNull String> l1 = noNulls;
							List<@Nullable String> l2 = withNulls;
							List<String> l3 = dubious;
						}
						void arguments(List<@NonNull String> noNulls, List<String> dubious, List<@Nullable String> withNulls) {
							assigns(noNulls, dubious, withNulls);
						}
					}
					"""
			},
			getCompilerOptions(),
			"");
	}

	// types with null annotations on details (array content) are compatible to equal types
	public void testCompatibility1a() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					public class X {
						@NonNull String[] return1(@NonNull String[] noNulls) {
							return noNulls;
						}
						@Nullable String[] return2(@Nullable String[] noNulls) {
							return noNulls;
						}
						void assigns(@NonNull String[] noNulls, String dubious[], @Nullable String[] withNulls) {
							@NonNull String[] l1 = noNulls;
							@Nullable String[] l2 = withNulls;
							String[] l3 = dubious;
						}
						void arguments(@NonNull String[] noNulls, String[] dubious, @Nullable String[] withNulls) {
							assigns(noNulls, dubious, withNulls);
						}
					}
					"""
			},
			getCompilerOptions(),
			"");
	}

	// types with null annotations on details (type parameter) are compatible to types lacking the annotation
	public void testCompatibility2() {
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_ANNOTATED_TYPE_ARGUMENT_TO_UNANNOTATED, JavaCore.IGNORE);
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					import java.util.*;
					public class X {
						List<String> return1(List<@NonNull String> noNulls) {
							return noNulls;
						}
						List<String> return2(List<String> dubious) {
							return dubious;
						}
						List<String> return3(List<@Nullable String> withNulls) {
							return withNulls;
						}
						void assigns(List<@NonNull String> noNulls, List<String> dubious, List<@Nullable String> withNulls) {
							List<String> l1 = noNulls;
							List<String> l2 = dubious;
							List<String> l3 = withNulls;
						}
						void arguments(List<@NonNull String> noNulls, List<String> dubious, List<@Nullable String> withNulls) {
							takeAny(noNulls);
							takeAny(dubious);
							takeAny(withNulls);
						}
						void takeAny(List<String> any) {}
					}
					"""
			},
			options,
			"");
	}

	// types with null annotations on details (array content) are compatible to types lacking the annotation
	public void testCompatibility2a() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					public class X {
						String[] return1(@NonNull String[] noNulls) {
							return noNulls;
						}
						String[] return2(String[] dubious) {
							return dubious;
						}
						String[] return3(@Nullable String[] withNulls) {
							return withNulls;
						}
						void assigns(@NonNull String[] noNulls, String[] dubious, @Nullable String[] withNulls) {
							String[] l1 = noNulls;
							String[] l2 = dubious;
							String[] l3 = withNulls;
						}
						void arguments(@NonNull String[] noNulls, String[] dubious, @Nullable String[] withNulls) {
							takeAny(noNulls);
							takeAny(dubious);
							takeAny(withNulls);
						}
						void takeAny(String[] any) {}
					}
					"""
			},
			getCompilerOptions(),
			"");
	}

	// types without null annotations are converted (unsafe) to types with detail annotations (type parameter)
	public void testCompatibility3() {
		runWarningTestWithLibs(
			true/*flush*/,
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					import java.util.*;
					public class X {
						List<@NonNull String> return1(List<String> dubious) {
							return dubious;
						}
						List<@Nullable String> return2(List<String> dubious) {
							return dubious;
						}
						void assigns(List<String> dubious) {
							List<@Nullable String> l1 = dubious;
							List<@NonNull String> l2 = dubious;
						}
						void arguments(List<String> dubious) {
							acceptNulls(dubious);
							acceptNoNulls(dubious);
						}
						void acceptNulls(List<@NonNull String> noNulls) {}
						void acceptNoNulls(List<@NonNull String> noNulls) {}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. WARNING in X.java (at line 5)
					return dubious;
					       ^^^^^^^
				Null type safety (type annotations): The expression of type \'List<String>\' needs unchecked conversion to conform to \'List<@NonNull String>\'
				----------
				2. WARNING in X.java (at line 8)
					return dubious;
					       ^^^^^^^
				Null type safety (type annotations): The expression of type \'List<String>\' needs unchecked conversion to conform to \'List<@Nullable String>\'
				----------
				3. WARNING in X.java (at line 11)
					List<@Nullable String> l1 = dubious;
					                            ^^^^^^^
				Null type safety (type annotations): The expression of type \'List<String>\' needs unchecked conversion to conform to \'List<@Nullable String>\'
				----------
				4. WARNING in X.java (at line 12)
					List<@NonNull String> l2 = dubious;
					                           ^^^^^^^
				Null type safety (type annotations): The expression of type \'List<String>\' needs unchecked conversion to conform to \'List<@NonNull String>\'
				----------
				5. WARNING in X.java (at line 15)
					acceptNulls(dubious);
					            ^^^^^^^
				Null type safety (type annotations): The expression of type \'List<String>\' needs unchecked conversion to conform to \'List<@NonNull String>\'
				----------
				6. WARNING in X.java (at line 16)
					acceptNoNulls(dubious);
					              ^^^^^^^
				Null type safety (type annotations): The expression of type \'List<String>\' needs unchecked conversion to conform to \'List<@NonNull String>\'
				----------
				""");
	}

	// types without null annotations are converted (unsafe) to types with detail annotations (array content)
	public void testCompatibility3a() {
		runWarningTestWithLibs(
			true/*flush*/,
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					public class X {
						@NonNull String[] return1(String[] dubious) {
							return dubious;
						}
						@Nullable String[] return2(String[] dubious) {
							return dubious;
						}
						void assigns(String[] dubious) {
							@Nullable String[] l1 = dubious;
							@NonNull String[] l2 = dubious;
						}
						void arguments(String[] dubious) {
							acceptNulls(dubious);
							acceptNoNulls(dubious);
						}
						void acceptNulls(@Nullable String[] withNulls) {}
						void acceptNoNulls(@NonNull String[] noNulls) {}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. WARNING in X.java (at line 4)
					return dubious;
					       ^^^^^^^
				Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'@NonNull String []\'
				----------
				2. WARNING in X.java (at line 7)
					return dubious;
					       ^^^^^^^
				Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'@Nullable String []\'
				----------
				3. WARNING in X.java (at line 10)
					@Nullable String[] l1 = dubious;
					                        ^^^^^^^
				Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'@Nullable String []\'
				----------
				4. WARNING in X.java (at line 11)
					@NonNull String[] l2 = dubious;
					                       ^^^^^^^
				Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'@NonNull String []\'
				----------
				5. WARNING in X.java (at line 14)
					acceptNulls(dubious);
					            ^^^^^^^
				Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'@Nullable String []\'
				----------
				6. WARNING in X.java (at line 15)
					acceptNoNulls(dubious);
					              ^^^^^^^
				Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'@NonNull String []\'
				----------
				""");
	}

	// types with null annotations on details (type parameter) are incompatible to opposite types
	public void testCompatibility4() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					import java.util.*;
					public class X {
						List<@Nullable String> return1(List<@NonNull String> noNulls) {
							return noNulls;
						}
						List<@NonNull String> return2(List<@Nullable String> withNulls) {
							return withNulls;
						}
						void assigns(List<@NonNull String> noNulls, List<@Nullable String> withNulls) {
							List<@NonNull String> l1 = withNulls;
							List<@Nullable String> l2 = noNulls;
						}
						void arguments(List<@NonNull String> noNulls, List<@Nullable String> withNulls) {
							assigns(withNulls, noNulls);
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					return noNulls;
					       ^^^^^^^
				Null type mismatch (type annotations): required 'List<@Nullable String>' but this expression has type 'List<@NonNull String>'
				----------
				2. ERROR in X.java (at line 8)
					return withNulls;
					       ^^^^^^^^^
				Null type mismatch (type annotations): required 'List<@NonNull String>' but this expression has type 'List<@Nullable String>'
				----------
				3. ERROR in X.java (at line 11)
					List<@NonNull String> l1 = withNulls;
					                           ^^^^^^^^^
				Null type mismatch (type annotations): required 'List<@NonNull String>' but this expression has type 'List<@Nullable String>'
				----------
				4. ERROR in X.java (at line 12)
					List<@Nullable String> l2 = noNulls;
					                            ^^^^^^^
				Null type mismatch (type annotations): required 'List<@Nullable String>' but this expression has type 'List<@NonNull String>'
				----------
				5. ERROR in X.java (at line 15)
					assigns(withNulls, noNulls);
					        ^^^^^^^^^
				Null type mismatch (type annotations): required 'List<@NonNull String>' but this expression has type 'List<@Nullable String>'
				----------
				6. ERROR in X.java (at line 15)
					assigns(withNulls, noNulls);
					                   ^^^^^^^
				Null type mismatch (type annotations): required 'List<@Nullable String>' but this expression has type 'List<@NonNull String>'
				----------
				""");
	}

	// types with null annotations on details (array content) are incompatible to opposite types
	public void testCompatibility4a() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					public class X {
						@Nullable String[] return1(@NonNull String[] noNulls) {
							return noNulls;
						}
						@NonNull String[] return2(@Nullable String[] withNulls) {
							return withNulls;
						}
						void assigns(@NonNull String[] noNulls, @Nullable String[] withNulls) {
							@NonNull String[] l1 = withNulls;
							@Nullable String[] l2 = noNulls;
						}
						void arguments(@NonNull String[] noNulls, @Nullable String[] withNulls) {
							assigns(withNulls, noNulls);
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					return noNulls;
					       ^^^^^^^
				Null type mismatch (type annotations): required \'@Nullable String []\' but this expression has type \'@NonNull String []\'
				----------
				2. ERROR in X.java (at line 7)
					return withNulls;
					       ^^^^^^^^^
				Null type mismatch (type annotations): required \'@NonNull String []\' but this expression has type \'@Nullable String []\'
				----------
				3. ERROR in X.java (at line 10)
					@NonNull String[] l1 = withNulls;
					                       ^^^^^^^^^
				Null type mismatch (type annotations): required \'@NonNull String []\' but this expression has type \'@Nullable String []\'
				----------
				4. ERROR in X.java (at line 11)
					@Nullable String[] l2 = noNulls;
					                        ^^^^^^^
				Null type mismatch (type annotations): required \'@Nullable String []\' but this expression has type \'@NonNull String []\'
				----------
				5. ERROR in X.java (at line 14)
					assigns(withNulls, noNulls);
					        ^^^^^^^^^
				Null type mismatch (type annotations): required \'@NonNull String []\' but this expression has type \'@Nullable String []\'
				----------
				6. ERROR in X.java (at line 14)
					assigns(withNulls, noNulls);
					                   ^^^^^^^
				Null type mismatch (type annotations): required \'@Nullable String []\' but this expression has type \'@NonNull String []\'
				----------
				""");
	}

	// challenge parameterized type with partial substitution of super's type parameters
	public void testCompatibility5() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import java.util.Map;
					
					import org.eclipse.jdt.annotation.*;
					
					abstract public class X<Y> implements Map<@NonNull String,Y> {
						void foo(X<Object> x) {
							Map<@NonNull String, Object> m1 = x; // OK
							Map<@Nullable String, Object> m2 = x; // NOK
						}
					}"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in X.java (at line 8)
					Map<@Nullable String, Object> m2 = x; // NOK
					                                   ^
				Null type mismatch (type annotations): required \'Map<@Nullable String,Object>\' but this expression has type \'X<Object>\', corresponding supertype is \'Map<@NonNull String,Object>\'
				----------
				""");
	}

	// challenge parameterized type with partial substitution of super's type parameters
	public void testCompatibility6() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import java.util.Map;
					
					import org.eclipse.jdt.annotation.*;
					
					abstract public class X<@Nullable Y> implements Map<@Nullable String,Y> {
						void foo(X<@Nullable Object> x) {
							Map<@Nullable String, @Nullable Object> m1 = x; // OK
							Map<@Nullable String, @NonNull Object> m2 = x; // NOK
						}
					}"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in X.java (at line 8)
					Map<@Nullable String, @NonNull Object> m2 = x; // NOK
					                                            ^
				Null type mismatch (type annotations): required \'Map<@Nullable String,@NonNull Object>\' but this expression has type \'X<@Nullable Object>\', corresponding supertype is \'Map<@Nullable String,@Nullable Object>\'
				----------
				""");
	}

	// illegal for type declaration
	public void testUnsupportedLocation01() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"public @NonNull class X {}\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public @NonNull class X {}
					       ^^^^^^^^
				The nullness annotation \'NonNull\' is not applicable at this location
				----------
				""");
	}

	// illegal for enclosing class (locations: field, argument, return type, local
	public void testUnsupportedLocation02() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					public class X {
					    class Inner {}
					    @NonNull X.Inner f;
					    @NonNull X.Inner foo(@NonNull X.Inner arg) {
					        @NonNull X.Inner local = arg;
					        return local;
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					@NonNull X.Inner f;
					^^^^^^^^
				The nullness annotation \'NonNull\' is not applicable at this location, it must be placed directly before the nested type name.
				----------
				2. ERROR in X.java (at line 5)
					@NonNull X.Inner foo(@NonNull X.Inner arg) {
					^^^^^^^^
				The nullness annotation \'NonNull\' is not applicable at this location, it must be placed directly before the nested type name.
				----------
				3. ERROR in X.java (at line 5)
					@NonNull X.Inner foo(@NonNull X.Inner arg) {
					                     ^^^^^^^^
				The nullness annotation \'NonNull\' is not applicable at this location, it must be placed directly before the nested type name.
				----------
				4. ERROR in X.java (at line 6)
					@NonNull X.Inner local = arg;
					^^^^^^^^
				The nullness annotation \'NonNull\' is not applicable at this location, it must be placed directly before the nested type name.
				----------
				""");
	}

	// illegal / unchecked for cast & instanceof with scalar type
	public void testUnsupportedLocation03() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					public class X {
					    @NonNull X foo(X arg) {
					        if (!(arg instanceof @NonNull X))
								return (@NonNull X)arg;
					        return arg;
					    }
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 4)
					if (!(arg instanceof @NonNull X))
					     ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The expression of type X is already an instance of type X
				----------
				2. ERROR in X.java (at line 4)
					if (!(arg instanceof @NonNull X))
					                     ^^^^^^^^^^
				Nullness annotations are not applicable at this location\s
				----------
				3. WARNING in X.java (at line 5)
					return (@NonNull X)arg;
					       ^^^^^^^^^^^^^^^
				Null type safety: Unchecked cast from X to @NonNull X
				----------
				""");
	}

	// illegal / unchecked for cast & instanceof with complex type
	public void testUnsupportedLocation04() {
		runNegativeTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import org.eclipse.jdt.annotation.*;
					import java.util.*;
					public class X {
					    List<@NonNull X> parameterized(List<X> arg) {
					        if (!(arg instanceof List<@NonNull X>))
								return (java.util.List<@NonNull X>)arg;
					        return arg;
					    }
					    X @NonNull[] arrays(X[] arg) {
					        if (!(arg instanceof X @NonNull[]))
								return (p.X @NonNull[])arg;
					        return arg;
					    }
						ArrayList<@NonNull String> foo(List<@NonNull String> l) {
							return (ArrayList<@NonNull String>) l;
						}\
						ArrayList<@NonNull String> foo2(List<@NonNull String> l) {
							return (ArrayList<String>) l;
						}\
					}
					"""
			},
			((this.complianceLevel >= ClassFileConstants.JDK16) ?
					"----------\n" +
					"1. WARNING in p\\X.java (at line 6)\n" +
					"	if (!(arg instanceof List<@NonNull X>))\n" +
					"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
					"The expression of type List<X> is already an instance of type List<X>\n"
					:
						"----------\n" +
						"1. ERROR in p\\X.java (at line 6)\n" +
						"	if (!(arg instanceof List<@NonNull X>))\n" +
						"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
						"Cannot perform instanceof check against parameterized type List<X>. Use the form List<?> instead since further generic type information will be erased at runtime\n"
					) +

			"----------\n" +
			"2. ERROR in p\\X.java (at line 6)\n" +
			"	if (!(arg instanceof List<@NonNull X>))\n" +
			"	                     ^^^^^^^^^^^^^^^^\n" +
			"Nullness annotations are not applicable at this location \n" +
			"----------\n" +
			"3. WARNING in p\\X.java (at line 7)\n" +
			"	return (java.util.List<@NonNull X>)arg;\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Null type safety: Unchecked cast from List<X> to List<@NonNull X>\n" +
			"----------\n" +
			"4. WARNING in p\\X.java (at line 11)\n" +
			"	if (!(arg instanceof X @NonNull[]))\n" +
			"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The expression of type X[] is already an instance of type X[]\n" +
			"----------\n" +
			"5. ERROR in p\\X.java (at line 11)\n" +
			"	if (!(arg instanceof X @NonNull[]))\n" +
			"	                     ^^^^^^^^^^^^\n" +
			"Nullness annotations are not applicable at this location \n" +
			"----------\n" +
			"6. WARNING in p\\X.java (at line 12)\n" +
			"	return (p.X @NonNull[])arg;\n" +
			"	       ^^^^^^^^^^^^^^^^^^^\n" +
			"Null type safety: Unchecked cast from X[] to X @NonNull[]\n" +
			"----------\n" +
			"7. WARNING in p\\X.java (at line 18)\n" +
			"	return (ArrayList<String>) l;\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'ArrayList<String>\' needs unchecked conversion to conform to \'ArrayList<@NonNull String>\'\n" +
			"----------\n",
			this.LIBS,
			true/*flush*/);
	}

	// illegal instanceof check with annotated type argument
	public void testUnsupportedLocation04a() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					import java.util.*;
					public class X {
						boolean instanceOf2(Object o) {
							return o instanceof List<@Nullable ?>;
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					return o instanceof List<@Nullable ?>;
					                    ^^^^^^^^^^^^^^^^^
				Nullness annotations are not applicable at this location\s
				----------
				""");
	}

	// illegal for allocation expression
	public void testUnsupportedLocation05() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					public class X {
						X x = new @NonNull X();
						class Inner {}
					   Inner i = this.new @Nullable Inner();
						java.util.List<@NonNull String> s = new java.util.ArrayList<@NonNull String>();
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					X x = new @NonNull X();
					          ^^^^^^^^
				The nullness annotation \'NonNull\' is not applicable at this location
				----------
				2. ERROR in X.java (at line 5)
					Inner i = this.new @Nullable Inner();
					                   ^^^^^^^^^
				The nullness annotation \'Nullable\' is not applicable at this location
				----------
				""");
	}

	// method receiver
	public void testUnsupportedLocation06() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					public class X {
						void receiver(@Nullable X this, Object o) {}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					void receiver(@Nullable X this, Object o) {}
					              ^^^^^^^^^^^
				Nullness annotations are not applicable at this location\s
				----------
				""");
	}

	// receiver type in method/constructor reference
	public void testUnsupportedLocation07() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					import java.util.function.Supplier;
					public class X {
						void consume(Supplier<Object> c) {}
						static Object supply() { return null; }
						void consumeSupplied() {
							consume(@NonNull X::supply);
							consume(@NonNull X::new);
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					consume(@NonNull X::supply);
					        ^^^^^^^^^^
				Nullness annotations are not applicable at this location\s
				----------
				2. ERROR in X.java (at line 8)
					consume(@NonNull X::new);
					        ^^^^^^^^^^
				Nullness annotations are not applicable at this location\s
				----------
				""");
	}

	// exceptions (throws & catch)
	public void testUnsupportedLocation08() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					import java.io.*;
					public class X {
						void throwsDecl() throws @Nullable IOException {}
						void excParam() {
							try {
								throwsDecl();
							} catch (@NonNull IOException ioe) {}
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					void throwsDecl() throws @Nullable IOException {}
					                         ^^^^^^^^^^^^^^^^^^^^^
				Nullness annotations are not applicable at this location\s
				----------
				2. ERROR in X.java (at line 8)
					} catch (@NonNull IOException ioe) {}
					                  ^^^^^^^^^^^
				Nullness annotations are not applicable at this location\s
				----------
				""");
	}

	public void testForeach() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					import java.util.*;
					public class X {
						void foo(List<@NonNull String> nns) {
							for(String s1 : nns) {
								logMsg(s1);
							}
							for(String s2 : getStrings()) {
								logMsg(s2);
							}
						}
						Collection<@Nullable String> getStrings() { return null; }
						void logMsg(@NonNull String msg) { }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					logMsg(s2);
					       ^^
				Null type mismatch: required \'@NonNull String\' but the provided value is inferred as @Nullable
				----------
				""");
	}

	// poly-null method
	public void testNullTypeInference1() {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportNonNullTypeVariableFromLegacyInvocation, CompilerOptions.IGNORE);
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					import java.util.*;
					public class X {
						<T> List<T> polyNullMethod(List<T> in) { return in; }
						@NonNull String test1(List<@NonNull String> strings) {
							 return polyNullMethod(strings).get(0);
						}
						@NonNull String test2(List<@Nullable String> strings) {
							 return polyNullMethod(strings).get(0);
						}
						@Nullable String test3(List<@NonNull String> strings) {
							 return polyNullMethod(strings).get(0);
						}
						@Nullable String test4(List<@Nullable String> strings) {
							 return polyNullMethod(strings).get(0);
						}
					}
					"""
			},
			compilerOptions,
			"""
				----------
				1. ERROR in X.java (at line 9)
					return polyNullMethod(strings).get(0);
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'
				----------
				""");
	}

	// functional interface with explicit nullness
	public void testNullTypeInference2a() {
		runNegativeTestWithLibs(
			new String[] {
				"PolyNull.java",
				"""
					import org.eclipse.jdt.annotation.*;
					
					interface NNFunc {
						@NonNull String a(@NonNull String i);
					}
					public class PolyNull {
						@NonNull String extract(NNFunc f, @NonNull String s) { return f.a(s); }
						@NonNull String testOK() {
							return extract(i -> i, "hallo");
						}
						@NonNull String testERR() {
							return extract(i -> null, "hallo"); // err
						}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in PolyNull.java (at line 12)
					return extract(i -> null, "hallo"); // err
					                    ^^^^
				Null type mismatch: required \'@NonNull String\' but the provided value is null
				----------
				""");
	}

	// functional interface with nullness inferred from target type with explicit nullness
	public void testNullTypeInference2b() {
		runNegativeTestWithLibs(
			new String[] {
				"PolyNull.java",
				"""
					import org.eclipse.jdt.annotation.*;
					
					interface Func<T>  {
						T a(T i);
					}
					public class PolyNull {
						@NonNull String extract(Func<@NonNull String> f, @NonNull String s) { return f.a(s); }
						@NonNull String testOK() {
							return extract(i -> i, "hallo");
						}
						@NonNull String testERR() {
							return extract(i -> null, "hallo"); // err
						}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in PolyNull.java (at line 12)
					return extract(i -> null, "hallo"); // err
					                    ^^^^
				Null type mismatch: required \'@NonNull String\' but the provided value is null
				----------
				""");
	}

	// functional interface with unspecified nullness matched against lambda parameter with explicit type & nullness
	public void testNullTypeInference2c() {
		runNegativeTestWithLibs(
			new String[] {
				"PolyNull.java",
				"""
					import org.eclipse.jdt.annotation.*;
					
					interface Func<T>  {
						T a(T i);
					}
					public class PolyNull {
						<X> X extract(Func<X> f, X s) { return f.a(s); }
						@NonNull String testOK() {
							return extract((@NonNull String i) -> i, "hallo");
						}
						@NonNull String testERR() {
							return extract((@NonNull String i) -> null, "hallo"); // err
						}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in PolyNull.java (at line 12)
					return extract((@NonNull String i) -> null, "hallo"); // err
					                                      ^^^^
				Null type mismatch: required \'@NonNull String\' but the provided value is null
				----------
				""");
	}

	// the only null annotation is on the target type, which propagates into the implicitly typed lambda argument
	public void testNullTypeInference2d() {
		runNegativeTestWithLibs(
			new String[] {
				"PolyNull.java",
				"""
					import org.eclipse.jdt.annotation.*;
					
					interface Func<T>  {
						T a(T i);
					}
					public class PolyNull {
						<X> X extract(Func<X> f, X s) { return f.a(s); }
						@NonNull String testOK() {
							return extract(i -> i, "hallo");
						}
						@NonNull String testERR() {
							return extract(i -> null, "hallo"); // err
						}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in PolyNull.java (at line 12)
					return extract(i -> null, "hallo"); // err
					                    ^^^^
				Null type mismatch: required \'@NonNull String\' but the provided value is null
				----------
				""");
	}

	// demonstrate that null annotations from the functional interface win, resulting in successful inference but null-safety issues
	public void testNullTypeInference2e() {
		runWarningTestWithLibs(
			true/*flush*/,
			new String[] {
				"PolyNull.java",
				"""
					import org.eclipse.jdt.annotation.*;
					
					interface Func<T>  {
						T a(T i);
					}
					public class PolyNull {
						String extract(Func<@Nullable String> f, @Nullable String s) { return f.a(s); }
						@NonNull String testWARN() {
							return extract(i -> null, "hallo"); // OK to pass null
						}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. WARNING in PolyNull.java (at line 9)
					return extract(i -> null, "hallo"); // OK to pass null
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'@NonNull String\'
				----------
				""");
	}

	// demonstrate that null annotations from the functional interface win, resulting in successful inference but null-safety issues
	public void testNullTypeInference2f() {
		runNegativeTestWithLibs(
			new String[] {
				"PolyNull.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"interface Func<T>  {\n" +
				"	T a(T i);\n" +
				"}\n" +
				"public class PolyNull {\n" +
				"	<X> X extract(Func<@Nullable X> f, @Nullable X s) { return f.a(s); }\n" +
				"	@NonNull String testERR() {\n" +
				"		return extract(i -> needNN(i), \"ola\");\n" +
				"	}\n" +
				"	@NonNull String needNN(@NonNull String s) { return \"\"; }\n" +
				"" +
				"}\n"
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in PolyNull.java (at line 7)
					<X> X extract(Func<@Nullable X> f, @Nullable X s) { return f.a(s); }
					                                                           ^^^^^^
				Null type mismatch (type annotations): required \'X\' but this expression has type \'@Nullable X\', where 'X' is a free type variable
				----------
				2. ERROR in PolyNull.java (at line 9)
					return extract(i -> needNN(i), "ola");
					                           ^
				Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'
				----------
				""");
	}

	// seemingly conflicting annotations from type variable application and type variable substitution
	// -> ignore @Nullable which overrides the type variable's nullness for this one location
	public void testNullTypeInference3() {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
		runNegativeTestWithLibs(
			new String[] {
				"Generics.java",
				"""
					import org.eclipse.jdt.annotation.*;
					
					public class Generics {
						<X> X m(@Nullable X a) { return null; }
						void test(@NonNull String in) {
							@NonNull String s = m(in);
							System.out.println(s.toLowerCase());
						}
						public static void main(String[] args) {
							new Generics().test("hallo");
						}
					}
					"""
			},
			compilerOptions,
			"""
				----------
				1. ERROR in Generics.java (at line 4)
					<X> X m(@Nullable X a) { return null; }
					                                ^^^^
				Null type mismatch (type annotations): \'null\' is not compatible to the free type variable 'X'
				----------
				""");
	}

	// conflicting annotations from type variable application and type variable substitution -> exclude null annotations from inference
	public void testNullTypeInference3b() {
		runNegativeTestWithLibs(
			new String[] {
				"Generics.java",
				"""
					import org.eclipse.jdt.annotation.*;
					
					public class Generics {
						<X> @Nullable X m1(@Nullable X a) { return null; }
						<X> @Nullable X m2(X a) { return null; }
						void test(@NonNull String in) {
							@NonNull String s1 = m1(in);
							@NonNull String s2 = m2(in);
						}
						public static void main(String[] args) {
							new Generics().test("hallo");
						}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in Generics.java (at line 7)
					@NonNull String s1 = m1(in);
					                     ^^^^^^
				Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'
				----------
				2. ERROR in Generics.java (at line 8)
					@NonNull String s2 = m2(in);
					                     ^^^^^^
				Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'
				----------
				""");
	}

	// conflicting annotations from type variable application and type variable substitution
	public void testNullTypeInference3c() {
		runNegativeTestWithLibs(
			new String[] {
				"Generics.java",
				"""
					import org.eclipse.jdt.annotation.*;
					import java.util.*;
					
					interface Function<I,O> { }
					abstract class MyFunc implements Function<@NonNull Object, @Nullable String> { }
					 \s
					public class Generics {
					  <@NonNull I,@Nullable O>\s
					  Collection<O> map1(Collection<I> in, Function<I, O> f) { return null; }
					  <@Nullable I,@NonNull O>\s
					  Collection<O> map2(Collection<I> in, Function<I, O> f) { return null; }
						void test(@NonNull List<Object> inList, MyFunc f) {
							Collection<@Nullable String> result = map1(inList, f);
							map2(inList, f);
						}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. WARNING in Generics.java (at line 13)
					Collection<@Nullable String> result = map1(inList, f);
					                                           ^^^^^^
				Null type safety (type annotations): The expression of type \'@NonNull List<Object>\' needs unchecked conversion to conform to \'Collection<@NonNull Object>\', corresponding supertype is 'Collection<Object>'
				----------
				2. WARNING in Generics.java (at line 14)
					map2(inList, f);
					     ^^^^^^
				Null type safety (type annotations): The expression of type \'@NonNull List<Object>\' needs unchecked conversion to conform to \'Collection<@Nullable Object>\', corresponding supertype is 'Collection<Object>'
				----------
				3. ERROR in Generics.java (at line 14)
					map2(inList, f);
					             ^
				Null type mismatch (type annotations): required \'Function<@Nullable Object,@NonNull String>\' but this expression has type \'MyFunc\', corresponding supertype is \'Function<@NonNull Object,@Nullable String>\'
				----------
				""");
	}

	// missing return type should not cause NPE
	public void testBug415850_01() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					public class X {
						@NonNull foo() {}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					@NonNull foo() {}
					         ^^^^^
				Return type for the method is missing
				----------
				""",
			this.LIBS,
			true/*flush*/);
	}

	// enum constant inside raw type: initialization must be recognized as conform to the implicitly @NonNull declaration
	public void testBug415850_02(){
		runConformTestWithLibs(
			new String[] {
				"Callable.java",
				"""
					interface Callable<T> {
						public enum Result {
							GOOD, BAD
						};
						public Result call(T arg);
					}
					"""
			},
			getCompilerOptions(),
			"");
	}

	// when mapping 1st parameter to method receiver, avoid AIOOBE in ReferenceExpression#resolveType(..)
	public void testBug415850_03() throws Exception {
		Runner runner = new Runner();
		runner.customOptions = getCompilerOptions();
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, JavaCore.IGNORE);
		runner.javacTestOptions = new JavacTestOptions.SuppressWarnings("deprecation");
		runner.classLibraries = this.LIBS;
		runner.testFiles =
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import java.util.Date;
					import static java.lang.annotation.ElementType.*;\s
					@Target(TYPE_USE)
					@interface Vernal {}
					interface I {
						int f(Date d);
					}
					class X {
						static void monitorTemperature(Object myObject) {
							I i = @Vernal Date::getDay;
						}
					}
					""",
			};
		runner.runConformTest();
	}

	// ensure annotation type has super types connected, to avoid NPE in ImplicitNullAnnotationVerifier.collectOverriddenMethods(..)
	public void testBug415850_04() throws Exception {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"public class X implements @B @C('i') J { }",
				"B.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(CLASS)
					@interface B {
						int value() default -1;
					}""",
				"C.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import static java.lang.annotation.RetentionPolicy.*;
					@Target(TYPE_USE)
					@Retention(RUNTIME)
					@interface C {
						char value() default '-';
					}
					""",
				"J.java",
				"interface J {}\n"
			},
			getCompilerOptions(),
			"");
	}

	// don't let type annotations on array dimensions spoil type compatibility
	public void testBug415850_05() {
		runNegativeTest(
			new String[]{
				"X.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					public class X {
						public void foo() {
							int @Marker [][][] i = new @Marker int @Marker [2] @Marker [@Marker bar()] @Marker [];
						}
						public int bar() {
							return 2;
						}
					}
					@Target (java.lang.annotation.ElementType.TYPE_USE)
					@interface Marker {}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					int @Marker [][][] i = new @Marker int @Marker [2] @Marker [@Marker bar()] @Marker [];
					                                                            ^^^^^^^
				Syntax error, type annotations are illegal here
				----------
				""",
			this.LIBS,
			true/*flush*/);
	}

	// don't let type annotations on array dimensions spoil type compatibility
	// case without any error
	public void testBug415850_06() {
		runConformTestWithLibs(
			new String[]{
				"X.java",
				"""
					import java.lang.annotation.Target;
					public class X {
						public void foo() {
							int @Marker [][][] i = new @Marker int @Marker [2] @Marker [bar()] @Marker [];
						}
						public int bar() {
							return 2;
						}
					}
					@Target (java.lang.annotation.ElementType.TYPE_USE)
					@interface Marker {}
					"""
			},
			getCompilerOptions(),
			"");
	}

	public void testBug416172() {
        runNegativeTestWithLibs(
            new String[] {
                "X.java",
                """
					import org.eclipse.jdt.annotation.NonNull;
					
					public class X {
					   class Y {}
					   X.@NonNull Y  foo(X.@NonNull Y xy) {
					       return new X().new Y();
					   }
					}
					
					class Z extends X {
					   @Override
					   X.@NonNull Y  foo(X.Y xy) {
					       return null;
					   }
					}
					"""
            },
            getCompilerOptions(),
            """
				----------
				1. WARNING in X.java (at line 12)
					X.@NonNull Y  foo(X.Y xy) {
					                  ^^^
				Missing non-null annotation: inherited method from X specifies this parameter as @NonNull
				----------
				2. ERROR in X.java (at line 13)
					return null;
					       ^^^^
				Null type mismatch: required \'X.@NonNull Y\' but the provided value is null
				----------
				""");
    }

	// incompatible null constraints on parameters
	public void testBug416174() {
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_NONNULL_PARAMETER_ANNOTATION_DROPPED, JavaCore.IGNORE);
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import java.util.List;
					
					import org.eclipse.jdt.annotation.*;
					
					public class X {
						void  foo1(List<X> lx) {}
						void  foo2(List<@NonNull X> lx) {}
						void  foo3(List<@Nullable X> lx) {}
						void  foo4(@NonNull List<@Nullable X> lx) {}
					}
					
					class Z extends X {
						@Override void foo1(List<@NonNull X> xy) {}
						@Override void foo2(List<X> lx) {}
						@Override void foo3(List<X> lx) {}
						@Override void foo4(List<@Nullable X> lx) {}
					}
					"""
			},
			options,
			"""
				----------
				1. ERROR in X.java (at line 13)
					@Override void foo1(List<@NonNull X> xy) {}
					                    ^^^^
				Illegal redefinition of parameter xy, inherited method from X declares this parameter as \'List<X>\' (mismatching null constraints)
				----------
				2. ERROR in X.java (at line 14)
					@Override void foo2(List<X> lx) {}
					                    ^^^^
				Illegal redefinition of parameter lx, inherited method from X declares this parameter as \'List<@NonNull X>\' (mismatching null constraints)
				----------
				3. ERROR in X.java (at line 15)
					@Override void foo3(List<X> lx) {}
					                    ^^^^
				Illegal redefinition of parameter lx, inherited method from X declares this parameter as \'List<@Nullable X>\' (mismatching null constraints)
				----------
				""");
	}

	// incompatibility at return type, which should be shown here in the error message
	public void testBug416174b() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					
					import org.eclipse.jdt.annotation.*;
					
					public abstract class X {
						List<X> foo1() {
							return null;
						}
						List<@Nullable X> foo2() {
							return null;
						}
						abstract @NonNull List<@NonNull X> foo3();
						List<@Nullable X> foo4() {
							return null;
						}
					}
					
					abstract class Z extends X {
						@Override
						List<@NonNull X> foo1() {
							return null;
						}
						@Override
						List<@NonNull X> foo2() {
							return null;
						}
						@Override
						@NonNull List<X> foo3() {
							return new ArrayList<>();
						}
						@Override
						@NonNull List<@Nullable X> foo4() {
							return new ArrayList<>();
						}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in X.java (at line 20)
					List<@NonNull X> foo1() {
					^^^^
				The return type is incompatible with \'List<X>\' returned from X.foo1() (mismatching null constraints)
				----------
				2. ERROR in X.java (at line 24)
					List<@NonNull X> foo2() {
					^^^^
				The return type is incompatible with \'List<@Nullable X>\' returned from X.foo2() (mismatching null constraints)
				----------
				3. ERROR in X.java (at line 28)
					@NonNull List<X> foo3() {
					         ^^^^
				The return type is incompatible with \'@NonNull List<@NonNull X>\' returned from X.foo3() (mismatching null constraints)
				----------
				""");
	}

	// overriding an unconstrained return with nullable
	public void testNullableReturn() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					
					import org.eclipse.jdt.annotation.*;
					
					public abstract class X {
						X foo1() {
							return null;
						}
					}
					
					abstract class Z extends X {
						@Override
						@Nullable X foo1() {
							return null;
						}
					}
					"""
			},
			getCompilerOptions(),
			"");
	}

	public void testBug416175() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.List;
					
					import org.eclipse.jdt.annotation.NonNull;
					
					public class X {
						public static void main(String[] args) {
							List<@NonNull ? extends @NonNull String> ls = new ArrayList<String>();
							ls.add(null);
							@NonNull String s = ls.get(0);
						}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. WARNING in X.java (at line 8)
					List<@NonNull ? extends @NonNull String> ls = new ArrayList<String>();
					                                              ^^^^^^^^^^^^^^^^^^^^^^^
				Null type safety (type annotations): The expression of type \'@NonNull ArrayList<String>\' needs unchecked conversion to conform to \'List<@NonNull ? extends @NonNull String>\', corresponding supertype is 'List<String>'
				----------
				2. ERROR in X.java (at line 9)
					ls.add(null);
					       ^^^^
				Null type mismatch: required \'@NonNull ? extends @NonNull String\' but the provided value is null
				----------
				3. INFO in X.java (at line 10)
					@NonNull String s = ls.get(0);
					                    ^^^^^^^^^
				Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'List<@NonNull capture#of ? extends @NonNull String>\'. Type \'List<E>\' doesn\'t seem to be designed with null type annotations in mind
				----------
				""");
	}

	// original test (was throwing stack overflow)
	public void testBug416176() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.NonNull;
					
					public class X<@NonNull T> {
						T foo(T t) {
							return t;
						}
					}
					"""
			},
			getCompilerOptions(),
			"");
	}

	// variant to challenge merging of annotation on type variable and its use
	public void testBug416176a() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.NonNull;
					import org.eclipse.jdt.annotation.Nullable;
					
					public class X<@NonNull T> {
						T foo(T t) {
							return t;
						}
						@NonNull T bar1(@NonNull T t) {
							return t;
						}
						@NonNull T bar2(@Nullable T t) { // argument: no contradiction (1)
							return t; // mismatch (1)
						}
						@Nullable T bar3(T t) { // return type: no contradiction (2)
							@Nullable T l = t; // local: no contradiction (3)
							return l;
						}
						class Inner {
							@Nullable T f; // field: no contradiction (4)
						}
						T bar3() {
							return null; // mismatch (2)
						}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in X.java (at line 12)
					return t; // mismatch (1)
					       ^
				Null type mismatch (type annotations): required \'@NonNull T\' but this expression has type \'@Nullable T\'
				----------
				2. ERROR in X.java (at line 22)
					return null; // mismatch (2)
					       ^^^^
				Null type mismatch: required \'@NonNull T\' but the provided value is null
				----------
				""");
	}

	// variant to challenge duplicate methods, though with different parameter annotations
	public void testBug416176b() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.NonNull;
					import org.eclipse.jdt.annotation.Nullable;
					
					public class X<T> {
						@NonNull T bar(@NonNull T t) {
							return t;
						}
						@NonNull T bar(@Nullable T t) {
							return t;
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					@NonNull T bar(@NonNull T t) {
					           ^^^^^^^^^^^^^^^^^
				Duplicate method bar(T) in type X<T>
				----------
				2. ERROR in X.java (at line 8)
					@NonNull T bar(@Nullable T t) {
					           ^^^^^^^^^^^^^^^^^^
				Duplicate method bar(T) in type X<T>
				----------
				""",
			this.LIBS,
			true/*flush*/);
	}

	public void testBug416180() {
		runWarningTestWithLibs(
			true,
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.NonNull;
					
					public class X<T> {
						T foo(T t) {
							return t;
						}
					\t
						public static void main(String[] args) {
							X<String> x = new Y();
						}
					}\s
					
					class Y extends X<@NonNull String> {
					   @Override
						@NonNull String foo(java.lang.@NonNull String t) {
							return "";
						};
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. INFO in X.java (at line 9)
					X<String> x = new Y();
					              ^^^^^^^
				Unsafe null type conversion (type annotations): The value of type '@NonNull Y' is made accessible using the less-annotated type 'X<String>', corresponding supertype is 'X<@NonNull String>'
				----------
				""");
	}

	public void testBug416181() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.NonNull;
					
					public class X<T> {
						class Y {
						\t
						}
					\t
						X<String>.@NonNull Y y = null; // 1st error here.
					\t
						@NonNull Y y2 = null; // 2nd error here.
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in X.java (at line 8)
					X<String>.@NonNull Y y = null; // 1st error here.
					                         ^^^^
				Null type mismatch: required \'X<String>.@NonNull Y\' but the provided value is null
				----------
				2. ERROR in X.java (at line 10)
					@NonNull Y y2 = null; // 2nd error here.
					                ^^^^
				Null type mismatch: required \'X<T>.@NonNull Y\' but the provided value is null
				----------
				""");
	}

	public void testBug416182() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.NonNull;
					import org.eclipse.jdt.annotation.Nullable;
					
					public class X<T> {
						T foo(@NonNull T t) {
							return t;
						}
						public static void main(String[] args) {
							X<@Nullable String> xs = new X<String>();
							xs.foo(null);
						}
					\t
						public void test(X<String> x) {
							X<@Nullable String> xs = x;
							xs.bar(null);
						}
						public void bar(T t) {}
					
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. WARNING in X.java (at line 9)
					X<@Nullable String> xs = new X<String>();
					                         ^^^^^^^^^^^^^^^
				Null type safety (type annotations): The expression of type \'@NonNull X<String>\' needs unchecked conversion to conform to \'X<@Nullable String>\'
				----------
				2. ERROR in X.java (at line 10)
					xs.foo(null);
					       ^^^^
				Null type mismatch: required \'@NonNull String\' but the provided value is null
				----------
				3. WARNING in X.java (at line 14)
					X<@Nullable String> xs = x;
					                         ^
				Null type safety (type annotations): The expression of type \'X<String>\' needs unchecked conversion to conform to \'X<@Nullable String>\'
				----------
				""");
	}

	// introduce unrelated method lookup before the bogus one
	public void testBug416182a() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.NonNull;
					import org.eclipse.jdt.annotation.Nullable;
					
					public class X<T> {
						T foo(@NonNull T t) {
							return t;
						}
						void foo() {}
						public static void main(String[] args) {
							X<@Nullable String> xs = new X<String>();
							xs.foo();
							xs.foo(null);
						}
					\t
						public void test(X<String> x) {
							X<@Nullable String> xs = x;
							xs.bar(null);
						}
						public void bar(T t) {}
					
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. WARNING in X.java (at line 10)
					X<@Nullable String> xs = new X<String>();
					                         ^^^^^^^^^^^^^^^
				Null type safety (type annotations): The expression of type \'@NonNull X<String>\' needs unchecked conversion to conform to \'X<@Nullable String>\'
				----------
				2. ERROR in X.java (at line 12)
					xs.foo(null);
					       ^^^^
				Null type mismatch: required \'@NonNull String\' but the provided value is null
				----------
				3. WARNING in X.java (at line 16)
					X<@Nullable String> xs = x;
					                         ^
				Null type safety (type annotations): The expression of type \'X<String>\' needs unchecked conversion to conform to \'X<@Nullable String>\'
				----------
				""");
	}

	// avoid extra warning by use of diamond.
	public void testBug416182b() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.NonNull;
					import org.eclipse.jdt.annotation.Nullable;
					
					public class X<T> {
						T foo(@NonNull T t) {
							return t;
						}
						public static void main(String[] args) {
							X<@Nullable String> xs = new X<>();
							xs.foo(null);
						}
					\t
						public void test(X<String> x) {
							X<@Nullable String> xs = x;
							xs.bar(null);
						}
						public void bar(T t) {}
					
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in X.java (at line 10)
					xs.foo(null);
					       ^^^^
				Null type mismatch: required \'@NonNull String\' but the provided value is null
				----------
				2. WARNING in X.java (at line 14)
					X<@Nullable String> xs = x;
					                         ^
				Null type safety (type annotations): The expression of type \'X<String>\' needs unchecked conversion to conform to \'X<@Nullable String>\'
				----------
				""");
	}

	public void testBug416183() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.NonNull;
					
					public class X<T> {
						T foo(@NonNull T t) {
							return t;
						}
						public static void main(String[] args) {
							X<String> xs = new X<String>();
							xs.foo("");
						}
					\t
					}
					"""
			},
			getCompilerOptions(),
			"");
	}
	// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=417113#c25, point 4.
	public void testSubstitution() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.NonNull;
					import java.util.List;
					import java.util.ArrayList;
					public class X<T> {
						T foo(@NonNull List<@NonNull T> l) {
							return l.get(0);
						}\t
						public static void main(String[] args) {
							X<String> s = new X<>();
							s.foo(new ArrayList<String>()); // (1)
							s.foo(null); // (2)
						}
					}
					"""

			},
			getCompilerOptions(),
			"""
				----------
				1. INFO in X.java (at line 6)
					return l.get(0);
					       ^^^^^^^^
				Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'@NonNull List<@NonNull T>\'. Type \'List<E>\' doesn\'t seem to be designed with null type annotations in mind
				----------
				2. WARNING in X.java (at line 10)
					s.foo(new ArrayList<String>()); // (1)
					      ^^^^^^^^^^^^^^^^^^^^^^^
				Null type safety (type annotations): The expression of type \'@NonNull ArrayList<String>\' needs unchecked conversion to conform to \'@NonNull List<@NonNull String>\', corresponding supertype is 'List<String>'
				----------
				3. ERROR in X.java (at line 11)
					s.foo(null); // (2)
					      ^^^^
				Null type mismatch: required \'@NonNull List<@NonNull String>\' but the provided value is null
				----------
				""");
	}
	// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=417113#c25, point 4.
	public void testSubstitution2() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.NonNull;
					import org.eclipse.jdt.annotation.Nullable;
					public class X<T> {
						T foo(@NonNull T @NonNull [] l) {
							return l[0];
						}\t
						public static void main(String[] args) {
							X<String> s = new X<>();
					       s.foo(new String [] { null });
					       s.foo(new String @Nullable [] { null });
					       s.foo(new String @NonNull [] { null });
					       s.foo(new @Nullable String @NonNull [] { null });
					       s.foo(new @NonNull String @NonNull [] { "" });
							s.foo(null); // (2)
						}
					}
					"""

			},
			getCompilerOptions(),
			"""
				----------
				1. WARNING in X.java (at line 9)
					s.foo(new String [] { null });
					      ^^^^^^^^^^^^^^^^^^^^^^
				Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'@NonNull String @NonNull[]\'
				----------
				2. WARNING in X.java (at line 10)
					s.foo(new String @Nullable [] { null });
					      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Null type safety (type annotations): The expression of type \'String @Nullable[]\' needs unchecked conversion to conform to \'@NonNull String @NonNull[]\'
				----------
				3. WARNING in X.java (at line 11)
					s.foo(new String @NonNull [] { null });
					      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Null type safety (type annotations): The expression of type \'String @NonNull[]\' needs unchecked conversion to conform to \'@NonNull String @NonNull[]\'
				----------
				4. ERROR in X.java (at line 12)
					s.foo(new @Nullable String @NonNull [] { null });
					      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Null type mismatch (type annotations): required \'@NonNull String @NonNull[]\' but this expression has type \'@Nullable String @NonNull[]\'
				----------
				5. ERROR in X.java (at line 14)
					s.foo(null); // (2)
					      ^^^^
				Null type mismatch: required \'@NonNull String @NonNull[]\' but the provided value is null
				----------
				""");
	}
	// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=417113#c25, point 4.
	public void testSubstitution3() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.NonNull;
					public class X<T> {
						T foo(@NonNull T l) {
							return l;
						}\t
						public static void main(String[] args) {
							X<String> s = new X<>();
					       s.foo(null);
						}
					}
					"""

			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in X.java (at line 8)
					s.foo(null);
					      ^^^^
				Null type mismatch: required \'@NonNull String\' but the provided value is null
				----------
				""");
	}
	// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=417113#c25, point 4.
	public void testSubstitution4() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					import org.eclipse.jdt.annotation.NonNull;
					@Target(ElementType.TYPE_USE)
					@interface TypeAnnotation {
					}
					public class X<T> {
					    class Y {}
					    void foo(@TypeAnnotation X<T>.@NonNull Y l) {
					    }\t
					    public static void main(String[] args) {
					        X<String> s = new X<>();
					        s.foo(null);
					    }
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in X.java (at line 13)
					s.foo(null);
					      ^^^^
				Null type mismatch: required \'X<String>.@NonNull Y\' but the provided value is null
				----------
				""");
	}
	// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=417113#c25, point 4.
	public void testSubstitution5() {
		runWarningTestWithLibs(
			true/*flush*/,
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.NonNull;
					public class X<T> {
					    void foo(@NonNull X<@NonNull ? extends T> p) {
					    }\t
					    public static void main(String[] args) {
					        X<String> s = new X<>();
					        X<@NonNull String> s2 = new X<@NonNull String>();
					        s.foo(s);
					        s.foo(s2);
					    }
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. WARNING in X.java (at line 8)
					s.foo(s);
					      ^
				Null type safety (type annotations): The expression of type \'X<String>\' needs unchecked conversion to conform to \'@NonNull X<@NonNull ? extends String>\'
				----------
				""");
	}

	// https://bugs.eclipse.org/417758 - [1.8][null] Null safety compromise during array creation.
	// original test case
	public void testArray1() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.NonNull;
					
					public class X<T> {
					  \s
						public static void main(String[] args) {
							@NonNull String @NonNull [] s = new @NonNull String [] { null };
							if (s != null && s[0] != null) {
								System.out.println("Not null");
							}
							System.out.println("Length = " + s[0].length());
						}
					}"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in X.java (at line 6)
					@NonNull String @NonNull [] s = new @NonNull String [] { null };
					                                                         ^^^^
				Null type mismatch: required \'@NonNull String\' but the provided value is null
				----------
				2. ERROR in X.java (at line 7)
					if (s != null && s[0] != null) {
					    ^
				Redundant null check: comparing \'@NonNull String @NonNull[]\' against null
				----------
				3. ERROR in X.java (at line 7)
					if (s != null && s[0] != null) {
					                 ^^^^
				Redundant null check: comparing \'@NonNull String\' against null
				----------
				""");
	}

	// https://bugs.eclipse.org/417758 - [1.8][null] Null safety compromise during array creation.
	// two-dim array with annotations on dimensions
	public void testArray2() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.NonNull;
					
					public class X<T> {
					  \s
						public static void main(String[] args) {
							@NonNull String @NonNull [][] s1 = new @NonNull String @NonNull [][] { null, { null} }; // problem at inner null
							@NonNull String @NonNull [][] s2 = new @NonNull String [] @NonNull [] { null, { null} }; // problem at both nulls
						}
					}"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in X.java (at line 6)
					@NonNull String @NonNull [][] s1 = new @NonNull String @NonNull [][] { null, { null} }; // problem at inner null
					                                                                               ^^^^
				Null type mismatch: required \'@NonNull String\' but the provided value is null
				----------
				2. INFO in X.java (at line 7)
					@NonNull String @NonNull [][] s2 = new @NonNull String [] @NonNull [] { null, { null} }; // problem at both nulls
					                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Unsafe null type conversion (type annotations): The value of type \'@NonNull String [] @NonNull[]\' is made accessible using the less-annotated type \'@NonNull String @NonNull[] []\'
				----------
				3. ERROR in X.java (at line 7)
					@NonNull String @NonNull [][] s2 = new @NonNull String [] @NonNull [] { null, { null} }; // problem at both nulls
					                                                                        ^^^^
				Null type mismatch: required \'@NonNull String @NonNull[]\' but the provided value is null
				----------
				4. ERROR in X.java (at line 7)
					@NonNull String @NonNull [][] s2 = new @NonNull String [] @NonNull [] { null, { null} }; // problem at both nulls
					                                                                                ^^^^
				Null type mismatch: required \'@NonNull String\' but the provided value is null
				----------
				""");
	}

	// https://bugs.eclipse.org/417758 - [1.8][null] Null safety compromise during array creation.
	// three-dim array with annotations on dimensions, also assignment has a problem
	public void testArray3() {
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_ANNOTATED_TYPE_ARGUMENT_TO_UNANNOTATED, JavaCore.WARNING);
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.NonNull;
					
					public class X<T> {
					  \s
						public static void main(String[] args) {
							@NonNull String [][] @NonNull [] s = new @NonNull String []@NonNull [][] { null, { {null}, null/*ok*/ } };
						}
					}"""
			},
			options,
			"""
				----------
				1. WARNING in X.java (at line 6)
					@NonNull String [][] @NonNull [] s = new @NonNull String []@NonNull [][] { null, { {null}, null/*ok*/ } };
					                                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Unsafe null type conversion (type annotations): The value of type \'@NonNull String [] @NonNull[] []\' is made accessible using the less-annotated type \'@NonNull String [] [] @NonNull[]\'
				----------
				2. ERROR in X.java (at line 6)
					@NonNull String [][] @NonNull [] s = new @NonNull String []@NonNull [][] { null, { {null}, null/*ok*/ } };
					                                                                           ^^^^
				Null type mismatch: required \'@NonNull String @NonNull[] []\' but the provided value is null
				----------
				3. ERROR in X.java (at line 6)
					@NonNull String [][] @NonNull [] s = new @NonNull String []@NonNull [][] { null, { {null}, null/*ok*/ } };
					                                                                                    ^^^^
				Null type mismatch: required \'@NonNull String\' but the provided value is null
				----------
				""");
	}

	public void testArray4() {
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_ANNOTATED_TYPE_ARGUMENT_TO_UNANNOTATED, JavaCore.WARNING);
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.NonNull;
					
					public class X<T> {
					  \s
						void ok() {
							@NonNull String @NonNull [] s0 = new @NonNull String @NonNull [0];
							@NonNull String @NonNull[]@NonNull[] s2 = new @NonNull String @NonNull[2]@NonNull[0];
							String []@NonNull[] s4 = new String [getDims()]@NonNull[1];
							@NonNull String @NonNull[][] s5 = new @NonNull String @NonNull[5][];
						}
						void nok() {
							@NonNull String @NonNull [] s1 = new @NonNull String @NonNull [1];
							@NonNull String @NonNull[]@NonNull[] s2 = new @NonNull String @NonNull[2]@NonNull[];
							@NonNull String @NonNull[][] s3 = new @NonNull String @NonNull[1][3];
							@NonNull String @NonNull[]@NonNull[] s6 = new @NonNull String @NonNull[5]@NonNull[];
						}
						int getDims() { return 1; }
					}"""
			},
			options,
			"""
				----------
				1. INFO in X.java (at line 12)
					@NonNull String @NonNull [] s1 = new @NonNull String @NonNull [1];
					                                                              ^^^
				This array dimension with declared element type @NonNull String will be initialized with \'null\' entries
				----------
				2. INFO in X.java (at line 13)
					@NonNull String @NonNull[]@NonNull[] s2 = new @NonNull String @NonNull[2]@NonNull[];
					                                                                      ^^^
				This array dimension with declared element type @NonNull String @NonNull[] will be initialized with \'null\' entries
				----------
				3. INFO in X.java (at line 14)
					@NonNull String @NonNull[][] s3 = new @NonNull String @NonNull[1][3];
					                                                                 ^^^
				This array dimension with declared element type @NonNull String will be initialized with \'null\' entries
				----------
				4. INFO in X.java (at line 15)
					@NonNull String @NonNull[]@NonNull[] s6 = new @NonNull String @NonNull[5]@NonNull[];
					                                                                      ^^^
				This array dimension with declared element type @NonNull String @NonNull[] will be initialized with \'null\' entries
				----------
				""");
	}

	public void testBug417759() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					
					public class X<T> {
						void foo(@NonNull X<@NonNull ?> l) {\s
						}\t
						public static void main(String[] args) {
							@NonNull X<String> s = new X<>();
					       s.foo(s);  // String vs. @NonNull ?
					       @NonNull X<@Nullable String> s2 = new X<>();
							s.foo(s2); // @Nullable String vs. @NonNull ?
					       @NonNull X<@NonNull String> s3 = new X<>();
							s.foo(s3); // good
						}
					}"""
			},
			getCompilerOptions(),
			"""
				----------
				1. WARNING in X.java (at line 8)
					s.foo(s);  // String vs. @NonNull ?
					      ^
				Null type safety (type annotations): The expression of type \'@NonNull X<String>\' needs unchecked conversion to conform to \'@NonNull X<@NonNull ?>\'
				----------
				2. ERROR in X.java (at line 10)
					s.foo(s2); // @Nullable String vs. @NonNull ?
					      ^^
				Null type mismatch (type annotations): required \'@NonNull X<@NonNull ?>\' but this expression has type \'@NonNull X<@Nullable String>\'
				----------
				""");
	}
	public void testTypeVariable1() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					import org.eclipse.jdt.annotation.NonNull;
					@Target(ElementType.TYPE_USE)
					@interface Junk {
					}
					public class X<@NonNull T> {
						T t = null;
						@Junk T t2 = null;
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in X.java (at line 8)
					T t = null;
					      ^^^^
				Null type mismatch: required \'@NonNull T\' but the provided value is null
				----------
				2. ERROR in X.java (at line 9)
					@Junk T t2 = null;
					             ^^^^
				Null type mismatch: required \'@NonNull T\' but the provided value is null
				----------
				""");
	}
	// free type variable does not ensure @NonNull, but cannot accept null either, unbounded type variable
	public void testTypeVariable2() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					public class X<T> {
						void consumeAny(T t) {
							consume(t); // error, t can be null
							consumeObject(t); // error, t can be null
						}
						void consumeNullable(@Nullable T t) {
							consume(t); // error, both sides explicit, mismatch
							consumeObject(t); // error, both sides explicit, mismatch
						}
						void consume(@NonNull T t) {}
						void consumeObject(@NonNull Object o) {}
						T produce() {
							return null; // error, T may not accept null
						}
						T produceFromNullable(@Nullable T t) {
							return t; // error, T may not accept nullable
						}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in X.java (at line 4)
					consume(t); // error, t can be null
					        ^
				Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
				----------
				2. ERROR in X.java (at line 5)
					consumeObject(t); // error, t can be null
					              ^
				Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
				----------
				3. ERROR in X.java (at line 8)
					consume(t); // error, both sides explicit, mismatch
					        ^
				Null type mismatch (type annotations): required \'@NonNull T\' but this expression has type \'@Nullable T\'
				----------
				4. ERROR in X.java (at line 9)
					consumeObject(t); // error, both sides explicit, mismatch
					              ^
				Null type mismatch (type annotations): required \'@NonNull Object\' but this expression has type \'@Nullable T\'
				----------
				5. ERROR in X.java (at line 14)
					return null; // error, T may not accept null
					       ^^^^
				Null type mismatch (type annotations): \'null\' is not compatible to the free type variable 'T'
				----------
				6. ERROR in X.java (at line 17)
					return t; // error, T may not accept nullable
					       ^
				Null type mismatch (type annotations): required \'T\' but this expression has type \'@Nullable T\', where \'T\' is a free type variable
				----------
				""");
	}
	// free type variable does not ensure @NonNull, but cannot accept null either, type variable with upper bound
	public void testTypeVariable3() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					public class X<T extends Number> {
						void consumeAny(T t) {
							consume(t); // error, t can be null
							consumeObject(t); // error, t can be null
						}
						void consumeNullable(@Nullable T t) {
							consume(t); // error, both sides explicit, mismatch
							consumeObject(t); // error, both sides explicit, mismatch
						}
						void consume(@NonNull T t) {}
						void consumeObject(@NonNull Object o) {}
						T produce() {
							return null; // error, T may not accept null
						}
						T produceFromNullable(@Nullable T t) {
							return t; // error, T may not accept nullable
						}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in X.java (at line 4)
					consume(t); // error, t can be null
					        ^
				Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
				----------
				2. ERROR in X.java (at line 5)
					consumeObject(t); // error, t can be null
					              ^
				Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
				----------
				3. ERROR in X.java (at line 8)
					consume(t); // error, both sides explicit, mismatch
					        ^
				Null type mismatch (type annotations): required \'@NonNull T extends Number\' but this expression has type \'@Nullable T extends Number\'
				----------
				4. ERROR in X.java (at line 9)
					consumeObject(t); // error, both sides explicit, mismatch
					              ^
				Null type mismatch (type annotations): required \'@NonNull Object\' but this expression has type \'@Nullable T extends Number\'
				----------
				5. ERROR in X.java (at line 14)
					return null; // error, T may not accept null
					       ^^^^
				Null type mismatch (type annotations): \'null\' is not compatible to the free type variable \'T\'
				----------
				6. ERROR in X.java (at line 17)
					return t; // error, T may not accept nullable
					       ^
				Null type mismatch (type annotations): required \'T\' but this expression has type \'@Nullable T extends Number\', where \'T\' is a free type variable
				----------
				""");
	}
	// free type variable is compatible to itself even with different not null-related type annotations
	public void testTypeVariable4() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					import org.eclipse.jdt.annotation.*;
					@Target(ElementType.TYPE_USE) @interface TypeMarker {}
					public class X<T> {
						T passThrough1(@TypeMarker T t) {
							return t; // OK
						}
						@TypeMarker T passThrough2(T t) {
							return t; // OK
						}
						@TypeMarker T passThrough3(@Nullable @TypeMarker T t) {
							return t; // Not OK
						}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in X.java (at line 12)
					return t; // Not OK
					       ^
				Null type mismatch (type annotations): required \'T\' but this expression has type \'@Nullable T\', where 'T' is a free type variable
				----------
				""");
	}
	// https://bugs.eclipse.org/433906
	public void testTypeVariable5() {
		runConformTestWithLibs(
			new String[] {
				"ExFunction.java",
				"""
					@FunctionalInterface
					public interface ExFunction<T, R, E extends Exception> {
						R apply(T t1) throws E;
					
						default <V>  ExFunction<V, R, E> compose(ExFunction<? super V, ? extends T, E> before) {
							java.util.Objects.requireNonNull(before);
							//warning on before.apply(v):
							//Null type safety (type annotations): The expression of type 'capture#of ? extends T' needs unchecked conversion to conform to 'T'
							return (V v) -> apply(before.apply(v));
						}
					}
					"""
			},
			getCompilerOptions(),
			"");
	}
	public void testSE7AnnotationCopy() { // we were dropping annotations here, but null analysis worked already since the tagbits were not "dropped", just the same capturing in a test
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					import org.eclipse.jdt.annotation.NonNull;
					@Target(ElementType.TYPE_USE)
					@interface T {
					}
					public class X {
						class Y {}
						void foo(@T X.@NonNull Y p) {
							foo(null);
						}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in X.java (at line 10)
					foo(null);
					    ^^^^
				Null type mismatch: required \'X.@NonNull Y\' but the provided value is null
				----------
				""");
	}
	public void testWildcardCapture() {
		runWarningTestWithLibs(
			true/*flush*/,
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					import java.util.ArrayList;
					import java.util.List;
					import org.eclipse.jdt.annotation.NonNull;
					
					@Target(ElementType.TYPE_USE)
					@interface T {
					}
					
					public class X {
						public static void main(String[] args) {
					       List<X> ax = new ArrayList<X>();
					       ax.add(new X());
							List<? extends X> lx = ax;
							getAdd(lx);
						}
						static <@NonNull P>  void getAdd(List<P> lt) {
							lt.add(lt.get(0));
						}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. WARNING in X.java (at line 16)
					getAdd(lx);
					       ^^
				Null type safety (type annotations): The expression of type \'List<capture#of ? extends X>\' needs unchecked conversion to conform to \'List<@NonNull capture#of ? extends X>\'
				----------
				""");
	}
	public void testWildcardCapture2() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					import java.util.ArrayList;
					import java.util.List;
					import org.eclipse.jdt.annotation.NonNull;
					
					@Target(ElementType.TYPE_USE)
					@interface T {
					}
					
					public class X {
						public static void main(String[] args) {
					       List<@NonNull X> ax = new ArrayList<@NonNull X>();
					       ax.add(new X());
							List<@NonNull ? extends X> lx = ax;
							getAdd(lx);
						}
						static <@NonNull P>  void getAdd(List<P> lt) {
							lt.add(lt.get(0));
						}
					}
					"""
			},
			getCompilerOptions(),
			"");
	}
	public void testWildcardCapture3() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					import java.util.ArrayList;
					import java.util.List;
					import org.eclipse.jdt.annotation.NonNull;
					import org.eclipse.jdt.annotation.Nullable;
					
					@Target(ElementType.TYPE_USE)
					@interface T {
					}
					
					public class X {
						public static void main(String[] args) {
					       List<@Nullable X> ax = new ArrayList<@Nullable X>();
					       ax.add(new X());
							List<@Nullable ? extends X> lx = ax;
							getAdd(lx);
						}
						static <@NonNull P>  void getAdd(List<P> lt) {
							lt.add(lt.get(0));
						}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in X.java (at line 17)
					getAdd(lx);
					       ^^
				Null type mismatch (type annotations): required \'List<@NonNull capture#of ? extends X>\' but this expression has type \'List<@Nullable capture#of ? extends X>\'
				----------
				""");
	}
	public void testLocalArrays() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					import org.eclipse.jdt.annotation.NonNull;
					import org.eclipse.jdt.annotation.Nullable;
					
					@Target(ElementType.TYPE_USE)
					@interface T {
					}
					
					public class X {
						public static void main(String[] args) {
					       class L {};
					       L @NonNull [] @Nullable [] la = new L[5][];
					       L @Nullable [] @NonNull [] la2 = new L[3][];
					       la = la2;
					   }
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. WARNING in X.java (at line 13)
					L @NonNull [] @Nullable [] la = new L[5][];
					                                ^^^^^^^^^^
				Null type safety (type annotations): The expression of type \'L[][]\' needs unchecked conversion to conform to \'L @NonNull[] @Nullable[]\'
				----------
				2. WARNING in X.java (at line 14)
					L @Nullable [] @NonNull [] la2 = new L[3][];
					                                 ^^^^^^^^^^
				Null type safety (type annotations): The expression of type \'L[][]\' needs unchecked conversion to conform to \'L @Nullable[] @NonNull[]\'
				----------
				3. ERROR in X.java (at line 15)
					la = la2;
					     ^^^
				Null type mismatch (type annotations): required \'L @NonNull[] @Nullable[]\' but this expression has type \'L @Nullable[] @NonNull[]\'
				----------
				""");

		// Without annotations.
		runConformTestWithLibs(
				false /* don't flush output dir */,
				new String[] {
					"X.java",
					"""
						public class X {
							public static void main(String[] args) {
						       class L {};
						       L [] [] la = new L[5][];
						       L []  [] la2 = new L[3][];
						       la = la2;
						       System.out.println("Done");
						   }
						}
						"""
				},
				getCompilerOptions(),
				"",
				"Done");
	}
	public void testRawType() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.NonNull;
					public class X<T> {
						class Y <P> {}
						public static void main(String[] args) {
							@NonNull X x = null;
							X.@NonNull Y xy = null;
						}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. WARNING in X.java (at line 5)
					@NonNull X x = null;
					         ^
				X is a raw type. References to generic type X<T> should be parameterized
				----------
				2. ERROR in X.java (at line 5)
					@NonNull X x = null;
					               ^^^^
				Null type mismatch: required \'@NonNull X\' but the provided value is null
				----------
				3. WARNING in X.java (at line 6)
					X.@NonNull Y xy = null;
					^^^^^^^^^^^^
				X.Y is a raw type. References to generic type X<T>.Y<P> should be parameterized
				----------
				4. ERROR in X.java (at line 6)
					X.@NonNull Y xy = null;
					                  ^^^^
				Null type mismatch: required \'X.@NonNull Y\' but the provided value is null
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=420456, [1.8][null] AIOOB in null analysis code.
	public void test420456() {
		final Map compilerOptions = getCompilerOptions();
		compilerOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.IGNORE);
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					import java.util.Arrays;
					public class X {
						public static void main(String [] args) {
							Integer [] array = new Integer[] { 1234, 5678, 789 };
							Arrays.sort(array, Integer::compare);
					       System.out.println("" + array[0] + array[1] + array[2]);
						}
					}
					"""
			},
			compilerOptions,
			"",
			"78912345678");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422134, [1.8] NPE in NullAnnotationMatching with inlined lambda expression used with a raw type
	public void test422134() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.Collections;
					public class X {
						public static void main(String args[]) {
							Collections.sort(new ArrayList(), (o1, o2) -> {
								return o1.compareToIgnoreCase(o1);
							});
						}
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 5)
					Collections.sort(new ArrayList(), (o1, o2) -> {
							return o1.compareToIgnoreCase(o1);
						});
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked invocation sort(ArrayList, (<no type> o1, <no type> o2) -> {})\
				 of the generic method sort(List<T>, Comparator<? super T>) of type Collections
				----------
				2. WARNING in X.java (at line 5)
					Collections.sort(new ArrayList(), (o1, o2) -> {
					                 ^^^^^^^^^^^^^^^
				Type safety: The expression of type ArrayList needs unchecked conversion to conform to List<Object>
				----------
				3. WARNING in X.java (at line 5)
					Collections.sort(new ArrayList(), (o1, o2) -> {
					                     ^^^^^^^^^
				ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized
				----------
				4. ERROR in X.java (at line 6)
					return o1.compareToIgnoreCase(o1);
					          ^^^^^^^^^^^^^^^^^^^
				The method compareToIgnoreCase(Object) is undefined for the type Object
				----------
				""",
			this.LIBS,
			true/*flush*/);
	}

	// should not try to analyze arguments of a polymorphic method call
	public void testBug424725() {
		runConformTestWithLibs(
			new String[] {
				"AnnotatedRecordMapper.java",
				"""
					import java.lang.invoke.MethodHandle;
					
					public final class AnnotatedRecordMapper<T> {
					  private MethodHandle afterLoadStore;
					
					  public void invokeAfterLoadStore(Object object, Object database) {
					    if(afterLoadStore != null) {
					      try {
					        afterLoadStore.invoke(object, database);
					      }
					      catch(Throwable e) {
					        throw new RuntimeException(e);
					      }
					    }
					  }
					}"""
			},
			null,
			"");
	}

	public void testBug424727() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					@org.eclipse.jdt.annotation.NonNull public class X {
						static X singleton = new X();
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in X.java (at line 1)
					@org.eclipse.jdt.annotation.NonNull public class X {
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The nullness annotation \'NonNull\' is not applicable at this location
				----------
				""");
		// note: to be updated with https://bugs.eclipse.org/415918
	}

public void testBug424637() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				import java.nio.file.Files;
				import java.nio.file.Path;
				import java.util.function.Function;
				import java.util.stream.Stream;
				
				public class X {
				  public static void method() {
				    Function<Path, Stream<Path>> method = Files::walk;
				  }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				Function<Path, Stream<Path>> method = Files::walk;
				                                      ^^^^^^^^^^^
			Unhandled exception type IOException
			----------
			""",
		this.LIBS,
		true/*flush*/);
}

public void testBug424637a() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import java.nio.file.FileVisitOption;
				import java.nio.file.Path;
				import java.util.function.BiFunction;
				import java.util.stream.Stream;
				import org.eclipse.jdt.annotation.*;
				
				interface TriFunc<A,B,C,D> { D apply(A a, B b, C c); }
				public class X {
				  public static Stream<Path> myWalk(Path p, @NonNull FileVisitOption ... options) { return null; }
				  public static void method() {
				    BiFunction<Path, @Nullable FileVisitOption, Stream<Path>> method1 = X::myWalk;
				    BiFunction<Path, @Nullable FileVisitOption[], Stream<Path>> method2 = X::myWalk;
				    BiFunction<Path, FileVisitOption[], Stream<Path>> method3 = X::myWalk;
				 	 TriFunc<Path, @NonNull FileVisitOption, @Nullable FileVisitOption, Stream<Path>> method4 = X::myWalk;
				  }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 11)
				BiFunction<Path, @Nullable FileVisitOption, Stream<Path>> method1 = X::myWalk;
				                                                                    ^^^^^^^^^
			Null type mismatch at parameter 2: required \'@NonNull FileVisitOption\' but provided \'@Nullable FileVisitOption\' via method descriptor BiFunction<Path,FileVisitOption,Stream<Path>>.apply(Path, FileVisitOption)
			----------
			2. ERROR in X.java (at line 12)
				BiFunction<Path, @Nullable FileVisitOption[], Stream<Path>> method2 = X::myWalk;
				                                                                      ^^^^^^^^^
			Null type mismatch at parameter 2: required \'@NonNull FileVisitOption []\' but provided \'@Nullable FileVisitOption []\' via method descriptor BiFunction<Path,FileVisitOption[],Stream<Path>>.apply(Path, FileVisitOption[])
			----------
			3. WARNING in X.java (at line 13)
				BiFunction<Path, FileVisitOption[], Stream<Path>> method3 = X::myWalk;
				                                                            ^^^^^^^^^
			Null type safety: parameter 2 provided via method descriptor BiFunction<Path,FileVisitOption[],Stream<Path>>.apply(Path, FileVisitOption[]) needs unchecked conversion to conform to \'@NonNull FileVisitOption []\'
			----------
			4. ERROR in X.java (at line 14)
				TriFunc<Path, @NonNull FileVisitOption, @Nullable FileVisitOption, Stream<Path>> method4 = X::myWalk;
				                                                                                           ^^^^^^^^^
			Null type mismatch at parameter 3: required \'@NonNull FileVisitOption\' but provided \'@Nullable FileVisitOption\' via method descriptor TriFunc<Path,FileVisitOption,FileVisitOption,Stream<Path>>.apply(Path, FileVisitOption, FileVisitOption)
			----------
			""");
}

public void testBug424637_comment3() {
	runConformTestWithLibs(
		new String[] {
			"VarArgsMethodReferenceTest.java",
			"""
				import java.util.function.Consumer;
				public class VarArgsMethodReferenceTest {
				  public static void main(String[] argv) {
				    Consumer<String> printffer;
				    printffer = System.out::printf;
				  }
				}"""
		},
		null,
		"");
}
public void testBug427163() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
					void consume(@NonNull String @Nullable... strings) {
					}
				}
				"""
		},
		getCompilerOptions(),
		""
	);
}
public void testBug427163b() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
					void consume1(@NonNull @Nullable String @Nullable[] strings) {}
					void consume2(@Nullable String @NonNull @Nullable... strings) {}
					void consume3(@Nullable String[] @NonNull @Nullable[] strings) {}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 3)
				void consume1(@NonNull @Nullable String @Nullable[] strings) {}
				                       ^^^^^^^^^
			Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location
			----------
			2. ERROR in X.java (at line 4)
				void consume2(@Nullable String @NonNull @Nullable... strings) {}
				                               ^^^^^^^^^^^^^^^^^^
			Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location
			----------
			3. ERROR in X.java (at line 5)
				void consume3(@Nullable String[] @NonNull @Nullable[] strings) {}
				                                 ^^^^^^^^^^^^^^^^^^
			Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location
			----------
			"""
	);
}
public void testBug427163c() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
					String[][] strings0 = new @NonNull String @Nullable[] @Nullable[] {};
					String[] strings1 = new String @NonNull @Nullable[] {};
					Object[] objects2 = new Object @NonNull @Nullable[1];
					String[] strings3 = new @NonNull @Nullable String [1];
					String[] strings4 = new @NonNull String  @Nullable @NonNull[1];
					String[][] strings5 = new String[] @NonNull @Nullable[] {};
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 4)
				String[] strings1 = new String @NonNull @Nullable[] {};
				                               ^^^^^^^^^^^^^^^^^^
			Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location
			----------
			2. ERROR in X.java (at line 5)
				Object[] objects2 = new Object @NonNull @Nullable[1];
				                               ^^^^^^^^^^^^^^^^^^
			Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location
			----------
			3. ERROR in X.java (at line 6)
				String[] strings3 = new @NonNull @Nullable String [1];
				                        ^^^^^^^^^^^^^^^^^^
			Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location
			----------
			4. ERROR in X.java (at line 7)
				String[] strings4 = new @NonNull String  @Nullable @NonNull[1];
				                                         ^^^^^^^^^^^^^^^^^^
			Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location
			----------
			5. INFO in X.java (at line 7)
				String[] strings4 = new @NonNull String  @Nullable @NonNull[1];
				                                                           ^^^
			This array dimension with declared element type @NonNull String will be initialized with \'null\' entries
			----------
			6. ERROR in X.java (at line 8)
				String[][] strings5 = new String[] @NonNull @Nullable[] {};
				                                   ^^^^^^^^^^^^^^^^^^
			Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location
			----------
			"""
	);
}
// assorted tests with upper-bounded wildcards with null annotations
public void testTypeBounds1() {
	runNegativeTestWithLibs(
		new String[] {
			"C.java",
			"""
				import java.util.List;
				
				import org.eclipse.jdt.annotation.NonNull;
				import org.eclipse.jdt.annotation.Nullable;
				
				class A { }
				class B extends A {}
				public class C {
				\t
					@NonNull A testExtends(List<@NonNull B> lb1, List<@Nullable B> lb2, boolean f) {
						List<? extends @NonNull A> la1 = lb1;
						la1.add(null); // ERR1
						if (la1.size() > 0)
							return la1.get(0); // OK
						la1 = lb2; // ERR2
						List<? extends @Nullable A> la2 = lb1; // OK
						la2.add(null); // ERR3
						if (la2.size() > 0)
							return la2.get(0); // ERR4
						la2 = lb2; // OK
						if (f)
							return mExtends1(lb1); // OK, since we infer T to @NonNull B
						return mExtends2(lb1);
					}
					<T extends @Nullable A> T mExtends1(List<T> t) { return null; /*ERR5*/ }
					<T extends @NonNull A> T mExtends2(List<T> t) { return null; /*ERR6*/ }
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in C.java (at line 12)
				la1.add(null); // ERR1
				        ^^^^
			Null type mismatch: required \'? extends @NonNull A\' but the provided value is null
			----------
			2. INFO in C.java (at line 14)
				return la1.get(0); // OK
				       ^^^^^^^^^^
			Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'List<capture#of ? extends @NonNull A>\'. Type \'List<E>\' doesn\'t seem to be designed with null type annotations in mind
			----------
			3. ERROR in C.java (at line 15)
				la1 = lb2; // ERR2
				      ^^^
			Null type mismatch (type annotations): required \'List<? extends @NonNull A>\' but this expression has type \'List<@Nullable B>\'
			----------
			4. ERROR in C.java (at line 17)
				la2.add(null); // ERR3
				        ^^^^
			Null type mismatch: required \'? extends @Nullable A\' but the provided value is null
			----------
			5. ERROR in C.java (at line 19)
				return la2.get(0); // ERR4
				       ^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull A\' but this expression has type \'capture#of ? extends @Nullable A\'
			----------
			6. ERROR in C.java (at line 25)
				<T extends @Nullable A> T mExtends1(List<T> t) { return null; /*ERR5*/ }
				                                                        ^^^^
			Null type mismatch: required \'T extends @Nullable A\' but the provided value is null
			----------
			7. ERROR in C.java (at line 26)
				<T extends @NonNull A> T mExtends2(List<T> t) { return null; /*ERR6*/ }
				                                                       ^^^^
			Null type mismatch: required \'T extends @NonNull A\' but the provided value is null
			----------
			"""
	);
}
// assorted tests with lower-bounded wildcards with null annotations
public void testTypeBounds2() {
	runNegativeTestWithLibs(
		new String[] {
			"C.java",
			"""
				import java.util.List;
				
				import org.eclipse.jdt.annotation.NonNull;
				import org.eclipse.jdt.annotation.Nullable;
				
				class A { }
				class B extends A {}
				public class C {
				\t
					@NonNull Object testSuper(List<@Nullable A> la1, List<@NonNull A> la2, boolean f) {
						List<? super @NonNull B> lb1 = la1; // OK
						lb1.add(null); // ERR1
						if (lb1.size() > 0)
							return lb1.get(0); // ERR2
						lb1 = la2; // OK
						List<? super @Nullable B> lb2 = la1;
						lb2.add(null);
						if (lb2.size() > 0)
							return lb2.get(0); // ERR3
						lb2 = la2; // ERR4
						if (f)
							return mSuper1(la1); // ERR5
						return mSuper2(la1); // ERR6 on arg
					}
					<T extends @Nullable A> T mSuper1(List<T> t) { return null; /*ERR7*/ }
					<T extends @NonNull A> T mSuper2(List<T> t) { return null; /*ERR8*/ }
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in C.java (at line 12)
				lb1.add(null); // ERR1
				        ^^^^
			Null type mismatch: required \'? super @NonNull B\' but the provided value is null
			----------
			2. ERROR in C.java (at line 14)
				return lb1.get(0); // ERR2
				       ^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull Object\' but this expression has type \'capture#of ? super @NonNull B\'
			----------
			3. ERROR in C.java (at line 19)
				return lb2.get(0); // ERR3
				       ^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull Object\' but this expression has type \'capture#of ? super @Nullable B\'
			----------
			4. ERROR in C.java (at line 20)
				lb2 = la2; // ERR4
				      ^^^
			Null type mismatch (type annotations): required \'List<? super @Nullable B>\' but this expression has type \'List<@NonNull A>\'
			----------
			5. ERROR in C.java (at line 22)
				return mSuper1(la1); // ERR5
				       ^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull Object\' but this expression has type \'@Nullable A\'
			----------
			6. ERROR in C.java (at line 23)
				return mSuper2(la1); // ERR6 on arg
				               ^^^
			Null type mismatch (type annotations): required \'List<@NonNull A>\' but this expression has type \'List<@Nullable A>\'
			----------
			7. ERROR in C.java (at line 25)
				<T extends @Nullable A> T mSuper1(List<T> t) { return null; /*ERR7*/ }
				                                                      ^^^^
			Null type mismatch: required \'T extends @Nullable A\' but the provided value is null
			----------
			8. ERROR in C.java (at line 26)
				<T extends @NonNull A> T mSuper2(List<T> t) { return null; /*ERR8*/ }
				                                                     ^^^^
			Null type mismatch: required \'T extends @NonNull A\' but the provided value is null
			----------
			"""
	);
}
// assigning values upper bounded wildcard types carrying null annotations
public void testTypeBounds3() {
	runNegativeTestWithLibs(
		new String[] {
			"C.java",
			"""
				import java.util.List;
				
				import org.eclipse.jdt.annotation.NonNull;
				import org.eclipse.jdt.annotation.Nullable;
				
				class A { }
				class B extends A {}
				public class C {
				\t
					void testExtends(List<? extends @NonNull B> lb1, List<? extends @Nullable B> lb2) {
						List<? extends @NonNull A> la1 = lb1;
						la1 = lb2; // ERR
						List<? extends @Nullable A> la2 = lb1;
						la2 = lb2;
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in C.java (at line 12)
				la1 = lb2; // ERR
				      ^^^
			Null type mismatch (type annotations): required \'List<? extends @NonNull A>\' but this expression has type \'List<capture#of ? extends @Nullable B>\'
			----------
			"""
	);
}
// assigning values lower bounded wildcard types carrying null annotations
public void testTypeBounds4() {
	runNegativeTestWithLibs(
		new String[] {
			"C.java",
			"""
				import java.util.List;
				
				import org.eclipse.jdt.annotation.NonNull;
				import org.eclipse.jdt.annotation.Nullable;
				
				class A { }
				class B extends A {}
				public class C {
				\t
					void testSuper(List<? super @Nullable A> la1, List<? super @NonNull A> la2) {
						List<? super @NonNull B> lb1 = la1; // OK
						lb1 = la2; // OK
						List<? super @Nullable B> lb2 = la1;
						lb2 = la2; // ERR4
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in C.java (at line 14)
				lb2 = la2; // ERR4
				      ^^^
			Null type mismatch (type annotations): required \'List<? super @Nullable B>\' but this expression has type \'List<capture#of ? super @NonNull A>\'
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429387, [1.8][compiler] AIOOBE in AbstractMethodDeclaration.createArgumentBindings
public void test429387() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.function.BiFunction;
				import java.util.function.Supplier;
				import java.util.function.ToIntFunction;
				import java.util.stream.IntStream;
				import java.util.stream.Stream;
				public interface X {
				static <BT, T extends BT, IS extends IntStream, E extends Exception> IntStreamy<E>
				internalFlatMapToInt(Functionish<BT, IS, E> mapper,
				Class<E> classOfE,
				Supplier<Stream<T>> maker) {
				BiFunction<Stream<T>, ToIntFunction<BT>, IntStream> func = (Stream<T> t, ToIntFunction<BT, IS> m) -> t.flatmmapToInt(m);
				return IntStreamy.fromFlatMap(func, mapper, classOfE, maker);
				}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				static <BT, T extends BT, IS extends IntStream, E extends Exception> IntStreamy<E>
				                                                                     ^^^^^^^^^^
			IntStreamy cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 8)
				internalFlatMapToInt(Functionish<BT, IS, E> mapper,
				                     ^^^^^^^^^^^
			Functionish cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 11)
				BiFunction<Stream<T>, ToIntFunction<BT>, IntStream> func = (Stream<T> t, ToIntFunction<BT, IS> m) -> t.flatmmapToInt(m);
				                                                                         ^^^^^^^^^^^^^
			Incorrect number of arguments for type ToIntFunction<T>; it cannot be parameterized with arguments <BT, IS>
			----------
			4. ERROR in X.java (at line 11)
				BiFunction<Stream<T>, ToIntFunction<BT>, IntStream> func = (Stream<T> t, ToIntFunction<BT, IS> m) -> t.flatmmapToInt(m);
				                                                                                                                     ^
			m cannot be resolved to a variable
			----------
			5. ERROR in X.java (at line 12)
				return IntStreamy.fromFlatMap(func, mapper, classOfE, maker);
				       ^^^^^^^^^^
			IntStreamy cannot be resolved
			----------
			""",
		this.LIBS,
		true/*flush*/);
}
public void testBug429403() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				import org.eclipse.jdt.annotation.*;
				class Person {}
				public class X {
					List<@NonNull Person> l = new ArrayList<@Nullable Person>();\
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				List<@NonNull Person> l = new ArrayList<@Nullable Person>();}
				                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'List<@NonNull Person>\' but this expression has type \'@NonNull ArrayList<@Nullable Person>\', corresponding supertype is \'List<@Nullable Person>\'
			----------
			""");
}
public void testBug430219() {
    runNegativeTest(
        new String[] {
            "X.java",
            """
				import org.eclipse.jdt.annotation.NonNullByDefault;
				@NonNullByDefault
				public class X {
				       void foo(int @NonNull [] x) {}
				}
				"""
        },
        """
			----------
			1. ERROR in X.java (at line 4)
				void foo(int @NonNull [] x) {}
				              ^^^^^^^
			NonNull cannot be resolved to a type
			----------
			""",
	   this.LIBS,
	   true/*flush*/);
}
public void testBug430219a() {
    runConformTestWithLibs(
        new String[] {
            "X.java",
            """
				import org.eclipse.jdt.annotation.NonNullByDefault;
				import java.lang.annotation.*;
				@Target(ElementType.TYPE_USE) @interface Marker{}
				@NonNullByDefault
				public class X {
				       void foo(int @Marker[] x) {}
				}
				"""
        },
        getCompilerOptions(),
        "");
}

// apply null default to type arguments:
public void testDefault01() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				import java.util.*;
				@NonNullByDefault(DefaultLocation.TYPE_ARGUMENT)
				public class X {
					List<Number> test1(List<Number> in) {
						in.add(null); // ERR
						return new ArrayList<@Nullable Number>(); // ERR
					}
					java.util.List<java.lang.Number> test2(java.util.List<java.lang.Number> in) {
						in.add(null); // ERR
						return new ArrayList<java.lang.@Nullable Number>(); // ERR
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 6)
				in.add(null); // ERR
				       ^^^^
			Null type mismatch: required \'@NonNull Number\' but the provided value is null
			----------
			2. ERROR in X.java (at line 7)
				return new ArrayList<@Nullable Number>(); // ERR
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'List<@NonNull Number>\' but this expression has type \'@NonNull ArrayList<@Nullable Number>\', corresponding supertype is \'List<@Nullable Number>\'
			----------
			3. ERROR in X.java (at line 10)
				in.add(null); // ERR
				       ^^^^
			Null type mismatch: required \'@NonNull Number\' but the provided value is null
			----------
			4. ERROR in X.java (at line 11)
				return new ArrayList<java.lang.@Nullable Number>(); // ERR
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'List<@NonNull Number>\' but this expression has type \'@NonNull ArrayList<@Nullable Number>\', corresponding supertype is \'List<@Nullable Number>\'
			----------
			""");
}

// apply null default to type arguments - no effect on type variable or wildcard, but apply strict checking assuming nothing
public void testDefault01b() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				import java.util.*;
				@NonNullByDefault(DefaultLocation.TYPE_ARGUMENT)
				public class X<T> {
					List<T> test(List<? extends Number> in) {
						in.add(null); // NOK, cannot assume nullable
						needNN(in.get(0)); // NOK, cannot assume nonnull
						return new ArrayList<@Nullable T>(); // NOK, cannot assume nullable for T in List<T>
					}
					void needNN(@NonNull Number n) {}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 6)
				in.add(null); // NOK, cannot assume nullable
				       ^^^^
			Null type mismatch (type annotations): \'null\' is not compatible to the free type variable \'? extends Number\'
			----------
			2. ERROR in X.java (at line 7)
				needNN(in.get(0)); // NOK, cannot assume nonnull
				       ^^^^^^^^^
			Null type safety: required \'@NonNull\' but this expression has type \'capture#2-of ? extends java.lang.Number\', a free type variable that may represent a \'@Nullable\' type
			----------
			3. ERROR in X.java (at line 8)
				return new ArrayList<@Nullable T>(); // NOK, cannot assume nullable for T in List<T>
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'List<T>\' but this expression has type \'@NonNull ArrayList<@Nullable T>\', corresponding supertype is \'List<@Nullable T>\'
			----------
			""");
}

// apply null default to parameters:
public void testDefault02() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault(DefaultLocation.PARAMETER)
				public class X {
					Number test1(Number in) {
						System.out.print(in.intValue()); // OK
						test1(null); // ERR
						return null; // OK
					}
					java.lang.Number test2(java.lang.Number in) {
						System.out.print(in.intValue()); // OK
						test2(null); // ERR
						return null; // OK
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 6)
				test1(null); // ERR
				      ^^^^
			Null type mismatch: required \'@NonNull Number\' but the provided value is null
			----------
			2. ERROR in X.java (at line 11)
				test2(null); // ERR
				      ^^^^
			Null type mismatch: required \'@NonNull Number\' but the provided value is null
			----------
			""");
}

// apply null default to return type - annotation at method:
public void testDefault03() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
					@NonNullByDefault(DefaultLocation.RETURN_TYPE)
					Number test(Number in) {
						System.out.print(in.intValue());
						test(null); // OK
						return null; // ERR
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 7)
				return null; // ERR
				       ^^^^
			Null type mismatch: required \'@NonNull Number\' but the provided value is null
			----------
			""");
}

// apply null default to field
public void testDefault04() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault(DefaultLocation.FIELD)
				public class X {
					Number field; // ERR since uninitialized
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 4)
				Number field; // ERR since uninitialized
				       ^^^^^
			The @NonNull field field may not have been initialized
			----------
			""");
}

// default default
public void testDefault05() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class X {
					Number field; // ERR since uninitialized
					void test1(Number[] ns) {
						ns[0] = null; // OK since not affected by default
					}
					void test2(java.lang.Number[] ns) {
						ns[0] = null; // OK since not affected by default
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 4)
				Number field; // ERR since uninitialized
				       ^^^^^
			The @NonNull field field may not have been initialized
			----------
			""");
}

//default default
public void testDefault05_custom() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
	runner.customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
	runner.customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "org.foo.NonNullByDefault");
	runner.testFiles =
		new String[] {
			CUSTOM_NULLABLE_NAME,
			CUSTOM_NULLABLE_CONTENT,
			CUSTOM_NONNULL_NAME,
			CUSTOM_NONNULL_CONTENT,
			CUSTOM_NNBD_NAME,
			CUSTOM_NNBD_CONTENT,
			"test/package-info.java",
			"@org.foo.NonNullByDefault\n" +
			"package test;\n",
			"test/X.java",
			"""
				package test;
				public class X {
					Number field; // ERR since uninitialized
					void test1(Number[] ns) {
						ns[0] = null; // OK since not affected by default
					}
					void test2(java.lang.Number[] ns) {
						ns[0] = null; // OK since not affected by default
					}
				}
				"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in test\\X.java (at line 3)
				Number field; // ERR since uninitialized
				       ^^^^^
			The @NonNull field field may not have been initialized
			----------
			""";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

//default default
public void testDefault05_custom2() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
	runner.customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
	runner.customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "org.foo.NonNullByDefault");
	runner.testFiles =
		new String[] {
			CUSTOM_NULLABLE_NAME,
			CUSTOM_NULLABLE_CONTENT,
			CUSTOM_NONNULL_NAME,
			CUSTOM_NONNULL_CONTENT,
			CUSTOM_NNBD_NAME,
			CUSTOM_NNBD_CONTENT
		};
	runner.runConformTest();
	runner.shouldFlushOutputDirectory = false;
	runner.testFiles =
		new String[] {
			"test/package-info.java",
			"@org.foo.NonNullByDefault\n" +
			"package test;\n",
			"test/X.java",
			"""
				package test;
				public class X {
					Number field; // ERR since uninitialized
					void test1(Number[] ns) {
						ns[0] = null; // OK since not affected by default
					}
					void test2(java.lang.Number[] ns) {
						ns[0] = null; // OK since not affected by default
					}
				}
				"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in test\\X.java (at line 3)
				Number field; // ERR since uninitialized
				       ^^^^^
			The @NonNull field field may not have been initialized
			----------
			""";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// apply default to type parameter - inner class
public void testDefault06() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault(DefaultLocation.TYPE_PARAMETER)
				public class X {
					class Inner<T> {
						T process(T t) {
							@NonNull T t2 = t; // OK
							return null; // ERR
				 		}
					}
					void test(Inner<Number> inum) {
						@NonNull Number nnn = inum.process(null); // ERR on argument
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 7)
				return null; // ERR
				       ^^^^
			Null type mismatch: required \'@NonNull T\' but the provided value is null
			----------
			2. ERROR in X.java (at line 10)
				void test(Inner<Number> inum) {
				                ^^^^^^
			Null constraint mismatch: The type \'Number\' is not a valid substitute for the type parameter \'@NonNull T\'
			----------
			3. ERROR in X.java (at line 11)
				@NonNull Number nnn = inum.process(null); // ERR on argument
				                                   ^^^^
			Null type mismatch: required \'@NonNull Number\' but the provided value is null
			----------
			""");
}

//apply default to type parameter - class above
public void testDefault06_b() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault({DefaultLocation.TYPE_PARAMETER, DefaultLocation.TYPE_ARGUMENT})
					class Inner<T> {
						T process(T t) {
							@NonNull T t2 = t; // OK
							return null; // ERR
				 		}
					}
				@NonNullByDefault({DefaultLocation.TYPE_PARAMETER, DefaultLocation.TYPE_ARGUMENT})
				public class X {
					void test(Inner<Number> inum) {
						@NonNull Number nnn = inum.process(null); // ERR on argument
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 6)
				return null; // ERR
				       ^^^^
			Null type mismatch: required \'@NonNull T\' but the provided value is null
			----------
			2. ERROR in X.java (at line 12)
				@NonNull Number nnn = inum.process(null); // ERR on argument
				                                   ^^^^
			Null type mismatch: required \'@NonNull Number\' but the provided value is null
			----------
			""");
}

// apply default to type bound - method in inner class
public void testDefault07() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				import java.util.*;
				@NonNullByDefault(DefaultLocation.TYPE_BOUND)
				public class X {
					class Inner {
						<T extends Number> T process(T t, List<? extends Number> l) {
							@NonNull T t2 = t; // OK
							@NonNull Number n = l.get(0); // OK
							return null; // ERR
				 		}
					}
					void test(Inner inner) {
						@NonNull Number nnn = inner.process(Integer.valueOf(3), new ArrayList<@Nullable Integer>()); // WARN on 1. arg; ERR on 2. arg
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. INFO in X.java (at line 8)
				@NonNull Number n = l.get(0); // OK
				                    ^^^^^^^^
			Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'List<capture#of ? extends @NonNull Number>\'. Type \'List<E>\' doesn\'t seem to be designed with null type annotations in mind
			----------
			2. ERROR in X.java (at line 9)
				return null; // ERR
				       ^^^^
			Null type mismatch: required \'T extends @NonNull Number\' but the provided value is null
			----------
			3. WARNING in X.java (at line 13)
				@NonNull Number nnn = inner.process(Integer.valueOf(3), new ArrayList<@Nullable Integer>()); // WARN on 1. arg; ERR on 2. arg
				                                    ^^^^^^^^^^^^^^^^^^
			Null type safety (type annotations): The expression of type \'Integer\' needs unchecked conversion to conform to \'@NonNull Integer\'
			----------
			4. ERROR in X.java (at line 13)
				@NonNull Number nnn = inner.process(Integer.valueOf(3), new ArrayList<@Nullable Integer>()); // WARN on 1. arg; ERR on 2. arg
				                                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'List<? extends @NonNull Number>\' but this expression has type \'@NonNull ArrayList<@Nullable Integer>\', corresponding supertype is \'List<@Nullable Integer>\'
			----------
			""");
}

//apply null default to type arguments:
public void testDefault01_bin() {
	runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					import java.util.*;
					import java.lang.annotation.*;
					
					@Target(ElementType.TYPE_USE) @Retention(RetentionPolicy.CLASS) @interface Important {}
					
					@NonNullByDefault(DefaultLocation.TYPE_ARGUMENT)
					public class X {
						List<Number> test1(List<@Important Number> in) {
							return new ArrayList<@NonNull Number>();
						}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. WARNING in X.java (at line 10)
					return new ArrayList<@NonNull Number>();
					                     ^^^^^^^^^^^^^^^
				The nullness annotation is redundant with a default that applies to this location
				----------
				""",
			"");
	runNegativeTestWithLibs(
		new String[] {
			"Y.java",
			"""
				import org.eclipse.jdt.annotation.*;
				import java.util.*;
				public class Y {
					void test(List<Number> in, X x) {
						x.test1(new ArrayList<@Nullable Number>()) // ERR at arg
							.add(null); // ERR
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in Y.java (at line 5)
				x.test1(new ArrayList<@Nullable Number>()) // ERR at arg
				        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'List<@NonNull Number>\' but this expression has type \'@NonNull ArrayList<@Nullable Number>\', corresponding supertype is \'List<@Nullable Number>\'
			----------
			2. ERROR in Y.java (at line 6)
				.add(null); // ERR
				     ^^^^
			Null type mismatch: required \'@NonNull Number\' but the provided value is null
			----------
			""");
}

//apply null default to parameters:
public void testDefault02_bin() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault(DefaultLocation.PARAMETER)
				public class X {
					Number test1(Number in) {
						return null; // OK
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
	runNegativeTestWithLibs(
		new String[] {
			"Y.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Y {
					@NonNull Number test(X x) {
						return x.test1(null); // error at arg, unchecked at return
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in Y.java (at line 4)
				return x.test1(null); // error at arg, unchecked at return
				       ^^^^^^^^^^^^^
			Null type safety (type annotations): The expression of type \'Number\' needs unchecked conversion to conform to \'@NonNull Number\'
			----------
			2. ERROR in Y.java (at line 4)
				return x.test1(null); // error at arg, unchecked at return
				               ^^^^
			Null type mismatch: required \'@NonNull Number\' but the provided value is null
			----------
			""");
}

//apply null default to return type - annotation at method:
public void testDefault03_bin() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runner.classLibraries = this.LIBS;
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
					@NonNullByDefault(DefaultLocation.RETURN_TYPE)
					Number test(Number in) {
						return new Integer(13);
					}
				}
				"""
		};
	runner.javacTestOptions = new JavacTestOptions.SuppressWarnings("deprecation");
	runner.runConformTest();

	runner.shouldFlushOutputDirectory = false;
	runner.testFiles =
		new String[] {
			"Y.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Y {
					@NonNull Number test(X x) {
						return x.test(null); // both OK
					}
				}
				"""
		};
	runner.runConformTest();
}

// apply null default to field - also test mixing of explicit annotation with default @NonNull (other annot is not rendered in error)
public void testDefault04_bin() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runner.classLibraries = this.LIBS;
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				import java.lang.annotation.*;
				@Target(ElementType.TYPE_USE) @Retention(RetentionPolicy.CLASS) @interface Important {}
				@NonNullByDefault(DefaultLocation.FIELD)
				public class X {
					@Important Number field = new Double(1.1);
				}
				"""
		};
	runner.javacTestOptions = new JavacTestOptions.SuppressWarnings("deprecation");
	runner.runConformTest();

	runner.shouldFlushOutputDirectory = false;
	runner.testFiles =
		new String[] {
			"Y.java",
			"""
				public class Y {
					void test(X x) {
						x.field = null; // ERR
					}
				}
				"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in Y.java (at line 3)
				x.field = null; // ERR
				          ^^^^
			Null type mismatch: required \'@NonNull Number\' but the provided value is null
			----------
			""";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// default default
public void testDefault05_bin() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runner.classLibraries = this.LIBS;
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class X {
					Number field = new Long(13);
					void test1(Number[] ns) {
						ns[0] = null; // OK since not affected by default
					}
				}
				"""
		};
	runner.runConformTest();

	runNegativeTestWithLibs(
		new String[] {
			"Y.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Y {
					void test(X x, @Nullable Number @NonNull[] ns) {
						x.test1(ns); // OK since not affected by default
						x.field = null; // ERR
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in Y.java (at line 5)
				x.field = null; // ERR
				          ^^^^
			Null type mismatch: required \'@NonNull Number\' but the provided value is null
			----------
			""");}

// apply default to type parameter - inner class
public void testDefault06_bin() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault(DefaultLocation.TYPE_PARAMETER)
				public class X {
					static class Inner<T> {
						T process(T t) {
							return t;
				 		}
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
	runNegativeTestWithLibs(
		new String[] {
			"Y.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Y {
					void test(X.Inner<Number> inum) { // illegal substitution
						@NonNull Number nnn = inum.process(null); // ERR on argument
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in Y.java (at line 3)
				void test(X.Inner<Number> inum) { // illegal substitution
				                  ^^^^^^
			Null constraint mismatch: The type \'Number\' is not a valid substitute for the type parameter \'@NonNull T extends Object\'
			----------
			2. ERROR in Y.java (at line 4)
				@NonNull Number nnn = inum.process(null); // ERR on argument
				                                   ^^^^
			Null type mismatch: required \'@NonNull Number\' but the provided value is null
			----------
			""");}

// apply default to type bound - method in inner class
public void testDefault07_bin() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				import java.util.*;
				@NonNullByDefault(DefaultLocation.TYPE_BOUND)
				public class X {
					static class Inner {
						<T extends Number> T process(T t, List<? extends Number> l) {
							return t;
				 		}
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
	runNegativeTestWithLibs(
		new String[] {
			"Y.java",
			"""
				import org.eclipse.jdt.annotation.*;
				import java.util.*;
				public class Y {
					void test(X.Inner inner) {
						@NonNull Number nnn = inner.process(Integer.valueOf(3), new ArrayList<@Nullable Integer>()); // WARN on 1. arg; ERR on 2. arg
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in Y.java (at line 5)
				@NonNull Number nnn = inner.process(Integer.valueOf(3), new ArrayList<@Nullable Integer>()); // WARN on 1. arg; ERR on 2. arg
				                                    ^^^^^^^^^^^^^^^^^^
			Null type safety (type annotations): The expression of type \'Integer\' needs unchecked conversion to conform to \'@NonNull Integer\'
			----------
			2. ERROR in Y.java (at line 5)
				@NonNull Number nnn = inner.process(Integer.valueOf(3), new ArrayList<@Nullable Integer>()); // WARN on 1. arg; ERR on 2. arg
				                                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'List<? extends @NonNull Number>\' but this expression has type \'@NonNull ArrayList<@Nullable Integer>\', corresponding supertype is \'List<@Nullable Integer>\'
			----------
			""");
}
public void testBug431269() {
	runNegativeTest(
		new String[] {
			"p/QField.java",
			"""
				package p;
				
				import org.eclipse.jdt.annotation.*;
				
				public class QField<R extends QField<R, ? >, T> {
					@NonNull
					protected R m_root;
				
					public QField(@Nullable R root, @Nullable QField<R, ? > parent, @Nullable String propertyNameInParent) {
						m_root = root;
					}
				}
				""",
			"p/PLogLine.java",
			"""
				package p;
				
				import org.eclipse.jdt.annotation.*;
				
				public class PLogLine<R extends QField<R, ? >> extends QField<R, LogLine> {
					public PLogLine(@Nullable R root, @Nullable QField<R, ? > parent, @Nullable String name) {
						super(root, parent, name);
					}
				
					@NonNull
					public final QField<R, java.lang.String> lastName() {
						return new QField<R, java.lang.Long>(m_root, this, "lastName");
					}
				
				}
				""",
			"p/LogLine.java",
			"""
				package p;
				
				public class LogLine {
					private String m_lastName;
				
					public String getLastName() {
						return m_lastName;
					}
				
					public void setLastName(String property) {
						m_lastName = property;
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in p\\QField.java (at line 10)
				m_root = root;
				         ^^^^
			Null type mismatch (type annotations): required \'@NonNull R extends QField<R extends QField<R,?>,?>\' but this expression has type \'@Nullable R extends QField<R extends QField<R,?>,?>\'
			----------
			----------
			1. ERROR in p\\PLogLine.java (at line 12)
				return new QField<R, java.lang.Long>(m_root, this, "lastName");
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from QField<R,Long> to QField<R,String>
			----------
			""",
		this.LIBS,
		true/*flush*/);
}
// was inferring null type annotations too aggressively
public void testBug432223() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
					String val;
					public static @NonNull <T> T assertNotNull(@Nullable T object) {
						return assertNotNull(null, object);
					}
				
					public static @NonNull <T> T assertNotNull(@Nullable String message, @Nullable T object) {
						if (object == null) {
							throw new NullPointerException(message);
						}
						return object;
					}
					void test(@Nullable X x) {
						@NonNull X safe = assertNotNull(x);
						System.out.println(safe.val);
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug432977() {
	runConformTestWithLibs(
		new String[] {
			"Bar.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class Bar {
					private String prop = "";
				
					public String getProp() {
						return prop;
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
	runConformTestWithLibs(
		false /* flush */,
		new String[] {
			"Fu.java",
			"""
				public class Fu {
					private Bar fubar = new Bar();
				\t
					public void method() {
						fubar.getProp().equals("");
					}\t
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug433586() {
	runConformTestWithLibs(
		new String[] {
			"NullConversionWarning.java",
			"""
				import java.util.function.Consumer;
				public class NullConversionWarning<T> {
				
					public Consumer<T> peek2(Consumer<? super T> action) {
						// Null type safety: parameter 1 provided via
						// method descriptor Consumer<T>.accept(T) needs
						// unchecked conversion to conform to 'capture#of ? super T'
						Consumer<T> action2 = action::accept;
						return action2;
					}
					void foo(Consumer<? super T> action, T t) {
					  Consumer<T> action2 = t2 -> action.accept(t2);
					  action.accept(t);
					  action2.accept(t);
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
// NPE without the fix.
public void testBug433478() {
	runNegativeTestWithLibs(
            new String[] {
                "X.java",
                """
					import org.eclipse.jdt.annotation.NonNullByDefault;
					import org.eclipse.jdt.annotation.Nullable;
					
					@NonNullByDefault class Y { }
					
					interface I<T> {
					       @Nullable T foo();
					}
					
					@NonNullByDefault\s
					class X implements I<Y> {
					       @Override
					       public Y foo() {
					               return null;
					       }
					}
					"""
            },
            """
				----------
				1. ERROR in X.java (at line 14)
					return null;
					       ^^^^
				Null type mismatch: required \'@NonNull Y\' but the provided value is null
				----------
				""");
}
// https://bugs.eclipse.org/434899
public void testTypeVariable6() {
	runNegativeTestWithLibs(
		new String[] {
			"Assert.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Assert {
					public static void caller() {
						assertNotNull("not null");	// Compiler error
						assertNotNull(null);		// Compiler error
					}
					private static @NonNull <T> T assertNotNull(@Nullable T object) {
						return object; // this IS bogus
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in Assert.java (at line 8)
				return object; // this IS bogus
				       ^^^^^^
			Null type mismatch (type annotations): required \'@NonNull T\' but this expression has type \'@Nullable T\'
			----------
			""");
}
// https://bugs.eclipse.org/434899 - variant which has always worked
public void testTypeVariable6a() {
	runConformTestWithLibs(
		new String[] {
			"Assert.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Assert {
					public static Object caller() {
						@NonNull Object result = assertNotNull("not null");
						result = assertNotNull(null);
						return result;
					}
					private static @NonNull <T> T assertNotNull(@Nullable T object) {
						if (object == null) throw new NullPointerException();
						return object;
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
// Bug 438458 - [1.8][null] clean up handling of null type annotations wrt type variables
// - type parameter with explicit nullness, cannot infer otherwise
public void testTypeVariable7() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				interface I1 <@NonNull T> { T get(); }
				public class X {
					<U> U m(I1<U> in) { return in.get(); }
					public void test(I1<@NonNull String> in) {
						@NonNull String s = m(in);
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 4)
				<U> U m(I1<U> in) { return in.get(); }
				           ^
			Null constraint mismatch: The type \'U\' is not a valid substitute for the type parameter \'@NonNull T\'
			----------
			""");
}
// Bug 438458 - [1.8][null] clean up handling of null type annotations wrt type variables
// - type parameter with explicit nullness, nullness must not spoil inference
public void testTypeVariable7a() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.WARNING); // allow ignoring bad substitution
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				interface I1 <@NonNull T> { T get(); }
				public class X {
					<U> U m(I1<U> in) { return in.get(); }
					public void test1() {
						@Nullable String s = m(() -> "OK");
						System.out.println(s);
					}
					public static void main(String[] args) {
						new X().test1();
					}
				}
				"""
		},
		compilerOptions,
		"""
			----------
			1. WARNING in X.java (at line 4)
				<U> U m(I1<U> in) { return in.get(); }
				           ^
			Null constraint mismatch: The type \'U\' is not a valid substitute for the type parameter \'@NonNull T\'
			----------
			2. WARNING in X.java (at line 6)
				@Nullable String s = m(() -> "OK");
				                       ^^^^^^^^^^
			Contradictory null annotations: function type was inferred as \'@NonNull @Nullable String ()\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location
			----------
			""",
		"OK");
}
// Bug 438458 - [1.8][null] clean up handling of null type annotations wrt type variables
// - type parameter with explicit nullness, nullness must not spoil inference
public void testTypeVariable7err() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				interface I1 <@Nullable T> { T get(); }
				public class X {
					<U> U m(I1<U> in) { return in.get(); }
					public void test1() {
						@NonNull String s = m(() -> "");
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 4)
				<U> U m(I1<U> in) { return in.get(); }
				           ^
			Null constraint mismatch: The type \'U\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			2. ERROR in X.java (at line 4)
				<U> U m(I1<U> in) { return in.get(); }
				                           ^^^^^^^^
			Null type mismatch (type annotations): required \'U\' but this expression has type \'@Nullable U\', where \'U\' is a free type variable
			----------
			3. ERROR in X.java (at line 6)
				@NonNull String s = m(() -> "");
				                      ^^^^^^^^
			Contradictory null annotations: function type was inferred as \'@Nullable @NonNull String ()\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location
			----------
			""");
}
//Bug 435570 - [1.8][null] @NonNullByDefault illegally tries to affect "throws E"
public void testTypeVariable8() {
	runConformTestWithLibs(
		new String[] {
			"Test.java",
			"""
				@org.eclipse.jdt.annotation.NonNullByDefault
				public class Test<E extends Exception> {
					void test() throws E {}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
// Bug 438012 - Bogus Warning: The nullness annotation is redundant with a default that applies to this location
public void testTypeVariable9() {
	runConformTestWithLibs(
		new String[] {
			"Bar.java",
			"""
				import org.eclipse.jdt.annotation.NonNullByDefault;
				import static org.eclipse.jdt.annotation.DefaultLocation.*;
				
				@NonNullByDefault({ PARAMETER, RETURN_TYPE, FIELD, TYPE_BOUND, TYPE_ARGUMENT, TYPE_PARAMETER })
				interface Bar<V> {
				    V getV(V in);
				    void setV(V v);
				}"""
		},
		getCompilerOptions(),
		"");
}
// Bug 439516 - [1.8][null] NonNullByDefault wrongly applied to implicit type bound of binary type
public void testTypeVariable10() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				@org.eclipse.jdt.annotation.NonNullByDefault
				public class X<T> {
					void test(T t) {}
				}
				"""
		},
		getCompilerOptions(),
		"");
	runConformTestWithLibs(
		false,
		new String[] {
			"Y.java",
			"""
				public class Y {
					void foo(X<@org.eclipse.jdt.annotation.Nullable String> xs) {
						xs.test(null);
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
// Bug 439516 - [1.8][null] NonNullByDefault wrongly applied to implicit type bound of binary type
// Problem 1 from: Bug 438971 - [1.8][null] @NonNullByDefault/@Nullable on parameter of generic interface
public void testTypeVariable10a() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class X<T> {
					void test(@Nullable T t) {}
				}
				"""
		},
		getCompilerOptions(),
		"");
	runConformTestWithLibs(
		false,
		new String[] {
			"Y.java",
			"""
				public class Y {
					void foo(X<String> xs) {
						xs.test("OK");
						xs.test(null);
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
// Bug 439516 - [1.8][null] NonNullByDefault wrongly applied to implicit type bound of binary type
// warning for explicit "<T extends Object>"
public void testTypeVariable11() {
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.DefaultLocation;
				@org.eclipse.jdt.annotation.NonNullByDefault({DefaultLocation.TYPE_BOUND})
				public class X<T extends Object> {
					void test(T t) {}
				}
				""",
			"Y.java",
			"""
				public class Y {
					void foo(X<@org.eclipse.jdt.annotation.Nullable String> xs) {
						xs.test(null);
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in X.java (at line 3)
				public class X<T extends Object> {
				                         ^^^^^^
			The explicit type bound \'Object\' is not affected by the nullness default for DefaultLocation.TYPE_BOUND.
			----------
			""");
}
// Bug 438179 - [1.8][null] 'Contradictory null annotations' error on type variable with explicit null-annotation.
public void testTypeVariable12() {
	runConformTestWithLibs(
		new String[] {
			"Test.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class Test {
					private Fu<String> fu = new Fu<>();
					public void foo() {
						fu.method();   // 'Contradictory null annotations' error
					}
				}
				class Fu<T> {
					@Nullable T method() {
						return null;
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
// Bug 438250 - [1.8][null] NPE trying to report bogus null annotation conflict
public void testTypeVariable13() {
	runConformTestWithLibs(
		new String[] {
			"FooBar.java",
			"""
				@org.eclipse.jdt.annotation.NonNullByDefault(org.eclipse.jdt.annotation.DefaultLocation.TYPE_BOUND)
				public interface FooBar {
				    <@org.eclipse.jdt.annotation.Nullable R extends Runnable> R foobar(R r);
				}
				"""
		},
		getCompilerOptions(),
		"");
}
// Bug 438469 - [null] How-to use null type annotations with generic methods from interfaces in some library you only have as binary JAR?
public void testTypeVariable14() {
	runConformTestWithLibs(
		new String[] {
			"ITest.java",
			"""
				interface ITest {
					<T> T foo(T arg); // or arg Class<T> or TypeToken<T> + return TypeAdapter<T>, etc.
				}"""
		},
		getCompilerOptions(),
		"");
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SUPPRESS_OPTIONAL_ERRORS, JavaCore.ENABLED);
	runConformTestWithLibs(
		false,
		new String[] {
			"Test.java",
			"""
				class Test implements ITest {
					@Override
					@SuppressWarnings("null")
					public <T> @org.eclipse.jdt.annotation.Nullable T foo(T arg) {
						return null;
					}
				}
				"""
		},
		options,
		"");
}
// Bug 438467 - [compiler][null] Better error position for "The method _ cannot implement the corresponding method _ due to incompatible nullness constraints"
public void testTypeVariable15() {
	runNegativeTestWithLibs(
		new String[] {
			"ITest.java",
			"""
				interface ITest {
					<T> T foo(T arg); // or arg Class<T> or TypeToken<T> + return TypeAdapter<T>, etc.
				}""",
			"Test.java",
			"""
				class Test implements ITest {
					@Override
					public <T> @org.eclipse.jdt.annotation.Nullable T foo(T arg) {
						return null;
					}
				}
				""",
			"Test2.java",
			"""
				class Test2 implements ITest {
					@Override
					public <T> T foo(@org.eclipse.jdt.annotation.NonNull T arg) {
						return arg;
					}
				}
				""",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in Test.java (at line 3)
				public <T> @org.eclipse.jdt.annotation.Nullable T foo(T arg) {
				           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The return type is incompatible with the free type variable 'T' returned from ITest.foo(T) (mismatching null constraints)
			----------
			----------
			1. ERROR in Test2.java (at line 3)
				public <T> T foo(@org.eclipse.jdt.annotation.NonNull T arg) {
				                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Illegal redefinition of parameter arg, inherited method from ITest does not constrain this parameter
			----------
			""");
}
// Bug 438467 - [compiler][null] Better error position for "The method _ cannot implement the corresponding method _ due to incompatible nullness constraints"
public void testTypeVariable15a() {
	runNegativeTestWithLibs(
		new String[] {
			"ITest.java",
			"""
				import java.util.List;
				interface ITest {
					<T> T foo(List<T> arg); // or arg Class<T> or TypeToken<T> + return TypeAdapter<T>, etc.
				}""",
			"Test.java",
			"""
				import java.util.List;
				class Test implements ITest {
					@Override
					public <T> T foo(List<@org.eclipse.jdt.annotation.NonNull T> arg) {
						return arg.get(0);
					}
				}
				""",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in Test.java (at line 4)
				public <T> T foo(List<@org.eclipse.jdt.annotation.NonNull T> arg) {
				                 ^^^^
			Illegal redefinition of parameter arg, inherited method from ITest declares this parameter as \'List<T>\' (mismatching null constraints)
			----------
			2. INFO in Test.java (at line 5)
				return arg.get(0);
				       ^^^^^^^^^^
			Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'List<@NonNull T>\'. Type \'List<E>\' doesn\'t seem to be designed with null type annotations in mind
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=434602
// Possible error with inferred null annotations leading to contradictory null annotations
public void testTypeVariable16() {
	runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.NonNullByDefault;
					import org.eclipse.jdt.annotation.Nullable;
					
					class Y { void doit() {} }
					@NonNullByDefault
					class X {
						void foo() {
							X x = new X();
							Y y = x.bar(); // Error: Contradictory null annotations before the fix
							y.doit(); // check that @Nullable from bar's declaration has effect on 'y'
						}
					
						public <T extends Y> @Nullable T bar() {
							return null;
						}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in X.java (at line 10)
					y.doit(); // check that @Nullable from bar's declaration has effect on 'y'
					^
				Potential null pointer access: The variable y may be null at this location
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=434602
// Possible error with inferred null annotations leading to contradictory null annotations
// Method part of parameterized class.
public void testTypeVariable16a() {
	runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.NonNullByDefault;
					import org.eclipse.jdt.annotation.Nullable;
					
					class Y {}
					@NonNullByDefault
					public class X <T> {
						void foo() {
							X<Y> x = new X<Y>();
							x.bar(); // Error: Contradictory null annotations before the fix
						}
					
						public @Nullable T bar() {
							return null;
						}
					}
					"""
			},
			getCompilerOptions(),
			"");
}
public void testTypeVariable16b() {
	runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.Nullable;
					import org.eclipse.jdt.annotation.NonNull;
					
					class Y {}
					class Y2 extends Y {}
					
					class X {
						void foo() {
							X x = new X();
							x.bar(null); // null arg is illegal
						}
						public <T extends @NonNull Y> @Nullable T bar(T t) {
							return null; // OK
						}
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in X.java (at line 10)
					x.bar(null); // null arg is illegal
					      ^^^^
				Null type mismatch: required \'@NonNull Y\' but the provided value is null
				----------
				""");
}
// Bug 440143 - [1.8][null] one more case of contradictory null annotations regarding type variables
public void testTypeVariable17() {
	runNegativeTestWithLibs(
		new String[] {
			"Test7.java",
			"""
				import org.eclipse.jdt.annotation.*;
				
				public class Test7<@Nullable E> {
					E e;
				
					@Nullable
					E test() {
						return null;
					}
				
					@NonNull
					E getNotNull() {
						if (e == null)
							throw new NullPointerException();
						return e;
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in Test7.java (at line 15)
				return e;
				       ^
			Null type mismatch (type annotations): required \'@NonNull E\' but this expression has type \'@Nullable E\'
			----------
			""");
}
// Bug 440143 - [1.8][null] one more case of contradictory null annotations regarding type variables
// use local variable to avoid the null type mismatch
public void testTypeVariable17a() {
	runConformTestWithLibs(
		new String[] {
			"Test7.java",
			"""
				import org.eclipse.jdt.annotation.*;
				
				public class Test7<@Nullable E> {
					E e;
				
					@Nullable
					E test() {
						return null;
					}
				
					@NonNull
					E getNotNull() {
						E el = e;
						if (el == null)
							throw new NullPointerException();
						return el;
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
// NPE reported in https://bugs.eclipse.org/bugs/show_bug.cgi?id=438458#c5
public void testTypeVariable18() {
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"""
				import java.util.*;
				import org.eclipse.jdt.annotation.*;
				
				interface Lib1 {
				    <T extends Collection<?>> T constrainedTypeParameter(@NonNull T in);
				}
				
				public class Test {
				  @NonNull Collection<?> test4(Lib1 lib, @Nullable Collection<String> in) {
				    return lib.constrainedTypeParameter(in);
				  }
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in Test.java (at line 10)
				return lib.constrainedTypeParameter(in);
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type safety (type annotations): The expression of type \'Collection<String>\' needs unchecked conversion to conform to \'@NonNull Collection<?>\'
			----------
			2. ERROR in Test.java (at line 10)
				return lib.constrainedTypeParameter(in);
				                                    ^^
			Null type mismatch (type annotations): required \'@NonNull Collection<String>\' but this expression has type \'@Nullable Collection<String>\'
			----------
			""");
}
public void testTypeVariable18raw() {
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"""
				import java.util.*;
				import org.eclipse.jdt.annotation.*;
				
				interface Lib1 {
				    <T extends Collection<?>> T constrainedTypeParameter(@NonNull T in);
				}
				
				public class Test {
				  @SuppressWarnings("rawtypes")
				  @NonNull Collection test4(Lib1 lib, @Nullable Collection in) {
				    return lib.constrainedTypeParameter(in);
				  }
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in Test.java (at line 11)
				return lib.constrainedTypeParameter(in);
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type safety (type annotations): The expression of type \'Collection\' needs unchecked conversion to conform to \'@NonNull Collection\'
			----------
			2. ERROR in Test.java (at line 11)
				return lib.constrainedTypeParameter(in);
				                                    ^^
			Null type mismatch (type annotations): required \'@NonNull Collection\' but this expression has type \'@Nullable Collection\'
			----------
			""");
}
// top-level annotation is overridden at use-site, details remain - parameterized type
public void testTypeVariable19() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportNonNullTypeVariableFromLegacyInvocation, CompilerOptions.IGNORE);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				
				import org.eclipse.jdt.annotation.NonNull;
				import org.eclipse.jdt.annotation.Nullable;
				interface I<T,U extends List<T>> {
					U get0();
					@Nullable U get1();
					@NonNull U get2();
				}
				class X {
					static String test (I<@Nullable String, @NonNull ArrayList<@Nullable String>> i1,
										I<@NonNull String, @Nullable ArrayList<@NonNull String>> i2, int s) {
						switch(s) {
							case 0 : return i1.get0().get(0).toUpperCase(); // problem at detail
							case 1 : return i1.get1().get(0).toUpperCase(); // 2 problems
							case 2 : return i1.get2().get(0).toUpperCase(); // problem at detail
							case 3 : return i2.get0().get(0).toUpperCase(); // problem at top
							case 4 : return i2.get1().get(0).toUpperCase(); // problem at top
							case 5 : return i2.get2().get(0).toUpperCase(); // OK
							default : return "";\
						}
					}
				}
				"""
		},
		compilerOptions,
		"""
			----------
			1. ERROR in X.java (at line 15)
				case 0 : return i1.get0().get(0).toUpperCase(); // problem at detail
				                ^^^^^^^^^^^^^^^^
			Potential null pointer access: The method get(int) may return null
			----------
			2. ERROR in X.java (at line 16)
				case 1 : return i1.get1().get(0).toUpperCase(); // 2 problems
				                ^^^^^^^^^
			Potential null pointer access: The method get1() may return null
			----------
			3. ERROR in X.java (at line 16)
				case 1 : return i1.get1().get(0).toUpperCase(); // 2 problems
				                ^^^^^^^^^^^^^^^^
			Potential null pointer access: The method get(int) may return null
			----------
			4. ERROR in X.java (at line 17)
				case 2 : return i1.get2().get(0).toUpperCase(); // problem at detail
				                ^^^^^^^^^^^^^^^^
			Potential null pointer access: The method get(int) may return null
			----------
			5. ERROR in X.java (at line 18)
				case 3 : return i2.get0().get(0).toUpperCase(); // problem at top
				                ^^^^^^^^^
			Potential null pointer access: The method get0() may return null
			----------
			6. ERROR in X.java (at line 19)
				case 4 : return i2.get1().get(0).toUpperCase(); // problem at top
				                ^^^^^^^^^
			Potential null pointer access: The method get1() may return null
			----------
			""");
}
// top-level annotation is overridden at use-site, array with anotations on dimensions
public void testTypeVariable19a() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.NonNull;
				import org.eclipse.jdt.annotation.Nullable;
				interface I1<T> {
					T @Nullable[] get0();
					@Nullable T @NonNull[] get1();
					@Nullable T @Nullable[] get2();
				}
				interface I2<T> {
					T @NonNull[] get0();
					@NonNull T @NonNull[] get1();
					@NonNull T @Nullable[] get2();
				}
				class X {
					static String test (I1<@NonNull String> i1, I2<@Nullable String> i2, int s) {
						switch (s) {
							case 0: return i1.get0()[0].toUpperCase(); // problem on array
							case 1: return i1.get1()[0].toUpperCase(); // problem on element
							case 2: return i1.get2()[0].toUpperCase(); // 2 problems
							case 3: return i2.get0()[0].toUpperCase(); // problem on element
							case 4: return i2.get1()[0].toUpperCase(); // OK
							case 5: return i2.get2()[0].toUpperCase(); // problem on array
							default: return "";
						}
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 16)
				case 0: return i1.get0()[0].toUpperCase(); // problem on array
				               ^^^^^^^^^
			Potential null pointer access: The method get0() may return null
			----------
			2. ERROR in X.java (at line 17)
				case 1: return i1.get1()[0].toUpperCase(); // problem on element
				               ^^^^^^^^^^^^
			Potential null pointer access: array element may be null
			----------
			3. ERROR in X.java (at line 18)
				case 2: return i1.get2()[0].toUpperCase(); // 2 problems
				               ^^^^^^^^^
			Potential null pointer access: The method get2() may return null
			----------
			4. ERROR in X.java (at line 18)
				case 2: return i1.get2()[0].toUpperCase(); // 2 problems
				               ^^^^^^^^^^^^
			Potential null pointer access: array element may be null
			----------
			5. ERROR in X.java (at line 19)
				case 3: return i2.get0()[0].toUpperCase(); // problem on element
				               ^^^^^^^^^^^^
			Potential null pointer access: array element may be null
			----------
			6. ERROR in X.java (at line 21)
				case 5: return i2.get2()[0].toUpperCase(); // problem on array
				               ^^^^^^^^^
			Potential null pointer access: The method get2() may return null
			----------
			""");
}
public void testTypeVariable20() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.Nullable;
				interface I<@Nullable T> { }
				public class X implements I<String> {}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 3)
				public class X implements I<String> {}
				                            ^^^^^^
			Null constraint mismatch: The type \'String\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			""");
}
public void testBug434600() {
	runConformTestWithLibs(
		new String[] {
			"bug/Main.java",
			"""
				package bug;
				public class Main {
					public static void main(final String[] args) {
						System.out.println("Hello World");
					}
				}
				""",
			"bug/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package bug;\n",
			"bug/ExpressionNode.java",
			"""
				package bug;
				
				public interface ExpressionNode extends CopyableNode<ExpressionNode> {
				\t
				}
				""",
			"bug/ExtendedNode.java",
			"""
				package bug;
				
				public interface ExtendedNode {
				\t
				}
				""",
			"bug/CopyableNode.java",
			"""
				package bug;
				
				public interface CopyableNode<T extends ExtendedNode> extends ExtendedNode {
				\t
				}
				"""
		},
		getCompilerOptions(),
		"",
		"Hello World");
}
public void testBug434600a() {
	runConformTestWithLibs(
		new String[] {
			"I.java",
			"""
				import java.util.*;
				import org.eclipse.jdt.annotation.*;
				interface I<S, T extends @Nullable List<@NonNull List<S>>> {
				}
				""",
			"C.java",
			"""
				import java.util.*;
				import org.eclipse.jdt.annotation.*;
				public class C implements I<@Nullable String, @Nullable ArrayList<@NonNull List<@Nullable String>>> {}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug434600a_qualified() {
	runConformTestWithLibs(
		new String[] {
			"p/I.java",
			"""
				package p;
				import java.util.*;
				import org.eclipse.jdt.annotation.*;
				public interface I<S, T extends @Nullable List<@NonNull List<S>>> {
				}
				""",
			"C.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class C implements p.I<java.lang.@Nullable String, java.util.@Nullable ArrayList<java.util.@NonNull List<java.lang.@Nullable String>>> {}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug434600b() {
	runNegativeTestWithLibs(
		new String[] {
			"I.java",
			"""
				import java.util.*;
				import org.eclipse.jdt.annotation.*;
				interface I<S, T extends @NonNull List<@NonNull List<S>>> {
				}
				""",
			"C.java",
			"""
				import java.util.*;
				import org.eclipse.jdt.annotation.*;
				public class C implements I<@Nullable String, ArrayList<@NonNull List<@Nullable String>>> {}
				class C1 {
					I<String, @NonNull ArrayList<@Nullable List<String>>> field;
				}
				class C2 implements I<@NonNull String, @NonNull ArrayList<@NonNull List<@Nullable String>>> {}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in C.java (at line 3)
				public class C implements I<@Nullable String, ArrayList<@NonNull List<@Nullable String>>> {}
				                                              ^^^^^^^^^
			Null constraint mismatch: The type \'ArrayList<@NonNull List<@Nullable String>>\' is not a valid substitute for the type parameter \'T extends @NonNull List<@NonNull List<S>>\'
			----------
			2. ERROR in C.java (at line 5)
				I<String, @NonNull ArrayList<@Nullable List<String>>> field;
				          ^^^^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'@NonNull ArrayList<@Nullable List<String>>\' is not a valid substitute for the type parameter \'T extends @NonNull List<@NonNull List<S>>\'
			----------
			3. ERROR in C.java (at line 7)
				class C2 implements I<@NonNull String, @NonNull ArrayList<@NonNull List<@Nullable String>>> {}
				                                       ^^^^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'@NonNull ArrayList<@NonNull List<@Nullable String>>\' is not a valid substitute for the type parameter \'T extends @NonNull List<@NonNull List<S>>\'
			----------
			""");
}
public void testBug434600b_qualified() {
	runNegativeTestWithLibs(
		new String[] {
			"p/I.java",
			"""
				package p;
				import java.util.*;
				import org.eclipse.jdt.annotation.*;
				public interface I<S, T extends @Nullable List<@NonNull List<S>>> {
				}
				""",
			"C.java",
			"""
				import java.util.*;
				import org.eclipse.jdt.annotation.*;
				public class C implements p.I<@Nullable String, ArrayList<@NonNull List<@Nullable String>>> {}
				class C1 {
					p.I<String, @Nullable ArrayList<@Nullable List<String>>> field;
				}
				class C2 implements p.I<@NonNull String, @Nullable ArrayList<@NonNull List<@Nullable String>>> {}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in C.java (at line 5)
				p.I<String, @Nullable ArrayList<@Nullable List<String>>> field;
				            ^^^^^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'@Nullable ArrayList<@Nullable List<String>>\' is not a valid substitute for the type parameter \'T extends @Nullable List<@NonNull List<S>>\'
			----------
			2. ERROR in C.java (at line 7)
				class C2 implements p.I<@NonNull String, @Nullable ArrayList<@NonNull List<@Nullable String>>> {}
				                                         ^^^^^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'@Nullable ArrayList<@NonNull List<@Nullable String>>\' is not a valid substitute for the type parameter \'T extends @Nullable List<@NonNull List<S>>\'
			----------
			""");
}
public void testBug435399() {
	runConformTestWithLibs(
		new String[] {
			"bug/Bug1.java",
			"""
				package bug;
				
				import org.eclipse.jdt.annotation.Nullable;
				
				public class Bug1 {
				    public static <T> void method(@Nullable T value, T defaultValue) {
				    }
				    public void invoke() {
				        method(Integer.valueOf(1), Boolean.TRUE);
				    }
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug435962() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_ANNOTATION, JavaCore.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"interfaces/CopyableNode.java",
			"""
				package interfaces;
				public interface CopyableNode<T extends ExtendedNode> extends ExtendedNode {
					public T deepCopy();
				}
				""",
			"interfaces/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package interfaces;\n",
			"interfaces/ExtendedNode.java",
			"""
				package interfaces;
				import java.util.ArrayList;
				import org.eclipse.jdt.annotation.Nullable;
				public interface ExtendedNode {
					ExtendedNode getParent();
					void setParent(ExtendedNode newParent);
					int numChildren();
					void mutateNode(ExtendedNode root);
					void getAllNodes(ArrayList<ExtendedNode> array);
					ExtendedNode getNode(int nodeIndex);
					<N extends ExtendedNode> void getNodesOfType(Class<N> desiredType,
							ArrayList<N> array);
					<N extends ExtendedNode> @Nullable N getRandomNodeOfType(
							Class<N> desiredType, ExtendedNode root, ExtendedNode caller);
				}
				""",
			"interfaces/ValueNode.java",
			"""
				package interfaces;
				public interface ValueNode extends ExtendedNode {
				}
				""",
			"framework/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package framework;\n",
			"framework/BinaryOpNode.java",
			"""
				package framework;
				
				import interfaces.CopyableNode;
				import interfaces.ValueNode;
				public abstract class BinaryOpNode<T extends ValueNode & CopyableNode<T>, O>
						extends EqualBinaryNode<T> implements ValueNode {
					@SuppressWarnings("unused") private O op;
				\t
					protected BinaryOpNode(final T left, @org.eclipse.jdt.annotation.NonNull final O op, final T right) {
						super(left, right);
						this.op = op;
					}
				}
				""",
			"framework/EqualBinaryNode.java",
			"""
				package framework;
				
				import interfaces.CopyableNode;
				import interfaces.ExtendedNode;
				public abstract class EqualBinaryNode<T extends ExtendedNode & CopyableNode<T>>
						implements ExtendedNode {
					protected T left;
					protected T right;
				\t
					protected EqualBinaryNode(final T left, final T right) {
						this.left = left;
						this.right = right;
					}
				}
				"""
		},
		options,
		"");
}
public void testBug440462() {
	runConformTestWithLibs(
		new String[]{
			"CompilerError.java",
			"""
				import org.eclipse.jdt.annotation.NonNullByDefault;
				import org.eclipse.jdt.annotation.Nullable;
				import java.util.*;
				@NonNullByDefault
				public class CompilerError {
				
				    List<@Nullable ? extends Integer> list = new ArrayList<@Nullable Integer>();
				
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug440773() {
	runConformTestWithLibs(
		new String[] {
			"CountingComparator.java",
			"""
				import java.util.Comparator;
				
				import org.eclipse.jdt.annotation.*;
				
				
				@NonNullByDefault
				public class CountingComparator<T> implements Comparator<T> {
				
				    private int m_accessCount = 0;
				
				    private final Comparator<T> m_wrapped;
				
				    public CountingComparator(final Comparator<T> wrapped) {
				        m_wrapped = wrapped;
				    }
				
				    @Override
				    @NonNullByDefault(DefaultLocation.RETURN_TYPE)
				    public int compare(final T element1, final T element2) {
				        m_accessCount++;
				        return m_wrapped.compare(element1, element2);
				    }
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug439298_comment2() {
	runConformTestWithLibs(
		new String[] {
			"Extract.java",
			"""
				import org.eclipse.jdt.annotation.*;
				
				class R<T> {
					R(@Nullable T t) {}
				}
				class A {}
				@NonNullByDefault
				public class Extract {
					R<A> test() {
						return new R<A>(null);
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug439298_comment3() {
	runWarningTestWithLibs(
		true,
		new String[] {
			"Extract.java",
			"""
				import org.eclipse.jdt.annotation.*;
				
				class R<T> {
					R(@Nullable T t) {}
				}
				class A {}
				public class Extract {
					R<A> test() {
						return new R<@NonNull A>(null);
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. INFO in Extract.java (at line 9)
				return new R<@NonNull A>(null);
				       ^^^^^^^^^^^^^^^^^^^^^^^
			Unsafe null type conversion (type annotations): The value of type '@NonNull R<@NonNull A>' is made accessible using the less-annotated type 'R<A>'
			----------
			""");
}
public void testBug439298_comment4() {
	runConformTestWithLibs(
		new String[] {
			"Extract.java",
			"""
				import org.eclipse.jdt.annotation.*;
				
				class R<T> {
				    R(@Nullable T t) {}
				}
				class A {}
				public class Extract {
				    R<@NonNull A> test() {
				        return new R<>(null);
				    }
				}
				"""
		},
		getCompilerOptions(),
		"");
}
// this code raised: java.lang.IllegalArgumentException: Type doesn't have its own method?
// at org.eclipse.jdt.internal.compiler.lookup.SyntheticFactoryMethodBinding.applyTypeArgumentsOnConstructor(SyntheticFactoryMethodBinding.java:40)
public void testBug440764() {
	runNegativeTestWithLibs(
		new String[] {
			"Extract.java",
			"""
				import java.util.Comparator;
				
				import org.eclipse.jdt.annotation.*;
				
				@NonNullByDefault({ DefaultLocation.TYPE_PARAMETER })
				public class Extract<T> implements Comparator<@NonNull T>  {
					public Extract(Comparator<T> wrapped) {
					}
				
					@Override
					public int compare(T o1, T o2) {
						return 0;
					}
				\t
					void test(final Comparator<@Nullable Integer> c) {
						new Extract<>(c).compare(1, null);
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in Extract.java (at line 16)
				new Extract<>(c).compare(1, null);
				              ^
			Null type mismatch (type annotations): required \'Comparator<@NonNull Integer>\' but this expression has type \'Comparator<@Nullable Integer>\'
			----------
			2. ERROR in Extract.java (at line 16)
				new Extract<>(c).compare(1, null);
				                            ^^^^
			Null type mismatch: required \'@NonNull Integer\' but the provided value is null
			----------
			""");
}
public void testBug440759a() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class X<T> {
					T test(T t) {
						@NonNull T localT = t; // err#1
						return null; // err must mention free type variable, not @NonNull
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 5)
				@NonNull T localT = t; // err#1
				                    ^
			Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			2. ERROR in X.java (at line 6)
				return null; // err must mention free type variable, not @NonNull
				       ^^^^
			Null type mismatch (type annotations): \'null\' is not compatible to the free type variable \'T\'
			----------
			""");
}
// involves overriding, work done in ImplicitNullAnnotationVerifier.checkNullSpecInheritance()
public void testBug440759b() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				interface Y<T> {
					T test(T t);
				}
				@NonNullByDefault
				public class X<T> implements Y<T> {
					public T test(T t) {
						@NonNull T localT = t; // err#1
						return null; // err must mention free type variable, not @NonNull
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 8)
				@NonNull T localT = t; // err#1
				                    ^
			Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			2. ERROR in X.java (at line 9)
				return null; // err must mention free type variable, not @NonNull
				       ^^^^
			Null type mismatch (type annotations): \'null\' is not compatible to the free type variable \'T\'
			----------
			""");
}
public void testBug438383() {
	runConformTestWithLibs(
		new String[] {
			"Foo.java",
			"""
				import java.util.*;
				import java.util.function.Supplier;
				import org.eclipse.jdt.annotation.NonNullByDefault;
				
				@NonNullByDefault public class Foo {
				    static void foo(Supplier<List<?>> f) { }
				   \s
				    static void test() {
				        foo(ArrayList::new);
				    }
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug437270() {
	runConformTestWithLibs(
		new String[] {
			"Foo.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Foo {
					void test(String[] arguments) {
						if (arguments != null) {
							String @NonNull [] temp = arguments;
						}
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug437270_comment3() {
	runConformTestWithLibs(
		new String[] {
			"Foo.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Foo {
				    void test()  {
				        @NonNull Object b = new Object();
				        Object @NonNull[] c = { new Object() };
				       \s
				        test2( b );
				        test3( c );
				    }
				   \s
				    void test2(@Nullable Object z)  {  }
				   \s
				    void test3(Object @Nullable[] z)  {  }
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug435841() {
	runConformTestWithLibs(
		new String[] {
			"ArrayProblem.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class ArrayProblem {
					private String[] data = new String[0];
				\t
					void error1() {
						foo(data);  // Compiler error: required 'String @Nullable[]', but this expression has type 'String @NonNull[]'
					}
				\t
					private String[] foo(String @Nullable[] input) {
						return new String[0];
					}
				\t
					String @Nullable[] error2() {
						String @NonNull[] nonnull = new String[0];
						return nonnull;  // Compiler error: required 'String @Nullable[]' but this expression has type 'String @NonNull[]'
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug441693() {
	runConformTestWithLibs(
		new String[] {
			"Foo.java",
			"""
				import org.eclipse.jdt.annotation.NonNull;
				import org.eclipse.jdt.annotation.NonNullByDefault;
				import org.eclipse.jdt.annotation.Nullable;
				
				@NonNullByDefault({})
				public abstract class Foo {
				   \s
				    abstract <T> @NonNull T requireNonNull(@Nullable T obj);
				   \s
				    @NonNull Iterable<@NonNull String> iterable;
				   \s
				    Foo(@Nullable Iterable<@NonNull String> iterable) {
				        this.iterable = requireNonNull(iterable); // (*)
				    }
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug441693other() {
	runNegativeTestWithLibs(
		new String[] {
			"Foo.java",
			"""
				import org.eclipse.jdt.annotation.NonNull;
				import org.eclipse.jdt.annotation.NonNullByDefault;
				import org.eclipse.jdt.annotation.Nullable;
				import java.util.*;
				
				@NonNullByDefault({})
				public abstract class Foo {
				   \s
				    abstract <T> @NonNull T requireNonNull(@Nullable T obj);
				   \s
				    @NonNull String @NonNull[] array;
				   \s
				    Foo(@NonNull String @Nullable[] arr) {
				        this.array = requireNonNull(arr); // (*)
				    }
				    @NonNull Foo testWild1(@Nullable List<? extends @NonNull Foo> foos) {
				        return requireNonNull(foos).get(0);
				    }
				    @NonNull Foo testWild2(@Nullable List<@Nullable ? extends List<@NonNull Foo>> foos) {
				        return requireNonNull(foos.get(0)).get(0);
				    }
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. INFO in Foo.java (at line 17)
				return requireNonNull(foos).get(0);
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'@NonNull List<capture#of ? extends @NonNull Foo>\'. Type \'List<E>\' doesn\'t seem to be designed with null type annotations in mind
			----------
			2. INFO in Foo.java (at line 20)
				return requireNonNull(foos.get(0)).get(0);
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'List<@NonNull Foo>\'. Type \'List<E>\' doesn\'t seem to be designed with null type annotations in mind
			----------
			3. ERROR in Foo.java (at line 20)
				return requireNonNull(foos.get(0)).get(0);
				                      ^^^^
			Potential null pointer access: this expression has a \'@Nullable\' type
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=439158, [1.8][compiler][null] Adding null annotation to return type causes IllegalStateException and sometimes InvocationTargetException
public void testBug439158() {
	runConformTestWithLibs(
		new String[] {
			"Test.java",
			"""
				import java.util.Collection;
				import java.util.List;
				import java.util.Set;
				import org.eclipse.jdt.annotation.*;
				
				public class Test {
					class X {
					\t
					}
				\t
					public static <C extends Collection<?>, A extends C, B extends C>
							@Nullable A transform(B arg) {
						return null;
					}
				\t
					public static void main(String[] args) {
						List<X> list = null;
						Set<X> result = transform(list);
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=434579, [1.8][compiler][null] Annotation-based null analysis causes incorrect type errors
public void testBug434579() {
	Map options = getCompilerOptions();
	runConformTestWithLibs(
		new String[] {
			"AbstractNode.java",
			"""
				import org.eclipse.jdt.annotation.NonNullByDefault;
				@NonNullByDefault
				interface ExtendedNode {
					ExtendedNode getParent();
					void setParent(ExtendedNode newParent);
				}
				@NonNullByDefault
				public class AbstractNode implements ExtendedNode {
					private ExtendedNode parent;
					protected AbstractNode() {
						parent = this;
					}
					@Override
					public ExtendedNode getParent() {
						return parent;
					}
					@Override
					public void setParent(final ExtendedNode newParent) {
						parent = newParent;
					}
				}
				"""
		},
		options,
		"");
	runNegativeTestWithLibs(
		new String[] {
			"UnequalBinaryNode.java",
			"""
				public class UnequalBinaryNode<L extends ExtendedNode, R extends ExtendedNode>
						extends AbstractNode {
					private L left;
					private R right;
					public UnequalBinaryNode(final L initialLeft, final R initialRight) {
						left = initialLeft;
						right = initialRight;
						left.setParent(this);
						right.setParent(this); // error on this line without fix
					}
				}
				"""
		},
		options,
		"""
			----------
			1. ERROR in UnequalBinaryNode.java (at line 8)
				left.setParent(this);
				^^^^
			Potential null pointer access: this expression has type \'L\', a free type variable that may represent a \'@Nullable\' type
			----------
			2. ERROR in UnequalBinaryNode.java (at line 9)
				right.setParent(this); // error on this line without fix
				^^^^^
			Potential null pointer access: this expression has type \'R\', a free type variable that may represent a \'@Nullable\' type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=434582,
//[1.8][compiler][null] @Nullable annotation in type parameter causes NullPointerException in JDT core
public void testBug434582() {
	runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.Nullable;
					import org.eclipse.jdt.annotation.NonNullByDefault;
					@NonNullByDefault
					class ProgramNode {}
					@NonNullByDefault
					interface ConcreteNodeVisitor<R, P> {
						R visit(ProgramNode node, P extraParameter);
					}
					public class X implements
							ConcreteNodeVisitor<Boolean, @Nullable Object> {
						public Boolean visit(ProgramNode node, Object extraParameter) {
							return Boolean.FALSE;
						}
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 11)
					public Boolean visit(ProgramNode node, Object extraParameter) {
					                     ^^^^^^^^^^^
				Missing non-null annotation: inherited method from ConcreteNodeVisitor<Boolean,Object> specifies this parameter as @NonNull
				----------
				2. ERROR in X.java (at line 11)
					public Boolean visit(ProgramNode node, Object extraParameter) {
					                                       ^^^^^^
				Missing nullable annotation: inherited method from ConcreteNodeVisitor<Boolean,Object> specifies this parameter as @Nullable
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=434582,
//[1.8][compiler][null] @Nullable annotation in type parameter causes NullPointerException in JDT core
public void testBug434582a() {
	runNegativeTestWithLibs(
		new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.Nullable;
					import org.eclipse.jdt.annotation.NonNullByDefault;
					@NonNullByDefault
					class ProgramNode {}
					@NonNullByDefault
					interface ConcreteNodeVisitor<R, P> {
						void visit(ProgramNode node, P extraParameter);
					}
					public class X implements
							ConcreteNodeVisitor<Boolean, @Nullable Object> {
						public void visit(ProgramNode node, Object extraParameter) {}
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 11)
					public void visit(ProgramNode node, Object extraParameter) {}
					                  ^^^^^^^^^^^
				Missing non-null annotation: inherited method from ConcreteNodeVisitor<Boolean,Object> specifies this parameter as @NonNull
				----------
				2. ERROR in X.java (at line 11)
					public void visit(ProgramNode node, Object extraParameter) {}
					                                    ^^^^^^
				Missing nullable annotation: inherited method from ConcreteNodeVisitor<Boolean,Object> specifies this parameter as @Nullable
				----------
				""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=443467, [1.8][null]InternalError: Unexpected binding type
public void test443467() throws Exception {
	runNegativeTest(
		new String[] {
			"BuildIdeMain.java",
			"""
				import java.nio.file.Path;
				import java.time.Instant;
				import java.util.AbstractMap.SimpleEntry;
				import java.util.HashMap;
				import java.util.stream.Stream;
				
				public class BuildIdeMain {
				static void writeUpdates(Stream<Path> filter2, HashMap<Path, SimpleEntry<byte[], Instant>> ideFiles, HashMap<Path, Path> updateToFile) {
				   filter2.map(p -> new SimpleEntry<>(updateToFile.get(p), p->ideFiles.get(p)));
				}
				}
				""",
		},
		"""
			----------
			1. ERROR in BuildIdeMain.java (at line 9)
				filter2.map(p -> new SimpleEntry<>(updateToFile.get(p), p->ideFiles.get(p)));
				                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Cannot infer type arguments for SimpleEntry<>
			----------
			""",
		this.LIBS,
		true/*flush*/);
}
public void testBug445227() {
	runConformTestWithLibs(
		new String[] {
			"Bar.java",
			"""
				@org.eclipse.jdt.annotation.NonNullByDefault
				class Bar<E extends Bar.Foo<E>> {
				    final Iterable<E> list;
				
				    Bar() {
				        this((Iterable<E>) emptyList());
				    }
				
				    Bar(Iterable<E> list) { this.list = list; }
				
				    private static <X extends Foo<X>> Iterable<X> emptyList() { throw new UnsupportedOperationException(); }
				
				    interface Foo<F extends Foo<F>> { }
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in Bar.java (at line 6)
				this((Iterable<E>) emptyList());
				     ^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: Unchecked cast from Iterable<Bar.Foo<Bar.Foo<X>>> to Iterable<E>
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446715, [compiler] org.eclipse.jdt.internal.compiler.lookup.TypeSystem.cacheDerivedType
public void test446715() {
	Map options = getCompilerOptions();
	runConformTestWithLibs(
		new String[] {
			"Y.java",
			"""
				import org.eclipse.jdt.annotation.NonNull;
				public class Y {
					public Z.ZI @NonNull [] zz = new Z.ZI[0];
				}
				""",
			"Z.java",
			"""
				public class Z {
					public class ZI {
					}
				}
				"""
		},
		options,
		"");
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Y y = new Y();
						y.zz = null;
					}
				}
				"""
		},
		options,
		"""
			----------
			1. ERROR in X.java (at line 4)
				y.zz = null;
				       ^^^^
			Null type mismatch: required \'Z.ZI @NonNull[]\' but the provided value is null
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=445669, java.lang.IllegalStateException at org.eclipse.jdt.internal.compiler.lookup.UnresolvedReferenceBinding.clone
public void test445669() {
	Map options = getCompilerOptions();
	runConformTestWithLibs(
		new String[] {
			"Y.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault(DefaultLocation.FIELD)
				public class Y {
					public Z.ZI zzi = new Z().new ZI();
					public Z z = new Z();
				}
				""",
			"Z.java",
			"""
				public class Z {
					public class ZI {
					}
				}
				"""
		},
		options,
		"");
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Y y = new Y();
						y.zzi = null;
				       y.z = null;
					}
				}
				"""
		},
		options,
		"""
			----------
			1. ERROR in X.java (at line 4)
				y.zzi = null;
				        ^^^^
			Null type mismatch: required \'Z.@NonNull ZI\' but the provided value is null
			----------
			2. ERROR in X.java (at line 5)
				y.z = null;
				      ^^^^
			Null type mismatch: required \'@NonNull Z\' but the provided value is null
			----------
			""");
}
public void testArrayOfArrays() {
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.Arrays;
				import org.eclipse.jdt.annotation.*;
				public class X {
				   public static void main(String[] args) {
				      String [] @Nullable [] @NonNull [] arr = new String[][][] {};
				      ArrayList<String[][]> al = new ArrayList<String [][]>(Arrays.asList(arr));
				   }
				}
				""",
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in X.java (at line 6)
				String [] @Nullable [] @NonNull [] arr = new String[][][] {};
				                                         ^^^^^^^^^^^^^^^^^^^
			Null type safety (type annotations): The expression of type \'String[][][]\' needs unchecked conversion to conform to \'String [] @Nullable[] @NonNull[]\'
			----------
			""");
}
public void testBug447088() {
	runConformTestWithLibs(
		new String[] {
			"FullyQualifiedNullable.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class FullyQualifiedNullable {
					java.lang.@Nullable String text;
					java.lang.@Nullable String getText() {
						return text;
					}
				}
				"""
		},
		null,
		"");
}
public void testBug448777() {
	runNegativeTestWithLibs(
		new String[] {
			"DoubleInference.java",
			"""
				import org.eclipse.jdt.annotation.*;
				
				public class DoubleInference {
				
					@FunctionalInterface
					interface Func<@Nullable T>  {
						T a(T i);
					}
				
					<X> X applyWith(Func<X> f, X x) { return x; }
				
					@NonNull String test1() {
						return applyWith(i -> i, "hallo");
					}
					void test2(Func<String> f1, Func<@NonNull String> f2) {
						f1.a(null);
						f2.a(null);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in DoubleInference.java (at line 10)
				<X> X applyWith(Func<X> f, X x) { return x; }
				                     ^
			Null constraint mismatch: The type \'X\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			2. ERROR in DoubleInference.java (at line 13)
				return applyWith(i -> i, "hallo");
				                 ^^^^^^
			Contradictory null annotations: function type was inferred as \'@Nullable @NonNull String (@Nullable @NonNull String)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location
			----------
			3. ERROR in DoubleInference.java (at line 15)
				void test2(Func<String> f1, Func<@NonNull String> f2) {
				                ^^^^^^
			Null constraint mismatch: The type \'String\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			4. ERROR in DoubleInference.java (at line 15)
				void test2(Func<String> f1, Func<@NonNull String> f2) {
				                                 ^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'@NonNull String\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			5. ERROR in DoubleInference.java (at line 17)
				f2.a(null);
				^^^^^^^^^^
			Contradictory null annotations: method was inferred as \'@Nullable @NonNull String a(@Nullable @NonNull String)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location
			----------
			""");
}
public void testBug446442_comment2a() {
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"""
				import org.eclipse.jdt.annotation.*;
				interface Foo<T, N extends Number> {
					void m(@NonNull N arg2);
				
					void m(@Nullable T arg1);
				}
				
				interface Baz extends Foo<Integer, Integer> {}
				
				class Impl implements Baz {
				  public void m(@NonNull Integer i) {}
				}
				
				public class Test {
					Baz baz= x -> {
						x= null;
					};\s
				}
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 11)
				public void m(@NonNull Integer i) {}
				              ^^^^^^^^^^^^^^^^
			Illegal redefinition of parameter i, inherited method from Foo<Integer,Integer> declares this parameter as @Nullable
			----------
			""");
}
// swapped order of method declarations
public void testBug446442_comment2b() {
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"""
				import org.eclipse.jdt.annotation.*;
				interface Foo<T, N extends Number> {
					void m(@Nullable T arg1);
				
					void m(@NonNull N arg2);
				}
				
				interface Baz extends Foo<Integer, Integer> {}
				
				class Impl implements Baz {
				  public void m(@NonNull Integer i) {}
				}
				
				public class Test {
					Baz baz= x -> {
						x= null;
					};\s
				}
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 11)
				public void m(@NonNull Integer i) {}
				              ^^^^^^^^^^^^^^^^
			Illegal redefinition of parameter i, inherited method from Foo<Integer,Integer> declares this parameter as @Nullable
			----------
			""");
}
// inherit from two different supers
public void testBug446442_comment2c() {
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"""
				import org.eclipse.jdt.annotation.*;
				interface Foo0<T, N extends Number> {
					void m(@Nullable T arg1);
				}
				
				interface Foo1<T, N extends Number> {
					void m(@NonNull N arg2);
				}
				
				interface Baz extends Foo1<Integer, Integer>,  Foo0<Integer, Integer> {}
				
				class Impl implements Baz {
				  public void m(@NonNull Integer i) {}
				}
				
				public class Test {
					Baz baz= x -> {
						x= null;
					};\s
				}
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 13)
				public void m(@NonNull Integer i) {}
				              ^^^^^^^^^^^^^^^^
			Illegal redefinition of parameter i, inherited method from Foo0<Integer,Integer> declares this parameter as @Nullable
			----------
			""");
}
// merging @NonNull & unannotated in arg-position must answer unannotated
public void testBug446442_2a() {
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"Test.java",
			"""
				import org.eclipse.jdt.annotation.*;
				interface Foo<T, N extends Number> {
					void m(@NonNull N arg2);
				
					void m(T arg1);
				}
				
				interface Baz extends Foo<Integer, Integer> {}
				
				public class Test {
					Baz baz= x -> {
						@NonNull Object o = x;
					};\s
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in Test.java (at line 12)
				@NonNull Object o = x;
				                    ^
			Null type safety (type annotations): The expression of type \'Integer\' needs unchecked conversion to conform to \'@NonNull Object\'
			----------
			""");
}
// merging @NonNull & unannotated in arg-position must answer unannotated - swapped order
public void testBug446442_2b() {
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"Test.java",
			"""
				import org.eclipse.jdt.annotation.*;
				interface Foo<T, N extends Number> {
					void m(T arg1);
				
					void m(@NonNull N arg2);
				}
				
				interface Baz extends Foo<Integer, Integer> {}
				
				public class Test {
					Baz baz= x -> {
						@NonNull Object o = x;
					};\s
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in Test.java (at line 12)
				@NonNull Object o = x;
				                    ^
			Null type safety (type annotations): The expression of type \'Integer\' needs unchecked conversion to conform to \'@NonNull Object\'
			----------
			""");
}
// using inherited implementation to fulfill both contracts
public void testBug446442_3() {
	runConformTestWithLibs(
		new String[] {
			"Test.java",
			"""
				import org.eclipse.jdt.annotation.*;
				interface Foo<T, N extends Number> {
					void m(@NonNull N arg2);
				
					void m(T arg1);
				}
				
				interface Baz extends Foo<Integer, Integer> {}
				class Impl {
				  public void m(Integer a) {}
				}
				class BazImpl extends Impl implements Baz {}
				
				public class Test {
					void test(BazImpl b) {
						b.m(null);
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
// unsuccessful attempt to trigger use of MostSpecificExceptionMethodBinding
public void testBug446442_4() {
	runConformTestWithLibs(
		new String[] {
			"Test.java",
			"""
				import org.eclipse.jdt.annotation.*;
				interface Foo<T, N extends Number> {
					abstract void m(@NonNull N arg2) throws Exception;
				
					default void m(T arg1) throws java.io.IOException {}
				}
				
				interface Baz extends Foo<Integer, Integer> {}
				abstract class Impl {
				  public void m(Integer a) throws java.io.IOException {}
				}
				class BazImpl extends Impl implements Baz {}
				
				public class Test {
					void test(BazImpl b) throws java.io.IOException {
						b.m(null);
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
// annotated return types
public void testBug446442_5() {
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"""
				import org.eclipse.jdt.annotation.*;
				interface Foo<T, N extends Number> {
					T m(T t);
				
					@NonNull N m(N n);
				}
				
				interface Baz extends Foo<Integer, Integer> {}
				
				class Impl implements Baz {
				  public Integer m(Integer i) { return Integer.valueOf(0); }
				}
				
				public class Test {
					Baz baz= x -> null;
				}
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 11)
				public Integer m(Integer i) { return Integer.valueOf(0); }
				       ^^^^^^^
			The return type is incompatible with \'@NonNull Integer\' returned from Foo<Integer,Integer>.m(Integer) (mismatching null constraints)
			----------
			2. ERROR in Test.java (at line 15)
				Baz baz= x -> null;
				              ^^^^
			Null type mismatch: required \'@NonNull Integer\' but the provided value is null
			----------
			""");
}
// conflicting annotations on type arguments
public void testBug446442_6a() {
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"""
				import org.eclipse.jdt.annotation.*;
				import java.util.*;
				interface Foo<T,C1 extends Collection<T>, C2 extends List<T>> {
					void m(C1 a1);
				
					void m(C2 a2);
				}
				
				interface Baz extends Foo<Integer, ArrayList<@NonNull Integer>, ArrayList<@Nullable Integer>> {}
				
				class Impl implements Baz {
				  public void m(ArrayList<@NonNull Integer> i) {} // contradictory type cannot be implemented
				}
				
				public class Test {
					Baz baz= x -> { // contradictory type cannot be used as SAM
						x.add(null); // contradictory type cause errors at call sites
					};\s
				}
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 12)
				public void m(ArrayList<@NonNull Integer> i) {} // contradictory type cannot be implemented
				              ^^^^^^^^^
			Illegal redefinition of parameter i, inherited method from Foo<Integer,ArrayList<Integer>,ArrayList<Integer>> declares this parameter as \'ArrayList<@Nullable Integer>\' (mismatching null constraints)
			----------
			2. ERROR in Test.java (at line 16)
				Baz baz= x -> { // contradictory type cannot be used as SAM
					x.add(null); // contradictory type cause errors at call sites
				};\s
				         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Contradictory null annotations: function type was inferred as \'void (ArrayList<@NonNull @Nullable Integer>)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location
			----------
			3. ERROR in Test.java (at line 17)
				x.add(null); // contradictory type cause errors at call sites
				^^^^^^^^^^^
			Contradictory null annotations: method was inferred as \'boolean add(@NonNull @Nullable Integer)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location
			----------
			""");
}
// swapped order of method declarations + added return type
public void testBug446442_6b() {
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"""
				import org.eclipse.jdt.annotation.*;
				import java.util.*;
				interface Foo<T,C1 extends Collection<T>, C2 extends List<T>> {
					C2 m(C2 a2);
				
					C1 m(C1 a1);
				}
				
				interface Baz extends Foo<Integer, ArrayList<@NonNull Integer>, ArrayList<@Nullable Integer>> {}
				
				class Impl implements Baz {
				  public ArrayList<@NonNull Integer> m(ArrayList<@Nullable Integer> i) { return i; }
				}
				
				public class Test {
					Baz baz= x -> {
						x.add(null);
						x.get(0);
						return x;
					};
				}
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 12)
				public ArrayList<@NonNull Integer> m(ArrayList<@Nullable Integer> i) { return i; }
				       ^^^^^^^^^
			The return type is incompatible with \'ArrayList<@Nullable Integer>\' returned from Foo<Integer,ArrayList<Integer>,ArrayList<Integer>>.m(ArrayList<Integer>) (mismatching null constraints)
			----------
			2. ERROR in Test.java (at line 12)
				public ArrayList<@NonNull Integer> m(ArrayList<@Nullable Integer> i) { return i; }
				                                     ^^^^^^^^^
			Illegal redefinition of parameter i, inherited method from Foo<Integer,ArrayList<Integer>,ArrayList<Integer>> declares this parameter as \'ArrayList<@NonNull Integer>\' (mismatching null constraints)
			----------
			3. ERROR in Test.java (at line 12)
				public ArrayList<@NonNull Integer> m(ArrayList<@Nullable Integer> i) { return i; }
				                                                                              ^
			Null type mismatch (type annotations): required \'ArrayList<@NonNull Integer>\' but this expression has type \'ArrayList<@Nullable Integer>\'
			----------
			4. ERROR in Test.java (at line 16)
				Baz baz= x -> {
					x.add(null);
					x.get(0);
					return x;
				};
				         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Contradictory null annotations: function type was inferred as \'ArrayList<@NonNull @Nullable Integer> (ArrayList<@Nullable @NonNull Integer>)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location
			----------
			5. ERROR in Test.java (at line 17)
				x.add(null);
				^^^^^^^^^^^
			Contradictory null annotations: method was inferred as \'boolean add(@Nullable @NonNull Integer)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location
			----------
			6. ERROR in Test.java (at line 18)
				x.get(0);
				^^^^^^^^
			Contradictory null annotations: method was inferred as \'@Nullable @NonNull Integer get(int)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location
			----------
			""");
}
public void testBug453475() {
	runConformTestWithLibs(
		new String[] {
			"TestMap.java",
			"""
				import java.util.*;
				import org.eclipse.jdt.annotation.*;
				
				@NonNullByDefault
				public abstract class TestMap extends AbstractMap<String,@Nullable String> {
				
				}
				"""
		}, null, "");
	runConformTestWithLibs(
			false /* don't flush */,
			new String[] {
			"Test.java",
			"""
				import java.util.*;
				import org.eclipse.jdt.annotation.*;
				
				@NonNullByDefault
				public class Test {
				
				  public static final void test(TestMap testMap) {
				    testMap.putAll(new HashMap<String,@Nullable String>()); // Error: Contradictory null annotations: method was inferred as 'void putAll(Map<? extends @NonNull String,? extends @NonNull @Nullable String>)', but only one of '@NonNull' and '@Nullable' can be effective at any location
				  }
				
				}
				"""
		}, null, "");
}
// also: don't apply default to use of type variable
public void testBug453475a() {
	runConformTestWithLibs(
		new String[] {
			"NamespaceStorage.java",
			"""
				import java.util.*;
				@org.eclipse.jdt.annotation.NonNullByDefault
				public interface NamespaceStorage<T>\s
				{
				
					Set<T> getObjects();\s
					T getObject(T in);
				}
				"""
		}, null, "");
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"NamespaceStorageImpl.java",
			"""
				import java.util.*;
				@org.eclipse.jdt.annotation.NonNullByDefault
				public class NamespaceStorageImpl<T> implements NamespaceStorage<T>
				{
					@Override
					public  Set<T> getObjects()\s
					{
						return new TreeSet<T>();
					}
					@Override
					public T getObject(T in)
					{
						return in;
					}
				}
				"""
		},
		null, "");
}
// also: don't apply default to wildcard
public void testBug453475b() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				@org.eclipse.jdt.annotation.NonNullByDefault
				public interface X {
				
					void test(List<?> list);
				\t
				}
				"""
		}, null, "");
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"Y.java",
			"""
				import java.util.*;
				import org.eclipse.jdt.annotation.*;
				public class Y {
					public void run(X x, @NonNull List<@Nullable String> s)\s
					{
						x.test(s);
					}
				}
				"""
		},
		null, "");
}
public void testBug456236() {
	runConformTestWithLibs(
		new String[] {
			"Nullsafe.java",
			"""
				import org.eclipse.jdt.annotation.*;
				class Nullsafe<T> {
					final @Nullable T t;
				
					Nullsafe(@Nullable T t) {
						this.t = t;
					}
					public static <U> Nullsafe<U> of(@Nullable U u) {
						return new Nullsafe<>(u); // compile error
					}
				}
				"""
		},
		null,
		"");
}

public void testBug456497() throws Exception {
	runConformTestWithLibs(
		new String[] {
			"libs/Lib1.java",
			"""
				package libs;
				
				import java.util.Collection;
				import java.util.Iterator;
				import org.eclipse.jdt.annotation.*;
				
				public interface Lib1 {
					<T> Iterator<T> unconstrainedTypeArguments1(Collection<@Nullable T> in);
					Iterator<@NonNull String> unconstrainedTypeArguments2(Collection<String> in);
				}
				""",
			"tests/Test1.java",
			"""
				package tests;
				import org.eclipse.jdt.annotation.*;
				
				import java.util.Collection;
				import java.util.Iterator;
				
				import libs.Lib1;
				
				public class Test1 {
					Iterator<@NonNull String> test1(Lib1 lib, Collection<@Nullable String> coll) {
						return lib.unconstrainedTypeArguments1(coll);
					}
					Iterator<@NonNull String> test2(Lib1 lib, Collection<@Nullable String> coll) {
						return lib.unconstrainedTypeArguments2(coll);
					}
				}
				"""
		},
		null,
		"");
}
// original case
public void testBug456487a() {
	runConformTestWithLibs(
		new String[]{
			"Optional.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Optional<@NonNull T> {
				  @Nullable T value;
				  private Optional(T value) { this.value = value; }
				  public static <@NonNull T> Optional<T> of(T value) { return new Optional<T>(value); }
				  public T get() {\s
				    @Nullable T t = this.value;
				    if (t != null) return t;\s
				    throw new RuntimeException("No value present");
				  }
				  public @Nullable T orElse(@Nullable T other) { return (this.value != null) ? this.value : other; }
				}
				"""
		},
		null,
		"");
}
// witness for NPE in NullAnnotationMatching.providedNullTagBits:
public void testBug456487b() {
	runNegativeTestWithLibs(
		new String[]{
			"Optional.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Optional<@Nullable T> {
				  @Nullable T value;
				  private Optional(T value) { this.value = value; }
				  public static <@NonNull T> Optional<T> of(T value) { return new Optional<T>(value); }
				  public T get() {\s
				    @Nullable T t = this.value;
				    if (t != null) return t;\s
				    throw new RuntimeException("No value present");
				  }
				  public @Nullable T orElse(@Nullable T other) { return (this.value != null) ? this.value : other; }
				}
				""",
			"OTest.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				class OTest {
				  public static void good() {
				    Optional<String> os1 = Optional.of("yes");
				    @NonNull String s = os1.get();
				    @Nullable String ns = os1.orElse(null);
				  }
				  public static void bad() {
				    Optional<String> os = Optional.of(null);
				    @NonNull String s = os.orElse(null);
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in Optional.java (at line 5)
				public static <@NonNull T> Optional<T> of(T value) { return new Optional<T>(value); }
				                                    ^
			Null constraint mismatch: The type \'@NonNull T\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			2. ERROR in Optional.java (at line 5)
				public static <@NonNull T> Optional<T> of(T value) { return new Optional<T>(value); }
				                                                            ^^^^^^^^^^^^^^^^^^^^^^
			Contradictory null annotations: method was inferred as \'void <init>(@Nullable @NonNull T)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location
			----------
			3. ERROR in Optional.java (at line 5)
				public static <@NonNull T> Optional<T> of(T value) { return new Optional<T>(value); }
				                                                                         ^
			Null constraint mismatch: The type \'@NonNull T\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			----------
			1. ERROR in OTest.java (at line 5)
				Optional<String> os1 = Optional.of("yes");
				         ^^^^^^
			Null constraint mismatch: The type \'@NonNull String\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			2. ERROR in OTest.java (at line 6)
				@NonNull String s = os1.get();
				                    ^^^^^^^^^
			Contradictory null annotations: method was inferred as \'@Nullable @NonNull String get()\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location
			----------
			3. ERROR in OTest.java (at line 7)
				@Nullable String ns = os1.orElse(null);
				                      ^^^^^^^^^^^^^^^^
			Contradictory null annotations: method was inferred as \'@Nullable @NonNull String orElse(@Nullable @NonNull String)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location
			----------
			4. ERROR in OTest.java (at line 10)
				Optional<String> os = Optional.of(null);
				         ^^^^^^
			Null constraint mismatch: The type \'@NonNull String\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			5. ERROR in OTest.java (at line 10)
				Optional<String> os = Optional.of(null);
				                                  ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			6. ERROR in OTest.java (at line 11)
				@NonNull String s = os.orElse(null);
				                    ^^^^^^^^^^^^^^^
			Contradictory null annotations: method was inferred as \'@Nullable @NonNull String orElse(@Nullable @NonNull String)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location
			----------
			""");
}
public void testBug454182() {

	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annot.NonNullByDefault");
	String[] libs = this.LIBS.clone();
	libs[libs.length-1] = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test454182.jar";
	runConformTest(
		new String[] {
			"p/package-info.java",
			"@annot.NonNullByDefault package p;\n"
		},
		"",
		libs,
		false,
		null,
		options,
		null);
}
public void testBug443870() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				interface Listener<T> {}
				interface I0<T,U extends Listener<T>> {}
				interface I1<T> extends I0<T,Listener<T>> {}
				class Y<S> {
					private @NonNull I0<S,Listener<S>> f;
					Y (@NonNull I0<S,Listener<S>> in) { this.f = in; }
					@NonNull I0<S,Listener<S>> getI() { return f; }
				}
				public class X<V> extends Y<V> {
					private @NonNull I1<V> f;
					X (@NonNull I1<V> in) { super(in); this.f = in; }
					@Override
					@NonNull I1<V> getI() { return f; }
				}
				"""
		},
		null,
		"");
}
public void testBug437072() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				import java.util.List;
				public class X {
					@NonNull int[][] ints = new int[3][4];
					@NonNull int[][] test1() { return new int[3][4]; }
					void test2(@NonNull boolean[][] bools) {
						@NonNull boolean[][] bools2 = bools;
					}
					List<@NonNull int[]> intslist;
					List<@NonNull int> intlist;
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				@NonNull int[][] ints = new int[3][4];
				^^^^^^^^
			The nullness annotation @NonNull is not applicable for the primitive type int
			----------
			2. ERROR in X.java (at line 5)
				@NonNull int[][] test1() { return new int[3][4]; }
				^^^^^^^^
			The nullness annotation @NonNull is not applicable for the primitive type int
			----------
			3. ERROR in X.java (at line 6)
				void test2(@NonNull boolean[][] bools) {
				           ^^^^^^^^
			The nullness annotation @NonNull is not applicable for the primitive type boolean
			----------
			4. ERROR in X.java (at line 7)
				@NonNull boolean[][] bools2 = bools;
				^^^^^^^^
			The nullness annotation @NonNull is not applicable for the primitive type boolean
			----------
			5. ERROR in X.java (at line 9)
				List<@NonNull int[]> intslist;
				     ^^^^^^^^
			The nullness annotation @Nullable is not applicable for the primitive type int
			----------
			6. ERROR in X.java (at line 10)
				List<@NonNull int> intlist;
				              ^^^
			Syntax error, insert "Dimensions" to complete ReferenceType
			----------
			""",
		this.LIBS,
		true/*flush*/);
}
public void testBug448709() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.WARNING); // ensure program is runnable
	compilerOptions.put(JavaCore.COMPILER_PB_PESSIMISTIC_NULL_ANALYSIS_FOR_FREE_TYPE_VARIABLES, JavaCore.WARNING); // ensure program is runnable
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"Test.java",
			"""
				import java.util.*;
				import java.util.function.*;
				import org.eclipse.jdt.annotation.*;
				
				public class Test {
				
				  /**
				   * A null-annotated version of {@link Objects#requireNonNull(Object)}.
				   */
				  public static final <T> @NonNull T requireNonNull(final @Nullable T obj) {
				    if (obj == null) throw new NullPointerException();
				    return obj;
				  }
				
				  /**
				   * A null-annotated version of {@link Optional#map(Function)}.
				   */
				  public static final <T,U> @NonNull Optional<U> map(final @NonNull Optional<T> optional, final Function<@NonNull ? super T,? extends U> mapper) {
				    if (!optional.isPresent()) return requireNonNull(Optional.empty());
				    final T source = optional.get();
				    final U result = mapper.apply(source);
				    System.out.println(source+"->"+result);
				    return requireNonNull(Optional.<U> ofNullable(result));
				  }
				
				  /**
				   * A method with a {@link NonNull} {@link DefaultLocation#PARAMETER} and {@link DefaultLocation#RETURN_TYPE}.
				   */
				  public static final @NonNull Integer testMethod(final @NonNull String s) {
				    final Integer r = Integer.valueOf(s);
				    if (r == null) throw new NullPointerException();
				    return r+1;
				  }
				
				  public static void main(final String[] args) {
				    final @NonNull Optional<@Nullable String> optNullableString = requireNonNull(Optional.ofNullable("1"));
				
				    final Function<@NonNull String,@NonNull Integer> testMethodRef = Test::testMethod;
				    map(optNullableString, testMethodRef);
				
				    map(optNullableString, Test::testMethod); // Error: Null type mismatch at parameter 1: required '@NonNull String' but provided '@Nullable String' via method descriptor Function<String,Integer>.apply(String)
				
				    map(optNullableString, (s) -> Test.testMethod(s));
				  }
				
				}
				"""
		},
		compilerOptions,
		"""
			----------
			1. WARNING in Test.java (at line 21)
				final U result = mapper.apply(source);
				                              ^^^^^^
			Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			2. WARNING in Test.java (at line 39)
				map(optNullableString, testMethodRef);
				                       ^^^^^^^^^^^^^
			Contradictory null annotations: method was inferred as \'@NonNull Optional<@NonNull Integer> map(@NonNull Optional<@Nullable String>, Function<@NonNull ? super @Nullable String,? extends @NonNull Integer>)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location
			----------
			3. WARNING in Test.java (at line 41)
				map(optNullableString, Test::testMethod); // Error: Null type mismatch at parameter 1: required \'@NonNull String\' but provided \'@Nullable String\' via method descriptor Function<String,Integer>.apply(String)
				                       ^^^^^^^^^^^^^^^^
			Contradictory null annotations: method was inferred as \'@NonNull Optional<@NonNull Integer> map(@NonNull Optional<@Nullable String>, Function<@NonNull ? super @Nullable String,? extends @NonNull Integer>)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location
			----------
			4. WARNING in Test.java (at line 43)
				map(optNullableString, (s) -> Test.testMethod(s));
				                       ^^^^^^^^^^^^^^^^^^^^^^^^^
			Contradictory null annotations: method was inferred as \'@NonNull Optional<@NonNull Integer> map(@NonNull Optional<@Nullable String>, Function<@NonNull ? super @Nullable String,? extends @NonNull Integer>)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location
			----------
			5. WARNING in Test.java (at line 43)
				map(optNullableString, (s) -> Test.testMethod(s));
				                                              ^
			Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'
			----------
			""",
		"""
			1->2
			1->2
			1->2""");
}
public void testBug448709b() {
	runConformTestWithLibs(
		new String[] {
			"Test.java",
			"""
				import java.util.*;
				import java.util.function.*;
				import org.eclipse.jdt.annotation.*;
				
				public class Test {
				
				  public static final <T,U> void map(final @NonNull Optional<T> optional, final Function<@NonNull ? super T,? extends U> mapper) {
				    final T source = optional.get();
				    if (source != null) {
				      final U result = mapper.apply(source);
				      System.out.println(source+"->"+result);
				    }
				  }
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug459967_Array_constructor() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				interface FI<T> {
					T @NonNull[] getArray(int size);\
				}
				public class X {
					void consumer(FI<String> fis) {}
					void test() {
						consumer(String[]::new);
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug459967_Array_constructor_b() {
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				interface FI<T> {
					@NonNull T @NonNull[] getArray(int size);\
				}
				public class X {
					void consumer(FI<String> fis) {}
					void test() {
						consumer(String[]::new);
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in X.java (at line 7)
				consumer(String[]::new);
				         ^^^^^^^^^^^^^
			Null type safety at method return type: Method descriptor FI<String>.getArray(int) promises \'@NonNull String @NonNull[]\' but referenced method provides \'String @NonNull[]\'
			----------
			""");
}
public void testBug459967_Array_clone() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				interface FI<T> {
					T @NonNull[] getArray(T[] orig);\
				}
				public class X {
					void consumer(FI<String> fis) {}
					void test() {
						consumer(String[]::clone);
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug459967_Array_clone_b() {
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				interface FI<T> {
					@NonNull T @NonNull[] getArray(T[] orig);\
				}
				public class X {
					void consumer(FI<String> fis) {}
					void test() {
						consumer(String[]::clone);
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in X.java (at line 7)
				consumer(String[]::clone);
				         ^^^^^^^^^^^^^^^
			Null type safety at method return type: Method descriptor FI<String>.getArray(String[]) promises \'@NonNull String @NonNull[]\' but referenced method provides \'String @NonNull[]\'
			----------
			""");
}
public void testBug448709_allocationExpression1() {
	// inference prioritizes constraint (<@Nullable T>) over expected type (@NonNull String), hence a null type mismatch results
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				interface F0<T> {}
				class FI<@Nullable T> implements F0<T> {
				}
				public abstract class X {
					abstract <Z> Z zork(F0<Z> f);
					@NonNull String test() {
						 return zork(new FI<>());
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 8)
				return zork(new FI<>());
				            ^^^^^^^^^^
			Null type mismatch (type annotations): required \'F0<@NonNull String>\' but this expression has type \'@NonNull FI<@Nullable String>\', corresponding supertype is \'F0<@Nullable String>\'
			----------
			""");
}
public void testBug448709_allocationExpression2() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				class F {
					<@Nullable U> F(U arg1, U arg2) {}
				}
				public class X {
					F f = new <@NonNull Integer>F(1,2);
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 6)
				F f = new <@NonNull Integer>F(1,2);
				           ^^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'@NonNull Integer\' is not a valid substitute for the type parameter \'@Nullable U\'
			----------
			""");
}
public void testBug448709_allocationExpression3() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
					class F {
						<@Nullable U> F(U arg1, U arg2) {}
					}
					F f = this.new <@NonNull Integer>F(1,2);
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 6)
				F f = this.new <@NonNull Integer>F(1,2);
				                ^^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'@NonNull Integer\' is not a valid substitute for the type parameter \'@Nullable U\'
			----------
			""");
}
public void testBug465513() {
	runConformTestWithLibs(
		new String[] {
			"pack1/A.java",
			"""
				package pack1;\r
				import java.math.BigInteger;\r
				\r
				interface A { Object m(Class c); }\r
				interface B<S extends Number> { Object m(Class<S> c); }\r
				interface C<T extends BigInteger> { Object m(Class<T> c); }\r
				@FunctionalInterface\r
				interface D<S,T> extends A, B<BigInteger>, C<BigInteger> {}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in pack1\\A.java (at line 4)
				interface A { Object m(Class c); }
				                       ^^^^^
			Class is a raw type. References to generic type Class<T> should be parameterized
			----------
			""");
}

public void testBug455180() {
    runConformTestWithLibs( // same warning from ecj & javac
    		true/*flush*/,
            new String[] {
                "projA/GenericType.java",
                """
					package projA;
					public class GenericType<T> {
					}
					""",
                "projA/ClassWithRawUsage.java",
                """
					package projA;
					@org.eclipse.jdt.annotation.NonNullByDefault
					public class ClassWithRawUsage {
					   public java.util.List<GenericType> method() {
					           throw new RuntimeException();
					   }
					}
					"""
            },
            getCompilerOptions(),
            """
				----------
				1. WARNING in projA\\ClassWithRawUsage.java (at line 4)
					public java.util.List<GenericType> method() {
					                      ^^^^^^^^^^^
				GenericType is a raw type. References to generic type GenericType<T> should be parameterized
				----------
				""");
    runConformTestWithLibs( // same warning from ecj & javac
    		false/*flush*/,
            new String[] {
                "projB/ClassThatImports.java",
                """
					package projB;
					import projA.ClassWithRawUsage;
					import projA.GenericType;
					import org.eclipse.jdt.annotation.*;
					public class ClassThatImports {
						void test(ClassWithRawUsage cwru) {
							@NonNull GenericType gt = cwru.method().get(0);
						}
					}
					"""
            },
            getCompilerOptions(),
            """
				----------
				1. WARNING in projB\\ClassThatImports.java (at line 7)
					@NonNull GenericType gt = cwru.method().get(0);
					         ^^^^^^^^^^^
				GenericType is a raw type. References to generic type GenericType<T> should be parameterized
				----------
				2. INFO in projB\\ClassThatImports.java (at line 7)
					@NonNull GenericType gt = cwru.method().get(0);
					                          ^^^^^^^^^^^^^^^^^^^^
				Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'@NonNull List<@NonNull GenericType>\'. Type \'List<E>\' doesn\'t seem to be designed with null type annotations in mind
				----------
				""");
}

public void testBug455180WithOtherAnnotation() {
	runConformTestWithLibs(
			new String[] {
				"proj0/MyAnnotation.java",
				"""
					package proj0;
					@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.CLASS)\
					@java.lang.annotation.Target({ java.lang.annotation.ElementType.TYPE_USE })\
					public @interface MyAnnotation {}"""
			}, null, "");
	runConformTestWithLibs( // same warning from ecj & javac
			false/*flush*/,
			new String[] {
				"projA/GenericType.java",
				"""
					package projA;
					public class GenericType<T> {
					}
					""",
				"projA/ClassWithRawUsage.java",
				"""
					package projA;
					public class ClassWithRawUsage {
					   public java.util.List<@proj0.MyAnnotation GenericType> method() {
					      		throw new RuntimeException();
					   }
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. WARNING in projA\\ClassWithRawUsage.java (at line 3)
					public java.util.List<@proj0.MyAnnotation GenericType> method() {
					                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				GenericType is a raw type. References to generic type GenericType<T> should be parameterized
				----------
				""");
	runWarningTestWithLibs(
			false/*flush*/,
			new String[] {
				"projB/ClassThatImports.java",
				"""
					package projB;
					import projA.ClassWithRawUsage;
					import projA.GenericType;
					public class ClassThatImports {
					}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. WARNING in projB\\ClassThatImports.java (at line 2)
					import projA.ClassWithRawUsage;
					       ^^^^^^^^^^^^^^^^^^^^^^^
				The import projA.ClassWithRawUsage is never used
				----------
				2. WARNING in projB\\ClassThatImports.java (at line 3)
					import projA.GenericType;
					       ^^^^^^^^^^^^^^^^^
				The import projA.GenericType is never used
				----------
				""");
}
// original test, witnessing NPE
public void testBug466713() {
	runConformTestWithLibs(
		new String[] {
			"Bug.java",
			"""
				class Bug {
				    java.util.Iterator<int @org.eclipse.jdt.annotation.Nullable []> x;
				}
				"""
		},
		getCompilerOptions(),
		"");
}
// variant to ensure we are still reporting the error at the other location
public void testBug466713b() {
	runNegativeTestWithLibs(
		new String[] {
			"Bug.java",
			"""
				import org.eclipse.jdt.annotation.*;
				class Bug {
				    java.util.Iterator<@Nullable int @Nullable []> x;
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in Bug.java (at line 3)
				java.util.Iterator<@Nullable int @Nullable []> x;
				                   ^^^^^^^^^
			The nullness annotation @Nullable is not applicable for the primitive type int
			----------
			""");
}
// variant to ensure we are not complaining against an unrelated annotation
public void testBug466713c() {
	runConformTestWithLibs(
		new String[] {
			"MyAnnot.java",
			"""
				import java.lang.annotation.*;
				@Retention(RetentionPolicy.CLASS)
				@Target(ElementType.TYPE_USE)
				@interface MyAnnot {}
				""",
			"Bug.java",
			"""
				class Bug {
				    java.util.Iterator<@MyAnnot int @org.eclipse.jdt.annotation.Nullable []> x;
				}
				"""
		},
		getCompilerOptions(),
		"");
}
// variant for https://bugs.eclipse.org/bugs/show_bug.cgi?id=466713#c5
public void testBug466713d() {
	runNegativeTest(
		new String[] {
			"MyAnnot.java",
			"""
				import java.lang.annotation.*;
				@Retention(RetentionPolicy.CLASS)
				@Target(ElementType.TYPE_USE)
				@interface MyAnnot {}
				""",
			"Bug.java",
			"""
				class Bug {
					boolean test(Object o) {
						return o instanceof java.util.Iterator<java.lang. @MyAnnot @org.eclipse.jdt.annotation.Nullable String>;
					}
				}
				"""
		},
		"----------\n" +
		"1. ERROR in Bug.java (at line 3)\n" +
		"	return o instanceof java.util.Iterator<java.lang. @MyAnnot @org.eclipse.jdt.annotation.Nullable String>;\n" +
		((this.complianceLevel >= ClassFileConstants.JDK16) ?
				"	       ^\n" +
				"Type Object cannot be safely cast to Iterator<String>\n"
				:
					"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
					"Cannot perform instanceof check against parameterized type Iterator<String>. Use the form Iterator<?> instead since further generic type information will be erased at runtime\n"
				) +
		"----------\n" +
		"2. ERROR in Bug.java (at line 3)\n" +
		"	return o instanceof java.util.Iterator<java.lang. @MyAnnot @org.eclipse.jdt.annotation.Nullable String>;\n" +
		"	                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Nullness annotations are not applicable at this location \n" +
		"----------\n",
		this.LIBS,
		true/*flush*/);
}
public void testBug466969() {
	runConformTestWithLibs(
		new String[] {
			"GenericType.java",
			"""
				public abstract class GenericType<T extends @org.eclipse.jdt.annotation.NonNull Runnable> {
					abstract T get();
				}""",
			"WildcardUsage.java",
			"""
				import static org.eclipse.jdt.annotation.DefaultLocation.*;
				
				import org.eclipse.jdt.annotation.NonNullByDefault;
				
				@NonNullByDefault({ ARRAY_CONTENTS, FIELD, PARAMETER, RETURN_TYPE, TYPE_ARGUMENT, TYPE_BOUND, TYPE_PARAMETER })
				public class WildcardUsage {
					void f(GenericType<?> p) {
						p.get().run();
					}
				}"""
			}, getCompilerOptions(), "");
}
public void testBug467032() {
	runConformTestWithLibs(
			new String[] {
				"Class1.java",
				"""
					class Class1 {;
					   enum E {}
					   void m1(E @org.eclipse.jdt.annotation.Nullable [] a) {}
					}
					"""
			}, getCompilerOptions(), "");
	runConformTestWithLibs(
			false /* don't flush */,
			new String[] {
				"Class2.java",
				"""
					class Class2 {;
					  Class1 x;\
					}
					"""
			}, getCompilerOptions(), "");
}
public void testBug467430() {
	runConformTestWithLibs(
		new String[] {
				"A.java",
				"""
					public class A {
						@org.eclipse.jdt.annotation.NonNullByDefault
						void m(java.util.@org.eclipse.jdt.annotation.Nullable Map<String, Integer> map) {
						}
						void m2(A a) {
							final java.util.Map<String, Integer> v = null;
							a.m(v);
						}
					}"""
			},
			getCompilerOptions(),
			"");
}
public void testBug467430mismatch() {
	runConformTestWithLibs(
		new String[] {
				"A.java",
				"""
					public class A {
						@org.eclipse.jdt.annotation.NonNullByDefault
						void m(java.util.@org.eclipse.jdt.annotation.Nullable Map<String, Integer> map) {
						}
						void m2(A a) {
							final java.util.Map<String, @org.eclipse.jdt.annotation.Nullable Integer> v = null;
							a.m(v);
						}
					}"""
			},
			getCompilerOptions(),
			"");
}
public void testBug467430array() {
	runConformTestWithLibs(
		new String[] {
				"A.java",
				"""
					import org.eclipse.jdt.annotation.*;
					public class A {
						@NonNullByDefault
						void m(@NonNull String @Nullable [] array) {
						}
						void m2(A a) {
							final String[] v = null;
							a.m(v);
						}
					}"""
			},
			getCompilerOptions(),
			"");
}
public void testBug467430arrayMismatch() {
	runConformTestWithLibs(
		new String[] {
				"A.java",
				"""
					import org.eclipse.jdt.annotation.*;
					public class A {
						@NonNullByDefault
						void m(@NonNull String @Nullable [] array) {
						}
						void m2(A a) {
							final @Nullable String @Nullable [] v = null;
							a.m(v);
						}
					}"""
			},
			getCompilerOptions(),
			"");
}

public void testBug446217() {
	Runner runner = new Runner();
	runner.classLibraries = this.LIBS;
	runner.testFiles =
		new String[] {
			"sol/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package sol;",
			"sol/FuncList.java",
			"""
				
				package sol;
				
				interface FuncList<A> {}
				\t
				@SuppressWarnings("unused")
				final class Node<A> implements FuncList<A> {
					private final A a;
					private final FuncList<A> tail;
				\t
					Node(final A a, final FuncList<A> tail) {
						this.a = a;
						this.tail = tail;
					}
				}
				
				final class Empty<A> implements FuncList<A> {
					Empty() {}
				}
				""",
			"sol/Test.java",
			"""
				package sol;
				
				public class Test {
					public static void main(final String[] args) {
						 System.out.println(new Node<>("A", new Empty<>()));
					}
				}
				"""
		};
	runner.javacTestOptions = new JavacTestOptions.SuppressWarnings("auxiliaryclass");
	runner.runConformTest();
}
public void testBug456584orig() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(JavaCore.COMPILER_PB_PESSIMISTIC_NULL_ANALYSIS_FOR_FREE_TYPE_VARIABLES, JavaCore.WARNING);
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"MyObjects.java",
			"""
				public class MyObjects {
					public static <T> T requireNonNull(T in) { return in; }
				}
				""",
			"Test.java",
			"""
				
				import java.util.function.*;
				import org.eclipse.jdt.annotation.*;
				
				@NonNullByDefault
				public class Test {
				
				  public static final <T,R> @NonNull R applyRequired(final T input, final Function<? super T,? extends R> function) { // Warning on '@NonNull R': "The nullness annotation is redundant with a default that applies to this location"
				    return MyObjects.requireNonNull(function.apply(input));
				  }
				
				}
				"""
		},
		compilerOptions,
		"""
			----------
			1. WARNING in Test.java (at line 9)
				return MyObjects.requireNonNull(function.apply(input));
				                                ^^^^^^^^^^^^^^^^^^^^^
			Null type safety: required \'@NonNull\' but this expression has type \'capture#2-of ? extends R\', a free type variable that may represent a \'@Nullable\' type
			----------
			""");
}
public void testBug456584() {
	// the compiler now has special information regarding Objects.requireNonNull
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(JavaCore.COMPILER_PB_PESSIMISTIC_NULL_ANALYSIS_FOR_FREE_TYPE_VARIABLES, JavaCore.WARNING);
	runConformTestWithLibs(
		true/*flush*/,
		new String[] {
			"Test.java",
			"""
				import java.util.*;
				import java.util.function.*;
				import org.eclipse.jdt.annotation.*;
				
				@NonNullByDefault
				public class Test {
				
				  public static final <T,R> @NonNull R applyRequired(final T input, final Function<? super T,? extends R> function) { // Warning on '@NonNull R': "The nullness annotation is redundant with a default that applies to this location"
				    return Objects.requireNonNull(function.apply(input));
				  }
				
				}
				"""
		},
		compilerOptions,
		"");
}
public void testBug447661() {
	runConformTestWithLibs(
		new String[] {
			"Two.java",
			"""
				import java.util.*;
				public class Two {
				
					@org.eclipse.jdt.annotation.NonNullByDefault
					public static Set<String> getSet() {
						return new HashSet<>();
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"One.java",
			"""
				import java.util.*;
				@org.eclipse.jdt.annotation.NonNullByDefault
				public class One {
				
					public void test() {
						Set<String> set = Two.getSet();
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug436091() {
	runConformTestWithLibs(
		new String[] {
			"p/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package p;\n",

			"p/Program.java",
			"""
				package p;
				public class Program {
					private final ProgramNode program;
					\
					public Program(final ProgramNode astRoot) {
						program = astRoot;
					}
					\
					public Integer execute() {
						return program.accept(ExecutionEvaluationVisitor.VISITOR);
					}
					\
					class ProgramNode {
						public <R> R accept(final ConcreteNodeVisitor<R> visitor) {
							return visitor.visit(this);
						}
					}
				}
				""",

			"p/ConcreteNodeVisitor.java",
			"""
				package p;
				import p.Program.ProgramNode;
				public interface ConcreteNodeVisitor<R> {
					R visit(ProgramNode node);
				}
				""",

			"p/ExecutionEvaluationVisitor.java",
			"package p;\n" +
			"" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"" +
			"import p.Program.ProgramNode;\n" +
			"" +
			"public enum ExecutionEvaluationVisitor implements ConcreteNodeVisitor<Integer> {\n" +
			"	" +
			"	VISITOR;\n" +
			"	" +
			"	@Override" +
			"	public Integer visit(final ProgramNode node) {\n" +
			"		@SuppressWarnings(\"null\")\n" +
			"		@NonNull\n" +
			"		final Integer i = Integer.valueOf(0);\n" +
			"		return i;\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
	// re-compile only one of the above:
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"p/Program.java",
			"""
				package p;
				public class Program {
					private final ProgramNode program;
					\
					public Program(final ProgramNode astRoot) {
						program = astRoot;
					}
					\
					public Integer execute() {
						return program.accept(ExecutionEvaluationVisitor.VISITOR);
					}
					\
					class ProgramNode {
						public <R> R accept(final ConcreteNodeVisitor<R> visitor) {
							return visitor.visit(this);
						}
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug474239() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.ERROR);
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runConformTestWithLibs(
		new String[] {
			"Test.java",
			"""
				public class Test {
					static String s1 = null, s2 = null;
				
					public static void main(String[] args) {
						int val = (int) System.currentTimeMillis();
						switch (val % 2) {
						case 0:
							if (s1 != null)
								s2 = "";
							break;
						case 1:
							if (s1 != null) // compiler thinks s1 is never null at this point
								throw new RuntimeException("");
							break;
						}
					}
				}
				"""
		},
		options,
		"");
}

public void testBug467482() {
	runConformTestWithLibs(
		new String[]{
			"Util.java",
			"""
				public abstract class Util {
					public static <T> @org.eclipse.jdt.annotation.Nullable T f(T[] valuesArray, java.util.Comparator<T> comparator) {
						@org.eclipse.jdt.annotation.Nullable
						T winner = null;
						for (T value : valuesArray) {
							if (winner == null) {
								winner = value;
							} else {
								if (comparator.compare(winner, value) < 0) {
									winner = value;
								}
							}
						}
						return winner;
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug467482simple() {
	// reduced example without generics that still exhibits the bug
	runConformTestWithLibs(
		new String[]{
			"Util.java",
			"""
				public abstract class Util {
				public static void f(Object unknown) {
					@org.eclipse.jdt.annotation.Nullable
					Object winner = null;
					for (int i = 0; i < 1; i++) {
							winner = unknown;
					}
					if (winner == null) {
						assert false;
					}
				}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug467482while() {
	// even simpler with while loop
	runConformTestWithLibs(
		new String[]{
			"Util.java",
			"""
				public abstract class Util {
				public static void f(Object unknown, boolean flag) {
					@org.eclipse.jdt.annotation.Nullable
					Object winner = null;
					while (flag) {
							winner = unknown;
							flag = false;
					}
					if (winner == null) {
						assert false;
					}
				}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug467482switch() {
	// bug behaviour visible via switch
	runConformTestWithLibs(
		new String[]{
			"Util.java",
			"""
				public abstract class Util {
				public static void f(Object unknown, boolean flag) {
					@org.eclipse.jdt.annotation.Nullable
					Object winner = null;
					switch (1) {
					case 1:	winner = unknown;
					}
					if (winner == null) {
						assert false;
					}
				}
				}
				"""
		},
		getCompilerOptions(),
		"");
}

public void testBug467482regression() {
	// simple regression test that verifies that possibly be the patch affected messages stay unchanged
	runNegativeTestWithLibs(
		new String[]{
			"Check.java",
			"""
				public abstract class Check {
					public static void check(@org.eclipse.jdt.annotation.NonNull Object x) {
					}
					public static void f(Object unknown, boolean flag) {
						check(unknown); // expected: null type safety warning
						@org.eclipse.jdt.annotation.Nullable
						Object nullable = unknown;
						check(nullable); // expected: null type mismatch error
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in Check.java (at line 5)
				check(unknown); // expected: null type safety warning
				      ^^^^^^^
			Null type safety (type annotations): The expression of type \'Object\' needs unchecked conversion to conform to \'@NonNull Object\'
			----------
			2. ERROR in Check.java (at line 8)
				check(nullable); // expected: null type mismatch error
				      ^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull Object\' but this expression has type \'@Nullable Object\'
			----------
			""");
}
public void testBug484735() {
	runConformTestWithLibs(
		new String[] {
			"test/NullabilityLoopBug.java",
			"""
				package test;
				
				import java.util.HashMap;
				import java.util.Map;
				import java.util.Map.Entry;
				
				import org.eclipse.jdt.annotation.Nullable;
				
				@org.eclipse.jdt.annotation.NonNullByDefault
				public class NullabilityLoopBug {
				
					public static void main(String[] args)
					{
						Map<String, String> map = new HashMap<>();
					\t
						map.put("key", "value");
					\t
						System.out.println(getKeyByValue(map, "value"));
					}
				\t
					private static <K, V> K getKeyByValue(Map<K, V> map, @Nullable V value)
					{
						@Nullable K result = null; // some nullability bug? assigning null results in compiler complaining 'result can only be null' below
						for (Entry<K, V> entry : map.entrySet())
						{
							boolean equals;
							if (value == null)
								equals = (entry.getValue() == null);
							else
								equals = value.equals(entry.getValue());
						\t
							if (equals)
							{
								if (result == null) // Incorrect warning: Redundant null check: The variable result can only be null at this location
									result = entry.getKey();
								else
									throw new IllegalStateException("Multiple matches for looking up key via value [" + value + "]: [" + result + "] and [" + entry.getKey() + "]");
							}
						}
					\t
						if (result == null) // Incorrect warning: Redundant null check: The variable result can only be null at this location
							throw new IllegalStateException("No matches for looking up key via value [" + value + "]");
					\t
						return result; // Incorrect warning: Dead code
					}
				}
				"""
		},
		getCompilerOptions(),
		"",
		"key");
}
public void testBug474239b() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.ERROR);
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runConformTestWithLibs(
		new String[] {
			"Test.java",
			"""
				public class Test {
					static String s2 = null;
				
					public static void main(String[] args) {
						int val = (int) System.currentTimeMillis();
						switch (val % 2) {
						case 0:
							s2 = "";
							break;
						case 1:
							if (s2 != null)
								throw new RuntimeException("");
							break;
						}
					}
				}
				"""
		},
		options,
		"");
}
public void testBug472663() {
	runConformTestWithLibs(
		new String[] {
			"test/Callee.java",
			"""
				package test;
				
				import org.eclipse.jdt.annotation.NonNullByDefault;
				
				@NonNullByDefault
				public class Callee {
					public static String staticOtherClass(String foo) {
						return foo;
					}
				
					public String instanceOtherClass(String foo) {
						return foo;
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
	// and now consume Callee.class:
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"test/Caller.java",
			"""
				package test;
				
				import java.util.function.Function;
				
				import org.eclipse.jdt.annotation.NonNullByDefault;
				
				@NonNullByDefault
				public class Caller {
					public void foo(final Callee callee) {
						Function<String, String> function;
				
						// assignments with warnings (wrong):
						function = Callee::staticOtherClass;
						function = callee::instanceOtherClass;
				
						// assignments with no warnings (ok):
						function = foo -> Callee.staticOtherClass(foo);
						function = foo -> callee.instanceOtherClass(foo);
						function = Caller::staticSameClass;
						function = this::instanceSameClass;
					}
				
					public static String staticSameClass(String foo) {
						return foo;
					}
				
					public String instanceSameClass(String foo) {
						return foo;
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug467094() {
	runConformTestWithLibs(
		new String[] {
			"A.java",
			"""
				class A {;
				   @org.eclipse.jdt.annotation.NonNull String @org.eclipse.jdt.annotation.Nullable [] x;
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug467094_local() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo(@org.eclipse.jdt.annotation.NonNull Object[] o) {
						o.hashCode();
						if (o != null) {
							System.out.print(o.toString());
						}
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (o != null) {
				    ^
			Redundant null check: The variable o cannot be null at this location
			----------
			""");
}
public void testBug467094_method() {
	runConformTestWithLibs(
			new String[] {
				"A.java",
				"""
					class A {;
						@org.eclipse.jdt.annotation.NonNull String @org.eclipse.jdt.annotation.Nullable [] m(){
							return null;
						}
						int usage(){
							if(m() == null) return 1;\s
							return 0;\s
						}
					}
					"""
			}, getCompilerOptions(), "");
}
public void testBug440398() {
	runConformTestWithLibs(
		new String[] {
			"NullTest.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault({})\r
				public class NullTest {\r
				    public static @NonNull Object[] obj = null;\r
				    public static void main(String[] args) {\r
				        obj = null;\r
				        if (obj == null) { // WARNING 1\r
				            System.out.println("NULL"); // WARNING 2\r
				        }\r
				    }\r
				}
				"""
		},
		getCompilerOptions(),
		"",
		"NULL");
}
public void testBug440398_comment2() {
	runConformTestWithLibs(
		new String[] {
			"MyClass.java",
			"""
				import org.eclipse.jdt.annotation.*;
				
				@NonNullByDefault({DefaultLocation.FIELD})
				public class MyClass {
				    private @NonNull String [] names = new @NonNull String[]{"Alice", "Bob", "Charlie"};
				
				    public String getName(int index) {
				        String name = names[index];
				        return name; /* statement A */
				    }
				}""",
		},
		getCompilerOptions(),
		"");
}
public void testBug440398_comment2a() {
	runConformTestWithLibs(
		new String[] {
			"p/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault({org.eclipse.jdt.annotation.DefaultLocation.FIELD})\n" +
			"package p;\n",
			"p/MyClass.java",
			"""
				package p;
				import org.eclipse.jdt.annotation.*;
				
				public class MyClass {
				    private @NonNull String [] names = new @NonNull String[]{"Alice", "Bob", "Charlie"};
				
				    public String getName(int index) {
				        String name = names[index];
				        return name; /* statement A */
				    }
				}""",
		},
		getCompilerOptions(),
		"");
}
public void testBug481332() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				import org.eclipse.jdt.annotation.*;
				public class X {
					public void foo() {
						@Nullable
						List<@NonNull String> list = new ArrayList<>();
						checkNotNull(list); // OK
				
						@Nullable
						Map<@NonNull String, @NonNull String> map = new HashMap<>();
						checkNotNull(map); // OK
				
						@NonNull
						Object @Nullable [] objects = new @NonNull Object[0];
						// Error: Null type mismatch (type annotations): required '@NonNull Object @NonNull[]' but this expression ...
						checkNotNull(objects);
					}
				\t
					public static <@Nullable T> T[] checkNotNull(T @Nullable [] array) {
						if (array == null) {
							throw new NullPointerException();
						}
						return array;
					}
				
					public static <@Nullable T, C extends Iterable<T>> C checkNotNull(@Nullable C container) {
						if (container == null) {
							throw new NullPointerException();
						}
						return container;
					}
				
					public static <@Nullable K, @Nullable V, M extends Map<K, V>> M checkNotNull(@Nullable M map) {
						if (map == null) {
							throw new NullPointerException();
						}
						return map;
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 7)
				checkNotNull(list); // OK
				^^^^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'List<@NonNull String>\' is not a valid substitute for the type parameter \'C extends Iterable<@Nullable T>\'
			----------
			2. ERROR in X.java (at line 11)
				checkNotNull(map); // OK
				^^^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'Map<@NonNull String,@NonNull String>\' is not a valid substitute for the type parameter \'M extends Map<@Nullable K,@Nullable V>\'
			----------
			3. ERROR in X.java (at line 16)
				checkNotNull(objects);
				             ^^^^^^^
			Null type mismatch (type annotations): required \'@Nullable Object @Nullable[]\' but this expression has type \'@NonNull Object @Nullable[]\'
			----------
			""");
}
public void testBug481322a() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				
				import org.eclipse.jdt.annotation.NonNull;
				import org.eclipse.jdt.annotation.Nullable;
				
				class Super<S, T extends List<S>> {
					S pick(T list) {
						return list.get(0);
					}
				}
				public class X extends Super<@NonNull String, List<@Nullable String>> {
					@Override
					public @NonNull String pick(List<@Nullable String> list) {
						return super.pick(list);
					}
					public static void main(String[] args) {
						List<@Nullable String> withNulls = new ArrayList<@Nullable String>();
						withNulls.add(null);
						System.out.println(new X().pick(withNulls).toUpperCase());
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 9)
				return list.get(0);
				       ^^^^
			Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			2. ERROR in X.java (at line 12)
				public class X extends Super<@NonNull String, List<@Nullable String>> {
				                                              ^^^^
			Null constraint mismatch: The type \'List<@Nullable String>\' is not a valid substitute for the type parameter \'T extends List<S>\'
			----------
			""");
}
public void testBug477719() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportNonNullTypeVariableFromLegacyInvocation, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runner.classLibraries = this.LIBS;
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
					@NonNull Number instantiate(@NonNull Class<? extends @NonNull Number> c) throws Exception {
						return c.newInstance();
					}
					void test(Double d) throws Exception {
						instantiate(Integer.class);
						instantiate(d.getClass());
					}
				}
				"""
		};
	runner.javacTestOptions = new JavacTestOptions.SuppressWarnings("deprecation");
	runner.runConformTest();
}
public void testBug482247() {
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
					@NonNull String @NonNull[] s1 = new String[0];
					@Nullable String @NonNull[] s2 = new String[0];
					<T> @NonNull T first(@NonNull T @NonNull[] arr) {
						return arr[0];
					}
					void other(@Nullable String[] s) {
						s[0] = null;
					}
					@NonNull String test()  {
						other(new String[0]);
						return first(new String[0]);
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in X.java (at line 12)
				other(new String[0]);
				      ^^^^^^^^^^^^^
			Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'@Nullable String []\'
			----------
			2. WARNING in X.java (at line 13)
				return first(new String[0]);
				             ^^^^^^^^^^^^^
			Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'@NonNull String @NonNull[]\'
			----------
			""");
}
public void testBug482247_comment5() {
	runConformTestWithLibs(
		new String[] {
			"Snippet.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Snippet {
					@NonNull String[] s1 = new String[0]; // No warning
					public void handleIncidentBeforeCreate() {
						@NonNull String[] s = new String[0]; // Warning
						String [] @NonNull[] s2 = new String[0][];
						String [] @NonNull[] @Nullable[] s3 = new String[0][][];
					}
				}"""
		},
		getCompilerOptions(),
		"");
}
public void testBug483146() {
	runConformTestWithLibs(
		new String[] {
			"Foo.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				
				import org.eclipse.jdt.annotation.NonNull;
				import org.eclipse.jdt.annotation.Nullable;
				
				class Foo {
				
					void example1() {
				        @Nullable List<String> container = new ArrayList<>();
				        @NonNull List<String> list = checkNotNull(container);
					}
				
					void example2() {
				        @Nullable List<String> container= new ArrayList<>();
				        @NonNull List<String> list = checkNotNull(container);
					}
				   \s
				    @NonNull <T, C extends  Iterable<T>> C checkNotNull(C container) {
				        if (container == null) {
				            throw new NullPointerException();
				        }
						return container;
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug483146b() {
	runConformTestWithLibs(
		new String[] {
			"Foo.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				
				import org.eclipse.jdt.annotation.NonNull;
				import org.eclipse.jdt.annotation.Nullable;
				
				class Foo {
				
					void example1() {
				        @Nullable List<String> container = new ArrayList<>();
				        @NonNull List<String> list = checkNotNull(container);
					}
				
					void example2() {
				        @Nullable List<String> container= new ArrayList<>();
				        @NonNull List<String> list = checkNotNull(container);
					}
				   \s
				    <T, C extends  Iterable<T>> @NonNull C checkNotNull(@Nullable C container) {
				        if (container == null) {
				            throw new NullPointerException();
				        }
						return container;
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug473713() {
	runConformTestWithLibs(
		new String[] {
			"a/A1.java",
			"""
				package a;
				@org.eclipse.jdt.annotation.NonNullByDefault
				public class A1 {
					public class NestedInA1 {
					}
				}
				""",
			"a/A2.java",
			"""
				package a;
				@org.eclipse.jdt.annotation.NonNullByDefault
				public class A2 {
					public static abstract class NestedInA2 {
						public final A1 a1 = new A1();
						protected abstract void handleApplicationSpecific(A1.NestedInA1 detail);
					}
				}
				""",
		}, getCompilerOptions(), "");
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"b/B.java",
			"""
				package b;
				@org.eclipse.jdt.annotation.NonNullByDefault
				public class B {
					public static a.A1 m(a.A2.NestedInA2 nestedInA2) {
						return nestedInA2.a1;
					}
				}
				""",
		}, getCompilerOptions(), "");
}
public void testBug482228() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				
				import org.eclipse.jdt.annotation.NonNull;
				import org.eclipse.jdt.annotation.Nullable;
				
				class Super<S> {
					<T extends List<S>> S pick(T list) {
						return list.get(0);
					}
				}
				public class X extends Super<@NonNull String> {
					@Override
					<T extends List<@Nullable String>> @NonNull String pick(T list) {
						return super.pick(list);
					}
					public static void main(String[] args) {
						List<@Nullable String> withNulls = new ArrayList<>();
						withNulls.add(null);
						System.out.println(new X().pick(withNulls).toUpperCase());
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 9)
				return list.get(0);
				       ^^^^
			Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			2. ERROR in X.java (at line 14)
				<T extends List<@Nullable String>> @NonNull String pick(T list) {
				 ^
			Cannot redefine null constraints of type variable \'T extends List<@NonNull String>\' declared in \'Super<String>.pick(T)\'
			----------
			3. ERROR in X.java (at line 15)
				return super.pick(list);
				       ^^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'T extends List<@Nullable String>\' is not a valid substitute for the type parameter \'T extends List<@NonNull String>\'
			----------
			""");
}
public void testBug483527() {
	final Map compilerOptions = getCompilerOptions();
	compilerOptions.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runConformTestWithLibs(
		new String[] {
			"Test.java",
			"""
				public class Test  {
				    static final short foo;
					static {
						foo = 1;
						for (int i=0; i<10; i++) {
						}
					}
				}
				"""
		},
		compilerOptions,
		"");
}
public void testMultipleAnnotations1() {
	Map options1 = new HashMap<>(getCompilerOptions());
	options1.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
	options1.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
	runConformTest(
		new String[] {
			"org/foo/Nullable.java",
			"""
				package org.foo;
				import java.lang.annotation.*;
				@Retention(RetentionPolicy.CLASS)
				@Target({ElementType.TYPE_USE})
				public @interface Nullable {}
				""",
			"org/foo/NonNull.java",
			"""
				package org.foo;
				import java.lang.annotation.*;
				@Retention(RetentionPolicy.CLASS)
				@Target({ElementType.TYPE_USE})
				public @interface NonNull {}
				""",
			"p1/TestNulls.java",
			"""
				package p1;
				import org.foo.*;
				
				public class TestNulls {
					public @Nullable String weaken(@NonNull String theValue) {
						return theValue;
					}
				
				}"""
		},
		options1);
	Map options2 = getCompilerOptions();
	options2.put(CompilerOptions.OPTION_NonNullAnnotationSecondaryNames, "org.foo.NonNull");
	options2.put(CompilerOptions.OPTION_NullableAnnotationSecondaryNames, "org.foo.Nullable");
	runNegativeTestWithLibs(
		new String[] {
			"p2/Test.java",
			"""
				package p2;
				import p1.TestNulls;
				import org.eclipse.jdt.annotation.*;
				public class Test {
					@NonNull String test(TestNulls test, @Nullable String input) {
						return test.weaken(input);
					}
				}
				"""
		},
		options2,
		"""
			----------
			1. ERROR in p2\\Test.java (at line 6)
				return test.weaken(input);
				       ^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'
			----------
			2. ERROR in p2\\Test.java (at line 6)
				return test.weaken(input);
				                   ^^^^^
			Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'
			----------
			""");
}
public void test483952 () {
	runConformTestWithLibs(
		new String[] {
			"test/Test.java",
			"""
				package test;
				import java.util.function.Function;
				import org.eclipse.jdt.annotation.Nullable;
				public class Test {
					void test1() {
						Function function = x -> x;
						String @Nullable [] z = test2(function, "");
					}
					<T> T @Nullable [] test2(Function<T, T> function, T t) {
						return null;
					}
				}"""

		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in test\\Test.java (at line 6)
				Function function = x -> x;
				^^^^^^^^
			Function is a raw type. References to generic type Function<T,R> should be parameterized
			----------
			2. WARNING in test\\Test.java (at line 7)
				String @Nullable [] z = test2(function, "");
				                        ^^^^^^^^^^^^^^^^^^^
			Type safety: Unchecked invocation test2(Function, String) of the generic method test2(Function<T,T>, T) of type Test
			----------
			3. WARNING in test\\Test.java (at line 7)
				String @Nullable [] z = test2(function, "");
				                              ^^^^^^^^
			Type safety: The expression of type Function needs unchecked conversion to conform to Function<String,String>
			----------
			""");
}
public void test484055() {
	runConformTestWithLibs(
		new String[] {
			"B.java",
			"""
				interface A {
					public void f(String[] x);
				
					public void f2(String x);
				}
				
				public class B implements A {
					public void f(String @org.eclipse.jdt.annotation.Nullable [] x) {
					}
				
					public void f2(@org.eclipse.jdt.annotation.Nullable String x) {
					}
				}"""
		},
		null,
		"");
}
public void testBug484108() {
	runConformTestWithLibs(
		new String[] {
			"test/Test.java",
			"""
				package test;
				
				import org.eclipse.jdt.annotation.NonNull;
				
				public interface Test <T0 extends Other> {
				    public void a ( @NonNull T0 test );
				}
				""",
			"test/Other.java",
			"""
				package test;
				
				public interface Other { }
				"""
		},
		getCompilerOptions(),
		"");
	runConformTestWithLibs(
		false /* don't flush output dir */,
		new String[] {
			"test/TestImpl.java",
			"""
				package test;
				
				import org.eclipse.jdt.annotation.NonNull;
				import java.lang.reflect.*;
				
				public class TestImpl <T extends Other> implements Test<T> {
				
				    /**
				     * {@inheritDoc}
				     *
				     * @see test.Test#a(java.lang.Object)
				     */
				    @Override
				    public void a ( @NonNull T test ) {
				    }
					public static void main(String... args) {
						Class<?> c = TestImpl.class;
						Method[] ms = c.getDeclaredMethods();
						System.out.println(ms.length);
					}
				}
				"""
		},
		getCompilerOptions(),
		"",
		"2");
}
public void testBug484954() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runConformTest(
		new String[] {
			CUSTOM_NULLABLE_NAME,
			CUSTOM_NULLABLE_CONTENT, // sic: declaration annotation
			CUSTOM_NONNULL_NAME,
			CUSTOM_NONNULL_CONTENT, // sic: declaration annotation
			"Snippet.java",
			"""
				import java.util.function.*;
				import org.foo.*;
				
				public class Snippet {
				
					public void test() {
						doStuff((@NonNull Object[] data) -> updateSelectionData(data));\s
					}
				
					private void doStuff(Consumer<Object[]> postAction) { }
					private void updateSelectionData(final @NonNull Object data) { }
				}
				"""
		},
		customOptions,
		"");
}
public void testBug484981() {
	runNegativeTestWithLibs(
		new String[] {
			"test1/GenericWithNullableBound.java",
			"""
				package test1;
				import org.eclipse.jdt.annotation.NonNullByDefault;
				import org.eclipse.jdt.annotation.Nullable;
				@NonNullByDefault
				public class GenericWithNullableBound<T extends @Nullable Number> {
				}
				""",

			"test1/GenericWithNullableBound2.java",
			"""
				package test1;
				import static java.lang.annotation.ElementType.TYPE_USE;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;
				import org.eclipse.jdt.annotation.NonNullByDefault;
				import org.eclipse.jdt.annotation.Nullable;
				@Retention(RetentionPolicy.CLASS)
				@Target({ TYPE_USE })
				@interface SomeAnnotation {
				}
				@NonNullByDefault
				public class GenericWithNullableBound2<@SomeAnnotation T extends @Nullable Number> {
				}
				""",

			"test1/GenericWithNullable.java",
			"""
				package test1;
				import org.eclipse.jdt.annotation.NonNullByDefault;
				import org.eclipse.jdt.annotation.Nullable;
				@NonNullByDefault
				public class GenericWithNullable<@Nullable T> {
				}
				""",

			"test1/GenericWithNonNullBound.java",
			"""
				package test1;
				import org.eclipse.jdt.annotation.NonNull;
				public class GenericWithNonNullBound<T extends @NonNull Number> {
				}
				""",
			"test1/ClassInSameProject.java",
			"""
				package test1;
				import org.eclipse.jdt.annotation.NonNull;
				import org.eclipse.jdt.annotation.Nullable;
				public class ClassInSameProject {
					static void f1() {
						new GenericWithNullableBound<@NonNull Number>();
					}
					static void f2() {
						new GenericWithNullableBound2<@NonNull Number>();
					}
					static void f3() {
						new GenericWithNonNullBound<@Nullable Number>(); // error 1 expected
					}
					static void f4() {
						new GenericWithNullable<@NonNull Number>(); // error 2 expected
					}
				}"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test1\\ClassInSameProject.java (at line 12)
				new GenericWithNonNullBound<@Nullable Number>(); // error 1 expected
				                            ^^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'@Nullable Number\' is not a valid substitute for the type parameter \'T extends @NonNull Number\'
			----------
			2. ERROR in test1\\ClassInSameProject.java (at line 15)
				new GenericWithNullable<@NonNull Number>(); // error 2 expected
				                        ^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'@NonNull Number\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			"""
	);
	runNegativeTestWithLibs(
			new String[] {
				"test2/ClassInOtherProject.java",
				"""
					package test2;
					import org.eclipse.jdt.annotation.NonNull;
					import org.eclipse.jdt.annotation.Nullable;
					import test1.GenericWithNonNullBound;
					import test1.GenericWithNullable;
					import test1.GenericWithNullableBound;
					import test1.GenericWithNullableBound2;
					public class ClassInOtherProject {
						static void g1() {
							new GenericWithNullableBound<@NonNull Number>();
						}
						static void g2() {
							new GenericWithNullableBound2<@NonNull Number>();
						}
						static void g3() {
							new GenericWithNonNullBound<@Nullable Number>(); // error 3 expected
						}
						static void g4() {
							new GenericWithNullable<@NonNull Number>(); // error 4 expected
						}
					}"""
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in test2\\ClassInOtherProject.java (at line 16)
					new GenericWithNonNullBound<@Nullable Number>(); // error 3 expected
					                            ^^^^^^^^^^^^^^^^
				Null constraint mismatch: The type \'@Nullable Number\' is not a valid substitute for the type parameter \'T extends @NonNull Number\'
				----------
				2. ERROR in test2\\ClassInOtherProject.java (at line 19)
					new GenericWithNullable<@NonNull Number>(); // error 4 expected
					                        ^^^^^^^^^^^^^^^
				Null constraint mismatch: The type \'@NonNull Number\' is not a valid substitute for the type parameter \'@Nullable T extends Object\'
				----------
				"""
	);
}
// same testBinary06 but via SourceTypeBindings
public void testBug484981b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	customOptions.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.IGNORE);
	runNegativeTestWithLibs(
			new String[] {
				"p/X1.java",
				"""
					package p;
					import java.util.ArrayList;
					import org.eclipse.jdt.annotation.*;
					public abstract class X1<T extends @NonNull Object> extends ArrayList<T> {
					    public <U, V extends @Nullable Object> void foo(U u, V v) {}
					}
					""",
				"p/X2.java",
				"""
					package p;
					import org.eclipse.jdt.annotation.*;
					public class X2<@Nullable W extends Object> {}
					""",
				"Y1.java",
				"""
					import p.X1;
					import p.X2;
					import org.eclipse.jdt.annotation.*;
					public class Y1 {
						X1<@Nullable String> maybeStrings;
					   X2<@NonNull String> strings;
						void test(X1<@NonNull String> x) {
							x.<Y1, @NonNull Object>foo(this, new Object());
						}
					}
					"""
			},
			customOptions,
			"""
				----------
				1. ERROR in Y1.java (at line 5)
					X1<@Nullable String> maybeStrings;
					   ^^^^^^^^^^^^^^^^
				Null constraint mismatch: The type \'@Nullable String\' is not a valid substitute for the type parameter \'T extends @NonNull Object\'
				----------
				2. ERROR in Y1.java (at line 6)
					X2<@NonNull String> strings;
					   ^^^^^^^^^^^^^^^
				Null constraint mismatch: The type \'@NonNull String\' is not a valid substitute for the type parameter \'@Nullable W extends Object\'
				----------
				"""
			);
}

// same testBinary06b but via SourceTypeBindings
public void testBug484981c() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	customOptions.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.IGNORE);
	// fix the bug:
	runNegativeTestWithLibs(
			new String[] {
				"p/X1.java",
				"""
					package p;
					import java.util.ArrayList;
					import org.eclipse.jdt.annotation.*;
					public abstract class X1<T extends java.lang.@NonNull Object> extends ArrayList<T> {
					    public <U, V extends java.lang.@Nullable Object> void foo(U u, V v) {}
					}
					""",
				"p/X2.java",
				"""
					package p;
					import org.eclipse.jdt.annotation.*;
					public class X2<@Nullable W extends Object> {}
					""",
				"Y1.java",
				"""
					import org.eclipse.jdt.annotation.*;
					public class Y1 {
						p.X1<java.lang.@Nullable String> maybeStrings;
					   p.X2<java.lang.@NonNull String> strings;
						void test(p.X1<java.lang.@NonNull String> x) {
							x.<Y1, java.lang.@NonNull Object>foo(this, new Object());
						}
					}
					"""
			},
			customOptions,
			"""
				----------
				1. ERROR in Y1.java (at line 3)
					p.X1<java.lang.@Nullable String> maybeStrings;
					     ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Null constraint mismatch: The type \'@Nullable String\' is not a valid substitute for the type parameter \'T extends @NonNull Object\'
				----------
				2. ERROR in Y1.java (at line 4)
					p.X2<java.lang.@NonNull String> strings;
					     ^^^^^^^^^^^^^^^^^^^^^^^^^
				Null constraint mismatch: The type \'@NonNull String\' is not a valid substitute for the type parameter \'@Nullable W extends Object\'
				----------
				"""
			);
}

// same testBinary07 but via SourceTypeBindings
public void testBug484981d() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	customOptions.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.IGNORE);
	runNegativeTestWithLibs(
			new String[] {
				"p/List.java",
				"""
					package p;
					public interface List<T> {
						T get(int i);
					}
					""",
				"p/X1.java",
				"""
					package p;
					import java.util.Map;
					import org.eclipse.jdt.annotation.*;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.*;
					@Retention(RetentionPolicy.CLASS)
					@Target(TYPE_USE)
					@interface Immutable {}
					public abstract class X1 {
					    public <@NonNull U, V extends @Nullable Object> List<@NonNull Map<Object, @NonNull String>> foo(@Immutable X1 this, U u, V v) { return null; }
					}
					""",
				"Y1.java",
				"""
					import p.X1;
					import org.eclipse.jdt.annotation.*;
					public class Y1 {
						void test(X1 x) {
							x.<@NonNull Y1, @NonNull Object>foo(this, new Object())
								.get(0).put(null, null);
						}
					}
					"""
			},
			customOptions,
			"""
				----------
				1. ERROR in Y1.java (at line 6)
					.get(0).put(null, null);
					                  ^^^^
				Null type mismatch: required \'@NonNull String\' but the provided value is null
				----------
				""");
}
public void testBug466562() {
	runNegativeTestWithLibs(
		new String[] {
			"x/C.java",
			"""
				package x;
				import org.eclipse.jdt.annotation.NonNull;
				import org.eclipse.jdt.annotation.NonNullByDefault;
				import org.eclipse.jdt.annotation.Nullable;
				@NonNullByDefault({})
				public class C <T1 extends @Nullable Number> {
				    String consume(T1 t) {
				        @NonNull Object x = t; // error, should warn?
				        x.toString();
				        return t.toString(); // legal???
				    }
				    void y() {
				        consume(null);  // illegal - OK
				        @NonNull Object t = provide();  // error, should warn?
				        t.toString();
				    }
				    T1 provide() {
				        return null; // error, should warn?
				    }
				    C<Integer> cString;  // OK - Null constraint mismatch: The type 'Integer' is not a valid substitute for the type parameter 'T1 extends @Nullable Number'
				    C<@NonNull Integer> c1String;  // Wrong: Null constraint mismatch: The type '@NonNull Integer' is not a valid substitute for the type parameter 'T1 extends @Nullable Number'
				    C<@Nullable Integer> c2String; // legal - OK
				}"""
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in x\\C.java (at line 5)
				@NonNullByDefault({})
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing package x
			----------
			2. ERROR in x\\C.java (at line 8)
				@NonNull Object x = t; // error, should warn?
				                    ^
			Null type safety: required \'@NonNull\' but this expression has type \'T1\', a free type variable that may represent a \'@Nullable\' type
			----------
			3. ERROR in x\\C.java (at line 10)
				return t.toString(); // legal???
				       ^
			Potential null pointer access: this expression has type \'T1\', a free type variable that may represent a \'@Nullable\' type
			----------
			4. ERROR in x\\C.java (at line 13)
				consume(null);  // illegal - OK
				        ^^^^
			Null type mismatch: required \'T1 extends @Nullable Number\' but the provided value is null
			----------
			5. ERROR in x\\C.java (at line 14)
				@NonNull Object t = provide();  // error, should warn?
				                    ^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull Object\' but this expression has type \'T1 extends @Nullable Number\'
			----------
			6. ERROR in x\\C.java (at line 18)
				return null; // error, should warn?
				       ^^^^
			Null type mismatch: required \'T1 extends @Nullable Number\' but the provided value is null
			----------
			"""
	);
}
public void testBug485056() {
	runConformTestWithLibs(
		new String[] {
			"TestExplainedValue.java",
			"""
				import java.io.Serializable;
				import org.eclipse.jdt.annotation.NonNull;
				import org.eclipse.jdt.annotation.Nullable;
				class ExplainedValue<T extends Serializable> {
					public @Nullable T featureValue;
				}
				public class TestExplainedValue {
					static @Nullable Serializable g(ExplainedValue<? extends @NonNull Serializable> explainedValue) {
						return explainedValue.featureValue;
					}
				}"""
		},
		getCompilerOptions(),
		""
	);
}
public void testBug484741() {
	runConformTestWithLibs(
		new String[] {
			"test/Test.java",
			"""
				package test;
				
				public class Test {
					static <T, E extends T> void f(java.util.ArrayList<T> list, E element) {
						list.add(element);\
					}
				
					static <A> void g(A a) {
						f(new java.util.ArrayList<A>(), a);
					}
				
					static <T1, E1 extends T1> void h(E1 element1, java.util.ArrayList<T1> list1) {
						f(list1, element1);
					}
				}"""
	}, getCompilerOptions(), "");
}
public void testBug484741b() {
	runConformTestWithLibs(
		new String[] {
			"test/TestDep.java",
			"""
				package test;
				public class TestDep {
					static <T, E extends T> T f(E e) {
						return e;
					}
				}"""
	}, getCompilerOptions(), "");
}
public void testBug484741c() {
	runConformTestWithLibs(
		new String[] {
			"test/Test3.java",
			"""
				package test;
				import org.eclipse.jdt.annotation.DefaultLocation;
				import org.eclipse.jdt.annotation.NonNullByDefault;
				@NonNullByDefault({ DefaultLocation.PARAMETER, DefaultLocation.RETURN_TYPE, DefaultLocation.FIELD, DefaultLocation.TYPE_ARGUMENT })
				class Feature3<ValueType extends java.io.Serializable, PartitionKeyType> {
				}
				@NonNullByDefault({ DefaultLocation.PARAMETER, DefaultLocation.RETURN_TYPE, DefaultLocation.FIELD, DefaultLocation.TYPE_ARGUMENT })
				public class Test3 {
					public static <T extends java.io.Serializable, F extends Feature3<T, ?>> T[] getValues(F feature) {
						throw new RuntimeException();
					}
					public static void f(Feature3<?, ?> feature) {
						getValues(feature);
					}
				}"""
	}, getCompilerOptions(), "");
}
public void testBug484741d() {
	runConformTestWithLibs(
		new String[] {
			"BaseNNBD.java",
			"""
				@org.eclipse.jdt.annotation.NonNullByDefault
				public class BaseNNBD<S extends Runnable, I extends S> {
				}
				""",
			"DerivedNNBD.java",
			"""
				@org.eclipse.jdt.annotation.NonNullByDefault
				public class DerivedNNBD<S1 extends Runnable, I1 extends S1> extends BaseNNBD<S1, I1> {\t
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug484741e() {
	runConformTestWithLibs(
		new String[] {
			"test/AbstractFeature.java",
			"""
				package test;
				import java.io.Serializable;
				import org.eclipse.jdt.annotation.NonNull;
				
				abstract class AbstractFeature<T extends @NonNull Serializable> {
				}
				""",
			"test/SubFeature.java",
			"""
				package test;
				import java.io.Serializable;
				import org.eclipse.jdt.annotation.NonNull;
				
				public class SubFeature<T1 extends @NonNull Serializable> extends AbstractFeature<T1> {
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug484741Invoke() {
	runNegativeTestWithLibs(
		new String[] {
			"test/TestInterdepInvoke.java",
			"""
				package test;
				
				import org.eclipse.jdt.annotation.Nullable;
				
				public class TestInterdepInvoke {
					static <T, E extends T> T f1(E e) {
						return e;
					}
				
					static <T, @Nullable E extends T> T f2(E e) {
						return e; // error 1 expected
					}
				
					static <@Nullable T, E extends T> T f3(E e) {
						return e;
					}
				
					static <@Nullable T, @Nullable E extends T> T f4(E e) {
						return e;
					}
				
					// -------- invocations of f1 --------
				
					static <T11, E11 extends T11> T11 g11(E11 e) {
						return f1(e);
					}
				
					static <T12, @Nullable E12 extends T12> T12 g12(E12 e) {
						return f1(e); // error 2 expected
					}
				
					static <@Nullable T13, E13 extends T13> T13 g13(E13 e) {
						return f1(e);
					}
				
					static <@Nullable T14, @Nullable E14 extends T14> T14 g14(E14 e) {
						return f1(e);
					}
				
					// -------- invocations of f2 --------
				
					static <T21, E21 extends T21> T21 g21(E21 e) {
						return f2(e);
					}
				
					static <T22, @Nullable E22 extends T22> T22 g22(E22 e) {
						return f2(e);
					}
				
					static <@Nullable T23, E23 extends T23> T23 g23(E23 e) {
						return f2(e);
					}
				
					static <@Nullable T24, @Nullable E24 extends T24> T24 g24(E24 e) {
						return f2(e);
					}
				
					// -------- invocations of f3 --------
				
					static <T31, E31 extends T31> T31 g31(E31 e) {
						return f3(e); // error 3 expected
					}
				
					static <T32, @Nullable E32 extends T32> T32 g32(E32 e) {
						return f3(e); // error 4 expected
					}
				
					static <@Nullable T33, E33 extends T33> T33 g33(E33 e) {
						return f3(e);
					}
				
					static <@Nullable T34, @Nullable E34 extends T34> T34 g34(E34 e) {
						return f3(e);
					}
				
					// -------- invocations of f4 --------
				
					static <T41, E41 extends T41> T41 g41(E41 e) {
						return f4(e); /// error 5 expected
					}
				
					static <T42, @Nullable E42 extends T42> T42 g42(E42 e) {
						return f4(e); // error 6 expected
					}
				
					static <@Nullable T43, E43 extends T43> T43 g43(E43 e) {
						return f4(e);
					}
				
					static <@Nullable T44, @Nullable E44 extends T44> T44 g44(E44 e) {
						return f4(e);
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\TestInterdepInvoke.java (at line 11)
				return e; // error 1 expected
				       ^
			Null type mismatch (type annotations): required \'T\' but this expression has type \'@Nullable E extends T\', where \'T\' is a free type variable
			----------
			2. ERROR in test\\TestInterdepInvoke.java (at line 29)
				return f1(e); // error 2 expected
				       ^^^^^
			Null type mismatch (type annotations): required \'T12\' but this expression has type \'@Nullable E12 extends T12\', where \'T12\' is a free type variable
			----------
			3. ERROR in test\\TestInterdepInvoke.java (at line 61)
				return f3(e); // error 3 expected
				       ^^^^^
			Null type mismatch (type annotations): required \'T31\' but this expression has type \'@Nullable E31 extends T31\', where \'T31\' is a free type variable
			----------
			4. ERROR in test\\TestInterdepInvoke.java (at line 65)
				return f3(e); // error 4 expected
				       ^^^^^
			Null type mismatch (type annotations): required \'T32\' but this expression has type \'@Nullable E32 extends T32\', where \'T32\' is a free type variable
			----------
			5. ERROR in test\\TestInterdepInvoke.java (at line 79)
				return f4(e); /// error 5 expected
				       ^^^^^
			Null type mismatch (type annotations): required \'T41\' but this expression has type \'@Nullable E41 extends T41\', where \'T41\' is a free type variable
			----------
			6. ERROR in test\\TestInterdepInvoke.java (at line 83)
				return f4(e); // error 6 expected
				       ^^^^^
			Null type mismatch (type annotations): required \'T42\' but this expression has type \'@Nullable E42 extends T42\', where \'T42\' is a free type variable
			----------
			"""
	);
}
public void testBug484741Invoke2() {
	runNegativeTestWithLibs(
		new String[] {
			"test/TestInterdepInvokeNN.java",
			"""
				package test;
				
				import org.eclipse.jdt.annotation.NonNull;
				
				@java.lang.annotation.Target({ java.lang.annotation.ElementType.TYPE_USE })
				@interface SomeAnnotation {
					// just needed as workaround if bug 484981 is not fixed
				}
				
				public class TestInterdepInvokeNN {
					static <T, @SomeAnnotation E extends T> T f1(E e) {
						return e;
					}
				
					static <T, @NonNull E extends T> T f2(E e) {
						return e;
					}
				
					static <@NonNull T, @SomeAnnotation E extends T> T f3(E e) {
						return e;
					}
				
					static <@NonNull T, @NonNull E extends T> T f4(E e) {
						return e;
					}
				
					// -------- invocations of f1 --------
				
					static <T11, @SomeAnnotation E11 extends T11> T11 g11(E11 e) {
						return f1(e);
					}
				
					static <T12, @NonNull E12 extends T12> T12 g12(E12 e) {
						return f1(e);
					}
				
					static <@NonNull T13, @SomeAnnotation E13 extends T13> T13 g13(E13 e) {
						return f1(e);
					}
				
					static <@NonNull T14, @NonNull E14 extends T14> T14 g14(E14 e) {
						return f1(e);
					}
				
					// -------- invocations of f2 --------
				
					static <T21, @SomeAnnotation E21 extends T21> T21 g21(E21 e) {
						return f2(e); // error 1 expected
					}
				
					static <T22, @NonNull E22 extends T22> T22 g22(E22 e) {
						return f2(e);
					}
				
					static <@NonNull T23, @SomeAnnotation E23 extends T23> T23 g23(E23 e) {
						return f2(e);
					}
				
					static <@NonNull T24, @NonNull E24 extends T24> T24 g24(E24 e) {
						return f2(e);
					}
				
					// -------- invocations of f3 --------
				
					static <T31, @SomeAnnotation E31 extends T31> T31 g31(E31 e) {
						return f3(e); // error 2 expected
					}
				
					static <T32, @NonNull E32 extends T32> T32 g32(E32 e) {
						return f3(e);
					}
				
					static <@NonNull T33, @SomeAnnotation E33 extends T33> T33 g33(E33 e) {
						return f3(e);
					}
				
					static <@NonNull T34, @NonNull E34 extends T34> T34 g34(E34 e) {
						return f3(e);
					}
				
					// -------- invocations of f4 --------
				
					static <T41, @SomeAnnotation E41 extends T41> T41 g41(E41 e) {
						return f4(e); // error 3 expected
					}
				
					static <T42, @NonNull E42 extends T42> T42 g42(E42 e) {
						return f4(e);
					}
				
					static <@NonNull T43, @SomeAnnotation E43 extends T43> T43 g43(E43 e) {
						return f4(e);
					}
				
					static <@NonNull T44, @NonNull E44 extends T44> T44 g44(E44 e) {
						return f4(e);
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\TestInterdepInvokeNN.java (at line 48)
				return f2(e); // error 1 expected
				          ^
			Null type safety: required '@NonNull' but this expression has type 'E21', a free type variable that may represent a '@Nullable' type
			----------
			2. ERROR in test\\TestInterdepInvokeNN.java (at line 66)
				return f3(e); // error 2 expected
				          ^
			Null type safety: required '@NonNull' but this expression has type 'E31', a free type variable that may represent a '@Nullable' type
			----------
			3. ERROR in test\\TestInterdepInvokeNN.java (at line 84)
				return f4(e); // error 3 expected
				          ^
			Null type safety: required '@NonNull' but this expression has type 'E41', a free type variable that may represent a '@Nullable' type
			----------
			"""
	);
}
public void testBug484741Invoke3() {
	runNegativeTestWithLibs(
		new String[] {
			"test/TestInterdepInvoke.java",
			"""
				package test;
				
				import java.util.ArrayList;
				
				import org.eclipse.jdt.annotation.NonNull;
				import org.eclipse.jdt.annotation.Nullable;
				
				public class TestInterdepInvoke {
					static <T1, E1 extends T1> void f1(ArrayList<T1> list, E1 e) {
						list.add(e);
					}
				
					static <T2, @Nullable E2 extends T2> void f2(ArrayList<T2> list, E2 e) {
						list.add(e); // error expected
					}
				
					static <T3, @NonNull E3 extends T3> void f3(ArrayList<T3> list, E3 e) {
						list.add(e);
					}
				
					static <@Nullable T4, E4 extends T4> void f4(ArrayList<T4> list, E4 e) {
						list.add(e);
					}
				
					static <@Nullable T5, @Nullable E5 extends T5> void f5(ArrayList<T5> list, E5 e) {
						list.add(e);
					}
				
					static <@Nullable T6, @NonNull E6 extends T6> void f6(ArrayList<T6> list, E6 e) {
						list.add(e);
					}
				
					static <@NonNull T7, E7 extends T7> void f7(ArrayList<T7> list, E7 e) {
						list.add(e);
					}
				
					static <@NonNull T8, @Nullable E8 extends T8> void f8(ArrayList<T8> list, E8 e) {
						list.add(e); // error expected
					}
				
					static <@NonNull T9, @NonNull E9 extends T9> void f9(ArrayList<T9> list, E9 e) {
						list.add(e);
					}
				
					// -------- invocations, but all of the 81 combinations removed, that were already handled correctly  -----
				
					static <S1, F1 extends S1> void g1(ArrayList<S1> list, F1 e) {
						f1(list, e);
						f2(list, e);
					}
				
					static <S2, @Nullable F2 extends S2> void g2(ArrayList<S2> list, F2 e) {
						f1(list, e);
						f2(list, e);
					}
				
					static <S3, @NonNull F3 extends S3> void g3(ArrayList<S3> list, F3 e) {
						f2(list, e);
					}
				}
				""",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\TestInterdepInvoke.java (at line 14)
				list.add(e); // error expected
				         ^
			Null type mismatch (type annotations): required \'T2\' but this expression has type \'@Nullable E2 extends T2\', where \'T2\' is a free type variable
			----------
			2. ERROR in test\\TestInterdepInvoke.java (at line 38)
				list.add(e); // error expected
				         ^
			Null type mismatch (type annotations): required \'@NonNull T8\' but this expression has type \'@Nullable E8 extends @NonNull T8\'
			----------
			"""
	);
}


public void testBug484471SubclassNullable() {
	runNegativeTestWithLibs(
		new String[] {
			"test/TestInterdepSubClass.java",
			"""
				package test;
				
				import org.eclipse.jdt.annotation.Nullable;
				
				public class TestInterdepSubClass {
					static class A1<T, E extends T> {
					}
				
					static class A2<T, @Nullable E extends T> {
					}
				
					static class A3<@Nullable T, E extends T> {
					}
				
					static class A4<@Nullable T, @Nullable E extends T> {
					}
				
					// -------- subclasses of A1<T, E extends T> --------
				
					static class B11<T11, E11 extends T11> extends A1<T11, E11> {
					}
				
					static class B12<T12, @Nullable E12 extends T12> extends A1<T12, E12> {
					}
				
					static class B13<@Nullable T13, E13 extends T13> extends A1<T13, E13> {
					}
				
					static class B14<@Nullable T14, @Nullable E14 extends T14> extends A1<T14, E14> {
					}
				
					// -------- subclasses of A2<T, @Nullable E extends T> --------
				
					static class B21<T21, E21 extends T21> extends A2<T21, E21> { // expect error 1
					}
				
					static class B22<T22, @Nullable E22 extends T22> extends A2<T22, E22> {
					}
				
					static class B23<@Nullable T23, E23 extends T23> extends A2<T23, E23> { // expect error 2
					}
				
					static class B24<@Nullable T24, @Nullable E24 extends T24> extends A2<T24, E24> {
					}
				
					// -------- subclasses of A3<@Nullable T, E extends T> --------
				
					static class B31<T31, E31 extends T31> extends A3<T31, E31> { // expect error 3
					}
				
					static class B32<T32, @Nullable E32 extends T32> extends A3<T32, E32> { // expect error 4
					}
				
					static class B33<@Nullable T33, E33 extends T33> extends A3<T33, E33> {
					}
				
					static class B34<@Nullable T34, @Nullable E34 extends T34> extends A3<T34, E34> {
					}
				
					// -------- subclasses of A4<@Nullable T, @Nullable E extends T> --------
				
					static class B41<T41, E41 extends T41> extends A4<T41, E41> { // expect errors 5 & 6
					}
				
					static class B42<T42, @Nullable E42 extends T42> extends A4<T42, E42> { // expect error 7
					}
				
					static class B43<@Nullable T43, E43 extends T43> extends A4<T43, E43> { // expect error 8
					}
				
					static class B44<@Nullable T44, @Nullable E44 extends T44> extends A4<T44, E44> {
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\TestInterdepSubClass.java (at line 34)
				static class B21<T21, E21 extends T21> extends A2<T21, E21> { // expect error 1
				                                                       ^^^
			Null constraint mismatch: The type \'E21 extends T21\' is not a valid substitute for the type parameter \'@Nullable E extends T\'
			----------
			2. ERROR in test\\TestInterdepSubClass.java (at line 40)
				static class B23<@Nullable T23, E23 extends T23> extends A2<T23, E23> { // expect error 2
				                                                                 ^^^
			Null constraint mismatch: The type \'E23 extends @Nullable T23\' is not a valid substitute for the type parameter \'@Nullable E extends T\'
			----------
			3. ERROR in test\\TestInterdepSubClass.java (at line 48)
				static class B31<T31, E31 extends T31> extends A3<T31, E31> { // expect error 3
				                                                  ^^^
			Null constraint mismatch: The type \'T31\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			4. ERROR in test\\TestInterdepSubClass.java (at line 51)
				static class B32<T32, @Nullable E32 extends T32> extends A3<T32, E32> { // expect error 4
				                                                            ^^^
			Null constraint mismatch: The type \'T32\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			5. ERROR in test\\TestInterdepSubClass.java (at line 62)
				static class B41<T41, E41 extends T41> extends A4<T41, E41> { // expect errors 5 & 6
				                                                  ^^^
			Null constraint mismatch: The type \'T41\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			6. ERROR in test\\TestInterdepSubClass.java (at line 62)
				static class B41<T41, E41 extends T41> extends A4<T41, E41> { // expect errors 5 & 6
				                                                       ^^^
			Null constraint mismatch: The type \'E41 extends T41\' is not a valid substitute for the type parameter \'@Nullable E extends @Nullable T\'
			----------
			7. ERROR in test\\TestInterdepSubClass.java (at line 65)
				static class B42<T42, @Nullable E42 extends T42> extends A4<T42, E42> { // expect error 7
				                                                            ^^^
			Null constraint mismatch: The type \'T42\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			8. ERROR in test\\TestInterdepSubClass.java (at line 68)
				static class B43<@Nullable T43, E43 extends T43> extends A4<T43, E43> { // expect error 8
				                                                                 ^^^
			Null constraint mismatch: The type \'E43 extends @Nullable T43\' is not a valid substitute for the type parameter \'@Nullable E extends @Nullable T\'
			----------
			"""
	);
}
public void testBug484471SubclassNonNull() {
	runNegativeTestWithLibs(
		new String[] {
			"test/TestInterdepSubClassNN.java",
			"""
				package test;
				
				import org.eclipse.jdt.annotation.NonNull;
				
				@java.lang.annotation.Target({ java.lang.annotation.ElementType.TYPE_USE })
				@interface SomeAnnotation {
					// just needed as workaround if bug 484981 is not fixed
				}
				
				public class TestInterdepSubClassNN {
					static class A1<T, @SomeAnnotation E extends T> {
					}
				
					static class A2<T, @NonNull E extends T> {
					}
				
					static class A3<@NonNull T, @SomeAnnotation E extends T> {
					}
				
					static class A4<@NonNull T, @NonNull E extends T> {
					}
				
					// -------- subclasses of A1<T, E extends T> --------
				
					static class B11<T11, @SomeAnnotation E11 extends T11> extends A1<T11, E11> {
					}
				
					static class B12<T12, @NonNull E12 extends T12> extends A1<T12, E12> {
					}
				
					static class B13<@NonNull T13, @SomeAnnotation E13 extends T13> extends A1<T13, E13> {
					}
				
					static class B14<@NonNull T14, @NonNull E14 extends T14> extends A1<T14, E14> {
					}
				
					// -------- subclasses of A2<T, @NonNull E extends T> --------
				
					static class B21<T21, @SomeAnnotation E21 extends T21> extends A2<T21, E21> { // expect error 1
					}
				
					static class B22<T22, @NonNull E22 extends T22> extends A2<T22, E22> {
					}
				
					static class B23<@NonNull T23, @SomeAnnotation E23 extends T23> extends A2<T23, E23> {
					}
				
					static class B24<@NonNull T24, @NonNull E24 extends T24> extends A2<T24, E24> {
					}
				
					// -------- subclasses of A3<@NonNull T, E extends T> --------
				
					static class B31<T31, @SomeAnnotation E31 extends T31> extends A3<T31, E31> { // expect errors 2 & 3
					}
				
					static class B32<T32, @NonNull E32 extends T32> extends A3<T32, E32> { // expect error 4
					}
				
					static class B33<@NonNull T33, @SomeAnnotation E33 extends T33> extends A3<T33, E33> {
					}
				
					static class B34<@NonNull T34, @NonNull E34 extends T34> extends A3<T34, E34> {
					}
				
					// -------- subclasses of A4<@NonNull T, @NonNull E extends T> --------
				
					static class B41<T41, @SomeAnnotation E41 extends T41> extends A4<T41, E41> { // expect error 5 & 6
					}
				
					static class B42<T42, @NonNull E42 extends T42> extends A4<T42, E42> { // expect error 7
					}
				
					static class B43<@NonNull T43, @SomeAnnotation E43 extends T43> extends A4<T43, E43> {
					}
				
					static class B44<@NonNull T44, @NonNull E44 extends T44> extends A4<T44, E44> {
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\TestInterdepSubClassNN.java (at line 39)
				static class B21<T21, @SomeAnnotation E21 extends T21> extends A2<T21, E21> { // expect error 1
				                                                                       ^^^
			Null constraint mismatch: The type \'E21 extends T21\' is not a valid substitute for the type parameter \'@NonNull E extends T\'
			----------
			2. ERROR in test\\TestInterdepSubClassNN.java (at line 53)
				static class B31<T31, @SomeAnnotation E31 extends T31> extends A3<T31, E31> { // expect errors 2 & 3
				                                                                  ^^^
			Null constraint mismatch: The type \'T31\' is not a valid substitute for the type parameter \'@NonNull T\'
			----------
			3. ERROR in test\\TestInterdepSubClassNN.java (at line 53)
				static class B31<T31, @SomeAnnotation E31 extends T31> extends A3<T31, E31> { // expect errors 2 & 3
				                                                                       ^^^
			Null constraint mismatch: The type \'E31 extends T31\' is not a valid substitute for the type parameter \'E extends @NonNull T\'
			----------
			4. ERROR in test\\TestInterdepSubClassNN.java (at line 56)
				static class B32<T32, @NonNull E32 extends T32> extends A3<T32, E32> { // expect error 4
				                                                           ^^^
			Null constraint mismatch: The type \'T32\' is not a valid substitute for the type parameter \'@NonNull T\'
			----------
			5. ERROR in test\\TestInterdepSubClassNN.java (at line 67)
				static class B41<T41, @SomeAnnotation E41 extends T41> extends A4<T41, E41> { // expect error 5 & 6
				                                                                  ^^^
			Null constraint mismatch: The type \'T41\' is not a valid substitute for the type parameter \'@NonNull T\'
			----------
			6. ERROR in test\\TestInterdepSubClassNN.java (at line 67)
				static class B41<T41, @SomeAnnotation E41 extends T41> extends A4<T41, E41> { // expect error 5 & 6
				                                                                       ^^^
			Null constraint mismatch: The type \'E41 extends T41\' is not a valid substitute for the type parameter \'@NonNull E extends @NonNull T\'
			----------
			7. ERROR in test\\TestInterdepSubClassNN.java (at line 70)
				static class B42<T42, @NonNull E42 extends T42> extends A4<T42, E42> { // expect error 7
				                                                           ^^^
			Null constraint mismatch: The type \'T42\' is not a valid substitute for the type parameter \'@NonNull T\'
			----------
			"""
	);
}
public void testBug485058() {
	runNegativeTestWithLibs(
		new String[] {
			"test/Test4.java",
			"""
				package test;
				
				import java.io.Serializable;
				import org.eclipse.jdt.annotation.NonNullByDefault;
				import org.eclipse.jdt.annotation.NonNull;
				import org.eclipse.jdt.annotation.Nullable;
				
				@NonNullByDefault
				class Feature4<Q extends Serializable> {
						Q q() {
							throw new RuntimeException();
						}
				}
				
				@NonNullByDefault
				public class Test4 {
					public static <Q1 extends java.io.Serializable, F extends Feature4<Q1>> Q1[] getValues(F feature) {
						throw new RuntimeException();
					}
				
					public static void f(Feature4<?> feature) {
						getValues(feature);
					}
				
					public static void g(Feature4<@Nullable ? extends @NonNull Serializable> feature) {
						getValues(feature);
					}
				}"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\Test4.java (at line 25)
				public static void g(Feature4<@Nullable ? extends @NonNull Serializable> feature) {
				                              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'@Nullable ? extends @NonNull Serializable\' is not a valid substitute for the type parameter \'Q extends @NonNull Serializable\'
			----------
			2. WARNING in test\\Test4.java (at line 25)
				public static void g(Feature4<@Nullable ? extends @NonNull Serializable> feature) {
				                                                  ^^^^^^^^^^^^^^^^^^^^^
			The nullness annotation is redundant with a default that applies to this location
			----------
			2. ERROR in test\\Test4.java (at line 25)
				public static void g(Feature4<@Nullable ? extends @NonNull Serializable> feature) {
				                                                  ^^^^^^^^
			This nullness annotation conflicts with a \'@Nullable\' annotation which is effective on the same type parameter\s
			----------
			3. ERROR in test\\Test4.java (at line 26)
				getValues(feature);
				^^^^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'@NonNull Feature4<@Nullable capture#of ? extends @NonNull Serializable>\' is not a valid substitute for the type parameter \'F extends @NonNull Feature4<Q1 extends @NonNull Serializable>\'
			----------
			"""
	);
}
public void testBug485030() {
	runConformTestWithLibs(new String[] {
			"SomeAnnotation.java",
			"""
				import static java.lang.annotation.ElementType.TYPE_USE;
				import java.lang.annotation.Target;
				@Target({ TYPE_USE })
				@interface SomeAnnotation {
				}
				""",

			"TestContradictoryOnGenericArray.java",
			"""
				import java.io.Serializable;
				import org.eclipse.jdt.annotation.NonNullByDefault;
				import org.eclipse.jdt.annotation.Nullable;
				@NonNullByDefault
				public class TestContradictoryOnGenericArray {
					public <@SomeAnnotation Q extends Serializable> void f() {
						final @Nullable Q[] array = null;
					}
				}
				"""
	}, getCompilerOptions(), "");
}
public void testBug485302() {
	runNegativeTestWithLibs(
		new String[] {
		"WildCardNullable.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"public class WildCardNullable {\n" +
			"	static class A<T> {\n" +
			"		@Nullable\n" +
			"		T returnNull() {\n" +
			"			return null;\n" +
			"		}\n" +
			"\n" +
			"		void acceptNonNullT(@NonNull T t) {\n" +
			"		}\n" +
			"\n" +
			"		void acceptNonNullObject(@NonNull Object x) {\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	static @NonNull Number g(A<? extends @NonNull Number> a) {\n" +
			"		return a.returnNull(); // error 1 expected\n" +
			"	}\n" +
			"\n" +
			"	public static final <T> void map(final A<@NonNull ? super T> a, T t) {\n" +
			"		a.acceptNonNullT(t); // warning 2 expected\n" +
			"		a.acceptNonNullObject(t); // warning 3 expected\n" +
			"	}\n" +
			"}\n" +
			""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in WildCardNullable.java (at line 21)
				return a.returnNull(); // error 1 expected
				       ^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull Number\' but this expression has type \'@Nullable capture#of ? extends Number\'
			----------
			2. ERROR in WildCardNullable.java (at line 25)
				a.acceptNonNullT(t); // warning 2 expected
				                 ^
			Null type safety: required '@NonNull' but this expression has type 'T', a free type variable that may represent a '@Nullable' type
			----------
			3. ERROR in WildCardNullable.java (at line 26)
				a.acceptNonNullObject(t); // warning 3 expected
				                      ^
			Null type safety: required '@NonNull' but this expression has type 'T', a free type variable that may represent a '@Nullable' type
			----------
			"""
	);
}

public void testBug485027() {
	runConformTestWithLibs(new String[] {
			"SomeAnnotation.java",
			"""
				import static java.lang.annotation.ElementType.TYPE_USE;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;
				@Retention(RetentionPolicy.CLASS)
				@Target({ TYPE_USE })
				@interface SomeAnnotation {
				}
				""",

			"Base.java",
			"""
				import java.io.Serializable;
				import org.eclipse.jdt.annotation.NonNullByDefault;
				import org.eclipse.jdt.annotation.Nullable;
				@NonNullByDefault
				public class Base {
					public <@SomeAnnotation Q extends Serializable> void setValuesArray(Q @Nullable [] value) {
					}
				}
				"""
	}, getCompilerOptions(), "");

	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"Derived.java",
			"""
				import java.io.Serializable;
				import org.eclipse.jdt.annotation.NonNullByDefault;
				import org.eclipse.jdt.annotation.Nullable;
				@NonNullByDefault
				public class Derived extends Base {
					@Override
					public final <@SomeAnnotation Q1 extends Serializable> void setValuesArray(Q1 @Nullable [] value) {
					}
				}"""
	}, getCompilerOptions(), "");
}
public void testBug485565() {
	runConformTestWithLibs(
			new String[] {
			"test2/ClassWithRegistry.java",
			"""
				package test2;
				
				import java.rmi.registry.Registry;
				
				import org.eclipse.jdt.annotation.Nullable;
				
				public class ClassWithRegistry {
				    @Nullable
				    public Registry registry;
				}
				"""
			},
			getCompilerOptions(),
			""
		);
		runConformTestWithLibs(
			false /* don't flush */,
			new String[] {
				"test1/ClassWithLambda.java",
				"""
					package test1;
					
					import test2.ClassWithRegistry;
					
					// must be compiled before ZClassWithBug
					public class ClassWithLambda {
						interface Lambda {
							void f();
						}
					
						public static void invoke(Lambda lambda) {
							lambda.f();
						}
					
						public void f() {
							new ClassWithRegistry(); // must be accessed as class file
							invoke(() -> java.rmi.registry.Registry.class.hashCode());
						}
					}
					""",
				"test1/ZClassWithBug.java",
				"""
					package test1;
					
					import java.rmi.registry.Registry;
					
					import org.eclipse.jdt.annotation.NonNullByDefault;
					import org.eclipse.jdt.annotation.Nullable;
					
					@NonNullByDefault
					public abstract class ZClassWithBug {
					
						@Nullable
						public Registry rmiregistry;
					}
					"""
			},
			getCompilerOptions(),
			""
	);
}
public void testBug485814() {
	runConformTestWithLibs(
		new String[] {
			"test/ExplainedResult.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class ExplainedResult<V2> extends Result<V2> {\n" +
			"\n" +
			"	public ExplainedResult(int score, V2 extractedValue2) {\n" +
			"		super(score, extractedValue2);\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	public <OtherV2> ExplainedResult<OtherV2> withValue(OtherV2 otherValue2) {\n" +
			"		return new ExplainedResult<OtherV2>(this.score, otherValue2);\n" +
			"	}\n" +
			"}\n" +
			"",
			"test/Result.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Result<V1> {\n" +
			"\n" +
			"	public final int score;\n" +
			"\n" +
			"	public final V1 extractedValue;\n" +
			"\n" +
			"	public Result(int score, V1 extractedValue1) {\n" +
			"		this.score = score;\n" +
			"		this.extractedValue = extractedValue1;\n" +
			"	}\n" +
			"\n" +
			"	public <OtherV1> Result<OtherV1> withValue(OtherV1 otherValue1) {\n" +
			"		return new Result<OtherV1>(score, otherValue1);\n" +
			"	}\n" +
			"}\n" +
			""
		},
		getCompilerOptions(),
		""
	);
}
public void testBug485581() {
	runConformTestWithLibs(
		new String[] {
		"test/MatchResult.java",
			"""
				package test;
				
				import org.eclipse.jdt.annotation.NonNullByDefault;
				
				@NonNullByDefault
				public class MatchResult<V> implements Comparable<MatchResult<?>> {
					public final int score;
					public final V value;
				
					public MatchResult(int score, V value) {
						this.score = score;
						this.value = value;
					}
				
					@Override
					public int compareTo(MatchResult<?> o) {
						return score - o.score;
					}
				}
				"""
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"test/FVEHandler.java",
			"""
				package test;
				
				import org.eclipse.jdt.annotation.NonNullByDefault;
				
				@NonNullByDefault
				public class FVEHandler {
					public static void process(MatchResult<?> matchResult) {
						if (matchResult.value != null) {
						}
					}
				}
				""",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug482752_lambda() {
	runConformTestWithLibs(
		new String[] {
			"test/StringProcessor.java",
			"package test;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public interface StringProcessor {\n" +
			"	void process(String value);\n" +
			"\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
			false /* don't flush */,
			new String[] {
				"test/Foo.java",
				"package test;\n" +
				"\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"\n" +
				"public final class Foo {\n" +
				"\n" +
				"	public static StringProcessor createProcessorLambdaExpression() {\n" +
				"		return (@NonNull String value) -> Foo.test(value);\n" +
				"	}\n" +
				"\n" +
				"	public static void test(@NonNull String value) {\n" +
				"		System.out.println(value);\n" +
				"	}\n" +
				"}\n" +
				"",
			},
			getCompilerOptions(),
			""
		);
}

public void testBug482752_methodref() {
	runConformTestWithLibs(
		new String[] {
			"test/StringProcessor.java",
			"package test;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public interface StringProcessor {\n" +
			"	void process(String value);\n" +
			"\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
			false /* don't flush */,
			new String[] {
				"test/Foo.java",
				"package test;\n" +
				"\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"\n" +
				"public final class Foo {\n" +
				"\n" +
				"	public static StringProcessor createProcessorMethodReference() {\n" +
				"		return Foo::test;\n" +
				"	}\n" +
				"\n" +
				"	public static void test(@NonNull String value) {\n" +
				"		System.out.println(value);\n" +
				"	}\n" +
				"}\n" +
				"",
			},
			getCompilerOptions(),
			""
		);
}

public void testBug485374() {
	runConformTestWithLibs(
		new String[] {
			"test/I.java",
			"package test;\n" +
			"public interface I<W> {\n" +
			"    public class Nested {\n" +
			"    }\n" +
			"}\n" +
			"",
			"test/D.java",
			"package test;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"public class D implements I<I.@NonNull Nested> {\n" +
			"}\n" +
			""
		},
		getCompilerOptions(),
		""
	);
	runWarningTestWithLibs(
			false/*flush*/,
			new String[] {
			"test2/Import.java",
				"""
					package test2;
					import test.D;
					class Import {}
					"""
			},
			getCompilerOptions(),
			"""
				----------
				1. WARNING in test2\\Import.java (at line 2)
					import test.D;
					       ^^^^^^
				The import test.D is never used
				----------
				"""
		);
}

public void testBug466556a() {
	runNegativeTestWithLibs(
		new String[] {
			"test/C.java",
			"""
				package test;
				class C<T extends Number> {
				    int consume(T t) {
				        return t.intValue(); // NOT OK since T could be nullable
				    }
				    T provide() {
				        return null;         // NOT OK since T could require nonnull
				    }
				}
				""",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\C.java (at line 4)
				return t.intValue(); // NOT OK since T could be nullable
				       ^
			Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			2. ERROR in test\\C.java (at line 7)
				return null;         // NOT OK since T could require nonnull
				       ^^^^
			Null type mismatch (type annotations): \'null\' is not compatible to the free type variable \'T\'
			----------
			"""
	);
}
public void testBug466556nonfree() {
	runNegativeTestWithLibs(
		new String[] {
			"test/C.java",
			"""
				package test;
				class C<T extends @org.eclipse.jdt.annotation.NonNull Number> {
				    int consume(T t) {
				        return t.intValue(); // OK since T has upper bound with @NonNull
				    }
				    T provide() {
				        return null;         // NOT OK since T could require nonnull
				    }
				}
				""",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\C.java (at line 7)
				return null;         // NOT OK since T could require nonnull
				       ^^^^
			Null type mismatch: required \'T extends @NonNull Number\' but the provided value is null
			----------
			"""
	);
}
public void testBug466556b() {
	runNegativeTestWithLibs(
		new String[] {
			"test/C.java",
			"""
				package test;
				
				import java.util.function.Supplier;
				
				class C<T> {
					int consume(T t) {
						return t.hashCode();
					}
					void consume2(Supplier<T> s) {
						s.get().hashCode();
					}
				}
				""",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\C.java (at line 7)
				return t.hashCode();
				       ^
			Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			2. ERROR in test\\C.java (at line 10)
				s.get().hashCode();
				^^^^^^^
			Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			"""
	);
}
public void testBug466556c() {
	runNegativeTestWithLibs(
		new String[] {
			"test/C.java",
			"""
				package test;
				
				import java.util.function.Supplier;
				
				class C<T extends Number> {
					int consume(T t) {
						Number n = t;
						return n.intValue();
					}
				
					int consume2(Supplier<T> s) {
						Number n = s.get();
						return n.intValue();
					}
				}
				""",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\C.java (at line 8)
				return n.intValue();
				       ^
			Potential null pointer access: The variable n may be null at this location
			----------
			2. ERROR in test\\C.java (at line 13)
				return n.intValue();
				       ^
			Potential null pointer access: The variable n may be null at this location
			----------
			"""
	);
}
public void testBug466556field() {
	runNegativeTestWithLibs(
		new String[] {
			"test/D.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"class E<T> {\n" +
			"	T t;\n" +
			"}\n" +
			"\n" +
			"class D<T> {\n" +
			"	enum X {\n" +
			"		x\n" +
			"	};\n" +
			"\n" +
			"	T t1;\n" +
			"	T t2;\n" +
			"	T t3;\n" +
			"	@Nullable\n" +
			"	T t4;\n" +
			"	@NonNull\n" +
			"	T t5;\n" +
			"	@NonNull\n" +
			"	T t6;\n" +
			"	@NonNull\n" +
			"	T t7;\n" +
			"\n" +
			"	D(@NonNull T t) {\n" +
			"		t2 = t;\n" +
			"		switch (X.x) {\n" +
			"		case x:\n" +
			"			t1 = t;\n" +
			"			t5 = t;\n" +
			"		}\n" +
			"		t6 = t;\n" +
			"	}\n" +
			"\n" +
			"	void f() {\n" +
			"		t1.hashCode();\n" +
			"		t2.hashCode();\n" +
			"		t3.hashCode();\n" +
			"		t4.hashCode();\n" +
			"		t5.hashCode();\n" +
			"		t6.hashCode();\n" +
			"		t7.hashCode();\n" +
			"		T t = t1;\n" +
			"		t.hashCode();\n" +
			"	}\n" +
			"	void g() {\n" +
			"		if(t1 != null)\n" +
			"			t1.hashCode();\n // problem report expected because syntactic null analysis for fields is off\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\D.java (at line 7)
				T t;
				  ^
			The field t may not have been initialized, whereas its type \'T\' is a free type variable that may represent a \'@NonNull\' type
			----------
			2. ERROR in test\\D.java (at line 27)
				D(@NonNull T t) {
				^^^^^^^^^^^^^^^
			The field t1 may not have been initialized, whereas its type \'T\' is a free type variable that may represent a \'@NonNull\' type. Note that a problem regarding missing \'default:\' on \'switch\' has been suppressed, which is perhaps related to this problem
			----------
			3. ERROR in test\\D.java (at line 27)
				D(@NonNull T t) {
				^^^^^^^^^^^^^^^
			The field t3 may not have been initialized, whereas its type \'T\' is a free type variable that may represent a \'@NonNull\' type. Note that a problem regarding missing \'default:\' on \'switch\' has been suppressed, which is perhaps related to this problem
			----------
			4. ERROR in test\\D.java (at line 27)
				D(@NonNull T t) {
				^^^^^^^^^^^^^^^
			The @NonNull field t5 may not have been initialized. Note that a problem regarding missing \'default:\' on \'switch\' has been suppressed, which is perhaps related to this problem
			----------
			5. ERROR in test\\D.java (at line 27)
				D(@NonNull T t) {
				^^^^^^^^^^^^^^^
			The @NonNull field t7 may not have been initialized. Note that a problem regarding missing \'default:\' on \'switch\' has been suppressed, which is perhaps related to this problem
			----------
			6. ERROR in test\\D.java (at line 38)
				t1.hashCode();
				^^
			Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			7. ERROR in test\\D.java (at line 39)
				t2.hashCode();
				^^
			Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			8. ERROR in test\\D.java (at line 40)
				t3.hashCode();
				^^
			Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			9. ERROR in test\\D.java (at line 41)
				t4.hashCode();
				^^
			Potential null pointer access: this expression has a \'@Nullable\' type
			----------
			10. ERROR in test\\D.java (at line 46)
				t.hashCode();
				^
			Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			11. ERROR in test\\D.java (at line 50)
				t1.hashCode();
				^^
			Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			"""
	);
}
public void testBug466556withRaw() {
	runConformTestWithLibs(
		new String[] {
			"test/TestWithRaw.java",
			"package test;\n" +
			"\n" +
			"public class TestWithRaw {\n" +
			"	@SuppressWarnings({ \"unchecked\", \"rawtypes\" })\n" +
			"	public static void uncheckedEnumValueOf(final Class<?> valueClass, final String value) {\n" +
			"		Class valueClass2 = valueClass;\n" +
			"		Enum.valueOf(valueClass2, value).name();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug466556withPGMB() {
	runNegativeTestWithLibs(
		new String[] {
			"test/TestWithParameterizedGenericMethodBinding.java",
			"package test;\n" +
			"\n" +
			"public class TestWithParameterizedGenericMethodBinding {\n" +
			"	static <T, E extends T> T f1(E e) {\n" +
			"		return e;\n" +
			"	}\n" +
			"\n" +
			"	static <T11, E11 extends T11> void g11(E11 e) {\n" +
			"		f1(e).hashCode();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\TestWithParameterizedGenericMethodBinding.java (at line 9)
				f1(e).hashCode();
				^^^^^
			Potential null pointer access: this expression has type \'E11\', a free type variable that may represent a \'@Nullable\' type
			----------
			"""
	);
}
public void testBug466556captures() {
	runNegativeTestWithLibs(
		new String[] {
			"test/TestCapture.java",
			"package test;\n" +
			"\n" +
			"class I {\n" +
			"	int i;\n" +
			"\n" +
			"	String s() {\n" +
			"		return \"\";\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class KE<E extends I> {\n" +
			"	public final E e;\n" +
			"\n" +
			"	public E getE() {\n" +
			"		return e;\n" +
			"	}\n" +
			"\n" +
			"	public KE(E element) {\n" +
			"		this.e = element;\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class TestFreeTypeVariable<E2 extends I> {\n" +
			"	public void test(KE<E2> ke) {\n" +
			"		int i1 = ke.e.i; // error 1\n" +
			"		ke.e.s().substring(i1); // error 2\n" +
			"		int i2 = ke.getE().i; // error 3\n" +
			"		ke.getE().s().substring(i2); // error 4\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"public class TestCapture {\n" +
			"	public void test(KE<? extends I> ke) {\n" +
			"		int i1 = ke.e.i; // error 5\n" +
			"		ke.e.s().substring(i1); // error 6\n" +
			"		int i2 = ke.getE().i; // error 7\n" +
			"		ke.getE().s().substring(i2); // error 8\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\TestCapture.java (at line 25)
				int i1 = ke.e.i; // error 1
				            ^
			Potential null pointer access: this expression has type \'E2\', a free type variable that may represent a \'@Nullable\' type
			----------
			2. ERROR in test\\TestCapture.java (at line 26)
				ke.e.s().substring(i1); // error 2
				   ^
			Potential null pointer access: this expression has type \'E2\', a free type variable that may represent a \'@Nullable\' type
			----------
			3. ERROR in test\\TestCapture.java (at line 27)
				int i2 = ke.getE().i; // error 3
				         ^^^^^^^^^
			Potential null pointer access: this expression has type \'E2\', a free type variable that may represent a \'@Nullable\' type
			----------
			4. ERROR in test\\TestCapture.java (at line 28)
				ke.getE().s().substring(i2); // error 4
				^^^^^^^^^
			Potential null pointer access: this expression has type \'E2\', a free type variable that may represent a \'@Nullable\' type
			----------
			5. ERROR in test\\TestCapture.java (at line 34)
				int i1 = ke.e.i; // error 5
				            ^
			Potential null pointer access: this expression has type \'capture#1-of ? extends test.I\', a free type variable that may represent a \'@Nullable\' type
			----------
			6. ERROR in test\\TestCapture.java (at line 35)
				ke.e.s().substring(i1); // error 6
				   ^
			Potential null pointer access: this expression has type \'capture#2-of ? extends test.I\', a free type variable that may represent a \'@Nullable\' type
			----------
			7. ERROR in test\\TestCapture.java (at line 36)
				int i2 = ke.getE().i; // error 7
				         ^^^^^^^^^
			Potential null pointer access: this expression has type \'capture#3-of ? extends test.I\', a free type variable that may represent a \'@Nullable\' type
			----------
			8. ERROR in test\\TestCapture.java (at line 37)
				ke.getE().s().substring(i2); // error 8
				^^^^^^^^^
			Potential null pointer access: this expression has type \'capture#4-of ? extends test.I\', a free type variable that may represent a \'@Nullable\' type
			----------
			"""
	);
}
public void testBug466556Loops() {
	runNegativeTestWithLibs(
		new String[] {
			"test/TestLoop.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"public class TestLoop<T> {\n" +
			"	boolean b;\n" +
			"\n" +
			"	public static void nn(@NonNull Object value) {\n" +
			"		assert value != null;\n" +
			"	}\n" +
			"\n" +
			"	public void testDoWhile(T t1) {\n" +
			"		nn(t1); // 1: unchecked warning\n" +
			"		do {\n" +
			"			nn(t1); // 2: unchecked warning\n" +
			"			t1.hashCode(); // 3: Potential null pointer access...free type variable\n" +
			"		} while (b);\n" +
			"	}\n" +
			"\n" +
			"	public void testWhileWithBreak(T t1) {\n" +
			"		while (true) {\n" +
			"			nn(t1); // 4: unchecked warning\n" +
			"			t1.hashCode(); // 5: Potential null pointer access...free type variable\n" +
			"			if (b)\n" +
			"				break;\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public void testWhile(T t1) {\n" +
			"		while (TestLoop.class.hashCode() == 4711) {\n" +
			"			nn(t1); // 6: unchecked warning\n" +
			"			t1.hashCode(); // 7: Potential null pointer access...free type variable\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public void testFor(T t1) {\n" +
			"		for (int i = 0; i < 1; i++) {\n" +
			"			nn(t1); // 8: unchecked warning\n" +
			"			t1.hashCode(); // 9: Potential null pointer access...free type variable\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public void testForEach(T t1) {\n" +
			"		for (int i = 0; i < 1; i++) {\n" +
			"			nn(t1); // 10: unchecked warning\n" +
			"			t1.hashCode(); // 11: Potential null pointer access: The variable t1 may be null at this location\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\TestLoop.java (at line 13)
				nn(t1); // 1: unchecked warning
				   ^^
			Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			2. ERROR in test\\TestLoop.java (at line 15)
				nn(t1); // 2: unchecked warning
				   ^^
			Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			3. ERROR in test\\TestLoop.java (at line 16)
				t1.hashCode(); // 3: Potential null pointer access...free type variable
				^^
			Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			4. ERROR in test\\TestLoop.java (at line 22)
				nn(t1); // 4: unchecked warning
				   ^^
			Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			5. ERROR in test\\TestLoop.java (at line 23)
				t1.hashCode(); // 5: Potential null pointer access...free type variable
				^^
			Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			6. ERROR in test\\TestLoop.java (at line 31)
				nn(t1); // 6: unchecked warning
				   ^^
			Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			7. ERROR in test\\TestLoop.java (at line 32)
				t1.hashCode(); // 7: Potential null pointer access...free type variable
				^^
			Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			8. ERROR in test\\TestLoop.java (at line 38)
				nn(t1); // 8: unchecked warning
				   ^^
			Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			9. ERROR in test\\TestLoop.java (at line 39)
				t1.hashCode(); // 9: Potential null pointer access...free type variable
				^^
			Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			10. ERROR in test\\TestLoop.java (at line 45)
				nn(t1); // 10: unchecked warning
				   ^^
			Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			11. ERROR in test\\TestLoop.java (at line 46)
				t1.hashCode(); // 11: Potential null pointer access: The variable t1 may be null at this location
				^^
			Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			"""
	);
}
public void testBug461268() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportNonNullTypeVariableFromLegacyInvocation, JavaCore.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_PessimisticNullAnalysisForFreeTypeVariables, JavaCore.IGNORE);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				import org.eclipse.jdt.annotation.*;
				public class X {
					void test(List<@NonNull String> list) {
						@NonNull String s = list.get(0);
					}
				}
				"""
		},
		compilerOptions,
		"""
			----------
			1. ERROR in X.java (at line 5)
				@NonNull String s = list.get(0);
				                    ^^^^^^^^^^^
			Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'List<@NonNull String>\'. Type \'List<E>\' doesn\'t seem to be designed with null type annotations in mind
			----------
			""");
}
public void testBug461268invoke() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportNonNullTypeVariableFromLegacyInvocation, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import java.util.Map;
				import org.eclipse.jdt.annotation.*;
				public class X {
					void test(Map<Object, @NonNull String> map) {
						map.get(this).length();
					}
				}
				"""
		},
		compilerOptions,
		"""
			----------
			1. ERROR in X.java (at line 5)
				map.get(this).length();
				^^^^^^^^^^^^^
			Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'Map<Object,@NonNull String>\'. Type \'Map<K,V>\' doesn\'t seem to be designed with null type annotations in mind
			----------
			""");
}
public void testBug461268nnbd() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportNonNullTypeVariableFromLegacyInvocation, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
			"test2/Container.java",
			"""
				package test2;
				
				import org.eclipse.jdt.annotation.NonNullByDefault;
				
				@NonNullByDefault
				public class Container<T> {
					public static <T> T getFrom(Container<T> container) {
						return container.get();
					}
				
					private final T t;
				
					public Container(T t) {
						this.t = t;
					}
				
					private T get() {
						return this.t;
					}
				}
				""",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"import test2.Container;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Test {\n" +
			"	String f(Container<String> c) {\n" +
			"		return Container.getFrom(c);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug485988WildcardOverride() {
	runConformTestWithLibs(
		new String[] {
			"test/Result.java",
			"""
				package test;
				
				public class Result<V> implements Comparable<Result<?>> {
					public final int score;
					public final V value;
				
					protected Result(int score, V value) {
						this.score = score;
						this.value = value;
					}
					@Override
					public int compareTo(Result<?> o) {
						return score - o.score;
					}
				}
				""",
			"test/Base.java",
			"""
				package test;
				
				import org.eclipse.jdt.annotation.NonNullByDefault;
				
				@NonNullByDefault
				public abstract class Base {
					public abstract Result<?> matches();
				}
				""",
			"test/Derived.java",
			"""
				package test;
				
				import java.math.BigDecimal;
				
				import org.eclipse.jdt.annotation.NonNullByDefault;
				
				@NonNullByDefault
				public class Derived extends Base {
					@Override
					public Result<BigDecimal> matches() {
						return new Result<BigDecimal>(0, new BigDecimal("1"));
					}
				}
				""",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug485988neutral() {
	runConformTestWithLibs(
		new String[] {
			"neutral/WildcardTest.java",
			"package neutral;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"class A<T> {\n" +
			"}\n" +
			"\n" +
			"abstract class X {\n" +
			"	abstract A<?> g1();\n" +
			"\n" +
			"	abstract A<?> g2();\n" +
			"\n" +
			"	abstract A<?> g2b();\n" +
			"\n" +
			"	abstract A<?> g3();\n" +
			"\n" +
			"	abstract A<?> h1();\n" +
			"\n" +
			"	abstract A<?> h2();\n" +
			"\n" +
			"	abstract A<?> h2b();\n" +
			"\n" +
			"	abstract A<?> h3();\n" +
			"}\n" +
			"\n" +
			"class Y extends X {\n" +
			"	@Override\n" +
			"	A<?> g1() {\n" +
			"		return new A<@NonNull String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<@NonNull ?> g2() {\n" +
			"		return new A<@NonNull String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<? extends @NonNull Number> g2b() {\n" +
			"		return new A<@NonNull Integer>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<@NonNull String> g3() {\n" +
			"		return new A<@NonNull String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<?> h1() {\n" +
			"		return new A<@Nullable String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<@Nullable ?> h2() {\n" +
			"		return new A<@Nullable String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<? super @Nullable Number> h2b() {\n" +
			"		return new A<@Nullable Object>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<@Nullable String> h3() {\n" +
			"		return new A<@Nullable String>();\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class WildcardTest {\n" +
			"	void f(A<?> a) {\n" +
			"	}\n" +
			"\n" +
			"	<T1> void g(A<T1> a) {\n" +
			"	}\n" +
			"\n" +
			"	<T2> void invoke(T2 t) {\n" +
			"		f(new A<T2>());\n" +
			"		g(new A<T2>());\n" +
			"\n" +
			"		f(new A<@NonNull T2>());\n" +
			"		g(new A<@NonNull T2>());\n" +
			"\n" +
			"		f(new A<@Nullable T2>());\n" +
			"		g(new A<@Nullable T2>());\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug485988nonnull() {
	runNegativeTestWithLibs(
		new String[] {
			"nonnull/WildcardNonNullTest.java",
			"package nonnull;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"class A<@NonNull T> {\n" +
			"}\n" +
			"\n" +
			"abstract class X {\n" +
			"	abstract A<@NonNull ?> g1();\n" +
			"\n" +
			"	abstract A<@NonNull ?> g2();\n" +
			"\n" +
			"	abstract A<@NonNull ?> g2b();\n" +
			"\n" +
			"	abstract A<@NonNull ?> g3();\n" +
			"\n" +
			"	abstract A<@NonNull ?> h1();\n" +
			"\n" +
			"	abstract A<@NonNull ?> h2();\n" +
			"\n" +
			"	abstract A<@NonNull ?> h2b();\n" +
			"\n" +
			"	abstract A<@NonNull ?> h3();\n" +
			"}\n" +
			"\n" +
			"class Y extends X {\n" +
			"	@Override\n" +
			"	A<?> g1() {\n" +
			"		return new A<@NonNull String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<@NonNull ?> g2() {\n" +
			"		return new A<@NonNull String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<? extends @NonNull Number> g2b() {\n" +
			"		return new A<@NonNull Integer>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<@NonNull String> g3() {\n" +
			"		return new A<@NonNull String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<?> h1() {\n" +
			"		return new A<@Nullable String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<@Nullable ?> h2() {\n" +
			"		return new A<@Nullable String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<? super @Nullable String> h2b() {\n" +
			"		return new A<@Nullable String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<@Nullable String> h3() {\n" +
			"		return new A<@Nullable String>();\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class WildcardNonNullTest {\n" +
			"	void f(A<?> a) {\n" +
			"	}\n" +
			"\n" +
			"	<@NonNull T1> void g(A<T1> a) {\n" +
			"	}\n" +
			"\n" +
			"	<T2> void invoke(T2 t) {\n" +
			"		f(new A<T2>());\n" +
			"		g(new A<T2>());\n" +
			"\n" +
			"		f(new A<@NonNull T2>());\n" +
			"		g(new A<@NonNull T2>());\n" +
			"\n" +
			"		f(new A<@Nullable T2>());\n" +
			"		g(new A<@Nullable T2>());\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in nonnull\\WildcardNonNullTest.java (at line 52)
				return new A<@Nullable String>();
				             ^^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'@Nullable String\' is not a valid substitute for the type parameter \'@NonNull T\'
			----------
			2. ERROR in nonnull\\WildcardNonNullTest.java (at line 56)
				A<@Nullable ?> h2() {
				^
			The return type is incompatible with \'A<@NonNull ?>\' returned from X.h2() (mismatching null constraints)
			----------
			3. ERROR in nonnull\\WildcardNonNullTest.java (at line 56)
				A<@Nullable ?> h2() {
				  ^^^^^^^^^^^
			Null constraint mismatch: The type \'@Nullable ?\' is not a valid substitute for the type parameter \'@NonNull T\'
			----------
			4. ERROR in nonnull\\WildcardNonNullTest.java (at line 57)
				return new A<@Nullable String>();
				             ^^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'@Nullable String\' is not a valid substitute for the type parameter \'@NonNull T\'
			----------
			5. ERROR in nonnull\\WildcardNonNullTest.java (at line 61)
				A<? super @Nullable String> h2b() {
				^
			The return type is incompatible with \'A<@NonNull ?>\' returned from X.h2b() (mismatching null constraints)
			----------
			6. ERROR in nonnull\\WildcardNonNullTest.java (at line 61)
				A<? super @Nullable String> h2b() {
				  ^^^^^^^^^^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'? super @Nullable String\' is not a valid substitute for the type parameter \'@NonNull T\'
			----------
			7. ERROR in nonnull\\WildcardNonNullTest.java (at line 62)
				return new A<@Nullable String>();
				             ^^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'@Nullable String\' is not a valid substitute for the type parameter \'@NonNull T\'
			----------
			8. ERROR in nonnull\\WildcardNonNullTest.java (at line 66)
				A<@Nullable String> h3() {
				^
			The return type is incompatible with \'A<@NonNull ?>\' returned from X.h3() (mismatching null constraints)
			----------
			9. ERROR in nonnull\\WildcardNonNullTest.java (at line 66)
				A<@Nullable String> h3() {
				  ^^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'@Nullable String\' is not a valid substitute for the type parameter \'@NonNull T\'
			----------
			10. ERROR in nonnull\\WildcardNonNullTest.java (at line 67)
				return new A<@Nullable String>();
				             ^^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'@Nullable String\' is not a valid substitute for the type parameter \'@NonNull T\'
			----------
			11. ERROR in nonnull\\WildcardNonNullTest.java (at line 80)
				f(new A<T2>());
				        ^^
			Null constraint mismatch: The type \'T2\' is not a valid substitute for the type parameter \'@NonNull T\'
			----------
			12. WARNING in nonnull\\WildcardNonNullTest.java (at line 81)
				g(new A<T2>());
				  ^^^^^^^^^^^
			Null type safety (type annotations): The expression of type \'@NonNull A<T2>\' needs unchecked conversion to conform to \'@NonNull A<@NonNull T2>\'
			----------
			13. ERROR in nonnull\\WildcardNonNullTest.java (at line 81)
				g(new A<T2>());
				        ^^
			Null constraint mismatch: The type \'T2\' is not a valid substitute for the type parameter \'@NonNull T\'
			----------
			14. ERROR in nonnull\\WildcardNonNullTest.java (at line 86)
				f(new A<@Nullable T2>());
				        ^^^^^^^^^^^^
			Null constraint mismatch: The type \'@Nullable T2\' is not a valid substitute for the type parameter \'@NonNull T\'
			----------
			15. ERROR in nonnull\\WildcardNonNullTest.java (at line 87)
				g(new A<@Nullable T2>());
				  ^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull A<@NonNull T2>\' but this expression has type \'@NonNull A<@Nullable T2>\'
			----------
			16. ERROR in nonnull\\WildcardNonNullTest.java (at line 87)
				g(new A<@Nullable T2>());
				        ^^^^^^^^^^^^
			Null constraint mismatch: The type \'@Nullable T2\' is not a valid substitute for the type parameter \'@NonNull T\'
			----------
			"""
	);
}
public void testBug485988nullable() {
	runNegativeTestWithLibs(
		new String[] {
			"nullable/WildcardNullableTest.java",
			"package nullable;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"class A<@Nullable T> {\n" +
			"}\n" +
			"\n" +
			"abstract class X {\n" +
			"	abstract A<@Nullable ?> g1();\n" +
			"\n" +
			"	abstract A<@Nullable ?> g2();\n" +
			"\n" +
			"	abstract A<@Nullable ?> g2b();\n" +
			"\n" +
			"	abstract A<@Nullable ?> g3();\n" +
			"\n" +
			"	abstract A<@Nullable ?> h1();\n" +
			"\n" +
			"	abstract A<@Nullable ?> h2();\n" +
			"\n" +
			"	abstract A<@Nullable ?> h2b();\n" +
			"\n" +
			"	abstract A<@Nullable ?> h3();\n" +
			"}\n" +
			"\n" +
			"class Y extends X {\n" +
			"	@Override\n" +
			"	A<?> g1() {\n" +
			"		return new A<@NonNull String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<@NonNull ?> g2() {\n" +
			"		return new A<@NonNull String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<? extends @NonNull Number> g2b() {\n" +
			"		return new A<@NonNull Integer>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<@NonNull String> g3() {\n" +
			"		return new A<@NonNull String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<?> h1() {\n" +
			"		return new A<@Nullable String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<@Nullable ?> h2() {\n" +
			"		return new A<@Nullable String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<? super @Nullable String> h2b() {\n" +
			"		return new A<@Nullable String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<@Nullable String> h3() {\n" +
			"		return new A<@Nullable String>();\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class WildcardNullableTest {\n" +
			"	void f(A<?> a) {\n" +
			"	}\n" +
			"\n" +
			"	<@Nullable T1> void g(A<T1> a) {\n" +
			"	}\n" +
			"\n" +
			"	<T2> void invoke(T2 t) {\n" +
			"		f(new A<T2>());\n" +
			"		g(new A<T2>());\n" +
			"\n" +
			"		f(new A<@NonNull T2>());\n" +
			"		g(new A<@NonNull T2>());\n" +
			"\n" +
			"		f(new A<@Nullable T2>());\n" +
			"		g(new A<@Nullable T2>());\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in nullable\\WildcardNullableTest.java (at line 32)
				return new A<@NonNull String>();
				             ^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'@NonNull String\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			2. ERROR in nullable\\WildcardNullableTest.java (at line 36)
				A<@NonNull ?> g2() {
				^
			The return type is incompatible with \'A<@Nullable ?>\' returned from X.g2() (mismatching null constraints)
			----------
			3. ERROR in nullable\\WildcardNullableTest.java (at line 36)
				A<@NonNull ?> g2() {
				  ^^^^^^^^^^
			Null constraint mismatch: The type \'@NonNull ?\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			4. ERROR in nullable\\WildcardNullableTest.java (at line 37)
				return new A<@NonNull String>();
				             ^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'@NonNull String\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			5. ERROR in nullable\\WildcardNullableTest.java (at line 41)
				A<? extends @NonNull Number> g2b() {
				^
			The return type is incompatible with \'A<@Nullable ?>\' returned from X.g2b() (mismatching null constraints)
			----------
			6. ERROR in nullable\\WildcardNullableTest.java (at line 41)
				A<? extends @NonNull Number> g2b() {
				  ^^^^^^^^^^^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'? extends @NonNull Number\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			7. ERROR in nullable\\WildcardNullableTest.java (at line 42)
				return new A<@NonNull Integer>();
				             ^^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'@NonNull Integer\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			8. ERROR in nullable\\WildcardNullableTest.java (at line 46)
				A<@NonNull String> g3() {
				^
			The return type is incompatible with \'A<@Nullable ?>\' returned from X.g3() (mismatching null constraints)
			----------
			9. ERROR in nullable\\WildcardNullableTest.java (at line 46)
				A<@NonNull String> g3() {
				  ^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'@NonNull String\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			10. ERROR in nullable\\WildcardNullableTest.java (at line 47)
				return new A<@NonNull String>();
				             ^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'@NonNull String\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			11. ERROR in nullable\\WildcardNullableTest.java (at line 80)
				f(new A<T2>());
				        ^^
			Null constraint mismatch: The type \'T2\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			12. WARNING in nullable\\WildcardNullableTest.java (at line 81)
				g(new A<T2>());
				  ^^^^^^^^^^^
			Null type safety (type annotations): The expression of type \'@NonNull A<T2>\' needs unchecked conversion to conform to \'@NonNull A<@Nullable T2>\'
			----------
			13. ERROR in nullable\\WildcardNullableTest.java (at line 81)
				g(new A<T2>());
				        ^^
			Null constraint mismatch: The type \'T2\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			14. ERROR in nullable\\WildcardNullableTest.java (at line 83)
				f(new A<@NonNull T2>());
				        ^^^^^^^^^^^
			Null constraint mismatch: The type \'@NonNull T2\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			15. ERROR in nullable\\WildcardNullableTest.java (at line 84)
				g(new A<@NonNull T2>());
				  ^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull A<@Nullable T2>\' but this expression has type \'@NonNull A<@NonNull T2>\'
			----------
			16. ERROR in nullable\\WildcardNullableTest.java (at line 84)
				g(new A<@NonNull T2>());
				        ^^^^^^^^^^^
			Null constraint mismatch: The type \'@NonNull T2\' is not a valid substitute for the type parameter \'@Nullable T\'
			----------
			"""
	);
}
public void testBug485988WildCardForTVWithNonNullBound() {
	runConformTestWithLibs(
		new String[] {
			"test/WildCard.java",
			"package test;\n" +
			"\n" +
			"import java.io.Serializable;\n" +
			"import java.util.ArrayList;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"class F<T extends Serializable> {\n" +
			"}\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class WildCard {\n" +
			"	void f(ArrayList<F<?>> list) {\n" +
			"		for (F<? extends Serializable> f : list) {\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug485988WildcardWithGenericBound() {
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"test/Test1.java",
			"""
				package test;
				import java.util.Collection;
				import java.util.Iterator;
				
				import org.eclipse.jdt.annotation.NonNull;
				interface LibA {
					<T> Iterator<? extends T> constrainedWildcards(Collection<? extends T> in);
				}
				public class Test1 {
					Iterator<? extends @NonNull String> test3(LibA lib, Collection<String> coll) {
						return lib.constrainedWildcards(coll);
					}
				}
				
				""",
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in test\\Test1.java (at line 11)
				return lib.constrainedWildcards(coll);
				                                ^^^^
			Null type safety (type annotations): The expression of type \'Collection<String>\' needs unchecked conversion to conform to \'Collection<? extends @NonNull String>\'
			----------
			"""
	);
}
public void testBug485988Contradictory() {
	runNegativeTestWithLibs(
		new String[] {
			"test/Test1.java",
			"""
				package test;
				
				import org.eclipse.jdt.annotation.*;
				interface A<T> {
				}
				public class Test1{
					void f1(A<@Nullable @NonNull ?> a) {
					}
					void f2(A<@NonNull @Nullable ?> a) {
					}
					void f3(A<@Nullable ? extends @NonNull Object> a) {
					}
					void f4(A<@NonNull ? super @Nullable Integer> a) {
					}
					void f5(A<@Nullable ? super @Nullable Integer> a) {
					}
					@NonNullByDefault void f6(A<@Nullable ? extends Integer> a) {
					}
				}
				
				""",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\Test1.java (at line 7)
				void f1(A<@Nullable @NonNull ?> a) {
				                    ^^^^^^^^
			Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location
			----------
			2. ERROR in test\\Test1.java (at line 9)
				void f2(A<@NonNull @Nullable ?> a) {
				                   ^^^^^^^^^
			Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location
			----------
			3. ERROR in test\\Test1.java (at line 11)
				void f3(A<@Nullable ? extends @NonNull Object> a) {
				                              ^^^^^^^^
			This nullness annotation conflicts with a \'@Nullable\' annotation which is effective on the same type parameter\s
			----------
			4. ERROR in test\\Test1.java (at line 13)
				void f4(A<@NonNull ? super @Nullable Integer> a) {
				                           ^^^^^^^^^
			This nullness annotation conflicts with a \'@NonNull\' annotation which is effective on the same type parameter\s
			----------
			"""
	);
}
public void testBug485988bound() {
	runConformTestWithLibs(
		new String[] {
			"C.java",
			"""
				import org.eclipse.jdt.annotation.NonNull;
				
				interface I<T> { }
				
				public class C {
					I<@NonNull ?> m1(I<? extends @NonNull C> i) {
						return i;
					}
					I<? extends @NonNull C> m2(I<@NonNull ? extends C> i) {
						return i;
					}
				\t
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug466585_comment_0() {
	runConformTestWithLibs(
		new String[] {
			"C3.java",
			"""
				import org.eclipse.jdt.annotation.*;
				class C3<T extends @NonNull Number> {
				    C3<?> x; // Null constraint mismatch: The type '?' is not a valid substitute for the type parameter 'T extends @NonNull Number'
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug466585_comment_4() {
	runNegativeTestWithLibs(
		new String[] {
			"C3.java",
			"""
				import org.eclipse.jdt.annotation.*;
				
				class C4<T extends @NonNull Number> {
				  C4<@Nullable ?> err1;
				  C4<@Nullable ? extends Integer> err2;
				  C4<? super @Nullable Integer> err3;
				  C4<@Nullable ? super Integer> err4;
				  C4<@NonNull ? super Integer> ok1;
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in C3.java (at line 4)
				C4<@Nullable ?> err1;
				   ^^^^^^^^^^^
			Null constraint mismatch: The type \'@Nullable ?\' is not a valid substitute for the type parameter \'T extends @NonNull Number\'
			----------
			2. ERROR in C3.java (at line 5)
				C4<@Nullable ? extends Integer> err2;
				   ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'@Nullable ? extends Integer\' is not a valid substitute for the type parameter \'T extends @NonNull Number\'
			----------
			3. ERROR in C3.java (at line 6)
				C4<? super @Nullable Integer> err3;
				   ^^^^^^^^^^^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'? super @Nullable Integer\' is not a valid substitute for the type parameter \'T extends @NonNull Number\'
			----------
			4. ERROR in C3.java (at line 7)
				C4<@Nullable ? super Integer> err4;
				   ^^^^^^^^^^^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'@Nullable ? super Integer\' is not a valid substitute for the type parameter \'T extends @NonNull Number\'
			----------
			""");
}
public void testBug489978() {
	runConformTestWithLibs(
		new String[] {
			"test/BinaryClass.java",
			"package test;\n" +
			"\n" +
			"import java.util.ArrayList;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class BinaryClass {\n" +
			"	public ArrayList<Object> list;\n" +
			"\n" +
			"	public BinaryClass(ArrayList<Object> list) {\n" +
			"		this.list = list;\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"test/Usage.java",
			"package test;\n" +
			"\n" +
			"import java.util.ArrayList;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Usage {\n" +
			"	ArrayList<Object> f(BinaryClass b) {\n" +
			"		return b.list;\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug489245() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_PessimisticNullAnalysisForFreeTypeVariables, JavaCore.INFO);
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"test/TestBogusProblemReportOnlyAsInfo.java",
			"package test;\n" +
			"\n" +
			"import java.util.function.Supplier;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class TestBogusProblemReportOnlyAsInfo {\n" +
			"	static <U> void get(Supplier<U> supplier, @NonNull U defaultValue) {\n" +
			"	}\n" +
			"\n" +
			"	static void f() {\n" +
			"		get(() -> {\n" +
			"			return null; // bogus problem report only as info\n" +
			"		}, \"\");\n" +
			"	}\n" +
			"\n" +
			"	static <T> void h(@NonNull T t) {\n" +
			"		get(() -> {\n" +
			"			return null; // correctly reported (but twice with the bug)\n" +
			"		}, t);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		compilerOptions,
		"""
			----------
			1. INFO in test\\TestBogusProblemReportOnlyAsInfo.java (at line 21)
				return null; // correctly reported (but twice with the bug)
				       ^^^^
			Null type mismatch (type annotations): \'null\' is not compatible to the free type variable \'T\'
			----------
			"""
	);
}
public void testBug489674() {
	Map options = new HashMap<>(getCompilerOptions());
	options.put(JavaCore.COMPILER_NONNULL_ANNOTATION_SECONDARY_NAMES, "org.foo.NonNull");
	options.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_SECONDARY_NAMES, "org.foo.Nullable");
	runConformTest(
		new String[] {
			"org/foo/Nullable.java",
			"""
				package org.foo;
				import java.lang.annotation.*;
				import static java.lang.annotation.ElementType.*;
				@Retention(RetentionPolicy.CLASS)
				@Target({ FIELD, METHOD, PARAMETER, LOCAL_VARIABLE })
				public @interface Nullable {}
				""",
			"org/foo/NonNull.java",
			"""
				package org.foo;
				import java.lang.annotation.*;
				import static java.lang.annotation.ElementType.*;
				@Retention(RetentionPolicy.CLASS)
				@Target({ FIELD, METHOD, PARAMETER, LOCAL_VARIABLE })
				public @interface NonNull {}
				""",
		},
		options);
	runConformTestWithLibs(
			false /* don't flush */,
			new String[] {
				"with_other_nullable/P1.java",
				"package with_other_nullable;\n" +
				"\n" +
				"import org.foo.Nullable;\n" +
				"\n" +
				"public class P1 {\n" +
				"	public static @Nullable String f0() {\n" +
				"		return null;\n" +
				"	}\n" +
				"\n" +
				"	public static <T> T check(T t) {\n" +
				"		return t;\n" +
				"	}\n" +
				"}\n" +
				"",
				"with_other_nullable/P2.java",
				"package with_other_nullable;\n" +
				"\n" +
				"import org.foo.NonNull;\n" +
				"\n" +
				"public class P2 {\n" +
				"	public static void f(@NonNull String s) {\n" +
				"	}\n" +
				"\n" +
				"	public static <T> T check(T t) {\n" +
				"		return t;\n" +
				"	}\n" +
				"}\n" +
				"",
			},
			options,
			""
	);
	runNegativeTestWithLibs(
			false /* don't flush */,
			new String[] {
				"test/Test4.java",
				"package test;\n" +
				"\n" +
				"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
				"\n" +
				"import with_other_nullable.P1;\n" +
				"import with_other_nullable.P2;\n" +
				"\n" +
				"@NonNullByDefault\n" +
				"public class Test4 {\n" +
				"	void m1(String s) {\n" +
				"		P1.f0().hashCode();\n" +
				"		s = P1.check(s);\n" +
				"	}\n" +
				"	void m2(String s) {\n" +
				"		P2.f(null);\n" +
				"		s = P2.check(s);\n" +
				"	}\n" +
				"}\n" +
				"",
			},
			options,
			"""
				----------
				1. ERROR in test\\Test4.java (at line 11)
					P1.f0().hashCode();
					^^^^^^^
				Potential null pointer access: The method f0() may return null
				----------
				2. ERROR in test\\Test4.java (at line 15)
					P2.f(null);
					     ^^^^
				Null type mismatch: required \'@NonNull String\' but the provided value is null
				----------
				""",
			false
		);
}
public void testBug492327() {
	runConformTestWithLibs(
		new String[] {
			"WatchEvent.java",
			"""
				public interface WatchEvent<T> {
					public static interface Modifier {
					}
				}
				""",
			"Watchable.java",
			"""
				public interface Watchable {
					void register(WatchEvent.Modifier[] modifiers);
				}
				""",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"Path.java",
			"""
				public interface Path extends Watchable {
				  @Override
				  void register(WatchEvent.Modifier[] modifiers);
				}
				""",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug488495collector() {
	runConformTestWithLibs(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"interface Collector<A, R> {\n" +
			"}\n" +
			"\n" +
			"interface Stream {\n" +
			"    <A1, R1> R1 collect(Collector<A1, R1> collector);\n" +
			"}\n" +
			"\n" +
			"interface List<E> {\n" +
			"}\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Test {\n" +
			"    public static <T> Collector<?, List<T>> toList() {\n" +
			"        return new Collector<Object, List<T>>(){};\n" +
			"    }\n" +
			"\n" +
			"    public static List<String> myMethod(Stream stream) {\n" +
			"        List<String> list = stream.collect(toList());\n" +
			"        return list;\n" +
			"    }\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}

public void testBug496591() {
	runConformTestWithLibs(
		new String[] {
			"test2/Descriptors.java",
			"package test2;\n" +
			"\n" +
			"public final class Descriptors {\n" +
			"	public static final class FieldDescriptor implements FieldSet.FieldDescriptorLite<FieldDescriptor> { }\n" +
			"}\n" +
			"",
			"test2/FieldSet.java",
			"package test2;\n" +
			"\n" +
			"public final class FieldSet<F1 extends FieldSet.FieldDescriptorLite<F1>> {\n" +
			"	public interface FieldDescriptorLite<F2 extends FieldDescriptorLite<F2>> { }\n" +
			"\n" +
			"	void f(final Map.Entry<F1> entry) { }\n" +
			"}\n" +
			"",
			"test2/Map.java",
			"package test2;\n" +
			"\n" +
			"public class Map<K> {\n" +
			"	interface Entry<K1> { }\n" +
			"}\n" +
			"",
			"test2/MessageOrBuilder.java",
			"package test2;\n" +
			"\n" +
			"public interface MessageOrBuilder {\n" +
			"	Map<Descriptors.FieldDescriptor> getAllFields();\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"test1/GeneratedMessage.java",
			"package test1;\n" +
			"\n" +
			"import test2.Descriptors.FieldDescriptor;\n" +
			"import test2.Map;\n" +
			"import test2.MessageOrBuilder;\n" +
			"\n" +
			"public abstract class GeneratedMessage implements MessageOrBuilder {\n" +
			"	@Override\n" +
			"	public abstract Map<FieldDescriptor> getAllFields();\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug497698() {
	runNegativeTest(
		new String[] {
			"test/And.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class And {\n" +
			"	public static void createAnd() {\n" +
			"		Or.create();\n" +
			"	}\n" +
			"}\n" +
			"",
			"test/Or.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Or<D, V> {\n" +
			"	public static <V> Or<V> create() {\n" +
			"		return new Or<V, V>();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		"""
			----------
			1. ERROR in test\\Or.java (at line 7)
				public static <V> Or<V> create() {
				                  ^^
			Incorrect number of arguments for type Or<D,V>; it cannot be parameterized with arguments <V>
			----------
			""",
		this.LIBS,
		true/*flush*/
	);
}
public void testBug497698raw() {
	runNegativeTest(
		new String[] {
			"test/And.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class And {\n" +
			"	public static void createAnd() {\n" +
			"		new Or().create();\n" +
			"	}\n" +
			"}\n" +
			"",
			"test/Or.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Or<D, V> {\n" +
			"	public <V1> Or<V1> create() {\n" +
			"		return new Or<V1, V1>();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		"""
			----------
			1. WARNING in test\\And.java (at line 8)
				new Or().create();
				    ^^
			Or is a raw type. References to generic type Or<D,V> should be parameterized
			----------
			----------
			1. ERROR in test\\Or.java (at line 7)
				public <V1> Or<V1> create() {
				            ^^
			Incorrect number of arguments for type Or<D,V>; it cannot be parameterized with arguments <V1>
			----------
			""",
		this.LIBS,
		false/*shouldFlush*/
	);
}
public void testBug497698nestedinraw() {
	runNegativeTest(
		new String[] {
			"test/And.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class And {\n" +
			"	public static void createAnd(X.Or x) {\n" +
			"		x.create();\n" +
			"	}\n" +
			"}\n" +
			"",
			"test/X.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class X<Z> {\n" +
			"	public class Or<D, V> {\n" +
			"		public <V1> Or<V1> create() {\n" +
			"			return new Or<V1,V1>();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		"""
			----------
			1. WARNING in test\\And.java (at line 7)
				public static void createAnd(X.Or x) {
				                             ^^^^
			X.Or is a raw type. References to generic type X<Z>.Or<D,V> should be parameterized
			----------
			----------
			1. ERROR in test\\X.java (at line 8)
				public <V1> Or<V1> create() {
				            ^^
			Incorrect number of arguments for type X<Z>.Or<D,V>; it cannot be parameterized with arguments <V1>
			----------
			""",
		this.LIBS,
		true/*flush*/
	);
}
public void testBug492322() {
	runConformTestWithLibs(
		new String[] {
			"test1/Base.java",
			"package test1;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"public abstract class Base {\n" +
			"  public class GenericInner<T> {\n" +
			"  }\n" +
			"\n" +
			"  @NonNullByDefault(DefaultLocation.PARAMETER)\n" +
			"  public Object method(@Nullable GenericInner<Object> nullable) {\n" +
			"    return new Object();\n" +
			"  }\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"test2/Derived.java",
			"package test2;\n" +
			"\n" +
			"import test1.Base;\n" +
			"\n" +
			"class Derived extends Base {\n" +
			"  void test() {\n" +
			"    method(null);\n" +
			"  }\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug492322field() {
	runConformTestWithLibs(
		new String[] {
			"test1/Base.java",
			"package test1;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public abstract class Base {\n" +
			"  public class GenericInner<T> {\n" +
			"  }\n" +
			"\n" +
			"  protected @Nullable GenericInner<Object> field;\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
			false /* don't flush */,
			new String[] {
				"test2/Derived.java",
				"package test2;\n" +
				"\n" +
				"import test1.Base;\n" +
				"\n" +
				"class Derived extends Base {\n" +
				"  void test() {\n" +
				"    field = null;\n" +
				"  }\n" +
				"}\n" +
				"",
			},
			getCompilerOptions(),
			""
		);
}
public void testBug492322deep() {
	runConformTestWithLibs(
		new String[] {
			"test1/Base.java",
			"package test1;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"public abstract class Base {\n" +
			"  public static class Static {\n" +
			"   public class Middle1 {\n" +
			"     public class Middle2<M> {\n" +
			"       public class Middle3 {\n" +
			"        public class GenericInner<T> {\n" +
			"        }\n" +
			"       }\n" +
			"     }\n" +
			"   }\n" +
			"  }\n" +
			"\n" +
			"  @NonNullByDefault(DefaultLocation.PARAMETER)\n" +
			"  public Object method( Static.Middle1.Middle2<Object>.Middle3.@Nullable GenericInner<String> nullable) {\n" +
			"    return new Object();\n" +
			"  }\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"test2/Derived.java",
			"package test2;\n" +
			"\n" +
			"import test1.Base;\n" +
			"\n" +
			"class Derived extends Base {\n" +
			"  void test() {\n" +
			"    method(null);\n" +
			"  }\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug492322withGenericBase() {
	runConformTestWithLibs(
		new String[] {
			"test1/Base.java",
			"package test1;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"public abstract class Base<B> {\n" +
			"   static public class Static {\n" +
			"    public class Middle1 {\n" +
			"     public class Middle2<M> {\n" +
			"       public class Middle3 {\n" +
			"        public class GenericInner<T> {\n" +
			"        }\n" +
			"       }\n" +
			"     }\n" +
			"   }\n" +
			"  }\n" +
			"\n" +
			"  @NonNullByDefault(DefaultLocation.PARAMETER)\n" +
			"  public Object method( Static.Middle1.Middle2<Object>.Middle3.@Nullable GenericInner<String> nullable) {\n" +
			"    return new Object();\n" +
			"  }\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"test2/Derived.java",
			"package test2;\n" +
			"\n" +
			"import test1.Base;\n" +
			"\n" +
			"class Derived extends Base<Number> {\n" +
			"  void test() {\n" +
			"    method(null);\n" +
			"  }\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug499862a() {
	runConformTestWithLibs(
		new String[] {
			"Test.java",
			"""
				import org.eclipse.jdt.annotation.*;
				import java.util.*;
				public class Test {
					static void printChecked(Collection<? extends @Nullable String> collection) {
						for(String s : collection)
							if (s != null)
								System.out.println(s.toString());
							else
								System.out.println("NULL");
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug499862b() {
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"""
				import org.eclipse.jdt.annotation.*;
				import java.util.*;
				public class Test {
					static void printChecked(Collection<? extends @Nullable String> collection) {
						for(String s : collection)
							System.out.println(s.toString());
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in Test.java (at line 6)
				System.out.println(s.toString());
				                   ^
			Potential null pointer access: The variable s may be null at this location
			----------
			""");
}
public void testBug499862c() {
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"""
				import java.util.*;
				public class Test {
					static <T> void printUnchecked(Collection<T> collection) {
						for(T t : collection)
							System.out.println(t.toString());
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in Test.java (at line 5)
				System.out.println(t.toString());
				                   ^
			Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			""");
}
public void testBug499597simplified() {
	runConformTestWithLibs(
		new String[] {
			"Foo2.java",
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"class Foo2 {\n" +
			"	static <T> T of(T t) {\n" +
			"		return t;\n" +
			"	}\n" +
			"\n" +
			"	static String foo() {\n" +
			"		return Foo2.<String>of(\"\"); // <-- warning here\n" +
			"	}\n" +
			"\n" +
			"	static String bar() {\n" +
			"		return Foo2.<@NonNull String>of(\"\"); // <-- no warning\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in Foo2.java (at line 15)
				return Foo2.<@NonNull String>of(""); // <-- no warning
				             ^^^^^^^^^^^^^^^
			The nullness annotation is redundant with a default that applies to this location
			----------
			""",
		""
	);
}
public void testBug499597original() {
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"Foo.java",
			"import static org.eclipse.jdt.annotation.DefaultLocation.*;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"import java.util.Collection;\n" +
			"import java.util.Collections;\n" +
			"\n" +
			"class Foo {\n" +
			"	static @NonNull String @NonNull [] X = { \"A\" };\n" +
			"\n" +
			"	@NonNullByDefault({ PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS })\n" +
			"	@SafeVarargs\n" +
			"	static <T> Collection<T> of(@NonNull T @NonNull... elements) {\n" +
			"		return Collections.singleton(elements[0]);\n" +
			"	}\n" +
			"\n" +
			"	@NonNullByDefault({ PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS })\n" +
			"	static Collection<String[]> foo() {\n" +
			"		return Foo.<String[]>of(X); // <-- warning here\n" +
			"	}\n" +
			"\n" +
			"	@NonNullByDefault({ PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS })\n" +
			"	static Collection<String[]> bar() {\n" +
			"		return Foo.<String @NonNull []>of(X); // <-- no warning\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in Foo.java (at line 12)
				static <T> Collection<T> of(@NonNull T @NonNull... elements) {
				                            ^^^^^^^^^^^^^^^^^^^^^^
			The nullness annotation is redundant with a default that applies to this location
			----------
			2. WARNING in Foo.java (at line 12)
				static <T> Collection<T> of(@NonNull T @NonNull... elements) {
				                                       ^^^^^^^^^^^
			The nullness annotation is redundant with a default that applies to this location
			----------
			3. WARNING in Foo.java (at line 13)
				return Collections.singleton(elements[0]);
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type safety (type annotations): The expression of type \'Set<@NonNull T>\' needs unchecked conversion to conform to \'@NonNull Collection<@NonNull T>\', corresponding supertype is \'Collection<@NonNull T>\'
			----------
			4. WARNING in Foo.java (at line 23)
				return Foo.<String @NonNull []>of(X); // <-- no warning
				                   ^^^^^^^^^^^
			The nullness annotation is redundant with a default that applies to this location
			----------
			"""
	);
}
public void testBug501031() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault(DefaultLocation.TYPE_PARAMETER)\n" +
			"class X {\n" +
			"	<T> @NonNull Object identity(T t) {\n" +
			"		return t;\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}

public void testBug501031return() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault(DefaultLocation.TYPE_PARAMETER)\n" +
			"class X {\n" +
			"	<T> T identity() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 7)
				return null;
				       ^^^^
			Null type mismatch: required \'@NonNull T\' but the provided value is null
			----------
			"""
	);
}
public void testBug501031btb() {
	// this already worked without the patch for bug 501031.
	runConformTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
				"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
				"\n" +
				"@NonNullByDefault(DefaultLocation.TYPE_PARAMETER)\n" +
				"class X {\n" +
				"	<T> void identity(T t) {\n" +
				"	}\n" +
				"}\n" +
				"",
			},
			getCompilerOptions(),
			""
		);
	runNegativeTestWithLibs(
			new String[] {
				"Y.java",
				"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
				"import org.eclipse.jdt.annotation.Nullable;\n" +
				"\n" +
				"@NonNullByDefault\n" +
				"class Y {\n" +
				"	void test(X x, @Nullable String string) {\n" +
				"		x.identity(string);\n" +
				"	}\n" +
				"}\n" +
				"",
			},
			getCompilerOptions(),
			"""
				----------
				1. ERROR in Y.java (at line 7)
					x.identity(string);
					           ^^^^^^
				Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'
				----------
				"""
		);
}
public void testBug501449() {
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"public class Test {\n" +
			"	<T, S extends T> void f(T[] objects, @Nullable T nullableValue, T value, S subclassValue) {\n" +
			"		objects[0] = null;\n" +
			"		objects[1] = nullableValue;\n" +
			"		objects[2] = value;\n" +
			"		objects[3] = subclassValue;\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in Test.java (at line 5)
				objects[0] = null;
				             ^^^^
			Null type mismatch (type annotations): \'null\' is not compatible to the free type variable \'T\'
			----------
			2. ERROR in Test.java (at line 6)
				objects[1] = nullableValue;
				             ^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'T\' but this expression has type \'@Nullable T\', where \'T\' is a free type variable
			----------
			"""
	);
}
public void testBug502112() {
	runConformTest(
		new String[] {
			"org/foo/Nullable.java",
			"""
				package org.foo;
				import java.lang.annotation.*;
				@Retention(RetentionPolicy.CLASS)
				public @interface Nullable {}
				""",
		},
		getCompilerOptions());
	runConformTestWithLibs(
			false /* don't flush */,
			new String[] {
				"util/Optional.java",
				"package util;\n" +
				"\n" +
				"import org.foo.Nullable;\n" +
				"\n" +
				"public class Optional {\n" +
				"	public static <T> T fromNullable(@Nullable T nullableReference, @Nullable T nullableReference2) {\n" +
				"		return nullableReference;\n" +
				"	}\n" +
				"	@Nullable\n" +
				"	public static <T> T returnNull(T nullableReference) {\n" +
				"		return nullableReference;\n" +
				"	}\n" +
				"}\n" +
				"",
			},
			getCompilerOptions(),
			""
		);
	Map options = new HashMap<>(getCompilerOptions());
	options.put(JavaCore.COMPILER_NONNULL_ANNOTATION_SECONDARY_NAMES, "org.foo.NonNull");
	options.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_SECONDARY_NAMES, "org.foo.Nullable");
	runNegativeTestWithLibs(
	new String[] {
		"test/Test.java",
		"package test;\n" +
		"\n" +
		"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
		"import org.eclipse.jdt.annotation.Nullable;\n" +
		"\n" +
		"import util.Optional;\n" +
		"\n" +
		"@NonNullByDefault\n" +
		"public class Test {\n" +
		"	void f(@Nullable String s) {\n" +
		"		Optional.<String>fromNullable(s, null);\n" +
		"	}\n" +
		"	String g(@Nullable String s) {\n" +
		"		return Optional.<String>returnNull(s);\n" +
		"	}\n" +
		"}\n" +
		"",
	},
	options,
	"""
		----------
		1. ERROR in test\\Test.java (at line 14)
			return Optional.<String>returnNull(s);
			       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'
		----------
		2. ERROR in test\\Test.java (at line 14)
			return Optional.<String>returnNull(s);
			                                   ^
		Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'
		----------
		"""
);
}
public void testBug502112b() {
	runConformTest(
		new String[] {
		"org/foo/NonNull.java",
		"""
			package org.foo;
			import java.lang.annotation.*;
			@Retention(RetentionPolicy.CLASS)
			public @interface NonNull {}
			""",
		"org/foo/Nullable.java",
		"""
			package org.foo;
			import java.lang.annotation.*;
			@Retention(RetentionPolicy.CLASS)
			public @interface Nullable {}
			""",
		},
		getCompilerOptions());
	runConformTestWithLibs(
			false /* don't flush */,
			new String[] {
				"util/X.java",
				"package util;\n" +
				"\n" +
				"import org.foo.NonNull;\n" +
				"import org.foo.Nullable;\n" +
				"\n" +
				"public class X {\n" +
				"	@NonNull\n" +
				"	public <T> T nonNull(@Nullable T t, @Nullable T t2) {\n" +
				"		return java.util.Objects.requireNonNull(t);\n" +
				"	}\n" +
				"}\n" +
				"",
			},
			getCompilerOptions(),
			""
		);
	Map options = new HashMap<>(getCompilerOptions());
	options.put(JavaCore.COMPILER_NONNULL_ANNOTATION_SECONDARY_NAMES, "org.foo.NonNull");
	options.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_SECONDARY_NAMES, "org.foo.Nullable");
	runNegativeTestWithLibs(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"import util.X;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Test extends X{\n" +
			"	@Override\n" +
			"	public <T> @Nullable T nonNull(@NonNull T t, T t2) {\n" +
			"		return t;\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		options,
		"""
			----------
			1. ERROR in test\\Test.java (at line 12)
				public <T> @Nullable T nonNull(@NonNull T t, T t2) {
				           ^^^^^^^^^^^
			The return type is incompatible with \'@NonNull T extends Object\' returned from X.nonNull(T, T) (mismatching null constraints)
			----------
			2. ERROR in test\\Test.java (at line 12)
				public <T> @Nullable T nonNull(@NonNull T t, T t2) {
				                               ^^^^^^^^^^
			Illegal redefinition of parameter t, inherited method from X declares this parameter as @Nullable
			----------
			3. ERROR in test\\Test.java (at line 12)
				public <T> @Nullable T nonNull(@NonNull T t, T t2) {
				                                             ^
			Missing nullable annotation: inherited method from X specifies this parameter as @Nullable
			----------
			"""
	);
}
public void testBug484926locals() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_ANNOTATED_TYPE_ARGUMENT_TO_UNANNOTATED, JavaCore.IGNORE);
	runNegativeTestWithLibs(
		new String[] {
			"test/NNBDOnLocalOrField.java",
			"""
				package test;
				
				import org.eclipse.jdt.annotation.*;
				
				class AtomicReference<T> {
				
					public void set(T object) {
					}
				
				}
				
				@NonNullByDefault
				public class NNBDOnLocalOrField {
					void someMethod() {
						AtomicReference<String> x1 = new AtomicReference<>();
						AtomicReference<String> x2 = new AtomicReference<@NonNull String>(), x3=new AtomicReference<@Nullable String>();
						@NonNullByDefault({})
						AtomicReference<String> y1 = new AtomicReference<>();
						@NonNullByDefault({})
						AtomicReference<String> y2 = new AtomicReference<@NonNull String>(), y3=new AtomicReference<@Nullable String>();
						x1.set(null);
						x2.set(null);
						x3.set(null);
						y1.set(null);
						y2.set(null);
						y3.set(null);
					}
				}
				"""
		},
		options,
		"""
			----------
			1. WARNING in test\\NNBDOnLocalOrField.java (at line 16)
				AtomicReference<String> x2 = new AtomicReference<@NonNull String>(), x3=new AtomicReference<@Nullable String>();
				                                                 ^^^^^^^^^^^^^^^
			The nullness annotation is redundant with a default that applies to this location
			----------
			2. ERROR in test\\NNBDOnLocalOrField.java (at line 16)
				AtomicReference<String> x2 = new AtomicReference<@NonNull String>(), x3=new AtomicReference<@Nullable String>();
				                                                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'AtomicReference<@NonNull String>\' but this expression has type \'@NonNull AtomicReference<@Nullable String>\'
			----------
			3. ERROR in test\\NNBDOnLocalOrField.java (at line 21)
				x1.set(null);
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			4. ERROR in test\\NNBDOnLocalOrField.java (at line 22)
				x2.set(null);
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			5. ERROR in test\\NNBDOnLocalOrField.java (at line 23)
				x3.set(null);
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			"""
	);
}
public void testBug484926fields() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_ANNOTATED_TYPE_ARGUMENT_TO_UNANNOTATED, JavaCore.IGNORE);
	runNegativeTestWithLibs(
		new String[] {
			"test/NNBDOnLocalOrField.java",
			"""
				package test;
				
				import org.eclipse.jdt.annotation.*;
				
				class AtomicReference<T> {
				
					public void set(T object) {
					}
				
				}
				
				@NonNullByDefault
				public class NNBDOnLocalOrField {
					AtomicReference<String> x1 = new AtomicReference<>();
					AtomicReference<String> x2 = new AtomicReference<@NonNull String>(), x3=new AtomicReference<@Nullable String>();
					@NonNullByDefault({})
					AtomicReference<String> y1 = new AtomicReference<>();
					@NonNullByDefault({})
					AtomicReference<String> y2 = new AtomicReference<@NonNull String>(), y3=new AtomicReference<@Nullable String>();
					void someMethod() {
						x1.set(null);
						x2.set(null);
						x3.set(null);
						y1.set(null);
						y2.set(null);
						y3.set(null);
					}
				}
				"""
		},
		options,
		"""
			----------
			1. WARNING in test\\NNBDOnLocalOrField.java (at line 15)
				AtomicReference<String> x2 = new AtomicReference<@NonNull String>(), x3=new AtomicReference<@Nullable String>();
				                                                 ^^^^^^^^^^^^^^^
			The nullness annotation is redundant with a default that applies to this location
			----------
			2. ERROR in test\\NNBDOnLocalOrField.java (at line 15)
				AtomicReference<String> x2 = new AtomicReference<@NonNull String>(), x3=new AtomicReference<@Nullable String>();
				                                                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull AtomicReference<@NonNull String>\' but this expression has type \'@NonNull AtomicReference<@Nullable String>\'
			----------
			3. ERROR in test\\NNBDOnLocalOrField.java (at line 21)
				x1.set(null);
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			4. ERROR in test\\NNBDOnLocalOrField.java (at line 22)
				x2.set(null);
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			5. ERROR in test\\NNBDOnLocalOrField.java (at line 23)
				x3.set(null);
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			"""
	);
}
public void testBug484926() {
	runConformTestWithLibs(
		new String[] {
			"test/NNBDOnLocalOrField.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"class AtomicReference<T> {\n" +
			"\n" +
			"	public void set(T object) {\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class NNBDOnLocalOrField {\n" +
			"	@NonNullByDefault({})\n" +
			"	AtomicReference<String> f = new AtomicReference<>();\n" +
			"\n" +
			"	{\n" +
			"		f.set(null);\n" +
			"	}\n" +
			"\n" +
			"	@NonNullByDefault({})\n" +
			"	Runnable r = () -> {\n" +
			"		AtomicReference<String> x1 = new AtomicReference<>();\n" +
			"		x1.set(null);\n" +
			"	};\n" +
			"\n" +
			"	Object someMethod() {\n" +
			"		@NonNullByDefault({})\n" +
			"		AtomicReference<String> x2 = new AtomicReference<>();\n" +
			"		x2.set(null);\n" +
			"\n" +
			"		@NonNullByDefault({})\n" +
			"		Runnable r1 = () -> {\n" +
			"			AtomicReference<String> x3 = new AtomicReference<>();\n" +
			"			x3.set(null);\n" +
			"		};\n" +
			"		\n" +
			"		return r1;\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug484926nesting() {
	runNegativeTestWithLibs(
		new String[] {
			"test/NNBDOnLocalOrField.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"class AtomicReference<T> {\n" +
			"\n" +
			"	public void set(T object) {\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"\n" +
			"public class NNBDOnLocalOrField {\n" +
			"	@NonNullByDefault()\n" +
			"	Runnable r = () -> {\n" +
			"		@NonNullByDefault({})\n" +
			"		AtomicReference<String> x1 = new AtomicReference<>();\n" +
			"		x1.set(null);\n" +
			"	};\n" +
			"	@NonNullByDefault\n" +
			"	Object someMethod() {\n" +
			"		@NonNullByDefault({})\n" +
			"		AtomicReference<String> x2 = new AtomicReference<>();\n" +
			"		x2.set(null);\n" +
			"\n" +
			"		@NonNullByDefault({})\n" +
			"		Runnable r1 = () -> {\n" +
			"			@NonNullByDefault\n" +
			"			AtomicReference<String> x3 = new AtomicReference<>();\n" +
			"			x3.set(null);\n" +
			"		};\n" +
			"		\n" +
			"		return r1;\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\NNBDOnLocalOrField.java (at line 29)
				x3.set(null);
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			"""
	);
}
public void testBug484926localDeclarationInForLoop() {
	runConformTestWithLibs(
		new String[] {
			"test/NNBDOnLocalOrField.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"class AtomicReference<T> {\n" +
			"\n" +
			"	public void set(T object) {\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class NNBDOnLocalOrField {\n" +
			"	void someMethod() {\n" +
			"		for(@NonNullByDefault({})\n" +
			"		Runnable r1 = () -> {\n" +
			"			AtomicReference<String> x3 = new AtomicReference<>();\n" +
			"			x3.set(null);\n" +
			"		}, r2 = () -> {\n" +
			"			AtomicReference<String> x4 = new AtomicReference<>();\n" +
			"			x4.set(null);\n" +
			"		};;) {\n" +
			"			r1.run();\n" +
			"			r2.run();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug484926redundantNNBD() {
	runNegativeTestWithLibs(
		new String[] {
			"testnnbd/NNBDRedundantOnLocalOrField.java",
			"package testnnbd;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault({})\n" +
			"class AtomicReference<T> {\n" +
			"	public void set(T object) {\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"public class NNBDRedundantOnLocalOrField {\n" +
			"	@NonNullByDefault\n" +
			"	Runnable r1 = () -> {\n" +
			"		@NonNullByDefault\n" +
			"		AtomicReference<String> x3 = new AtomicReference<>();\n" +
			"		x3.set(null);\n" +
			"	}, r2 = () -> {\n" +
			"		@NonNullByDefault({})\n" +
			"		AtomicReference<String> x4 = new AtomicReference<String>() {\n" +
			"			@NonNullByDefault({})\n" +
			"			public void set(String object) {\n" +
			"			}\n" +
			"		};\n" +
			"		x4.set(null);\n" +
			"	};\n" +
			"\n" +
			"	@NonNullByDefault\n" +
			"	class X1 {\n" +
			"		@NonNullByDefault\n" +
			"		Runnable r = () -> {\n" +
			"			@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"			class Local extends AtomicReference<String> {\n" +
			"				@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"				class X2 {\n" +
			"					@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"					String s;\n" +
			"					\n" +
			"					{\n" +
			"						set(null);\n" +
			"					}\n" +
			"				}\n" +
			"				{\n" +
			"				new X2().hashCode();\n" +
			"				}\n" +
			"			}\n" +
			"			Local x1 = new Local();\n" +
			"			x1.set(null);\n" +
			"		};\n" +
			"	}\n" +
			"\n" +
			"	@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"	Object someMethod() {\n" +
			"		@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"		AtomicReference<String> x2 = new AtomicReference<>();\n" +
			"		x2.set(null);\n" +
			"\n" +
			"		@NonNullByDefault({})\n" +
			"		Runnable r = () -> {\n" +
			"			@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"			AtomicReference<String> x3 = new AtomicReference<>();\n" +
			"			x3.set(null);\n" +
			"		};\n" +
			"\n" +
			"		@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"		Runnable r2 = new Runnable() {\n" +
			"			@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"			@Override\n" +
			"			public void run() {\n" +
			"			}\n" +
			"		};\n" +
			"\n" +
			"		r2.run();\n" +
			"		return r;\n" +
			"	}\n" +
			"\n" +
			"	@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"	void forLoopVariable() {\n" +
			"		{\n" +
			"			@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"			Runnable r = () -> {\n" +
			"				AtomicReference<String> x3 = new AtomicReference<>();\n" +
			"				x3.set(null);\n" +
			"			}, r2 = () -> {\n" +
			"				AtomicReference<String> x4 = new AtomicReference<>();\n" +
			"				x4.set(null);\n" +
			"			};\n" +
			"			r.run();\n" +
			"			r2.run();\n" +
			"		}\n" +
			"		for (@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"		Runnable r = () -> {\n" +
			"			@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"			AtomicReference<String> x3 = new AtomicReference<>();\n" +
			"			x3.set(null);\n" +
			"		}, r2 = () -> {\n" +
			"			@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"			AtomicReference<String> x4 = new AtomicReference<>();\n" +
			"			x4.set(null);\n" +
			"		};;) {\n" +
			"			r.run();\n" +
			"			r2.run();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"",
			"testnnbd/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package testnnbd;\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 13)
				@NonNullByDefault
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing package testnnbd
			----------
			2. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 15)
				@NonNullByDefault
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the field r1
			----------
			3. ERROR in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 17)
				x3.set(null);
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			4. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 21)
				@NonNullByDefault({})
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the variable x4
			----------
			5. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 22)
				public void set(String object) {
				            ^^^^^^^^^^^^^^^^^^
			The method set(String) of type new AtomicReference<String>(){} should be tagged with @Override since it actually overrides a superclass method
			----------
			6. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 28)
				@NonNullByDefault
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing package testnnbd
			----------
			7. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 30)
				@NonNullByDefault
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing type NNBDRedundantOnLocalOrField.X1
			----------
			8. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 34)
				@NonNullByDefault(DefaultLocation.RETURN_TYPE)
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing type Local
			----------
			9. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 36)
				@NonNullByDefault(DefaultLocation.RETURN_TYPE)
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing type Local.X2
			----------
			10. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 37)
				String s;
				       ^
			The value of the field Local.X2.s is not used
			----------
			11. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 54)
				@NonNullByDefault(DefaultLocation.RETURN_TYPE)
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing method someMethod()
			----------
			12. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 65)
				@NonNullByDefault(DefaultLocation.RETURN_TYPE)
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing method someMethod()
			----------
			13. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 66)
				Runnable r2 = new Runnable() {
				         ^^
			The local variable r2 is hiding a field from type NNBDRedundantOnLocalOrField
			----------
			14. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 67)
				@NonNullByDefault(DefaultLocation.RETURN_TYPE)
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the variable r2
			----------
			15. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 80)
				@NonNullByDefault(DefaultLocation.RETURN_TYPE)
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing method forLoopVariable()
			----------
			16. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 84)
				}, r2 = () -> {
				   ^^
			The local variable r2 is hiding a field from type NNBDRedundantOnLocalOrField
			----------
			17. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 91)
				for (@NonNullByDefault(DefaultLocation.RETURN_TYPE)
				     ^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing method forLoopVariable()
			----------
			18. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 91)
				for (@NonNullByDefault(DefaultLocation.RETURN_TYPE)
				     ^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing method forLoopVariable()
			----------
			19. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 93)
				@NonNullByDefault(DefaultLocation.RETURN_TYPE)
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the variable r
			----------
			20. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 96)
				}, r2 = () -> {
				   ^^
			The local variable r2 is hiding a field from type NNBDRedundantOnLocalOrField
			----------
			21. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 97)
				@NonNullByDefault(DefaultLocation.RETURN_TYPE)
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the variable r2
			----------
			"""
	);
}
public void testBug484926BTB() {
	runConformTestWithLibs(
		new String[] {
			"test/ClassWithNNBDOnField.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"class AtomicReference<T> {\n" +
			"	public void set(T object) {\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"@NonNullByDefault()\n" +
			"public class ClassWithNNBDOnField {\n" +
			"	@NonNullByDefault({})\n" +
			"	AtomicReference<String> f = new AtomicReference<>();\n" +
			"	{\n" +
			"		f.set(null);\n" +
			"	}\n" +
			"\n" +
			"	public static class X {\n" +
			"		@NonNullByDefault({})\n" +
			"		AtomicReference<String> nested = new AtomicReference<>();\n" +
			"		{\n" +
			"			nested.set(null);\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public X x = new X();\n" +
			"	\n" +
			"	void test() {\n" +
			"		new ClassWithNNBDOnField().f.set(null);\n" +
			"		new ClassWithNNBDOnField().f = null;\n" +
			"		new ClassWithNNBDOnField().x.nested.set(null);\n" +
			"		new ClassWithNNBDOnField().x.nested = null;\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"public class Test {\n" +
			"	void test() {\n" +
			"		new ClassWithNNBDOnField().f.set(null);\n" +
			"		new ClassWithNNBDOnField().f = null;\n" +
			"		new ClassWithNNBDOnField().x.nested.set(null);\n" +
			"		new ClassWithNNBDOnField().x.nested = null;\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug500885() {
	runConformTest(
		new String[] {
			"annot/NonNull.java",
			"""
				package annot;
				@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
				public @interface NonNull {}
				""",
			"annot/NonNullByDefault.java",
			"""
				package annot;
				@annot.NonNull
				@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
				public @interface NonNullByDefault {}
				""",
			"annot/package-info.java",
			"@annot.NonNullByDefault package annot;\n",
			"test/package-info.java",
			"@annot.NonNullByDefault package test;\n",
			"test/X.java",
			"""
				package test;
				public interface X {
					public String get();
				}
				"""
		},
		getCompilerOptions(),
		"");
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_SECONDARY_NAMES, "annot.NonNullByDefault");
	options.put(JavaCore.COMPILER_NONNULL_ANNOTATION_SECONDARY_NAMES, "annot.NonNull");
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"test2/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault package test2;\n",
			"test2/Y.java",
			"""
				package test2;
				import test.X;
				public class Y implements X {
					public String get() {
						return "";
					}
				}
				"""
		},
		options,
		"");
}
public void testBug505671() {
	runConformTestWithLibs(
		new String[] {
			"snippet/Pair.java",
			"package snippet;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Pair {\n" +
			"	public static <S, T> S make(S left, T right, Object x) {\n" +
			"		throw new RuntimeException();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
	getCompilerOptions(),
	""
	);
	runNegativeTestWithLibs(
		new String[] {
			"snippet/Snippet.java",
			"package snippet;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"public class Snippet {\n" +
			"	public static final @NonNull Object FALSE = new Object();\n" +
			"\n" +
			"	public static @NonNull Object abbreviateExplained0() {\n" +
			"		return Pair.<String, @NonNull Object>make(null, FALSE, null);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in snippet\\Snippet.java (at line 9)
				return Pair.<String, @NonNull Object>make(null, FALSE, null);
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'@NonNull Object\'
			----------
			2. ERROR in snippet\\Snippet.java (at line 9)
				return Pair.<String, @NonNull Object>make(null, FALSE, null);
				                                                       ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			"""
	);
}
public void testBug501564() {
	runNegativeTestWithLibs(
		new String[] {
			"xxx/Foo.java",
			"package xxx;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"\n" +
			"class Generic<E1 extends Generic<E1>> { \n" +
			"}\n" +
			"class Foo { \n" +
			"    static <E2 extends Generic<E2>> Bar<E2> foo() {\n" +
			"        return new Bar<>();\n" +
			"    }\n" +
			"\n" +
			"    @NonNullByDefault(DefaultLocation.TYPE_PARAMETER)\n" +
			"    static class Bar<E3 extends Generic<E3>> { }\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in xxx\\Foo.java (at line 8)
				static <E2 extends Generic<E2>> Bar<E2> foo() {
				                                    ^^
			Null constraint mismatch: The type \'E2 extends Generic<E2>\' is not a valid substitute for the type parameter \'@NonNull E3 extends Generic<E3 extends Generic<E3>>\'
			----------
			"""
	);
}
public void testBug501564interface() {
	runNegativeTestWithLibs(
		new String[] {
			"xxx/Foo.java",
			"package xxx;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"\n" +
			"interface Generic<E1 extends Generic<E1>> { \n" +
			"}\n" +
			"class Foo { \n" +
			"    static <E2 extends Generic<E2>> Bar<E2> foo() {\n" +
			"        return new Bar<>();\n" +
			"    }\n" +
			"\n" +
			"    @NonNullByDefault(DefaultLocation.TYPE_PARAMETER)\n" +
			"    static class Bar<E3 extends Generic<E3>> { }\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in xxx\\Foo.java (at line 8)
				static <E2 extends Generic<E2>> Bar<E2> foo() {
				                                    ^^
			Null constraint mismatch: The type \'E2 extends Generic<E2>\' is not a valid substitute for the type parameter \'@NonNull E3 extends Generic<E3 extends Generic<E3>>\'
			----------
			"""
	);
}
public void testBug501464() {
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"Foo.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"interface MyList<T> { @NonNull T getAny(); }\n" +
			"\n" +
			"@NonNullByDefault({})\n" +
			"class Foo {\n" +
			"    @Nullable Object b;\n" +
			"    \n" +
			"    void foo() {\n" +
			"        @Nullable Object f = b;\n" +
			"        ((@NonNull Object)f).hashCode(); // Error (unexpected): Potential null pointer access: this expression has a '@Nullable' type\n" +
			"    }\n" +
			"    \n" +
			"    void workaround() {\n" +
			"        @Nullable Object f = b;\n" +
			"        @NonNull Object g = (@NonNull Object)f; // Warning (expected): Null type safety: Unchecked cast from @Nullable Object to @NonNull Object\n" +
			"        g.hashCode();\n" +
			"    }\n" +
			"	 String three(@NonNull MyList<@Nullable String> list) {\n" +
			"		return ((@NonNull MyList<@NonNull String>) list).getAny().toUpperCase();\n" +
			"	 }\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in Foo.java (at line 11)
				((@NonNull Object)f).hashCode(); // Error (unexpected): Potential null pointer access: this expression has a \'@Nullable\' type
				^^^^^^^^^^^^^^^^^^^^
			Null type safety: Unchecked cast from @Nullable Object to @NonNull Object
			----------
			2. WARNING in Foo.java (at line 16)
				@NonNull Object g = (@NonNull Object)f; // Warning (expected): Null type safety: Unchecked cast from @Nullable Object to @NonNull Object
				                    ^^^^^^^^^^^^^^^^^^
			Null type safety: Unchecked cast from @Nullable Object to @NonNull Object
			----------
			3. WARNING in Foo.java (at line 20)
				return ((@NonNull MyList<@NonNull String>) list).getAny().toUpperCase();
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type safety: Unchecked cast from @NonNull MyList<@Nullable String> to @NonNull MyList<@NonNull String>
			----------
			"""
	);
}
public void testBug507840() {
	runConformTestWithLibs(
		new String[] {
			"nnbd_on_typevar/AtomicReference.java",
			"package nnbd_on_typevar;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"class AtomicReference<T> {\n" +
			"	public void set(T t) {\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"nnbd_on_typevar/Usage.java",
			"package nnbd_on_typevar;\n" +
			"\n" +
			"public class Usage {\n" +
			"	void m(AtomicReference<String> ref) {\n" +
			"		ref.set(null);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug508497() {
	runConformTestWithLibs(
		new String[] {
			"Reference.java",
			"interface Fluent<SELF extends Fluent<SELF>> {\n" +
			"	SELF self();\n" +
			"}\n" +
			"abstract class Reference<T> {\n" +
			"	abstract T get();\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"B2.java",
			"class B2 {\n" +
			"	void b1(Fluent f) {\n" +
			"		f.self();\n" +
			"	}\n" +
			"\n" +
			"	void b2(Reference<@org.eclipse.jdt.annotation.NonNull Fluent> ref) {\n" +
			"		ref.get().self();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in B2.java (at line 2)
				void b1(Fluent f) {
				        ^^^^^^
			Fluent is a raw type. References to generic type Fluent<SELF> should be parameterized
			----------
			2. WARNING in B2.java (at line 6)
				void b2(Reference<@org.eclipse.jdt.annotation.NonNull Fluent> ref) {
				                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Fluent is a raw type. References to generic type Fluent<SELF> should be parameterized
			----------
			3. INFO in B2.java (at line 7)
				ref.get().self();
				^^^^^^^^^
			Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'Reference<@NonNull Fluent>\'. Type \'Reference<T>\' doesn\'t seem to be designed with null type annotations in mind
			----------
			"""
	);
}
public void testBug509025_a() {
	runConformTestWithLibs(
		new String[] {
			"MyAnno.java",
			"import java.lang.annotation.Retention;\n" +
			"import java.lang.annotation.RetentionPolicy;\n" +
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@Retention(RetentionPolicy.RUNTIME)\n" +
			"@NonNullByDefault(DefaultLocation.ARRAY_CONTENTS)\n" +
			"public @interface MyAnno {\n" +
			"	@NonNull String[] items();\n" +
			"\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in MyAnno.java (at line 10)
				@NonNull String[] items();
				^^^^^^^^^^^^^^^^^
			The nullness annotation is redundant with a default that applies to this location
			----------
			""",
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"AnnoLoop.java",
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"@NonNullByDefault(DefaultLocation.ARRAY_CONTENTS)\n" +
			"public class AnnoLoop {\n" +
			"	@NonNull String[] test(MyAnno anno) {\n" +
			"		return anno.items();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in AnnoLoop.java (at line 6)
				@NonNull String[] test(MyAnno anno) {
				^^^^^^^^^^^^^^^^^
			The nullness annotation is redundant with a default that applies to this location
			----------
			""",
		""
	);
}
public void testBug509025_b() {
	runConformTestWithLibs(
		new String[] {
			"MyAnno.java",
			"import java.lang.annotation.Retention;\n" +
			"import java.lang.annotation.RetentionPolicy;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@Retention(RetentionPolicy.RUNTIME)\n" +
			"@NonNullByDefault\n" +
			"public @interface MyAnno {\n" +
			"	String @NonNull[] items();\n" +
			"\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in MyAnno.java (at line 10)
				String @NonNull[] items();
				       ^^^^^^^^^^
			The nullness annotation is redundant with a default that applies to this location
			----------
			""",
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"AnnoLoop.java",
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class AnnoLoop {\n" +
			"	String @NonNull[] test(MyAnno anno) {\n" +
			"		return anno.items();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in AnnoLoop.java (at line 6)
				String @NonNull[] test(MyAnno anno) {
				       ^^^^^^^^^^
			The nullness annotation is redundant with a default that applies to this location
			----------
			""",
		""
	);
}
public void testBug509025_c() {
	runConformTestWithLibs(
		new String[] {
			"MyAnno.java",
			"import java.lang.annotation.Retention;\n" +
			"import java.lang.annotation.RetentionPolicy;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@Retention(RetentionPolicy.RUNTIME)\n" +
			"@NonNullByDefault\n" +
			"public @interface MyAnno {\n" +
			"	@NonNull String[] items();\n" +
			"\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"",
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"AnnoLoop.java",
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class AnnoLoop {\n" +
			"	@NonNull String[] test(MyAnno anno) {\n" +
			"		return anno.items();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"",
		""
	);
}
public void testBug501598() {
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"Foo.java",
			"import java.util.List;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"class Foo {\n" +
			"	static <T> @NonNull List<?> f() {\n" +
			"		throw new Error();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in Foo.java (at line 8)
				static <T> @NonNull List<?> f() {
				           ^^^^^^^^^^^^^
			The nullness annotation is redundant with a default that applies to this location
			----------
			"""
	);
}
public void testBug509328() {
	runConformTestWithLibs(
		new String[] {
			"test/Feature.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Feature {\n" +
			"	public Feature(String name) {\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runNegativeTestWithLibs(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Test {\n" +
			"	public static void f() {\n" +
			"		new Feature(null) {\n" +
			"			// anonymous subclass\n" +
			"		};\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\Test.java (at line 8)
				new Feature(null) {
				            ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			"""
	);
}
public void testBug510799() {
	runConformTestWithLibs(
		new String[] {
			"test/TestNNBDBreaksDimensionAnnotation.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"class TestNNBDBreaksDimensionAnnotation {\n" +
			"	Object f(String[] @NonNull [] a) {\n" +
			"		return a[0];\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug490403() {
	runConformTestWithLibs(
		new String[] {
			"test/TestNullInt.java",
			"package test;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"public class TestNullInt {\n" +
			"\n" +
			"	public void test() {\n" +
			"		@NonNull Integer[] keys = new @NonNull Integer[12];\n" +
			"		@NonNull Integer index = 0;\n" +
			"		for (int i = 0; i < 10; i++) {\n" +
			"			keys[index] = index;\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug490403while() {
	runConformTestWithLibs(
		new String[] {
			"test/TestNullInt.java",
			"package test;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"public abstract class TestNullInt {\n" +
			"	public abstract boolean b();\n" +
			"\n" +
			"	public void test(@NonNull Object[] keys, @NonNull String o) {\n" +
			"		while (b()) {\n" +
			"			keys[0] = o;\n" +
			"			keys[1] = b() ? o : o;\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}

public void testBug490403negative() {
	runNegativeTestWithLibs(
		new String[] {
			"test/TestNullInt.java",
			"package test;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"public abstract class TestNullInt {\n" +
			"	public abstract boolean b();\n" +
			"\n" +
			"	public void warning(@NonNull Object[] keys, String o) {\n" +
			"		while (b()) {\n" +
			"			keys[0] = o;\n" +
			"			keys[1] = b() ? o : \"\";\n" +
			"		}\n" +
			"	}\n" +
			"	public void error(@NonNull Object[] keys, @Nullable String o) {\n" +
			"		while (b()) {\n" +
			"			keys[0] = o;\n" +
			"			keys[1] = b() ? \"\" : o;\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in test\\TestNullInt.java (at line 9)
				keys[0] = o;
				          ^
			Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'@NonNull Object\'
			----------
			2. WARNING in test\\TestNullInt.java (at line 10)
				keys[1] = b() ? o : "";
				                ^
			Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'@NonNull Object\'
			----------
			3. ERROR in test\\TestNullInt.java (at line 15)
				keys[0] = o;
				          ^
			Null type mismatch (type annotations): required \'@NonNull Object\' but this expression has type \'@Nullable String\'
			----------
			4. ERROR in test\\TestNullInt.java (at line 16)
				keys[1] = b() ? "" : o;
				                     ^
			Null type mismatch (type annotations): required \'@NonNull Object\' but this expression has type \'@Nullable String\'
			----------
			"""
	);
}
public void testBug490403typeArgAnnotationMismatch() {
	runNegativeTestWithLibs(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"class Ref<T> {\n" +
			"}\n" +
			"\n" +
			"public abstract class Test {\n" +
			"    abstract boolean b();\n" +
			"\n" +
			"    public void testAnnotationMismatch(@NonNull Ref<@Nullable String> x, @NonNull Ref<@NonNull String>[] keys) {\n" +
			"        keys[0] = x;\n" +
			"        while (b()) {\n" +
			"            keys[0] = x;\n" +
			"            keys[1] = b() ? keys[0] : x;\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\Test.java (at line 13)
				keys[0] = x;
				          ^
			Null type mismatch (type annotations): required \'@NonNull Ref<@NonNull String>\' but this expression has type \'@NonNull Ref<@Nullable String>\'
			----------
			2. ERROR in test\\Test.java (at line 15)
				keys[0] = x;
				          ^
			Null type mismatch (type annotations): required \'@NonNull Ref<@NonNull String>\' but this expression has type \'@NonNull Ref<@Nullable String>\'
			----------
			3. ERROR in test\\Test.java (at line 16)
				keys[1] = b() ? keys[0] : x;
				                          ^
			Null type mismatch (type annotations): required \'@NonNull Ref<@NonNull String>\' but this expression has type \'@NonNull Ref<@Nullable String>\'
			----------
			"""
	);
}
public void testBug499589() {
	runConformTestWithLibs(
		new String[] {
			"test/BogusWarning.java",
			"package test;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.ARRAY_CONTENTS;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.PARAMETER;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault({ PARAMETER, ARRAY_CONTENTS })\n" +
			"class BogusWarning {\n" +
			"	static void a(String[] array) {\n" +
			"		x(array[0]); // <----- bogus warning\n" +
			"	}\n" +
			"\n" +
			"	static void x(String s) {\n" +
			"		System.out.println(s);\n" +
			"	}\n" +
			"\n" +
			"	static void b(String[][] array) {\n" +
			"		y(array[0]); // <----- bogus warning\n" +
			"	}\n" +
			"\n" +
			"	static void y(String[] s) {\n" +
			"		System.out.println(s[0]);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug499589multidim() {
	runNegativeTestWithLibs(
		new String[] {
			"test/BogusWarning.java",
			"package test;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.ARRAY_CONTENTS;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.PARAMETER;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"@NonNullByDefault({ PARAMETER, ARRAY_CONTENTS })\n" +
			"class BogusWarning {\n" +
			"	static void foo(String[] @Nullable [] array) {\n" +
			"		x(array[0]);\n" +
			"	}\n" +
			"	static void x(String[] s) {\n" +
			"		System.out.println(s[0]);\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\BogusWarning.java (at line 11)
				x(array[0]);
				  ^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull String @NonNull[]\' but this expression has type \'@NonNull String @Nullable[]\'
			----------
			"""
	);
}

public void testBug499589leafTypeNullable() {
	runNegativeTestWithLibs(
		new String[] {
			"test/BogusWarning.java",
			"package test;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.ARRAY_CONTENTS;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.PARAMETER;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"@NonNullByDefault({ PARAMETER, ARRAY_CONTENTS })\n" +
			"class BogusWarning {\n" +
			"	static void foo(@Nullable String[] array) {\n" +
			"		x(array[0]);\n" +
			"	}\n" +
			"\n" +
			"	static void x(String s) {\n" +
			"		System.out.println(s);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\BogusWarning.java (at line 11)
				x(array[0]);
				  ^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'
			----------
			"""
	);
}

public void testBug499589qualified() {
	runConformTestWithLibs(
		new String[] {
			"test/BogusWarning.java",
			"package test;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.ARRAY_CONTENTS;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.PARAMETER;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault({ PARAMETER, ARRAY_CONTENTS })\n" +
			"class BogusWarning {\n" +
			"	static void foo(java.lang.String[] array) {\n" +
			"		x(array[0]);\n" +
			"	}\n" +
			"\n" +
			"	static void x(String s) {\n" +
			"		System.out.println(s);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}

public void testBug499589qualified_leafTypeNullable() {
	runNegativeTestWithLibs(
		new String[] {
			"test/BogusWarning.java",
			"package test;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.ARRAY_CONTENTS;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.PARAMETER;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"@NonNullByDefault({ PARAMETER, ARRAY_CONTENTS })\n" +
			"class BogusWarning {\n" +
			"	static void foo(java.lang.@Nullable String[] array) {\n" +
			"		x(array[0]);\n" +
			"	}\n" +
			"\n" +
			"	static void x(String s) {\n" +
			"		System.out.println(s);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\BogusWarning.java (at line 11)
				x(array[0]);
				  ^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'
			----------
			"""
	);
}
public void testBug499589qualified_multidim() {
	runNegativeTestWithLibs(
		new String[] {
			"test/BogusWarning.java",
			"package test;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.ARRAY_CONTENTS;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.PARAMETER;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"@NonNullByDefault({ PARAMETER, ARRAY_CONTENTS })\n" +
			"class BogusWarning {\n" +
			"	static void foo(java.lang.String[] @Nullable [] array) {\n" +
			"		x(array[0]);\n" +
			"	}\n" +
			"	static void x(java.lang.String[] s) {\n" +
			"		System.out.println(s[0]);\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\BogusWarning.java (at line 11)
				x(array[0]);
				  ^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull String @NonNull[]\' but this expression has type \'@NonNull String @Nullable[]\'
			----------
			"""
	);
}
public void testBug499589STB() {
	runNegativeTestWithLibs(
		new String[] {
			"test/Ref.java",
			"package test;\n" +
			"\n" +
			"public class Ref<T> {\n" +
			"	T get() {\n" +
			"		throw new RuntimeException();\n" +
			"	}\n" +
			"}\n" +
			"",
			"test/X.java",
			"package test;\n" +
			"\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.*;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"@SuppressWarnings({ \"unchecked\" })\n" +
			"@NonNullByDefault({ FIELD, RETURN_TYPE, PARAMETER, ARRAY_CONTENTS, TYPE_ARGUMENT })\n" +
			"public abstract class X {\n" +
			"	public final String[][] field = {};\n" +
			"	public final @Nullable String[][] fieldWithNullable1 = {};\n" +
			"	public final String[] @Nullable [] fieldWithNullable2 = {};\n" +
			"\n" +
			"	public final Ref<String[][]> list = new Ref<>();\n" +
			"	public final Ref<@Nullable String[][]> listWithNullable1 = new Ref<>();\n" +
			"	public final Ref<String[] @Nullable []> listWithNullable2 = new Ref<>();\n" +
			"\n" +
			"	public abstract String[][] method();\n" +
			"	public abstract @Nullable String[][] methodWithNullable1();\n" +
			"	public abstract String[] @Nullable [] methodWithNullable2();\n" +
			"\n" +
			"	public final Ref<String[][]>[][] genericField = new Ref[0][];\n" +
			"	public final @Nullable Ref<@Nullable String[][]>[][] genericFieldWithNullable1 = new Ref[0][];\n" +
			"	public final Ref<String[] @Nullable []>[] @Nullable [] genericFieldWithNullable2 = new Ref[0][];\n" +
			"}\n" +
			"\n" +
			"class SourceUsage {\n" +
			"	void check(@NonNull String @NonNull [] @NonNull [] s) {\n" +
			"	}\n" +
			"\n" +
			"	void checkGeneric(@NonNull Ref<@NonNull String @NonNull [] @NonNull []> @NonNull [] @NonNull [] s) {\n" +
			"	}\n" +
			"\n" +
			"	void f(X x) {\n" +
			"		check(x.field);\n" +
			"		check(x.fieldWithNullable1);\n" +
			"		check(x.fieldWithNullable2);\n" +
			"		check(x.list.get());\n" +
			"		check(x.listWithNullable1.get());\n" +
			"		check(x.listWithNullable2.get());\n" +
			"		check(x.method());\n" +
			"		check(x.methodWithNullable1());\n" +
			"		check(x.methodWithNullable2());\n" +
			"		checkGeneric(x.genericField);\n" +
			"		checkGeneric(x.genericFieldWithNullable1);\n" +
			"		checkGeneric(x.genericFieldWithNullable2);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\X.java (at line 36)
				check(x.fieldWithNullable1);
				      ^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull String @NonNull[] @NonNull[]\' but this expression has type \'@Nullable String @NonNull[] @NonNull[]\'
			----------
			2. ERROR in test\\X.java (at line 37)
				check(x.fieldWithNullable2);
				      ^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull String @NonNull[] @NonNull[]\' but this expression has type \'@NonNull String @NonNull[] @Nullable[]\'
			----------
			3. ERROR in test\\X.java (at line 39)
				check(x.listWithNullable1.get());
				      ^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull String @NonNull[] @NonNull[]\' but this expression has type \'@Nullable String @NonNull[] @NonNull[]\'
			----------
			4. ERROR in test\\X.java (at line 40)
				check(x.listWithNullable2.get());
				      ^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull String @NonNull[] @NonNull[]\' but this expression has type \'@NonNull String @NonNull[] @Nullable[]\'
			----------
			5. ERROR in test\\X.java (at line 42)
				check(x.methodWithNullable1());
				      ^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull String @NonNull[] @NonNull[]\' but this expression has type \'@Nullable String @NonNull[] @NonNull[]\'
			----------
			6. ERROR in test\\X.java (at line 43)
				check(x.methodWithNullable2());
				      ^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull String @NonNull[] @NonNull[]\' but this expression has type \'@NonNull String @NonNull[] @Nullable[]\'
			----------
			7. ERROR in test\\X.java (at line 45)
				checkGeneric(x.genericFieldWithNullable1);
				             ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull Ref<@NonNull String @NonNull[] @NonNull[]> @NonNull[] @NonNull[]\' but this expression has type \'@Nullable Ref<@Nullable String @NonNull[] @NonNull[]> @NonNull[] @NonNull[]\'
			----------
			8. ERROR in test\\X.java (at line 46)
				checkGeneric(x.genericFieldWithNullable2);
				             ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull Ref<@NonNull String @NonNull[] @NonNull[]> @NonNull[] @NonNull[]\' but this expression has type \'@NonNull Ref<@NonNull String @NonNull[] @Nullable[]> @NonNull[] @Nullable[]\'
			----------
			"""
	);
}
public void testBug499589BTB() {
	Runner runner = new Runner();
	runner.classLibraries = this.LIBS;
	runner.testFiles =
		new String[] {
			"test/Ref.java",
			"package test;\n" +
			"\n" +
			"public class Ref<T> {\n" +
			"	T get() {\n" +
			"		throw new RuntimeException();\n" +
			"	}\n" +
			"}\n" +
			"",
			"test/X.java",
			"package test;\n" +
			"\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.ARRAY_CONTENTS;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.FIELD;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.PARAMETER;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.RETURN_TYPE;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.TYPE_ARGUMENT;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"@SuppressWarnings({ \"unchecked\" })\n" +
			"@NonNullByDefault({ FIELD, RETURN_TYPE, PARAMETER, ARRAY_CONTENTS, TYPE_ARGUMENT })\n" +
			"public abstract class X {\n" +
			"	public final String[][] field = {};\n" +
			"	public final @Nullable String[][] fieldWithNullable1 = {};\n" +
			"	public final String[] @Nullable [] fieldWithNullable2 = {};\n" +
			"\n" +
			"	public final Ref<String[][]> list = new Ref<>();\n" +
			"	public final Ref<@Nullable String[][]> listWithNullable1 = new Ref<>();\n" +
			"	public final Ref<String[] @Nullable []> listWithNullable2 = new Ref<>();\n" +
			"\n" +
			"	public abstract String[][] method();\n" +
			"	public abstract @Nullable String[][] methodWithNullable1();\n" +
			"	public abstract String[] @Nullable [] methodWithNullable2();\n" +
			"\n" +
			"	public final Ref<String[][]>[][] genericField = new Ref[0][];\n" +
			"	public final @Nullable Ref<@Nullable String[][]>[][] genericFieldWithNullable1 = new Ref[0][];\n" +
			"	public final Ref<String[] @Nullable []>[] @Nullable [] genericFieldWithNullable2 = new Ref[0][];\n" +
			"}\n" +
			"",
		};
	runner.javacTestOptions = new JavacTestOptions.SuppressWarnings("rawtypes"); // javac detects rawtypes at new Ref[0][0]
	runner.runConformTest();
	runNegativeTestWithLibs(
		new String[] {
			"test/BinaryUsage.java",
			"package test;\n" +
			"\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"class BinaryUsage {\n" +
			"	void check(@NonNull String @NonNull [] @NonNull [] s) {\n" +
			"	}\n" +
			"\n" +
			"	void checkGeneric(@NonNull Ref<@NonNull String @NonNull [] @NonNull []> @NonNull [] @NonNull [] s) {\n" +
			"	}\n" +
			"\n" +
			"	void f(X x) {\n" +
			"		check(x.field);\n" +
			"		check(x.fieldWithNullable1);\n" +
			"		check(x.fieldWithNullable2);\n" +
			"		check(x.list.get());\n" +
			"		check(x.listWithNullable1.get());\n" +
			"		check(x.listWithNullable2.get());\n" +
			"		check(x.method());\n" +
			"		check(x.methodWithNullable1());\n" +
			"		check(x.methodWithNullable2());\n" +
			"		checkGeneric(x.genericField);\n" +
			"		checkGeneric(x.genericFieldWithNullable1);\n" +
			"		checkGeneric(x.genericFieldWithNullable2);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\BinaryUsage.java (at line 15)
				check(x.fieldWithNullable1);
				      ^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull String @NonNull[] @NonNull[]\' but this expression has type \'@Nullable String @NonNull[] @NonNull[]\'
			----------
			2. ERROR in test\\BinaryUsage.java (at line 16)
				check(x.fieldWithNullable2);
				      ^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull String @NonNull[] @NonNull[]\' but this expression has type \'@NonNull String @NonNull[] @Nullable[]\'
			----------
			3. ERROR in test\\BinaryUsage.java (at line 18)
				check(x.listWithNullable1.get());
				      ^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull String @NonNull[] @NonNull[]\' but this expression has type \'@Nullable String @NonNull[] @NonNull[]\'
			----------
			4. ERROR in test\\BinaryUsage.java (at line 19)
				check(x.listWithNullable2.get());
				      ^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull String @NonNull[] @NonNull[]\' but this expression has type \'@NonNull String @NonNull[] @Nullable[]\'
			----------
			5. ERROR in test\\BinaryUsage.java (at line 21)
				check(x.methodWithNullable1());
				      ^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull String @NonNull[] @NonNull[]\' but this expression has type \'@Nullable String @NonNull[] @NonNull[]\'
			----------
			6. ERROR in test\\BinaryUsage.java (at line 22)
				check(x.methodWithNullable2());
				      ^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull String @NonNull[] @NonNull[]\' but this expression has type \'@NonNull String @NonNull[] @Nullable[]\'
			----------
			7. ERROR in test\\BinaryUsage.java (at line 24)
				checkGeneric(x.genericFieldWithNullable1);
				             ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull Ref<@NonNull String @NonNull[] @NonNull[]> @NonNull[] @NonNull[]\' but this expression has type \'@Nullable Ref<@Nullable String @NonNull[] @NonNull[]> @NonNull[] @NonNull[]\'
			----------
			8. ERROR in test\\BinaryUsage.java (at line 25)
				checkGeneric(x.genericFieldWithNullable2);
				             ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull Ref<@NonNull String @NonNull[] @NonNull[]> @NonNull[] @NonNull[]\' but this expression has type \'@NonNull Ref<@NonNull String @NonNull[] @Nullable[]> @NonNull[] @Nullable[]\'
			----------
			"""
	);
}

public void testBug499589STBqualified() {
	runNegativeTestWithLibs(
		new String[] {
			"test/Ref.java",
			"package test;\n" +
			"\n" +
			"public class Ref<T> {\n" +
			"	T get() {\n" +
			"		throw new RuntimeException();\n" +
			"	}\n" +
			"}\n" +
			"",
			"test/A.java",
			"package test;\n" +
			"\n" +
			"public class A {\n" +
			"	class B {\n" +
			"	}\n" +
			"}\n" +
			"",
			"test/X.java",
			"package test;\n" +
			"\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.*;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"@SuppressWarnings({ \"unchecked\" })\n" +
			"@NonNullByDefault({ FIELD, RETURN_TYPE, PARAMETER, ARRAY_CONTENTS, TYPE_ARGUMENT })\n" +
			"public abstract class X {\n" +
			"	public final test.A.B[][] field = {};\n" +
			"	public final test.A.@Nullable B[][] fieldWithNullable1 = {};\n" +
			"	public final test.A.B[] @Nullable [] fieldWithNullable2 = {};\n" +
			"\n" +
			"	public final test.Ref<test.A.B[][]> list = new Ref<>();\n" +
			"	public final test.Ref<test.A.@Nullable B[][]> listWithNullable1 = new Ref<>();\n" +
			"	public final test.Ref<test.A.B[] @Nullable []> listWithNullable2 = new Ref<>();\n" +
			"\n" +
			"	public abstract test.A.B[][] method();\n" +
			"	public abstract test.A.@Nullable B[][] methodWithNullable1();\n" +
			"	public abstract test.A.B[] @Nullable [] methodWithNullable2();\n" +
			"\n" +
			"	public final test.Ref<test.A.B[][]>[][] genericField = new Ref[0][];\n" +
			"	public final test.@Nullable Ref<test.A.@Nullable B[][]>[][] genericFieldWithNullable1 = new Ref[0][];;\n" +
			"	public final test.Ref<test.A.B[] @Nullable []>[] @Nullable[] genericFieldWithNullable2 = new Ref[0][];;\n" +
			"}\n" +
			"\n" +
			"class SourceUsage {\n" +
			"	void check(test.A.@NonNull B @NonNull [] @NonNull [] s) {\n" +
			"	}\n" +
			"	void checkGeneric(test.@NonNull Ref<test.A.@NonNull B @NonNull [] @NonNull []> @NonNull [] @NonNull [] s) {\n" +
			"	}\n" +
			"\n" +
			"	void f(X x) {\n" +
			"		check(x.field);\n" +
			"		check(x.fieldWithNullable1);\n" +
			"		check(x.fieldWithNullable2);\n" +
			"		check(x.list.get());\n" +
			"		check(x.listWithNullable1.get());\n" +
			"		check(x.listWithNullable2.get());\n" +
			"		check(x.method());\n" +
			"		check(x.methodWithNullable1());\n" +
			"		check(x.methodWithNullable2());\n" +
			"		checkGeneric(x.genericField);\n" +
			"		checkGeneric(x.genericFieldWithNullable1);\n" +
			"		checkGeneric(x.genericFieldWithNullable2);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\X.java (at line 35)
				check(x.fieldWithNullable1);
				      ^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'A.@NonNull B @NonNull[] @NonNull[]\' but this expression has type \'A.@Nullable B @NonNull[] @NonNull[]\'
			----------
			2. ERROR in test\\X.java (at line 36)
				check(x.fieldWithNullable2);
				      ^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'A.@NonNull B @NonNull[] @NonNull[]\' but this expression has type \'A.@NonNull B @NonNull[] @Nullable[]\'
			----------
			3. ERROR in test\\X.java (at line 38)
				check(x.listWithNullable1.get());
				      ^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'A.@NonNull B @NonNull[] @NonNull[]\' but this expression has type \'A.@Nullable B @NonNull[] @NonNull[]\'
			----------
			4. ERROR in test\\X.java (at line 39)
				check(x.listWithNullable2.get());
				      ^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'A.@NonNull B @NonNull[] @NonNull[]\' but this expression has type \'A.@NonNull B @NonNull[] @Nullable[]\'
			----------
			5. ERROR in test\\X.java (at line 41)
				check(x.methodWithNullable1());
				      ^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'A.@NonNull B @NonNull[] @NonNull[]\' but this expression has type \'A.@Nullable B @NonNull[] @NonNull[]\'
			----------
			6. ERROR in test\\X.java (at line 42)
				check(x.methodWithNullable2());
				      ^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'A.@NonNull B @NonNull[] @NonNull[]\' but this expression has type \'A.@NonNull B @NonNull[] @Nullable[]\'
			----------
			7. ERROR in test\\X.java (at line 44)
				checkGeneric(x.genericFieldWithNullable1);
				             ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull Ref<A.@NonNull B @NonNull[] @NonNull[]> @NonNull[] @NonNull[]\' but this expression has type \'@Nullable Ref<A.@Nullable B @NonNull[] @NonNull[]> @NonNull[] @NonNull[]\'
			----------
			8. ERROR in test\\X.java (at line 45)
				checkGeneric(x.genericFieldWithNullable2);
				             ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull Ref<A.@NonNull B @NonNull[] @NonNull[]> @NonNull[] @NonNull[]\' but this expression has type \'@NonNull Ref<A.@NonNull B @NonNull[] @Nullable[]> @NonNull[] @Nullable[]\'
			----------
			"""
	);
}
public void testBug499589BTBqualified() {
	Runner runner = new Runner();
	runner.classLibraries = this.LIBS;
	runner.testFiles =
		new String[] {
			"test/Ref.java",
			"package test;\n" +
			"\n" +
			"public class Ref<T> {\n" +
			"	T get() {\n" +
			"		throw new RuntimeException();\n" +
			"	}\n" +
			"}\n" +
			"",
			"test/A.java",
			"package test;\n" +
			"\n" +
			"public class A {\n" +
			"	class B {\n" +
			"	}\n" +
			"}\n" +
			"",
			"test/X.java",
			"package test;\n" +
			"\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.*;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"@SuppressWarnings({ \"unchecked\" })\n" +
			"@NonNullByDefault({ FIELD, RETURN_TYPE, PARAMETER, ARRAY_CONTENTS, TYPE_ARGUMENT })\n" +
			"public abstract class X {\n" +
			"	public final test.A.B[][] field = {};\n" +
			"	public final test.A.@Nullable B[][] fieldWithNullable1 = {};\n" +
			"	public final test.A.B[] @Nullable [] fieldWithNullable2 = {};\n" +
			"\n" +
			"	public final test.Ref<test.A.B[][]> list = new Ref<>();\n" +
			"	public final test.Ref<test.A.@Nullable B[][]> listWithNullable1 = new Ref<>();\n" +
			"	public final test.Ref<test.A.B[] @Nullable []> listWithNullable2 = new Ref<>();\n" +
			"\n" +
			"	public abstract test.A.B[][] method();\n" +
			"	public abstract test.A.@Nullable B[][] methodWithNullable1();\n" +
			"	public abstract test.A.B[] @Nullable [] methodWithNullable2();\n" +
			"\n" +
			"	public final test.Ref<test.A.B[][]>[][] genericField = new Ref[0][];\n" +
			"	public final test.@Nullable Ref<test.A.@Nullable B[][]>[][] genericFieldWithNullable1 = new Ref[0][];;\n" +
			"	public final test.Ref<test.A.B[] @Nullable []>[] @Nullable[] genericFieldWithNullable2 = new Ref[0][];;\n" +
			"}\n" +
			"",
		};
	runner.javacTestOptions = new JavacTestOptions.SuppressWarnings("rawtypes"); // javac detects rawtypes at new Ref[0][0]
	runner.runConformTest();
	runNegativeTestWithLibs(
		new String[] {
			"test/BinaryUsage.java",
			"package test;\n" +
			"\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"class BinaryUsage {\n" +
			"	void check(test.A.@NonNull B @NonNull [] @NonNull [] s) {\n" +
			"	}\n" +
			"	void checkGeneric(test.@NonNull Ref<test.A.@NonNull B @NonNull [] @NonNull []> @NonNull [] @NonNull [] s) {\n" +
			"	}\n" +
			"\n" +
			"	void f(X x) {\n" +
			"		check(x.field);\n" +
			"		check(x.fieldWithNullable1);\n" +
			"		check(x.fieldWithNullable2);\n" +
			"		check(x.list.get());\n" +
			"		check(x.listWithNullable1.get());\n" +
			"		check(x.listWithNullable2.get());\n" +
			"		check(x.method());\n" +
			"		check(x.methodWithNullable1());\n" +
			"		check(x.methodWithNullable2());\n" +
			"		checkGeneric(x.genericField);\n" +
			"		checkGeneric(x.genericFieldWithNullable1);\n" +
			"		checkGeneric(x.genericFieldWithNullable2);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\BinaryUsage.java (at line 14)
				check(x.fieldWithNullable1);
				      ^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'A.@NonNull B @NonNull[] @NonNull[]\' but this expression has type \'A.@Nullable B @NonNull[] @NonNull[]\'
			----------
			2. ERROR in test\\BinaryUsage.java (at line 15)
				check(x.fieldWithNullable2);
				      ^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'A.@NonNull B @NonNull[] @NonNull[]\' but this expression has type \'A.@NonNull B @NonNull[] @Nullable[]\'
			----------
			3. ERROR in test\\BinaryUsage.java (at line 17)
				check(x.listWithNullable1.get());
				      ^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'A.@NonNull B @NonNull[] @NonNull[]\' but this expression has type \'A.@Nullable B @NonNull[] @NonNull[]\'
			----------
			4. ERROR in test\\BinaryUsage.java (at line 18)
				check(x.listWithNullable2.get());
				      ^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'A.@NonNull B @NonNull[] @NonNull[]\' but this expression has type \'A.@NonNull B @NonNull[] @Nullable[]\'
			----------
			5. ERROR in test\\BinaryUsage.java (at line 20)
				check(x.methodWithNullable1());
				      ^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'A.@NonNull B @NonNull[] @NonNull[]\' but this expression has type \'A.@Nullable B @NonNull[] @NonNull[]\'
			----------
			6. ERROR in test\\BinaryUsage.java (at line 21)
				check(x.methodWithNullable2());
				      ^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'A.@NonNull B @NonNull[] @NonNull[]\' but this expression has type \'A.@NonNull B @NonNull[] @Nullable[]\'
			----------
			7. ERROR in test\\BinaryUsage.java (at line 23)
				checkGeneric(x.genericFieldWithNullable1);
				             ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull Ref<A.@NonNull B @NonNull[] @NonNull[]> @NonNull[] @NonNull[]\' but this expression has type \'@Nullable Ref<A.@Nullable B @NonNull[] @NonNull[]> @NonNull[] @NonNull[]\'
			----------
			8. ERROR in test\\BinaryUsage.java (at line 24)
				checkGeneric(x.genericFieldWithNullable2);
				             ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch (type annotations): required \'@NonNull Ref<A.@NonNull B @NonNull[] @NonNull[]> @NonNull[] @NonNull[]\' but this expression has type \'@NonNull Ref<A.@NonNull B @NonNull[] @Nullable[]> @NonNull[] @Nullable[]\'
			----------
			"""
	);
}
public void testBug499589arrayAllocation() {
	runNegativeTestWithLibs(
		new String[] {
			"test/ArrayAllocation.java",
			"package test;\n" +
			"\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.ARRAY_CONTENTS;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault({ ARRAY_CONTENTS })\n" +
			"public class ArrayAllocation {\n" +
			"	public Integer[] x1 = { 1, 2, 3, null };\n" +
			"	public Integer[] x2 = new Integer[] { 1, 2, 3 };\n" +
			"	public Integer[] x3 = new Integer[] { 1, 2, 3, null };\n" +
			"	public Integer[] x4 = new Integer[3];\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\ArrayAllocation.java (at line 9)
				public Integer[] x1 = { 1, 2, 3, null };
				                                 ^^^^
			Null type mismatch: required \'@NonNull Integer\' but the provided value is null
			----------
			2. ERROR in test\\ArrayAllocation.java (at line 11)
				public Integer[] x3 = new Integer[] { 1, 2, 3, null };
				                                               ^^^^
			Null type mismatch: required \'@NonNull Integer\' but the provided value is null
			----------
			3. WARNING in test\\ArrayAllocation.java (at line 12)
				public Integer[] x4 = new Integer[3];
				                      ^^^^^^^^^^^^^^
			Null type safety (type annotations): The expression of type \'Integer[]\' needs unchecked conversion to conform to \'@NonNull Integer []\'
			----------
			"""
	);
}
public void testBug499589generics() {
	runNegativeTestWithLibs(
		new String[] {
			"test/Methods.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"@org.eclipse.jdt.annotation.NonNullByDefault({ DefaultLocation.TYPE_ARGUMENT, DefaultLocation.ARRAY_CONTENTS })\n" +
			"public class Methods {\n" +
			"	static interface List<T> {\n" +
			"		T get(int i);\n" +
			"	}\n" +
			"\n" +
			"	public static List<String> f0(List<String> list) {\n" +
			"		@NonNull\n" +
			"		Object o = list.get(0);\n" +
			"		return list;\n" +
			"	}\n" +
			"\n" +
			"	public static String[] f1(String[] array) {\n" +
			"		@NonNull\n" +
			"		Object o = array[0];\n" +
			"		return array;\n" +
			"	}\n" +
			"\n" +
			"	public static <T> List<T> g0(List<T> list) {\n" +
			"		@NonNull\n" +
			"		Object o = list.get(0); // problem\n" +
			"		return list;\n" +
			"	}\n" +
			"\n" +
			"	public static <T> T[] g1(T[] array) {\n" +
			"		@NonNull\n" +
			"		Object o = array[0]; // problem\n" +
			"		return array;\n" +
			"	}\n" +
			"\n" +
			"	public static <@NonNull T> List<@NonNull T> h0(List<T> list) {\n" +
			"		@NonNull\n" +
			"		Object o = list.get(0);\n" +
			"		return list;\n" +
			"	}\n" +
			"\n" +
			"	public static <@NonNull T> @NonNull T[] h1(T[] array) {\n" +
			"		@NonNull\n" +
			"		Object o = array[0];\n" +
			"		return array;\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\Methods.java (at line 26)
				Object o = list.get(0); // problem
				           ^^^^^^^^^^^
			Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			2. ERROR in test\\Methods.java (at line 32)
				Object o = array[0]; // problem
				           ^^^^^^^^
			Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			"""
	);
}
public void testBug511723() {
	runNegativeTestWithLibs(
		new String[] {
			"test/ArrayVsList.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"public class ArrayVsList {\n" +
			"	static interface List<T> {\n" +
			"		T get(int i);\n" +
			"	}\n" +
			"	public static <T> void f(List<T> list) {\n" +
			"		@NonNull\n" +
			"		Object o = list.get(0); // problem\n" +
			"		o.hashCode();\n" +
			"	}\n" +
			"\n" +
			"	public static <T> void g(T[] array) {\n" +
			"		@NonNull\n" +
			"		Object o = array[0]; // problem\n" +
			"		o.hashCode();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\ArrayVsList.java (at line 11)
				Object o = list.get(0); // problem
				           ^^^^^^^^^^^
			Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			2. ERROR in test\\ArrayVsList.java (at line 17)
				Object o = array[0]; // problem
				           ^^^^^^^^
			Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type
			----------
			"""
	);
}
public void testBug498084() {
	runConformTestWithLibs(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"import java.util.HashMap;\n" +
			"import java.util.Map;\n" +
			"import java.util.function.Function;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Test {\n" +
			"\n" +
			"	protected static final <K, V> V cache(final Map<K, V> cache, final V value, final Function<V, K> keyFunction) {\n" +
			"		cache.put(keyFunction.apply(value), value);\n" +
			"		return value;\n" +
			"	}\n" +
			"\n" +
			"	public static final void main(final String[] args) {\n" +
			"		Map<Integer, String> cache = new HashMap<>();\n" +
			"		cache(cache, \"test\", String::length); // Warning: Null type safety at\n" +
			"											// method return type: Method\n" +
			"											// descriptor\n" +
			"											// Function<String,Integer>.apply(String)\n" +
			"											// promises '@NonNull Integer'\n" +
			"											// but referenced method\n" +
			"											// provides 'int'\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug498084b() {
	runNegativeTestWithLibs(
		new String[] {
			"test/Test2.java",
			"package test;\n" +
			"\n" +
			"import java.util.function.Consumer;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"public class Test2 {\n" +
			"	static void f(int i) {\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		Consumer<@Nullable Integer> sam = Test2::f;\n" +
			"		sam.accept(null); // <- NullPointerExpection when run\n" +
			"		Consumer<Integer> sam2 = Test2::f;\n" +
			"		sam2.accept(null); // variation: unchecked \n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\Test2.java (at line 12)
				Consumer<@Nullable Integer> sam = Test2::f;
				                                  ^^^^^^^^
			Null type mismatch at parameter 1: required \'int\' but provided \'@Nullable Integer\' via method descriptor Consumer<Integer>.accept(Integer)
			----------
			2. WARNING in test\\Test2.java (at line 14)
				Consumer<Integer> sam2 = Test2::f;
				                         ^^^^^^^^
			Null type safety: parameter 1 provided via method descriptor Consumer<Integer>.accept(Integer) needs unchecked conversion to conform to \'int\'
			----------
			"""
	);
}
public void testBug513495() {
	runNegativeTestWithLibs(
		new String[] {
			"test/Test3.java",
			"package test;\n" +
			"\n" +
			"import java.util.function.Function;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"public class Test3 {\n" +
			"	public static void main(String[] args) {\n" +
			"		Function<@Nullable Integer, Object> sam = Integer::intValue;\n" +
			"		sam.apply(null); // <- NullPointerExpection\n" +
			"		Function<Integer, Object> sam2 = Integer::intValue;\n" +
			"		sam2.apply(null); // variation: unchecked, so intentionally no warning reported, but would give NPE too \n" +
			"	}\n" +
			"	void wildcards(Class<?>[] params) { // unchecked case with wildcards\n" +
			"		java.util.Arrays.stream(params).map(Class::getName).toArray(String[]::new);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\Test3.java (at line 9)
				Function<@Nullable Integer, Object> sam = Integer::intValue;
				                                          ^^^^^^^^^^^^^^^^^
			Null type mismatch at parameter 'this': required \'@NonNull Integer\' but provided \'@Nullable Integer\' via method descriptor Function<Integer,Object>.apply(Integer)
			----------
			"""
	);
}
public void testBug513855() {
	runConformTestWithLibs(
		new String[] {
			"test1/X.java",
			"package test1;\n" +
			"\n" +
			"import java.math.BigDecimal;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class X {\n" +
			"	interface Sink<T extends Number> {\n" +
			"		void receive(T t);\n" +
			"	}\n" +
			"\n" +
			"	interface Source<U extends BigDecimal> {\n" +
			"		U get();\n" +
			"	}\n" +
			"\n" +
			"	void nn(Object x) {\n" +
			"	}\n" +
			"\n" +
			"	void f(Source<?> source) {\n" +
			"		nn(source.get());\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug513855lambda() {
	runConformTestWithLibs(
		new String[] {
			"test1/Lambda3.java",
			"package test1;\n" +
			"\n" +
			"import java.math.BigDecimal;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Lambda3 {\n" +
			"	interface Sink<T extends Number> {\n" +
			"		void receive(T t);\n" +
			"	}\n" +
			"\n" +
			"	interface Source<U extends BigDecimal> {\n" +
			"		void sendTo(Sink<? super U> c);\n" +
			"	}\n" +
			"\n" +
			"	void f(Source<?> source) {\n" +
			"		source.sendTo(a -> a.scale());\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug514091() {
	runConformTestWithLibs(
		new String[] {
			"test1/SAM.java",
			"package test1;\n" +
			"\n" +
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"interface SAM<A> {\n" +
			"	void f(A[] a);\n" +
			"}\n" +
			""
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"test1/LambdaNN.java",
			"package test1;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"public class LambdaNN {\n" +
			"	void g1() {\n" +
			"		SAM<? super Number> sam = (Number @NonNull [] a) -> {};\n" +
			"		sam.f(new Number[0]);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug514570() {
	final Map compilerOptions = getCompilerOptions();
	compilerOptions.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
	runConformTestWithLibs(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"import java.util.List;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"public class Test {\n" +
			"	/**\n" +
			"	 * {@link #bug()}\n" +
			"	 */\n" +
			"	<E, T extends List<@NonNull E>> void bug() {\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		compilerOptions,
		""
	);
}
public void testBug514977() {
	runNegativeTestWithLibs(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"public class Test {\n" +
			"	static void nn(@NonNull Object i) {\n" +
			"		i.hashCode();\n" +
			"	}\n" +
			"\n" +
			"	static void f(@NonNull Integer @NonNull... args) {\n" +
			"		nn(args);\n" +
			"		for (Integer s : args) {\n" +
			"			nn(s);\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	@NonNullByDefault({ DefaultLocation.ARRAY_CONTENTS, DefaultLocation.PARAMETER })\n" +
			"	static void g(Integer... args) {\n" +
			"		nn(args);\n" +
			"		for (Integer s : args) {\n" +
			"			nn(s);\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		Integer i = args.length == 0 ? null : 1;\n" +
			"		Integer[] array = i == null ? null : new Integer[] {i};\n" +
			"		f(array);\n" +
			"		f(i);\n" +
			"		f(1, i);\n" +
			"		g(array);\n" +
			"		g(i);\n" +
			"		g(1, i);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\Test.java (at line 30)
				f(array);
				  ^^^^^
			Null type mismatch: required \'@NonNull Integer @NonNull[]\' but the provided value is inferred as @Nullable
			----------
			2. ERROR in test\\Test.java (at line 31)
				f(i);
				  ^
			Null type mismatch: required \'@NonNull Integer\' but the provided value is inferred as @Nullable
			----------
			3. ERROR in test\\Test.java (at line 32)
				f(1, i);
				     ^
			Null type mismatch: required \'@NonNull Integer\' but the provided value is inferred as @Nullable
			----------
			4. ERROR in test\\Test.java (at line 33)
				g(array);
				  ^^^^^
			Null type mismatch: required \'@NonNull Integer @NonNull[]\' but the provided value is inferred as @Nullable
			----------
			5. ERROR in test\\Test.java (at line 34)
				g(i);
				  ^
			Null type mismatch: required \'@NonNull Integer\' but the provided value is inferred as @Nullable
			----------
			6. ERROR in test\\Test.java (at line 35)
				g(1, i);
				     ^
			Null type mismatch: required \'@NonNull Integer\' but the provided value is inferred as @Nullable
			----------
			"""
	);
}
public void testBug515292() {
	runConformTestWithLibs(
		new String[] {
			"test/BoundedByFinal.java",
			"package test;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"public abstract class BoundedByFinal {\n" +
			"	abstract <T extends @Nullable String> void setSelection(T[] selectedObjects);\n" +
			"\n" +
			"	abstract @NonNull String @NonNull [] toArray1();\n" +
			"\n" +
			"	abstract @Nullable String @NonNull [] toArray2();\n" +
			"\n" +
			"	void test() {\n" +
			"		setSelection(toArray1());\n" +
			"		setSelection(toArray2());\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug526555() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.WARNING);

	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"ztest/OverrideTest.java",
			"""
				package ztest;
				
				import static org.eclipse.jdt.annotation.DefaultLocation.ARRAY_CONTENTS;
				import static org.eclipse.jdt.annotation.DefaultLocation.FIELD;
				import static org.eclipse.jdt.annotation.DefaultLocation.PARAMETER;
				import static org.eclipse.jdt.annotation.DefaultLocation.RETURN_TYPE;
				import static org.eclipse.jdt.annotation.DefaultLocation.TYPE_ARGUMENT;
				import static org.eclipse.jdt.annotation.DefaultLocation.TYPE_BOUND;
				
				import org.eclipse.jdt.annotation.NonNullByDefault;
				import org.eclipse.jdt.annotation.Nullable;
				
				interface X509TrustManager {
					void checkClientTrusted(String[] arg0, String arg1);
				
				}
				
				@NonNullByDefault({ PARAMETER, RETURN_TYPE, FIELD, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS })
				public class OverrideTest implements X509TrustManager {
					@Override
					public void checkClientTrusted(String @Nullable [] arg0, @Nullable String arg1) {
					}
				}""",
		},
		customOptions,
		"""
			----------
			1. WARNING in ztest\\OverrideTest.java (at line 21)
				public void checkClientTrusted(String @Nullable [] arg0, @Nullable String arg1) {
				                               ^^^^^^^^^^^^^^^^^^^
			Illegal redefinition of parameter arg0, inherited method from X509TrustManager declares this parameter as \'String[]\' (mismatching null constraints)
			----------
			"""
	);
}
public void testBug530913() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullByDefault");
	customOptions.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"annotation/DefaultLocation.java",
			"package annotation;\n" +
			"\n" +
			"public enum DefaultLocation {\n" +
			"    PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS\n" +
			"}\n" +
			"",
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullByDefault.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.*;\n" +
			" \n" +
			"public @interface NonNullByDefault {\n" +
			"	DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD, TYPE_BOUND, TYPE_ARGUMENT };\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	runConformTestWithLibs(
		false,
		new String[] {
			"nnbd_test2/Data.java",
			"package nnbd_test2;\n" +
			"\n" +
			"import java.util.function.Supplier;\n" +
			"\n" +
			"import annotation.DefaultLocation;\n" +
			"import annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Data {\n" +
			"    public void f(@NonNullByDefault({}) String s1, String s2) {\n" +
			"        s1.equals(s2);\n" +
			"    }\n" +
			"\n" +
			"    public void g(String s1, @NonNullByDefault({}) String s2) {\n" +
			"        s1.equals(s2);\n" +
			"    }\n" +
			"    \n" +
			"    @NonNullByDefault({})\n" +
			"    public void h(@NonNullByDefault({ DefaultLocation.PARAMETER }) Supplier<String> s1, @NonNullByDefault Supplier<String> s2) {\n" +
			"        s1.equals(s2);\n" +
			"    }\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	runNegativeTestWithLibs(
		new String[] {
			"nnbd_test1/Test.java",
			"package nnbd_test1;\n" +
			"\n" +
			"import java.util.function.Supplier;\n" +
			"\n" +
			"import annotation.DefaultLocation;\n" +
			"import annotation.NonNullByDefault;\n" +
			"import nnbd_test2.Data;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Test {\n" +
			"    void f(@NonNullByDefault({}) String s1, String s2) {\n" +
			"        if (s1 == null) {\n" +
			"            System.out.println(\"s is null\");\n" +
			"        }\n" +
			"        if (s2 == null) { // warning expected\n" +
			"            System.out.println(\"s2 is null\");\n" +
			"        }\n" +
			"    }\n" +
			"\n" +
			"    void g(String s1, @NonNullByDefault({}) String s2) {\n" +
			"        if (s1 == null) { // warning expected\n" +
			"            System.out.println(\"s is null\");\n" +
			"        }\n" +
			"        if (s2 == null) {\n" +
			"            System.out.println(\"s2 is null\");\n" +
			"        }\n" +
			"    }\n" +
			"\n" +
			"    @NonNullByDefault({})\n" +
			"    void h(@NonNullByDefault({ DefaultLocation.PARAMETER }) Supplier<String> s1, @NonNullByDefault Supplier<String> s2) {\n" +
			"        if (s1 == null) { // warning expected\n" +
			"            System.out.println(\"s is null\");\n" +
			"            return;\n" +
			"        }\n" +
			"        if (s2 == null) { // warning expected\n" +
			"            System.out.println(\"s2 is null\");\n" +
			"            return;\n" +
			"        }\n" +
			"        if (s1.get() == null) {\n" +
			"            System.out.println(\"s is null\");\n" +
			"        }\n" +
			"        if (s2.get() == null) { // warning expected\n" +
			"            System.out.println(\"s2 is null\");\n" +
			"        }\n" +
			"    }\n" +
			"\n" +
			"    void checkInvocation() {\n" +
			"        Test d = new Test();\n" +
			"        d.f(null, null); // warning on the second null expected\n" +
			"        d.g(null, null); // warning on the first null expected\n" +
			"    }\n" +
			"\n" +
			"    void checkBTBInvocation() {\n" +
			"        Data d = new Data();\n" +
			"        d.f(null, null); // warning on the second null expected\n" +
			"        d.g(null, null); // warning on the first null expected\n" +
			"    }\n" +
			"\n" +
			"    void checkInheritance() {\n" +
			"        Test t = new Test() {\n" +
			"            @Override\n" +
			"            void f(String s1, String s2) { // warning on the first parameter expected\n" +
			"                super.f(null, null); // warning on the second null expected\n" +
			"            }\n" +
			"\n" +
			"            @Override\n" +
			"            void g(String s1, String s2) { // warning on the second parameter expected\n" +
			"                super.g(null, null); // warning on the first null expected\n" +
			"            }\n" +
			"\n" +
			"            @Override\n" +
			"            void h(Supplier<String> s1, Supplier<String> s2) { // warning on the first parameter expected\n" +
			"            }\n" +
			"        };\n" +
			"    }\n" +
			"\n" +
			"    void checkBTBInheritance() {\n" +
			"        Data d = new Data() {\n" +
			"            @Override\n" +
			"            public void f(String s1, String s2) { // warning on the first parameter expected\n" +
			"                super.f(null, null); // warning on the second null expected\n" +
			"            }\n" +
			"\n" +
			"            @Override\n" +
			"            public void g(String s1, String s2) { // warning on the second parameter expected\n" +
			"                super.g(null, null); // warning on the first null expected\n" +
			"            }\n" +
			"\n" +
			"            @Override\n" +
			"            public void h(Supplier<String> s1, Supplier<String> s2) { // warning on the first parameter expected\n" +
			"            }\n" +
			"        };\n" +
			"    }\n" +
			"}\n" +
			"",
		},
		customOptions,
		"""
			----------
			1. ERROR in nnbd_test1\\Test.java (at line 15)
				if (s2 == null) { // warning expected
				    ^^
			Redundant null check: comparing \'@NonNull String\' against null
			----------
			2. ERROR in nnbd_test1\\Test.java (at line 21)
				if (s1 == null) { // warning expected
				    ^^
			Redundant null check: comparing \'@NonNull String\' against null
			----------
			3. ERROR in nnbd_test1\\Test.java (at line 31)
				if (s1 == null) { // warning expected
				    ^^
			Redundant null check: comparing \'@NonNull Supplier<String>\' against null
			----------
			4. ERROR in nnbd_test1\\Test.java (at line 35)
				if (s2 == null) { // warning expected
				    ^^
			Redundant null check: comparing \'@NonNull Supplier<@NonNull String>\' against null
			----------
			5. ERROR in nnbd_test1\\Test.java (at line 42)
				if (s2.get() == null) { // warning expected
				    ^^^^^^^^
			Redundant null check: comparing \'@NonNull String\' against null
			----------
			6. ERROR in nnbd_test1\\Test.java (at line 49)
				d.f(null, null); // warning on the second null expected
				          ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			7. ERROR in nnbd_test1\\Test.java (at line 50)
				d.g(null, null); // warning on the first null expected
				    ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			8. ERROR in nnbd_test1\\Test.java (at line 55)
				d.f(null, null); // warning on the second null expected
				          ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			9. ERROR in nnbd_test1\\Test.java (at line 56)
				d.g(null, null); // warning on the first null expected
				    ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			10. ERROR in nnbd_test1\\Test.java (at line 62)
				void f(String s1, String s2) { // warning on the first parameter expected
				       ^^^^^^
			Illegal redefinition of parameter s1, inherited method from Test does not constrain this parameter
			----------
			11. ERROR in nnbd_test1\\Test.java (at line 63)
				super.f(null, null); // warning on the second null expected
				              ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			12. ERROR in nnbd_test1\\Test.java (at line 67)
				void g(String s1, String s2) { // warning on the second parameter expected
				                  ^^^^^^
			Illegal redefinition of parameter s2, inherited method from Test does not constrain this parameter
			----------
			13. ERROR in nnbd_test1\\Test.java (at line 68)
				super.g(null, null); // warning on the first null expected
				        ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			14. ERROR in nnbd_test1\\Test.java (at line 72)
				void h(Supplier<String> s1, Supplier<String> s2) { // warning on the first parameter expected
				       ^^^^^^^^
			Illegal redefinition of parameter s1, inherited method from Test declares this parameter as \'@NonNull Supplier<String>\' (mismatching null constraints)
			----------
			15. ERROR in nnbd_test1\\Test.java (at line 80)
				public void f(String s1, String s2) { // warning on the first parameter expected
				              ^^^^^^
			Illegal redefinition of parameter s1, inherited method from Data does not constrain this parameter
			----------
			16. ERROR in nnbd_test1\\Test.java (at line 81)
				super.f(null, null); // warning on the second null expected
				              ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			17. ERROR in nnbd_test1\\Test.java (at line 85)
				public void g(String s1, String s2) { // warning on the second parameter expected
				                         ^^^^^^
			Illegal redefinition of parameter s2, inherited method from Data does not constrain this parameter
			----------
			18. ERROR in nnbd_test1\\Test.java (at line 86)
				super.g(null, null); // warning on the first null expected
				        ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			19. ERROR in nnbd_test1\\Test.java (at line 90)
				public void h(Supplier<String> s1, Supplier<String> s2) { // warning on the first parameter expected
				              ^^^^^^^^
			Illegal redefinition of parameter s1, inherited method from Data declares this parameter as \'@NonNull Supplier<String>\' (mismatching null constraints)
			----------
			"""
	);
}
public void testBug530913b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullByDefault");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_SECONDARY_NAMES, "annotation.NNBDField,annotation.NNBDParam,annotation.NNBDReturn,annotation.NNBDTypeArg,annotation.NNBDTypeBound");
	customOptions.put(JavaCore.COMPILER_PB_SUPPRESS_OPTIONAL_ERRORS, JavaCore.ENABLED);
	customOptions.put(JavaCore.COMPILER_PB_MISSING_OVERRIDE_ANNOTATION, JavaCore.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"annotation/DefaultLocation.java",
			"package annotation;\n" +
			"\n" +
			"public enum DefaultLocation {\n" +
			"    PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS\n" +
			"}\n" +
			"",
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullByDefault.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.FIELD;\n" +
			"import static annotation.DefaultLocation.PARAMETER;\n" +
			"import static annotation.DefaultLocation.RETURN_TYPE;\n" +
			"import static annotation.DefaultLocation.TYPE_ARGUMENT;\n" +
			"import static annotation.DefaultLocation.TYPE_BOUND;\n" +
			" \n" +
			"public @interface NonNullByDefault {\n" +
			"    DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD, TYPE_BOUND, TYPE_ARGUMENT };\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	Runner runner = new Runner();
	runner.classLibraries = this.LIBS;
	runner.shouldFlushOutputDirectory = false;
	runner.testFiles =
		new String[] {
			"test/X.java",
			"""
				package test;
				
				import annotation.*;
				
				interface C<T1, T2> {
				}
				
				abstract class X {
				    @NonNullByDefault(DefaultLocation.RETURN_TYPE) abstract void f2(@NonNullByDefault C<Object, ? extends Number> p1);
				}
				"""
		};
	runner.customOptions = customOptions;
	runner.javacTestOptions = new JavacTestOptions.SuppressWarnings("auxiliaryclass");
	runner.runConformTest();

	runner.testFiles =
		new String[] {
			"test/ExplicitNonNull.java",
			"package test;\n" +
			"\n" +
			"import annotation.*;\n" +
			"\n" +
			"\n" +
			"class ExplicitNonNull extends X {\n" +
			"    void f2(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1) {\n" +
			"    }\n" +
			"}\n" +
			"",
		};
	runner.runConformTest();
}
public void testBug530971() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullByDefault");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_SECONDARY_NAMES, "annotation.NNBDField,annotation.NNBDParam,annotation.NNBDReturn,annotation.NNBDTypeArg,annotation.NNBDTypeBound");
	customOptions.put(JavaCore.COMPILER_PB_MISSING_OVERRIDE_ANNOTATION, JavaCore.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"annotation/DefaultLocation.java",
			"package annotation;\n" +
			"\n" +
			"public enum DefaultLocation {\n" +
			"    PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS\n" +
			"}\n" +
			"",
			"annotation/NNBDField.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.FIELD;\n" +
			" \n" +
			"public @interface NNBDField {\n" +
			"	DefaultLocation[] value() default { FIELD };\n" +
			"}\n" +
			"",
			"annotation/NNBDParam.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.PARAMETER;\n" +
			"\n" +
			"public @interface NNBDParam {\n" +
			"    DefaultLocation[] value() default { PARAMETER };\n" +
			"}\n" +
			"",
			"annotation/NNBDReturn.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.RETURN_TYPE;\n" +
			"\n" +
			"public @interface NNBDReturn {\n" +
			"    DefaultLocation[] value() default { RETURN_TYPE };\n" +
			"}\n" +
			"",
			"annotation/NNBDTypeArg.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.TYPE_ARGUMENT;\n" +
			"\n" +
			"public @interface NNBDTypeArg {\n" +
			"    DefaultLocation[] value() default { TYPE_ARGUMENT };\n" +
			"}\n" +
			"",
			"annotation/NNBDTypeBound.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.TYPE_BOUND;\n" +
			"\n" +
			"public @interface NNBDTypeBound {\n" +
			"    DefaultLocation[] value() default { TYPE_BOUND };\n" +
			"}\n" +
			"",
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullByDefault.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.FIELD;\n" +
			"import static annotation.DefaultLocation.PARAMETER;\n" +
			"import static annotation.DefaultLocation.RETURN_TYPE;\n" +
			"import static annotation.DefaultLocation.TYPE_ARGUMENT;\n" +
			"import static annotation.DefaultLocation.TYPE_BOUND;\n" +
			" \n" +
			"public @interface NonNullByDefault {\n" +
			"    DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD, TYPE_BOUND, TYPE_ARGUMENT };\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	runNegativeTestWithLibs(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"import annotation.NNBDField;\n" +
			"import annotation.NNBDParam;\n" +
			"import annotation.NNBDReturn;\n" +
			"import annotation.NNBDTypeArg;\n" +
			"import annotation.NNBDTypeBound;\n" +
			"import annotation.NonNull;\n" +
			"\n" +
			"interface C<T1, T2> {\n" +
			"}\n" +
			"\n" +
			"abstract class X {\n" +
			"    @NNBDTypeArg\n" +
			"    @NNBDField\n" +
			"    @NNBDTypeBound\n" +
			"    C<Object, ? extends Number> f1; // warning 1\n" +
			"\n" +
			"    @NonNull\n" +
			"    C<@NonNull Object, ? extends @NonNull Number> f2; // warning 2\n" +
			"\n" +
			"    @NNBDTypeArg\n" +
			"    @NNBDReturn\n" +
			"    @NNBDParam\n" +
			"    @NNBDTypeBound\n" +
			"    abstract Object m1(C<Object, ? extends Number> p1, Object p2);\n" +
			"\n" +
			"    abstract @NNBDReturn Object m2(@NNBDParam @NNBDTypeArg @NNBDTypeBound C<Object, ? extends Number> p1,\n" +
			"            @NNBDParam Object p2);\n" +
			"\n" +
			"    abstract @NonNull Object m3(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2);\n" +
			"}\n" +
			"\n" +
			"class ExplicitNonNull extends X {\n" +
			"    Object m1(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2) { // warning 3 on return type\n" +
			"        f1 = null; // warning 4\n" +
			"        f2 = null; // warning 5\n" +
			"        f1 = p1;\n" +
			"        f2 = p1;\n" +
			"        return p2;\n" +
			"    }\n" +
			"\n" +
			"    Object m2(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2) { // warning 6 on return type\n" +
			"        f1 = p1;\n" +
			"        f2 = p1;\n" +
			"        return p2;\n" +
			"    }\n" +
			"\n" +
			"    Object m3(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2) { // warning 7 on return type\n" +
			"        f1 = p1;\n" +
			"        f2 = p1;\n" +
			"        return p2;\n" +
			"    }\n" +
			"}\n" +
			"\n" +
			"@NNBDParam\n" +
			"@NNBDTypeArg\n" +
			"@NNBDTypeBound\n" +
			"class OnClass extends X {\n" +
			"    Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 8 on return type\n" +
			"        f1 = null; // warning 9\n" +
			"        f2 = null; // warning 10\n" +
			"        f1 = p1;\n" +
			"        f2 = p1;\n" +
			"        return p2;\n" +
			"    }\n" +
			"\n" +
			"    Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 11 on return type\n" +
			"        f1 = p1;\n" +
			"        f2 = p1;\n" +
			"        return p2;\n" +
			"    }\n" +
			"\n" +
			"    Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 12 on return type\n" +
			"        f1 = p1;\n" +
			"        f2 = p1;\n" +
			"        return p2;\n" +
			"    }\n" +
			"}\n" +
			"\n" +
			"class Test {\n" +
			"    @NNBDParam\n" +
			"    @NNBDTypeArg\n" +
			"    @NNBDTypeBound\n" +
			"    X onField = new X() {\n" +
			"        Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 13 on return type\n" +
			"            f1 = null; // warning 14\n" +
			"            f2 = null; // warning 15\n" +
			"            f1 = p1;\n" +
			"            f2 = p1;\n" +
			"            return p2;\n" +
			"        }\n" +
			"\n" +
			"        Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 16 on return type\n" +
			"            f1 = p1;\n" +
			"            f2 = p1;\n" +
			"            return p2;\n" +
			"        }\n" +
			"\n" +
			"        Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 17 on return type\n" +
			"            f1 = p1;\n" +
			"            f2 = p1;\n" +
			"            return p2;\n" +
			"        }\n" +
			"    };\n" +
			"\n" +
			"    {\n" +
			"        @NNBDParam\n" +
			"        @NNBDTypeArg\n" +
			"        @NNBDTypeBound\n" +
			"        X onLocal = new X() {\n" +
			"            Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 18 on return type\n" +
			"                f1 = null; // warning 19\n" +
			"                f2 = null; // warning 20\n" +
			"                f1 = p1;\n" +
			"                f2 = p1;\n" +
			"                return p2;\n" +
			"            }\n" +
			"\n" +
			"            Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 21 on return type\n" +
			"                f1 = p1;\n" +
			"                f2 = p1;\n" +
			"                return p2;\n" +
			"            }\n" +
			"\n" +
			"            Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 22 on return type\n" +
			"                f1 = p1;\n" +
			"                f2 = p1;\n" +
			"                return p2;\n" +
			"            }\n" +
			"        };\n" +
			"    }\n" +
			"\n" +
			"    @NNBDParam\n" +
			"    @NNBDTypeArg\n" +
			"    @NNBDTypeBound\n" +
			"    void onMethod() {\n" +
			"        X l1 = new X() {\n" +
			"            Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 23 on return type\n" +
			"                f1 = null; // warning 24\n" +
			"                f2 = null; // warning 25\n" +
			"                f1 = p1;\n" +
			"                f2 = p1;\n" +
			"                return p2;\n" +
			"            }\n" +
			"\n" +
			"            Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 26 on return type\n" +
			"                f1 = p1;\n" +
			"                f2 = p1;\n" +
			"                return p2;\n" +
			"            }\n" +
			"\n" +
			"            Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 27 on return type\n" +
			"                f1 = p1;\n" +
			"                f2 = p1;\n" +
			"                return p2;\n" +
			"            }\n" +
			"        };\n" +
			"    }\n" +
			"}\n" +
			"",
		},
		customOptions,
		"""
			----------
			1. ERROR in test\\Test.java (at line 17)
				C<Object, ? extends Number> f1; // warning 1
				                            ^^
			The @NonNull field f1 may not have been initialized
			----------
			2. ERROR in test\\Test.java (at line 20)
				C<@NonNull Object, ? extends @NonNull Number> f2; // warning 2
				                                              ^^
			The @NonNull field f2 may not have been initialized
			----------
			3. ERROR in test\\Test.java (at line 35)
				Object m1(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2) { // warning 3 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m1(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			4. ERROR in test\\Test.java (at line 36)
				f1 = null; // warning 4
				     ^^^^
			Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null
			----------
			5. ERROR in test\\Test.java (at line 37)
				f2 = null; // warning 5
				     ^^^^
			Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null
			----------
			6. ERROR in test\\Test.java (at line 43)
				Object m2(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2) { // warning 6 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m2(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			7. ERROR in test\\Test.java (at line 49)
				Object m3(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2) { // warning 7 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m3(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			8. ERROR in test\\Test.java (at line 60)
				Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 8 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m1(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			9. ERROR in test\\Test.java (at line 61)
				f1 = null; // warning 9
				     ^^^^
			Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null
			----------
			10. ERROR in test\\Test.java (at line 62)
				f2 = null; // warning 10
				     ^^^^
			Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null
			----------
			11. ERROR in test\\Test.java (at line 68)
				Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 11 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m2(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			12. ERROR in test\\Test.java (at line 74)
				Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 12 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m3(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			13. ERROR in test\\Test.java (at line 86)
				Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 13 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m1(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			14. ERROR in test\\Test.java (at line 87)
				f1 = null; // warning 14
				     ^^^^
			Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null
			----------
			15. ERROR in test\\Test.java (at line 88)
				f2 = null; // warning 15
				     ^^^^
			Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null
			----------
			16. ERROR in test\\Test.java (at line 94)
				Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 16 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m2(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			17. ERROR in test\\Test.java (at line 100)
				Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 17 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m3(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			18. ERROR in test\\Test.java (at line 112)
				Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 18 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m1(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			19. ERROR in test\\Test.java (at line 113)
				f1 = null; // warning 19
				     ^^^^
			Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null
			----------
			20. ERROR in test\\Test.java (at line 114)
				f2 = null; // warning 20
				     ^^^^
			Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null
			----------
			21. ERROR in test\\Test.java (at line 120)
				Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 21 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m2(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			22. ERROR in test\\Test.java (at line 126)
				Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 22 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m3(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			23. ERROR in test\\Test.java (at line 139)
				Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 23 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m1(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			24. ERROR in test\\Test.java (at line 140)
				f1 = null; // warning 24
				     ^^^^
			Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null
			----------
			25. ERROR in test\\Test.java (at line 141)
				f2 = null; // warning 25
				     ^^^^
			Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null
			----------
			26. ERROR in test\\Test.java (at line 147)
				Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 26 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m2(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			27. ERROR in test\\Test.java (at line 153)
				Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 27 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m3(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			"""
	);
}

// same as testBug530971, but X is read via class file
public void testBug530971_BTB() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullByDefault");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_SECONDARY_NAMES, "annotation.NNBDField,annotation.NNBDParam,annotation.NNBDReturn,annotation.NNBDTypeArg,annotation.NNBDTypeBound");
	customOptions.put(JavaCore.COMPILER_PB_SUPPRESS_OPTIONAL_ERRORS, JavaCore.ENABLED);
	customOptions.put(JavaCore.COMPILER_PB_MISSING_OVERRIDE_ANNOTATION, JavaCore.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"annotation/DefaultLocation.java",
			"package annotation;\n" +
			"\n" +
			"public enum DefaultLocation {\n" +
			"    PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS\n" +
			"}\n" +
			"",
			"annotation/NNBDField.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.FIELD;\n" +
			" \n" +
			"public @interface NNBDField {\n" +
			"	DefaultLocation[] value() default { FIELD };\n" +
			"}\n" +
			"",
			"annotation/NNBDParam.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.PARAMETER;\n" +
			"\n" +
			"public @interface NNBDParam {\n" +
			"    DefaultLocation[] value() default { PARAMETER };\n" +
			"}\n" +
			"",
			"annotation/NNBDReturn.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.RETURN_TYPE;\n" +
			"\n" +
			"public @interface NNBDReturn {\n" +
			"    DefaultLocation[] value() default { RETURN_TYPE };\n" +
			"}\n" +
			"",
			"annotation/NNBDTypeArg.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.TYPE_ARGUMENT;\n" +
			"\n" +
			"public @interface NNBDTypeArg {\n" +
			"    DefaultLocation[] value() default { TYPE_ARGUMENT };\n" +
			"}\n" +
			"",
			"annotation/NNBDTypeBound.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.TYPE_BOUND;\n" +
			"\n" +
			"public @interface NNBDTypeBound {\n" +
			"    DefaultLocation[] value() default { TYPE_BOUND };\n" +
			"}\n" +
			"",
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullByDefault.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.FIELD;\n" +
			"import static annotation.DefaultLocation.PARAMETER;\n" +
			"import static annotation.DefaultLocation.RETURN_TYPE;\n" +
			"import static annotation.DefaultLocation.TYPE_ARGUMENT;\n" +
			"import static annotation.DefaultLocation.TYPE_BOUND;\n" +
			" \n" +
			"public @interface NonNullByDefault {\n" +
			"    DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD, TYPE_BOUND, TYPE_ARGUMENT };\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	runConformTestWithLibs(
		false,
		new String[] {
			"test/X.java",
			"""
				package test;
				
				import annotation.NNBDField;
				import annotation.NNBDParam;
				import annotation.NNBDReturn;
				import annotation.NNBDTypeArg;
				import annotation.NNBDTypeBound;
				import annotation.NonNull;
				
				interface C<T1, T2> {
				}
				
				@SuppressWarnings("null")
				abstract class X {
				    @NNBDTypeArg
				    @NNBDField
				    @NNBDTypeBound
				    C<Object, ? extends Number> f1; // warning 1
				
				    @NonNull
				    C<@NonNull Object, ? extends @NonNull Number> f2; // warning 2
				
				    @NNBDTypeArg
				    @NNBDReturn
				    @NNBDParam
				    @NNBDTypeBound
				    abstract Object m1(C<Object, ? extends Number> p1, Object p2);
				
				    abstract @NNBDReturn Object m2(@NNBDParam @NNBDTypeArg @NNBDTypeBound C<Object, ? extends Number> p1,
				            @NNBDParam Object p2);
				
				    abstract @NonNull Object m3(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2);
				}
				"""
		},
		customOptions,
		""
	);
	runNegativeTestWithLibs(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"import annotation.NNBDField;\n" +
			"import annotation.NNBDParam;\n" +
			"import annotation.NNBDReturn;\n" +
			"import annotation.NNBDTypeArg;\n" +
			"import annotation.NNBDTypeBound;\n" +
			"import annotation.NonNull;\n" +
			"\n" +
			"interface C_IGNORED<T1, T2> {\n" +
			"}\n" +
			"\n" +
			"abstract class X_IGNORED {\n" +
			"    @NNBDTypeArg\n" +
			"    @NNBDField\n" +
			"    @NNBDTypeBound\n" +
			"    C<Object, ? extends Number> f1; // warning 1\n" +
			"\n" +
			"    @NonNull\n" +
			"    C<@NonNull Object, ? extends @NonNull Number> f2; // warning 2\n" +
			"\n" +
			"    @NNBDTypeArg\n" +
			"    @NNBDReturn\n" +
			"    @NNBDParam\n" +
			"    @NNBDTypeBound\n" +
			"    abstract Object m1(C<Object, ? extends Number> p1, Object p2);\n" +
			"\n" +
			"    abstract @NNBDReturn Object m2(@NNBDParam @NNBDTypeArg @NNBDTypeBound C<Object, ? extends Number> p1,\n" +
			"            @NNBDParam Object p2);\n" +
			"\n" +
			"    abstract @NonNull Object m3(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2);\n" +
			"}\n" +
			"\n" +
			"class ExplicitNonNull extends X {\n" +
			"    Object m1(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2) { // warning 3 on return type\n" +
			"        f1 = null; // warning 4\n" +
			"        f2 = null; // warning 5\n" +
			"        f1 = p1;\n" +
			"        f2 = p1;\n" +
			"        return p2;\n" +
			"    }\n" +
			"\n" +
			"    Object m2(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2) { // warning 6 on return type\n" +
			"        f1 = p1;\n" +
			"        f2 = p1;\n" +
			"        return p2;\n" +
			"    }\n" +
			"\n" +
			"    Object m3(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2) { // warning 7 on return type\n" +
			"        f1 = p1;\n" +
			"        f2 = p1;\n" +
			"        return p2;\n" +
			"    }\n" +
			"}\n" +
			"\n" +
			"@NNBDParam\n" +
			"@NNBDTypeArg\n" +
			"@NNBDTypeBound\n" +
			"class OnClass extends X {\n" +
			"    Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 8 on return type\n" +
			"        f1 = null; // warning 9\n" +
			"        f2 = null; // warning 10\n" +
			"        f1 = p1;\n" +
			"        f2 = p1;\n" +
			"        return p2;\n" +
			"    }\n" +
			"\n" +
			"    Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 11 on return type\n" +
			"        f1 = p1;\n" +
			"        f2 = p1;\n" +
			"        return p2;\n" +
			"    }\n" +
			"\n" +
			"    Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 12 on return type\n" +
			"        f1 = p1;\n" +
			"        f2 = p1;\n" +
			"        return p2;\n" +
			"    }\n" +
			"}\n" +
			"\n" +
			"class Test {\n" +
			"    @NNBDParam\n" +
			"    @NNBDTypeArg\n" +
			"    @NNBDTypeBound\n" +
			"    X onField = new X() {\n" +
			"        Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 13 on return type\n" +
			"            f1 = null; // warning 14\n" +
			"            f2 = null; // warning 15\n" +
			"            f1 = p1;\n" +
			"            f2 = p1;\n" +
			"            return p2;\n" +
			"        }\n" +
			"\n" +
			"        Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 16 on return type\n" +
			"            f1 = p1;\n" +
			"            f2 = p1;\n" +
			"            return p2;\n" +
			"        }\n" +
			"\n" +
			"        Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 17 on return type\n" +
			"            f1 = p1;\n" +
			"            f2 = p1;\n" +
			"            return p2;\n" +
			"        }\n" +
			"    };\n" +
			"\n" +
			"    {\n" +
			"        @NNBDParam\n" +
			"        @NNBDTypeArg\n" +
			"        @NNBDTypeBound\n" +
			"        X onLocal = new X() {\n" +
			"            Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 18 on return type\n" +
			"                f1 = null; // warning 19\n" +
			"                f2 = null; // warning 20\n" +
			"                f1 = p1;\n" +
			"                f2 = p1;\n" +
			"                return p2;\n" +
			"            }\n" +
			"\n" +
			"            Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 21 on return type\n" +
			"                f1 = p1;\n" +
			"                f2 = p1;\n" +
			"                return p2;\n" +
			"            }\n" +
			"\n" +
			"            Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 22 on return type\n" +
			"                f1 = p1;\n" +
			"                f2 = p1;\n" +
			"                return p2;\n" +
			"            }\n" +
			"        };\n" +
			"    }\n" +
			"\n" +
			"    @NNBDParam\n" +
			"    @NNBDTypeArg\n" +
			"    @NNBDTypeBound\n" +
			"    void onMethod() {\n" +
			"        X l1 = new X() {\n" +
			"            Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 23 on return type\n" +
			"                f1 = null; // warning 24\n" +
			"                f2 = null; // warning 25\n" +
			"                f1 = p1;\n" +
			"                f2 = p1;\n" +
			"                return p2;\n" +
			"            }\n" +
			"\n" +
			"            Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 26 on return type\n" +
			"                f1 = p1;\n" +
			"                f2 = p1;\n" +
			"                return p2;\n" +
			"            }\n" +
			"\n" +
			"            Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 27 on return type\n" +
			"                f1 = p1;\n" +
			"                f2 = p1;\n" +
			"                return p2;\n" +
			"            }\n" +
			"        };\n" +
			"    }\n" +
			"}\n" +
			"",
		},
		customOptions,
		"""
			----------
			1. ERROR in test\\Test.java (at line 17)
				C<Object, ? extends Number> f1; // warning 1
				                            ^^
			The @NonNull field f1 may not have been initialized
			----------
			2. ERROR in test\\Test.java (at line 20)
				C<@NonNull Object, ? extends @NonNull Number> f2; // warning 2
				                                              ^^
			The @NonNull field f2 may not have been initialized
			----------
			3. ERROR in test\\Test.java (at line 35)
				Object m1(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2) { // warning 3 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m1(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			4. ERROR in test\\Test.java (at line 36)
				f1 = null; // warning 4
				     ^^^^
			Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null
			----------
			5. ERROR in test\\Test.java (at line 37)
				f2 = null; // warning 5
				     ^^^^
			Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null
			----------
			6. ERROR in test\\Test.java (at line 43)
				Object m2(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2) { // warning 6 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m2(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			7. ERROR in test\\Test.java (at line 49)
				Object m3(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2) { // warning 7 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m3(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			8. ERROR in test\\Test.java (at line 60)
				Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 8 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m1(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			9. ERROR in test\\Test.java (at line 61)
				f1 = null; // warning 9
				     ^^^^
			Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null
			----------
			10. ERROR in test\\Test.java (at line 62)
				f2 = null; // warning 10
				     ^^^^
			Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null
			----------
			11. ERROR in test\\Test.java (at line 68)
				Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 11 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m2(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			12. ERROR in test\\Test.java (at line 74)
				Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 12 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m3(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			13. ERROR in test\\Test.java (at line 86)
				Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 13 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m1(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			14. ERROR in test\\Test.java (at line 87)
				f1 = null; // warning 14
				     ^^^^
			Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null
			----------
			15. ERROR in test\\Test.java (at line 88)
				f2 = null; // warning 15
				     ^^^^
			Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null
			----------
			16. ERROR in test\\Test.java (at line 94)
				Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 16 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m2(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			17. ERROR in test\\Test.java (at line 100)
				Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 17 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m3(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			18. ERROR in test\\Test.java (at line 112)
				Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 18 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m1(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			19. ERROR in test\\Test.java (at line 113)
				f1 = null; // warning 19
				     ^^^^
			Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null
			----------
			20. ERROR in test\\Test.java (at line 114)
				f2 = null; // warning 20
				     ^^^^
			Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null
			----------
			21. ERROR in test\\Test.java (at line 120)
				Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 21 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m2(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			22. ERROR in test\\Test.java (at line 126)
				Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 22 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m3(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			23. ERROR in test\\Test.java (at line 139)
				Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 23 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m1(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			24. ERROR in test\\Test.java (at line 140)
				f1 = null; // warning 24
				     ^^^^
			Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null
			----------
			25. ERROR in test\\Test.java (at line 141)
				f2 = null; // warning 25
				     ^^^^
			Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null
			----------
			26. ERROR in test\\Test.java (at line 147)
				Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 26 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m2(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			27. ERROR in test\\Test.java (at line 153)
				Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 27 on return type
				^^^^^^
			The return type is incompatible with \'@NonNull Object\' returned from X.m3(C<Object,? extends Number>, Object) (mismatching null constraints)
			----------
			"""
	);
}
public void testBug530971_redundant() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullByDefault");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_SECONDARY_NAMES, "annotation.NNBDField,annotation.NNBDParam,annotation.NNBDReturn,annotation.NNBDTypeArg,annotation.NNBDTypeBound");
	customOptions.put(JavaCore.COMPILER_PB_MISSING_OVERRIDE_ANNOTATION, JavaCore.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"annotation/DefaultLocation.java",
			"package annotation;\n" +
			"\n" +
			"public enum DefaultLocation {\n" +
			"    PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS\n" +
			"}\n" +
			"",
			"annotation/NNBDField.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.FIELD;\n" +
			" \n" +
			"public @interface NNBDField {\n" +
			"	DefaultLocation[] value() default { FIELD };\n" +
			"}\n" +
			"",
			"annotation/NNBDParam.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.PARAMETER;\n" +
			"\n" +
			"public @interface NNBDParam {\n" +
			"    DefaultLocation[] value() default { PARAMETER };\n" +
			"}\n" +
			"",
			"annotation/NNBDReturn.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.RETURN_TYPE;\n" +
			"\n" +
			"public @interface NNBDReturn {\n" +
			"    DefaultLocation[] value() default { RETURN_TYPE };\n" +
			"}\n" +
			"",
			"annotation/NNBDTypeArg.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.TYPE_ARGUMENT;\n" +
			"\n" +
			"public @interface NNBDTypeArg {\n" +
			"    DefaultLocation[] value() default { TYPE_ARGUMENT };\n" +
			"}\n" +
			"",
			"annotation/NNBDTypeBound.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.TYPE_BOUND;\n" +
			"\n" +
			"public @interface NNBDTypeBound {\n" +
			"    DefaultLocation[] value() default { TYPE_BOUND };\n" +
			"}\n" +
			"",
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullByDefault.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.FIELD;\n" +
			"import static annotation.DefaultLocation.PARAMETER;\n" +
			"import static annotation.DefaultLocation.RETURN_TYPE;\n" +
			"import static annotation.DefaultLocation.TYPE_ARGUMENT;\n" +
			"import static annotation.DefaultLocation.TYPE_BOUND;\n" +
			" \n" +
			"public @interface NonNullByDefault {\n" +
			"    DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD, TYPE_BOUND, TYPE_ARGUMENT };\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	Runner runner = new Runner();
	runner.shouldFlushOutputDirectory = false;
	runner.testFiles =
		new String[] {
			"test/X.java",
			"package test;\n" +
			"\n" +
			"import annotation.*;\n" +
			"\n" +
			"@NNBDReturn\n" +
			"@NNBDParam\n" +
			"@NNBDField\n" +
			"abstract class X {\n" +
			"    @NNBDReturn\n" +
			"    @NNBDParam\n" +
			"    @NNBDField // warning 1\n" +
			"    abstract class OnClass {\n" +
			"    }\n" +
			"\n" +
			"    @NNBDReturn\n" +
			"    @NNBDParam\n" +
			"    @NNBDField // warning 2\n" +
			"    Object onField = \"\";\n" +
			"\n" +
			"    {\n" +
			"        @NNBDReturn\n" +
			"        @NNBDParam\n" +
			"        @NNBDField // warning 3\n" +
			"        Object onLocal;\n" +
			"    }\n" +
			"\n" +
			"    @NNBDReturn\n" +
			"    @NNBDParam\n" +
			"    @NNBDField // warning 4\n" +
			"    abstract void onMethod();\n" +
			"\n" +
			"    abstract void m(//\n" +
			"            @NNBDReturn //\n" +
			"            @NNBDParam //\n" +
			"            @NNBDField // warning 5\n" +
			"            Object onParameter);\n" +
			"}\n" +
			"",
		};
	runner.customOptions = customOptions;
	runner.expectedCompilerLog =
		"""
			----------
			1. WARNING in test\\X.java (at line 11)
				@NNBDField // warning 1
				^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing type X
			----------
			2. WARNING in test\\X.java (at line 17)
				@NNBDField // warning 2
				^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing type X
			----------
			3. WARNING in test\\X.java (at line 23)
				@NNBDField // warning 3
				^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing type X
			----------
			4. WARNING in test\\X.java (at line 29)
				@NNBDField // warning 4
				^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing type X
			----------
			5. WARNING in test\\X.java (at line 35)
				@NNBDField // warning 5
				^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing type X
			----------
			""";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
public void testBug530971_locally_redundant() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullByDefault");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_SECONDARY_NAMES, "annotation.NNBDField,annotation.NNBDParam,annotation.NNBDReturn,annotation.NNBDTypeArg,annotation.NNBDTypeBound");
	customOptions.put(JavaCore.COMPILER_PB_MISSING_OVERRIDE_ANNOTATION, JavaCore.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"annotation/DefaultLocation.java",
			"package annotation;\n" +
			"\n" +
			"public enum DefaultLocation {\n" +
			"    PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS\n" +
			"}\n" +
			"",
			"annotation/NNBDField.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.FIELD;\n" +
			" \n" +
			"public @interface NNBDField {\n" +
			"	DefaultLocation[] value() default { FIELD };\n" +
			"}\n" +
			"",
			"annotation/NNBDParam.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.PARAMETER;\n" +
			"\n" +
			"public @interface NNBDParam {\n" +
			"    DefaultLocation[] value() default { PARAMETER };\n" +
			"}\n" +
			"",
			"annotation/NNBDReturn.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.RETURN_TYPE;\n" +
			"\n" +
			"public @interface NNBDReturn {\n" +
			"    DefaultLocation[] value() default { RETURN_TYPE };\n" +
			"}\n" +
			"",
			"annotation/NNBDTypeArg.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.TYPE_ARGUMENT;\n" +
			"\n" +
			"public @interface NNBDTypeArg {\n" +
			"    DefaultLocation[] value() default { TYPE_ARGUMENT };\n" +
			"}\n" +
			"",
			"annotation/NNBDTypeBound.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.TYPE_BOUND;\n" +
			"\n" +
			"public @interface NNBDTypeBound {\n" +
			"    DefaultLocation[] value() default { TYPE_BOUND };\n" +
			"}\n" +
			"",
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullByDefault.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.FIELD;\n" +
			"import static annotation.DefaultLocation.PARAMETER;\n" +
			"import static annotation.DefaultLocation.RETURN_TYPE;\n" +
			"import static annotation.DefaultLocation.TYPE_ARGUMENT;\n" +
			"import static annotation.DefaultLocation.TYPE_BOUND;\n" +
			" \n" +
			"public @interface NonNullByDefault {\n" +
			"    DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD, TYPE_BOUND, TYPE_ARGUMENT };\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	runConformTestWithLibs(
		false,
		new String[] {
			"testredundant/TestRedundantOnSame.java",
			"package testredundant;\n" +
			"\n" +
			"import annotation.DefaultLocation;\n" +
			"import annotation.NNBDField;\n" +
			"import annotation.NNBDParam;\n" +
			"import annotation.NNBDReturn;\n" +
			"import annotation.NonNullByDefault;\n" +
			"\n" +
			"@NNBDReturn\n" +
			"@NNBDParam\n" +
			"@NNBDField\n" +
			"@NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER, DefaultLocation.FIELD })\n" +
			"abstract class TestRedundantOnSame {\n" +
			"    @NNBDReturn\n" +
			"    @NNBDParam\n" +
			"    @NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })\n" +
			"    abstract class OnClass {\n" +
			"    }\n" +
			"\n" +
			"    @NNBDReturn\n" +
			"    @NNBDParam\n" +
			"    @NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })\n" +
			"    Object onField = \"\";\n" +
			"\n" +
			"    {\n" +
			"        @NNBDReturn\n" +
			"        @NNBDParam\n" +
			"        @NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })\n" +
			"        Object onLocal;\n" +
			"    }\n" +
			"\n" +
			"    @NNBDReturn\n" +
			"    @NNBDParam\n" +
			"    @NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })\n" +
			"    abstract void onMethod();\n" +
			"\n" +
			"    abstract void m(//\n" +
			"            @NNBDReturn //\n" +
			"            @NNBDParam //\n" +
			"            @NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER }) //\n" +
			"            Object onParameter);\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
}
public void testBug518839() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullApi");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_SECONDARY_NAMES, "annotation.NonNullFields");
	customOptions.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.IGNORE);

	runConformTestWithLibs(
		new String[] {
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullApi.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"\n" +
			"@TypeQualifierDefault({ElementType.METHOD, ElementType.PARAMETER})\n" +
			"public @interface NonNullApi {\n" +
			"}\n" +
			"",
			"annotation/NonNullFields.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"\n" +
			"@TypeQualifierDefault(ElementType.FIELD)\n" +
			"public @interface NonNullFields {\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
			"annotation/TypeQualifierDefault.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"\n" +
			"public @interface TypeQualifierDefault {\n" +
			"    ElementType[] value();\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	runNegativeTestWithLibs(
		new String[] {
			"nn_api/NNApi.java",
			"package nn_api;\n" +
			"\n" +
			"public class NNApi {\n" +
			"    public String f;\n" +
			"\n" +
			"    public Object m(Object p) {\n" +
			"        if (p != null) { // warning\n" +
			"            //\n" +
			"        }\n" +
			"        return null; // warning\n" +
			"    }\n" +
			"}\n" +
			"",
			"nn_api/package-info.java",
			"@annotation.NonNullApi\n" +
			"package nn_api;\n" +
			"",
			"nn_api_and_fields/NNApiAndFields.java",
			"package nn_api_and_fields;\n" +
			"\n" +
			"public class NNApiAndFields {\n" +
			"    public String f; // warning 1\n" +
			"\n" +
			"    public Object m(Object p) {\n" +
			"        if (p != null) { // warning 2\n" +
			"            //\n" +
			"        }\n" +
			"        return null; // warning 3\n" +
			"    }\n" +
			"}\n" +
			"",
			"nn_api_and_fields/package-info.java",
			"@annotation.NonNullApi\n" +
			"@annotation.NonNullFields\n" +
			"package nn_api_and_fields;\n" +
			"",
			"nn_fields/NNFields.java",
			"package nn_fields;\n" +
			"\n" +
			"public class NNFields {\n" +
			"    public String f; // warning\n" +
			"\n" +
			"    public Object m(Object p) {\n" +
			"        if (p != null) {\n" +
			"            //\n" +
			"        }\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n" +
			"",
			"nn_fields/package-info.java",
			"@annotation.NonNullFields\n" +
			"package nn_fields;\n" +
			"",
		},
		customOptions,
		"""
			----------
			1. ERROR in nn_api\\NNApi.java (at line 7)
				if (p != null) { // warning
				    ^
			Redundant null check: The variable p is specified as @NonNull
			----------
			2. ERROR in nn_api\\NNApi.java (at line 10)
				return null; // warning
				       ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			----------
			1. ERROR in nn_api_and_fields\\NNApiAndFields.java (at line 4)
				public String f; // warning 1
				              ^
			The @NonNull field f may not have been initialized
			----------
			2. ERROR in nn_api_and_fields\\NNApiAndFields.java (at line 7)
				if (p != null) { // warning 2
				    ^
			Redundant null check: The variable p is specified as @NonNull
			----------
			3. ERROR in nn_api_and_fields\\NNApiAndFields.java (at line 10)
				return null; // warning 3
				       ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			----------
			1. ERROR in nn_fields\\NNFields.java (at line 4)
				public String f; // warning
				              ^
			The @NonNull field f may not have been initialized
			----------
			"""
	);
}
public void testBug518839_BTB() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullApi");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_SECONDARY_NAMES, "annotation.NonNullFields");
	customOptions.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.IGNORE);

	runConformTestWithLibs(
		new String[] {
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullApi.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"\n" +
			"@TypeQualifierDefault({ElementType.METHOD, ElementType.PARAMETER})\n" +
			"public @interface NonNullApi {\n" +
			"}\n" +
			"",
			"annotation/NonNullFields.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"\n" +
			"@TypeQualifierDefault(ElementType.FIELD)\n" +
			"public @interface NonNullFields {\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
			"annotation/TypeQualifierDefault.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"\n" +
			"public @interface TypeQualifierDefault {\n" +
			"    ElementType[] value();\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	// compile with jdt annotations, so no warnings are created.
	runConformTestWithLibs(
		false,
		new String[] {
			"nn_api/NNApi.java",
			"package nn_api;\n" +
			"\n" +
			"public class NNApi {\n" +
			"    public String f;\n" +
			"\n" +
			"    public Object m(Object p) {\n" +
			"        if (p != null) { // warning\n" +
			"            //\n" +
			"        }\n" +
			"        return null; // warning\n" +
			"    }\n" +
			"}\n" +
			"",
			"nn_api/package-info.java",
			"@annotation.NonNullApi\n" +
			"package nn_api;\n" +
			"",
			"nn_api_and_fields/NNApiAndFields.java",
			"package nn_api_and_fields;\n" +
			"\n" +
			"public class NNApiAndFields {\n" +
			"    public String f; // warning 1\n" +
			"\n" +
			"    public Object m(Object p) {\n" +
			"        if (p != null) { // warning 2\n" +
			"            //\n" +
			"        }\n" +
			"        return null; // warning 3\n" +
			"    }\n" +
			"}\n" +
			"",
			"nn_api_and_fields/package-info.java",
			"@annotation.NonNullApi\n" +
			"@annotation.NonNullFields\n" +
			"package nn_api_and_fields;\n" +
			"",
			"nn_fields/NNFields.java",
			"package nn_fields;\n" +
			"\n" +
			"public class NNFields {\n" +
			"    public String f; // warning\n" +
			"\n" +
			"    public Object m(Object p) {\n" +
			"        if (p != null) {\n" +
			"            //\n" +
			"        }\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n" +
			"",
			"nn_fields/package-info.java",
			"@annotation.NonNullFields\n" +
			"package nn_fields;\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runNegativeTestWithLibs(
		new String[] {
			"btbtest/BTBTest.java",
			"package btbtest;\n" +
			"\n" +
			"import nn_api.NNApi;\n" +
			"import nn_api_and_fields.NNApiAndFields;\n" +
			"import nn_fields.NNFields;\n" +
			"\n" +
			"public class BTBTest {\n" +
			"    void api(NNApi p) {\n" +
			"        if (p.m(null) == null) { // 2 warnings\n" +
			"        }\n" +
			"        p.f = null;\n" +
			"    }\n" +
			"\n" +
			"    void apiAndFields(NNApiAndFields p) {\n" +
			"        if (p.m(null) == null) { // 2 warnings\n" +
			"        }\n" +
			"        p.f = null; // warning\n" +
			"    }\n" +
			"\n" +
			"    void fields(NNFields p) {\n" +
			"        if (p.m(null) == null) {\n" +
			"        }\n" +
			"        p.f = null; // warning\n" +
			"    }\n" +
			"}\n" +
			"",
		},
		customOptions,
		"""
			----------
			1. ERROR in btbtest\\BTBTest.java (at line 9)
				if (p.m(null) == null) { // 2 warnings
				    ^^^^^^^^^
			Null comparison always yields false: The method m(Object) cannot return null
			----------
			2. ERROR in btbtest\\BTBTest.java (at line 9)
				if (p.m(null) == null) { // 2 warnings
				        ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			3. ERROR in btbtest\\BTBTest.java (at line 15)
				if (p.m(null) == null) { // 2 warnings
				    ^^^^^^^^^
			Null comparison always yields false: The method m(Object) cannot return null
			----------
			4. ERROR in btbtest\\BTBTest.java (at line 15)
				if (p.m(null) == null) { // 2 warnings
				        ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			5. ERROR in btbtest\\BTBTest.java (at line 17)
				p.f = null; // warning
				      ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			6. ERROR in btbtest\\BTBTest.java (at line 23)
				p.f = null; // warning
				      ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			"""
	);
}
public void testBug531040() {
	if (this.complianceLevel < ClassFileConstants.JDK10)
		return;
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"import java.util.*;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"public class Test {\n" +
			"	void test() {\n" +
			"		var list1 = new ArrayList<@NonNull String>();\n" +
			"		list1.add(null);\n" +
			"		@NonNull String val = \"\";\n" +
			"		var list2 = getList(val);\n" +
			"		list2.add(null);\n" +
			"	}\n" +
			"	<T> List<T> getList(T... in) {\n" +
			"		return Arrays.asList(in);\n" +
			"	}\n" +
			"}\n" +
			""
		},
		"""
			----------
			1. ERROR in Test.java (at line 8)
				list1.add(null);
				          ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			2. ERROR in Test.java (at line 11)
				list2.add(null);
				          ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			3. WARNING in Test.java (at line 13)
				<T> List<T> getList(T... in) {
				                         ^^
			Type safety: Potential heap pollution via varargs parameter in
			----------
			"""
	);
}
public void testBug533339() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Test.java",
			"""
				import org.eclipse.jdt.annotation.NonNull;
				import org.eclipse.jdt.annotation.Nullable;
				
				public class Test {
				
					interface Foo {
				
						@Nullable
						String getString();
					}
				
					class Bar {
				
						Bar(@NonNull String s) {
						}
					}
				
					Bar hasWarning(Foo foo) {
						@NonNull String s = checkNotNull(foo.getString());
						return new Bar(s);// Null type mismatch: required '@NonNull String' but the provided value is inferred as @Nullable
					}
				
					Bar hasNoWarning(Foo foo) {
						return new Bar(checkNotNull(foo.getString()));// no warning when s is inlined
					}
					static <T> T checkNotNull(T reference) {
						if (reference == null) throw new NullPointerException();
						return reference;
					}
				}
				"""
		};
	runner.classLibraries = this.LIBS;
	runner.expectedCompilerLog =
		"""
			----------
			1. WARNING in Test.java (at line 19)
				@NonNull String s = checkNotNull(foo.getString());
				                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'@NonNull String\'
			----------
			2. WARNING in Test.java (at line 24)
				return new Bar(checkNotNull(foo.getString()));// no warning when s is inlined
				               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'@NonNull String\'
			----------
			""";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
public void testBug534516() {
	runConformTestWithLibs(
			new String[] {
				"testbug/nullannotations/Utility.java",
				"package testbug.nullannotations;\n" +
				"\n" +
				"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
				"\n" +
				"@NonNullByDefault\n" +
				"public class Utility {\n" +
				"\n" +
				"	public static String massageString(final String input) {\n" +
				"		return input + \" .\";\n" +
				"	}\n" +
				"\n" +
				"	private Utility() {\n" +
				"\n" +
				"	}\n" +
				"\n" +
				"}\n" +
				"",
			},
			getCompilerOptions(),
			""
		);
	runConformTestWithLibs(
			false,
			new String[] {
				"testbug/nullannotations/ApplyIfNonNullElseGetBugDemo.java",
				"package testbug.nullannotations;\n" +
				"\n" +
				"import java.util.function.Function;\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"import org.eclipse.jdt.annotation.Nullable;\n" +
				"\n" +
				"public class ApplyIfNonNullElseGetBugDemo {\n" +
				"\n" +
				"	public static <T, U> U applyIfNonNullElse(@Nullable T value, @NonNull Function<@NonNull ? super T, ? extends U> function, U fallbackValue) {\n" +
				"		if (value != null)\n" +
				"			return function.apply(value);\n" +
				"		return fallbackValue;\n" +
				"	}\n" +
				"\n" +
				"	public static void main(final @Nullable String[] args) {\n" +
				"		final @Nullable String arg = args.length == 0 ? null : args[0];\n" +
				"		System.out.println(applyIfNonNullElse(arg, Utility::massageString, \"\")); // incorrect warning here\n" +
				"	}\n" +
				"\n" +
				"}\n" +
				"",
			},
			getCompilerOptions(),
			""
		);
}
public void testBug536459() {
	runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.Nullable;
					
					public class X {
					    static void x() {
					        @Nullable String x1 = "";
					        @Nullable String[] x2 = { "" };
					    }
					}
					"""
			},
			getCompilerOptions(),
			"");
}
public void testBug536555() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
	runner.testFiles =
			new String[] {
				"Foo.java",
				"""
					public class Foo
					{
						/** Test {@link #foo(boolean)}. */
						public static final String TEST = "foo";
					
						public void foo(@SuppressWarnings(TEST) final boolean test)
						{
							System.out.println(test);
						}
					}
					"""
			};
	runner.expectedCompilerLog =
			"""
				----------
				1. WARNING in Foo.java (at line 6)
					public void foo(@SuppressWarnings(TEST) final boolean test)
					                                  ^^^^
				Unsupported @SuppressWarnings("foo")
				----------
				""";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
public void testBug540264() {
	runNegativeTest(
		true,
		new String[] {
			"example/Example.java",
			"package example;\n" +
			"\n" +
			"public abstract class Example {\n" +
			"    void f() {\n" +
			"        for (X.Y<Z> entry : x) {\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"",
		},
		this.LIBS,
		getCompilerOptions(),
		"""
			----------
			1. ERROR in example\\Example.java (at line 5)
				for (X.Y<Z> entry : x) {
				     ^
			X cannot be resolved to a type
			----------
			2. ERROR in example\\Example.java (at line 5)
				for (X.Y<Z> entry : x) {
				         ^
			Z cannot be resolved to a type
			----------
			3. ERROR in example\\Example.java (at line 5)
				for (X.Y<Z> entry : x) {
				                    ^
			x cannot be resolved to a variable
			----------
			""",
		JavacTestOptions.DEFAULT
	);
}
public void testBug542707_1() {
	if (!checkPreviewAllowed()) return; // switch expression
	// switch expression has a functional type with interesting type inference and various null issues:
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
	runner.customOptions.put(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
	runner.classLibraries = this.LIBS;
	runner.testFiles = new String[] {
		"X.java",
		"""
			import org.eclipse.jdt.annotation.*;
			import java.util.function.*;
			interface IN0 {}
			interface IN1 extends IN0 {}
			interface IN2 extends IN0 {}
			public class X {
				@NonNull IN1 n1() { return new IN1() {}; }
				IN2 n2() { return null; }
				<M> void m(@NonNull Supplier<@NonNull M> m2) { }
				void testSw(int i) {
					m(switch(i) {
						case 1 -> this::n1;
						case 2 -> () -> n1();
						case 3 -> null;
						case 4 -> () -> n2();
						default -> this::n2; });
				}
			}
			"""
	};
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in X.java (at line 14)
					case 3 -> null;
					          ^^^^
				Null type mismatch: required \'@NonNull Supplier<@NonNull IN0>\' but the provided value is null
				----------
				2. WARNING in X.java (at line 15)
					case 4 -> () -> n2();
					                ^^^^
				Null type safety (type annotations): The expression of type \'IN2\' needs unchecked conversion to conform to \'@NonNull IN0\'
				----------
				3. WARNING in X.java (at line 16)
					default -> this::n2; });
					           ^^^^^^^^
				Null type safety at method return type: Method descriptor Supplier<IN0>.get() promises \'@NonNull IN0\' but referenced method provides \'IN2\'
				----------
				""";
	runner.runNegativeTest();
}
public void testBug499714() {
	runNegativeTestWithLibs(
		new String[] {
			"Type.java",
			"""
				import org.eclipse.jdt.annotation.*;
				
				interface Type<@Nullable K> {
				    K get();
				
				    static <@Nullable T> void x(Type<T> t) {
				        t.get().toString();
				    }
				}"""
		},
		"""
			----------
			1. ERROR in Type.java (at line 7)
				t.get().toString();
				^^^^^^^
			Potential null pointer access: The method get() may return null
			----------
			""");
}
public void testBug482242_simple() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_ANNOTATED_TYPE_ARGUMENT_TO_UNANNOTATED, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"""
				import java.util.*;
				import org.eclipse.jdt.annotation.*;
				public class Test {
				    static void dangerous(List<String> list) {
				        list.add(null);
				    }
				    public static void main(String[] args) {
				        List<@NonNull String> l = new ArrayList<>();
				        dangerous(l);
				        for (String string : l)
				            System.out.println(string.toLowerCase());
				    }
				}
				"""
		},
		options,
		"""
			----------
			1. ERROR in Test.java (at line 9)
				dangerous(l);
				          ^
			Unsafe null type conversion (type annotations): The value of type \'List<@NonNull String>\' is made accessible using the less-annotated type \'List<String>\'
			----------
			""");
}
public void testBug482242_intermediate() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_ANNOTATED_TYPE_ARGUMENT_TO_UNANNOTATED, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"""
				import java.util.*;
				import org.eclipse.jdt.annotation.*;
				
				public class Test {
				    public static void main(String[] args) {
				        ArrayList<@NonNull String> list = new ArrayList<>();
				        collect(list, null);
				        for (String s : list)
				            System.out.println(s.toUpperCase());
				    }
				    static void collect(List<@NonNull String> list, String string) {
				        list.add(string);     // (1)
				        insert(list, string); // (2)
				    }
				    static void insert(List<? super String> l, String s) {
				        l.add(s);
				    }
				}
				"""
		},
		options,
		"""
			----------
			1. WARNING in Test.java (at line 12)
				list.add(string);     // (1)
				         ^^^^^^
			Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'@NonNull String\'
			----------
			2. ERROR in Test.java (at line 13)
				insert(list, string); // (2)
				       ^^^^
			Unsafe null type conversion (type annotations): The value of type \'List<@NonNull String>\' is made accessible using the less-annotated type \'List<? super String>\'
			----------
			""");
}
public void testBug482242_annotatedTypeVariable() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_ANNOTATED_TYPE_ARGUMENT_TO_UNANNOTATED, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"""
				import org.eclipse.jdt.annotation.*;
				
				interface List<T extends @NonNull Object> {
					void add(T elem);\
				}
				public class Test {
				    public static void test(List<@NonNull String> list) {
				        collect(list, null);
				    }
				    static void collect(List<@NonNull String> list, String string) {
				        insert(list, string);
				    }
				    static void insert(List<? super String> l, String s) {
				        l.add(s);
				    }
				}
				"""
		},
		options,
		"""
			----------
			1. ERROR in Test.java (at line 12)
				static void insert(List<? super String> l, String s) {
				                        ^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'? super String\' is not a valid substitute for the type parameter \'T extends @NonNull Object\'
			----------
			2. WARNING in Test.java (at line 13)
				l.add(s);
				      ^
			Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'capture#of ? super String\'
			----------
			""");
}
public void testBug482242_boundedWildcard() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_ANNOTATED_TYPE_ARGUMENT_TO_UNANNOTATED, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"""
				import org.eclipse.jdt.annotation.*;
				
				interface List<T extends @NonNull Object> {
					void add(T elem);\
				}
				public class Test {
				    public static void test(List<@NonNull String> list) {
				        collect(list, null);
				    }
				    static void collect(List<@NonNull String> list, String string) {
				        insert(list, string);
				    }
				    static void insert(List<? super @Nullable String> l, String s) {
				        l.add(s);
				    }
				}
				"""
		},
		options,
		"""
			----------
			1. ERROR in Test.java (at line 10)
				insert(list, string);
				       ^^^^
			Null type mismatch (type annotations): required \'List<? super @Nullable String>\' but this expression has type \'List<@NonNull String>\'
			----------
			2. ERROR in Test.java (at line 12)
				static void insert(List<? super @Nullable String> l, String s) {
				                        ^^^^^^^^^^^^^^^^^^^^^^^^
			Null constraint mismatch: The type \'? super @Nullable String\' is not a valid substitute for the type parameter \'T extends @NonNull Object\'
			----------
			3. WARNING in Test.java (at line 13)
				l.add(s);
				      ^
			Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'capture#of ? super @Nullable String\'
			----------
			""");
}
public void testBug560213source() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.classLibraries = this.LIBS;
	runner.testFiles = new String[] {
		"nullEnumSort/MyEnum.java",
		"""
			package nullEnumSort;
			
			import org.eclipse.jdt.annotation.NonNullByDefault;
			
			@NonNullByDefault
			enum MyEnum {
			    x
			}
			""",
		"nullEnumSort/EnumProblem.java",
		"""
			package nullEnumSort;
			
			import java.util.Collections;
			import java.util.List;
			
			import org.eclipse.jdt.annotation.NonNullByDefault;
			
			@NonNullByDefault
			public class EnumProblem {
			    void f(List<MyEnum> list) {
			        Collections.sort(list);
			    }
			
			}"""
	};
	runner.runConformTest();
}
public void testBug560213binary() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.testFiles = new String[] {
		"nullEnumSort/MyEnum.java",
		"""
			package nullEnumSort;
			
			import org.eclipse.jdt.annotation.NonNullByDefault;
			
			@NonNullByDefault
			enum MyEnum {
			    x
			}
			"""
	};
	runner.classLibraries = this.LIBS;
	runner.runConformTest();

	runner.shouldFlushOutputDirectory = false;
	runner.testFiles = new String[] {
		"nullEnumSort/EnumProblem.java",
		"""
			package nullEnumSort;
			
			import java.util.Collections;
			import java.util.List;
			
			import org.eclipse.jdt.annotation.NonNullByDefault;
			
			@NonNullByDefault
			public class EnumProblem {
			    void f(List<MyEnum> list) {
			        Collections.sort(list);
			    }
			
			}"""
	};
	runner.runConformTest();
}
public void testBug560310() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.testFiles =
		new String[] {
			"confusing/Confusing.java",
			"""
				package confusing;
				
				import java.util.ArrayList;
				
				import org.eclipse.jdt.annotation.NonNullByDefault;
				
				public abstract class Confusing {
				    abstract int unannotated(ArrayList<String> list);
				
				    @NonNullByDefault
				    public void f(boolean x) {
				        ArrayList<String> list = x ? null : new ArrayList<>();
				
				        while (true) {
				            unannotated(list);
				        }
				    }
				}
				"""
		};
	runner.classLibraries = this.LIBS;
	runner.javacTestOptions = Excuse.EclipseHasSomeMoreWarnings;
	runner.expectedCompilerLog =
		"""
			----------
			1. INFO in confusing\\Confusing.java (at line 15)
				unannotated(list);
				            ^^^^
			Unsafe null type conversion (type annotations): The value of type \'ArrayList<@NonNull String>\' is made accessible using the less-annotated type \'ArrayList<String>\'
			----------
			""";
	runner.runWarningTest();
}
public void testBug560310try_finally() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.testFiles =
		new String[] {
			"confusing/Confusing.java",
			"""
				package confusing;
				
				import java.util.ArrayList;
				
				import org.eclipse.jdt.annotation.NonNullByDefault;
				
				public abstract class Confusing {
				    abstract int unannotated(ArrayList<String> list);
				
				    @NonNullByDefault
				    public void f(boolean x) {
				        ArrayList<String> list = x ? null : new ArrayList<>();
				
				        try {
				            unannotated(list);
				        } finally {
				            unannotated(list);
				        }
				    }
				}
				"""
		};
	runner.classLibraries = this.LIBS;
	runner.javacTestOptions = Excuse.EclipseHasSomeMoreWarnings;
	runner.expectedCompilerLog =
		"""
			----------
			1. INFO in confusing\\Confusing.java (at line 15)
				unannotated(list);
				            ^^^^
			Unsafe null type conversion (type annotations): The value of type \'ArrayList<@NonNull String>\' is made accessible using the less-annotated type \'ArrayList<String>\'
			----------
			2. INFO in confusing\\Confusing.java (at line 17)
				unannotated(list);
				            ^^^^
			Unsafe null type conversion (type annotations): The value of type \'ArrayList<@NonNull String>\' is made accessible using the less-annotated type \'ArrayList<String>\'
			----------
			""";
	runner.runWarningTest();
}
public void testBug562347_561280c9() {
	runNegativeTestWithLibs(
		new String[] {
			"Example.java",
			"""
				import java.util.HashMap;
				import java.util.Map;
				
				import org.eclipse.jdt.annotation.NonNullByDefault;
				
				@NonNullByDefault
				public class Example {
				    static <X> X f(X x) {
				        return x;
				    }
				
				    public void g() {
				        Object x0, x1, x2, x3, x4, x5, x6, x7, x8, x9;
				        Object x10, x11, x12, x13, x14, x15, x16, x17, x18, x19;
				        Object x20, x21, x22, x23, x24, x25, x26, x27, x28, x29;
				        Object x30, x31, x32, x33, x34, x35, x36, x37, x38, x39;
				        Object x40, x41, x42, x43, x44, x45, x46, x47, x48, x49;
				        Object x50, x51, x52, x53, x54, x55, x56, x57, x58, x59;
				        Object x60;
				        Object x61;
				        for (Map.Entry<String, String> entry : new HashMap<String, String>().entrySet()) {
				            if (f(entry.getKey()) != null) {
				                continue;
				            }
				            String x = "asdf";
				            x.hashCode();
				        }
				    }
				}
				
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in Example.java (at line 22)
				if (f(entry.getKey()) != null) {
				    ^^^^^^^^^^^^^^^^^
			Redundant null check: comparing \'@NonNull String\' against null
			----------
			2. WARNING in Example.java (at line 25)
				String x = "asdf";
				^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			""");
}
public void testBug562347() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"NotificationListingHolder.java",
			"""
				@SuppressWarnings("unused")
				public final class NotificationListingHolder {
				    private String f1,f2,f3,f4;
				
				    private void setupActionButtons() {
				        Test listItemNotificationsBinding2;
				        boolean z;
				        String a1,a2,a3,a4,a5,a6,a7,a8,a9,a10,a11,a12,a13,a14,a15,a16,a17,a18,a19,a20,a21,a22,a23,a24,a25,a26,a27,a28,a29,a30,a31,a32,a33,a34,a35,a36,a37,a38,a39,a40,a41,a42,a43,a44,a45,a46,a47,a48,a49,a50,a51,a52,a53,a54,a55,a56,a57,a58;
				        if (z) {
				            String button4 = listItemNotificationsBinding2.field;
				            if (listItemNotificationsBinding2 != null) {
				                return;
				            }
				        }
				    }
				}
				
				class Test {
				    public final String field;
				}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in NotificationListingHolder.java (at line 9)
				if (z) {
				    ^
			The local variable z may not have been initialized
			----------
			2. ERROR in NotificationListingHolder.java (at line 10)
				String button4 = listItemNotificationsBinding2.field;
				                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The local variable listItemNotificationsBinding2 may not have been initialized
			----------
			3. ERROR in NotificationListingHolder.java (at line 11)
				if (listItemNotificationsBinding2 != null) {
				    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The local variable listItemNotificationsBinding2 may not have been initialized
			----------
			4. ERROR in NotificationListingHolder.java (at line 11)
				if (listItemNotificationsBinding2 != null) {
				    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Redundant null check: The variable listItemNotificationsBinding2 cannot be null at this location
			----------
			5. ERROR in NotificationListingHolder.java (at line 19)
				public final String field;
				                    ^^^^^
			The blank final field field may not have been initialized
			----------
			""";
	runner.classLibraries = this.LIBS;
	runner.runNegativeTest();
}
public void testBug578300() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"A.java",
			"""
				import org.eclipse.jdt.annotation.Nullable;
				public class A {
				
					@Nullable
					public A next;
				
					@Nullable
					public A previous;
				
					public void disconnectOK() {
						this.next.previous = null; // Potential null pointer access: this expression has a '@Nullable' type
						this.next = null;
					}
				
					public void disconnectKO() {
						next.previous = null; // <-- Expected the same error here since we are accessing the same field
						next = null;
					}
				}
				"""
		};
	runner.customOptions = getCompilerOptions();
	runner.classLibraries = this.LIBS;
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in A.java (at line 11)
					this.next.previous = null; // Potential null pointer access: this expression has a \'@Nullable\' type
					     ^^^^
				Potential null pointer access: this expression has a \'@Nullable\' type
				----------
				2. ERROR in A.java (at line 16)
					next.previous = null; // <-- Expected the same error here since we are accessing the same field
					^^^^
				Potential null pointer access: this expression has a \'@Nullable\' type
				----------
				""";
	runner.runNegativeTest();
}

public void testRequireNonNull() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
					@Nullable Object o;
					@NonNull X foo(X x) {
						return java.util.Objects.requireNonNull(x);
					}
					public static void main(String... args) {
						try {
							new X().foo(null);
						} catch (NullPointerException e) {
							System.out.print("caught");
						}
					}
				}
				"""
		};
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_PB_NONNULL_TYPEVAR_FROM_LEGACY_INVOCATION, JavaCore.ERROR);
	runner.classLibraries = this.LIBS;
	runner.expectedCompilerLog = "";
	runner.expectedOutputString = "caught";
	runner.expectedErrorString = "";
	runner.runConformTest();
}

public void testBug522142_redundant1() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Foo.java",
			"""
				import static org.eclipse.jdt.annotation.DefaultLocation.*;\
				import org.eclipse.jdt.annotation.*;\
				@NonNullByDefault({PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT})
				interface Foo<A> {
				    interface Bar<@NonNull A> extends Iterable<Foo<A>> {
				    }
				}
				"""
		};
	runner.expectedCompilerLog =
			"""
				----------
				1. WARNING in Foo.java (at line 3)
					interface Bar<@NonNull A> extends Iterable<Foo<A>> {
					              ^^^^^^^^^^
				The nullness annotation is redundant with a default that applies to this location
				----------
				""";
	runner.customOptions = getCompilerOptions();
	runner.classLibraries = this.LIBS;
	runner.runWarningTest();
}

public void testBug522142_redundant2() {
	// challenge ArrayQualifiedTypeReference:
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Foo.java",
			"""
				import static org.eclipse.jdt.annotation.DefaultLocation.*;\
				import org.eclipse.jdt.annotation.*;\
				@NonNullByDefault({PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT})
				class Foo {
					java.util.List<java.lang.String @NonNull[]> f1 = new java.util.ArrayList<>();
					java.util.List<java.lang.String @NonNull[][]> f2 = new java.util.ArrayList<>();
				}
				"""
		};
	runner.expectedCompilerLog =
			"""
				----------
				1. WARNING in Foo.java (at line 3)
					java.util.List<java.lang.String @NonNull[]> f1 = new java.util.ArrayList<>();
					                                ^^^^^^^^^^
				The nullness annotation is redundant with a default that applies to this location
				----------
				2. WARNING in Foo.java (at line 4)
					java.util.List<java.lang.String @NonNull[][]> f2 = new java.util.ArrayList<>();
					                                ^^^^^^^^^^^^
				The nullness annotation is redundant with a default that applies to this location
				----------
				""";
	runner.customOptions = getCompilerOptions();
	runner.classLibraries = this.LIBS;
	runner.runWarningTest();
}

public void testBug522142_redundant3() {
	// challenge ArrayQualifiedTypeReference:
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Foo.java",
			"""
				import static org.eclipse.jdt.annotation.DefaultLocation.*;\
				import org.eclipse.jdt.annotation.*;\
				@NonNullByDefault({PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT})
				class Foo {
					java.util.List<java.lang. @NonNull String> f = new java.util.ArrayList<>();
				}
				"""
		};
	runner.expectedCompilerLog =
			"""
				----------
				1. WARNING in Foo.java (at line 3)
					java.util.List<java.lang. @NonNull String> f = new java.util.ArrayList<>();
					                          ^^^^^^^^^^^^^^^
				The nullness annotation is redundant with a default that applies to this location
				----------
				""";
	runner.customOptions = getCompilerOptions();
	runner.classLibraries = this.LIBS;
	runner.runWarningTest();
}
public void testBug522142_bogusError() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Foo.java",
			"""
				import static org.eclipse.jdt.annotation.DefaultLocation.*;\
				import org.eclipse.jdt.annotation.*;\
				@NonNullByDefault({PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT})
				interface Foo<A> {
				    interface Bar<A> extends Iterable<Foo<A>> {
				    }
				}
				"""
		};
	runner.expectedCompilerLog =
			"";
	runner.customOptions = getCompilerOptions();
	runner.classLibraries = this.LIBS;
	runner.runConformTest();
}
public void testBug499596() throws Exception {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Foo.java",
			"""
				import static org.eclipse.jdt.annotation.DefaultLocation.*;
				import org.eclipse.jdt.annotation.*;
				import java.util.*;
				
				@NonNullByDefault({ PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS })
				abstract class Foo {
					abstract <T> Collection<T> singleton(T t);
					Collection<String[]> from0(@NonNull String @NonNull [] @NonNull... elements) { // <-- 3 warnings here, ok
						return singleton(elements[0]);
					}
					Collection<String[]> from1(String @NonNull [] @NonNull... elements) { // <-- 2 warnings here, ok
						return singleton(elements[0]);
					}
					Collection<String[]> from2(String [] @NonNull... elements) { // <-- 1 warning here, ok
						return singleton(elements[0]);
					}
					Collection<String[]> from3(String []... elements) {
						return singleton(elements[0]);
					}
					@NonNullByDefault({}) // cancel outer default
					Collection<@NonNull String @NonNull[]> from4(String []... elements) {
						return singleton(elements[0]); // <-- should warn
					}
				}
				"""
		};
	// Expectations:
	// from0 .. from3:
	// 		declarations should show the indicated number of warnings
	// 		statements are OK, since everything is covered by the outer @NNBD
	// from4 should flag the statement (only)
	runner.expectedCompilerLog =
			"""
				----------
				1. WARNING in Foo.java (at line 8)
					Collection<String[]> from0(@NonNull String @NonNull [] @NonNull... elements) { // <-- 3 warnings here, ok
					                           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The nullness annotation is redundant with a default that applies to this location
				----------
				2. WARNING in Foo.java (at line 8)
					Collection<String[]> from0(@NonNull String @NonNull [] @NonNull... elements) { // <-- 3 warnings here, ok
					                                           ^^^^^^^^^^^^^^^^^^^^^^^
				The nullness annotation is redundant with a default that applies to this location
				----------
				3. WARNING in Foo.java (at line 8)
					Collection<String[]> from0(@NonNull String @NonNull [] @NonNull... elements) { // <-- 3 warnings here, ok
					                                                       ^^^^^^^^^^^
				The nullness annotation is redundant with a default that applies to this location
				----------
				4. WARNING in Foo.java (at line 11)
					Collection<String[]> from1(String @NonNull [] @NonNull... elements) { // <-- 2 warnings here, ok
					                                  ^^^^^^^^^^^^^^^^^^^^^^^
				The nullness annotation is redundant with a default that applies to this location
				----------
				5. WARNING in Foo.java (at line 11)
					Collection<String[]> from1(String @NonNull [] @NonNull... elements) { // <-- 2 warnings here, ok
					                                              ^^^^^^^^^^^
				The nullness annotation is redundant with a default that applies to this location
				----------
				6. WARNING in Foo.java (at line 14)
					Collection<String[]> from2(String [] @NonNull... elements) { // <-- 1 warning here, ok
					                                     ^^^^^^^^^^^
				The nullness annotation is redundant with a default that applies to this location
				----------
				7. WARNING in Foo.java (at line 22)
					return singleton(elements[0]); // <-- should warn
					                 ^^^^^^^^^^^
				Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'@NonNull String @NonNull[]\'
				----------
				""";
	runner.customOptions = getCompilerOptions();
	runner.classLibraries = this.LIBS;
	runner.runWarningTest();
}
public void testRedundantNonNull_field() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"test1/Foo.java",
			"""
				package test1;
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class Foo {
				    @NonNull Object f=new Object();
				}
				"""
		};
	runner.expectedCompilerLog =
			"""
				----------
				1. WARNING in test1\\Foo.java (at line 5)
					@NonNull Object f=new Object();
					^^^^^^^^^^^^^^^
				The nullness annotation is redundant with a default that applies to this location
				----------
				""";
	runner.classLibraries = this.LIBS;
	runner.runWarningTest();
}
public void testGH1007_srikanth() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"SubClass.java",
			"""
				// ECJ error in next line: Type mismatch: cannot convert from Class<SubClass> to Class<? extends SuperClass>[]
				@AnnotationWithArrayInitializer(annotationArgument = SubClass.class)
				class AnnotatedClass2 extends AnnotatedSuperClass {}
				
				//ECJ error in next line: Type mismatch: cannot convert from Class<SubClass> to Class<? extends SuperClass>
				@AnnotationWithArrayInitializer(annotationArgument = {SubClass.class})
				class AnnotatedClass extends AnnotatedSuperClass {}
				
				
				class AnnotatedSuperClass {}
				
				@interface AnnotationWithArrayInitializer {
				    Class<? extends SuperClass>[] annotationArgument();
				}
				
				class SubClass extends SuperClass {}
				abstract class SuperClass {}"""
		};
	runner.runConformTest();
}
public void testGH854() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Annot.java",
			"""
				public @interface Annot {
				    Class<? extends Init<? extends Configuration>>[] inits();\s
				}
				""",
			"Configuration.java",
			"public interface Configuration {\n" +
			"}\n",
			"Init.java",
			"public interface Init<C extends Configuration> {\n" +
			"}\n",
			"App.java",
			"""
				interface I<T> {}
				class IImpl<T> implements I<String>, Init<Configuration> {}
				@Annot(inits = {App.MyInit.class})
				public class App {
					static class MyInit extends IImpl<Configuration> {}
				}
				"""
		};
	runner.runConformTest();
}
// duplicate of #1077
public void testGH476() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Controller.java",
			"""
				public class Controller<T> {
				    final static String ENDPOINT = "controll";
				}
				""",
			"RequestMapping.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;
				
				@Target(ElementType.TYPE)
				@Retention(RetentionPolicy.RUNTIME)
				public @interface RequestMapping {
					String name() default "";
					String[] value() default {};
				}
				""",
			"CtlImpl.java",
			"""
				@RequestMapping(CtlImpl.CTL_ENDPOINT)
				public class CtlImpl extends Controller<String> {
				    final static String CTL_ENDPOINT = ENDPOINT + "/ctl";
				    static String value;
				}
				"""
		};
	runner.runConformTest();
}
public void testVSCodeIssue3076() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"demo/cache/AbstractCache.java",
			"""
				package demo.cache;
				
				public abstract class AbstractCache {
				    public enum Expiry {
				        ONE, TWO, THREE
				    }
				
				    protected abstract void cacheThis(int param1, Expiry param2);
				}
				""",
			"demo/Annot.java",
			"""
				package demo;
				public @interface Annot {
					String defaultProperty();
				}
				""",
			"demo/cache/MyCache.java",
			"""
				package demo.cache;
				
				import demo.Annot;
				
				/**
				 * This annotation is what causes the confusion around the nested Expiry type.
				 *
				 * If you comment out this annotation the language server has no problem
				 * figuring it out.
				 *
				 * It can be *any* annotation.
				 * So it would seem that referring to your own class outside of the
				 * class definition is what triggers this particular bug.
				 */
				@Annot(defaultProperty = MyCache.DEFAULT_PROPERTY_NAME)
				public class MyCache extends AbstractCache {
				    public static final String DEFAULT_PROPERTY_NAME = "WHATEVER";
				
				    @Override
				    protected void cacheThis(int param1, Expiry param2) {
				        throw new UnsupportedOperationException("Unimplemented method 'doSomethingElse'");
				    }
				}
				"""
		};
	runner.runConformTest();
}
public void testGH986() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"mypackage/Example.java",
			"""
				package mypackage;
				
				import java.io.Serializable;
				
				@Deprecated(since = Example.SINCE)
				public class Example<T> implements Serializable {
				\t
					static final String SINCE = "...";
				
					private T target;
				
				}"""
		};
	runner.runConformTest();
}
public void testGHjdtls2386() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"ConfigurableApplicationContext.java",
			"public class ConfigurableApplicationContext { }\n",
			"ApplicationContextInitializer.java",
			"""
				public interface ApplicationContextInitializer<T> {
					void initialize(T context);
				}
				""",
			"ContextConfiguration.java",
			"""
			import static java.lang.annotation.ElementType.*;
			import static java.lang.annotation.RetentionPolicy.*;
			import java.lang.annotation.*;

			@Target(TYPE)
			@Retention(RUNTIME)
			public @interface ContextConfiguration {
				Class<? extends ApplicationContextInitializer<?>>[] initializers();
			}
			""",
			"AbstractTest.java",
			"""
			@ContextConfiguration(initializers = {AbstractTest.Initializer.class})
			public abstract class AbstractTest {

			  static class Initializer
			      implements ApplicationContextInitializer<ConfigurableApplicationContext> {

			    @Override
			    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {

			    }
			  }
			}
			"""
		};
	runner.runConformTest();
}
public void testGH1311() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_SyntacticNullAnalysisForFields, CompilerOptions.ENABLED);
	runner.testFiles = new String[] {
		"nullable/Foo.java",
		"""
		package nullable;

		import org.eclipse.jdt.annotation.Nullable;

		public class Foo {

			@Nullable
			private String text;

			public Foo(String text) {
				this.text = text;
			}

			public int getTextSize() {
				return (text == null)? 0: text.length();
			}
		}
		"""
		};
	runner.classLibraries = this.LIBS;
	runner.runConformTest();
}
public void testGH1311_expiry() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_SyntacticNullAnalysisForFields, CompilerOptions.ENABLED);
	runner.testFiles = new String[] {
		"nullable/Foo.java",
		"""
		package nullable;

		import org.eclipse.jdt.annotation.Nullable;

		public class Foo {

			@Nullable
			private String text;

			public Foo(String text) {
				this.text = text;
			}

			public int getTextSize1() {
				int length = (text == null)? 0: text.length();
				return text.length();
			}
			public int getTextSize2() {
				int length = (text != null)? text.length() : 0;
				return text.length();
			}
			public int getTextSize3() {
				return (text == null)? 0:
							b() ? text.length() : -1;
			}
			boolean b() { return true; }
		}
		"""
		};
	runner.expectedCompilerLog =
		"""
		----------
		1. ERROR in nullable\\Foo.java (at line 16)
			return text.length();
			       ^^^^
		Potential null pointer access: this expression has a \'@Nullable\' type
		----------
		2. ERROR in nullable\\Foo.java (at line 20)
			return text.length();
			       ^^^^
		Potential null pointer access: this expression has a \'@Nullable\' type
		----------
		3. ERROR in nullable\\Foo.java (at line 24)
			b() ? text.length() : -1;
			      ^^^^
		Potential null pointer access: this expression has a \'@Nullable\' type
		----------
		""";

	runner.classLibraries = this.LIBS;
	runner.runNegativeTest();
}
public void testBreakInNested_GH1659() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
		"Foo.java",
		"""
		import org.eclipse.jdt.annotation.NonNull;

		public class Foo {
			@NonNull String foo(String... strings) {
				String s = getNonNull();
				loop: {
					for (String str : strings)
						if (str.isEmpty())
							break loop;
				}
				return s; // <<<
			}

			private @NonNull String getNonNull() {
				return "";
			}
		}
		"""
		};
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runner.classLibraries = this.LIBS;
	runner.runConformTest();
}
public void testBreakInNested_GH1659_defNull() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
		"Foo.java",
		"""
		public class Foo {
			void foo(String... strings) {
				String s = null;
				loop: {
					for (String str : strings)
						if (str.isEmpty())
							break loop;
				}
				if (s != null)
					System.out.println();
			}
		}
		"""
		};
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runner.classLibraries = this.LIBS;
	runner.expectedCompilerLog =
			"""
			----------
			1. ERROR in Foo.java (at line 9)
				if (s != null)
				    ^
			Null comparison always yields false: The variable s can only be null at this location
			----------
			2. WARNING in Foo.java (at line 10)
				System.out.println();
				^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			""";
	runner.runNegativeTest();
}
public void testBreakInNested_GH1661() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
		"Foo.java",
		"""
		import org.eclipse.jdt.annotation.NonNull;

		public class Foo {
			Object f0, f1, f2, f3, f4, f5, f6, f7, f8, f9;
			Object f10, f11, f12, f13, f14, f15, f16, f17, f18, f19;
			Object f20, f21, f22, f23, f24, f25, f26, f27, f28, f29;
			Object f30, f31, f32, f33, f34, f35, f36, f37, f38, f39;
			Object f40, f41, f42, f43, f44, f45, f46, f47, f48, f49;
			Object f50, f51, f52, f53, f54, f55, f56, f57, f58, f59;
			Object f60, f61, f62, f63, f64, f65, f66, f67, f68, f69;
			Object f70, f71, f72, f73, f74, f75, f76, f77, f78, f79;
			Object f80, f81, f82, f83, f84, f85, f86, f87, f88, f89;
			Object f90, f91, f92, f93, f94, f95, f96, f97, f98, f99;
			Object f100, f101, f102, f103, f104, f105, f106, f107, f108, f109;
			Object f110, f111, f112, f113, f114, f115, f116, f117, f118, f119;
			Object f120, f121, f122, f123;
			void foo(String... strings) {
				long l0 = 0;
				long l1 = 1;
				if (l1 != 0) {
					long l2 = l1;
					while (l2 != 0) {
						long l3 = 3;
						long l4 = 5;
						if (l4 == 3) {
							l0 = l3;
							break;
						}
					}
				}
			}
		}
		"""
		};
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runner.classLibraries = this.LIBS;
	runner.runConformTest();
}
public void testGH1693_a() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
		"X.java",
		"""
		import java.util.Iterator;
		import org.eclipse.jdt.annotation.NonNullByDefault;

		@NonNullByDefault
		public class X {
			Iterator<String> getProcessIterator() {
				abstract class StringIterator implements Iterator<String> { }
				return new StringIterator() {
					@Override
					public boolean hasNext() {
						return false;
					}
					@Override
					public String next() {
						return null;
					}
				};
			}
		}
		"""
	};
	runner.customOptions = getCompilerOptions();
	runner.classLibraries = this.LIBS;
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in X.java (at line 15)
					return null;
					       ^^^^
				Null type mismatch: required \'@NonNull String\' but the provided value is null
				----------
				""";
	runner.runNegativeTest();
}
public void testGH1693_b() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
		"X.java",
		"""
		import java.util.Iterator;
		import org.eclipse.jdt.annotation.NonNullByDefault;

		@NonNullByDefault
		public class X {
			Iterator<String> getProcessIterator() {
				class StringIterator implements Iterator<String> {
					@Override
					public boolean hasNext() {
						return false;
					}
					@Override
					public String next() {
						return null;
					}
				}
				return new StringIterator();
			}
		}
		"""
	};
	runner.customOptions = getCompilerOptions();
	runner.classLibraries = this.LIBS;
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in X.java (at line 14)
					return null;
					       ^^^^
				Null type mismatch: required \'@NonNull String\' but the provided value is null
				----------
				""";
	runner.runNegativeTest();
}
public void testGH1693_c() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
		"X.java",
		"""
		import java.util.Iterator;
		import org.eclipse.jdt.annotation.NonNull;

		public class X {
			Iterator<@NonNull String> getProcessIterator() {
				class StringIterator implements Iterator<@NonNull String> {
					@Override
					public boolean hasNext() {
						return false;
					}
					@Override
					public @NonNull String next() {
						return null;
					}
				}
				return new StringIterator();
			}
		}
		"""
	};
	runner.customOptions = getCompilerOptions();
	runner.classLibraries = this.LIBS;
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in X.java (at line 13)
					return null;
					       ^^^^
				Null type mismatch: required \'@NonNull String\' but the provided value is null
				----------
				""";
	runner.runNegativeTest();
}
}
