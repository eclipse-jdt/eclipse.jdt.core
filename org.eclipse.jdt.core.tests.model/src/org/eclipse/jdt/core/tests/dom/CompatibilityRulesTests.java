/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import junit.framework.Test;

public class CompatibilityRulesTests extends AbstractASTTests {
	
	ICompilationUnit[] workingCopies;

	public CompatibilityRulesTests(String name) {
		super(name);
	}
	
	protected IMethodBinding[] createMethodBindings(String[] pathAndSources, String[] bindingKeys) throws JavaModelException {
		WorkingCopyOwner owner = new WorkingCopyOwner() {};
		this.workingCopies = createWorkingCopies(pathAndSources, owner);
		IBinding[] bindings = resolveBindings(bindingKeys, owner);
		int length = bindings.length;
		IMethodBinding[] result = new IMethodBinding[length];
		System.arraycopy(bindings, 0, result, 0, length);
		return result;
	}

	protected ITypeBinding[] createTypeBindings(String[] pathAndSources, String[] bindingKeys) throws JavaModelException {
		WorkingCopyOwner owner = new WorkingCopyOwner() {};
		this.workingCopies = createWorkingCopies(pathAndSources, owner);
		IBinding[] bindings = resolveBindings(bindingKeys, owner);
		int length = bindings.length;
		ITypeBinding[] result = new ITypeBinding[length];
		System.arraycopy(bindings, 0, result, 0, length);
		return result;
	}

	public static Test suite() {
		if (false) {
			Suite suite = new Suite(CompatibilityRulesTests.class.getName());
			suite.addTest(new CompatibilityRulesTests("test005"));
			return suite;
		}
		return new Suite(CompatibilityRulesTests.class);
	}
	
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
	}
	
	protected void tearDown() throws Exception {
		discardWorkingCopies(this.workingCopies);
		this.workingCopies = null;
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
	 * Ensures that the int base type is not assignment compatible with the java.lang.Object type
	 */
	public void test012() throws JavaModelException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {},
			new String[] {
				"I",
				"Ljava/lang/Object;"
			});	
		assertTrue("int should not be assignment compatible with Object", !bindings[0].isAssignmentCompatible(bindings[1]));
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
				"Lp1/X;.foo()V;"
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
				"Lp1/X;.foo()V;",
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
				"Lp1/X;.foo()V;",
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
				"Ljava/lang/Integer;",
				"I"
			});	
		assertTrue("int should be assignment compatible with Integer", bindings[1].isAssignmentCompatible(bindings[0]));
	}
	
}
