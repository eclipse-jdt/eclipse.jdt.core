/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.File;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LocalVariable;

import junit.framework.Test;

public class MementoTests extends ModifyingResourceTests {
public MementoTests(String name) {
	super(name);
}
// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
//	TESTS_PREFIX =  "testArray";
//	TESTS_NAMES = new String[] { "testPackageFragmentRootMemento8" };
//	TESTS_NUMBERS = new int[] { 8 };
//	TESTS_RANGE = new int[] { 6, -1 };
}

public static Test suite() {
	return buildModelTestSuite(MementoTests.class);
}
protected void assertMemento(String expected, IJavaElement element) {
	String actual = element.getHandleIdentifier();
	if (!expected.equals(actual)){
		String escapedExternalJCL = getEscapedExternalJCLPath();
		int start = actual.indexOf(escapedExternalJCL);
		if (start != -1) {
			String firstPart = actual.substring(0, start);
		 	System.out.print(Util.displayString(firstPart, 2));
		 	System.out.print(" + getEscapedExternalJCLPath() + ");
		 	String secondPart = actual.substring(start+escapedExternalJCL.length());
		 	System.out.print(Util.displayString(secondPart, 0));
		} else {
			System.out.print(Util.displayString(actual, 2));
		}
	 	System.out.println(",");
	}
	assertEquals(
		"Unexpected memento for " + element,
		expected,
		actual);
	IJavaElement restored = JavaCore.create(actual);
	assertEquals(
		"Unexpected restored element",
		element,
		restored);
}
protected String getEscapedExternalJCLPath() {
	return getEscapedPath(getExternalJCLPath().toString());
}
protected String getEscapedPath(String path) {
	StringBuffer buffer = new StringBuffer();
	for (int i = 0; i < path.length(); i++) {
		char character = path.charAt(i);
		if (character == '/') buffer.append('\\');
		buffer.append(character);
	}
	return buffer.toString();
}
public void setUpSuite() throws Exception {
	super.setUpSuite();

	Util.createClassFolder(new String[] {
		"X.java",
		"public class X {}"
		}, 
		getExternalResourcePath("myLib"), 
		"1.4");
	this.createJavaProject(
			"P",
			new String[] {"src"},
			new String[] {
				getExternalJCLPathString(),
				"/P/lib",
				"/P/lib/myLib.jar",
				"/OtherProj/lib",
				"/OtherProj/lib/myLib.jar",
				getExternalResourcePath("myLib")
			},
			"bin");
}
public void tearDownSuite() throws Exception {
	this.deleteProject("P");
	deleteExternalResource("myLib");
	super.tearDownSuite();
}
/*
 * Tests that an annotation can be persisted and restored using its memento.
 */
public void testAnnotation1() {
	IAnnotation annotation = getCompilationUnit("/P/src/p/X.java").getType("X").getAnnotation("MyAnnot");
	assertMemento(
		"=P/src<p{X.java[X}MyAnnot",
		annotation);
}
/*
 * Tests that an annotation can be persisted and restored using its memento.
 */
public void testAnnotation2() {
	IAnnotation annotation = getCompilationUnit("/P/src/p/X.java").getType("X").getMethod("foo", new String[0]).getAnnotation("MyAnnot");
	assertMemento(
		"=P/src<p{X.java[X~foo}MyAnnot",
		annotation);
}
/*
 * Tests that an annotation can be persisted and restored using its memento.
 */
public void testAnnotation3() {
	IAnnotation annotation = getCompilationUnit("/P/src/p/X.java").getType("X").getField("field").getAnnotation("MyAnnot");
	assertMemento(
		"=P/src<p{X.java[X^field}MyAnnot",
		annotation);
}
/**
 * Tests that an anonymous type can be persisted and restored using its memento.
 */
