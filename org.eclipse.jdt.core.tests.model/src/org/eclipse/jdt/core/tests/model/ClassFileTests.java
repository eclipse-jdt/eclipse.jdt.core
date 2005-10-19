/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;

import junit.framework.Test;

public class ClassFileTests extends ModifyingResourceTests {
	
	IPackageFragmentRoot jarRoot;
	
	public ClassFileTests(String name) {
		super(name);
	}
	
	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
	//	TESTS_PREFIX = "testBug";
	//	TESTS_NAMES = new String[] { "testTypeParameter" };
	//	TESTS_NUMBERS = new int[] { 13 };
	//	TESTS_RANGE = new int[] { 16, -1 };
	}
	public static Test suite() {
		return buildTestSuite(ClassFileTests.class);
	}

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		IJavaProject javaProject = createJavaProject("P");
		String[] pathAndContents = new String[] {
			"nongeneric/A.java", 
			"package nongeneric;\n" +
			"public class A {\n" + 
			"}",			
			"generic/X.java", 
			"package generic;\n" +
			"public class X<T> {\n" + 
			"  <U extends Exception> X<T> foo(X<T> x) throws RuntimeException, U {\n" +
			"    return null;\n" +
			"  }\n" +
			"  <K, V> V foo(K key, V value) throws Exception {\n" +
			"    return value;\n" +
			"  }\n" +
			"}",
			"generic/Y.java", 
			"package generic;\n" +
			"public class Y<K, L> {\n" + 
			"}",
			"generic/Z.java", 
			"package generic;\n" +
			"public class Z<T extends Object & I<? super T>> {\n" + 
			"}",
			"generic/I.java", 
			"package generic;\n" +
			"public interface I<T> {\n" + 
			"}",
			"generic/W.java", 
			"package generic;\n" +
			"public class W<T extends X<T> , U extends T> {\n" + 
			"}",
			"generic/V.java", 
			"package generic;\n" +
			"public class V extends X<Thread> implements I<String> {\n" + 
			"}",
			"varargs/X.java", 
			"package varargs;\n" +
			"public class X {\n" + 
			"  void foo(String s, Object ... others) {\n" +
			"  }\n" +
			"}",
		};
		addLibrary(javaProject, "lib.jar", "libsrc.zip", pathAndContents, JavaCore.VERSION_1_5);
		this.jarRoot = javaProject.getPackageFragmentRoot(getFile("/P/lib.jar"));
			
	}
	
	public void tearDownSuite() throws Exception {
		super.tearDownSuite();
		deleteProject("P");
	}
	
	/*
	 * Ensure that the exception types of a binary method are correct.
	 */
	public void testExceptionTypes1() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("X.class").getType();
		IMethod method = type.getMethod("foo", new String[] {"TK;", "TV;"});
		assertStringsEqual(
			"Unexpected return type",
			"Ljava.lang.Exception;\n",
			method.getExceptionTypes());
	}

	/*
	 * Ensure that the exception types of a binary method is correct.
	 */
	public void testExceptionTypes2() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("X.class").getType();
		IMethod method = type.getMethod("foo", new String[] {"Lgeneric.X<TT;>;"});
		assertStringsEqual(
			"Unexpected return type",
			"Ljava.lang.RuntimeException;\n" + 
			"TU;\n",
			method.getExceptionTypes());
	}

	/*
	 * Ensures that IType#getSuperclassTypeSignature() is correct for a binary type.
	 * (regression test for bug 78520 [model] IType#getSuperInterfaceTypeSignatures() doesn't include type arguments)
	 */
	public void testGetSuperclassTypeSignature() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("V.class").getType();
		assertEquals(
			"Unexpected signature", 
			"Lgeneric.X<Ljava.lang.Thread;>;",
			type.getSuperclassTypeSignature());
	}

	/*
	 * Ensures that IType#getSuperInterfaceTypeSignatures() is correct for a binary type.
	 * (regression test for bug 78520 [model] IType#getSuperInterfaceTypeSignatures() doesn't include type arguments)
	 */
	public void testGetSuperInterfaceTypeSignatures() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("V.class").getType();
		assertStringsEqual(
			"Unexpected signatures", 
			"Lgeneric.I<Ljava.lang.String;>;\n",
			type.getSuperInterfaceTypeSignatures());
	}

	/**
	 * Ensure that the type parameter signatures of a binary type are correct.
	 */
	public void testParameterTypeSignatures1() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("X.class").getType();
		assertStringsEqual(
			"Unexpected type parameters",
			"T:Ljava.lang.Object;\n",
			type.getTypeParameterSignatures());
	}
	
	/**
	 * Ensure that the type parameter signatures of a binary type are correct.
	 */
	public void testParameterTypeSignatures2() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("nongeneric").getClassFile("A.class").getType();
		assertStringsEqual(
			"Unexpected type parameters",
			"",
			type.getTypeParameterSignatures());
	}
	
	/**
	 * Ensure that the type parameter signatures of a binary type are correct.
	 */
	public void testParameterTypeSignatures3() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("Y.class").getType();
		assertStringsEqual(
			"Unexpected type parameters",
			"K:Ljava.lang.Object;\n" + 
			"L:Ljava.lang.Object;\n",
			type.getTypeParameterSignatures());
	}

	/**
	 * Ensure that the type parameter signatures of a binary type are correct.
	 */
	public void testParameterTypeSignatures4() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("Z.class").getType();
		assertStringsEqual(
			"Unexpected type parameters",
			"T:Ljava.lang.Object;:Lgeneric.I<-TT;>;\n",
			type.getTypeParameterSignatures());
	}
	
	/**
	 * Ensure that the type parameter signatures of a binary type are correct.
	 */
	public void testParameterTypeSignatures5() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("W.class").getType();
		assertStringsEqual(
			"Unexpected type parameters",
			"T:Lgeneric.X<TT;>;\n" + 
			"U:TT;\n",
			type.getTypeParameterSignatures());
	}

	/**
	 * Ensure that the type parameter signatures of a binary method are correct.
	 * @deprecated
	 */
	public void testParameterTypeSignatures6() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("X.class").getType();
		IMethod method = type.getMethod("foo", new String[] {"TK;", "TV;"});
		assertStringsEqual(
			"Unexpected type parameters",
			"K:Ljava.lang.Object;\n" + 
			"V:Ljava.lang.Object;\n",
			method.getTypeParameterSignatures());
	}
	
	/*
	 * Ensure that the return type of a binary method is correct.
	 */
	public void testReturnType1() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("X.class").getType();
		IMethod method = type.getMethod("foo", new String[] {"TK;", "TV;"});
		assertEquals(
			"Unexpected return type",
			"TV;",
			method.getReturnType());
	}

	/*
	 * Ensure that the return type of a binary method is correct.
	 */
	public void testReturnType2() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("X.class").getType();
		IMethod method = type.getMethod("foo", new String[] {"Lgeneric.X<TT;>;"});
		assertEquals(
			"Unexpected return type",
			"Lgeneric.X<TT;>;",
			method.getReturnType());
	}
	
	/*
	 * Ensure that opening a binary type parameter when its parent has not been open yet
	 * doesn't throw a JavaModelException
	 * (regression test for bug 101228 JME on code assist)
	 */
	public void testTypeParameter() throws CoreException {
		IClassFile classFile = this.jarRoot.getPackageFragment("generic").getClassFile("X.class");
		ITypeParameter typeParameter = classFile.getType().getTypeParameter("T");
		classFile.close();
		assertStringsEqual(
			"Unexpected bounds", 
			"java.lang.Object\n",
			typeParameter.getBounds());
	}

	/*
	 * Ensure that a method with varargs has the AccVarargs flag set.
	 */
	public void testVarargs() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("varargs").getClassFile("X.class").getType();
		IMethod method = type.getMethod("foo", new String[]{"Ljava.lang.String;", "[Ljava.lang.Object;"});
		assertTrue("Should have the AccVarargs flag set", Flags.isVarargs(method.getFlags()));
	}
}
