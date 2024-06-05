/*******************************************************************************
 * Copyright (c) 2011, 2017 IBM Corporation and others.
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
 *								bug 392099 - [1.8][compiler][null] Apply null annotation on types for null analysis
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

@SuppressWarnings({ "rawtypes" })
public class GrammarCoverageTests308 extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 35 };
//		TESTS_NAMES = new String [] { "testnew" };
	}
	public static Class testClass() {
		return GrammarCoverageTests308.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}
	public GrammarCoverageTests308(String testName){
		super(testName);
	}
	// Lone test to verify that multiple annotations of all three kinds are accepted. All other tests will use only marker annotations
	public void test000() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X extends @Marker @SingleMember(0) @Normal(Value = 0) Object {\n" +
					"}\n"
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						public class X extends @Marker @SingleMember(0) @Normal(Value = 0) Object {
						                        ^^^^^^
					Marker cannot be resolved to a type
					----------
					2. ERROR in X.java (at line 1)
						public class X extends @Marker @SingleMember(0) @Normal(Value = 0) Object {
						                                ^^^^^^^^^^^^
					SingleMember cannot be resolved to a type
					----------
					3. ERROR in X.java (at line 1)
						public class X extends @Marker @SingleMember(0) @Normal(Value = 0) Object {
						                                                 ^^^^^^
					Normal cannot be resolved to a type
					----------
					""");
	}
	// FieldDeclaration ::= Modifiersopt Type VariableDeclarators ';'
	public void test001() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    @Marker int x;
						    Zork z;
						}
						@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)
						@interface Marker {}
						""",

					"java/lang/annotation/ElementType.java",
					"""
						package java.lang.annotation;
						public enum ElementType {
						    TYPE,
						    FIELD,
						    METHOD,
						    PARAMETER,
						    CONSTRUCTOR,
						    LOCAL_VARIABLE,
						    ANNOTATION_TYPE,
						    PACKAGE,
						    TYPE_PARAMETER,
						    TYPE_USE
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						@Marker int x;
						^^^^^^^
					The annotation @Marker is disallowed for this location
					----------
					2. ERROR in X.java (at line 3)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	// TYPE:   MethodHeaderName ::= Modifiersopt TypeParameters Type 'Identifier' '('
	public void test002() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    @Marker <T> @Marker int x() { return 10; };
						    Zork z;
						}
						@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)
						@interface Marker {}
						""",

					"java/lang/annotation/ElementType.java",
					"""
						package java.lang.annotation;
						public enum ElementType {
						    TYPE,
						    FIELD,
						    METHOD,
						    PARAMETER,
						    CONSTRUCTOR,
						    LOCAL_VARIABLE,
						    ANNOTATION_TYPE,
						    PACKAGE,
						    TYPE_PARAMETER,
						    TYPE_USE
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						@Marker <T> @Marker int x() { return 10; };
						^^^^^^^
					The annotation @Marker is disallowed for this location
					----------
					2. ERROR in X.java (at line 2)
						@Marker <T> @Marker int x() { return 10; };
						            ^^^^^^^
					The annotation @Marker is disallowed for this location
					----------
					3. ERROR in X.java (at line 3)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	// TYPE:   MethodHeaderName ::= Modifiersopt Type 'Identifier' '('
	public void test003() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    @Marker int x() { return 10; };
						    Zork z;
						}
						@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)
						@interface Marker {}
						""",

					"java/lang/annotation/ElementType.java",
					"""
						package java.lang.annotation;
						public enum ElementType {
						    TYPE,
						    FIELD,
						    METHOD,
						    PARAMETER,
						    CONSTRUCTOR,
						    LOCAL_VARIABLE,
						    ANNOTATION_TYPE,
						    PACKAGE,
						    TYPE_PARAMETER,
						    TYPE_USE
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						@Marker int x() { return 10; };
						^^^^^^^
					The annotation @Marker is disallowed for this location
					----------
					2. ERROR in X.java (at line 3)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	// FormalParameter ::= Modifiersopt Type VariableDeclaratorIdOrThis
	public void test004() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    int x(@Marker int p) { return 10; };
						    Zork z;
						}
						@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)
						@interface Marker {}
						""",

					"java/lang/annotation/ElementType.java",
					"""
						package java.lang.annotation;
						public enum ElementType {
						    TYPE,
						    FIELD,
						    METHOD,
						    PARAMETER,
						    CONSTRUCTOR,
						    LOCAL_VARIABLE,
						    ANNOTATION_TYPE,
						    PACKAGE,
						    TYPE_PARAMETER,
						    TYPE_USE
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						int x(@Marker int p) { return 10; };
						      ^^^^^^^
					The annotation @Marker is disallowed for this location
					----------
					2. ERROR in X.java (at line 3)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	// FormalParameter ::= Modifiersopt Type PushZeroTypeAnnotations '...' VariableDeclaratorIdOrThis
	public void test005() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    int x(@Marker int ... p) { return 10; };
						    Zork z;
						}
						@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)
						@interface Marker {}
						""",

					"java/lang/annotation/ElementType.java",
					"""
						package java.lang.annotation;
						public enum ElementType {
						    TYPE,
						    FIELD,
						    METHOD,
						    PARAMETER,
						    CONSTRUCTOR,
						    LOCAL_VARIABLE,
						    ANNOTATION_TYPE,
						    PACKAGE,
						    TYPE_PARAMETER,
						    TYPE_USE
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						int x(@Marker int ... p) { return 10; };
						      ^^^^^^^
					The annotation @Marker is disallowed for this location
					----------
					2. ERROR in X.java (at line 3)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	// FormalParameter ::= Modifiersopt Type @308... TypeAnnotations '...' VariableDeclaratorIdOrThis
	public void test006() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    int x(@Marker int [] @Marker ... p) { return 10; };
						    Zork z;
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						int x(@Marker int [] @Marker ... p) { return 10; };
						       ^^^^^^
					Marker cannot be resolved to a type
					----------
					2. ERROR in X.java (at line 2)
						int x(@Marker int [] @Marker ... p) { return 10; };
						                      ^^^^^^
					Marker cannot be resolved to a type
					----------
					3. ERROR in X.java (at line 3)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	// UnionType ::= Type
	// UnionType ::= UnionType '|' Type
	public void test007() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    int x() {
						        try {
						        } catch (@Marker NullPointerException | @Marker ArrayIndexOutOfBoundsException e) {
						        }
						        return 10;
						    }
						    Zork z;
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						} catch (@Marker NullPointerException | @Marker ArrayIndexOutOfBoundsException e) {
						          ^^^^^^
					Marker cannot be resolved to a type
					----------
					2. ERROR in X.java (at line 4)
						} catch (@Marker NullPointerException | @Marker ArrayIndexOutOfBoundsException e) {
						                                         ^^^^^^
					Marker cannot be resolved to a type
					----------
					3. ERROR in X.java (at line 8)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	// LocalVariableDeclaration ::= Type PushModifiers VariableDeclarators
    // LocalVariableDeclaration ::= Modifiers Type PushRealModifiers VariableDeclarators
	public void test008() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    int x() {
						        @Marker int p;
						        final @Marker int q;
						        @Marker final int r;
						        return 10;
						    }
						    Zork z;
						}
						@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)
						@interface Marker {}
						""",

					"java/lang/annotation/ElementType.java",
					"""
						package java.lang.annotation;
						public enum ElementType {
						    TYPE,
						    FIELD,
						    METHOD,
						    PARAMETER,
						    CONSTRUCTOR,
						    LOCAL_VARIABLE,
						    ANNOTATION_TYPE,
						    PACKAGE,
						    TYPE_PARAMETER,
						    TYPE_USE
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						@Marker int p;
						^^^^^^^
					The annotation @Marker is disallowed for this location
					----------
					2. ERROR in X.java (at line 4)
						final @Marker int q;
						      ^^^^^^^
					The annotation @Marker is disallowed for this location
					----------
					3. ERROR in X.java (at line 5)
						@Marker final int r;
						^^^^^^^
					The annotation @Marker is disallowed for this location
					----------
					4. ERROR in X.java (at line 8)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	// Resource ::= Type PushModifiers VariableDeclaratorId EnterVariable '=' ForceNoDiet VariableInitializer RestoreDiet ExitVariableWithInitialization
	// Resource ::= Modifiers Type PushRealModifiers VariableDeclaratorId EnterVariable '=' ForceNoDiet VariableInitializer RestoreDiet ExitVariableWithInitialization
	public void test009() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    int x() {
						        try (@Marker Integer p = null; final @Marker Integer q = null; @Marker final Integer r = null) {
						        }
						        return 10;
						    }
						    Zork z;
						}
						@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)
						@interface Marker {}
						""",

					"java/lang/annotation/ElementType.java",
					"""
						package java.lang.annotation;
						public enum ElementType {
						    TYPE,
						    FIELD,
						    METHOD,
						    PARAMETER,
						    CONSTRUCTOR,
						    LOCAL_VARIABLE,
						    ANNOTATION_TYPE,
						    PACKAGE,
						    TYPE_PARAMETER,
						    TYPE_USE
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						try (@Marker Integer p = null; final @Marker Integer q = null; @Marker final Integer r = null) {
						     ^^^^^^^
					The annotation @Marker is disallowed for this location
					----------
					2. ERROR in X.java (at line 3)
						try (@Marker Integer p = null; final @Marker Integer q = null; @Marker final Integer r = null) {
						             ^^^^^^^
					The resource type Integer does not implement java.lang.AutoCloseable
					----------
					3. ERROR in X.java (at line 3)
						try (@Marker Integer p = null; final @Marker Integer q = null; @Marker final Integer r = null) {
						                                     ^^^^^^^
					The annotation @Marker is disallowed for this location
					----------
					4. ERROR in X.java (at line 3)
						try (@Marker Integer p = null; final @Marker Integer q = null; @Marker final Integer r = null) {
						                                             ^^^^^^^
					The resource type Integer does not implement java.lang.AutoCloseable
					----------
					5. ERROR in X.java (at line 3)
						try (@Marker Integer p = null; final @Marker Integer q = null; @Marker final Integer r = null) {
						                                                               ^^^^^^^
					The annotation @Marker is disallowed for this location
					----------
					6. ERROR in X.java (at line 3)
						try (@Marker Integer p = null; final @Marker Integer q = null; @Marker final Integer r = null) {
						                                                                             ^^^^^^^
					The resource type Integer does not implement java.lang.AutoCloseable
					----------
					7. ERROR in X.java (at line 7)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	// EnhancedForStatementHeaderInit ::= 'for' '(' Type PushModifiers Identifier Dimsopt
	// EnhancedForStatementHeaderInit ::= 'for' '(' Modifiers Type PushRealModifiers Identifier Dimsopt
	public void test010() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    int x() {
						        for (@Marker int i: new int[3]) {}
						        for (final @Marker int i: new int[3]) {}
						        for (@Marker final int i: new int[3]) {}
						        return 10;
						    }
						    Zork z;
						}
						@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)
						@interface Marker {}
						""",

					"java/lang/annotation/ElementType.java",
					"""
						package java.lang.annotation;
						public enum ElementType {
						    TYPE,
						    FIELD,
						    METHOD,
						    PARAMETER,
						    CONSTRUCTOR,
						    LOCAL_VARIABLE,
						    ANNOTATION_TYPE,
						    PACKAGE,
						    TYPE_PARAMETER,
						    TYPE_USE
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						for (@Marker int i: new int[3]) {}
						     ^^^^^^^
					The annotation @Marker is disallowed for this location
					----------
					2. ERROR in X.java (at line 4)
						for (final @Marker int i: new int[3]) {}
						           ^^^^^^^
					The annotation @Marker is disallowed for this location
					----------
					3. ERROR in X.java (at line 5)
						for (@Marker final int i: new int[3]) {}
						     ^^^^^^^
					The annotation @Marker is disallowed for this location
					----------
					4. ERROR in X.java (at line 8)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	// AnnotationMethodHeaderName ::= Modifiersopt TypeParameters Type 'Identifier' '('
	// AnnotationMethodHeaderName ::= Modifiersopt Type 'Identifier' '('
	public void test011() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public @interface X {\s
							public @Marker String value();\s
							@Marker String value2();\s
							@Marker public String value3();\s
							public @Marker <T> @Marker String value4();\s
							@Marker <T> @Marker String value5();\s
							@Marker public <T> @Marker String value6();\s
						}
						@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)
						@interface Marker {}
						""",

					"java/lang/annotation/ElementType.java",
					"""
						package java.lang.annotation;
						public enum ElementType {
						    TYPE,
						    FIELD,
						    METHOD,
						    PARAMETER,
						    CONSTRUCTOR,
						    LOCAL_VARIABLE,
						    ANNOTATION_TYPE,
						    PACKAGE,
						    TYPE_PARAMETER,
						    TYPE_USE
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						public @Marker String value();\s
						       ^^^^^^^
					The annotation @Marker is disallowed for this location
					----------
					2. ERROR in X.java (at line 3)
						@Marker String value2();\s
						^^^^^^^
					The annotation @Marker is disallowed for this location
					----------
					3. ERROR in X.java (at line 4)
						@Marker public String value3();\s
						^^^^^^^
					The annotation @Marker is disallowed for this location
					----------
					4. ERROR in X.java (at line 5)
						public @Marker <T> @Marker String value4();\s
						       ^^^^^^^
					The annotation @Marker is disallowed for this location
					----------
					5. ERROR in X.java (at line 5)
						public @Marker <T> @Marker String value4();\s
						                   ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					6. ERROR in X.java (at line 5)
						public @Marker <T> @Marker String value4();\s
						                                  ^^^^^^^^
					Annotation attributes cannot be generic
					----------
					7. ERROR in X.java (at line 6)
						@Marker <T> @Marker String value5();\s
						^^^^^^^
					The annotation @Marker is disallowed for this location
					----------
					8. ERROR in X.java (at line 6)
						@Marker <T> @Marker String value5();\s
						            ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					9. ERROR in X.java (at line 6)
						@Marker <T> @Marker String value5();\s
						                           ^^^^^^^^
					Annotation attributes cannot be generic
					----------
					10. ERROR in X.java (at line 7)
						@Marker public <T> @Marker String value6();\s
						^^^^^^^
					The annotation @Marker is disallowed for this location
					----------
					11. ERROR in X.java (at line 7)
						@Marker public <T> @Marker String value6();\s
						                   ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					12. ERROR in X.java (at line 7)
						@Marker public <T> @Marker String value6();\s
						                                  ^^^^^^^^
					Annotation attributes cannot be generic
					----------
					""");
	}
	// PrimaryNoNewArray ::= PrimitiveType Dims '.' 'class'
	// PrimaryNoNewArray ::= PrimitiveType '.' 'class'
	public void test012() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {\s
							public void value() {
								Object o = @Marker int.class;
								Object o2 = @Marker int @Marker[] [] @Marker[].class;
						   }
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						Object o = @Marker int.class;
						           ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					2. ERROR in X.java (at line 4)
						Object o2 = @Marker int @Marker[] [] @Marker[].class;
						            ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					3. ERROR in X.java (at line 4)
						Object o2 = @Marker int @Marker[] [] @Marker[].class;
						                        ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					4. ERROR in X.java (at line 4)
						Object o2 = @Marker int @Marker[] [] @Marker[].class;
						                                     ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					""");
	}
	// ReferenceExpression ::= PrimitiveType Dims '::' NonWildTypeArgumentsopt IdentifierOrNew
	public void test013() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						interface I {
						    Object copy(int [] ia);
						}
						public class X  {
						    public static void main(String [] args) {
						        I i = @Marker int @Marker []::<String>clone;
						        Zork z;
						    }
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						I i = @Marker int @Marker []::<String>clone;
						       ^^^^^^
					Marker cannot be resolved to a type
					----------
					2. ERROR in X.java (at line 6)
						I i = @Marker int @Marker []::<String>clone;
						                   ^^^^^^
					Marker cannot be resolved to a type
					----------
					3. WARNING in X.java (at line 6)
						I i = @Marker int @Marker []::<String>clone;
						                               ^^^^^^
					Unused type arguments for the non generic method clone() of type Object; it should not be parameterized with arguments <String>
					----------
					4. ERROR in X.java (at line 7)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	// ArrayCreationWithoutArrayInitializer ::= 'new' PrimitiveType DimWithOrWithOutExprs
	// ArrayCreationWithArrayInitializer ::= 'new' PrimitiveType DimWithOrWithOutExprs ArrayInitializer
	public void test014() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X  {
						    public static void main(String [] args) {
						        int i [] = new @Marker int @Marker [4];
						        int j [] = new @Marker int @Marker [] { 10 };
						        Zork z;
						    }
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						int i [] = new @Marker int @Marker [4];
						                ^^^^^^
					Marker cannot be resolved to a type
					----------
					2. ERROR in X.java (at line 3)
						int i [] = new @Marker int @Marker [4];
						                            ^^^^^^
					Marker cannot be resolved to a type
					----------
					3. ERROR in X.java (at line 4)
						int j [] = new @Marker int @Marker [] { 10 };
						                ^^^^^^
					Marker cannot be resolved to a type
					----------
					4. ERROR in X.java (at line 4)
						int j [] = new @Marker int @Marker [] { 10 };
						                            ^^^^^^
					Marker cannot be resolved to a type
					----------
					5. ERROR in X.java (at line 5)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	// CastExpression ::= PushLPAREN PrimitiveType Dimsopt PushRPAREN InsideCastExpression UnaryExpression
	public void test015() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X  {
						    public static void main(String [] args) {
						        int i = (@Marker int) 0;
						        int j [] = (@Marker int @Marker []) null;
						        Zork z;
						    }
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						int i = (@Marker int) 0;
						          ^^^^^^
					Marker cannot be resolved to a type
					----------
					2. ERROR in X.java (at line 4)
						int j [] = (@Marker int @Marker []) null;
						             ^^^^^^
					Marker cannot be resolved to a type
					----------
					3. ERROR in X.java (at line 4)
						int j [] = (@Marker int @Marker []) null;
						                         ^^^^^^
					Marker cannot be resolved to a type
					----------
					4. ERROR in X.java (at line 5)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	// InstanceofExpression ::= InstanceofExpression 'instanceof' ReferenceType
	public void test016() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X  {
						    public static void main(String [] args) {
						        if (args instanceof @Readonly String) {
						        }
						    }
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						if (args instanceof @Readonly String) {
						    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Incompatible conditional operand types String[] and String
					----------
					2. ERROR in X.java (at line 3)
						if (args instanceof @Readonly String) {
						                     ^^^^^^^^
					Readonly cannot be resolved to a type
					----------
					""");
	}
	// TypeArgument ::= ReferenceType
	public void test017() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X extends Y<@Marker Integer, String> {}
						class Y<T, V> {
						    Zork z;
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						public class X extends Y<@Marker Integer, String> {}
						                          ^^^^^^
					Marker cannot be resolved to a type
					----------
					2. ERROR in X.java (at line 3)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	// ReferenceType1 ::= ReferenceType '>'
	public void test018() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X extends Y<@Marker Integer> {}
						class Y<T> {
						    Zork z;
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						public class X extends Y<@Marker Integer> {}
						                          ^^^^^^
					Marker cannot be resolved to a type
					----------
					2. ERROR in X.java (at line 3)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}

	// ReferenceType2 ::= ReferenceType '>>'
	public void test019() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X<T extends Object & Comparable<? super @Marker String>> {}
						class Y<T> {
						    Zork z;
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						public class X<T extends Object & Comparable<? super @Marker String>> {}
						                                                      ^^^^^^
					Marker cannot be resolved to a type
					----------
					2. ERROR in X.java (at line 3)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	// ReferenceType3 ::= ReferenceType '>>>'
	public void test020() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X<A extends X<X<X<@Marker String>>>> {}
						class Y<T> {
						    Zork z;
						}
						"""
 				},
 				"""
					----------
					1. ERROR in X.java (at line 1)
						public class X<A extends X<X<X<@Marker String>>>> {}
						                           ^
					Bound mismatch: The type X<X<String>> is not a valid substitute for the bounded parameter <A extends X<X<X<String>>>> of the type X<A>
					----------
					2. ERROR in X.java (at line 1)
						public class X<A extends X<X<X<@Marker String>>>> {}
						                             ^
					Bound mismatch: The type X<String> is not a valid substitute for the bounded parameter <A extends X<X<X<String>>>> of the type X<A>
					----------
					3. ERROR in X.java (at line 1)
						public class X<A extends X<X<X<@Marker String>>>> {}
						                               ^^^^^^^^^^^^^^
					Bound mismatch: The type String is not a valid substitute for the bounded parameter <A extends X<X<X<String>>>> of the type X<A>
					----------
					4. ERROR in X.java (at line 1)
						public class X<A extends X<X<X<@Marker String>>>> {}
						                                ^^^^^^
					Marker cannot be resolved to a type
					----------
					5. ERROR in X.java (at line 3)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	// WildcardBounds ::= 'extends' ReferenceType
	// WildcardBounds ::= 'super' ReferenceType
	public void test021() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}
						   void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}
						         ^^^
					Map cannot be resolved to a type
					----------
					2. ERROR in X.java (at line 2)
						void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}
						              ^^^^^^
					Marker cannot be resolved to a type
					----------
					3. ERROR in X.java (at line 2)
						void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}
						                              ^^^^^^
					Marker cannot be resolved to a type
					----------
					4. ERROR in X.java (at line 2)
						void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}
						                                              ^^^^^^
					Marker cannot be resolved to a type
					----------
					5. ERROR in X.java (at line 2)
						void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}
						                                                                ^^^^^^
					Marker cannot be resolved to a type
					----------
					6. ERROR in X.java (at line 3)
						void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}
						         ^^^
					Map cannot be resolved to a type
					----------
					7. ERROR in X.java (at line 3)
						void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}
						              ^^^^^^
					Marker cannot be resolved to a type
					----------
					8. ERROR in X.java (at line 3)
						void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}
						                                ^^^^^^
					Marker cannot be resolved to a type
					----------
					9. ERROR in X.java (at line 3)
						void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}
						                                                ^^^^^^
					Marker cannot be resolved to a type
					----------
					10. ERROR in X.java (at line 3)
						void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}
						                                                                ^^^^^^
					Marker cannot be resolved to a type
					----------
					""");
	}
	// TypeParameter ::= TypeParameterHeader 'extends' ReferenceType
	public void test022() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {
						}
						class Y<T> {}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {
						                 ^^^^^^
					Marker cannot be resolved to a type
					----------
					2. ERROR in X.java (at line 1)
						public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {
						                                   ^^^^^^
					Marker cannot be resolved to a type
					----------
					3. ERROR in X.java (at line 1)
						public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {
						                                             ^^^^^^
					Marker cannot be resolved to a type
					----------
					4. ERROR in X.java (at line 1)
						public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {
						                                                         ^^^^^^
					Marker cannot be resolved to a type
					----------
					5. WARNING in X.java (at line 1)
						public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {
						                                                                          ^^^^^^^^^^^^^^^
					The type parameter Q should not be bounded by the final type Integer. Final types cannot be further extended
					----------
					6. ERROR in X.java (at line 1)
						public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {
						                                                                           ^^^^^^
					Marker cannot be resolved to a type
					----------
					""");
	}
	// TypeParameter ::= TypeParameterHeader 'extends' ReferenceType AdditionalBoundList
	// AdditionalBound ::= '&' ReferenceType
	// TypeParameter1 ::= TypeParameterHeader 'extends' ReferenceType AdditionalBoundList1
	public void test023() throws Exception {
		this.runNegativeTest(
				new String[] {
					"I.java",
					"""
						public interface I<U extends J<? extends I<U>>> {
						}
						interface J<T extends I<? extends J<T>>> {
						}
						class CI<U extends CJ<T, U> & @Marker J<@Marker T>,
									T extends CI<U, T> & @Marker I<U>>
							implements I<U> {
						}
						class CJ<T extends CI<U, T> & @Marker I<@Marker U>,
									U extends CJ<T, U> & J<T>>
							implements J<T> {
						}
						"""
				},
				"""
					----------
					1. ERROR in I.java (at line 5)
						class CI<U extends CJ<T, U> & @Marker J<@Marker T>,
						                               ^^^^^^
					Marker cannot be resolved to a type
					----------
					2. ERROR in I.java (at line 5)
						class CI<U extends CJ<T, U> & @Marker J<@Marker T>,
						                                         ^^^^^^
					Marker cannot be resolved to a type
					----------
					3. ERROR in I.java (at line 6)
						T extends CI<U, T> & @Marker I<U>>
						                      ^^^^^^
					Marker cannot be resolved to a type
					----------
					4. ERROR in I.java (at line 9)
						class CJ<T extends CI<U, T> & @Marker I<@Marker U>,
						                               ^^^^^^
					Marker cannot be resolved to a type
					----------
					5. ERROR in I.java (at line 9)
						class CJ<T extends CI<U, T> & @Marker I<@Marker U>,
						                                         ^^^^^^
					Marker cannot be resolved to a type
					----------
					""");
	}
	// InstanceofExpression_NotName ::= Name 'instanceof' ReferenceType
	public void test024() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X<E> {
						  class Y {
						    E e;
						    E getOtherElement(Object other) {
						      if (!(other instanceof @Marker X<?>.Y)) {};
						      return null;
						    }
						  }
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						if (!(other instanceof @Marker X<?>.Y)) {};
						                        ^^^^^^
					Marker cannot be resolved to a type
					----------
					""");
	}
	// InstanceofExpression_NotName ::= InstanceofExpression_NotName 'instanceof' ReferenceType
	public void test025() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X<P, C> {
						  public X() {
						    if (!(this instanceof @Marker X)) {}
						  }
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						if (!(this instanceof @Marker X)) {}
						                       ^^^^^^
					Marker cannot be resolved to a type
					----------
					""");
	}
	// ReferenceExpressionTypeArgumentsAndTrunk ::= OnlyTypeArguments '.' ClassOrInterfaceType Dimsopt
	public void test026() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						interface I {
						    void foo(Y<String>.Z z, int x);
						}
						public class X  {
						    public static void main(String [] args) {
						        I i = Y<String>.@Marker Z::foo;
						        i.foo(new Y<String>().new Z(), 10);\s
						        Zork z;
						    }
						}
						class Y<T> {
						    class Z {
						        void foo(int x) {
							    System.out.println(x);
						        }
						    }
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						I i = Y<String>.@Marker Z::foo;
						                 ^^^^^^
					Marker cannot be resolved to a type
					----------
					2. ERROR in X.java (at line 8)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	// ArrayCreationWithoutArrayInitializer ::= 'new' ClassOrInterfaceType DimWithOrWithOutExprs
	// ArrayCreationWithArrayInitializer ::= 'new' ClassOrInterfaceType DimWithOrWithOutExprs ArrayInitializer
	public void test027() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X  {
						    public static void main(String [] args) {
						        X [] x = new @Marker X @Marker [5];
						        X [] x2 = new @Marker X @Marker [] { null };
						        Zork z;
						    }
						}
						"""				},
					"""
						----------
						1. ERROR in X.java (at line 3)
							X [] x = new @Marker X @Marker [5];
							              ^^^^^^
						Marker cannot be resolved to a type
						----------
						2. ERROR in X.java (at line 3)
							X [] x = new @Marker X @Marker [5];
							                        ^^^^^^
						Marker cannot be resolved to a type
						----------
						3. ERROR in X.java (at line 4)
							X [] x2 = new @Marker X @Marker [] { null };
							               ^^^^^^
						Marker cannot be resolved to a type
						----------
						4. ERROR in X.java (at line 4)
							X [] x2 = new @Marker X @Marker [] { null };
							                         ^^^^^^
						Marker cannot be resolved to a type
						----------
						5. ERROR in X.java (at line 5)
							Zork z;
							^^^^
						Zork cannot be resolved to a type
						----------
						""");
	}
	// CastExpression ::= PushLPAREN Name OnlyTypeArgumentsForCastExpression '.' ClassOrInterfaceType Dimsopt PushRPAREN InsideCastExpressionWithQualifiedGenerics UnaryExpressionNotPlusMinus
	public void test028() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X  {
						    public static void main(String [] args) {
						        java.util.Map.Entry [] e = (java.util.Map<String, String>.@Marker Entry []) null;
						    }
						}
						"""				},
					"""
						----------
						1. WARNING in X.java (at line 3)
							java.util.Map.Entry [] e = (java.util.Map<String, String>.@Marker Entry []) null;
							^^^^^^^^^^^^^^^^^^^
						Map.Entry is a raw type. References to generic type Map.Entry<K,V> should be parameterized
						----------
						2. ERROR in X.java (at line 3)
							java.util.Map.Entry [] e = (java.util.Map<String, String>.@Marker Entry []) null;
							                            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
						The member type Map.Entry<K,V> cannot be qualified with a parameterized type, since it is static. Remove arguments from qualifying type Map<String,String>
						----------
						3. ERROR in X.java (at line 3)
							java.util.Map.Entry [] e = (java.util.Map<String, String>.@Marker Entry []) null;
							                                                           ^^^^^^
						Marker cannot be resolved to a type
						----------
						""");
	}
	// ReferenceType1 ::= ClassOrInterface '<' TypeArgumentList2
	public void test029() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.io.Serializable;
						import java.util.List;
						public class X<T extends Comparable<T> & Serializable> {
							void foo(List<? extends @Marker Comparable<T>> p) {}\s
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						void foo(List<? extends @Marker Comparable<T>> p) {}\s
						                         ^^^^^^
					Marker cannot be resolved to a type
					----------
					""");
	}
	// ReferenceType2 ::= ClassOrInterface '<' TypeArgumentList3
	public void test030() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						class Base {
						}
						class Foo<U extends Base, V extends Bar<U, @Marker Foo<U, V>>> {
						}
						class Bar<E extends Base, F extends Foo<E, @Marker Bar<E, F>>> {
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						class Foo<U extends Base, V extends Bar<U, @Marker Foo<U, V>>> {
						                                           ^^^^^^^^^^^
					Bound mismatch: The type Foo<U,V> is not a valid substitute for the bounded parameter <F extends Foo<E,Bar<E,F>>> of the type Bar<E,F>
					----------
					2. ERROR in X.java (at line 3)
						class Foo<U extends Base, V extends Bar<U, @Marker Foo<U, V>>> {
						                                            ^^^^^^
					Marker cannot be resolved to a type
					----------
					3. ERROR in X.java (at line 5)
						class Bar<E extends Base, F extends Foo<E, @Marker Bar<E, F>>> {
						                                           ^^^^^^^^^^^
					Bound mismatch: The type Bar<E,F> is not a valid substitute for the bounded parameter <V extends Bar<U,Foo<U,V>>> of the type Foo<U,V>
					----------
					4. ERROR in X.java (at line 5)
						class Bar<E extends Base, F extends Foo<E, @Marker Bar<E, F>>> {
						                                            ^^^^^^
					Marker cannot be resolved to a type
					----------
					""");
	}
	// ClassHeaderExtends ::= 'extends' ClassType
	public void test031() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X extends @Marker Object {\n" +
					"}\n"
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						public class X extends @Marker Object {
						                        ^^^^^^
					Marker cannot be resolved to a type
					----------
					""");
	}
	// ClassInstanceCreationExpression ::= 'new' OnlyTypeArguments ClassType EnterInstanceCreationArgumentList '(' ArgumentListopt ')' UnqualifiedClassBodyopt
	// ClassInstanceCreationExpression ::= 'new' ClassType EnterInstanceCreationArgumentList '(' ArgumentListopt ')' UnqualifiedClassBodyopt
	public void test032() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    X x = new @Marker X();
						    X y = new <String> @Marker X();
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						X x = new @Marker X();
						           ^^^^^^
					Marker cannot be resolved to a type
					----------
					2. WARNING in X.java (at line 3)
						X y = new <String> @Marker X();
						           ^^^^^^
					Unused type arguments for the non generic constructor X() of type X; it should not be parameterized with arguments <String>
					----------
					3. ERROR in X.java (at line 3)
						X y = new <String> @Marker X();
						                    ^^^^^^
					Marker cannot be resolved to a type
					----------
					""");
	}
	// ClassInstanceCreationExpression ::= Primary '.' 'new' OnlyTypeArguments ClassType EnterInstanceCreationArgumentList '(' ArgumentListopt ')' QualifiedClassBodyopt
	// ClassInstanceCreationExpression ::= Primary '.' 'new' ClassType EnterInstanceCreationArgumentList '(' ArgumentListopt ')' QualifiedClassBodyopt
	public void test033() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    class Y {
						    }
						    Y y1 = new @Marker X().new @Marker Y();
						    Y y2 = new @Marker X().new <String> @Marker Y();
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						Y y1 = new @Marker X().new @Marker Y();
						            ^^^^^^
					Marker cannot be resolved to a type
					----------
					2. ERROR in X.java (at line 4)
						Y y1 = new @Marker X().new @Marker Y();
						                            ^^^^^^
					Marker cannot be resolved to a type
					----------
					3. ERROR in X.java (at line 5)
						Y y2 = new @Marker X().new <String> @Marker Y();
						            ^^^^^^
					Marker cannot be resolved to a type
					----------
					4. WARNING in X.java (at line 5)
						Y y2 = new @Marker X().new <String> @Marker Y();
						                            ^^^^^^
					Unused type arguments for the non generic constructor X.Y() of type X.Y; it should not be parameterized with arguments <String>
					----------
					5. ERROR in X.java (at line 5)
						Y y2 = new @Marker X().new <String> @Marker Y();
						                                     ^^^^^^
					Marker cannot be resolved to a type
					----------
					""");
	}
	// ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName 'new' ClassType EnterInstanceCreationArgumentList '(' ArgumentListopt ')' QualifiedClassBodyopt
	// ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName 'new' OnlyTypeArguments ClassType EnterInstanceCreationArgumentList '(' ArgumentListopt ')' QualifiedClassBodyopt
	public void test034() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    X x;
						    class Y {
						    }
						    Y y1 = @Marker x.new @Marker Y();
						    Y y2 = @Marker x.new <String> @Marker Y();
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						Y y1 = @Marker x.new @Marker Y();
						       ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					2. ERROR in X.java (at line 5)
						Y y1 = @Marker x.new @Marker Y();
						                      ^^^^^^
					Marker cannot be resolved to a type
					----------
					3. ERROR in X.java (at line 6)
						Y y2 = @Marker x.new <String> @Marker Y();
						       ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					4. WARNING in X.java (at line 6)
						Y y2 = @Marker x.new <String> @Marker Y();
						                      ^^^^^^
					Unused type arguments for the non generic constructor X.Y() of type X.Y; it should not be parameterized with arguments <String>
					----------
					5. ERROR in X.java (at line 6)
						Y y2 = @Marker x.new <String> @Marker Y();
						                               ^^^^^^
					Marker cannot be resolved to a type
					----------
					""");
	}
	// MethodHeaderThrowsClause ::= 'throws' ClassTypeList
	// ClassTypeList -> ClassTypeElt
	// ClassTypeList ::= ClassTypeList ',' ClassTypeElt
	// ClassTypeElt ::= ClassType
	public void test035() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    void foo() throws @Marker NullPointerException, @Marker ArrayIndexOutOfBoundsException {}
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						void foo() throws @Marker NullPointerException, @Marker ArrayIndexOutOfBoundsException {}
						                   ^^^^^^
					Marker cannot be resolved to a type
					----------
					2. ERROR in X.java (at line 2)
						void foo() throws @Marker NullPointerException, @Marker ArrayIndexOutOfBoundsException {}
						                                                 ^^^^^^
					Marker cannot be resolved to a type
					----------
					""");
	}
	// ClassHeaderImplements ::= 'implements' InterfaceTypeList
	// InterfaceHeaderExtends ::= 'extends' InterfaceTypeList
	// InterfaceTypeList -> InterfaceType
	// InterfaceTypeList ::= InterfaceTypeList ',' InterfaceType
	// InterfaceType ::= ClassOrInterfaceType
	public void test036() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						interface I {}
						interface J {}
						interface K extends @Marker I, @Marker J {}
						interface L {}
						public class X implements @Marker K, @Marker L {
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						interface K extends @Marker I, @Marker J {}
						                     ^^^^^^
					Marker cannot be resolved to a type
					----------
					2. ERROR in X.java (at line 3)
						interface K extends @Marker I, @Marker J {}
						                                ^^^^^^
					Marker cannot be resolved to a type
					----------
					3. ERROR in X.java (at line 5)
						public class X implements @Marker K, @Marker L {
						                           ^^^^^^
					Marker cannot be resolved to a type
					----------
					4. ERROR in X.java (at line 5)
						public class X implements @Marker K, @Marker L {
						                                      ^^^^^^
					Marker cannot be resolved to a type
					----------
					""");
	}
	// ReferenceExpression ::= Name Dimsopt '::' NonWildTypeArgumentsopt IdentifierOrNew
	public void test037() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						interface I {
						    void foo(int x);
						}
						public class X  {
						    public static void main(String [] args) {
						        I i = @Marker Y. @Marker Z @Marker [] [] @Marker [] ::foo;
						        i.foo(10);\s
						        Zork z;
						    }
						}
						class Y {
						    static class Z {
						        public static void foo(int x) {
							    System.out.println(x);
						        }
						    }
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						I i = @Marker Y. @Marker Z @Marker [] [] @Marker [] ::foo;
						      ^^^^^^^
					Type annotations are not allowed on type names used to access static members
					----------
					2. ERROR in X.java (at line 6)
						I i = @Marker Y. @Marker Z @Marker [] [] @Marker [] ::foo;
						      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					The type Y.Z[][][] does not define foo(int) that is applicable here
					----------
					3. ERROR in X.java (at line 6)
						I i = @Marker Y. @Marker Z @Marker [] [] @Marker [] ::foo;
						       ^^^^^^
					Marker cannot be resolved to a type
					----------
					4. ERROR in X.java (at line 6)
						I i = @Marker Y. @Marker Z @Marker [] [] @Marker [] ::foo;
						                  ^^^^^^
					Marker cannot be resolved to a type
					----------
					5. ERROR in X.java (at line 6)
						I i = @Marker Y. @Marker Z @Marker [] [] @Marker [] ::foo;
						                            ^^^^^^
					Marker cannot be resolved to a type
					----------
					6. ERROR in X.java (at line 6)
						I i = @Marker Y. @Marker Z @Marker [] [] @Marker [] ::foo;
						                                          ^^^^^^
					Marker cannot be resolved to a type
					----------
					7. ERROR in X.java (at line 8)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					""");
	}
	// ReferenceExpression ::= Name BeginTypeArguments ReferenceExpressionTypeArgumentsAndTrunk '::' NonWildTypeArgumentsopt IdentifierOrNew
	public void test038() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						interface I {
						    Y foo(int x);
						}
						public class X  {
						    class Z extends Y {
						        public Z(int x) {
						            super(x);
						            System.out.println();
						        }
						    }
						    public static void main(String [] args) {
						        i = @Marker W<@Marker Integer>::<@Marker String> new;
						    }
						}
						class W<T> extends Y {
						    public W(T x) {
						        super(0);
						        System.out.println(x);
						    }
						}
						class Y {
						    public Y(int x) {
						        System.out.println(x);
						    }
						}
						"""


				},
				"""
					----------
					1. ERROR in X.java (at line 12)
						i = @Marker W<@Marker Integer>::<@Marker String> new;
						^
					i cannot be resolved to a variable
					----------
					2. ERROR in X.java (at line 12)
						i = @Marker W<@Marker Integer>::<@Marker String> new;
						    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					The target type of this expression must be a functional interface
					----------
					3. ERROR in X.java (at line 12)
						i = @Marker W<@Marker Integer>::<@Marker String> new;
						     ^^^^^^
					Marker cannot be resolved to a type
					----------
					4. ERROR in X.java (at line 12)
						i = @Marker W<@Marker Integer>::<@Marker String> new;
						               ^^^^^^
					Marker cannot be resolved to a type
					----------
					5. ERROR in X.java (at line 12)
						i = @Marker W<@Marker Integer>::<@Marker String> new;
						                                  ^^^^^^
					Marker cannot be resolved to a type
					----------
					""");
	}
	// CastExpression ::= PushLPAREN Name PushRPAREN InsideCastExpressionLL1 UnaryExpressionNotPlusMinus
	// CastExpression ::= PushLPAREN Name Dims PushRPAREN InsideCastExpression UnaryExpressionNotPlusMinus
	// CastExpression ::= PushLPAREN Name OnlyTypeArgumentsForCastExpression Dimsopt PushRPAREN InsideCastExpression UnaryExpressionNotPlusMinus
	// CastExpression ::= PushLPAREN Name OnlyTypeArgumentsForCastExpression '.' ClassOrInterfaceType Dimsopt PushRPAREN InsideCastExpressionWithQualifiedGenerics UnaryExpressionNotPlusMinus
	public void test039() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X  {
						    Object o = (@Marker X) null;
						    Object p = (@Marker X @Marker []) null;
						    Object q = (@Marker java. @Marker util. @Marker List<@Marker String> []) null;
						    Object r = (@Marker java. @Marker util.@Marker Map<@Marker String, @Marker String>.@Marker Entry @Marker []) null;
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						Object o = (@Marker X) null;
						             ^^^^^^
					Marker cannot be resolved to a type
					----------
					2. ERROR in X.java (at line 3)
						Object p = (@Marker X @Marker []) null;
						             ^^^^^^
					Marker cannot be resolved to a type
					----------
					3. ERROR in X.java (at line 3)
						Object p = (@Marker X @Marker []) null;
						                       ^^^^^^
					Marker cannot be resolved to a type
					----------
					4. ERROR in X.java (at line 4)
						Object q = (@Marker java. @Marker util. @Marker List<@Marker String> []) null;
						            ^^^^^^^
					Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)
					----------
					5. ERROR in X.java (at line 4)
						Object q = (@Marker java. @Marker util. @Marker List<@Marker String> []) null;
						             ^^^^^^
					Marker cannot be resolved to a type
					----------
					6. ERROR in X.java (at line 4)
						Object q = (@Marker java. @Marker util. @Marker List<@Marker String> []) null;
						                          ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					7. ERROR in X.java (at line 4)
						Object q = (@Marker java. @Marker util. @Marker List<@Marker String> []) null;
						                                         ^^^^^^
					Marker cannot be resolved to a type
					----------
					8. ERROR in X.java (at line 4)
						Object q = (@Marker java. @Marker util. @Marker List<@Marker String> []) null;
						                                                      ^^^^^^
					Marker cannot be resolved to a type
					----------
					9. ERROR in X.java (at line 5)
						Object r = (@Marker java. @Marker util.@Marker Map<@Marker String, @Marker String>.@Marker Entry @Marker []) null;
						            ^^^^^^^
					Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)
					----------
					10. ERROR in X.java (at line 5)
						Object r = (@Marker java. @Marker util.@Marker Map<@Marker String, @Marker String>.@Marker Entry @Marker []) null;
						            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					The member type Map.Entry<K,V> cannot be qualified with a parameterized type, since it is static. Remove arguments from qualifying type Map<String,String>
					----------
					11. ERROR in X.java (at line 5)
						Object r = (@Marker java. @Marker util.@Marker Map<@Marker String, @Marker String>.@Marker Entry @Marker []) null;
						             ^^^^^^
					Marker cannot be resolved to a type
					----------
					12. ERROR in X.java (at line 5)
						Object r = (@Marker java. @Marker util.@Marker Map<@Marker String, @Marker String>.@Marker Entry @Marker []) null;
						                          ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					13. ERROR in X.java (at line 5)
						Object r = (@Marker java. @Marker util.@Marker Map<@Marker String, @Marker String>.@Marker Entry @Marker []) null;
						                                       ^^^^^^^
					Type annotations are not allowed on type names used to access static members
					----------
					14. ERROR in X.java (at line 5)
						Object r = (@Marker java. @Marker util.@Marker Map<@Marker String, @Marker String>.@Marker Entry @Marker []) null;
						                                        ^^^^^^
					Marker cannot be resolved to a type
					----------
					15. ERROR in X.java (at line 5)
						Object r = (@Marker java. @Marker util.@Marker Map<@Marker String, @Marker String>.@Marker Entry @Marker []) null;
						                                                    ^^^^^^
					Marker cannot be resolved to a type
					----------
					16. ERROR in X.java (at line 5)
						Object r = (@Marker java. @Marker util.@Marker Map<@Marker String, @Marker String>.@Marker Entry @Marker []) null;
						                                                                    ^^^^^^
					Marker cannot be resolved to a type
					----------
					17. ERROR in X.java (at line 5)
						Object r = (@Marker java. @Marker util.@Marker Map<@Marker String, @Marker String>.@Marker Entry @Marker []) null;
						                                                                                    ^^^^^^
					Marker cannot be resolved to a type
					----------
					18. ERROR in X.java (at line 5)
						Object r = (@Marker java. @Marker util.@Marker Map<@Marker String, @Marker String>.@Marker Entry @Marker []) null;
						                                                                                                  ^^^^^^
					Marker cannot be resolved to a type
					----------
					""");
	}
}
