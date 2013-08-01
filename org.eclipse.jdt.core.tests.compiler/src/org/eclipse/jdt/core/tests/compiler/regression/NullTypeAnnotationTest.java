/*******************************************************************************
 * Copyright (c) 2012, 2013 GK Software AG and others.
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

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;

public class NullTypeAnnotationTest extends AbstractNullAnnotationTest {

	// FIXME (stephan): use this type as long as we don't compile against a JRE8:
	private static final String ELEMENT_TYPE_JAVA = "java/lang/annotation/ElementType.java";
	private static final String ELEMENT_TYPE_SOURCE = "package java.lang.annotation;\n" +
	"public enum ElementType {\n" +
	"    TYPE,\n" +
	"    FIELD,\n" +
	"    METHOD,\n" +
	"    PARAMETER,\n" +
	"    CONSTRUCTOR,\n" +
	"    LOCAL_VARIABLE,\n" +
	"    ANNOTATION_TYPE,\n" +
	"    PACKAGE,\n" +
	"    TYPE_PARAMETER,\n" +
	"    TYPE_USE\n" +
	"}\n";



	// FIXME (stephan): using CUSTOM_NULLABLE_CONTENT_JSR308 et al. throughout,
	// as long as we don't compile against an updated org.eclipse.jdt.annotation bundle

	public NullTypeAnnotationTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which do not belong to the class are skipped...
	static {
//			TESTS_NAMES = new String[] { "testBinary" };
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
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
		customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
		runNegativeTest(
			new String[] {
				ELEMENT_TYPE_JAVA,
				ELEMENT_TYPE_SOURCE,
				CUSTOM_NULLABLE_NAME,
				CUSTOM_NULLABLE_CONTENT_JSR308,
				CUSTOM_NONNULL_NAME,
				CUSTOM_NONNULL_CONTENT_JSR308,
				"X.java",
				  "import org.foo.*;\n" +
				  "import java.util.List;\n" +
				  "public class X {\n" +
				  "    void foo(List<@Nullable Object> l) {\n" +
				  "        System.out.print(l.get(0).toString()); // problem: retrieved element can be null\n" +
				  "        l.add(null);\n" +
				  "    }\n" +
				  "    void bar(java.util.List<@Nullable Object> l) {\n" +
				  "        System.out.print(l.get(1).toString()); // problem: retrieved element can be null\n" +
				  "        l.add(null);\n" +
				  "    }\n" +
				  "}\n"},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	System.out.print(l.get(0).toString()); // problem: retrieved element can be null\n" + 
			"	                 ^^^^^^^^\n" + 
			"Potential null pointer access: The method get(int) may return null\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 9)\n" + 
			"	System.out.print(l.get(1).toString()); // problem: retrieved element can be null\n" + 
			"	                 ^^^^^^^^\n" + 
			"Potential null pointer access: The method get(int) may return null\n" + 
			"----------\n",
			null,
			true, /* shouldFlush*/
			customOptions);
	}

	// a list with nullable elements is used, @Nullable is second annotation
	public void test_nonnull_list_elements_02() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
		customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
		runNegativeTest(
			new String[] {
				ELEMENT_TYPE_JAVA,
				ELEMENT_TYPE_SOURCE,
				CUSTOM_NULLABLE_NAME,
				CUSTOM_NULLABLE_CONTENT_JSR308,
				CUSTOM_NONNULL_NAME,
				CUSTOM_NONNULL_CONTENT_JSR308,
				"Dummy.java",
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.*;\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@Target({METHOD,PARAMETER,LOCAL_VARIABLE,TYPE_USE})\n" +
				"public @interface Dummy {\n" +
				"}\n",
				"X.java",
				  "import org.foo.*;\n" +
				  "import java.util.List;\n" +
				  "public class X {\n" +
				  "    void foo(List<@Dummy @Nullable Object> l) {\n" +
				  "        System.out.print(l.get(0).toString()); // problem: retrieved element can be null\n" +
				  "        l.add(null);\n" +
				  "    }\n" +
				  "    void bar(java.util.List<@Dummy @Nullable Object> l) {\n" +
				  "        System.out.print(l.get(1).toString()); // problem: retrieved element can be null\n" +
				  "        l.add(null);\n" +
				  "    }\n" +
				  "}\n"},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	System.out.print(l.get(0).toString()); // problem: retrieved element can be null\n" + 
			"	                 ^^^^^^^^\n" + 
			"Potential null pointer access: The method get(int) may return null\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 9)\n" + 
			"	System.out.print(l.get(1).toString()); // problem: retrieved element can be null\n" + 
			"	                 ^^^^^^^^\n" + 
			"Potential null pointer access: The method get(int) may return null\n" + 
			"----------\n",
			null,
			true, /* shouldFlush*/
			customOptions);
	}

	// a list with non-null elements is used, list itself is nullable
	public void test_nonnull_list_elements_03() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
		customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
		runNegativeTest(
			new String[] {
				ELEMENT_TYPE_JAVA,
				ELEMENT_TYPE_SOURCE,
				CUSTOM_NULLABLE_NAME,
				CUSTOM_NULLABLE_CONTENT_JSR308,
				CUSTOM_NONNULL_NAME,
				CUSTOM_NONNULL_CONTENT_JSR308,
				"X.java",
				  "import org.foo.*;\n" +
				  "import java.util.List;\n" +
				  "public class X {\n" +
				  "    void foo(@Nullable List<@NonNull Object> l) {\n" +
				  "        System.out.print(l.get(0).toString()); // problem: l may be null\n" +
				  "        l.add(null); // problem: cannot insert 'null' into this list\n" +
				  "    }\n" +
				  "    void bar(@Nullable List<@NonNull Object> l) {\n" +
				  "        System.out.print(l.get(0).toString()); // problem: l may be null\n" +
				  "        l.add(0, null); // problem: cannot insert 'null' into this list\n" +
				  "    }\n" +
				  "}\n"},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	System.out.print(l.get(0).toString()); // problem: l may be null\n" + 
			"	                 ^\n" + 
			"Potential null pointer access: The variable l may be null at this location\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	l.add(null); // problem: cannot insert \'null\' into this list\n" + 
			"	      ^^^^\n" + 
			"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 9)\n" + 
			"	System.out.print(l.get(0).toString()); // problem: l may be null\n" + 
			"	                 ^\n" + 
			"Potential null pointer access: The variable l may be null at this location\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 10)\n" + 
			"	l.add(0, null); // problem: cannot insert \'null\' into this list\n" + 
			"	         ^^^^\n" + 
			"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
			"----------\n",
			null,
			true, /* shouldFlush*/
			customOptions);
	}

	// an outer and inner class both have a type parameter,
	// client instantiates with nullable/nonnull actual type arguments
	public void test_nestedType_01() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
		customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
		runNegativeTest(
			new String[] {
				ELEMENT_TYPE_JAVA,
				ELEMENT_TYPE_SOURCE,
				CUSTOM_NULLABLE_NAME,
				CUSTOM_NULLABLE_CONTENT_JSR308,
				CUSTOM_NONNULL_NAME,
				CUSTOM_NONNULL_CONTENT_JSR308,
				"A.java",
				  "import org.foo.*;\n" +
				  "public class A<X> {\n" +
				  "    public class I<Y> {\n" +
				  "        public X foo(Y l) {\n" +
				  "            return null;\n" +
				  "        }\n" +
				  "    }\n" +
				  "    void bar(A<@Nullable Object>.I<@NonNull Object> i) {\n" + // legal instantiation
				  "        @NonNull Object o = i.foo(null); // problems: argument and assignment violate null contracts\n" +
				  "    }\n" +
				  "}\n"},
			"----------\n" + 
			"1. ERROR in A.java (at line 9)\n" + 
			"	@NonNull Object o = i.foo(null); // problems: argument and assignment violate null contracts\n" + 
			"	                    ^^^^^^^^^^^\n" + 
			"Null type mismatch: required \'@NonNull Object\' but the provided value is inferred as @Nullable\n" + 
			"----------\n" + 
			"2. ERROR in A.java (at line 9)\n" + 
			"	@NonNull Object o = i.foo(null); // problems: argument and assignment violate null contracts\n" + 
			"	                          ^^^^\n" + 
			"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
			"----------\n",
			null,
			true, /* shouldFlush*/
			customOptions);
	}

	// an outer and inner class both have a type parameter,
	// a subclass instantiates with nullable/nonnull actual type arguments
	// and correctly implements an abstract inherited method
	// compile errors only inside that method
	public void test_nestedType_02() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
		customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
		runNegativeTest(
			new String[] {
				ELEMENT_TYPE_JAVA,
				ELEMENT_TYPE_SOURCE,
				CUSTOM_NULLABLE_NAME,
				CUSTOM_NULLABLE_CONTENT_JSR308,
				CUSTOM_NONNULL_NAME,
				CUSTOM_NONNULL_CONTENT_JSR308,
				"A.java",
				  "public class A<X> {\n" +
				  "    public abstract class I<Y> {\n" +
				  "        public abstract X foo(Y l);\n" +
				  "        public X idX(X in) { return in; }\n" +
				  "        public Y idY(Y in) { return in; }\n" +
				  "    }\n" +
				  "}\n",
				"B.java",
				  "import org.foo.*;\n" +
				  "public class B extends A<@NonNull Object> {\n" +
				  "    public class J extends I<@Nullable String> {\n" +
				  "        @Override\n" +
				  "        public @NonNull Object foo(@Nullable String l) {\n" +
				  "            System.out.print(idX(null));\n" +
				  "            return idY(null);\n" +
				  "        }\n" +
				  "    }\n" +
				  "}\n"},
			"----------\n" + 
			"1. ERROR in B.java (at line 6)\n" + 
			"	System.out.print(idX(null));\n" + 
			"	                     ^^^^\n" + 
			"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
			"----------\n" + 
			"2. ERROR in B.java (at line 7)\n" + 
			"	return idY(null);\n" + 
			"	       ^^^^^^^^^\n" + 
			"Null type mismatch: required \'@NonNull Object\' but the provided value is inferred as @Nullable\n" + 
			"----------\n",
			null,
			true, /* shouldFlush*/
			customOptions);
	}

	// an outer and inner class both have a type parameter,
	// a subclass instantiates with nullable/nonnull actual type arguments
	// and incorrectly implements an abstract inherited method
	public void test_nestedType_03() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
		customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
		runNegativeTest(
			new String[] {
				ELEMENT_TYPE_JAVA,
				ELEMENT_TYPE_SOURCE,
				CUSTOM_NULLABLE_NAME,
				CUSTOM_NULLABLE_CONTENT_JSR308,
				CUSTOM_NONNULL_NAME,
				CUSTOM_NONNULL_CONTENT_JSR308,
				"A.java",
				  "public class A<X> {\n" +
				  "    public abstract class I<Y> {\n" +
				  "        public abstract X foo(Y l);\n" +
				  "    }\n" +
				  "}\n",
				"B.java",
				  "import org.foo.*;\n" +
				  "public class B extends A<@NonNull Object> {\n" +
				  "    public class J extends I<@Nullable String> {\n" +
				  "        @Override\n" +
				  "        public @Nullable Object foo(@NonNull String l) {\n" +
				  "            return null;\n" +
				  "        }\n" +
				  "    }\n" +
				  "}\n"},
			"----------\n" + 
			"1. ERROR in B.java (at line 5)\n" + 
			"	public @Nullable Object foo(@NonNull String l) {\n" + 
			"	       ^^^^^^^^^^^^^^^^\n" + 
			"The return type is incompatible with the @NonNull return from A<Object>.I<String>.foo(String)\n" + 
			"----------\n" + 
			"2. ERROR in B.java (at line 5)\n" + 
			"	public @Nullable Object foo(@NonNull String l) {\n" + 
			"	                            ^^^^^^^^^^^^^^^\n" + 
			"Illegal redefinition of parameter l, inherited method from A<Object>.I<String> declares this parameter as @Nullable\n" + 
			"----------\n",
			null,
			true, /* shouldFlush*/
			customOptions);
	}

	// a reference to a nested type has annotations for both types
	public void test_nestedType_04() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
		customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
		runNegativeTest(
			new String[] {
				ELEMENT_TYPE_JAVA,
				ELEMENT_TYPE_SOURCE,
				CUSTOM_NULLABLE_NAME,
				CUSTOM_NULLABLE_CONTENT_JSR308,
				CUSTOM_NONNULL_NAME,
				CUSTOM_NONNULL_CONTENT_JSR308,
				"A.java",
				  "public class A<X> {\n" +
				  "    public abstract class I<Y> {\n" +
				  "        public abstract X foo(Y l);\n" +
				  "    }\n" +
				  "}\n",
				"B.java",
				  "import org.foo.*;\n" +
				  "public class B {\n" +
				  "    public void foo(@NonNull A<Object>.@Nullable I<@NonNull String> ai) {\n" +
				  "            ai.foo(null); // problems: ai can be null, arg must not be null\n" +
				  "    }\n" +
				  "}\n"},
			"----------\n" + 
			"1. ERROR in B.java (at line 4)\n" + 
			"	ai.foo(null); // problems: ai can be null, arg must not be null\n" + 
			"	^^\n" + 
			"Potential null pointer access: this expression has a '@Nullable' type\n" + 
			"----------\n" + 
			"2. ERROR in B.java (at line 4)\n" + 
			"	ai.foo(null); // problems: ai can be null, arg must not be null\n" + 
			"	       ^^^^\n" + 
			"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" + 
			"----------\n",
			null,
			true, /* shouldFlush*/
			customOptions);
	}

	public void testMissingAnnotationTypes_01() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public class U {}\n" +
				"   @Missing1 X.@Missing2 U fU;\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	@Missing1 X.@Missing2 U fU;\n" + 
			"	 ^^^^^^^^\n" + 
			"Missing1 cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	@Missing1 X.@Missing2 U fU;\n" + 
			"	             ^^^^^^^^\n" + 
			"Missing2 cannot be resolved to a type\n" + 
			"----------\n");
	}

	// bug 392862 - [1.8][compiler][null] Evaluate null annotations on array types
	// annotation on leaf type in 1-dim array
	public void testArrayType_01() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
		customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
		runNegativeTest(
			new String[] {
				ELEMENT_TYPE_JAVA,
				ELEMENT_TYPE_SOURCE,
				CUSTOM_NULLABLE_NAME,
				CUSTOM_NULLABLE_CONTENT_JSR308,
				CUSTOM_NONNULL_NAME,
				CUSTOM_NONNULL_CONTENT_JSR308,
				"Wrapper.java",
				  "public class Wrapper<T> {\n" +
				  "	T content;" +
				  "	public T content() { return content; }\n" +
				  "}\n",
				"A.java",
				  "import org.foo.*;\n" +
				  "public class A {\n" +
// Using Wrapper is a workaround until bug 391331 is fixed (to force the interesting annotation to be consumed as a type annotation):
				  "    void bar(Wrapper<@NonNull String[]> realStrings, Wrapper<@Nullable String[]> maybeStrings) {\n" +
				  "        System.out.println(realStrings.content()[0].toUpperCase()); // no problem\n" +
				  "        realStrings.content()[0] = null; // problem: cannot assign null as @NonNull element\n" +
				  "        System.out.println(maybeStrings.content()[0].toUpperCase()); // problem: element can be null\n" +
				  "        maybeStrings.content()[0] = null; // no problem\n" +
				  "    }\n" +
				  "}\n"},
		    "----------\n" + 
			"1. ERROR in A.java (at line 5)\n" + 
			"	realStrings.content()[0] = null; // problem: cannot assign null as @NonNull element\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" + 
			"----------\n" + 
			"2. ERROR in A.java (at line 6)\n" + 
			"	System.out.println(maybeStrings.content()[0].toUpperCase()); // problem: element can be null\n" + 
			"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Potential null pointer access: array element may be null\n" + 
			"----------\n",
			null,
			true, /* shouldFlush*/
			customOptions);
	}

	// bug 392862 - [1.8][compiler][null] Evaluate null annotations on array types
	// annotation on leaf type in 2-dim array
	public void testArrayType_02() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
		customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
		runNegativeTest(
			new String[] {
				ELEMENT_TYPE_JAVA,
				ELEMENT_TYPE_SOURCE,
				CUSTOM_NULLABLE_NAME,
				CUSTOM_NULLABLE_CONTENT_JSR308,
				CUSTOM_NONNULL_NAME,
				CUSTOM_NONNULL_CONTENT_JSR308,
				"Wrapper.java",
				  "public class Wrapper<T> {\n" +
				  "	T content;" +
				  "	public T content() { return content; }\n" +
				  "}\n",
				"A.java",
				  "import org.foo.*;\n" +
				  "public class A {\n" +
// Using Wrapper is a workaround until bug 391331 is fixed (to force the interesting annotation to be consumed as a type annotation):
				  "    void bar(Wrapper<@NonNull String[][]> realStrings, Wrapper<@Nullable String[][]> maybeStrings) {\n" +
				  "        System.out.println(realStrings.content()[0][0].toUpperCase()); // no problem\n" +
				  "        realStrings.content()[0][0] = null; // problem: cannot assign null as @NonNull element\n" +
				  "        System.out.println(maybeStrings.content()[0][0].toUpperCase()); // problem: element can be null\n" +
				  "        maybeStrings.content()[0][0] = null; // no problem\n" +
				  "    }\n" +
				  "}\n"},
		    "----------\n" + 
			"1. ERROR in A.java (at line 5)\n" + 
			"	realStrings.content()[0][0] = null; // problem: cannot assign null as @NonNull element\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" + 
			"----------\n" + 
			"2. ERROR in A.java (at line 6)\n" + 
			"	System.out.println(maybeStrings.content()[0][0].toUpperCase()); // problem: element can be null\n" + 
			"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Potential null pointer access: array element may be null\n" + 
			"----------\n",
			null,
			true, /* shouldFlush*/
			customOptions);
	}

	// bug 392862 - [1.8][compiler][null] Evaluate null annotations on array types
	// annotation on array type (1-dim array)
	public void testArrayType_03() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
		customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
		runNegativeTest(
			new String[] {
				ELEMENT_TYPE_JAVA,
				ELEMENT_TYPE_SOURCE,
				CUSTOM_NULLABLE_NAME,
				CUSTOM_NULLABLE_CONTENT_JSR308,
				CUSTOM_NONNULL_NAME,
				CUSTOM_NONNULL_CONTENT_JSR308,
				"A.java",
				  "import org.foo.*;\n" +
				  "public class A {\n" +
				  "    void array(String @NonNull[] realStringArray, String @Nullable[] maybeStringArray) {\n" +
				  "        @NonNull Object array;\n" +
				  "        array = realStringArray;  // no problem\n" +
				  "        realStringArray = null; 	 // problem: cannot assign null as @NonNull array\n" +
				  "        array = maybeStringArray; // problem: array can be null\n" +
				  "        maybeStringArray = null;  // no problem\n" +
				  "    }\n" +
				  "    void leaf(String @NonNull[] realStringArray, String @Nullable[] maybeStringArray) {\n" +
				  "        @NonNull String string;\n" +
				  "        string = realStringArray[0];  // problem: unchecked conversion\n" +
				  "        realStringArray[0] = null; 	 // no problem\n" +
				  "        string = maybeStringArray[0]; // problems: indexing nullable array & unchecked conversion\n" +
				  "        maybeStringArray[0] = null; 	 // problem: indexing nullable array\n" +
				  "    }\n" +
				  "}\n"},
		    "----------\n" + 
    		"1. ERROR in A.java (at line 7)\n" + 
    		"	array = maybeStringArray; // problem: array can be null\n" + 
    		"	        ^^^^^^^^^^^^^^^^\n" + 
    		"Null type mismatch: required \'@NonNull Object\' but the provided value is inferred as @Nullable\n" + 
    		"----------\n" + 
    		"2. WARNING in A.java (at line 12)\n" + 
    		"	string = realStringArray[0];  // problem: unchecked conversion\n" + 
    		"	         ^^^^^^^^^^^^^^^^^^\n" + 
    		"Null type safety: The expression of type String needs unchecked conversion to conform to \'@NonNull String\'\n" + 
    		"----------\n" + 
    		"3. ERROR in A.java (at line 14)\n" + 
    		"	string = maybeStringArray[0]; // problems: indexing nullable array & unchecked conversion\n" + 
    		"	         ^^^^^^^^^^^^^^^^\n" + 
    		"Potential null pointer access: this expression has a '@Nullable' type\n" + 
    		"----------\n" + 
    		"4. WARNING in A.java (at line 14)\n" + 
    		"	string = maybeStringArray[0]; // problems: indexing nullable array & unchecked conversion\n" + 
    		"	         ^^^^^^^^^^^^^^^^^^^\n" + 
    		"Null type safety: The expression of type String needs unchecked conversion to conform to \'@NonNull String\'\n" + 
    		"----------\n" + 
    		"5. ERROR in A.java (at line 15)\n" + 
    		"	maybeStringArray[0] = null; 	 // problem: indexing nullable array\n" + 
    		"	^^^^^^^^^^^^^^^^\n" + 
    		"Potential null pointer access: this expression has a '@Nullable' type\n" + 
			"----------\n",
			null,
			true, /* shouldFlush*/
			customOptions);
	}

	// bug 392862 - [1.8][compiler][null] Evaluate null annotations on array types
	// annotation on intermediate type in 2-dim array
	public void testArrayType_04() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
		customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
		runNegativeTest(
			new String[] {
				ELEMENT_TYPE_JAVA,
				ELEMENT_TYPE_SOURCE,
				CUSTOM_NULLABLE_NAME,
				CUSTOM_NULLABLE_CONTENT_JSR308,
				CUSTOM_NONNULL_NAME,
				CUSTOM_NONNULL_CONTENT_JSR308,
				"A.java",
				  "import org.foo.*;\n" +
				  "public class A {\n" +
				  "    void outer(String [] @NonNull[] realArrays, String [] @Nullable[] maybeArrays) {\n" +
				  "        @NonNull Object array;\n" +
				  "        array = realArrays; 		// problem: unchecked conversion\n" +
				  "        realArrays = null; 		// no problem, outer array is unspecified\n" +
				  "        array = maybeArrays; 	// problem: unchecked conversion\n" +
				  "        maybeArrays = null; 		// no problem\n" +
				  "    }\n" +
				  "    void inner(String [] @NonNull[] realArrays, String [] @Nullable[] maybeArrays) {\n" +
				  "        @NonNull Object array;\n" +
				  "        array = realArrays[0]; 	// no problem\n" +
				  "        realArrays[0] = null; 	// problem: cannot assign null to @NonNull array\n" +
				  "        array = maybeArrays[0]; 	// problem: element can be null\n" +
				  "        maybeArrays[0] = null; 	// no problem\n" +
				  "    }\n" +
				  "    void leaf(String [] @NonNull[] realArrays, String [] @Nullable[] maybeArrays) {\n" +
				  "        @NonNull Object array;\n" +
				  "        array = realArrays[0][0]; // problem: unchecked conversion\n" +
				  "        realArrays[0][0] = null;  // no problem, element type is unspecified\n" +
				  "        array = maybeArrays[0][0]; // problems: indexing nullable array & unchecked conversion\n" +
				  "        maybeArrays[0][0] = null; // problem: indexing nullable array\n" +
				  "    }\n" +
				  "}\n"},
		    "----------\n" + 
    		"1. WARNING in A.java (at line 5)\n" + 
    		"	array = realArrays; 		// problem: unchecked conversion\n" + 
    		"	        ^^^^^^^^^^\n" + 
    		"Null type safety: The expression of type String[][] needs unchecked conversion to conform to \'@NonNull Object\'\n" + 
		    "----------\n" + 
			"2. WARNING in A.java (at line 7)\n" + 
    		"	array = maybeArrays; 	// problem: unchecked conversion\n" + 
    		"	        ^^^^^^^^^^^\n" + 
    		"Null type safety: The expression of type String[][] needs unchecked conversion to conform to \'@NonNull Object\'\n" + 
			"----------\n" + 
			"3. ERROR in A.java (at line 13)\n" + 
			"	realArrays[0] = null; 	// problem: cannot assign null to @NonNull array\n" + 
			"	^^^^^^^^^^^^^\n" + 
			"Null type mismatch: required \'String @NonNull[]\' but the provided value is null\n" + 
			"----------\n" + 
			"4. ERROR in A.java (at line 14)\n" +
			"	array = maybeArrays[0]; 	// problem: element can be null\n" +
			"	        ^^^^^^^^^^^^^^\n" + 
			"Null type mismatch: required '@NonNull Object' but the provided value is inferred as @Nullable\n" + 
			"----------\n" + 
			"5. WARNING in A.java (at line 19)\n" +
			"	array = realArrays[0][0]; // problem: unchecked conversion\n" +
			"	        ^^^^^^^^^^^^^^^^\n" +
    		"Null type safety: The expression of type String needs unchecked conversion to conform to \'@NonNull Object\'\n" + 
			"----------\n" + 
			"6. ERROR in A.java (at line 21)\n" +
			"	array = maybeArrays[0][0]; // problems: indexing nullable array & unchecked conversion\n" +
			"	        ^^^^^^^^^^^^^^\n" +
    		"Potential null pointer access: array element may be null\n" + 
			"----------\n" + 
			"7. WARNING in A.java (at line 21)\n" +
			"	array = maybeArrays[0][0]; // problems: indexing nullable array & unchecked conversion\n" +
			"	        ^^^^^^^^^^^^^^^^^\n" +
			"Null type safety: The expression of type String needs unchecked conversion to conform to \'@NonNull Object\'\n" +
			"----------\n" + 
			"8. ERROR in A.java (at line 22)\n" + 
			"	maybeArrays[0][0] = null; // problem: indexing nullable array\n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"Potential null pointer access: array element may be null\n" + 
			"----------\n",
			null,
			true, /* shouldFlush*/
			customOptions);
	}

	// bug 392862 - [1.8][compiler][null] Evaluate null annotations on array types
	// mismatches against outer array type, test display of type annotation in error messages
	public void testArrayType_05() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
		customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
		runNegativeTest(
			new String[] {
				ELEMENT_TYPE_JAVA,
				ELEMENT_TYPE_SOURCE,
				CUSTOM_NULLABLE_NAME,
				CUSTOM_NULLABLE_CONTENT_JSR308,
				CUSTOM_NONNULL_NAME,
				CUSTOM_NONNULL_CONTENT_JSR308,
				"A.java",
				  "import org.foo.*;\n" +
				  "public class A {\n" +
				  "    void outer(String @NonNull[] @NonNull[] realArrays, String @NonNull[] @Nullable[] maybeArrays, String @Nullable[][] unknownArrays) {\n" +
				  "        realArrays[0] = maybeArrays[0];		// problem: inner array can be null\n" +
				  "        realArrays[0] = unknownArrays[0];	// problems: inner array is unspecified, outer can be null\n" +
				  "    }\n" +
				  "    void oneDim(String @Nullable[] maybeStrings, String[] unknownStrings) {\n" +
				  "        String @NonNull[] s = maybeStrings;\n" +
				  "        s = unknownStrings;\n" +
				  "        consume(maybeStrings);\n" +
				  "        consume(unknownStrings);\n" +
				  "    }\n" +
				  "    void consume(String @NonNull[] s) {};\n" +
				  "}\n"},
			"----------\n" + 
			"1. ERROR in A.java (at line 4)\n" + 
			"	realArrays[0] = maybeArrays[0];		// problem: inner array can be null\n" + 
			"	^^^^^^^^^^^^^\n" + 
			"Null type mismatch: required \'String @NonNull[]\' but the provided value is inferred as @Nullable\n" + 
			"----------\n" + 
			"2. WARNING in A.java (at line 5)\n" + 
			"	realArrays[0] = unknownArrays[0];	// problems: inner array is unspecified, outer can be null\n" + 
			"	^^^^^^^^^^^^^\n" + 
			"Null type safety: The expression of type String[] needs unchecked conversion to conform to \'String @NonNull[]\'\n" + 
			"----------\n" + 
			"3. ERROR in A.java (at line 5)\n" + 
			"	realArrays[0] = unknownArrays[0];	// problems: inner array is unspecified, outer can be null\n" + 
			"	                ^^^^^^^^^^^^^\n" + 
			"Potential null pointer access: this expression has a \'@Nullable\' type\n" + 
			"----------\n" + 
			"4. ERROR in A.java (at line 8)\n" + 
			"	String @NonNull[] s = maybeStrings;\n" + 
			"	                      ^^^^^^^^^^^^\n" + 
			"Null type mismatch (type annotations): required \'String @NonNull[]\' but this expression has type \'String @Nullable[]\'\n" + 
			"----------\n" + 
			"5. WARNING in A.java (at line 9)\n" + 
			"	s = unknownStrings;\n" + 
			"	    ^^^^^^^^^^^^^^\n" + 
			"Null type mismatch (type annotations): the expression of type \'String[]\' needs unchecked conversion to conform to \'String @NonNull[]\'\n" + 
			"----------\n" + 
			"6. ERROR in A.java (at line 10)\n" + 
			"	consume(maybeStrings);\n" + 
			"	        ^^^^^^^^^^^^\n" + 
			"Null type mismatch (type annotations): required \'String @NonNull[]\' but this expression has type \'String @Nullable[]\'\n" + 
			"----------\n" + 
			"7. WARNING in A.java (at line 11)\n" + 
			"	consume(unknownStrings);\n" + 
			"	        ^^^^^^^^^^^^^^\n" + 
			"Null type mismatch (type annotations): the expression of type \'String[]\' needs unchecked conversion to conform to \'String @NonNull[]\'\n" + 
			"----------\n",
			null,
			true, /* shouldFlush*/
			customOptions);
	}

	// bug 392862 - [1.8][compiler][null] Evaluate null annotations on array types
	// more compiler messages
	public void testArrayType_10() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
		customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
		runNegativeTest(
			new String[] {
				ELEMENT_TYPE_JAVA,
				ELEMENT_TYPE_SOURCE,
				CUSTOM_NULLABLE_NAME,
				CUSTOM_NULLABLE_CONTENT_JSR308,
				CUSTOM_NONNULL_NAME,
				CUSTOM_NONNULL_CONTENT_JSR308,
				"A.java",
				  "import org.foo.*;\n" +
				  "public class A {\n" +
				  "    void outer(String @NonNull[] @NonNull[] realArrays, String @NonNull[] @Nullable[] maybeArrays, String @Nullable[][] unknownArrays, String @NonNull[][] mixedArrays) {\n" +
				  "        realArrays = maybeArrays;			// problem on inner dimension!\n" +
				  "        realArrays = unknownArrays; 			// problems on both dimensions\n" +
				  "        maybeArrays = realArrays;			// problem on inner dimension\n" +
				  "        unknownArrays = maybeArrays;			// problsm on outer dimension\n" +
				  "        realArrays = mixedArrays;			// problem on inner\n" +
				  "        maybeArrays = mixedArrays;			// problem on inner\n" +
				  "        consume(maybeArrays, mixedArrays, maybeArrays);\n" +
				  "    }\n" +
				  "    void consume(String @NonNull[] @NonNull[] realStrings, String @NonNull[] @Nullable[] maybeArrays, String @Nullable[][] unknownArrays) {\n" +
				  "    }\n" +
				  "}\n"},
			"----------\n" + 
			"1. ERROR in A.java (at line 4)\n" + 
			"	realArrays = maybeArrays;			// problem on inner dimension!\n" + 
			"	             ^^^^^^^^^^^\n" + 
			"Null type mismatch (type annotations): required \'String @NonNull[] @NonNull[]\' but this expression has type \'String @NonNull[] @Nullable[]\'\n" + 
			"----------\n" + 
			"2. ERROR in A.java (at line 5)\n" + 
			"	realArrays = unknownArrays; 			// problems on both dimensions\n" + 
			"	             ^^^^^^^^^^^^^\n" + 
			"Null type mismatch (type annotations): required \'String @NonNull[] @NonNull[]\' but this expression has type \'String @Nullable[] []\'\n" + 
			"----------\n" + 
			"3. ERROR in A.java (at line 6)\n" + 
			"	maybeArrays = realArrays;			// problem on inner dimension\n" + 
			"	              ^^^^^^^^^^\n" + 
			"Null type mismatch (type annotations): required \'String @NonNull[] @Nullable[]\' but this expression has type \'String @NonNull[] @NonNull[]\'\n" + 
			"----------\n" + 
			"4. ERROR in A.java (at line 7)\n" + 
			"	unknownArrays = maybeArrays;			// problsm on outer dimension\n" + 
			"	                ^^^^^^^^^^^\n" + 
			"Null type mismatch (type annotations): required \'String @Nullable[] []\' but this expression has type \'String @NonNull[] @Nullable[]\'\n" + 
			"----------\n" + 
			"5. WARNING in A.java (at line 8)\n" + 
			"	realArrays = mixedArrays;			// problem on inner\n" + 
			"	             ^^^^^^^^^^^\n" + 
			"Null type mismatch (type annotations): the expression of type \'String @NonNull[] []\' needs unchecked conversion to conform to \'String @NonNull[] @NonNull[]\'\n" + 
			"----------\n" + 
			"6. WARNING in A.java (at line 9)\n" + 
			"	maybeArrays = mixedArrays;			// problem on inner\n" + 
			"	              ^^^^^^^^^^^\n" + 
			"Null type mismatch (type annotations): the expression of type \'String @NonNull[] []\' needs unchecked conversion to conform to \'String @NonNull[] @Nullable[]\'\n" + 
			"----------\n" + 
			"7. ERROR in A.java (at line 10)\n" + 
			"	consume(maybeArrays, mixedArrays, maybeArrays);\n" + 
			"	        ^^^^^^^^^^^\n" + 
			"Null type mismatch (type annotations): required \'String @NonNull[] @NonNull[]\' but this expression has type \'String @NonNull[] @Nullable[]\'\n" + 
			"----------\n" + 
			"8. WARNING in A.java (at line 10)\n" + 
			"	consume(maybeArrays, mixedArrays, maybeArrays);\n" + 
			"	                     ^^^^^^^^^^^\n" + 
			"Null type mismatch (type annotations): the expression of type \'String @NonNull[] []\' needs unchecked conversion to conform to \'String @NonNull[] @Nullable[]\'\n" + 
			"----------\n" + 
			"9. ERROR in A.java (at line 10)\n" + 
			"	consume(maybeArrays, mixedArrays, maybeArrays);\n" + 
			"	                                  ^^^^^^^^^^^\n" + 
			"Null type mismatch (type annotations): required \'String @Nullable[] []\' but this expression has type \'String @NonNull[] @Nullable[]\'\n" + 
			"----------\n",
			null,
			true, /* shouldFlush*/
			customOptions);
	}
	
	// https://bugs.eclipse.org/403216 - [1.8][null] TypeReference#captureTypeAnnotations treats type annotations as type argument annotations 
	public void testBug403216_1() {
		runConformTest(
			new String[] {
				"Test.java",
				"import java.lang.annotation.ElementType;\n" + 
				"import java.lang.annotation.Target;\n" + 
				"\n" + 
				"public class Test {}\n" + 
				"\n" + 
				"class X {\n" + 
				"	class Y {\n" + 
				"		public void foo( @A X. @B Y this) {}\n" + 
				"	}\n" + 
				"}\n" + 
				"@Target(value={ElementType.TYPE_USE})\n" + 
				"@interface A {}\n" + 
				"@Target(value={ElementType.TYPE_USE})\n" + 
				"@interface B {}\n"
			});
	}

	// https://bugs.eclipse.org/403457 - [1.8][compiler] NPE in WildcardBinding.signature
	public void testBug403457() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
		customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
		runNegativeTestWithLibs(
			new String[] {
				ELEMENT_TYPE_JAVA,
				ELEMENT_TYPE_SOURCE,
				CUSTOM_NULLABLE_NAME,
				CUSTOM_NULLABLE_CONTENT_JSR308,
				CUSTOM_NONNULL_NAME,
				CUSTOM_NONNULL_CONTENT_JSR308,
				"X.java",
				"import java.lang.annotation.ElementType;\n" + 
				"import java.lang.annotation.Target;\n" + 
				"import org.foo.*;\n" + 
				"\n" + 
				"public class X {\n" + 
				"	void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}\n" + 
				"   void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}\n" + 
				"}\n" + 
				"\n" + 
				"@Target(ElementType.TYPE_USE)\n" + 
				"@interface Marker {\n" + 
				"	\n" + 
				"}\n"
			},
			customOptions,
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}\n" + 
			"	         ^^^\n" + 
			"Map cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}\n" + 
			"	         ^^^\n" + 
			"Map cannot be resolved to a type\n" + 
			"----------\n");
	}

	// storing and decoding null-type-annotations to/from classfile: RETURN_TYPE
	public void testBinary01() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
		customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		runConformTestWithLibs(
				new String[] {
					ELEMENT_TYPE_JAVA,
					ELEMENT_TYPE_SOURCE,
					CUSTOM_NULLABLE_NAME,
					CUSTOM_NULLABLE_CONTENT_JSR308,
					CUSTOM_NONNULL_NAME,
					CUSTOM_NONNULL_CONTENT_JSR308,
					"p/X.java",
					"package p;\n" +
					"import java.util.List;\n" +
					"import org.foo.*;\n" +
					"public class X {\n" +
					"	public List<@Nullable String> getSomeStrings() { return null; }\n" +
					"}\n"
				},
				customOptions,
				"");
		runNegativeTestWithLibs(
				new String[] {
					"Y.java",
					"import p.X;\n" +
					"public class Y {\n" +
					"	public void test(X x) {\n" +
					"		String s0 = x.getSomeStrings().get(0);\n" +
					"		System.out.println(s0.toUpperCase());\n" +
					"	}\n" +
					"}\n"
				}, 
				customOptions,
				"----------\n" + 
				"1. ERROR in Y.java (at line 5)\n" + 
				"	System.out.println(s0.toUpperCase());\n" + 
				"	                   ^^\n" + 
				"Potential null pointer access: The variable s0 may be null at this location\n" + 
				"----------\n"
				);
	}

	// storing and decoding null-type-annotations to/from classfile: METHOD_FORMAL_PARAMETER & METHOD_RECEIVER
	// Note: receiver annotation is not evaluated by the compiler, this part of the test only serves debugging purposes.
	public void testBinary02() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
		customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		runConformTestWithLibs(
				new String[] {
					ELEMENT_TYPE_JAVA,
					ELEMENT_TYPE_SOURCE,
					CUSTOM_NULLABLE_NAME,
					CUSTOM_NULLABLE_CONTENT_JSR308,
					CUSTOM_NONNULL_NAME,
					CUSTOM_NONNULL_CONTENT_JSR308,
					"p/X.java",
					"package p;\n" +
					"import java.util.List;\n" +
					"import org.foo.*;\n" +
					"import static java.lang.annotation.ElementType.*;\n" +
					"import java.lang.annotation.*;\n" +
					"@Retention(RetentionPolicy.CLASS)\n" +
					"@Target(TYPE_USE)\n" +
					"@interface Immutable {}\n" +
					"public class X {\n" +
					"	public void setAllStrings(@Immutable X this, int dummy, List<@NonNull String> ss) { }\n" +
					"}\n"
				},
				customOptions,
				"");
		runNegativeTestWithLibs(
				new String[] {
					"Y.java",
					"import p.X;\n" +
					"import java.util.List;\n" +
					"import org.foo.*;\n" +
					"public class Y {\n" +
					"	public void test(X x, List<@Nullable String> ss) {\n" +
					"		x.setAllStrings(-1, ss);\n" +
					"	}\n" +
					"}\n"
				}, 
				customOptions,
				"----------\n" + 
				"1. ERROR in Y.java (at line 6)\n" + 
				"	x.setAllStrings(-1, ss);\n" + 
				"	  ^^^^^^^^^^^^^\n" + 
				"The method setAllStrings(int, java.util.List<java.lang.@NonNull String>) in the type X is not applicable for the arguments (int, java.util.List<java.lang.@Nullable String>)\n" + 
				"----------\n"
				);
	}

	// storing and decoding null-type-annotations to/from classfile: FIELD
	public void testBinary03() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
		customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		customOptions.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.IGNORE);
		runConformTestWithLibs(
				new String[] {
					ELEMENT_TYPE_JAVA,
					ELEMENT_TYPE_SOURCE,
					CUSTOM_NULLABLE_NAME,
					CUSTOM_NULLABLE_CONTENT_JSR308,
					CUSTOM_NONNULL_NAME,
					CUSTOM_NONNULL_CONTENT_JSR308,
					"p/X1.java",
					"package p;\n" +
					"import org.foo.*;\n" +
					"public abstract class X1 {\n" +
					"	public static String @Nullable [] f1 = null;\n" +
					"	public static String [] @Nullable [] f2 = new String[][] { null };\n" +
					"}\n"
				},
				customOptions,
				"");
		runNegativeTestWithLibs(
				new String[] {
					"Y1.java",
					"import p.X1;\n" +
					"public class Y1 {\n" +
					"	public void test() {\n" +
					"		System.out.println(X1.f1.length);\n" +
					"		System.out.println(X1.f2[0].length);\n" +
					"	}\n" +
					"}\n"
				}, 
				customOptions,
				"----------\n" + 
				"1. ERROR in Y1.java (at line 4)\n" + 
				"	System.out.println(X1.f1.length);\n" + 
				"	                   ^^\n" + // FIXME(stephan) should point to f1, see https://bugs.eclipse.org/414380 
				"Potential null pointer access: this expression has a '@Nullable' type\n" + 
				"----------\n" + 
				"2. ERROR in Y1.java (at line 5)\n" + 
				"	System.out.println(X1.f2[0].length);\n" + 
				"	                   ^^^^^^^^\n" + 
				"Potential null pointer access: array element may be null\n" + 
				"----------\n"
				);
	}

	// storing and decoding null-type-annotations to/from classfile: SUPER_TYPE
	public void testBinary04() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
		customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		customOptions.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.IGNORE);
		runConformTestWithLibs(
				new String[] {
					ELEMENT_TYPE_JAVA,
					ELEMENT_TYPE_SOURCE,
					CUSTOM_NULLABLE_NAME,
					CUSTOM_NULLABLE_CONTENT_JSR308,
					CUSTOM_NONNULL_NAME,
					CUSTOM_NONNULL_CONTENT_JSR308,
					"p/X1.java",
					"package p;\n" +
					"import java.util.ArrayList;\n" +
					"import org.foo.*;\n" +
					"public abstract class X1 extends ArrayList<@Nullable String> {\n" +
					"}\n",
					"p/X2.java",
					"package p;\n" +
					"import java.util.List;\n" +
					"import org.foo.*;\n" +
					"public abstract class X2 implements List<@Nullable String> {\n" +
					"}\n"
				},
				customOptions,
				"");
		runNegativeTestWithLibs(
				new String[] {
					"Y1.java",
					"import p.X1;\n" +
					"public class Y1 {\n" +
					"	public void test(X1 x) {\n" +
					"		String s0 = x.get(0);\n" +
					"		System.out.println(s0.toUpperCase());\n" +
					"	}\n" +
					"}\n",
					"Y2.java",
					"import p.X2;\n" +
					"public class Y2 {\n" +
					"	public void test(X2 x) {\n" +
					"		String s0 = x.get(0);\n" +
					"		System.out.println(s0.toUpperCase());\n" +
					"	}\n" +
					"}\n"
				}, 
				customOptions,
				"----------\n" + 
				"1. ERROR in Y1.java (at line 5)\n" + 
				"	System.out.println(s0.toUpperCase());\n" + 
				"	                   ^^\n" + 
				"Potential null pointer access: The variable s0 may be null at this location\n" +
				"----------\n" +
				"----------\n" +
				"1. ERROR in Y2.java (at line 5)\n" + 
				"	System.out.println(s0.toUpperCase());\n" + 
				"	                   ^^\n" + 
				"Potential null pointer access: The variable s0 may be null at this location\n" + 
				"----------\n"
				);
	}

	// storing and decoding null-type-annotations to/from classfile: CLASS_TYPE_PARAMETER & METHOD_TYPE_PARAMETER
	public void testBinary05() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
		customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		customOptions.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.IGNORE);
		runConformTestWithLibs(
				new String[] {
					ELEMENT_TYPE_JAVA,
					ELEMENT_TYPE_SOURCE,
					CUSTOM_NULLABLE_NAME,
					CUSTOM_NULLABLE_CONTENT_JSR308,
					CUSTOM_NONNULL_NAME,
					CUSTOM_NONNULL_CONTENT_JSR308,
					"p/X1.java",
					"package p;\n" +
					"import java.util.ArrayList;\n" +
					"import org.foo.*;\n" +
					"public abstract class X1<@NonNull T> extends ArrayList<T> {\n" +
					"    public <@Nullable S> void foo(S s) {}\n" +
					"}\n"
				},
				customOptions,
				"");
