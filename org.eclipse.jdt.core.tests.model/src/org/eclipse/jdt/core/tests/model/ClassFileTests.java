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

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.*;

import junit.framework.Test;

public class ClassFileTests extends ModifyingResourceTests {
	
	IPackageFragmentRoot jarRoot;
	ICompilationUnit workingCopy;
	IClassFile classFile;
	
public ClassFileTests(String name) {
	super(name);
}

// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
//	TESTS_PREFIX = "testBug";
//	TESTS_NAMES = new String[] { "testParameterNames01"};
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
		"workingcopy/X.java", 
		"package workingcopy;\n" +
		"public class X {\n" + 
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}",
		"workingcopy/Y.java", 
		"package workingcopy;\n" +
		"public class Y<W> {\n" + 
		"  <T> T foo(T t, String... args) {\n" +
		"    return t;\n" +
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

protected void tearDown() throws Exception {
	if (this.workingCopy != null)
		this.workingCopy.discardWorkingCopy();
	if (this.classFile != null) {
		removeLibrary(getJavaProject("P"), "lib2.jar", "src2.zip");
		this.classFile = null;
	}
	super.tearDown();
}

private IClassFile createClassFile(String contents) throws CoreException, IOException {
	IJavaProject project = getJavaProject("P");
	addLibrary(project, "lib2.jar", "src2.zip", new String[] {"p/X.java", contents}, "1.5");
	this.classFile =  project.getPackageFragmentRoot(getFile("/P/lib2.jar")).getPackageFragment("p").getClassFile("X.class");
	return this.classFile;
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
 * Ensure that the categories for a class are correct.
 */
public void testGetCategories01() throws CoreException, IOException {
	createClassFile(
		"package p;\n" +
		"/**\n" +
		" * @category test\n" +
		" */\n" +
		"public class X {\n" +
		"}"
	);
	String[] categories = this.classFile.getType().getCategories();
	assertStringsEqual(
		"Unexpected categories",
		"test\n",
		categories);
}

/*
 * Ensure that the categories for a field are correct.
 */
public void testGetCategories02() throws CoreException, IOException {
	createClassFile(
		"package p;\n" +
		"public class X {\n" +
		"  /**\n" +
		"   * @category test\n" +
		"   */\n" +
		"  int field;\n" +
		"}"
	);
	String[] categories = this.classFile.getType().getField("field").getCategories();
	assertStringsEqual(
		"Unexpected categories",
		"test\n",
		categories);
}

/*
 * Ensure that the categories for a method are correct.
 */
public void testGetCategories03() throws CoreException, IOException {
	createClassFile(
		"package p;\n" +
		"public class X {\n" +
		"  /**\n" +
		"   * @category test\n" +
		"   */\n" +
		"  void foo() {}\n" +
		"}"
	);
	String[] categories = this.classFile.getType().getMethod("foo", new String[0]).getCategories();
	assertStringsEqual(
		"Unexpected categories",
		"test\n",
		categories);
}

/*
 * Ensures that the children of a type for a given category are correct.
 */
public void testGetChildrenForCategory01() throws CoreException, IOException {
	createClassFile(
		"package p;\n" +
		"public class X {\n" +
		"  /**\n" +
		"   * @category test\n" +
		"   */\n" +
		"  int field;\n" +
		"  /**\n" +
		"   * @category test\n" +
		"   */\n" +
		"  void foo1() {}\n" +
		"  /**\n" +
		"   * @category test\n" +
		"   */\n" +
		"  void foo2() {}\n" +
		"  /**\n" +
		"   * @category other\n" +
		"   */\n" +
		"  void foo3() {}\n" +
		"}"
	);
	IJavaElement[] children = this.classFile.getType().getChildrenForCategory("test");
	assertElementsEqual(
		"Unexpected children",
		"field [in X [in X.class [in p [in lib2.jar [in P]]]]]\n" + 
		"foo1() [in X [in X.class [in p [in lib2.jar [in P]]]]]\n" + 
		"foo2() [in X [in X.class [in p [in lib2.jar [in P]]]]]",
		children);
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

/*
 * Ensures that the parameter names of a binary method with source attached are correct.
 */
public void testParameterNames01() throws CoreException {
	IMethod method = this.jarRoot.getPackageFragment("generic").getClassFile("X.class").getType().getMethod("foo", new String[] {"TK;", "TV;"});
	String[] parameterNames = method.getParameterNames();
	assertStringsEqual(
		"Unexpected parameter names", 
		"key\n" + 
		"value\n",
		parameterNames);
}

/*
 * Ensures that the parameter names of a binary method without source attached are correct.
 */
public void testParameterNames02() throws CoreException {
	IPath sourceAttachmentPath = this.jarRoot.getSourceAttachmentPath();
	try {
		attachSource(this.jarRoot, null, null);
		IMethod method = this.jarRoot.getPackageFragment("generic").getClassFile("X.class").getType().getMethod("foo", new String[] {"TK;", "TV;"});
		String[] parameterNames = method.getParameterNames();
		assertStringsEqual(
			"Unexpected parameter names", 
			"arg0\n" + 
			"arg1\n",
			parameterNames);
	} finally {
		attachSource(this.jarRoot, sourceAttachmentPath.toString(), null);
	}
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
 * Ensures that the raw parameter names of a binary method with source attached are correct.
 */
public void testRawParameterNames01() throws CoreException {
	IMethod method = this.jarRoot.getPackageFragment("generic").getClassFile("X.class").getType().getMethod("foo", new String[] {"TK;", "TV;"});
	String[] parameterNames = method.getRawParameterNames();
	assertStringsEqual(
		"Unexpected parameter names", 
		"arg0\n" + 
		"arg1\n",
		parameterNames);
}

/*
 * Ensures that the raw parameter names of a binary method without source attached are correct.
 */
public void testRawParameterNames02() throws CoreException {
	IPath sourceAttachmentPath = this.jarRoot.getSourceAttachmentPath();
	try {
		attachSource(this.jarRoot, null, null);
		IMethod method = this.jarRoot.getPackageFragment("generic").getClassFile("X.class").getType().getMethod("foo", new String[] {"TK;", "TV;"});
		String[] parameterNames = method.getParameterNames();
		assertStringsEqual(
			"Unexpected parameter names", 
			"arg0\n" + 
			"arg1\n",
			parameterNames);
	} finally {
		attachSource(this.jarRoot, sourceAttachmentPath.toString(), null);
	}
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
	IClassFile clazz = this.jarRoot.getPackageFragment("generic").getClassFile("X.class");
	ITypeParameter typeParameter = clazz.getType().getTypeParameter("T");
	clazz.close();
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

/*
 * Ensures that a class file can be turned into a working copy and that its children are correct.
 */
public void testWorkingCopy01() throws CoreException {
	IClassFile clazz = this.jarRoot.getPackageFragment("workingcopy").getClassFile("X.class");
	this.workingCopy = clazz.becomeWorkingCopy(null/*no problem requestor*/, null/*primary owner*/, null/*no progress*/);
	assertElementDescendants(
		"Unexpected children", 
		"[Working copy] X.class\n" + 
		"  package workingcopy\n" + 
		"  class X\n" + 
		"    void foo()",
		this.workingCopy);
}

/*
 * Ensures that a class file without source attached can be turned into a working copy and that its children are correct.
 */
public void testWorkingCopy02() throws CoreException {
	IPath sourceAttachmentPath = this.jarRoot.getSourceAttachmentPath();
	try {
		attachSource(this.jarRoot, null, null);
		IClassFile clazz = this.jarRoot.getPackageFragment("workingcopy").getClassFile("X.class");
		assertNull("Should not have source attached", clazz.getSource());
		this.workingCopy = clazz.becomeWorkingCopy(null/*no problem requestor*/, null/*primary owner*/, null/*no progress*/);
		assertElementDescendants(
			"Unexpected children", 
			"[Working copy] X.class\n" + 
			"  package workingcopy\n" + 
			"  class X\n" + 
			"    X()\n" + 
			"    void foo()",
			this.workingCopy);
	} finally {
		attachSource(this.jarRoot, sourceAttachmentPath.toString(), null);
	}
}

/*
 * Ensures that a class file can be turned into a working copy, modified and that its children are correct.
 */
public void testWorkingCopy03() throws CoreException {
	IClassFile clazz = this.jarRoot.getPackageFragment("workingcopy").getClassFile("X.class");
	this.workingCopy = clazz.becomeWorkingCopy(null/*no problem requestor*/, null/*primary owner*/, null/*no progress*/);
	this.workingCopy.getBuffer().setContents(
		"package workingcopy;\n" +
		"public class X {\n" + 
		"  void bar() {\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false/*don't force problems*/, null/*primary owner*/, null/*no progress*/);
	assertElementDescendants(
		"Unexpected children", 
		"[Working copy] X.class\n" + 
		"  package workingcopy\n" + 
		"  class X\n" + 
		"    void bar()",
		this.workingCopy);
}

/*
 * Ensures that a class file working copy cannot be commited
 */
public void testWorkingCopy04() throws CoreException {
	IClassFile clazz = this.jarRoot.getPackageFragment("workingcopy").getClassFile("X.class");
	this.workingCopy = clazz.becomeWorkingCopy(null/*no problem requestor*/, null/*primary owner*/, null/*no progress*/);
	this.workingCopy.getBuffer().setContents(
		"package workingcopy;\n" +
		"public class X {\n" + 
		"  void bar() {\n" +
		"  }\n" +
		"}"
	);
	JavaModelException exception = null;
	try {
		this.workingCopy.commitWorkingCopy(false/*don't force*/, null);
	} catch (JavaModelException e) {
		exception = e;
	}
	assertEquals(
		"Unxepected JavaModelException", 
		"Java Model Exception: Java Model Status [Operation not supported for specified element type(s):[Working copy] X.class [in workingcopy [in lib.jar [in P]]]]", 
		exception.toString());
}

/*
 * Ensures that a type can be created in class file working copy.
 */
public void testWorkingCopy05() throws CoreException {
	IClassFile clazz = this.jarRoot.getPackageFragment("workingcopy").getClassFile("X.class");
	this.workingCopy = clazz.becomeWorkingCopy(null/*no problem requestor*/, null/*primary owner*/, null/*no progress*/);
	this.workingCopy.createType(
		"class Y {\n" + 
		"}",
		null,
		false/*don't force*/,
		null);
	assertElementDescendants(
		"Unexpected children", 
		"[Working copy] X.class\n" + 
		"  package workingcopy\n" + 
		"  class X\n" + 
		"    void foo()\n" + 
		"  class Y",
		this.workingCopy);
}

/*
 * Ensures that the primary compilation unit of class file working copy is correct.
 */
public void testWorkingCopy06() throws CoreException {
	IClassFile clazz = this.jarRoot.getPackageFragment("workingcopy").getClassFile("X.class");
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopy = clazz.becomeWorkingCopy(null/*no problem requestor*/, owner, null/*no progress*/);
	ICompilationUnit primary = this.workingCopy.getPrimary();
	assertEquals("Unexpected owner of primary working copy", null, primary.getOwner());
}

/*
 * Ensures that a class file working copy can be restored from the original source.
 */
public void testWorkingCopy07() throws CoreException {
	IClassFile clazz = this.jarRoot.getPackageFragment("workingcopy").getClassFile("X.class");
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopy = clazz.becomeWorkingCopy(null/*no problem requestor*/, owner, null/*no progress*/);
	this.workingCopy.getBuffer().setContents(
		"package workingcopy;\n" +
		"public class X {\n" + 
		"  void bar() {\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false/*don't force problems*/, null/*primary owner*/, null/*no progress*/);
	this.workingCopy.restore();
	assertElementDescendants(
		"Unexpected children", 
		"[Working copy] X.class\n" + 
		"  package workingcopy\n" + 
		"  class X\n" + 
		"    void foo()",
		this.workingCopy);
}

/*
 * Ensures that a class file working copy can be reconciled against.
 */
public void testWorkingCopy08() throws CoreException {
	IClassFile clazz = this.jarRoot.getPackageFragment("workingcopy").getClassFile("X.class");
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopy = clazz.becomeWorkingCopy(null/*no problem requestor*/, owner, null/*no progress*/);
	this.workingCopy.getBuffer().setContents(
		"package workingcopy;\n" +
		"public class X {\n" + 
		"  public void bar() {\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.makeConsistent(null);
	
	ICompilationUnit cu = getCompilationUnit("/P/Y.java");
	ICompilationUnit copy = null;
	try {
		ProblemRequestor problemRequestor = new ProblemRequestor();
		copy = cu.getWorkingCopy(owner, problemRequestor, null/*no prpgress*/);
		copy.getBuffer().setContents(
			"public class Y {\n" +
			"  void foo(workingcopy.X x) {\n" +
			"    x.bar();\n" +
			"  }\n" +
			"}"
		);
		problemRequestor.problems = new StringBuffer();
		copy.reconcile(ICompilationUnit.NO_AST, false/*don't force problems*/, owner, null/*no progress*/);
		assertProblems(
			"Unexpected problems", 
			"----------\n" + 
			"----------\n",
			problemRequestor);
	} finally {
		if (copy != null)
			copy.discardWorkingCopy();
	}
}

/*
 * Ensures that types in a class file are hidden if the class file working copy is empty.
 */
public void testWorkingCopy09() throws CoreException {
	IClassFile clazz = this.jarRoot.getPackageFragment("workingcopy").getClassFile("X.class");
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopy = clazz.becomeWorkingCopy(null/*no problem requestor*/, owner, null/*no progress*/);
	this.workingCopy.getBuffer().setContents(	"");
	this.workingCopy.makeConsistent(null);
	
	ICompilationUnit cu = getCompilationUnit("/P/Y.java");
	ICompilationUnit copy = null;
	try {
		ProblemRequestor problemRequestor = new ProblemRequestor();
		copy = cu.getWorkingCopy(owner, problemRequestor, null/*no prpgress*/);
		copy.getBuffer().setContents(
			"public class Y {\n" +
			"  workingcopy.X x;\n" +
			"}"
		);
		problemRequestor.problems = new StringBuffer();
		copy.reconcile(ICompilationUnit.NO_AST, false/*don't force problems*/, owner, null/*no progress*/);
		assertProblems(
			"Unexpected problems", 
			"----------\n" + 
			"1. ERROR in /P/Y.java\n" + 
			"workingcopy.X cannot be resolved to a type\n" + 
			"----------\n",
			problemRequestor);
	} finally {
		if (copy != null)
			copy.discardWorkingCopy();
	}
}

/*
 * Ensures that a 1.5 class file without source attached can be turned into a working copy and that its source is correct.
 */
public void testWorkingCopy10() throws CoreException {
	IPath sourceAttachmentPath = this.jarRoot.getSourceAttachmentPath();
	try {
		attachSource(this.jarRoot, null, null);
		IClassFile clazz = this.jarRoot.getPackageFragment("workingcopy").getClassFile("Y.class");
		assertNull("Should not have source attached", clazz.getSource());
		this.workingCopy = clazz.becomeWorkingCopy(null/*no problem requestor*/, null/*primary owner*/, null/*no progress*/);
		assertSourceEquals(
			"Unexpected source", 
			"package workingcopy;\n" + 
			"public class Y<W> {\n" + 
			"  \n" + 
			"  public Y() {\n" + 
			"  }\n" + 
			"  \n" + 
			"  <T> T foo(T t, java.lang.String... args) {\n" + 
			"    return null;\n" + 
			"  }\n" + 
			"}",
			this.workingCopy.getSource());
	} finally {
		attachSource(this.jarRoot, sourceAttachmentPath.toString(), null);
	}
}

}
