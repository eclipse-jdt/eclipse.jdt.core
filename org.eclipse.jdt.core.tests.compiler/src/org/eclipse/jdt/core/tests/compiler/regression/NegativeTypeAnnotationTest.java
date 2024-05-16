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
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.EclipseJustification;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class NegativeTypeAnnotationTest extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 35 };
//		TESTS_NAMES = new String [] { "test0390882b" };
	}
	public static Class testClass() {
		return NegativeTypeAnnotationTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}
	public NegativeTypeAnnotationTest(String testName){
		super(testName);
	}
	public void test001() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X extends @Marker2 Object {}",
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						public class X extends @Marker2 Object {}
						                        ^^^^^^^
					Marker2 cannot be resolved to a type
					----------
					""");
	}
	public void test002() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.io.Serializable;
						public class X implements @Marker2 Serializable {
							private static final long serialVersionUID = 1L;
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						public class X implements @Marker2 Serializable {
						                           ^^^^^^^
					Marker2 cannot be resolved to a type
					----------
					""");
	}
	public void test003() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X extends @Marker Object {}",
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						public class X extends @Marker Object {}
						                        ^^^^^^
					Marker cannot be resolved to a type
					----------
					""");
	}
	public void test004() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X<@Marker T> {}",
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						public class X<@Marker T> {}
						                ^^^^^^
					Marker cannot be resolved to a type
					----------
					""");
	}
	public void test005() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X<@Marker T> {}",
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						public class X<@Marker T> {}
						                ^^^^^^
					Marker cannot be resolved to a type
					----------
					""");
	}
	public void test006() throws Exception {
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"class Y {}\n",
				"X.java",
				"public class X extends @A(id=\"Hello, World!\") @B @C('(') Y {\n" +
				"}",
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public class X extends @A(id="Hello, World!") @B @C('(') Y {
				                        ^
			A cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 1)
				public class X extends @A(id="Hello, World!") @B @C('(') Y {
				                                               ^
			B cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 1)
				public class X extends @A(id="Hello, World!") @B @C('(') Y {
				                                                  ^
			C cannot be resolved to a type
			----------
			""");
	}
	public void test007() throws Exception {
		this.runNegativeTest(
			new String[] {
				"I.java",
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
				"X.java",
				"public class X implements @A(id=\"Hello, World!\") I, @B @C('(') J {}",
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public class X implements @A(id="Hello, World!") I, @B @C('(') J {}
				                           ^
			A cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 1)
				public class X implements @A(id="Hello, World!") I, @B @C('(') J {}
				                                                     ^
			B cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 1)
				public class X implements @A(id="Hello, World!") I, @B @C('(') J {}
				                                                        ^
			C cannot be resolved to a type
			----------
			""");
	}
	public void test010() throws Exception {
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"class Y<T> {}\n",
				"X.java",
				"public class X extends @A(\"Hello, World!\") Y<@B @C('(') String> {\n" +
				"}",
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public class X extends @A("Hello, World!") Y<@B @C('(') String> {
				                        ^
			A cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 1)
				public class X extends @A("Hello, World!") Y<@B @C('(') String> {
				                                              ^
			B cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 1)
				public class X extends @A("Hello, World!") Y<@B @C('(') String> {
				                                                 ^
			C cannot be resolved to a type
			----------
			""");
	}
	public void test011() throws Exception {
		this.runNegativeTest(
			new String[] {
				"I.java",
				"interface I<T> {}\n",
				"J.java",
				"interface J<T> {}\n",
				"X.java",
				"public class X implements I<@A(\"Hello, World!\") String>,  @B J<@C('(') Integer> {}",
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public class X implements I<@A("Hello, World!") String>,  @B J<@C('(') Integer> {}
				                             ^
			A cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 1)
				public class X implements I<@A("Hello, World!") String>,  @B J<@C('(') Integer> {}
				                                                           ^
			B cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 1)
				public class X implements I<@A("Hello, World!") String>,  @B J<@C('(') Integer> {}
				                                                                ^
			C cannot be resolved to a type
			----------
			""");
	}
	// throws
	public void test012() throws Exception {
		this.runNegativeTest(
			new String[] {
				"E.java",
				"""
					class E extends RuntimeException {
						private static final long serialVersionUID = 1L;
					}
					""",
				"E1.java",
				"""
					class E1 extends RuntimeException {
						private static final long serialVersionUID = 1L;
					}
					""",
				"E2.java",
				"""
					class E2 extends RuntimeException {
						private static final long serialVersionUID = 1L;
					}
					""",
				"X.java",
				"""
					public class X {
						void foo() throws @A("Hello, World!") E, E1, @B @C('(') E2 {}
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				void foo() throws @A("Hello, World!") E, E1, @B @C('(') E2 {}
				                   ^
			A cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 2)
				void foo() throws @A("Hello, World!") E, E1, @B @C('(') E2 {}
				                                              ^
			B cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 2)
				void foo() throws @A("Hello, World!") E, E1, @B @C('(') E2 {}
				                                                 ^
			C cannot be resolved to a type
			----------
			""");
	}
	// method receiver
	public void test013() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void foo(@B(3) X this) {}
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				void foo(@B(3) X this) {}
				          ^
			B cannot be resolved to a type
			----------
			""");
	}
	// method return type
	public void test014() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						@B(3) int foo() {
							return 1;
						}
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				@B(3) int foo() {
				 ^
			B cannot be resolved to a type
			----------
			""");
	}
	// field type
	public void test015() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						@B(3) int field;
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				@B(3) int field;
				 ^
			B cannot be resolved to a type
			----------
			""");
	}
	// method parameter
	public void test016() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						int foo(@B(3) String s) {
							return s.length();
						}
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				int foo(@B(3) String s) {
				         ^
			B cannot be resolved to a type
			----------
			""");
	}
	// method parameter generic or array
	public void test017() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						int foo(String @B(3) [] s) {
							return s.length;
						}
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				int foo(String @B(3) [] s) {
				                ^
			B cannot be resolved to a type
			----------
			""");
	}
	// field type generic or array
	public void test018() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						int @B(3) [] field;
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				int @B(3) [] field;
				     ^
			B cannot be resolved to a type
			----------
			""");
	}
	// class type parameter
	public void test019() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<@A @B(3) T> {}",
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public class X<@A @B(3) T> {}
				                ^
			A cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 1)
				public class X<@A @B(3) T> {}
				                   ^
			B cannot be resolved to a type
			----------
			""");
	}
	// method type parameter
	public void test020() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						<@A @B(3) T> void foo(T t) {}
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				<@A @B(3) T> void foo(T t) {}
				  ^
			A cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 2)
				<@A @B(3) T> void foo(T t) {}
				     ^
			B cannot be resolved to a type
			----------
			""");
	}
	// class type parameter bound
	public void test021() throws Exception {
		this.runNegativeTest(
			new String[] {
				"Z.java",
				"public class Z {}",
				"X.java",
				"public class X<T extends @A Z & @B(3) Cloneable> {}",
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public class X<T extends @A Z & @B(3) Cloneable> {}
				                          ^
			A cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 1)
				public class X<T extends @A Z & @B(3) Cloneable> {}
				                                 ^
			B cannot be resolved to a type
			----------
			""");
	}
	// class type parameter bound generic or array
	public void test022() throws Exception {
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"public class Y<T> {}",
				"X.java",
				"public class X<T extends Y<@A String @C[][]@B[]> & @B(3) Cloneable> {}",
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public class X<T extends Y<@A String @C[][]@B[]> & @B(3) Cloneable> {}
				                            ^
			A cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 1)
				public class X<T extends Y<@A String @C[][]@B[]> & @B(3) Cloneable> {}
				                                      ^
			C cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 1)
				public class X<T extends Y<@A String @C[][]@B[]> & @B(3) Cloneable> {}
				                                            ^
			B cannot be resolved to a type
			----------
			4. ERROR in X.java (at line 1)
				public class X<T extends Y<@A String @C[][]@B[]> & @B(3) Cloneable> {}
				                                                    ^
			B cannot be resolved to a type
			----------
			""");
	}
	// method type parameter bound
	public void test023() throws Exception {
		this.runNegativeTest(
			new String[] {
				"Z.java",
				"public class Z {}",
				"X.java",
				"""
					public class X {
						<T extends @A Z & @B(3) Cloneable> void foo(T t) {}
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				<T extends @A Z & @B(3) Cloneable> void foo(T t) {}
				            ^
			A cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 2)
				<T extends @A Z & @B(3) Cloneable> void foo(T t) {}
				                   ^
			B cannot be resolved to a type
			----------
			""");
	}
	// class type parameter bound generic or array
	public void test024() throws Exception {
		this.runNegativeTest(
			new String[] {
				"Z.java",
				"public class Z {}",
				"Y.java",
				"public class Y<T> {}",
				"X.java",
				"""
					public class X {
						<T extends Y<@A Z @C[][]@B[]> & @B(3) Cloneable> void foo(T t) {}
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				<T extends Y<@A Z @C[][]@B[]> & @B(3) Cloneable> void foo(T t) {}
				              ^
			A cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 2)
				<T extends Y<@A Z @C[][]@B[]> & @B(3) Cloneable> void foo(T t) {}
				                   ^
			C cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 2)
				<T extends Y<@A Z @C[][]@B[]> & @B(3) Cloneable> void foo(T t) {}
				                         ^
			B cannot be resolved to a type
			----------
			4. ERROR in X.java (at line 2)
				<T extends Y<@A Z @C[][]@B[]> & @B(3) Cloneable> void foo(T t) {}
				                                 ^
			B cannot be resolved to a type
			----------
			""");
	}
	// local variable + generic or array
	public void test025() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void foo(String s) {
							@C int i;
							@A String [] @B(3)[] tab = new String[][] {};
							if (tab != null) {
								i = 0;
								System.out.println(i + tab.length);
							} else {
								System.out.println(tab.length);
							}
							i = 4;
							System.out.println(-i + tab.length);
						}
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				@C int i;
				 ^
			C cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 4)
				@A String [] @B(3)[] tab = new String[][] {};
				 ^
			A cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 4)
				@A String [] @B(3)[] tab = new String[][] {};
				              ^
			B cannot be resolved to a type
			----------
			""");
	}
	// type argument constructor call
	public void test026() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						<T> X(T t) {
						}
						public Object foo() {
							X x = new <@A @B(1) String>X(null);
							return x;
						}
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				X x = new <@A @B(1) String>X(null);
				            ^
			A cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 5)
				X x = new <@A @B(1) String>X(null);
				               ^
			B cannot be resolved to a type
			----------
			""");
	}
	// type argument constructor call generic or array
	public void test027() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						<T> X(T t) {
						}
						public Object foo() {
							X x = new <@A @B(1) String>X(null);
							return x;
						}
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				X x = new <@A @B(1) String>X(null);
				            ^
			A cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 5)
				X x = new <@A @B(1) String>X(null);
				               ^
			B cannot be resolved to a type
			----------
			""");
	}
	// type argument method call and generic or array
	public void test028() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					
						static <T, U> T foo(T t, U u) {
							return t;
						}
						public static void main(String[] args) {
							System.out.println(X.<@A @B(1) String[], @C('-') X>foo(new String[]{"SUCCESS"}, null)[0]);
						}
					}
					""",
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				System.out.println(X.<@A @B(1) String[], @C('-') X>foo(new String[]{"SUCCESS"}, null)[0]);
				                       ^
			A cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 7)
				System.out.println(X.<@A @B(1) String[], @C('-') X>foo(new String[]{"SUCCESS"}, null)[0]);
				                          ^
			B cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 7)
				System.out.println(X.<@A @B(1) String[], @C('-') X>foo(new String[]{"SUCCESS"}, null)[0]);
				                                          ^
			C cannot be resolved to a type
			----------
			""");
	}
	public void test029() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X extends @Marker2 Object {}",
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						public class X extends @Marker2 Object {}
						                        ^^^^^^^
					Marker2 cannot be resolved to a type
					----------
					""");
	}
	public void test030() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.io.Serializable;
						public class X implements @Marker2 Serializable {
							private static final long serialVersionUID = 1L;
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						public class X implements @Marker2 Serializable {
						                           ^^^^^^^
					Marker2 cannot be resolved to a type
					----------
					""");
	}
	public void test031() throws Exception {
		this.runNegativeTest(
				new String[] {
					"Marker.java",
					"""
						import java.lang.annotation.Target;
						import static java.lang.annotation.ElementType.*;
						@Target(TYPE)
						@interface Marker {}""",
					"X.java",
					"public class X<@Marker T> {}",

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
					1. ERROR in X.java (at line 1)
						public class X<@Marker T> {}
						               ^^^^^^^
					The annotation @Marker is disallowed for this location
					----------
					""");
	}
	public void test032() throws Exception {
		this.runConformTest(
				new String[] {
					"Marker.java",
					"@interface Marker {}",
					"X.java",
					"public class X<@Marker T> {}",
				},
				"");

	}
	public void test033() throws Exception {
		this.runNegativeTest(
				new String[] {
					"Marker.java",
					"@interface Marker {}",
					"Y.java",
					"public class Y {}",
					"X.java",
					"public class X extends @Marker Y {}",
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						public class X extends @Marker Y {}
						                       ^^^^^^^
					Annotation types that do not specify explicit target element types cannot be applied here
					----------
					""");
	}
	// check locations
	public void test034() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.Map;
					import java.util.List;
					public class X {
						@H String @E[] @F[] @G[] field;
						@A Map<@B String, @C List<@D Object>> field2;
						@A Map<@B String, @H String @E[] @F[] @G[]> field3;
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				@H String @E[] @F[] @G[] field;
				 ^
			H cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 4)
				@H String @E[] @F[] @G[] field;
				           ^
			E cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 4)
				@H String @E[] @F[] @G[] field;
				                ^
			F cannot be resolved to a type
			----------
			4. ERROR in X.java (at line 4)
				@H String @E[] @F[] @G[] field;
				                     ^
			G cannot be resolved to a type
			----------
			5. ERROR in X.java (at line 5)
				@A Map<@B String, @C List<@D Object>> field2;
				 ^
			A cannot be resolved to a type
			----------
			6. ERROR in X.java (at line 5)
				@A Map<@B String, @C List<@D Object>> field2;
				        ^
			B cannot be resolved to a type
			----------
			7. ERROR in X.java (at line 5)
				@A Map<@B String, @C List<@D Object>> field2;
				                   ^
			C cannot be resolved to a type
			----------
			8. ERROR in X.java (at line 5)
				@A Map<@B String, @C List<@D Object>> field2;
				                           ^
			D cannot be resolved to a type
			----------
			9. ERROR in X.java (at line 6)
				@A Map<@B String, @H String @E[] @F[] @G[]> field3;
				 ^
			A cannot be resolved to a type
			----------
			10. ERROR in X.java (at line 6)
				@A Map<@B String, @H String @E[] @F[] @G[]> field3;
				        ^
			B cannot be resolved to a type
			----------
			11. ERROR in X.java (at line 6)
				@A Map<@B String, @H String @E[] @F[] @G[]> field3;
				                   ^
			H cannot be resolved to a type
			----------
			12. ERROR in X.java (at line 6)
				@A Map<@B String, @H String @E[] @F[] @G[]> field3;
				                             ^
			E cannot be resolved to a type
			----------
			13. ERROR in X.java (at line 6)
				@A Map<@B String, @H String @E[] @F[] @G[]> field3;
				                                  ^
			F cannot be resolved to a type
			----------
			14. ERROR in X.java (at line 6)
				@A Map<@B String, @H String @E[] @F[] @G[]> field3;
				                                       ^
			G cannot be resolved to a type
			----------
			""");
	}
	// check locations
	public void test035() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.Map;
					import java.util.List;
					public class X {
						@H java.lang.String @E[] @F[] @G[] field;
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				@H java.lang.String @E[] @F[] @G[] field;
				 ^
			H cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 4)
				@H java.lang.String @E[] @F[] @G[] field;
				                     ^
			E cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 4)
				@H java.lang.String @E[] @F[] @G[] field;
				                          ^
			F cannot be resolved to a type
			----------
			4. ERROR in X.java (at line 4)
				@H java.lang.String @E[] @F[] @G[] field;
				                               ^
			G cannot be resolved to a type
			----------
			""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383884 -- Compiler tolerates illegal dimension annotation in class literal expressions
	public void test036() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public static void main(String[] args) {
					    System.out.println(int @NonEmpty [] [] @NonEmpty @Empty [] [] @NonEmpty[].class); // illegal!
					    System.out.println(X @NonEmpty [] [] @NonEmpty @Empty [] [] @NonEmpty[].class); // illegal!
					    System.out.println(int [] [] [] [] [].class);
					    System.out.println(X [] [] [] [] [].class);
					  }
					}
					@interface Empty {
					}
					@interface NonEmpty {
					}
					""",
		},
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println(int @NonEmpty [] [] @NonEmpty @Empty [] [] @NonEmpty[].class); // illegal!
					                       ^^^^^^^^^
				Syntax error, type annotations are illegal here
				----------
				2. ERROR in X.java (at line 3)
					System.out.println(int @NonEmpty [] [] @NonEmpty @Empty [] [] @NonEmpty[].class); // illegal!
					                                       ^^^^^^^^^^^^^^^^
				Syntax error, type annotations are illegal here
				----------
				3. ERROR in X.java (at line 3)
					System.out.println(int @NonEmpty [] [] @NonEmpty @Empty [] [] @NonEmpty[].class); // illegal!
					                                                              ^^^^^^^^^
				Syntax error, type annotations are illegal here
				----------
				4. ERROR in X.java (at line 4)
					System.out.println(X @NonEmpty [] [] @NonEmpty @Empty [] [] @NonEmpty[].class); // illegal!
					                     ^^^^^^^^^
				Syntax error, type annotations are illegal here
				----------
				5. ERROR in X.java (at line 4)
					System.out.println(X @NonEmpty [] [] @NonEmpty @Empty [] [] @NonEmpty[].class); // illegal!
					                                     ^^^^^^^^^^^^^^^^
				Syntax error, type annotations are illegal here
				----------
				6. ERROR in X.java (at line 4)
					System.out.println(X @NonEmpty [] [] @NonEmpty @Empty [] [] @NonEmpty[].class); // illegal!
					                                                            ^^^^^^^^^
				Syntax error, type annotations are illegal here
				----------
				""");
	}
	public void test037() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						@interface Marker {}
						@Marker	// line 2: Don't complain\s
						public class X<@Marker T>  extends @Marker Object{		// 3: Complain only on super type and not on class type parameter
							public @Marker Object foo(@Marker Object obj) {  // 4: Don't complain on both
								return null;
							}
						}
						""",
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						public class X<@Marker T>  extends @Marker Object{		// 3: Complain only on super type and not on class type parameter
						                                   ^^^^^^^
					Annotation types that do not specify explicit target element types cannot be applied here
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383950
	// [1.8][compiler] Type annotations must have target type meta annotation TYPE_USE
	public void test038() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							import java.lang.annotation.Target;
							import static java.lang.annotation.ElementType.*;
							@Target({PACKAGE, TYPE, METHOD, FIELD, CONSTRUCTOR, PARAMETER, LOCAL_VARIABLE})
							@interface Marker {}
							public class X<@Marker T>  extends @Marker Object{		// 3: Complain\s
							}
							""",
					},
					"""
						----------
						1. ERROR in X.java (at line 5)
							public class X<@Marker T>  extends @Marker Object{		// 3: Complain\s
							               ^^^^^^^
						The annotation @Marker is disallowed for this location
						----------
						2. ERROR in X.java (at line 5)
							public class X<@Marker T>  extends @Marker Object{		// 3: Complain\s
							                                   ^^^^^^^
						The annotation @Marker is disallowed for this location
						----------
						""");
	}
	// JSR 308: "It is not permitted to annotate the type name in an import statement."
	public void test039() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							import @Marker java.lang.String; // Compilation error\s
							public class X {\s
							}
							@interface Marker {}
							"""
					},
					"""
						----------
						1. ERROR in X.java (at line 1)
							import @Marker java.lang.String; // Compilation error\s
							       ^^^^^^^
						Syntax error, type annotations are illegal here
						----------
						""");
	}
	// Test that type name can't be left out in a cast expression with an annotations
	public void test040() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {\s
								public void foo(Object myObject) {
									String myString = (@NonNull) myObject;\
								}
							}
							@interface NonNull {}
							"""
					},
					"""
						----------
						1. ERROR in X.java (at line 3)
							String myString = (@NonNull) myObject;	}
							                   ^
						Syntax error on token "@", delete this token
						----------
						""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=385111
	// [1.8][compiler] Compiler fails to flag undefined annotation type.
	public void test0385111() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							import java.util.ArrayList;
							import java.util.List;
							public class X {
							    public void foo(String fileName) {
							        List<String> l = new @MissingTypeNotIgnored ArrayList<String>();
							        List<String> l1 = new @MissingTypeIgnored ArrayList<>();
							    }
							}
							""",
					},
					"""
						----------
						1. ERROR in X.java (at line 5)
							List<String> l = new @MissingTypeNotIgnored ArrayList<String>();
							                      ^^^^^^^^^^^^^^^^^^^^^
						MissingTypeNotIgnored cannot be resolved to a type
						----------
						2. ERROR in X.java (at line 6)
							List<String> l1 = new @MissingTypeIgnored ArrayList<>();
							                       ^^^^^^^^^^^^^^^^^^
						MissingTypeIgnored cannot be resolved to a type
						----------
						""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=385111
	// Test to exercise assorted cleanup along with bug fix.
	public void test0385111a() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    public void foo(String fileName) {
							        try (@Annot X x = null; @Annot X x2 = null) {
							        } catch (@Annot NullPointerException | @Annot UnsupportedOperationException e) {
							        }
							    }
							}
							""",
					},
					"""
						----------
						1. ERROR in X.java (at line 3)
							try (@Annot X x = null; @Annot X x2 = null) {
							      ^^^^^
						Annot cannot be resolved to a type
						----------
						2. ERROR in X.java (at line 3)
							try (@Annot X x = null; @Annot X x2 = null) {
							            ^
						The resource type X does not implement java.lang.AutoCloseable
						----------
						3. ERROR in X.java (at line 3)
							try (@Annot X x = null; @Annot X x2 = null) {
							                         ^^^^^
						Annot cannot be resolved to a type
						----------
						4. ERROR in X.java (at line 3)
							try (@Annot X x = null; @Annot X x2 = null) {
							                               ^
						The resource type X does not implement java.lang.AutoCloseable
						----------
						5. ERROR in X.java (at line 4)
							} catch (@Annot NullPointerException | @Annot UnsupportedOperationException e) {
							          ^^^^^
						Annot cannot be resolved to a type
						----------
						6. ERROR in X.java (at line 4)
							} catch (@Annot NullPointerException | @Annot UnsupportedOperationException e) {
							                                        ^^^^^
						Annot cannot be resolved to a type
						----------
						""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383913
	public void test0383913() {
		this.runNegativeTest(
				new String[]{
						"X.java",
						"""
							public class X {
								public void foo(Object obj, X this) {}
								public void foo(Object obj1, X this, Object obj2) {}
								public void foo(Object obj, Object obj2, Object obj3, X this) {}
								class Y {
									Y(Object obj, Y Y.this){}
								}
							}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						public void foo(Object obj, X this) {}
						                              ^^^^
					Only the first formal parameter may be declared explicitly as 'this'
					----------
					2. ERROR in X.java (at line 3)
						public void foo(Object obj1, X this, Object obj2) {}
						                               ^^^^
					Only the first formal parameter may be declared explicitly as 'this'
					----------
					3. ERROR in X.java (at line 4)
						public void foo(Object obj, Object obj2, Object obj3, X this) {}
						                                                        ^^^^
					Only the first formal parameter may be declared explicitly as 'this'
					----------
					4. ERROR in X.java (at line 6)
						Y(Object obj, Y Y.this){}
						                  ^^^^
					Only the first formal parameter may be declared explicitly as 'this'
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383913
	public void test0383913b() {
		this.runNegativeTest(
				new String[] {
						"Outer.java",
						"""
							public class Outer {
							    Outer(Outer Outer.this) {}
							    Outer(Outer this, int i) {}
							    class Inner<K,V> {
							        class InnerMost<T> {
							            InnerMost(Outer.Inner this) {}
							            InnerMost(Outer.Inner Outer.Inner.this, int i, float f) {}
							            InnerMost(Outer Outer.this, float f) {}
							            InnerMost(Outer.Inner<K,V>.InnerMost<T> Outer.Inner.InnerMost.this, Object obj) {}
							            InnerMost(Inner<K,V> Outer.Inner.InnerMost.this, int i) {}
							            InnerMost(Outer.Inner<K, V> this, float f, int i) {}
							            InnerMost(Outer.Inner<K,V> Inner.this, long l) {}
							        }
							    }
							}
							"""},
						"""
							----------
							1. ERROR in Outer.java (at line 2)
								Outer(Outer Outer.this) {}
								                  ^^^^
							Explicit 'this' parameter is allowed only in instance methods of non-anonymous classes and inner class constructors
							----------
							2. ERROR in Outer.java (at line 3)
								Outer(Outer this, int i) {}
								            ^^^^
							Explicit 'this' parameter is allowed only in instance methods of non-anonymous classes and inner class constructors
							----------
							3. WARNING in Outer.java (at line 6)
								InnerMost(Outer.Inner this) {}
								          ^^^^^^^^^^^
							Outer.Inner is a raw type. References to generic type Outer.Inner<K,V> should be parameterized
							----------
							4. ERROR in Outer.java (at line 6)
								InnerMost(Outer.Inner this) {}
								          ^^^^^^^^^^^
							The declared type of the explicit 'this' parameter is expected to be Outer.Inner<K,V>
							----------
							5. ERROR in Outer.java (at line 6)
								InnerMost(Outer.Inner this) {}
								                      ^^^^
							The explicit 'this' parameter is expected to be qualified with Inner
							----------
							6. WARNING in Outer.java (at line 7)
								InnerMost(Outer.Inner Outer.Inner.this, int i, float f) {}
								          ^^^^^^^^^^^
							Outer.Inner is a raw type. References to generic type Outer.Inner<K,V> should be parameterized
							----------
							7. ERROR in Outer.java (at line 7)
								InnerMost(Outer.Inner Outer.Inner.this, int i, float f) {}
								          ^^^^^^^^^^^
							The declared type of the explicit 'this' parameter is expected to be Outer.Inner<K,V>
							----------
							8. ERROR in Outer.java (at line 7)
								InnerMost(Outer.Inner Outer.Inner.this, int i, float f) {}
								                      ^^^^^^^^^^^^^^^^
							The explicit 'this' parameter is expected to be qualified with Inner
							----------
							9. ERROR in Outer.java (at line 8)
								InnerMost(Outer Outer.this, float f) {}
								          ^^^^^
							The declared type of the explicit 'this' parameter is expected to be Outer.Inner<K,V>
							----------
							10. ERROR in Outer.java (at line 8)
								InnerMost(Outer Outer.this, float f) {}
								                ^^^^^^^^^^
							The explicit 'this' parameter is expected to be qualified with Inner
							----------
							11. ERROR in Outer.java (at line 9)
								InnerMost(Outer.Inner<K,V>.InnerMost<T> Outer.Inner.InnerMost.this, Object obj) {}
								          ^^^^^^^^^^^^^^^^^^^^^^^^^^
							The declared type of the explicit 'this' parameter is expected to be Outer.Inner<K,V>
							----------
							12. ERROR in Outer.java (at line 9)
								InnerMost(Outer.Inner<K,V>.InnerMost<T> Outer.Inner.InnerMost.this, Object obj) {}
								                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^
							The explicit 'this' parameter is expected to be qualified with Inner
							----------
							13. ERROR in Outer.java (at line 10)
								InnerMost(Inner<K,V> Outer.Inner.InnerMost.this, int i) {}
								                     ^^^^^^^^^^^^^^^^^^^^^^^^^^
							The explicit 'this' parameter is expected to be qualified with Inner
							----------
							14. ERROR in Outer.java (at line 11)
								InnerMost(Outer.Inner<K, V> this, float f, int i) {}
								                            ^^^^
							The explicit 'this' parameter is expected to be qualified with Inner
							----------
							""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383913
	public void test0383913c() {
		this.runNegativeTest(
				new String[] {
						"Outer.java",
						"""
							public class Outer {
							    class Inner<K,V> {
							        class InnerMost<T> {
							            public void foo(Outer Outer.this) {}
							            public void foo(Inner<K,V> Inner.this, int i) {}
							            public void foo(InnerMost this, int i, int j) {}
							            public void foo(Inner.InnerMost<T> this, Object obj) {}
							            public void foo(InnerMost<T> this, float f) {}
							            public void foo(Inner<K,V>.InnerMost<T> this, long l) {}
							            public void foo(Outer.Inner<K,V>.InnerMost<T> this, float f, float ff) {}
							            public void foo(InnerMost<T> Outer.Inner.InnerMost.this, int i, float f) {}
							        }
							    }
							}
							"""},
						"""
							----------
							1. ERROR in Outer.java (at line 4)
								public void foo(Outer Outer.this) {}
								                ^^^^^
							The declared type of the explicit 'this' parameter is expected to be Outer.Inner<K,V>.InnerMost<T>
							----------
							2. ERROR in Outer.java (at line 4)
								public void foo(Outer Outer.this) {}
								                      ^^^^^^^^^^
							The explicit 'this' parameter for a method cannot have a qualifying name
							----------
							3. ERROR in Outer.java (at line 5)
								public void foo(Inner<K,V> Inner.this, int i) {}
								                ^^^^^
							The declared type of the explicit 'this' parameter is expected to be Outer.Inner<K,V>.InnerMost<T>
							----------
							4. ERROR in Outer.java (at line 5)
								public void foo(Inner<K,V> Inner.this, int i) {}
								                           ^^^^^^^^^^
							The explicit 'this' parameter for a method cannot have a qualifying name
							----------
							5. WARNING in Outer.java (at line 6)
								public void foo(InnerMost this, int i, int j) {}
								                ^^^^^^^^^
							Outer.Inner.InnerMost is a raw type. References to generic type Outer.Inner<K,V>.InnerMost<T> should be parameterized
							----------
							6. ERROR in Outer.java (at line 6)
								public void foo(InnerMost this, int i, int j) {}
								                ^^^^^^^^^
							The declared type of the explicit 'this' parameter is expected to be Outer.Inner<K,V>.InnerMost<T>
							----------
							7. ERROR in Outer.java (at line 7)
								public void foo(Inner.InnerMost<T> this, Object obj) {}
								                ^^^^^^^^^^^^^^^
							The member type Outer.Inner.InnerMost<T> must be qualified with a parameterized type, since it is not static
							----------
							8. ERROR in Outer.java (at line 7)
								public void foo(Inner.InnerMost<T> this, Object obj) {}
								                ^^^^^^^^^^^^^^^
							The declared type of the explicit 'this' parameter is expected to be Outer.Inner<K,V>.InnerMost<T>
							----------
							9. ERROR in Outer.java (at line 11)
								public void foo(InnerMost<T> Outer.Inner.InnerMost.this, int i, float f) {}
								                             ^^^^^^^^^^^^^^^^^^^^^^^^^^
							The explicit 'this' parameter for a method cannot have a qualifying name
							----------
							""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383913
	public void test0383913d() {
		this.runNegativeTest(
				new String[] {
						"Outer.java",
						"""
							import java.lang.annotation.Target;
							import static java.lang.annotation.ElementType.*;
							public class Outer {
							    class Inner<K,V> {
									public Inner(@Missing Outer Outer.this) {}
							        class InnerMost<T> {
							            public void bar() {
							                new AnonymousInner() {
							                    public void foobar(AnonymousInner this) {}
							                };
							            }
							            void bar(int i) {
							                class Local {
							                    public int hashCode(Local this, int k) { return 0; }
							                    public int hashCode(Outer.Local this) { return 0; }
							                }
							            }
							        }
							    }
							    static class StaticNested {
							        public StaticNested(@Marker Outer.StaticNested Outer.StaticNested.this) {}
							    }
							    public static void foo(@Marker Outer this) {}
							    public void foo(@Missing Outer this, int i) {}
							}
							interface AnonymousInner {
							    public void foobar(AnonymousInner this);
							}
							@Target(TYPE_USE)
							@interface Marker {}""",

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
								1. ERROR in Outer.java (at line 5)
									public Inner(@Missing Outer Outer.this) {}
									              ^^^^^^^
								Missing cannot be resolved to a type
								----------
								2. ERROR in Outer.java (at line 9)
									public void foobar(AnonymousInner this) {}
									                                  ^^^^
								Explicit 'this' parameter is allowed only in instance methods of non-anonymous classes and inner class constructors
								----------
								3. ERROR in Outer.java (at line 15)
									public int hashCode(Outer.Local this) { return 0; }
									                    ^^^^^^^^^^^
								Outer.Local cannot be resolved to a type
								----------
								4. ERROR in Outer.java (at line 21)
									public StaticNested(@Marker Outer.StaticNested Outer.StaticNested.this) {}
									                    ^^^^^^^
								Type annotations are not allowed on type names used to access static members
								----------
								5. ERROR in Outer.java (at line 21)
									public StaticNested(@Marker Outer.StaticNested Outer.StaticNested.this) {}
									                                                                  ^^^^
								Explicit 'this' parameter is allowed only in instance methods of non-anonymous classes and inner class constructors
								----------
								6. ERROR in Outer.java (at line 23)
									public static void foo(@Marker Outer this) {}
									                                     ^^^^
								Explicit 'this' parameter is allowed only in instance methods of non-anonymous classes and inner class constructors
								----------
								7. ERROR in Outer.java (at line 24)
									public void foo(@Missing Outer this, int i) {}
									                 ^^^^^^^
								Missing cannot be resolved to a type
								----------
								""");
	}
	public void test0383908() {
		this.runNegativeTest(
				new String[]{"X.java",
				"""
					public class X {\s
						void foo(X this) {}
					   void foo() {}
					}
					class Y {
						void foo(Y this) {}
						public static void main(String[] args) {
							new Y().foo();
						}
					}"""},
				"""
					----------
					1. ERROR in X.java (at line 2)
						void foo(X this) {}
						     ^^^^^^^^^^^
					Duplicate method foo() in type X
					----------
					2. ERROR in X.java (at line 3)
						void foo() {}
						     ^^^^^
					Duplicate method foo() in type X
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on nested package names.
	public void test383596() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;
					public class X {
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;
					           ^^^^^^^
				Syntax error, type annotations are illegal here
				----------
				2. ERROR in X.java (at line 1)
					package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;
					                        ^^^^^^^^^^^^^^^
				Syntax error, type annotations are illegal here
				----------
				3. ERROR in X.java (at line 1)
					package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;
					                                           ^^^^^^^^^^^^^^^^^^^^^^^
				Syntax error, type annotations are illegal here
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on nested package names.
	public void test383596a() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					@Marker package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;
					public class X {
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					@Marker package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;
					^^^^^^^
				Package annotations must be in file package-info.java
				----------
				2. ERROR in X.java (at line 1)
					@Marker package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;
					                   ^^^^^^^
				Syntax error, type annotations are illegal here
				----------
				3. ERROR in X.java (at line 1)
					@Marker package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;
					                                ^^^^^^^^^^^^^^^
				Syntax error, type annotations are illegal here
				----------
				4. ERROR in X.java (at line 1)
					@Marker package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;
					                                                   ^^^^^^^^^^^^^^^^^^^^^^^
				Syntax error, type annotations are illegal here
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on nested import names.
	public void test039b() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;
					public class X {
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;
					       ^
				The import p cannot be resolved
				----------
				2. ERROR in X.java (at line 1)
					import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;
					          ^^^^^^^
				Syntax error, type annotations are illegal here
				----------
				3. ERROR in X.java (at line 1)
					import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;
					                       ^^^^^^^^^^^^^^^
				Syntax error, type annotations are illegal here
				----------
				4. ERROR in X.java (at line 1)
					import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;
					                                          ^^^^^^^^^^^^^^^^^^^^^^^
				Syntax error, type annotations are illegal here
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on nested import names.
	public void test383596b() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;
					public class X {
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;
					       ^
				The import p cannot be resolved
				----------
				2. ERROR in X.java (at line 1)
					import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;
					          ^^^^^^^
				Syntax error, type annotations are illegal here
				----------
				3. ERROR in X.java (at line 1)
					import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;
					                       ^^^^^^^^^^^^^^^
				Syntax error, type annotations are illegal here
				----------
				4. ERROR in X.java (at line 1)
					import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;
					                                          ^^^^^^^^^^^^^^^^^^^^^^^
				Syntax error, type annotations are illegal here
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on nested static import names.
	public void test041() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.z2;
							public class X {
							}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.z2;
						              ^
					The import p cannot be resolved
					----------
					2. ERROR in X.java (at line 1)
						import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.z2;
						                 ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					3. ERROR in X.java (at line 1)
						import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.z2;
						                              ^^^^^^^^^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					4. ERROR in X.java (at line 1)
						import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.z2;
						                                                 ^^^^^^^^^^^^^^^^^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on nested static import names.
	public void test042() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;
							public class X {
							}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;
						              ^
					The import p cannot be resolved
					----------
					2. ERROR in X.java (at line 1)
						import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;
						                 ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					3. ERROR in X.java (at line 1)
						import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;
						                              ^^^^^^^^^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					4. ERROR in X.java (at line 1)
						import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;
						                                                 ^^^^^^^^^^^^^^^^^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on Qualified name in explicit this.
	// Much water has flown under the bridge. The grammar itself does not allow annotations in qualified name in explicit this.
	// We now use the production UnannotatableName instead of plain Name.
	public void test043() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					   class Y {
					       class Z {
					           Z(X. @Marker Y  Y.this) {
					           }
					       }
					    }
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					Z(X. @Marker Y  Y.this) {
					      ^^^^^^
				Marker cannot be resolved to a type
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on Qualified name in explicit constructor call -- super form
	public void test044() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								static X x;
								public class InnerBar {
								}
								public class SubInnerBar extends InnerBar {
									SubInnerBar() {
										X.@Marker x. @Marker @Marker @Marker x.super();
									}
								}
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						X.@Marker x. @Marker @Marker @Marker x.super();
						  ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					2. ERROR in X.java (at line 7)
						X.@Marker x. @Marker @Marker @Marker x.super();
						             ^^^^^^^^^^^^^^^^^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					3. WARNING in X.java (at line 7)
						X.@Marker x. @Marker @Marker @Marker x.super();
						                                     ^
					The static field X.x should be accessed in a static way
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on Qualified name in explicit constructor call, super form with explicit type arguments
	public void test045() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								static X x;
								public class InnerBar {
								}
								public class SubInnerBar extends InnerBar {
									SubInnerBar() {
										X.@Marker x. @Marker @Marker @Marker x.<String>super();
									}
								}
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						X.@Marker x. @Marker @Marker @Marker x.<String>super();
						  ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					2. ERROR in X.java (at line 7)
						X.@Marker x. @Marker @Marker @Marker x.<String>super();
						             ^^^^^^^^^^^^^^^^^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					3. WARNING in X.java (at line 7)
						X.@Marker x. @Marker @Marker @Marker x.<String>super();
						                                     ^
					The static field X.x should be accessed in a static way
					----------
					4. WARNING in X.java (at line 7)
						X.@Marker x. @Marker @Marker @Marker x.<String>super();
						                                        ^^^^^^
					Unused type arguments for the non generic constructor X.InnerBar() of type X.InnerBar; it should not be parameterized with arguments <String>
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on Qualified name in explicit constructor call - this form
	public void test046() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								Bar bar;
								class Bar {
									//static Bar x;
									public class InnerBar {
										InnerBar(Bar x) {
										}
									}
									public class SubInnerBar extends InnerBar {
										SubInnerBar() {
											X. @Marker bar.this();
										}
									}
								}
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 11)
						X. @Marker bar.this();
						^^^^^^^^^^^^^^
					Illegal enclosing instance specification for type X.Bar.SubInnerBar
					----------
					2. ERROR in X.java (at line 11)
						X. @Marker bar.this();
						^^^^^^^^^^^^^^
					Cannot make a static reference to the non-static field X.bar
					----------
					3. ERROR in X.java (at line 11)
						X. @Marker bar.this();
						   ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on Qualified name in explicit constructor call, this form with explicit type arguments
	public void test047() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								Bar bar;
								class Bar {
									//static Bar x;
									public class InnerBar {
										InnerBar(Bar x) {
										}
									}
									public class SubInnerBar extends InnerBar {
										SubInnerBar() {
											X.@Marker bar.<String>this();
										}
									}
								}
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 11)
						X.@Marker bar.<String>this();
						^^^^^^^^^^^^^
					Illegal enclosing instance specification for type X.Bar.SubInnerBar
					----------
					2. ERROR in X.java (at line 11)
						X.@Marker bar.<String>this();
						^^^^^^^^^^^^^
					Cannot make a static reference to the non-static field X.bar
					----------
					3. ERROR in X.java (at line 11)
						X.@Marker bar.<String>this();
						  ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					4. WARNING in X.java (at line 11)
						X.@Marker bar.<String>this();
						               ^^^^^^
					Unused type arguments for the non generic constructor X.Bar.SubInnerBar() of type X.Bar.SubInnerBar; it should not be parameterized with arguments <String>
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on Qualified name in PrimaryNoNewArray
	public void test048() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								X bar;
								private void foo(X x) {
									System.out.println((x. @Marker bar));
								}
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						System.out.println((x. @Marker bar));
						                       ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified this.
	public void test049() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								class Y {
									class Z {
										void foo() {
											Object o = X.@Marker Y.this;\s
										}
									}
								}
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						Object o = X.@Marker Y.this;\s
						             ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified super.
	public void test050() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public class Y  {
									public void foo() {
										X. @Marker Y.super.hashCode();
									}
								}
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						X. @Marker Y.super.hashCode();
						   ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in Name.class
	public void test051() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public class Y  {
									public void foo() {
										Class<?> c = X. @Marker @Illegal Y.class;
									}
								}
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						Class<?> c = X. @Marker @Illegal Y.class;
						                ^^^^^^^^^^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in Name [].class.
	public void test052() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public class Y  {
									public void foo() {
										Class<?> c = X. @Marker @Another Y @YetMore [].class;
									}
								}
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						Class<?> c = X. @Marker @Another Y @YetMore [].class;
						                ^^^^^^^^^^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					2. ERROR in X.java (at line 4)
						Class<?> c = X. @Marker @Another Y @YetMore [].class;
						                                   ^^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in binary expressions with qualified names.
	public void test053() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
							    static int x;
							    static boolean fb;
								 public void foo(boolean b) {
									x = (X.@Marker x * 10);
									x = (X.@Marker x / 10);
									x = (X.@Marker x % 10);
									x = (X.@Marker x + 10);
									x = (X.@Marker x - 10);
									x = (X.@Marker x << 10);
									x = (X.@Marker x >> 10);
									x = (X.@Marker x >>> 10);
									b = (X.@Marker x < 10);
									b = (X.@Marker x > 10);
									b = (X.@Marker x <= 10);
									b = (X.@Marker x >= 10);
									b = (X.@Marker x instanceof Object);
									b = (X.@Marker x == 10);
									b = (X.@Marker x != 10);
									x = (X.@Marker x & 10);
									x = (X.@Marker x ^ 10);
									x = (X.@Marker x | 10);
									fb = (X.@Marker fb && true);
									fb = (X.@Marker fb || true);
									x = (X.@Marker fb ? 10 : 10);
								 }
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						x = (X.@Marker x * 10);
						       ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					2. ERROR in X.java (at line 6)
						x = (X.@Marker x / 10);
						       ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					3. ERROR in X.java (at line 7)
						x = (X.@Marker x % 10);
						       ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					4. ERROR in X.java (at line 8)
						x = (X.@Marker x + 10);
						       ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					5. ERROR in X.java (at line 9)
						x = (X.@Marker x - 10);
						       ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					6. ERROR in X.java (at line 10)
						x = (X.@Marker x << 10);
						       ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					7. ERROR in X.java (at line 11)
						x = (X.@Marker x >> 10);
						       ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					8. ERROR in X.java (at line 12)
						x = (X.@Marker x >>> 10);
						       ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					9. ERROR in X.java (at line 13)
						b = (X.@Marker x < 10);
						       ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					10. ERROR in X.java (at line 14)
						b = (X.@Marker x > 10);
						       ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					11. ERROR in X.java (at line 15)
						b = (X.@Marker x <= 10);
						       ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					12. ERROR in X.java (at line 16)
						b = (X.@Marker x >= 10);
						       ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					13. ERROR in X.java (at line 17)
						b = (X.@Marker x instanceof Object);
						    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Incompatible conditional operand types int and Object
					----------
					14. ERROR in X.java (at line 17)
						b = (X.@Marker x instanceof Object);
						       ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					15. ERROR in X.java (at line 18)
						b = (X.@Marker x == 10);
						       ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					16. ERROR in X.java (at line 19)
						b = (X.@Marker x != 10);
						       ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					17. ERROR in X.java (at line 20)
						x = (X.@Marker x & 10);
						       ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					18. ERROR in X.java (at line 21)
						x = (X.@Marker x ^ 10);
						       ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					19. ERROR in X.java (at line 22)
						x = (X.@Marker x | 10);
						       ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					20. ERROR in X.java (at line 23)
						fb = (X.@Marker fb && true);
						        ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					21. ERROR in X.java (at line 24)
						fb = (X.@Marker fb || true);
						        ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					22. ERROR in X.java (at line 25)
						x = (X.@Marker fb ? 10 : 10);
						       ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					""");
	}
	/* https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in annotations with qualified names.
	   This test is disabled. Now the grammar itself forbids annotations in the said place by using the production
	   AnnotationName ::= '@' UnannotatableName. We don't want to add tests that will be fragile and unstable due to
	   syntax. If a construct is provably not parsed at the grammar level, that ought to be good enough.
	*/
	public void test054() throws Exception {
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified names used as annotation values.
	public void test055() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						@interface Annot {
							String bar();
						}
						@Annot(bar = X. @Marker s)
						public class X {
							final static String s = "";
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						@Annot(bar = X. @Marker s)
						                ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified names that are postfix expressions.
	public void test056() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    static int x;
						    int foo() {
						        return X.@Marker x;
						    }
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						return X.@Marker x;
						         ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified names used in array access.
	public void test057() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    static int x[];
						    int foo() {
						        return X.@Marker x[0];
						    }
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						return X.@Marker x[0];
						         ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified name with type arguments used in method invocation.
	public void test058() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    static X x;
						    int foo() {
						        return X.@Marker x.<String> foo();
						    }
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						return X.@Marker x.<String> foo();
						         ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					2. WARNING in X.java (at line 4)
						return X.@Marker x.<String> foo();
						                    ^^^^^^
					Unused type arguments for the non generic method foo() of type X; it should not be parameterized with arguments <String>
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified name used in method invocation.
	public void test059() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    static X x;
						    int foo() {
						        return X.@Marker x. @Blah foo();
						    }
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						return X.@Marker x. @Blah foo();
						         ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					2. ERROR in X.java (at line 4)
						return X.@Marker x. @Blah foo();
						                    ^^^^^
					Syntax error, type annotations are illegal here
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified name used in class instance creation
	public void test060() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    static Y y;
						    class Y {
						        class Z {
						            void foo() {
						                Z z = X. @Marker y.new Z();
						            }
						        }
						    }
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						Z z = X. @Marker y.new Z();
						         ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified name used in class instance creation
	public void test061() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    static X x;
						    X getX() {
						        return (X.@Marker x);
						    }
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						return (X.@Marker x);
						          ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					""");
	}
	public void test062() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public <T> @Marker Object foo() {
								return null;\
							}
						}
						@interface Marker {
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						public <T> @Marker Object foo() {
						           ^^^^^^^
					Annotation types that do not specify explicit target element types cannot be applied here
					----------
					""");
	}
	public void test063() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							Object o = @Marker int.class;
						}
						@interface Marker {
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						Object o = @Marker int.class;
						           ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					""");
	}
	public void test064() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						@interface X {
							<T> @Marker String foo();
						}
						@interface Marker {
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						<T> @Marker String foo();
						    ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					2. ERROR in X.java (at line 2)
						<T> @Marker String foo();
						                   ^^^^^
					Annotation attributes cannot be generic
					----------
					""");
	}
	public void test065() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							Object o = new <String> @Marker X();
						}
						@interface Marker {
						}
						"""
				},
				"""
					----------
					1. WARNING in X.java (at line 2)
						Object o = new <String> @Marker X();
						                ^^^^^^
					Unused type arguments for the non generic constructor X() of type X; it should not be parameterized with arguments <String>
					----------
					2. ERROR in X.java (at line 2)
						Object o = new <String> @Marker X();
						                        ^^^^^^^
					Annotation types that do not specify explicit target element types cannot be applied here
					----------
					""");
	}
	public void test066() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							Object o = new X().new <String> @Marker X();
						}
						@interface Marker {
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						Object o = new X().new <String> @Marker X();
						                                ^^^^^^^
					Annotation types that do not specify explicit target element types cannot be applied here
					----------
					2. ERROR in X.java (at line 2)
						Object o = new X().new <String> @Marker X();
						                                ^^^^^^^^^
					X.X cannot be resolved to a type
					----------
					""");
	}
	public void test067() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							Object o = x.new <String> @Marker X() {};
						}
						@interface Marker {
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						Object o = x.new <String> @Marker X() {};
						           ^
					x cannot be resolved to a variable
					----------
					""");
	}
	public void test068() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							Object o = new <String> @Marker X() {};
						}
						@interface Marker {
						}
						"""
				},
				"""
					----------
					1. WARNING in X.java (at line 2)
						Object o = new <String> @Marker X() {};
						                ^^^^^^
					Unused type arguments for the non generic constructor X() of type X; it should not be parameterized with arguments <String>
					----------
					2. ERROR in X.java (at line 2)
						Object o = new <String> @Marker X() {};
						                        ^^^^^^^
					Annotation types that do not specify explicit target element types cannot be applied here
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=385293
	public void test069() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						class X<final T> {
							Object o = (Object) (public X<final String>) null;
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						class X<final T> {
						        ^^^^^
					Syntax error on token "final", delete this token
					----------
					2. ERROR in X.java (at line 2)
						Object o = (Object) (public X<final String>) null;
						                     ^^^^^^
					Syntax error on token "public", delete this token
					----------
					3. ERROR in X.java (at line 2)
						Object o = (Object) (public X<final String>) null;
						                              ^^^^^
					Syntax error on token "final", delete this token
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388085
	public void test0388085() {
		this.runNegativeTest(
				new String[] {"X.java",
						"""
							class X {
								public void main() {
									final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;\
									one = null;
								}
							}
							class One<R> {}
							class Two<S> {}
							class Three<T> {}
							class Four<U, V> {}
							"""},
							"""
								----------
								1. ERROR in X.java (at line 3)
									final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;		one = null;
									           ^^^^^^
								Marker cannot be resolved to a type
								----------
								2. ERROR in X.java (at line 3)
									final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;		one = null;
									                                 ^^^^^^
								Marker cannot be resolved to a type
								----------
								3. ERROR in X.java (at line 3)
									final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;		one = null;
									                                                                      ^^^^^^
								Marker cannot be resolved to a type
								----------
								4. ERROR in X.java (at line 3)
									final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;		one = null;
									                                                                                              ^^^^^^
								Marker cannot be resolved to a type
								----------
								""");
	}
	public void test0388085a() {
		this.runNegativeTest(
				new String[] {"X.java",
						"""
							import java.lang.annotation.Target;
							import static java.lang.annotation.ElementType.*;
							class X {
								public void main() {
									final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;\
									one = null;
								}
							}
							class One<R> {}
							class Two<S> {}
							class Three<T> {}
							class Four<U, V> {}
							@interface Marker {}"""},
						"""
							----------
							1. ERROR in X.java (at line 5)
								final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;		one = null;
								          ^^^^^^^
							Annotation types that do not specify explicit target element types cannot be applied here
							----------
							2. ERROR in X.java (at line 5)
								final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;		one = null;
								                                ^^^^^^^
							Annotation types that do not specify explicit target element types cannot be applied here
							----------
							3. ERROR in X.java (at line 5)
								final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;		one = null;
								                                                                     ^^^^^^^
							Annotation types that do not specify explicit target element types cannot be applied here
							----------
							4. ERROR in X.java (at line 5)
								final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;		one = null;
								                                                                                             ^^^^^^^
							Annotation types that do not specify explicit target element types cannot be applied here
							""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=390882
	public void test0390882() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.lang.annotation.Target;
						import static java.lang.annotation.ElementType.*;
						public class X {   \s
						 \
							Object o1 = (@Marker java.lang.Integer) null;   // 1. Right.
							Object o2 = (java. @Marker lang.Integer) null;  // 2. Wrong.
							Object o3 = (java.lang. @Marker Integer) null;  // 3. Legal.
							public void foo(java. @Marker lang.Integer arg) {}
							public void bar(java.lang. @Marker Integer arg) {}
							public void foobar(@Marker java.lang.Integer arg) {}
						}
						@Target(TYPE_USE)
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
					1. ERROR in X.java (at line 4)
						Object o1 = (@Marker java.lang.Integer) null;   // 1. Right.
						             ^^^^^^^
					Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)
					----------
					2. ERROR in X.java (at line 5)
						Object o2 = (java. @Marker lang.Integer) null;  // 2. Wrong.
						                   ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					3. ERROR in X.java (at line 7)
						public void foo(java. @Marker lang.Integer arg) {}
						                      ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					4. ERROR in X.java (at line 9)
						public void foobar(@Marker java.lang.Integer arg) {}
						                   ^^^^^^^
					Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)
					----------
					""");
	}
	public void test0390882a() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.lang.annotation.Target;
						import static java.lang.annotation.ElementType.*;
						public class X {   \s
						 \
							Object o1 = (java. @Marker @Annot lang.Integer) null;  // 1. Wrong.
							Object o2 = (java.lang. @Marker @Annot Integer) null;  // 2. Legal
							Object o3 = (java.@lang lang) null;  // 3. Wrong.
						}
						@Target(TYPE_USE)
						@interface Marker {}
						@Target(TYPE_USE)
						@interface Annot {}""",

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
					1. ERROR in X.java (at line 4)
						Object o1 = (java. @Marker @Annot lang.Integer) null;  // 1. Wrong.
						                   ^^^^^^^^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					2. ERROR in X.java (at line 6)
						Object o3 = (java.@lang lang) null;  // 3. Wrong.
						             ^^^^^^^^^^^^^^^
					java.lang cannot be resolved to a type
					----------
					""");
	}
	public void test0390882b() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.lang.annotation.Target;
						import static java.lang.annotation.ElementType.*;
						public class X {   \s
						 \
							Object o1 = (@Marker @Annot java.util.List<String>) null; 	// 1. Wrong.
							Object o2 = (java. @Marker @Annot lang.Integer[]) null;		// 2. Wrong.
							Object o3 = (@Marker @Annot java.util.List<String>[]) null; // 3. Wrong.
							Object o4 = (java.util.List<String> @Marker @Annot []) null; // 4. Right.
							Object o5 = (java.lang.Integer @Marker @Annot []) null;	// 5. Right.
						}
						@Target(TYPE_USE)
						@interface Marker {}
						@Target(TYPE_USE)
						@interface Annot {}""",

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
					1. ERROR in X.java (at line 4)
						Object o1 = (@Marker @Annot java.util.List<String>) null; 	// 1. Wrong.
						             ^^^^^^^
					Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)
					----------
					2. ERROR in X.java (at line 4)
						Object o1 = (@Marker @Annot java.util.List<String>) null; 	// 1. Wrong.
						                     ^^^^^^
					Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)
					----------
					3. ERROR in X.java (at line 5)
						Object o2 = (java. @Marker @Annot lang.Integer[]) null;		// 2. Wrong.
						                   ^^^^^^^^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					4. ERROR in X.java (at line 6)
						Object o3 = (@Marker @Annot java.util.List<String>[]) null; // 3. Wrong.
						             ^^^^^^^
					Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)
					----------
					5. ERROR in X.java (at line 6)
						Object o3 = (@Marker @Annot java.util.List<String>[]) null; // 3. Wrong.
						                     ^^^^^^
					Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=385137
	public void test0385137() {
		this.runNegativeTest(
				new String[]{ "A.java",
				"""
					package p;\
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					public class A<T> {\s
						static class B<T> {\
							static class C<K, V> {\
							}	\
						}
					   public void foo() {
							Object o = (@Marker @Annot A.@Marker B.@Marker C) null;
							Object o2 = (@Marker p.@Marker A.@Marker B.@Marker C) null;
					   }
					}
					@Target(TYPE_USE)
					@interface Marker {}
					@Target(TYPE_USE)
					@interface Annot {}
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
					"""},
					"""
						----------
						1. ERROR in A.java (at line 6)
							Object o = (@Marker @Annot A.@Marker B.@Marker C) null;
							            ^^^^^^^^^^^^^^
						Type annotations are not allowed on type names used to access static members
						----------
						2. WARNING in A.java (at line 6)
							Object o = (@Marker @Annot A.@Marker B.@Marker C) null;
							            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
						A.B.C is a raw type. References to generic type A.B.C<K,V> should be parameterized
						----------
						3. ERROR in A.java (at line 6)
							Object o = (@Marker @Annot A.@Marker B.@Marker C) null;
							                             ^^^^^^^
						Type annotations are not allowed on type names used to access static members
						----------
						4. ERROR in A.java (at line 7)
							Object o2 = (@Marker p.@Marker A.@Marker B.@Marker C) null;
							             ^^^^^^^
						Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)
						----------
						5. WARNING in A.java (at line 7)
							Object o2 = (@Marker p.@Marker A.@Marker B.@Marker C) null;
							             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
						A.B.C is a raw type. References to generic type A.B.C<K,V> should be parameterized
						----------
						6. ERROR in A.java (at line 7)
							Object o2 = (@Marker p.@Marker A.@Marker B.@Marker C) null;
							                       ^^^^^^^
						Type annotations are not allowed on type names used to access static members
						----------
						7. ERROR in A.java (at line 7)
							Object o2 = (@Marker p.@Marker A.@Marker B.@Marker C) null;
							                                 ^^^^^^^
						Type annotations are not allowed on type names used to access static members
						----------
						""");
	}
	public void test0385137a() {
		this.runNegativeTest(
				new String[]{"A.java",
				"""
					package p;\
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					public class A {\s
						static class B<T> {\
							static class C<K, V> {\
							}	\
						}
					   public void foo() {
							Object o1 = (@Marker p.@Marker A.@Marker B.@Marker C[]) null;
							Object o2 = (@Marker @Annot A.@Annot B.C<Integer, String>) null;
							Object o5 = (@Marker @Annot A.B<String>[]) null;
					   }
					}
					@Target(TYPE_USE)
					@interface Marker {}
					@Target(TYPE_USE)
					@interface Annot {}
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
					""",
				},
				"""
					----------
					1. ERROR in A.java (at line 6)
						Object o1 = (@Marker p.@Marker A.@Marker B.@Marker C[]) null;
						             ^^^^^^^
					Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)
					----------
					2. ERROR in A.java (at line 6)
						Object o1 = (@Marker p.@Marker A.@Marker B.@Marker C[]) null;
						                       ^^^^^^^
					Type annotations are not allowed on type names used to access static members
					----------
					3. ERROR in A.java (at line 6)
						Object o1 = (@Marker p.@Marker A.@Marker B.@Marker C[]) null;
						                                 ^^^^^^^
					Type annotations are not allowed on type names used to access static members
					----------
					4. ERROR in A.java (at line 7)
						Object o2 = (@Marker @Annot A.@Annot B.C<Integer, String>) null;
						             ^^^^^^^^^^^^^^
					Type annotations are not allowed on type names used to access static members
					----------
					5. ERROR in A.java (at line 7)
						Object o2 = (@Marker @Annot A.@Annot B.C<Integer, String>) null;
						                              ^^^^^^
					Type annotations are not allowed on type names used to access static members
					----------
					6. ERROR in A.java (at line 8)
						Object o5 = (@Marker @Annot A.B<String>[]) null;
						             ^^^^^^^^^^^^^^
					Type annotations are not allowed on type names used to access static members
					----------
					""");
	}
	public void testBug391196() {
		this.runNegativeTest(
				new String[]{
					"p/Bug391196.java",
					"""
						package p;
						public class Bug391196 {
							@Marker
							public class X<@Marker @Marker2 T> {
								@Marker @Marker2 X(@Marker int i) {}
								@Unresolved X() {}
							}
							@Marker
							enum Color {RED, BLUE}
							@Marker
							interface Inter {}
						@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)
						@interface Marker {}
						@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)
						@interface Marker2 {}
						}
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
						""",
				},
				"""
					----------
					1. ERROR in p\\Bug391196.java (at line 6)
						@Unresolved X() {}
						 ^^^^^^^^^^
					Unresolved cannot be resolved to a type
					----------
					""");
	}
	public void testBug391315() {
		this.runNegativeTest(
				new String[]{
				"X.java",
				"""
					class X<T> {
						X<@Marker ?> l;
						X<@Marker2 ?> l2;
						X<@Marker3 ?> l3;
						class Y {
							void Y1(Y this) {}
						}
					}
					@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)
					@interface Marker {}
					@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)
					@interface Marker2 {}
					@interface Marker3 {}
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
					"""},
				"""
					----------
					1. ERROR in X.java (at line 2)
						X<@Marker ?> l;
						  ^^^^^^^
					The annotation @Marker is disallowed for this location
					----------
					2. ERROR in X.java (at line 4)
						X<@Marker3 ?> l3;
						  ^^^^^^^^
					Annotation types that do not specify explicit target element types cannot be applied here
					----------
					""");
	}
	public void testBug391315a() {
		this.runNegativeTest(
				new String[]{
				"X.java",
				"""
					public class X<@Marker T> {
						@Marker T t;
						T t2 = (@Marker T) null;
					}
					class X2<@Marker2 T> {
						@Marker2 T t;
						T t2 = (@Marker2 T) null;
					}
					@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)
					@interface Marker {}
					@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)
					@interface Marker2 {}""",
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
					"""},
				"""
					----------
					1. ERROR in X.java (at line 6)
						@Marker2 T t;
						^^^^^^^^
					The annotation @Marker2 is disallowed for this location
					----------
					2. ERROR in X.java (at line 7)
						T t2 = (@Marker2 T) null;
						        ^^^^^^^^
					The annotation @Marker2 is disallowed for this location
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391500
	public void testBug391500() {
		this.runNegativeTest(
				new String[]{
				"X.java",
				"""
					public class X {
						class Y {
							class Z {
							}
							Z z1 = new @Marker X().new @Marker Y().new @Marker Z();
							Z z3 = new @Marker Z(){};
						};
					}
					"""},
				"""
					----------
					1. ERROR in X.java (at line 5)
						Z z1 = new @Marker X().new @Marker Y().new @Marker Z();
						            ^^^^^^
					Marker cannot be resolved to a type
					----------
					2. ERROR in X.java (at line 5)
						Z z1 = new @Marker X().new @Marker Y().new @Marker Z();
						                            ^^^^^^
					Marker cannot be resolved to a type
					----------
					3. ERROR in X.java (at line 5)
						Z z1 = new @Marker X().new @Marker Y().new @Marker Z();
						                                            ^^^^^^
					Marker cannot be resolved to a type
					----------
					4. ERROR in X.java (at line 6)
						Z z3 = new @Marker Z(){};
						            ^^^^^^
					Marker cannot be resolved to a type
					----------
					""");
	}
	public void testBug391464() {
		this.runNegativeTest(
				new String[]{
				"X.java",
				"""
					public class X<T> {
						public void foo() {
							Object o = (X @Marker []) null;
							o = (java.lang.String @Marker []) null;
							o = (X<String> @Marker []) null;
							o = (java.util.List<String> @Marker []) null;
							if (o == null) return;
						}\
					}
					"""},
				"""
					----------
					1. ERROR in X.java (at line 3)
						Object o = (X @Marker []) null;
						               ^^^^^^
					Marker cannot be resolved to a type
					----------
					2. ERROR in X.java (at line 4)
						o = (java.lang.String @Marker []) null;
						                       ^^^^^^
					Marker cannot be resolved to a type
					----------
					3. ERROR in X.java (at line 5)
						o = (X<String> @Marker []) null;
						                ^^^^^^
					Marker cannot be resolved to a type
					----------
					4. ERROR in X.java (at line 6)
						o = (java.util.List<String> @Marker []) null;
						                             ^^^^^^
					Marker cannot be resolved to a type
					----------
					""");
	}
	public void testBug391464_2() {
		this.runNegativeTest(
				new String[]{
				"X.java",
				"""
					public class X  {
						class Y {
							class Z {}
						}
						@M X.@M Y.@Unreported Z z = null;
					}
					@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)
					@interface M {
					}
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
					1. ERROR in X.java (at line 5)
						@M X.@M Y.@Unreported Z z = null;
						           ^^^^^^^^^^
					Unreported cannot be resolved to a type
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391108
	public void testBug391108() {
		this.runNegativeTest(
				new String[]{
						"X.java",
						"""
							public class X {
								@Marker @Marker2 @Marker3 public void foo() {}
								@Marker @Marker2 @Marker3 void foo2() {}
							}
							@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)
							@interface Marker {}
							@java.lang.annotation.Target (java.lang.annotation.ElementType.METHOD)
							@interface Marker2 {}
							@java.lang.annotation.Target ({java.lang.annotation.ElementType.TYPE_USE, java.lang.annotation.ElementType.METHOD})
							@interface Marker3 {}""",
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
						@Marker @Marker2 @Marker3 public void foo() {}
						^^^^^^^
					Type annotation is illegal for a method that returns void
					----------
					2. ERROR in X.java (at line 3)
						@Marker @Marker2 @Marker3 void foo2() {}
						^^^^^^^
					Type annotation is illegal for a method that returns void
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=392119
	public void test392119() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					@Marker78 @Marker8 @Marker7
					public class X {
					    Zork z;
					}
					@java.lang.annotation.Target ({java.lang.annotation.ElementType.TYPE_USE, java.lang.annotation.ElementType.TYPE})
					@interface Marker78 {
					}
					@java.lang.annotation.Target ({java.lang.annotation.ElementType.TYPE})
					@interface Marker7 {
					}
					@java.lang.annotation.Target ({java.lang.annotation.ElementType.TYPE_USE})
					@interface Marker8 {
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
		String expectedOutput =
				"""
			  RuntimeInvisibleAnnotations:\s
			    #24 @Marker78(
			    )
			    #25 @Marker8(
			    )
			    #26 @Marker7(
			    )
			  Attribute: MissingTypes Length: 4
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=392119, variant with explicit class file retention.
	public void test392119b() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					@Marker78 @Marker8 @Marker7
					public class X {
					    Zork z;
					}
					@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.CLASS)
					@java.lang.annotation.Target ({java.lang.annotation.ElementType.TYPE_USE, java.lang.annotation.ElementType.TYPE})
					@interface Marker78 {
					}
					@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.CLASS)
					@java.lang.annotation.Target ({java.lang.annotation.ElementType.TYPE})
					@interface Marker7 {
					}
					@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.CLASS)
					@java.lang.annotation.Target ({java.lang.annotation.ElementType.TYPE_USE})
					@interface Marker8 {
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
		String expectedOutput =
				"""
			  RuntimeInvisibleAnnotations:\s
			    #24 @Marker78(
			    )
			    #25 @Marker8(
			    )
			    #26 @Marker7(
			    )
			  Attribute: MissingTypes Length: 4
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=392119, variant with explicit runtime retention.
	public void test392119c() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					@Marker78 @Marker8 @Marker7
					public class X {
					    Zork z;
					}
					@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
					@java.lang.annotation.Target ({java.lang.annotation.ElementType.TYPE_USE, java.lang.annotation.ElementType.TYPE})
					@interface Marker78 {
					}
					@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
					@java.lang.annotation.Target ({java.lang.annotation.ElementType.TYPE})
					@interface Marker7 {
					}
					@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
					@java.lang.annotation.Target ({java.lang.annotation.ElementType.TYPE_USE})
					@interface Marker8 {
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
		String expectedOutput =
				"""
			  RuntimeVisibleAnnotations:\s
			    #24 @Marker78(
			    )
			    #25 @Marker8(
			    )
			    #26 @Marker7(
			    )
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=394355
	public void testBug394355() {
		this.runNegativeTest(
			new String[]{
				"X.java",
				"""
					import java.lang.annotation.Target;
					import static java.lang.annotation.ElementType.*;
					public class X {
						public void foo(@Marker @Marker2 X this) {}
						class Y {
							Y(@Marker @Marker2 X X.this) {}
						}
					}
					@Target (java.lang.annotation.ElementType.TYPE_USE)
					@interface Marker {}
					@Target ({METHOD, PARAMETER, TYPE, PACKAGE, FIELD, CONSTRUCTOR, LOCAL_VARIABLE, TYPE_PARAMETER})
					@interface Marker2 {}""",
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
				1. ERROR in X.java (at line 4)
					public void foo(@Marker @Marker2 X this) {}
					                        ^^^^^^^^
				The annotation @Marker2 is disallowed for this location
				----------
				2. ERROR in X.java (at line 6)
					Y(@Marker @Marker2 X X.this) {}
					          ^^^^^^^^
				The annotation @Marker2 is disallowed for this location
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399453
	public void testBug399453() {
		this.runNegativeTest(
				new String[]{
					"X.java",
					"""
						import java.lang.annotation.Target;
						import static java.lang.annotation.ElementType.*;
						public class X {
							public void foo() {
								int @Marker [][][] i = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [@Marker bar()] @Marker @Marker2 [];
								int @Marker [][][] j = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [@Marker X.bar2(2)] @Marker @Marker2 [];
							}
							public int bar() {
								return 2;
							}
							public static int bar2(int k) {
								return k;
							}
						}
						@Target (java.lang.annotation.ElementType.TYPE_USE)
						@interface Marker {}
						@Target (java.lang.annotation.ElementType.TYPE_USE)
						@interface Marker2 {}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						int @Marker [][][] i = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [@Marker bar()] @Marker @Marker2 [];
						                                                                               ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					2. ERROR in X.java (at line 6)
						int @Marker [][][] j = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [@Marker X.bar2(2)] @Marker @Marker2 [];
						                                                                               ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399453
	public void testBug391894() {
		this.runNegativeTest(
				new String[]{
					"X.java",
					"""
						import java.lang.annotation.Target;
						import static java.lang.annotation.ElementType.*;
						public class X {
							public void foo() {
								int @Marker [][][] i = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [@Marker bar()] ;
								int @Marker [] j = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [@Marker bar()] ;
							}
							public int bar() {
								return 2;
							}
						}
						@Target (java.lang.annotation.ElementType.TYPE_USE)
						@interface Marker {}
						@Target (java.lang.annotation.ElementType.TYPE_USE)
						@interface Marker2 {}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						int @Marker [][][] i = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [@Marker bar()] ;
						                       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Type mismatch: cannot convert from int[][] to int[][][]
					----------
					2. ERROR in X.java (at line 5)
						int @Marker [][][] i = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [@Marker bar()] ;
						                                                                               ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					3. ERROR in X.java (at line 6)
						int @Marker [] j = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [@Marker bar()] ;
						                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Type mismatch: cannot convert from int[][] to int[]
					----------
					4. ERROR in X.java (at line 6)
						int @Marker [] j = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [@Marker bar()] ;
						                                                                           ^^^^^^^
					Syntax error, type annotations are illegal here
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402618, [1.8][compiler] Compiler fails to resolve type annotations on method/constructor references
	public void test402618() {
		this.runNegativeTest(
				new String[]{
					"X.java",
					"""
						import java.util.List;
						interface I {
							void foo(List<String> l);
						}
						
						public class X {
							public void main(String[] args) {
								I i = @Readonly List<@English String>::<@NonNegative Integer>size;
							}
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						I i = @Readonly List<@English String>::<@NonNegative Integer>size;
						       ^^^^^^^^
					Readonly cannot be resolved to a type
					----------
					2. ERROR in X.java (at line 8)
						I i = @Readonly List<@English String>::<@NonNegative Integer>size;
						                      ^^^^^^^
					English cannot be resolved to a type
					----------
					3. WARNING in X.java (at line 8)
						I i = @Readonly List<@English String>::<@NonNegative Integer>size;
						                                        ^^^^^^^^^^^^^^^^^^^^
					Unused type arguments for the non generic method size() of type List<String>; it should not be parameterized with arguments <Integer>
					----------
					4. ERROR in X.java (at line 8)
						I i = @Readonly List<@English String>::<@NonNegative Integer>size;
						                                         ^^^^^^^^^^^
					NonNegative cannot be resolved to a type
					----------
					""");
		}
	public void testBug403132() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							class Y {
								class Z {
									public Z (@A X.@B Y Y.this, String str) {}
						    	 	public void foo (@A X.@B Y.@C Z this, String str) {}
								}
						    }
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						public Z (@A X.@B Y Y.this, String str) {}
						           ^
					A cannot be resolved to a type
					----------
					2. ERROR in X.java (at line 4)
						public Z (@A X.@B Y Y.this, String str) {}
						                ^
					B cannot be resolved to a type
					----------
					3. ERROR in X.java (at line 5)
						public void foo (@A X.@B Y.@C Z this, String str) {}
						                  ^
					A cannot be resolved to a type
					----------
					4. ERROR in X.java (at line 5)
						public void foo (@A X.@B Y.@C Z this, String str) {}
						                       ^
					B cannot be resolved to a type
					----------
					5. ERROR in X.java (at line 5)
						public void foo (@A X.@B Y.@C Z this, String str) {}
						                            ^
					C cannot be resolved to a type
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=403410
	public void testBug403410() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.lang.annotation.Target;
						import static java.lang.annotation.ElementType.*;
						@Target (java.lang.annotation.ElementType.TYPE_USE)
						@interface A {}
						public class X {
							class Y {
								public Y (final @A X X.this) {}
								public Y (static @A X X.this, int i) {}
								public void foo(final @A Y this) {}
								public void foo(static @A Y this, int i) {}
						}
						}"""},
					"""
						----------
						1. ERROR in X.java (at line 7)
							public Y (final @A X X.this) {}
							          ^^^^^^^^^^^^^^^^^
						Syntax error, modifiers are not allowed here
						----------
						2. ERROR in X.java (at line 8)
							public Y (static @A X X.this, int i) {}
							          ^^^^^^^^^^^^^^^^^^
						Syntax error, modifiers are not allowed here
						----------
						3. ERROR in X.java (at line 9)
							public void foo(final @A Y this) {}
							                ^^^^^^^^^^^^^^^
						Syntax error, modifiers are not allowed here
						----------
						4. ERROR in X.java (at line 10)
							public void foo(static @A Y this, int i) {}
							                ^^^^^^^^^^^^^^^^
						Syntax error, modifiers are not allowed here
						----------
						""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=403581,  [1.8][compiler] Compile error on varargs annotations.
	public void test403581() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.util.List;
						public class X {
							void foo(List<String> @Marker ... ls) {}
						}
						@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE)
						@interface Marker {
						}
						"""
				},
				"""
					----------
					1. WARNING in X.java (at line 3)
						void foo(List<String> @Marker ... ls) {}
						                                  ^^
					Type safety: Potential heap pollution via varargs parameter ls
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=392671, [1.8][recovery] NPE with a method with explicit this and a following incomplete parameter
	public void test392671() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						class X {
						    public void foobar(X this, int, int k) {} // NPE!
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						class X {
						        ^
					Syntax error, insert "}" to complete ClassBody
					----------
					2. ERROR in X.java (at line 2)
						public void foobar(X this, int, int k) {} // NPE!
						                           ^^^
					Syntax error, insert "... VariableDeclaratorId" to complete FormalParameter
					----------
					3. ERROR in X.java (at line 3)
						}
						^
					Syntax error on token "}", delete this token
					----------
					""");
	}
	// [1.8][compiler] Missing expected error for incorrect placement of type annotation (https://bugs.eclipse.org/bugs/show_bug.cgi?id=406587)
	public void test406587() {
		this.runNegativeTest(
				new String[] {
					"p/X.java",
					"""
						package p;
						import java.lang.annotation.*;
						public class X {
							@B(1) @A(1) String field1;
							@B @A X.Y field3;
							@A @B p.X.Y field4;
							@B(1) @A(1) java.lang.@A(1) @B(1) String field2;
							public @B(1) @A(1) java.lang. @A(1) @B(1)  String foo(@A(1) @B(1) java.lang. @A(1) @B(1) String str1) {
								@A(1) @B(1)  String local1;
								@A(1) @B(1) java.lang.  @B(1) @A(1) String local2;
								@B @A X.Y local3;
								@B @A p.X.Y local4;
								@B @A p.q.X local5;
								return null;
							}
							class Y {}\
						}
						@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
						@interface A {
							int value() default -1;
						}
						@Target(ElementType.TYPE_USE)
						@interface B {
							int value() default -1;
						}
						"""
				},
				"""
					----------
					1. ERROR in p\\X.java (at line 6)
						@A @B p.X.Y field4;
						   ^^
					Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)
					----------
					2. ERROR in p\\X.java (at line 7)
						@B(1) @A(1) java.lang.@A(1) @B(1) String field2;
						^^
					Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)
					----------
					3. ERROR in p\\X.java (at line 7)
						@B(1) @A(1) java.lang.@A(1) @B(1) String field2;
						                      ^^
					The annotation @A is disallowed for this location
					----------
					4. ERROR in p\\X.java (at line 8)
						public @B(1) @A(1) java.lang. @A(1) @B(1)  String foo(@A(1) @B(1) java.lang. @A(1) @B(1) String str1) {
						       ^^
					Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)
					----------
					5. ERROR in p\\X.java (at line 8)
						public @B(1) @A(1) java.lang. @A(1) @B(1)  String foo(@A(1) @B(1) java.lang. @A(1) @B(1) String str1) {
						                              ^^
					The annotation @A is disallowed for this location
					----------
					6. ERROR in p\\X.java (at line 8)
						public @B(1) @A(1) java.lang. @A(1) @B(1)  String foo(@A(1) @B(1) java.lang. @A(1) @B(1) String str1) {
						                                                            ^^
					Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)
					----------
					7. ERROR in p\\X.java (at line 8)
						public @B(1) @A(1) java.lang. @A(1) @B(1)  String foo(@A(1) @B(1) java.lang. @A(1) @B(1) String str1) {
						                                                                             ^^
					The annotation @A is disallowed for this location
					----------
					8. ERROR in p\\X.java (at line 10)
						@A(1) @B(1) java.lang.  @B(1) @A(1) String local2;
						      ^^
					Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)
					----------
					9. ERROR in p\\X.java (at line 10)
						@A(1) @B(1) java.lang.  @B(1) @A(1) String local2;
						                              ^^
					The annotation @A is disallowed for this location
					----------
					10. ERROR in p\\X.java (at line 12)
						@B @A p.X.Y local4;
						^^
					Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)
					----------
					11. ERROR in p\\X.java (at line 13)
						@B @A p.q.X local5;
						      ^^^
					p.q cannot be resolved to a type
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417076, Eclipse compiler rejects multiple annotations for varargs.
	public void test417076() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.lang.annotation.ElementType;
						import java.lang.annotation.Target;
						@Target(ElementType.TYPE_USE)
						@interface A {
						}
						@Target(ElementType.TYPE_USE)
						@interface B {
						}
						@Target(ElementType.TYPE_USE)
						@interface C {
						}
						public class X {
							public @A String foo(int @B @C @D ... args) {
							      return null;
							}
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 13)
						public @A String foo(int @B @C @D ... args) {
						                                ^
					D cannot be resolved to a type
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417076, Eclipse compiler rejects multiple annotations for varargs.
	public void test417076b() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.lang.annotation.ElementType;
						import java.lang.annotation.Target;
						@Target(ElementType.TYPE_USE)
						@interface A {
						}
						@Target(ElementType.TYPE_USE)
						@interface B {
						}
						@Target(ElementType.TYPE_USE)
						@interface C {
						}
						public class X {
							public @A String foo(int @B @C @A ... args) {
							      return null;
							}
							public @A String goo(int @B @C @A ... args) {
							}
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 16)
						public @A String goo(int @B @C @A ... args) {
						                 ^^^^^^^^^^^^^^^^^^^^^^^^^^
					This method must return a result of type String
					----------
					""");
	}
	// [1.8][compiler] Illegal type annotations not rejected (https://bugs.eclipse.org/bugs/show_bug.cgi?id=415308)
	// This is the basic test case which demonstrated the issue for a local variable.
	// We correctly identified the problem in function bar but failed to do so for foo.
	public void test415308a() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.lang.annotation.ElementType;
						import java.lang.annotation.Target;
						
						@Target(ElementType.TYPE_USE)
						@interface Illegal {
						}
						class Y {
							static class Z {
								Z() {}
							}
						}
						class X {
							Y.Z foo() {
								@Illegal Y.Z z = null;
								return z;
							}
							Y.Z bar() {
								Y.Z z = (@Illegal Y.Z)null;
								return z;
							}
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 14)
						@Illegal Y.Z z = null;
						^^^^^^^^
					Type annotations are not allowed on type names used to access static members
					----------
					2. ERROR in X.java (at line 18)
						Y.Z z = (@Illegal Y.Z)null;
						         ^^^^^^^^
					Type annotations are not allowed on type names used to access static members
					----------
					""");
	}
	// [1.8][compiler] Illegal type annotations not rejected (https://bugs.eclipse.org/bugs/show_bug.cgi?id=415308)
	// This test case is similar to test415308a. SimpleTypes on which annotations are applied are modified to array
	// types.
	public void test415308a2() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							import java.lang.annotation.ElementType;
							import java.lang.annotation.Target;
							
							@Target(ElementType.TYPE_USE)
							@interface Illegal {
							}
							class Y {
								static class Z {
									Z() {}
								}
							}
							class X {
								Y.Z[] foo() {
									@Illegal Y.Z[] z = null;
									return z;
								}
								Y.Z[] bar() {
									Y.Z[] z = (@Illegal Y.Z[])null;
									return z;
								}
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 14)
						@Illegal Y.Z[] z = null;
						^^^^^^^^
					Type annotations are not allowed on type names used to access static members
					----------
					2. ERROR in X.java (at line 18)
						Y.Z[] z = (@Illegal Y.Z[])null;
						           ^^^^^^^^
					Type annotations are not allowed on type names used to access static members
					----------
					""");
	}
	// [1.8][compiler] Illegal type annotations not rejected (https://bugs.eclipse.org/bugs/show_bug.cgi?id=415308)
	// Testing type use annotations on nested types.
	// We check all the qualifiers as we look for a static type. This test checks if we are able to
	// go beyond 1 level as part of the loop.
	public void test415308b() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							import java.lang.annotation.ElementType;
							import java.lang.annotation.Target;
							
							@Target(ElementType.TYPE_USE)
							@interface Illegal {
							}
							class Y {
								static class YY {
									class Z {
										Z() {}
									}
								}
							}
							class X {
								Y.YY.Z foo() {
									@Illegal Y.YY.Z z = null;
									return z;
								}
								Y.YY.Z foo2() {
									Y.@Illegal YY.Z z = null;
									return z;
								}
								Y.YY.Z foo3() {
									Y.YY.@Illegal Z z = null;
									return z;
								}
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 16)
						@Illegal Y.YY.Z z = null;
						^^^^^^^^
					Type annotations are not allowed on type names used to access static members
					----------
					""");
	}
	// [1.8][compiler] Illegal type annotations not rejected (https://bugs.eclipse.org/bugs/show_bug.cgi?id=415308)
	// This test case is similar to test415308a. SimpleTypes on which annotations are applied are modified to array
	// types.
	public void test415308b2() {
		Runner runner = new Runner();
		runner.testFiles =
				new String[] {
						"X.java",
						"""
							import java.lang.annotation.ElementType;
							import java.lang.annotation.Target;
							
							@Target(ElementType.TYPE_USE)
							@interface Illegal {
							}
							class Y {
								static class YY {
									class Z {
										Z() {}
									}
								}
							}
							class X {
								Y.YY.Z[] foo() {
									@Illegal Y.YY.Z[] z = null;
									return z;
								}
								Y.YY.Z[] foo2() {
									Y.@Illegal YY.Z[] z = null;
									return z;
								}
								Y.YY.Z[] foo3() {
									Y.YY.@Illegal Z[] z = null;
									return z;
								}
							}
							"""
				};
		runner.expectedCompilerLog =
				"""
					----------
					1. ERROR in X.java (at line 16)
						@Illegal Y.YY.Z[] z = null;
						^^^^^^^^
					Type annotations are not allowed on type names used to access static members
					----------
					""";
		runner.javacTestOptions = EclipseJustification.EclipseBug561549;
		runner.runNegativeTest();
	}
	// [1.8][compiler] Illegal type annotations not rejected (https://bugs.eclipse.org/bugs/show_bug.cgi?id=415308)
	// The test case is to validate that we report errors for only type annotations and nothing else in case of
	// of parameter types.
	public void test415308c() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							import java.lang.annotation.ElementType;
							import java.lang.annotation.Target;
							
							@Target(ElementType.TYPE_USE)
							@interface IllegalTypeUse {
							}
							@Target({ElementType.TYPE_USE, ElementType.PARAMETER})
							@interface LegalTypeUseParam {
							}
							@Target(ElementType.PARAMETER)
							@interface LegalParam {
							}
							class Y {
								static class Z {
									Z() {}
								}
							}
							class X {
								Y.Z foo(@LegalParam Y.Z z) { //Legal
									return z;
								}
								Y.Z foo2(@LegalTypeUseParam Y.Z z) { //Legal
									return z;
								}
								Y.Z foo3(@IllegalTypeUse @LegalParam Y.Z z) { //Illegal
									return z;
								}
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 25)
						Y.Z foo3(@IllegalTypeUse @LegalParam Y.Z z) { //Illegal
						         ^^^^^^^^^^^^^^^
					Type annotations are not allowed on type names used to access static members
					----------
					""");
	}
	//[1.8][compiler] Illegal type annotations not rejected (https://bugs.eclipse.org/bugs/show_bug.cgi?id=415308)
	//The test case is to validate type use annotation for class fields.
	public void test415308d() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							import java.lang.annotation.ElementType;
							import java.lang.annotation.Target;
							
							@Target(ElementType.TYPE_USE)
							@interface Illegal {
							}
							class Y {
								static class Z {
									Z() {}
								}
							}
							class X {
							   @Illegal\s
								Y.Z z;
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 13)
						@Illegal\s
						^^^^^^^^
					Type annotations are not allowed on type names used to access static members
					----------
					""");
	}
	//[1.8][compiler] Illegal type annotations not rejected (https://bugs.eclipse.org/bugs/show_bug.cgi?id=415308)
	//The test case checks for annotations which are not exclusively TYPE_USE. We should not report a error.
	public void test415308d2() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							import java.lang.annotation.ElementType;
							import java.lang.annotation.Target;
							
							@Target({ElementType.TYPE_USE, ElementType.FIELD})
							@interface Legal {
							}
							class Y {
								static class Z {
									Z() {}
								}
							}
							class X {
							   @Legal\s
								Y.Z z;
							}
							"""
				},
				"");
	}
	//[1.8][compiler] Illegal type annotations not rejected (https://bugs.eclipse.org/bugs/show_bug.cgi?id=415308)
	//The test case is to validate type use annotation for class fields.
	//We check all the qualifiers as we look for a static type. This test checks if we are able to
	//go beyond 1 level as part of the loop.
	public void test415308e() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							import java.lang.annotation.ElementType;
							import java.lang.annotation.Target;
							
							@Target(ElementType.TYPE_USE)
							@interface Illegal {
							}
							@Target(ElementType.TYPE_USE)
							@interface Illegal2 {
							}
							@Target(ElementType.FIELD)
							@interface Legal {
							}
							class Y {
								static class YY {
									class Z {
										Z() {}
									}
								}
							}
							class X {
							   @Legal @Illegal @Illegal2
								Y.YY.Z z;
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 21)
						@Legal @Illegal @Illegal2
						       ^^^^^^^^
					Type annotations are not allowed on type names used to access static members
					----------
					2. ERROR in X.java (at line 21)
						@Legal @Illegal @Illegal2
						                ^^^^^^^^^
					Type annotations are not allowed on type names used to access static members
					----------
					""");
	}
	// [1.8][compiler] Illegal type annotations not rejected (https://bugs.eclipse.org/bugs/show_bug.cgi?id=415308)
	// The test case is to validate type use annotations on return types for methods.
	public void test415308f() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							import java.lang.annotation.ElementType;
							import java.lang.annotation.Target;
							
							@Target(ElementType.TYPE_USE)
							@interface Illegal {
							}
							class Y {
								static class Z {
									Z() {}
								}
							}
							class X {
							   public @Illegal Y.Z foo() { return null;}
							}
							"""
				},
				"""
					----------
					1. ERROR in X.java (at line 13)
						public @Illegal Y.Z foo() { return null;}
						       ^^^^^^^^
					Type annotations are not allowed on type names used to access static members
					----------
					""");
	}
	// [1.8][compiler] Illegal type annotations not rejected (https://bugs.eclipse.org/bugs/show_bug.cgi?id=415308)
	// The test case is a array version of test415308f.
	public void test415308f2() {
		Runner runner = new Runner();
		runner.testFiles =
				new String[] {
						"X.java",
						"""
							import java.lang.annotation.ElementType;
							import java.lang.annotation.Target;
							
							@Target(ElementType.TYPE_USE)
							@interface Illegal {
							}
							class Y {
								static class Z {
									Z() {}
								}
							}
							class X {
							   public @Illegal Y.Z[] foo() { return null;}
							}
							"""
				};
		runner.expectedCompilerLog =
				"""
					----------
					1. ERROR in X.java (at line 13)
						public @Illegal Y.Z[] foo() { return null;}
						       ^^^^^^^^
					Type annotations are not allowed on type names used to access static members
					----------
					""";
		runner.javacTestOptions = EclipseJustification.EclipseBug561549;
		runner.runNegativeTest();
	}
	// [1.8][compiler] Illegal type annotations not rejected (https://bugs.eclipse.org/bugs/show_bug.cgi?id=415308)
	// The test case is used to test enums with type annotations.
	public void test415308g() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.lang.annotation.ElementType;
						import java.lang.annotation.Target;
						
						@Target(ElementType.TYPE_USE)
						@interface Illegal {
						}
						class Y {
							enum A { B }
						}
						class X {
							@Illegal Y.A foo(@Illegal Y.A a) {
								return a;
							}
						}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 11)
						@Illegal Y.A foo(@Illegal Y.A a) {
						^^^^^^^^
					Type annotations are not allowed on type names used to access static members
					----------
					2. ERROR in X.java (at line 11)
						@Illegal Y.A foo(@Illegal Y.A a) {
						                 ^^^^^^^^
					Type annotations are not allowed on type names used to access static members
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=418041, NPE during AST creation.
	public void test418041() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.lang.annotation.ElementType;
						import java.lang.annotation.Target;
						import java.util.List;
						@Target(ElementType.TYPE_USE)
						@interface Readonly {
						}
						class UnmodifiableList<T> implements
						@Readonly List<@Readonly T> { }
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						class UnmodifiableList<T> implements
						      ^^^^^^^^^^^^^^^^
					The type UnmodifiableList<T> must implement the inherited abstract method List<T>.addAll(int, Collection<? extends T>)
					----------
					2. ERROR in X.java (at line 7)
						class UnmodifiableList<T> implements
						      ^^^^^^^^^^^^^^^^
					The type UnmodifiableList<T> must implement the inherited abstract method List<T>.addAll(Collection<? extends T>)
					----------
					3. ERROR in X.java (at line 7)
						class UnmodifiableList<T> implements
						      ^^^^^^^^^^^^^^^^
					The type UnmodifiableList<T> must implement the inherited abstract method List<T>.lastIndexOf(Object)
					----------
					4. ERROR in X.java (at line 7)
						class UnmodifiableList<T> implements
						      ^^^^^^^^^^^^^^^^
					The type UnmodifiableList<T> must implement the inherited abstract method List<T>.subList(int, int)
					----------
					5. ERROR in X.java (at line 7)
						class UnmodifiableList<T> implements
						      ^^^^^^^^^^^^^^^^
					The type UnmodifiableList<T> must implement the inherited abstract method List<T>.contains(Object)
					----------
					6. ERROR in X.java (at line 7)
						class UnmodifiableList<T> implements
						      ^^^^^^^^^^^^^^^^
					The type UnmodifiableList<T> must implement the inherited abstract method List<T>.get(int)
					----------
					7. ERROR in X.java (at line 7)
						class UnmodifiableList<T> implements
						      ^^^^^^^^^^^^^^^^
					The type UnmodifiableList<T> must implement the inherited abstract method List<T>.retainAll(Collection<?>)
					----------
					8. ERROR in X.java (at line 7)
						class UnmodifiableList<T> implements
						      ^^^^^^^^^^^^^^^^
					The type UnmodifiableList<T> must implement the inherited abstract method List<T>.clear()
					----------
					9. ERROR in X.java (at line 7)
						class UnmodifiableList<T> implements
						      ^^^^^^^^^^^^^^^^
					The type UnmodifiableList<T> must implement the inherited abstract method List<T>.indexOf(Object)
					----------
					10. ERROR in X.java (at line 7)
						class UnmodifiableList<T> implements
						      ^^^^^^^^^^^^^^^^
					The type UnmodifiableList<T> must implement the inherited abstract method List<T>.toArray(T[])
					----------
					11. ERROR in X.java (at line 7)
						class UnmodifiableList<T> implements
						      ^^^^^^^^^^^^^^^^
					The type UnmodifiableList<T> must implement the inherited abstract method List<T>.toArray()
					----------
					12. ERROR in X.java (at line 7)
						class UnmodifiableList<T> implements
						      ^^^^^^^^^^^^^^^^
					The type UnmodifiableList<T> must implement the inherited abstract method List<T>.isEmpty()
					----------
					13. ERROR in X.java (at line 7)
						class UnmodifiableList<T> implements
						      ^^^^^^^^^^^^^^^^
					The type UnmodifiableList<T> must implement the inherited abstract method List<T>.listIterator(int)
					----------
					14. ERROR in X.java (at line 7)
						class UnmodifiableList<T> implements
						      ^^^^^^^^^^^^^^^^
					The type UnmodifiableList<T> must implement the inherited abstract method List<T>.listIterator()
					----------
					15. ERROR in X.java (at line 7)
						class UnmodifiableList<T> implements
						      ^^^^^^^^^^^^^^^^
					The type UnmodifiableList<T> must implement the inherited abstract method List<T>.add(int, T)
					----------
					16. ERROR in X.java (at line 7)
						class UnmodifiableList<T> implements
						      ^^^^^^^^^^^^^^^^
					The type UnmodifiableList<T> must implement the inherited abstract method List<T>.add(T)
					----------
					17. ERROR in X.java (at line 7)
						class UnmodifiableList<T> implements
						      ^^^^^^^^^^^^^^^^
					The type UnmodifiableList<T> must implement the inherited abstract method List<T>.set(int, T)
					----------
					18. ERROR in X.java (at line 7)
						class UnmodifiableList<T> implements
						      ^^^^^^^^^^^^^^^^
					The type UnmodifiableList<T> must implement the inherited abstract method List<T>.size()
					----------
					19. ERROR in X.java (at line 7)
						class UnmodifiableList<T> implements
						      ^^^^^^^^^^^^^^^^
					The type UnmodifiableList<T> must implement the inherited abstract method List<T>.containsAll(Collection<?>)
					----------
					20. ERROR in X.java (at line 7)
						class UnmodifiableList<T> implements
						      ^^^^^^^^^^^^^^^^
					The type UnmodifiableList<T> must implement the inherited abstract method List<T>.remove(int)
					----------
					21. ERROR in X.java (at line 7)
						class UnmodifiableList<T> implements
						      ^^^^^^^^^^^^^^^^
					The type UnmodifiableList<T> must implement the inherited abstract method List<T>.remove(Object)
					----------
					22. ERROR in X.java (at line 7)
						class UnmodifiableList<T> implements
						      ^^^^^^^^^^^^^^^^
					The type UnmodifiableList<T> must implement the inherited abstract method List<T>.removeAll(Collection<?>)
					----------
					23. ERROR in X.java (at line 7)
						class UnmodifiableList<T> implements
						      ^^^^^^^^^^^^^^^^
					The type UnmodifiableList<T> must implement the inherited abstract method List<T>.iterator()
					----------
					""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=418041, NPE during AST creation.
	public void test418041a() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {
						}
						@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)
						@interface Marker {}
						"""
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {
						                                          ^
					Y cannot be resolved to a type
					----------
					2. WARNING in X.java (at line 1)
						public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {
						                                                                          ^^^^^^^^^^^^^^^
					The type parameter Q should not be bounded by the final type Integer. Final types cannot be further extended
					----------
					""");
	}
	public void testWildcardCapture() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					import java.util.List;
					@Target(ElementType.TYPE_USE)
					@interface NonNull {
					}
					@Target(ElementType.TYPE_USE)
					@interface Nullable {
					}
					@Target(ElementType.TYPE_USE)
					@interface T {
					}
					public class X {
						public static void main(String[] args) {
							List<@Nullable ? extends X> lx1 = null;
							List<@NonNull ? extends X> lx2 = null;
							lx1 = lx2;
							lx1.add(lx2.get(0));
							lx1.add(lx1.get(0));
					       getAdd(lx1, lx2);
						}
						static <@NonNull P>  void getAdd(List<P> p1, List<P> p2) {
							p1.add(p2.get(0));
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 18)
					lx1.add(lx2.get(0));
					    ^^^
				The method add(capture#3-of ? extends X) in the type List<capture#3-of ? extends X> is not applicable for the arguments (capture#4-of ? extends X)
				----------
				2. ERROR in X.java (at line 19)
					lx1.add(lx1.get(0));
					    ^^^
				The method add(capture#5-of ? extends X) in the type List<capture#5-of ? extends X> is not applicable for the arguments (capture#6-of ? extends X)
				----------
				3. ERROR in X.java (at line 20)
					getAdd(lx1, lx2);
					^^^^^^
				The method getAdd(List<P>, List<P>) in the type X is not applicable for the arguments (List<capture#7-of ? extends X>, List<capture#8-of ? extends X>)
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=414038, [1.8][compiler] CCE in resolveAnnotations
	public void testBug414038() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.*;
					@Target(ElementType.TYPE_USE)
					@interface NonNull { int[].class value() default 0;}
					public class X extends @NonNull() Object {   \s
					    public static int i = 0;\s
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					@interface NonNull { int[].class value() default 0;}
					                          ^^^^^^
				Syntax error on tokens, delete these tokens
				----------
				""");
	}
	public void testGenericConstructor() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Annotation;
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					@Target(ElementType.TYPE_USE)
					@interface T {
					}\s
					public class X {\s
					
						<P> @T X() {
						}
					   @T <P> X(X x) {
					   }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					<P> @T X() {
					    ^
				Syntax error on token "@", delete this token
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=419833, [1.8] NPE in CompilationUnitProblemFinder and ASTNode
	public void test419833() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.Target;
					import java.lang.annotation.ElementType;
					@Target(ElementType.TYPE_USE)
					@interface T {
					}
					class S {
					}
					interface I {
					}
					public class X extends @T S implements @T  {
						public int foo() {
					       return 0;
						}\t
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 10)
					public class X extends @T S implements @T  {
					                                       ^
				Syntax error on token "@", delete this token
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=420038,  [1.8][compiler] Tolerate type annotations on array dimensions of class literals for now for compatibility.
	public void test420038() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					@Target(ElementType.TYPE_USE)
					@interface T {
					}
					public class X {
						public static void main(String[] args) {
							Class<?> c = int @T [].class;\s
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 8)
					Class<?> c = int @T [].class;\s
					                 ^^
				Syntax error, type annotations are illegal here
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=420284, [1.8][compiler] IllegalStateException from TypeSystem.cacheDerivedType
	public void test420284() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					import java.util.List;
					public class X {
					    void foo(Object o) {
					        Integer i = (Integer & Serializable) o;
					        List<@NonNull Integer> l;
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					List<@NonNull Integer> l;
					      ^^^^^^^
				NonNull cannot be resolved to a type
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391521, [1.8][compiler] Error highlighting is not accurate for type references with type annotations
	public void test391521() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class Y {}
					public class X {
					    Y y1 = (@Marker Z) null;
					    Y y2 = new @Marker Z();
					    Y[] y3 = (@Marker Z[]) null;
					    Y[] y4 = new @Marker Z[0];
					    Y[] y5 = (@Marker Y.Z) null;
					    Y[] y6 = new @Marker Y.  Z();
					    Y[] y7 = (@Marker Y.Z[]) null;
					    Y[] y8 = new @Marker Y[0].  Z;
					    Y[] y9 = new @Marker Y.  Z[0];
					}
					@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)
					@interface Marker{}
					
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Y y1 = (@Marker Z) null;
					                ^
				Z cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 4)
					Y y2 = new @Marker Z();
					                   ^
				Z cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 5)
					Y[] y3 = (@Marker Z[]) null;
					                  ^
				Z cannot be resolved to a type
				----------
				4. ERROR in X.java (at line 6)
					Y[] y4 = new @Marker Z[0];
					                     ^
				Z cannot be resolved to a type
				----------
				5. ERROR in X.java (at line 7)
					Y[] y5 = (@Marker Y.Z) null;
					                  ^^^
				Y.Z cannot be resolved to a type
				----------
				6. ERROR in X.java (at line 8)
					Y[] y6 = new @Marker Y.  Z();
					                     ^^^^^
				Y.Z cannot be resolved to a type
				----------
				7. ERROR in X.java (at line 9)
					Y[] y7 = (@Marker Y.Z[]) null;
					                  ^^^
				Y.Z cannot be resolved to a type
				----------
				8. ERROR in X.java (at line 10)
					Y[] y8 = new @Marker Y[0].  Z;
					                            ^
				Z cannot be resolved or is not a field
				----------
				9. ERROR in X.java (at line 11)
					Y[] y9 = new @Marker Y.  Z[0];
					                     ^^^^^
				Y.Z cannot be resolved to a type
				----------
				""");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=414038, [1.8][compiler] CCE in resolveAnnotations
	public void test414038() {
		runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.lang.annotation.*;
						@Target(ElementType.TYPE_USE)
						@interface NonNull { int[].class value() default 0;}
						public class X extends @NonNull() Object {   \s
						    public static int i = 0;\s
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					@interface NonNull { int[].class value() default 0;}
					                          ^^^^^^
				Syntax error on tokens, delete these tokens
				----------
				""",
			true);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421791,  [1.8][compiler] TYPE_USE annotations should be allowed on annotation type declarations
	public void test421791() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							import java.lang.annotation.ElementType;
							import java.lang.annotation.Target;
							@Target(ElementType.TYPE_USE)
							@interface T {}
							@T
							@interface T2 {}
							public class X {}
							"""
				},
				"",
				true);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424977,  [1.8][compiler]ArrayIndexIndexOutOfBoundException in annotated wrong<> code
	public void testBug426977() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		runNegativeTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					
					public class X {
					    test.@A Outer<>.@A Inner<> i;
					}
					class Outer<T> {
					    class Inner {}
					}
					@Target(ElementType.TYPE_USE)
					@interface A {}
					"""
			},
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					test.@A Outer<>.@A Inner<> i;
					^^^^^^^^^^^^^
				Incorrect number of arguments for type Outer<T>; it cannot be parameterized with arguments <>
				----------
				""",
			null,
			true,
			customOptions);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424977,  [1.8][compiler] ArrayIndexIndexOutOfBoundException in annotated wrong<> code
	public void testBug426977a() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		runNegativeTest(
			new String[] {
				"test/X.java",
				"""
					package test;
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					
					public class X {
					    test.@A Outer<Object>.@A Inner<> i;
					}
					class Outer<T> {
					    class Inner {}
					}
					@Target(ElementType.TYPE_USE)
					@interface A {}
					"""
			},
			"""
				----------
				1. ERROR in test\\X.java (at line 6)
					test.@A Outer<Object>.@A Inner<> i;
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The type Outer<Object>.Inner is not generic; it cannot be parameterized with arguments <>
				----------
				""",
			null,
			true,
			customOptions);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425599, [1.8][compiler] ISE when trying to compile qualified and annotated class instance creation
	public void test425599() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					public class X {
					    Object ax = new @A Outer().new Middle<String>();
					}
					@Target(ElementType.TYPE_USE) @interface A {}
					class Outer {
					    class Middle<E> {}
					}
					"""
			},
			"",
			null,
			true,
			customOptions);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427955, [1.8][compiler] NPE in TypeSystem.getUnannotatedType
	public void test427955() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					/**
					 * @param <K> unused
					 * @param <V> unused
					 */
					public class X {}
					class Outer<K, V> {
					  void method() {
					    //Internal compiler error: java.lang.NullPointerException at
					    // org.eclipse.jdt.internal.compiler.lookup.TypeSystem.getUnannotatedType(TypeSystem.java:76)
					    new Inner<>(null);
					  }
					  final class Inner<K2, V2> {
					    /**
					     * @param next unused\s
					     */
					    Inner(Inner<K2, V2> next) {}
					  }
					}
					"""
			},
			"",
			null,
			true,
			customOptions);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=419827,  [1.8] Annotation with TYPE_USE as target is not allowed to use container with target TYPE
	public void test419827a() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.lang.annotation.ElementType;
						import java.lang.annotation.Repeatable;
						import java.lang.annotation.Target;
						
						@Target({ElementType.TYPE_USE})
						@Repeatable(FooContainer.class)
						@interface Foo {}
						@Target({ElementType.TYPE, ElementType.TYPE_USE})
						@interface FooContainer {
							Foo[] value();
						}
						public class X{}
						"""
				},
				"",
				true);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=419827,  [1.8] Annotation with TYPE_USE as target is not allowed to use container with target TYPE
	// Although the target of FooContainer is different from that of Foo, Foo container cannot be used in any place where
	// Foo can't be used.
	public void test419827b() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.lang.annotation.ElementType;
						import java.lang.annotation.Repeatable;
						import java.lang.annotation.Target;
						
						@Target({ElementType.TYPE_USE})
						@Repeatable(FooContainer.class)
						@interface Foo {}
						@Target({ElementType.TYPE})
						@interface FooContainer {
							Foo[] value();
						}
						public class X{}
						"""
				},
				"",
				true);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=552082
	public void test552082_comment_0() throws Exception {
		this.runNegativeTest(
				new String[] {
					"EclipseReturnValueAnnotationTest.java",
					"""
						class EclipseReturnValueAnnotationTest {
						   \s
						    @interface SomeAnnotation {}
						
						     public @SomeAnnotation String foo(Object anything) {
						         return "foo";
						     }
						
						     public  <T>  @SomeAnnotation String bar(T anything) { // Error - type annotation position
						         return "bar";
						     }
						
						     public @SomeAnnotation <T> String baz(T anything) {  // OK - declaration annotation on method\s
						         return "baz";
						     }
						}
						""",
				},
				"""
					----------
					1. ERROR in EclipseReturnValueAnnotationTest.java (at line 9)
						public  <T>  @SomeAnnotation String bar(T anything) { // Error - type annotation position
						             ^^^^^^^^^^^^^^^
					Annotation types that do not specify explicit target element types cannot be applied here
					----------
					""");
	}
}