public void testAnonymousTypeMemento1() {
	IType type = getCompilationUnit("/P/src/p/X.java").getType("X");

	IType anonymous = type.getInitializer(1).getType("", 1);
	assertMemento(
		"=P/src<p{X.java[X|1[",
		anonymous);

	anonymous = type.getInitializer(1).getType("", 2);
	assertMemento(
		"=P/src<p{X.java[X|1[!2",
		anonymous);
}
/**
 * Tests that an anonymous type can be persisted and restored using its memento.
 */
public void testAnonymousTypeMemento2() {
	IType type = getCompilationUnit("/P/src/p/X.java").getType("X");

	IType anonymous = type.getField("f").getType("", 1);
	assertMemento(
		"=P/src<p{X.java[X^f[",
		anonymous);

	anonymous = type.getField("f").getType("", 3);
	assertMemento(
		"=P/src<p{X.java[X^f[!3",
		anonymous);
}
/**
 * Tests that an anonymous type can be persisted and restored using its memento.
 */
public void testAnonymousTypeMemento3() {
	IType type = getCompilationUnit("/P/src/p/X.java").getType("X");

	IType anonymous = type.getMethod("foo", new String[]{}).getType("", 1);
	assertMemento(
		"=P/src<p{X.java[X~foo[",
		anonymous);

	anonymous = type.getMethod("foo", new String[]{}).getType("", 4);
	assertMemento(
		"=P/src<p{X.java[X~foo[!4",
		anonymous);
}
/**
 * Tests that a binary field can be persisted and restored using its memento.
 */
public void testBinaryFieldMemento() throws JavaModelException {
	IField field = getClassFile("/P/lib/p/X.class").getType().getField("field");
	assertMemento(
		"=P/lib<p(X.class[X^field",
		field);
}
/**
 * Tests that an inner type, inner field and inner method can be persisted and restored
 * using mementos.
 */
public void testBinaryInnerTypeMemento() throws JavaModelException {
	IType type = getClassFile("/P/lib/p/X$Inner.class").getType();

	assertMemento(
		"=P/lib<p(X$Inner.class[Inner",
		type);

	IField innerField = type.getField("field");
	assertMemento(
		"=P/lib<p(X$Inner.class[Inner^field",
		innerField);

	IMethod innerMethod = type.getMethod("foo", new String[] {"I", "Ljava.lang.String;"});
	assertMemento(
		"=P/lib<p(X$Inner.class[Inner~foo~I~Ljava.lang.String;",
		innerMethod);
}
/**
 * Tests that a binary method can be persisted and restored using its memento.
 */
public void testBinaryMethodMemento1() throws JavaModelException {
	IType type = getClassFile("/P/lib/p/X.class").getType();
	IMethod method = type.getMethod("foo", new String[] {"I", "Ljava.lang.String;"});
	assertMemento(
		"=P/lib<p(X.class[X~foo~I~Ljava.lang.String;",
		method);
}
/**
 * Tests that a binary method can be persisted and restored using its memento.
 */
public void testBinaryMethodMemento2() throws JavaModelException {
	IType type = getClassFile("/P/lib/p/X.class").getType();
	IMethod method = type.getMethod("bar", new String[] {});
	assertMemento(
		"=P/lib<p(X.class[X~bar",
		method);
}
/**
 * Tests that a binary method can be persisted and restored using its memento.
 */
public void testBinaryMethodMemento3() throws JavaModelException {
	IType type = getClassFile("/P/lib/p/X.class").getType();
	IMethod method = type.getMethod("fred", new String[] {"[Z"});
	assertMemento(
		"=P/lib<p(X.class[X~fred~\\[Z",
		method);
}
/**
 * Tests that a binary method with a parameter with wildcard can be persisted and restored using its memento.
 * (regression test for bug 75466 [1.5] IAE in JavaElement.exists() for Collection<E>#containsAll(Collection<?>))
 */