// FIXME(stephan): change to negative tests and fill in desired error messages
		runConformTestWithLibs(
//		runNegativeTestWithLibs(
				new String[] {
					"Y1.java",
					"import p.X1;\n" +
					"import org.foo.*;\n" +
					"public class Y1 {\n" +
					"	X1<@Nullable String> maybeStrings;\n" + // incompatible: T is constrained to @NonNull
					"	void test(X1<@NonNull String> x) {\n" + // OK
					"		x.<@NonNull Object>foo(new Object());\n" + // incompatible: S is constrained to @Nullable
					"	}\n" +
					"}\n"
				}, 
				customOptions,
				""
//				"----------\n" + 
//				"1. ERROR in Y1.java (at line 5)\n" + 
//				"	X1<@Nullable String> maybeStrings;\n" + 
//				"	   ^^^^^^^^^^^^^^^^\n" + 
//				"Incompatible type argument ...\n" + 
//				"----------\n"
				);
	}

	// storing and decoding null-type-annotations to/from classfile: CLASS_TYPE_PARAMETER_BOUND & METHOD_TYPE_PARAMETER_BOUND
	public void testBinary06() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
		customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		customOptions.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.IGNORE);
// FIXME(stephan): change to negative tests and fill in desired error messages
//		runNegativeTestWithLibs(
		runConformTestWithLibs(
				new String[] {
					ELEMENT_TYPE_JAVA,
					ELEMENT_TYPE_SOURCE,
					CUSTOM_NULLABLE_NAME,
					CUSTOM_NULLABLE_CONTENT_JSR308,
					CUSTOM_NONNULL_NAME,
					CUSTOM_NONNULL_CONTENT_JSR308,
					"p/X1.java",
					"package p;\n" +
					"import java.util.ArrayList;\n" +
					"import org.foo.*;\n" +
					"public abstract class X1<T extends @NonNull Object> extends ArrayList<T> {\n" +
					"    public <U, V extends @Nullable Object> void foo(U u, V v) {}\n" +
					"}\n" +
					"class X2<@NonNull W extends @Nullable Object> {}\n" // incompatible constraints
				},
				customOptions,
				"");
