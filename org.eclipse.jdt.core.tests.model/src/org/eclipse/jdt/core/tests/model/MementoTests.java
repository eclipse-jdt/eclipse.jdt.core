/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.tests.util.Util;
import junit.framework.Test;

public class MementoTests extends ModifyingResourceTests {
public MementoTests(String name) {
	super(name);
}
public static Test suite() {
	return new Suite(MementoTests.class);
}
protected void assertMemento(String expected, IJavaElement element) {
	String actual = element.getHandleIdentifier();
	if (!expected.equals(actual)){
	 	System.out.println(Util.displayString(actual, 2));
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
public void setUpSuite() throws Exception {
	super.setUpSuite();
	
	this.createJavaProject(
			"P", 
			new String[] {"src"}, 
			new String[] {
				getExternalJCLPathString(), 
				"/P/lib",
				"/P/lib/myLib.jar",
				"/OtherProj/lib", 
				"/OtherProj/lib/myLib.jar",
			},
			"bin");
}
public void tearDownSuite() throws Exception {
	this.deleteProject("P");
	super.tearDownSuite();
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
public void testBinaryMethodMemento() throws JavaModelException {
	IType type = getClassFile("/P/lib/p/X.class").getType();

	IMethod method = type.getMethod("foo", new String[] {"I", "Ljava.lang.String;"});
	assertMemento(
		"=P/lib<p(X.class[X~foo~I~Ljava.lang.String;",
		method);
		
	method = type.getMethod("bar", new String[] {});
	assertMemento(
		"=P/lib<p(X.class[X~bar",
		method);
		
	method = type.getMethod("fred", new String[] {"[Z"});
	assertMemento(
		"=P/lib<p(X.class[X~fred~[Z",
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
public void testClassFileMemento() throws JavaModelException {
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
public void testCompilationUnitMemento() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("/P/src/p/X.java");
	assertMemento(
		"=P/src<p{X.java",
		cu);
		
	cu = getCompilationUnit("/P/src/Y.java");
	assertMemento(
		"=P/src<{Y.java",
		cu);
}
/**
 * Tests that an import declaration can be persisted and restored using its memento.
 */
public void testImportDeclarationMemento() throws JavaModelException {
	IImportDeclaration importDecl = getCompilationUnit("/P/src/p/X.java").getImport("java.io.Serializable");
	assertMemento(
		"=P/src<p{X.java#java.io.Serializable",
		importDecl);
	
	importDecl = getCompilationUnit("/P/src/p/X.java").getImport("java.util.*");
	assertMemento(
		"=P/src<p{X.java#java.util.*",
		importDecl);
}
/**
 * Tests that an initializer can be persisted and restored using its memento.
 */
public void testInitializerMemento() throws JavaModelException {
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
		"=P/lib/myLib.jar<p(X.class[X^field",
		field);
}
/**
 * Tests that a inner binary type and field in an internal jar can be persisted and restored using its memento.
 */
public void testInternalJarBinaryInnerTypeMemento() throws JavaModelException {	
	IType type = getPackageFragmentRoot("/P/lib/myLib.jar").getPackageFragment("p").getClassFile("X$Inner.class").getType();
	assertMemento(
		"=P/lib/myLib.jar<p(X$Inner.class[Inner",
		type);
}
/**
 * Tests that a binary method in an internal jar can be persisted and restored using its memento.
 */
public void testInternalJarBinaryMethodMemento() throws JavaModelException {	
	IType type = getPackageFragmentRoot("/P/lib/myLib.jar").getPackageFragment("p").getClassFile("X.class").getType();
	IMethod method = type.getMethod("foo", new String[] {"[Ljava.lang.String;"});
	assertMemento(
		"=P/lib/myLib.jar<p(X.class[X~foo~[Ljava.lang.String;",
		method);
}
/**
 * Tests that a binary type in an internal jar can be persisted and restored using its memento.
 */
public void testInternalJarBinaryTypeMemento() throws JavaModelException {	
	IType type = getPackageFragmentRoot("/P/lib/myLib.jar").getPackageFragment("p").getClassFile("X.class").getType();
	assertMemento(
		"=P/lib/myLib.jar<p(X.class[X",
		type);	
}
/**
 * Tests that a binary field in an external jar can be persisted and restored using its memento.
 */
public void testExternalJarBinaryFieldMemento() throws JavaModelException {	
	IType type = getClassFile("P", getExternalJCLPathString(), "p", "X.class").getType();
	IField field = type.getField("field");
	assertMemento(
		"=P/" + getExternalJCLPath() + "<p(X.class[X^field",
		field);
}
/**
 * Tests that a inner binary type and field in an external jar can be persisted and restored using its memento.
 */
public void testExternalJarBinaryInnerTypeMemento() throws JavaModelException {
	IType type = getClassFile("P", getExternalJCLPathString(), "p", "X$Inner.class").getType();
	assertMemento(
		"=P/" + getExternalJCLPath() + "<p(X$Inner.class[Inner",
		type);
}
/**
 * Tests that a binary method in an external jar can be persisted and restored using its memento.
 */
public void testExternalJarBinaryMethodMemento() throws JavaModelException {	
	IType type = getClassFile("P", getExternalJCLPathString(), "p", "X.class").getType();
	IMethod method = type.getMethod("foo", new String[] {"[Ljava.lang.String;"});
	assertMemento(
		"=P/" + getExternalJCLPath() + "<p(X.class[X~foo~[Ljava.lang.String;",
		method);
}
/**
 * Tests that a binary type in an external jar can be persisted and restored using its memento.
 */
public void testExternalJarBinaryTypeMemento() throws JavaModelException {	
	IType type = getClassFile("P", getExternalJCLPathString(), "p", "X.class").getType();
	assertMemento(
		"=P/" + getExternalJCLPath() + "<p(X.class[X",
		type);	
}
/**
 * Tests that a package declaration can be persisted and restored using its memento.
 */
public void testPackageDeclarationMemento() throws JavaModelException {
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
public void testPackageFragmentMemento() throws JavaModelException {
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
 * Tests that a source folder package fragment root can be persisted and restored using its memento.
 */
public void testPackageFragmentRootMemento1() throws CoreException {
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
public void testPackageFragmentRootMemento3() throws CoreException {
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
public void testPackageFragmentRootMemento4() throws CoreException {
	IJavaProject project = getJavaProject("P");
	IFolder otherLibFolder = getFolder("/OtherProj/lib");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(otherLibFolder);
	assertMemento(
		"=P//OtherProj/lib",
		root);
}
/**
 * Tests that a jar package fragment root in the same project
 * can be persisted and restored using its memento.
 */
public void testPackageFragmentRootMemento5() throws CoreException {
	IJavaProject project = getJavaProject("P");
	IFile jar = getFile("/P/lib/myLib.jar");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(jar);
	assertMemento(
		"=P/lib/myLib.jar",
		root);
}
/**
 * Tests that a jar package fragment root in another project
 * can be persisted and restored using its memento.
 */
public void testPackageFragmentRootMemento6() throws CoreException {
	IJavaProject project = getJavaProject("P");
	IFile jar = getFile("/OtherProj/lib/myLib.jar");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(jar);
	assertMemento(
		"=P//OtherProj/lib/myLib.jar",
		root);
}
/**
 * Tests that an external jar package fragment root
 * can be persisted and restored using its memento.
 */
public void testPackageFragmentRootMemento7() throws CoreException {
	IPackageFragmentRoot root = getPackageFragmentRoot("P", getExternalJCLPathString());
	assertMemento(
		"=P/" + getExternalJCLPath(),
		root);
}
/**
 * Tests that a project can be persisted and restored using its memento.
 */
public void testProjectMemento() throws JavaModelException {
	IJavaProject project = getJavaProject("P");
	assertMemento(
		"=P",
		project);
}
/**
 * Tests that a bogus memento cannot be restored.
 */
public void testRestoreBogusMemento() throws JavaModelException {
	IJavaElement restored = JavaCore.create("bogus");
	assertEquals("should not be able to restore a bogus memento", null, restored);
}
/**
 * Tests that a source field can be persisted and restored using its memento.
 */
public void testSourceFieldMemento() throws JavaModelException {
	IField field = getCompilationUnit("/P/src/p/X.java").getType("X").getField("field");
	assertMemento(
		"=P/src<p{X.java[X^field",
		field);
}
/**
 * Tests that a source inner type, inner field and inner method can be persisted and restored
 * using mementos.
 */
public void testSourceInnerTypeMemento() throws JavaModelException {
	IType innerType = getCompilationUnit("/P/src/p/X.java").getType("X").getType("Inner");
	assertMemento(
		"=P/src<p{X.java[X[Inner",
		innerType);
}
/**
 * Tests that a source method can be persisted and restored using its memento.
 */
public void testSourceMethodMemento() throws JavaModelException {
	IType type = getCompilationUnit("/P/src/p/X.java").getType("X");

	IMethod method = type.getMethod("foo", new String[] {"I", "Ljava.lang.String;"});
	assertMemento(
		"=P/src<p{X.java[X~foo~I~Ljava.lang.String;",
		method);
		
	method = type.getMethod("bar", new String[] {});
	assertMemento(
		"=P/src<p{X.java[X~bar",
		method);
		
	method = type.getMethod("fred", new String[] {"[Z"});
	assertMemento(
		"=P/src<p{X.java[X~fred~[Z",
		method);
}
/**
 * Tests that a source type can be persisted and restored using its memento.
 */
public void testSourceTypeMemento() throws JavaModelException {
	IType type = getCompilationUnit("/P/src/p/X.java").getType("X");
	assertMemento(
		"=P/src<p{X.java[X",
		type);
}
}