public void testBinaryMethodMemento4() throws JavaModelException {
	IType type = getClassFile("/P/lib/p/X.class").getType();
	IMethod method = type.getMethod("foo", new String[] {"Ljava.util.Collection<*>;"});
	assertMemento(
		"=P/lib<p(X.class[X~foo~Ljava.util.Collection\\<*>;",
		method);
}

/**
 * Tests that a binary type can be persisted and restored using its memento.
 */
public void testBinaryTypeMemento() throws JavaModelException {
	IType type = getClassFile("/P/lib/p/X.class").getType();
	assertMemento(
		"=P/lib<p(X.class[X",
		type);
}
/**
 * Tests that a class file can be persisted and restored using its memento.
 */
public void testClassFileMemento() {
	IClassFile cf =  getClassFile("/P/lib/p/X.class");
	assertMemento(
		"=P/lib<p(X.class",
		cf);

	cf = getClassFile("/P/lib/Y.class");
	assertMemento(
		"=P/lib<(Y.class",
		cf);

}
/**
 * Tests that a compilation unit can be persisted and restored using its memento.
 */
public void testCompilationUnitMemento1() {
	ICompilationUnit cu = getCompilationUnit("/P/src/p/X.java");
	assertMemento(
		"=P/src<p{X.java",
		cu);

	cu = getCompilationUnit("/P/src/Y.java");
	assertMemento(
		"=P/src<{Y.java",
		cu);
}
/*
 * Ensure that restoring a compilation unit memento with a null owner doesn't create an
 * invalid handle
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=205917 )
 */
public void testCompilationUnitMemento2() throws Exception {
	ICompilationUnit cu = getCompilationUnit("/P/src/p/X.java");
	String handleIdentifier = cu.getHandleIdentifier();
	cu = (ICompilationUnit) JavaCore.create(handleIdentifier, null);
	assertEquals(cu, cu); // should not throw an NPE
}
/**
 * Tests that a binary field in an external jar can be persisted and restored using its memento.
 */
public void testExternalJarBinaryFieldMemento() throws JavaModelException {
	IType type = getClassFile("P", getExternalJCLPathString(), "p", "X.class").getType();
	IField field = type.getField("field");
	assertMemento(
		"=P/"+ getEscapedExternalJCLPath() + "<p(X.class[X^field",
		field);
}
/**
 * Tests that a inner binary type and field in an external jar can be persisted and restored using its memento.
 */
public void testExternalJarBinaryInnerTypeMemento() throws JavaModelException {
	IType type = getClassFile("P", getExternalJCLPathString(), "p", "X$Inner.class").getType();
	assertMemento(
		"=P/" + getEscapedExternalJCLPath() + "<p(X$Inner.class[Inner",
		type);
}
/**
 * Tests that a binary method in an external jar can be persisted and restored using its memento.
 */
public void testExternalJarBinaryMethodMemento() throws JavaModelException {
	IType type = getClassFile("P", getExternalJCLPathString(), "p", "X.class").getType();
	IMethod method = type.getMethod("foo", new String[] {"[Ljava.lang.String;"});
	assertMemento(
		"=P/" + getEscapedExternalJCLPath() + "<p(X.class[X~foo~\\[Ljava.lang.String;",
		method);
}
/**
 * Tests that a binary type in an external jar can be persisted and restored using its memento.
 */
public void testExternalJarBinaryTypeMemento() throws JavaModelException {
	IType type = getClassFile("P", getExternalJCLPathString(), "p", "X.class").getType();
	assertMemento(
		"=P/" + getEscapedExternalJCLPath() + "<p(X.class[X",
		type);
}
/**
 * Tests that a class file in an external jar at the root of the file system can be persisted and restored using its memento.
 */
public void testExternalJarClassFileMemento() throws JavaModelException {
	char separator = File.separatorChar;
	String device = separator == '/' ? "" : "C:";
	IClassFile classFile = getClassFile("P", device + separator + "lib.jar", "p", "X.class");
	assertMemento(
		"=P/" + device + "\\/lib.jar<p(X.class",
		classFile);
}
/*
 * Ensures that a class file in an external library folder can be persisted and restored using its memento.
 */