// FIXME(stephan): change to negative tests and fill in desired error messages
		runConformTestWithLibs(
//		runNegativeTestWithLibs(
				new String[] {
					"Y1.java",
					"import p.X1;\n" +
					"import org.foo.*;\n" +
					"public class Y1 {\n" +
					"	X1<@Nullable String> maybeStrings;\n" + // incompatible: T is constrained to @NonNull
					"	void test(X1<@NonNull String> x) {\n" + // OK
					"		x.<Y1, @NonNull Object>foo(this, new Object());\n" + // incompatible: S is constrained to @Nullable
					"	}\n" +
					"}\n"
				}, 
				customOptions,
				""
//				"----------\n" + 
//				"1. ERROR in Y1.java (at line 5)\n" + 
//				"	X1<@Nullable String> maybeStrings;\n" + 
//				"	   ^^^^^^^^^^^^^^^^\n" + 
//				"Incompatible type argument ...\n" + 
//				"----------\n"
				);
	}

	// storing and decoding null-type-annotations to/from classfile: method with all kinds of type annotations
	public void testBinary07() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
		customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		customOptions.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.IGNORE);
		runConformTestWithLibs(
				new String[] {
					ELEMENT_TYPE_JAVA,
					ELEMENT_TYPE_SOURCE,
					CUSTOM_NULLABLE_NAME,
					CUSTOM_NULLABLE_CONTENT_JSR308,
					CUSTOM_NONNULL_NAME,
					CUSTOM_NONNULL_CONTENT_JSR308,
					"p/X1.java",
					"package p;\n" +
					"import java.util.*;\n" +
					"import org.foo.*;\n" +
					"import static java.lang.annotation.ElementType.*;\n" +
					"import java.lang.annotation.*;\n" +
					"@Retention(RetentionPolicy.CLASS)\n" +
					"@Target(TYPE_USE)\n" +
					"@interface Immutable {}\n" +
					"public abstract class X1 {\n" +
					"    public <@NonNull U, V extends @Nullable Object> List<@NonNull Map<Object, @NonNull String>> foo(@Immutable X1 this, U u, V v) { return null; }\n" +
					"}\n"
				},
				customOptions,
				"");
