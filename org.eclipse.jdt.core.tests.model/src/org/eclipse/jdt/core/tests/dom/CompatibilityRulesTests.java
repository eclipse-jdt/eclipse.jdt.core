/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import junit.framework.Test;

public class CompatibilityRulesTests extends AbstractASTTests {

	public CompatibilityRulesTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(CompatibilityRulesTests.class);
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
//		TESTS_PREFIX =  "testBug86380";
//		TESTS_NAMES = new String[] { "test032" };
//		TESTS_NUMBERS = new int[] { 83230 };
//		TESTS_RANGE = new int[] { 83304, -1 };
		}

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
	}

	public void tearDownSuite() throws Exception {
		deleteProject("P");
		super.tearDownSuite();
	}

	/*
	 * Ensures that a subtype is subtype compatible with its super type
	 */
	public void test001() throws JavaModelException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y extends X {\n" +
				"}",
			},
			new String[] {
				"Lp1/X;",
				"Lp1/Y;"
			});
		assertTrue("X should be subtype compatible with Y", bindings[1].isSubTypeCompatible(bindings[0]));
	}

	/*
	 * Ensures that a type is subtype compatible with itself
	 */
	public void test002() throws JavaModelException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"}",
			},
			new String[] {
				"Lp1/X;",
			});
		assertTrue("X should be subtype compatible with itself", bindings[0].isSubTypeCompatible(bindings[0]));
	}

	/*
	 * Ensures that a supertype is not subtype compatible with its subtype
	 */
	public void test003() throws JavaModelException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y extends X {\n" +
				"}",
			},
			new String[] {
				"Lp1/X;",
				"Lp1/Y;"
			});
		assertTrue("X should not be subtype compatible with Y", !bindings[0].isSubTypeCompatible(bindings[1]));
	}

	/*
	 * Ensures that a type is not subtype compatible with an unrelated type.
	 */
	public void test004() throws JavaModelException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y {\n" +
				"}",
			},
			new String[] {
				"Lp1/X;",
				"Lp1/Y;"
			});
		assertTrue("X should not be subtype compatible with Y", !bindings[0].isSubTypeCompatible(bindings[1]));
	}

	/*
	 * Ensures that the int base type is not subtype compatible with the long base type
	 */
	public void test005() throws JavaModelException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {},
			new String[] {
				"I",
				"J"
			});
		assertTrue("int should not be subtype compatible with long", !bindings[0].isSubTypeCompatible(bindings[1]));
	}

	/*
	 * Ensures that the int base type is not subtype compatible with the java.lang.Object type
	 */
	public void test006() throws JavaModelException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {},
			new String[] {
				"I",
				"Ljava/lang/Object;"
			});
		assertTrue("int should not be subtype compatible with Object", !bindings[0].isSubTypeCompatible(bindings[1]));
	}

	/*
	 * Ensures that a subtype is assignment compatible with its super type
	 */
	public void test007() throws JavaModelException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y extends X {\n" +
				"}",
			},
			new String[] {
				"Lp1/X;",
				"Lp1/Y;"
			});
		assertTrue("X should be assignment compatible with Y", bindings[1].isAssignmentCompatible(bindings[0]));
	}

	/*
	 * Ensures that a type is assignment compatible with itself
	 */
	public void test008() throws JavaModelException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"}",
			},
			new String[] {
				"Lp1/X;",
			});
		assertTrue("X should be assignment compatible with itself", bindings[0].isAssignmentCompatible(bindings[0]));
	}

	/*
	 * Ensures that a supertype is not assignment compatible with its subtype
	 */
	public void test009() throws JavaModelException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y extends X {\n" +
				"}",
			},
			new String[] {
				"Lp1/X;",
				"Lp1/Y;"
			});
		assertTrue("X should not be assignment compatible with Y", !bindings[0].isAssignmentCompatible(bindings[1]));
	}

	/*
	 * Ensures that a type is not assigment compatible with an unrelated type.
	 */
	public void test010() throws JavaModelException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y {\n" +
				"}",
			},
			new String[] {
				"Lp1/X;",
				"Lp1/Y;"
			});
		assertTrue("X should not be assigment compatible with Y", !bindings[0].isAssignmentCompatible(bindings[1]));
	}

	/*
	 * Ensures that the int base type is assignment compatible with the long base type
	 */
	public void test011() throws JavaModelException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {},
			new String[] {
				"I",
				"J"
			});
		assertTrue("int should be assignment compatible with long", bindings[0].isAssignmentCompatible(bindings[1]));
	}

	/*
	 * Ensures that the int base type is not assignment compatible with the java.lang.Object type in 1.4 mode.
	 */
	public void test012() throws CoreException {
		try {
			IJavaProject project = createJavaProject("P14", new String[] {""}, new String[] {"JCL_LIB"}, "", "1.4");
			ITypeBinding[] bindings = createTypeBindings(
				new String[] {},
				new String[] {
					"I",
					"Ljava/lang/Object;"
				},
				project);
			assertTrue("int should not be assignment compatible with Object", !bindings[0].isAssignmentCompatible(bindings[1]));
		} finally {
			deleteProject("P14");
		}
	}

	/*
	 * Ensures that a subtype is cast compatible with its super type
	 */
	public void test013() throws JavaModelException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y extends X {\n" +
				"}",
			},
			new String[] {
				"Lp1/X;",
				"Lp1/Y;"
			});
		assertTrue("X should be cast compatible with Y", bindings[1].isCastCompatible(bindings[0]));
	}

	/*
	 * Ensures that a type is cast compatible with itself
	 */
	public void test014() throws JavaModelException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"}",
			},
			new String[] {
				"Lp1/X;",
			});
		assertTrue("X should be cast compatible with itself", bindings[0].isCastCompatible(bindings[0]));
	}

	/*
	 * Ensures that a supertype is cast compatible with its subtype
	 */
	public void test015() throws JavaModelException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y extends X {\n" +
				"}",
			},
			new String[] {
				"Lp1/X;",
				"Lp1/Y;"
			});
		assertTrue("X should be cast compatible with Y", bindings[0].isCastCompatible(bindings[1]));
	}

	/*
	 * Ensures that a type is not cast compatible with an unrelated type.
	 */
	public void test016() throws JavaModelException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y {\n" +
				"}",
			},
			new String[] {
				"Lp1/X;",
				"Lp1/Y;"
			});
		assertTrue("X should not be cast compatible with Y", !bindings[0].isCastCompatible(bindings[1]));
	}

	/*
	 * Ensures that the int base type is cast compatible with the long base type
	 */
	public void test017() throws JavaModelException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {},
			new String[] {
				"I",
				"J"
			});
		assertTrue("int should be cast compatible with long", bindings[0].isCastCompatible(bindings[1]));
	}

	/*
	 * Ensures that the int base type is not cast compatible with the java.lang.Object type
	 */
	public void test018() throws JavaModelException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {},
			new String[] {
				"I",
				"Ljava/lang/Object;"
			});
		assertTrue("int should not be cast compatible with Object", !bindings[0].isCastCompatible(bindings[1]));
	}

	/*
	 * Ensures that a method in a subtype overrides the corresponding method in the super type.
	 */
	public void test019() throws JavaModelException {
		IMethodBinding[] bindings = createMethodBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y extends X {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"}",
			},
			new String[] {
				"Lp1/Y;.foo()V",
				"Lp1/X;.foo()V"
			});
		assertTrue("Y#foo() should override X#foo()", bindings[0].overrides(bindings[1]));
	}

	/*
	 * Ensures that a method in a super type doesn't override the corresponding method in a subtype.
	 */
	public void test020() throws JavaModelException {
		IMethodBinding[] bindings = createMethodBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y extends X {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"}",
			},
			new String[] {
				"Lp1/X;.foo()V",
				"Lp1/Y;.foo()V"
			});
		assertTrue("X#foo() should not override Y#foo()", !bindings[0].overrides(bindings[1]));
	}

	/*
	 * Ensures that a method doesn't override the corresponding method in an unrelated type.
	 */
	public void test021() throws JavaModelException {
		IMethodBinding[] bindings = createMethodBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"}",
			},
			new String[] {
				"Lp1/X;.foo()V",
				"Lp1/Y;.foo()V"
			});
		assertTrue("X#foo() should not override Y#foo()", !bindings[0].overrides(bindings[1]));
	}

	/*
	 * Ensures that IMethodBinding#ovverides(IMethodBinding) doesn't throw a NullPointerException if
	 * the method was not built in a batch.
	 * (regression test for bug 79635 NPE when asking an IMethodBinding whether it overrides itself)
	 */
	public void test022() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/P/p1/X.java", true/*compute problems to get bindings*/);
			ASTNode node = buildAST(
				"package p1;\n" +
				"public class X {\n" +
				"  /*start*/void foo() {\n" +
				"  }/*end*/\n" +
				"}",
				workingCopy);
			IMethodBinding methodBinding = ((MethodDeclaration) node).resolveBinding();
			assertTrue("X#foo() should not override itself", !methodBinding.overrides(methodBinding));
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * Ensures that a base type is assignment compatible with its wrapper type
	 * (regression test for bug 80455 [5.0] ITypeBinding.canAssign not aware of type boxing)
	 */
	public void test023() throws JavaModelException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {
				"/P/java/lang/Integer.java",
				"package java.lang;\n" +
				"public class Integer {\n" +
				"}",
			},
			new String[] {
				"I",
				"Ljava/lang/Integer;",
			});
		assertTrue("int should be assignment compatible with Integer", bindings[0].isAssignmentCompatible(bindings[1]));
	}

	/*
	 * Ensures that a base type is assignment compatible with Object
	 */
	public void test024() throws JavaModelException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {},
			new String[] {
				"I",
				"Ljava/lang/Object;",
			});
		assertTrue("int should be assignment compatible with Object", bindings[0].isAssignmentCompatible(bindings[1]));
	}

	/*
	 * Ensures that a method is subsignature of itself.
	 */
	public void test025() throws JavaModelException {
		IMethodBinding[] bindings = createMethodBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"}",
			},
			new String[] {
				"Lp1/X;.foo()V"
			});
		assertTrue("X#foo() should be a subsignature of X#foo()", bindings[0].isSubsignature(bindings[0]));
	}

	/*
	 * Ensures that a method is subsignature of its super method.
	 */
	public void test026() throws JavaModelException {
		IMethodBinding[] bindings = createMethodBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  String foo(Object o) {\n" +
				"  }\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y extends X {\n" +
				"  String foo(Object o) {\n" +
				"  }\n" +
				"}",
			},
			new String[] {
				"Lp1/X;.foo(Ljava/lang/Object;)Ljava/lang/String;",
				"Lp1/Y;.foo(Ljava/lang/Object;)Ljava/lang/String;",
			});
		assertTrue("Y#foo(Object) should be a subsignature of X#foo(Object)", bindings[1].isSubsignature(bindings[0]));
	}

	/*
	 * Ensures that a method is subsignature of its super generic method.
	 */
	public void test027() throws JavaModelException {
		IMethodBinding[] bindings = createMethodBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T>  {\n" +
				"  Z<T> foo(Z<T> o) {\n" +
				"  }\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y extends X {\n" +
				"  Z foo(Z o) {\n" +
				"  }\n" +
				"}",
				"/P/p1/Z.java",
				"package p1;\n" +
				"public class Z<T> {\n" +
				"}",
			},
			new String[] {
				"Lp1/X;.foo(Lp1/Z<TT;>;)Lp1/Z<TT;>;",
				"Lp1/Y;.foo(Lp1/Z;)Lp1/Z;",
			});
		assertTrue("Y#foo(Z) should be a subsignature of X#foo(Z<T>)", bindings[1].isSubsignature(bindings[0]));
	}

	/*
	 * Ensures that a method is not the subsignature of an unrelated method.
	 */
	public void test028() throws JavaModelException {
		IMethodBinding[] bindings = createMethodBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y {\n" +
				"  void bar() {\n" +
				"  }\n" +
				"}",
			},
			new String[] {
				"Lp1/X;.foo()V",
				"Lp1/Y;.bar()V",
			});
		assertTrue("Y#bar() should not be a subsignature of X#foo()", !bindings[1].isSubsignature(bindings[0]));
	}

	/*
	 * Ensures that a method in a subtype doesn't override the a method with same parameters but with different name in the super type.
	 */
	public void test029() throws JavaModelException {
		IMethodBinding[] bindings = createMethodBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y extends X {\n" +
				"  void bar() {\n" +
				"  }\n" +
				"}",
			},
			new String[] {
				"Lp1/Y;.bar()V",
				"Lp1/X;.foo()V"
			});
		assertTrue("Y#bar() should not override X#foo()", !bindings[0].overrides(bindings[1]));
	}

	/*
	 * Ensures that a method in a subtype overrides a method in the super parameterized type.
	 * (regression test for bug 99608 IMethodBinding#overrides returns false on overridden method)
	 */
	public void test030() throws JavaModelException {
		IMethodBinding[] bindings = createMethodBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T> {\n" +
				"  void foo(T t) {\n" +
				"  }\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y extends X<String> {\n" +
				"  void foo(String s) {\n" +
				"  }\n" +
				"}",
			},
			new String[] {
				"Lp1/Y;.foo(Ljava/lang/String;)V",
				"Lp1/X;.foo(TT;)V"
			});
		assertTrue("Y#foo(String) should override X#foo(T)", bindings[0].overrides(bindings[1]));
	}

	/*
	 * Ensures that a method with the same parameter types but with different type parameters is not a subsignature of its super method.
	 * (regression test for bug 107110 IMethodBinding.isSubsignature not yet correctly implemented)
	 */
	public void test031() throws JavaModelException {
		IMethodBinding[] bindings = createMethodBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"}\n" +
				"class Y extends X {\n" +
				"  <T> void foo() {\n" +
				"  }\n" +
				"}",
			},
			new String[] {
				"Lp1/X;.foo()V",
				"Lp1/Y;.foo<T:Ljava/lang/Object;>()V"
			});
		assertFalse("Y#foo() should not be a subsignature of X#foo()", bindings[1].isSubsignature(bindings[0]));
	}

	/*
	 * Ensures that a method in a subtype overrides the corresponding method in the super type
	 * even if the two methods have different return types.
	 * (regression test for bug 105808 [1.5][dom] MethodBinding#overrides(..) should not consider return types)
	 */
	public void test032() throws CoreException {
		try {
			IJavaProject project = createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, "", "1.4");
			IMethodBinding[] bindings = createMethodBindings(
				new String[] {
					"/P2/p1/X.java",
					"package p1;\n" +
					"public class X {\n" +
					"  Object foo() {\n" +
					"  }\n" +
					"}",
					"/P2/p1/Y.java",
					"package p1;\n" +
					"public class Y extends X {\n" +
					"  String foo() {\n" +
					"  }\n" +
					"}",
				},
				new String[] {
					"Lp1/Y;.foo()Ljava/lang/String;",
					"Lp1/X;.foo()Ljava/lang/Object;"
				},
				project);
			assertTrue("Y#foo() should override X#foo()", bindings[0].overrides(bindings[1]));
		} finally {
			deleteProject("P2");
		}
	}

	/*
	 * Ensures that a method in a subtype doesn't override the corresponding private method in the super type.
	 * (regression test for bug 132191 IMethodBinding.overrides(IMethodBinding) returns true even if the given argument is private.)
	 */
	public void test033() throws JavaModelException {
		IMethodBinding[] bindings = createMethodBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  private void foo() {\n" +
				"  }\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y extends X {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"}",
			},
			new String[] {
				"Lp1/Y;.foo()V",
				"Lp1/X;.foo()V"
			});
		assertTrue("Y#foo() should not override X#foo()", !bindings[0].overrides(bindings[1]));
	}

	/*
	 * Ensures that a method in a subtype doesn't override the corresponding default method in the super type in a different package.
	 * (regression test for bug 132191 IMethodBinding.overrides(IMethodBinding) returns true even if the given argument is private.)
	 */
	public void test034() throws JavaModelException {
		IMethodBinding[] bindings = createMethodBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"}",
				"/P/p2/Y.java",
				"package p2;\n" +
				"public class Y extends p1.X {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"}",
			},
			new String[] {
				"Lp2/Y;.foo()V",
				"Lp1/X;.foo()V"
			});
		assertTrue("Y#foo() should not override X#foo()", !bindings[0].overrides(bindings[1]));
	}

	/*
	 * Ensures that a method with different paramter types is not a subsignature of its super method.
	 * (regression test for bug 111093 More problems with IMethodBinding#isSubsignature(..))
	 */
	public void test035() throws JavaModelException {
		IMethodBinding[] bindings = createMethodBindings(
			new String[] {
				"/P/p1/A.java",
				"package p1;\n" +
				"public class A<T> {\n" +
				"  public void o1_xoo2(A<?> s) {\n" +
				"  }\n" +
				"}\n" +
				"class B<S> extends A<S> {\n" +
				"  @Override\n" +
				"  public void o1_xoo2(A<Object> s) {\n" +
				"  }\n" +
				"}",
			},
			new String[] {
				"Lp1/A;.o1_xoo2(Lp1/A<*>;)V",
				"Lp1/A~B;.o1_xoo2(Lp1/A<Ljava/lang/Object;>;)V"
			});
		assertFalse("B#o1_xoo2() should not be a subsignature of A#o1_xoo2()", bindings[1].isSubsignature(bindings[0]));
	}

	/*
	 * Ensures that a method with different paramter types is not a subsignature of its super method.
	 * (regression test for bug 111093 More problems with IMethodBinding#isSubsignature(..))
	 */
	public void test036() throws JavaModelException {
		IMethodBinding[] bindings = createMethodBindings(
			new String[] {
				"/P/p1/A.java",
				"package p1;\n" +
				"public class A<T> {\n" +
				"  public void o1_xoo3(A<? extends T> s) {\n" +
				"  }\n" +
				"}\n" +
				"class B<S> extends A<S> {\n" +
				"  @Override\n" +
				"  public void o1_xoo3(A<? super S> s) {\n" +
				"  }\n" +
				"}",
			},
			new String[] {
				"Lp1/A;.o1_xoo3(Lp1/A<+TT;>;)V",
				"Lp1/A~B;.o1_xoo3(Lp1/A<-TS;>;)V"
			});
		assertFalse("B#o1_xoo3() should not be a subsignature of A#o1_xoo3()", bindings[1].isSubsignature(bindings[0]));
	}

	/*
	 * Ensures that a method with different paramter types is not a subsignature of its super method.
	 * (regression test for bug 111093 More problems with IMethodBinding#isSubsignature(..))
	 */
	public void test037() throws JavaModelException {
		IMethodBinding[] bindings = createMethodBindings(
			new String[] {
				"/P/p1/A.java",
				"package p1;\n" +
				"public class A<S, T> {\n" +
				"  public void o2_xoo1(List<? extends T> t) {\n" +
				"  }\n" +
				"}\n" +
				"class B<V, W> extends A<W, V> {\n" +
				"  @Override\n" +
				"  public void o2_xoo1(List<? extends W> t) {\n" +
				"  }\n" +
				"}\n" +
				"class List<T> {\n" +
				"}",
			},
			new String[] {
				"Lp1/A;.o2_xoo1(Lp1/List<+TT;>;)V",
				"Lp1/A~B;.o2_xoo1(Lp1/List<+TW;>;)V"
			});
		assertFalse("B#o1_xoo1() should not be a subsignature of A#o1_xoo1()", bindings[1].isSubsignature(bindings[0]));
	}

	/*
	 * Ensures that a method with different paramter types is not a subsignature of its super method.
	 * (regression test for bug 111093 More problems with IMethodBinding#isSubsignature(..))
	 */
	public void test038() throws JavaModelException {
		IMethodBinding[] bindings = createMethodBindings(
			new String[] {
				"/P/p1/A.java",
				"package p1;\n" +
				"public class A {\n" +
				"  public void o3_xoo1(List t) {\n" +
				"  }\n" +
				"}\n" +
				"class B extends A {\n" +
				"  @Override\n" +
				"  public void o3_xoo1(List<Object> t) {\n" +
				"  }\n" +
				"}\n" +
				"class List<T> {\n" +
				"}",
			},
			new String[] {
				"Lp1/A;.o3_xoo1(Lp1/List;)V",
				"Lp1/A~B;.o3_xoo1(Lp1/List<Ljava/lang/Object;>;)V"
			});
		assertFalse("B#o3_xoo1() should not be a subsignature of A#o3_xoo1()", bindings[1].isSubsignature(bindings[0]));
	}

	/*
	 * Ensures that a method with different paramter types is not a subsignature of its super method.
	 * (regression test for bug 111093 More problems with IMethodBinding#isSubsignature(..))
	 */
	public void test039() throws JavaModelException {
		IMethodBinding[] bindings = createMethodBindings(
			new String[] {
				"/P/p1/A.java",
				"package p1;\n" +
				"public class A<T> {\n" +
				"  public void o4_xoo1(T t) {\n" +
				"  }\n" +
				"}\n" +
				"class B extends A<List<String>> {\n" +
				"  @Override\n" +
				"  public void o4_xoo1(List<?> t) {\n" +
				"  }\n" +
				"}\n" +
				"class List<T> {\n" +
				"}",
			},
			new String[] {
				"Lp1/A;.o4_xoo1(TT;)V",
				"Lp1/A~B;.o4_xoo1(Lp1/List<*>;)V"
			});
		assertFalse("B#o4_xoo1() should not be a subsignature of A#o4_xoo1()", bindings[1].isSubsignature(bindings[0]));
	}

	/*
	 * Ensures that a method with different paramter types is not a subsignature of its super method.
	 * (regression test for bug 111093 More problems with IMethodBinding#isSubsignature(..))
	 */
	public void test040() throws JavaModelException {
		IMethodBinding[] bindings = createMethodBindings(
			new String[] {
				"/P/p1/A.java",
				"package p1;\n" +
				"public class A<S> {\n" +
				"  public <X, Y> void tp1_xoo3(X x, Y y) {\n" +
				"  }\n" +
				"}\n" +
				"class B extends A<String> {\n" +
				"  @Override\n" +
				"  public <W, V> void tp1_xoo3(V x, W y) {\n" +
				"  }\n" +
				"}",
			},
			new String[] {
				"Lp1/A;.tp1_xoo3<X:Ljava/lang/Object;Y:Ljava/lang/Object;>(TX;TY;)V",
				"Lp1/A~B;.tp1_xoo3<W:Ljava/lang/Object;V:Ljava/lang/Object;>(TV;TW;)V"
			});
		assertFalse("B#tp1_xoo3() should not be a subsignature of A#tp1_xoo3()", bindings[1].isSubsignature(bindings[0]));
	}

	/*
	 * Ensures that a method with different paramter types is not a subsignature of its super method.
	 * (regression test for bug 111093 More problems with IMethodBinding#isSubsignature(..))
	 */
	public void test041() throws JavaModelException {
		IMethodBinding[] bindings = createMethodBindings(
			new String[] {
				"/P/p1/A.java",
				"package p1;\n" +
				"public class A<S> {\n" +
				"  public <X, Y> void tp1_foo2(S s, X x, Y y) {\n" +
				"  }\n" +
				"}\n" +
				"class B extends A<String> {\n" +
				"  @Override\n" +
				"  public void tp1_foo2(String s, Object x, Object y) {\n" +
				"  }\n" +
				"}",
			},
			new String[] {
				"Lp1/A;.tp1_foo2<X:Ljava/lang/Object;Y:Ljava/lang/Object;>(TS;TX;TY;)V",
				"Lp1/A~B;.tp1_foo2(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"
			});
		assertTrue("B#tp1_foo2() should be a subsignature of A#tp1_foo2()", bindings[1].isSubsignature(bindings[0]));
	}

	/*
	 * Ensures that a method with different paramter types is not a subsignature of its super method.
	 * (regression test for bug 111093 More problems with IMethodBinding#isSubsignature(..))
	 */
	public void test042() throws JavaModelException {
		IMethodBinding[] bindings = createMethodBindings(
			new String[] {
				"/P/p1/A.java",
				"package p1;\n" +
				"public abstract class A<T> {\n" +
				"  void g2 (T t) {\n" +
				"  }\n" +
				"}\n" +
				"class B extends A<List<Number>> {\n" +
				"  void g2 (List<Number> t) {\n" +
				"  }\n" +
				"}\n" +
				"class List<T> {\n" +
				"}\n" +
				"class Number {\n" +
				"}",
			},
			new String[] {
				"Lp1/A~B;.g2(Lp1/List<Lp1/Number;>;)V"
			});
		ITypeBinding superType = bindings[0].getDeclaringClass().getSuperclass(); // parameterized type
		IMethodBinding ag2 = superType.getDeclaredMethods()[1];
		assertTrue("B#g2() should be a subsignature of A#g2()", bindings[0].isSubsignature(ag2));
	}

	/*
	 * Ensures that a method with same signature in a different hierarchy doesn't overide another one
	 */
	public void test043() throws JavaModelException {
		IMethodBinding[] bindings = createMethodBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"}",
			},
			new String[] {
				"Lp1/X;.foo()V",
				"Lp1/Y;.foo()V"
			});
		assertFalse("Y#foo() should not override X#foo()", bindings[1].overrides(bindings[0]));
	}

	/*
	 * Ensures that a method is a subsignature of the same method in a different hierarchy
	 */
	public void test044() throws JavaModelException {
		IMethodBinding[] bindings = createMethodBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"}",
			},
			new String[] {
				"Lp1/X;.foo()V",
				"Lp1/Y;.foo()V"
			});
		assertTrue("Y#foo() should be a subsignature of X#foo()", bindings[1].isSubsignature(bindings[0]));
	}
	
	/*
	 * Ensures that the byte base type is assignment compatible with the int base type
	 */
	public void test045() throws JavaModelException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {},
			new String[] {
				"B",
				"I"
			});
		assertTrue("byte should be assignment compatible with int", bindings[0].isAssignmentCompatible(bindings[1]));
	}


}
