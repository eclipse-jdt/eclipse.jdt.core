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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.ASTNode;

import junit.framework.Test;

/*
 * Test the bridge between the DOM AST and the Java model.
 */
public class ASTModelBridgeTests extends AbstractASTTests {
	
	ICompilationUnit workingCopy;

	public ASTModelBridgeTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new Suite(ASTModelBridgeTests.class);
	}
	
	/*
	 * Removes the marker comments "*start*" and "*end*" from the given contents,
	 * builds an AST from the resulting source, and returns the AST node that was delimited
	 * by "*start*" and "*end*".
	 */
	private ASTNode buildAST(String contents) throws JavaModelException {
		return buildAST(contents, this.workingCopy);
	}
	
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		if (JavaCore.getClasspathVariable("JCL_LIB") == null) {
			setupExternalJCL("jclMin");
			JavaCore.setClasspathVariables(
				new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
				new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
				null);
		} 
		createJavaProject("P", new String[] {""}, new String[] {"JCL_LIB"}, "", "1.5");
		this.workingCopy = getCompilationUnit("/P/X.java").getWorkingCopy(
			new WorkingCopyOwner() {}, 
			new IProblemRequestor() {
				public void acceptProblem(IProblem problem) {
					System.err.println(problem.getMessage());
				}
				public void beginReporting() {}
				public void endReporting() {}
				public boolean isActive() {
					return true;
				}
			}, 
			null);
	}
	
	public void tearDownSuite() throws Exception {
		if (this.workingCopy != null)
			this.workingCopy.discardWorkingCopy();
		deleteProject("P");
		super.tearDownSuite();
	}
	
	/*
	 * Ensures that the IJavaElement of an IBinding representing a field is correct.
	 */
	public void testField1() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  Object /*start*/field/*end*/;\n" +
			"}"
		);
		IBinding binding = ((VariableDeclaration) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"field [in X [in [Working copy] X.java [in <default> [in <project root> [in P]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a field is correct.
	 */
	public void testField2() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  Object foo() {\n" +
			"    return new Object() {\n" +
			"      Object /*start*/field/*end*/;\n" +
			"    };\n" +
			"  }\n" +
			"}"
		);
		IBinding binding = ((VariableDeclaration) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"field [in <anonymous #1> [in foo() [in X [in [Working copy] X.java [in <default> [in <project root> [in P]]]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a method is correct.
	 */
	public void testMethod1() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X<K, V> {\n" +
			"  /*start*/void foo(int i, Object o, java.lang.String s, Class[] c, X<K, V> x) {\n" +
			"  }/*end*/\n" +
			"}"
		);
		IBinding binding = ((MethodDeclaration) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"foo(int, Object, java.lang.String, Class[], X<K,V>) [in X [in [Working copy] X.java [in <default> [in <project root> [in P]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a method is correct.
	 */
	public void testMethod2() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X<K, V> {\n" +
			"  /*start*/void foo() {\n" +
			"  }/*end*/\n" +
			"}"
		);
		IBinding binding = ((MethodDeclaration) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"foo() [in X [in [Working copy] X.java [in <default> [in <project root> [in P]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing an anonymous type is correct.
	 */
	public void testAnonymousType() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  Object foo() {\n" +
			"    return new Object() /*start*/{\n" +
			"    }/*end*/;\n" +
			"  }\n" +
			"}"
		);
		IBinding binding = ((AnonymousClassDeclaration) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"<anonymous #1> [in foo() [in X [in [Working copy] X.java [in <default> [in <project root> [in P]]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a local type is correct.
	 */
	public void testLocalType() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  void foo() {\n" +
			"    /*start*/class Y {\n" +
			"    }/*end*/\n" +
			"  }\n" +
			"}"
		);
		IBinding binding = ((TypeDeclarationStatement) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"Y [in foo() [in X [in [Working copy] X.java [in <default> [in <project root> [in P]]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a member type is correct.
	 */
	public void testMemberType() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  /*start*/class Y {\n" +
			"  }/*end*/\n" +
			"}"
		);
		IBinding binding = ((TypeDeclaration) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"Y [in X [in [Working copy] X.java [in <default> [in <project root> [in P]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}
	
	/*
	 * Ensures that the IJavaElement of an IBinding representing a top level type is correct.
	 */
	public void testTopLevelType1() throws JavaModelException {
		ASTNode node = buildAST(
			"/*start*/public class X {\n" +
			"}/*end*/"
		);
		IBinding binding = ((TypeDeclaration) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"X [in [Working copy] X.java [in <default> [in <project root> [in P]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a top level type is correct
	 * (the top level type being in another compilation unit)
	 */
	public void testTopLevelType2() throws CoreException {
		try {
			createFile(
				"/P/Y.java",
				"public class Y {\n" +
				"}"
			);
			ASTNode node = buildAST(
				"public class X extends /*start*/Y/*end*/ {\n" +
				"}"
			);
			IBinding binding = ((SimpleType) node).resolveBinding();
			assertNotNull("No binding", binding);
			IJavaElement element = binding.getJavaElement();
			assertElementEquals(
				"Unexpected Java element",
				"Y [in Y.java [in <default> [in <project root> [in P]]]]",
				element
			);
			assertTrue("Element should exist", element.exists());
		} finally {
			deleteFile("/P/Y.java");
		}
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a top level type is correct
	 * (the top level type being in a jar)
	 */
	public void testTopLevelType3() throws CoreException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  /*start*/String/*end*/ field;\n" +
			"}"
		);
		IBinding binding = ((SimpleType) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"String [in String.class [in java.lang [in "+ getExternalJCLPathString() + " [in P]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a package is correct.
	 */
	public void testPackage1() throws CoreException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  /*start*/java.lang/*end*/.String field;\n" +
			"}"
		);
		IBinding binding = ((QualifiedName) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"java.lang [in "+ getExternalJCLPathString() + " [in P]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a package is correct
	 * (case of default package)
	 */
	public void testPackage2() throws CoreException {
		ASTNode node = buildAST(
			"/*start*/public class X {\n" +
			"}/*end*/"
		);
		ITypeBinding typeBinding = ((TypeDeclaration) node).resolveBinding();
		assertNotNull("No binding", typeBinding);
		IPackageBinding binding = typeBinding.getPackage();
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"<default> [in <project root> [in P]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}
	
	/*
	 * Ensures that the IJavaElement of an IBinding representing a parameter type is correct.
	 * (regression test for bug 78930 ITypeBinding#getJavaElement() throws NPE for type variable)
	 */
	public void testTypeParameter() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X</*start*/T/*end*/> {\n" +
			"}"
		);
		IBinding binding = ((TypeParameter) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"<T> [in X [in [Working copy] X.java [in <default> [in <project root> [in P]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}


}