public void testExternalLibraryFolderClassFileMemento() throws JavaModelException {
	IClassFile classFile = getClassFile("P", getExternalResourcePath("myLib"), "", "X.class");
	assertMemento(
		"=P/" + getEscapedPath(new Path(getExternalResourcePath("myLib")).toString()) + "<(X.class",
		classFile);
}
/**
 * Tests that an import declaration can be persisted and restored using its memento.
 */
public void testImportContainerMemento() {
	IImportContainer importContainer = getCompilationUnit("/P/src/p/X.java").getImportContainer();
	assertMemento(
		"=P/src<p{X.java#",
		importContainer);
}
/**
 * Tests that an import declaration can be persisted and restored using its memento.
 */
public void testImportDeclarationMemento() {
	IImportDeclaration importDecl = getCompilationUnit("/P/src/p/X.java").getImport("java.io.Serializable");
	assertMemento(
		"=P/src<p{X.java#java.io.Serializable",
		importDecl);

	importDecl = getCompilationUnit("/P/src/p/X.java").getImport("java.util.*");
	assertMemento(
		"=P/src<p{X.java#java.util.*",
		importDecl);
}
/*
 * Ensures that a Java element is returned for an invalid memento.
 * (regression test for bug 81762 [model] AIOOB in breakpoints view)
 */
public void testInvalidMemento() {
	IJavaElement element = JavaCore.create("=P/src<p{");
	assertElementEquals("Unexpected element", "p [in src [in P]]", element);
}
/**
 * Tests that an initializer can be persisted and restored using its memento.
 */
public void testInitializerMemento() {
	IType type = getCompilationUnit("/P/src/p/X.java").getType("X");

	IInitializer initializer = type.getInitializer(1);
	assertMemento(
		"=P/src<p{X.java[X|1",
		initializer);

	initializer = type.getInitializer(2);
	assertMemento(
		"=P/src<p{X.java[X|2",
		initializer);
}
/**
 * Tests that a binary field in an internal jar can be persisted and restored using its memento.
 */
public void testInternalJarBinaryFieldMemento() throws JavaModelException {
	IType type = getPackageFragmentRoot("/P/lib/myLib.jar").getPackageFragment("p").getClassFile("X.class").getType();
	IField field = type.getField("field");
	assertMemento(
		"=P/lib\\/myLib.jar<p(X.class[X^field",
		field);
}
/**
 * Tests that a inner binary type and field in an internal jar can be persisted and restored using its memento.
 */
public void testInternalJarBinaryInnerTypeMemento() throws JavaModelException {
	IType type = getPackageFragmentRoot("/P/lib/myLib.jar").getPackageFragment("p").getClassFile("X$Inner.class").getType();
	assertMemento(
		"=P/lib\\/myLib.jar<p(X$Inner.class[Inner",
		type);
}
/**
 * Tests that a binary method in an internal jar can be persisted and restored using its memento.
 */
public void testInternalJarBinaryMethodMemento() throws JavaModelException {
	IType type = getPackageFragmentRoot("/P/lib/myLib.jar").getPackageFragment("p").getClassFile("X.class").getType();
	IMethod method = type.getMethod("foo", new String[] {"[Ljava.lang.String;"});
	assertMemento(
		"=P/lib\\/myLib.jar<p(X.class[X~foo~\\[Ljava.lang.String;",
		method);
}
/**
 * Tests that a binary type in an internal jar can be persisted and restored using its memento.
 */
public void testInternalJarBinaryTypeMemento() throws JavaModelException {
	IType type = getPackageFragmentRoot("/P/lib/myLib.jar").getPackageFragment("p").getClassFile("X.class").getType();
	assertMemento(
		"=P/lib\\/myLib.jar<p(X.class[X",
		type);
}
/**
 * Tests that a local type can be persisted and restored using its memento.
 */