// FIXME(stephan): add desired error message
		runNegativeTestWithLibs(
				new String[] {
					"Y1.java",
					"import p.X1;\n" +
					"import org.foo.*;\n" +
					"public class Y1 {\n" +
					"	void test(X1 x) {\n" +
					"		x.<@NonNull Y1, @NonNull Object>foo(this, new Object())\n" + // @NonNull Object conflicts with "V extends @Nullable Object"
					"			.get(0).put(null, null);\n" + // second null is illegal
					"	}\n" +
					"}\n"
				}, 
				customOptions,
				"----------\n" + 
				"1. ERROR in Y1.java (at line 6)\n" + 
				"	.get(0).put(null, null);\n" + 
				"	                  ^^^^\n" + 
				"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" + 
				"----------\n");
	}

	// storing and decoding null-type-annotations to/from classfile: details
	public void testBinary08() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
		customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		customOptions.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.IGNORE);
		runConformTestWithLibs(
				new String[] {
					ELEMENT_TYPE_JAVA,
					ELEMENT_TYPE_SOURCE,
					CUSTOM_NULLABLE_NAME,
					CUSTOM_NULLABLE_CONTENT_JSR308,
					CUSTOM_NONNULL_NAME,
					CUSTOM_NONNULL_CONTENT_JSR308,
					"p/X1.java",
					"package p;\n" +
					"import java.util.*;\n" +
					"import org.foo.*;\n" +
					"public abstract class X1 {\n" +
					"    public class Inner {}\n" +
					"    public Object []@NonNull[] arrays(Object @NonNull[][] oa1) { return null; }\n" +
					"    public void nesting(@NonNull Inner i1, @NonNull X1.@Nullable Inner i2) { }\n" +
					"    public void wildcard1(List<@Nullable ? extends @NonNull X1> l) { }\n" + // contradiction
					"    public void wildcard2(List<? super @NonNull X1> l) { }\n" +
					"}\n"
				},
				customOptions,
				"");