public void testLocalTypeMemento1() {
	IType type = getCompilationUnit("/P/src/p/X.java").getType("X");

	IType anonymous = type.getInitializer(1).getType("Y", 1);
	assertMemento(
		"=P/src<p{X.java[X|1[Y",
		anonymous);

	anonymous = type.getInitializer(1).getType("Y", 2);
	assertMemento(
		"=P/src<p{X.java[X|1[Y!2",
		anonymous);
}
/**
 * Tests that a local type can be persisted and restored using its memento.
 */
public void testLocalTypeMemento2() {
	IType type = getCompilationUnit("/P/src/p/X.java").getType("X");

	IType anonymous = type.getMethod("foo", new String[]{}).getType("Y", 1);
	assertMemento(
		"=P/src<p{X.java[X~foo[Y",
		anonymous);

	anonymous = type.getMethod("foo", new String[]{}).getType("Y", 3);
	assertMemento(
		"=P/src<p{X.java[X~foo[Y!3",
		anonymous);
}
/**
 * Tests that a local variable can be persisted and restored using its memento.
 */
public void testLocalVariableMemento1() {
	IType type = getCompilationUnit("/P/src/p/X.java").getType("X");
	IMethod method = type.getMethod("foo", new String[]{});

	ILocalVariable localVar = new LocalVariable((JavaElement)method, "var", 1, 2, 3, 4, "Z", null);
	assertMemento(
		"=P/src<p{X.java[X~foo@var!1!2!3!4!Z",
		localVar);
}
/**
 * Tests that a local variable can be persisted and restored using its memento.
 */
public void testLocalVariableMemento2() throws JavaModelException {
	IType type = getClassFile("/P/src/p/X.class").getType();
	IMethod method = type.getMethod("foo", new String[]{"I"});

	ILocalVariable localVar = new LocalVariable((JavaElement)method, "var", 1, 2, 3, 4, "Z", null);
	assertMemento(
		"=P/src<p(X.class[X~foo~I@var!1!2!3!4!Z",
		localVar);
}
/**
 * Tests that a local variable can be persisted and restored using its memento.
 */
public void testLocalVariableMemento3() {
	IType type = getCompilationUnit("/P/src/p/X.java").getType("X");
	IInitializer initializer = type.getInitializer(1);

	ILocalVariable localVar = new LocalVariable((JavaElement)initializer, "var", 1, 2, 3, 4, "Z", null);
	assertMemento(
		"=P/src<p{X.java[X|1@var!1!2!3!4!Z",
		localVar);
}
/**
 * Tests that a local variable can be persisted and restored using its memento.
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=244549 )
 */
public void testLocalVariableMemento4() throws Exception {
	try {
		createJavaProject("P1", new String[] {"src"}, new String[] {getExternalJCLPathString("1.5")}, "bin", "1.5");
		createFile(
			"/P1/src/X.java",
			"public class X<T> {\n" +
			"  void foo() {\n" +
			"    X<String> var = null;\n" +
			"  }\n" +
			"}"
		);
		ILocalVariable localVar = getLocalVariable(getCompilationUnit("/P1/src/X.java"), "var", "var");
		String memento = localVar.getHandleIdentifier();
		IJavaElement restored = JavaCore.create(memento);
		String restoredMemento = restored.getHandleIdentifier();
		assertEquals("Unexpected restored memento", memento, restoredMemento);
	} finally {
		deleteProject("P1");
	}
}
/**
 * Tests that a package declaration can be persisted and restored using its memento.
 */
public void testPackageDeclarationMemento() {
	IPackageDeclaration declaration = getCompilationUnit("/P/src/p/X.java").getPackageDeclaration("p");
	assertMemento(
		"=P/src<p{X.java%p",
		declaration);

	declaration = getCompilationUnit("/P/src/p1/p2/X.java").getPackageDeclaration("p1.p2");
	assertMemento(
		"=P/src<p1.p2{X.java%p1.p2",
		declaration);
}
/**
 * Tests that a package fragment can be persisted and restored using its memento.
 */
public void testPackageFragmentMemento() {
	IPackageFragment pkg = getPackage("/P/src/p");
	assertMemento(
		"=P/src<p",
		pkg);

	pkg = getPackage("/P/src/p1/p2");
	assertMemento(
		"=P/src<p1.p2",
		pkg);

	pkg = getPackage("/P/src");
	assertMemento(
		"=P/src<",
		pkg);
}
/**
 * Tests that a package fragment in the default root can be persisted and restored using its memento.
 */
public void testPackageFragmentMemento2() throws CoreException {
	try {
		createJavaProject("P1", new String[] {""}, "");
		IPackageFragment pkg = getPackage("/P1/p");
		assertMemento(
			"=P1/<p",
			pkg);
	} finally {
		deleteProject("P1");
	}
}
/**
 * Tests that a source folder package fragment root can be persisted and restored using its memento.
 */
public void testPackageFragmentRootMemento1() {
	IJavaProject project = getJavaProject("P");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(project.getProject().getFolder("src"));
	assertMemento(
		"=P/src",
		root);
}
/**
 * Tests that a source folder package fragment root corresponding to the project
 * can be persisted and restored using its memento.
 */
public void testPackageFragmentRootMemento2() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P1", new String[] {""}, "");
		IPackageFragmentRoot root = project.getPackageFragmentRoot(project.getProject());
		assertMemento(
			"=P1/",
			root);
	} finally {
		this.deleteProject("P1");
	}
}
/**
 * Tests that a library folder package fragment root in the same project
 * can be persisted and restored using its memento.
 */
public void testPackageFragmentRootMemento3() {
	IJavaProject project = getJavaProject("P");
	IFolder libFolder = project.getProject().getFolder("lib");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(libFolder);
	assertMemento(
		"=P/lib",
		root);
}
/**
 * Tests that a library folder package fragment root in another project
 * can be persisted and restored using its memento.
 */
public void testPackageFragmentRootMemento4() {
	IJavaProject project = getJavaProject("P");
	IFolder otherLibFolder = getFolder("/OtherProj/lib");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(otherLibFolder);
	assertMemento(
		"=P/\\/OtherProj\\/lib",
		root);
}
/**
 * Tests that a jar package fragment root in the same project
 * can be persisted and restored using its memento.
 */
public void testPackageFragmentRootMemento5() {
	IJavaProject project = getJavaProject("P");
	IFile jar = getFile("/P/lib/myLib.jar");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(jar);
	assertMemento(
		"=P/lib\\/myLib.jar",
		root);
}
/**
 * Tests that a jar package fragment root in another project
 * can be persisted and restored using its memento.
 */
public void testPackageFragmentRootMemento6() {
	IJavaProject project = getJavaProject("P");
	IFile jar = getFile("/OtherProj/lib/myLib.jar");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(jar);
	assertMemento(
		"=P/\\/OtherProj\\/lib\\/myLib.jar",
		root);
}
/**
 * Tests that an external jar package fragment root
 * can be persisted and restored using its memento.
 */
public void testPackageFragmentRootMemento7() throws CoreException {
	IPackageFragmentRoot root = getPackageFragmentRoot("P", getExternalJCLPathString());
	assertMemento(
		"=P/" + getEscapedExternalJCLPath() + "",
		root);
}
/*
 * Tests that a library folder package fragment root being another project
 * can be persisted and restored using its memento.
 * (regression test for bug 108539 Error popup at breakpoint in tomcat project)
 */
public void testPackageFragmentRootMemento8() {
	IJavaProject project = getJavaProject("P");
	IProject otherLibFolder = getProject("/OtherProj");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(otherLibFolder);
	assertMemento(
		"=P/\\/OtherProj",
		root);
}
/**
 * Tests that a project can be persisted and restored using its memento.
 */