// FIXME(stephan): add desired error messages:
		runNegativeTestWithLibs(
				new String[] {
					"Y1.java",
					"import p.X1;\n" +
					"import org.foo.*;\n" +
//					"import java.util.*;\n" +
					"public class Y1 {\n" +
					"	void test(X1 x) {\n" +
					"		Object @NonNull[][] a = new Object[0][];\n" +
					"		x.arrays(a)[0] = null;\n" + // illegal
					"		x.nesting(null, null);\n" + // 1st null is illegal
//					"		x.wildcard2(new ArrayList<@NonNull Object>());\n" +
					"	}\n" +
					"}\n"
				}, 
				customOptions,
				"----------\n" + 
//				"1. ERROR in Y1.java (at line 5)\n" + 
//				"	X1<@Nullable String> maybeStrings;\n" + 
//				"	   ^^^^^^^^^^^^^^^^\n" + 
//				"Incompatible type argument ...\n" + 
//				"----------\n" + 
				"1. ERROR in Y1.java (at line 6)\n" + 
				"	x.arrays(a)[0] = null;\n" + 
				"	^^^^^^^^^^^^^^\n" + 
				"Null type mismatch: required \'Object @NonNull[]\' but the provided value is null\n" +
// TODO(stephan): not reported due to Bug 414384 - [1.8] type annotation on abbreviated inner class is not marked as inner type
//				"----------\n" + 
//				"3. ERROR in Y1.java (at line 8)\n" + 
//				"	x.nesting(null, null);\n" + 
//				"	          ^^^^\n" + 
//				"Null type mismatch: required \'@NonNull X1.Inner\' but the provided value is null\n" + 
				"----------\n");
	}

}