public void testProjectMemento() {
	IJavaProject project = getJavaProject("P");
	assertMemento(
		"=P",
		project);
}
/**
 * Tests that a project with special chararcters in its name can be persisted and restored using its memento.
 * (regression test for bug 47815 Refactoring doesn't work with some project names [refactoring])
 */
public void testProjectMemento2() {
	IJavaProject project = getJavaProject("P (abc) ~");
	assertMemento(
		"=P \\(abc) \\~",
		project);
}
/**
 * Tests that a project with a ']' in its name can be persisted and restored using its memento.
 * (regression test for bug 108615 Unable to inherit abstract methods from jarred interface)
 */
public void testProjectMemento3() {
	IJavaProject project = getJavaProject("P[]");
	assertMemento(
		"=P\\[\\]",
		project);
}
/**
 * Tests that a bogus memento cannot be restored.
 */
public void testRestoreBogusMemento() {
	IJavaElement restored = JavaCore.create("bogus");
	assertEquals("should not be able to restore a bogus memento", null, restored);
}
/**
 * Tests that a source field can be persisted and restored using its memento.
 */
public void testSourceFieldMemento() {
	IField field = getCompilationUnit("/P/src/p/X.java").getType("X").getField("field");
	assertMemento(
		"=P/src<p{X.java[X^field",
		field);
}
/**
 * Tests that a source inner type, inner field and inner method can be persisted and restored
 * using mementos.
 */
public void testSourceInnerTypeMemento() {
	IType innerType = getCompilationUnit("/P/src/p/X.java").getType("X").getType("Inner");
	assertMemento(
		"=P/src<p{X.java[X[Inner",
		innerType);
}
/**
 * Tests that a source method can be persisted and restored using its memento.
 */
public void testSourceMethodMemento1() {
	IType type = getCompilationUnit("/P/src/p/X.java").getType("X");
	IMethod method = type.getMethod("foo", new String[] {"I", "Ljava.lang.String;"});
	assertMemento(
		"=P/src<p{X.java[X~foo~I~Ljava.lang.String;",
		method);
}
/**
 * Tests that a source method can be persisted and restored using its memento.
 */
public void testSourceMethodMemento2() {
	IType type = getCompilationUnit("/P/src/p/X.java").getType("X");
	IMethod method = type.getMethod("bar", new String[] {});
	assertMemento(
		"=P/src<p{X.java[X~bar",
		method);
}
/**
 * Tests that a source method can be persisted and restored using its memento.
 */
public void testSourceMethodMemento3() {
	IType type = getCompilationUnit("/P/src/p/X.java").getType("X");
	IMethod method = type.getMethod("fred", new String[] {"[Z"});
	assertMemento(
		"=P/src<p{X.java[X~fred~\\[Z",
		method);
}
/**
 * Tests that a source type can be persisted and restored using its memento.
 */
public void testSourceTypeMemento() {
	IType type = getCompilationUnit("/P/src/p/X.java").getType("X");
	assertMemento(
		"=P/src<p{X.java[X",
		type);
}
/*
 * Tests that a type parameter can be persisted and restored using its memento.
 */
public void testTypeParameter1() {
	ITypeParameter typeParameter = getCompilationUnit("/P/src/p/X.java").getType("X").getTypeParameter("T");
	assertMemento(
		"=P/src<p{X.java[X]T",
		typeParameter);
}
/*
 * Tests that a type parameter can be persisted and restored using its memento.
 */
public void testTypeParameter2() {
	ITypeParameter typeParameter = getCompilationUnit("/P/src/p/X.java").getType("X").getMethod("foo", new String[0]).getTypeParameter("T");
	assertMemento(
		"=P/src<p{X.java[X~foo]T",
		typeParameter);
}
}
