/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
import java.io.IOException;

import junit.framework.Test;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * TO DO:
 * - source attachment on external jar.
 * - don't use assertTrue where assertEquals should be used
 * - don't hardcode positions
*/
public class AttachSourceTests extends ModifyingResourceTests {
	static {
//		TESTS_NAMES = new String[] { "testClassFileBuffer" };
//		TESTS_NUMBERS = new int[] { 5 };
//		TESTS_RANGE = new int[] { 169, 180 };
	}

	public static Test suite() {
		return buildModelTestSuite(AttachSourceTests.class);
	}

	/** @deprecated using deprecated code */
	private static final int AST_INTERNAL_JLS2 = AST.JLS2;

	private IPackageFragmentRoot pkgFragmentRoot;
	private IType genericType;
	private IPackageFragment innerClasses;

public AttachSourceTests(String name) {
	super(name);
}
protected String getExternalFolder() {
	return getExternalResourcePath("externalFolder");
}
public ASTNode runConversion(IClassFile classFile, boolean resolveBindings) {
	ASTParser parser = ASTParser.newParser(AST_INTERNAL_JLS2);
	parser.setSource(classFile);
	parser.setResolveBindings(resolveBindings);
	parser.setWorkingCopyOwner(null);
	return parser.createAST(null);
}
protected void setUp() throws Exception {
	super.setUp();
	attachSource(this.pkgFragmentRoot, "/AttachSourceTests/attachsrc.zip", "");
}
/**
 * Create project and set the jar placeholder.
 */
public void setUpSuite() throws Exception {
	super.setUpSuite();

	setUpJavaProject("AttachSourceTests");
	addLibraryEntry("/AttachSourceTests/b153133.jar", false);
	this.pkgFragmentRoot = this.currentProject.getPackageFragmentRoot(getFile("/AttachSourceTests/attach.jar"));
	setUpGenericJar();
	setUpInnerClassesJar();
	setupExternalLibrary();
}
private void setupExternalLibrary() throws IOException {
	String externalFolder = getExternalFolder();
	String[] pathsAndContents =
		new String[] {
			"p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"}"
		};
	org.eclipse.jdt.core.tests.util.Util.createClassFolder(pathsAndContents, externalFolder + "/lib", "1.4");
	org.eclipse.jdt.core.tests.util.Util.createSourceDir(pathsAndContents, externalFolder + "/src");

	org.eclipse.jdt.core.tests.util.Util.createJar(pathsAndContents, externalFolder + "/lib.abc", "1.4");
	org.eclipse.jdt.core.tests.util.Util.createSourceZip(pathsAndContents, externalFolder + "/src.abc");
}
private void setUpGenericJar() throws IOException, CoreException {
	String[] pathAndContents = new String[] {
		"generic/X.java",
		"package generic;\n" +
		"public class X<T> {\n" +
		"  void foo(X<T> x) {\n" +
		"  }\n" +
		"  <K, V> V foo(K key, V value) {\n" +
		"    return value;\n" +
		"  }\n" +
		"  void foo(int i, X<Object[]> x) {\n" +
		"  }\n" +
		"  void foo(boolean b, X<? extends X> x) {\n" +
		"  }\n" +
		"  void foo(float f, X<?> x) {\n" +
		"  }\n" +
		"  void foo(Y<? extends Integer, ? extends Object> y) {\n" +
		"  }\n" +
		"  void foo(Z.Inner<Object> inner) {\n" +
		"  }\n" +
		"  void foo(AType<Object> t) {\n" +
		"  }\n" +
		"}\n" +
		"class Y<K, V> {\n" +
		"}\n" +
		"class Z {\n" +
		"  class Inner<E> {\n" +
		"  }\n" +
		"}\n" +
		"class AType<E> {\n" + // type name containing character 'T'
		"}"
	};
	addLibrary("generic.jar", "genericsrc.zip", pathAndContents, JavaCore.VERSION_1_5);
	IFile jar = getFile("/AttachSourceTests/generic.jar");
	this.genericType = this.currentProject.getPackageFragmentRoot(jar).getPackageFragment("generic").getClassFile("X.class").getType();
}
private void setUpInnerClassesJar() throws IOException, CoreException {
	String[] pathAndContents = new String[] {
		"inner/X.java",
		"package inner;\n" +
		"public class X {\n" +
		"  void foo() {\n" +
		"    new X() {};\n" +
		"    class Y {}\n" +
		"    new Y() {\n" +
		"      class Z {}\n" +
		"    };\n" +
		"    class W {\n" +
		"      void bar() {\n" +
		"        new W() {};\n" +
		"      }\n" +
		"    }\n" +
		"    new Object() {\n" +
		"      class U {\n" +
		"        U(String s) {\n" +
		"        }\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"  class V {\n" +
		"    V(String s) {\n" +
		"    }\n" +
		"  }\n" +
		"  class Inner {\n" +
		"    Inner() {\n" +
		"    }\n" +
		"  }\n" +
		"}"
	};
	addLibrary("innerClasses.jar", "innerClassessrc.zip", pathAndContents, JavaCore.VERSION_1_4);
	IFile jar = getFile("/AttachSourceTests/innerClasses.jar");
	this.innerClasses = this.currentProject.getPackageFragmentRoot(jar).getPackageFragment("inner");
}
protected void tearDown() throws Exception {
	IPackageFragmentRoot[] roots = this.currentProject.getAllPackageFragmentRoots();
	for (int i = 0; i < roots.length; i++) {
		IPackageFragmentRoot root = roots[i];
		if (this.genericType != null && root.equals(this.genericType.getPackageFragment().getParent())) continue;
		if (this.innerClasses != null && root.equals(this.innerClasses.getParent())) continue;
		if (root.getKind() == IPackageFragmentRoot.K_BINARY) {
			attachSource(root, null, null); // detach source
		}
	}
	super.tearDown();
}

/**
 * Reset the jar placeholder and delete project.
 */
public void tearDownSuite() throws Exception {
	org.eclipse.jdt.core.tests.util.Util.flushDirectoryContent(new File(getExternalFolder()));
	deleteProject(this.currentProject);
	super.tearDownSuite();
}

/**
 * Test AST.parseCompilationUnit(IClassFile, boolean).
 */
public void testASTParsing() throws JavaModelException {
	attachSource(this.pkgFragmentRoot, "/AttachSourceTests/attachsrc.zip", "");
	IClassFile classFile = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	ASTNode node = runConversion(classFile, true);
	assertNotNull("No node", node);
	attachSource(this.pkgFragmentRoot, null, null);
	IClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	assertTrue("source code should no longer exist for A", cf.getSource() == null);
	try {
		node = runConversion(classFile, true);
		assertTrue("Should not be here", false);
	} catch(IllegalStateException e) {
		assertTrue(true);
	}
}
/**
 * Test AST.parseCompilationUnit(IClassFile, boolean).
 * Test for http://bugs.eclipse.org/bugs/show_bug.cgi?id=30471
 */
public void testASTParsing2() throws JavaModelException {
	attachSource(this.pkgFragmentRoot, "/AttachSourceTests/attachsrc.zip", "");
	IClassFile classFile = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	ASTNode node = runConversion(classFile, false);
	assertNotNull("No node", node);
	attachSource(this.pkgFragmentRoot, null, null);
	IClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	assertTrue("source code should no longer exist for A", cf.getSource() == null);
	try {
		node = runConversion(classFile, false);
		assertTrue("Should not be here", false);
	} catch(IllegalStateException e) {
		assertTrue(true);
	}
}
/**
 * Changing the source attachment file should update the java model.
 * (regression test for bug 23292 Must restart Eclipse after debug of source in .zip is updated)
 */
public void testChangeSourceAttachmentFile() throws CoreException {
	IClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	IMethod method = cf.getType().getMethod("foo", new String[] {});

	// check initial source
	assertSourceEquals(
		"unexpected initial source for foo()",
		"public void foo() {\n" +
		"	}",
		method.getSource());

	// replace source attachment file
	swapFiles("AttachSourceTests/attachsrc.zip", "AttachSourceTests/attachsrc.new.zip");
	assertSourceEquals(
		"unexpected source for foo() after replacement",
		"public void foo() {\n" +
		"		System.out.println(\"foo\");\n" +
		"	}",
		method.getSource());

	// delete source attachment file
	deleteFile("AttachSourceTests/attachsrc.zip");
	assertSourceEquals(
		"unexpected source for foo() after deletion",
		null,
		method.getSource());

	// add source attachment file back
	moveFile("AttachSourceTests/attachsrc.new.zip", "AttachSourceTests/attachsrc.zip");
	assertSourceEquals(
		"unexpected source for foo() after addition",
		"public void foo() {\n" +
		"	}",
		method.getSource());
}
/**
 * Ensure that a class file with an attached source can retrieve its children given a source index.
 */
public void testClassFileGetElementAt01() throws JavaModelException {
	IClassFile classFile = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	String source = classFile.getSource();
	IJavaElement element = classFile.getElementAt(source.indexOf("class A"));
	assertElementExists(
		"Unexpected element",
		"A [in A.class [in x.y [in attach.jar [in AttachSourceTests]]]]",
		element);
}
/**
 * Ensure that a class file with an attached source can retrieve its children given a source index.
 */
public void testClassFileGetElementAt02() throws JavaModelException {
	IClassFile classFile = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	String source = classFile.getSource();
	IJavaElement element = classFile.getElementAt(source.indexOf("public A"));
	assertElementExists(
		"Unexpected element",
		"A() [in A [in A.class [in x.y [in attach.jar [in AttachSourceTests]]]]]",
		element);
}
/**
 * Ensure that a class file with an attached source can retrieve its children given a source index.
 */
public void testClassFileGetElementAt03() throws JavaModelException {
	IClassFile classFile = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	String source = classFile.getSource();
	IJavaElement element = classFile.getElementAt(source.indexOf("void foo"));
	assertElementExists(
		"Unexpected element",
		"foo() [in A [in A.class [in x.y [in attach.jar [in AttachSourceTests]]]]]",
		element);
}
/*
 * Ensure that a constructor of a binary member type can be retrieved with its source position.
 * (regression test for bug 119249 codeResolve, search, etc. don't work on constructor of binary inner class)
 */
public void testClassFileGetElementAt04() throws JavaModelException {
	IClassFile classFile = this.innerClasses.getClassFile("X$V.class");
	String source = classFile.getSource();
	IJavaElement element = classFile.getElementAt(source.indexOf("V(String s)"));
	assertElementExists(
		"Unexpected element",
		"V(inner.X, java.lang.String) [in V [in X$V.class [in inner [in innerClasses.jar [in AttachSourceTests]]]]]",
		element);
}
/*
 * Ensures that the source of a .class file is implicetely attached when prj=src=bin
 * (regression test for bug 41444 [navigation] error dialog on opening class file)
 */
public void testClassFileInOutput() throws CoreException {
	IClassFile classFile = getClassFile("AttachSourceTests/src/A.class");
	String source = classFile.getSource();
	assertSourceEquals(
		"Unexpected source",
		"public class A {\n" +
		"}",
		source);
}
/**
 * Retrieves the source code for "A.class", which is
 * the entire CU for "A.java".
 */
public void testClassRetrieval() throws JavaModelException {
	IClassFile objectCF = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	assertTrue("source code does not exist for the entire attached compilation unit", objectCF.getSource() != null);
}
/**
 * Removes the source attachment from the jar.
 */
public void testDetachSource() throws JavaModelException {
	attachSource(this.pkgFragmentRoot, null, null);
	IClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	assertTrue("source code should no longer exist for A", cf.getSource() == null);
	assertTrue("name range should no longer exist for A", cf.getType().getNameRange().getOffset() == -1);
	assertTrue("source range should no longer exist for A", cf.getType().getSourceRange().getOffset() == -1);
	assertTrue("Source attachment path should be null", null == this.pkgFragmentRoot.getSourceAttachmentPath());
	assertTrue("Source attachment root path should be null", null ==this.pkgFragmentRoot.getSourceAttachmentRootPath());
}
/*
 * Ensures that one can attach an external source folder to a library folder.
 */
public void testExternalFolder1() throws CoreException {
	try {
		IProject p = createProject("P1");
		IFolder lib = p.getFolder("lib");
		lib.createLink(new Path(getExternalFolder() + "/lib"), IResource.NONE, null);
		IJavaProject javaProject = createJavaProject("P2", new String[0], new String[] {"/P1/lib"}, "");
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(lib);
		attachSource(root, getExternalFolder() + "/src", "");
		IType type = root.getPackageFragment("p").getClassFile("X.class").getType();
		assertSourceEquals(
			"Unexpected source",
			"public class X {\n" +
			"}",
			type.getSource());
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/*
 * Ensures that one can attach a source folder to an external library folder.
 */
public void testExternalFolder2() throws CoreException {
	try {
		IProject p = createProject("P1");
		IFolder src = p.getFolder("src");
		src.createLink(new Path(getExternalFolder() + "/src"), IResource.NONE, null);
		String externalLib = getExternalFolder() + "/lib";
		IJavaProject javaProject = createJavaProject("P2", new String[0], new String[] {externalLib}, "");
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(externalLib);
		attachSource(root, "/P1/src", "");
		IType type = root.getPackageFragment("p").getClassFile("X.class").getType();
		assertSourceEquals(
			"Unexpected source",
			"public class X {\n" +
			"}",
			type.getSource());
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/*
 * Ensures that one can attach an external source folder to an external library folder.
 */
public void testExternalFolder3() throws CoreException {
	try {
		String externalLib = getExternalFolder() + "/lib";
		IJavaProject javaProject = createJavaProject("P", new String[0], new String[] {externalLib}, "");
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(externalLib);
		attachSource(root, getExternalFolder() + "/src", "");
		IType type = root.getPackageFragment("p").getClassFile("X.class").getType();
		assertSourceEquals(
			"Unexpected source",
			"public class X {\n" +
			"}",
			type.getSource());
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that root paths are correctly detected when attaching an external source folder to an external library folder.
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=227813 )
 */
public void testExternalFolder4() throws Exception {
	try {
		String externalFolder = getExternalFolder();
		String[] pathsAndContents =
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"}"
			};
		org.eclipse.jdt.core.tests.util.Util.createSourceDir(pathsAndContents, externalFolder + "/src227813/root1/subroot");
		pathsAndContents =
			new String[] {
				"q/X.java",
				"package q;\n" +
				"public class X {\n" +
				"}"
			};
		org.eclipse.jdt.core.tests.util.Util.createSourceDir(pathsAndContents, externalFolder + "/src227813/root2/subroot");

		String externalLib = externalFolder + "/lib";
		IJavaProject javaProject = createJavaProject("P", new String[0], new String[] {externalLib}, "");
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(externalLib);
		attachSource(root, externalFolder + "/src227813", "");
		IType type = root.getPackageFragment("p").getClassFile("X.class").getType();
		assertSourceEquals(
			"Unexpected source",
			"public class X {\n" +
			"}",
			type.getSource());
	} finally {
		deleteExternalResource("externalFolder/src227813");
		deleteProject("P");
	}
}
/*
 * Ensures that root paths are correctly detected when attaching an external source folder that contains a META-INF folder to an external library folder.
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=228639 )
 */
public void testExternalFolder5() throws Exception {
	try {
		String externalFolder = getExternalFolder();
		String[] pathsAndContents =
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"}"
			};
		org.eclipse.jdt.core.tests.util.Util.createSourceDir(pathsAndContents, externalFolder + "/src228639/src");
		createExternalFolder("externalFolder/src228639/META-INF");
		createExternalFolder("externalFolder/lib/META-INF");

		String externalLib = externalFolder + "/lib";
		IJavaProject javaProject = createJavaProject("P", new String[0], new String[] {externalLib}, "");
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(externalLib);
		attachSource(root, externalFolder + "/src228639", "");
		IType type = root.getPackageFragment("p").getClassFile("X.class").getType();
		assertSourceEquals(
			"Unexpected source",
			"public class X {\n" +
			"}",
			type.getSource());
	} finally {
		deleteExternalResource("externalFolder/src228639");
		deleteExternalResource("externalFolder/lib/META-INF");
		deleteProject("P");
	}
}
/*
 * Ensures that one can attach an external ZIP archive containing sources to a library folder.
 */
public void testZIPArchive1() throws CoreException {
	try {
		IProject p = createProject("P1");
		IFolder lib = p.getFolder("lib");
		lib.createLink(new Path(getExternalFolder() + "/lib"), IResource.NONE, null);
		IJavaProject javaProject = createJavaProject("P2", new String[0], new String[] {"/P1/lib"}, "");
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(lib);
		attachSource(root, getExternalFolder() + "/src.abc", "");
		IType type = root.getPackageFragment("p").getClassFile("X.class").getType();
		assertSourceEquals(
			"Unexpected source",
			"public class X {\n" +
			"}",
			type.getSource());
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/*
 * Ensures that one can attach a source folder to an external ZIP archive.
 */
public void testZIPArchive2() throws CoreException {
	try {
		IProject p = createProject("P1");
		IFolder src = p.getFolder("src");
		src.createLink(new Path(getExternalFolder() + "/src"), IResource.NONE, null);
		String externalLib = getExternalFolder() + "/lib.abc";
		IJavaProject javaProject = createJavaProject("P2", new String[0], new String[] {externalLib}, "");
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(externalLib);
		attachSource(root, "/P1/src", "");
		IType type = root.getPackageFragment("p").getClassFile("X.class").getType();
		assertSourceEquals(
			"Unexpected source",
			"public class X {\n" +
			"}",
			type.getSource());
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/*
 * Ensures that one can attach an external ZIP archive containing sources to an external ZIP archive.
 */
public void testZIPArchive3() throws CoreException {
	try {
		String externalLib = getExternalFolder() + "/lib.abc";
		IJavaProject javaProject = createJavaProject("P", new String[0], new String[] {externalLib}, "");
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(externalLib);
		attachSource(root, getExternalFolder() + "/src.abc", "");
		IType type = root.getPackageFragment("p").getClassFile("X.class").getType();
		assertSourceEquals(
			"Unexpected source",
			"public class X {\n" +
			"}",
			type.getSource());
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that one can attach an internal ZIP archive containing sources to an internal ZIP archive.
 */
public void testZIPArchive4() throws CoreException {
	try {
		IProject p = createProject("P1");
		IFile lib = p.getFile("lib.abc");
		lib.createLink(new Path(getExternalFolder() + "/lib.abc"), IResource.NONE, null);
		IFile src = p.getFile("src.abc");
		src.createLink(new Path(getExternalFolder() + "/src.abc"), IResource.NONE, null);
		IJavaProject javaProject = createJavaProject("P2", new String[0], new String[] {"/P1/lib.abc"}, "");
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(lib);
		attachSource(root, "/P1/src.abc", "");
		IType type = root.getPackageFragment("p").getClassFile("X.class").getType();
		assertSourceEquals(
			"Unexpected source",
			"public class X {\n" +
			"}",
			type.getSource());
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/*
 * Ensures that the source of a generic method can be retrieved.
 */
public void testGeneric1() throws JavaModelException {
	IMethod method = this.genericType.getMethod("foo", new String[] {"QX<QT;>;"});
	assertSourceEquals(
		"Unexpected source",
		"void foo(X<T> x) {\n" +
		"  }",
		method.getSource());
}
/*
 * Ensures that the source of a generic method can be retrieved.
 */
public void testGeneric2() throws JavaModelException {
	IMethod method = this.genericType.getMethod("foo", new String[] {"QK;", "QV;"});
	assertSourceEquals(
		"Unexpected source",
		"<K, V> V foo(K key, V value) {\n" +
		"    return value;\n" +
		"  }",
		method.getSource());
}
/*
 * Ensures that the source of a generic method can be retrieved.
 * (regression test for bug 129317 Outline view inconsistent with code
 */
public void testGeneric3() throws JavaModelException {
	IMethod method = this.genericType.getMethod("foo", new String[] {"I", "Lgeneric.X<[Ljava.lang.Object;>;"});
	assertSourceEquals(
		"Unexpected source",
		"void foo(int i, X<Object[]> x) {\n" +
		"  }",
		method.getSource());
}
public void testGeneric4() throws JavaModelException {
	IMethod method = this.genericType.getMethod("foo", new String[] {"Z", "Lgeneric.X<+Lgeneric.X;>;"});
	assertSourceEquals(
		"Unexpected source",
		"void foo(boolean b, X<? extends X> x) {\n" +
		"  }",
		method.getSource());
}
public void testGeneric5() throws JavaModelException {
	IMethod method = this.genericType.getMethod("foo", new String[] {"F", "Lgeneric.X<*>;"});
	assertSourceEquals(
		"Unexpected source",
		"void foo(float f, X<?> x) {\n" +
		"  }",
		method.getSource());
}
public void testGeneric6() throws JavaModelException {
	IMethod method = this.genericType.getMethod("foo", new String[] {"Lgeneric.Y<+Ljava.lang.Integer;+Ljava.lang.Object;>;"});
	assertSourceEquals(
		"Unexpected source",
		"void foo(Y<? extends Integer, ? extends Object> y) {\n" +
		"  }",
		method.getSource());
}
public void testGeneric7() throws JavaModelException {
	IMethod method = this.genericType.getMethod("foo", new String[] {"Lgeneric.Z.Inner<Ljava.lang.Object;>;"});
	assertSourceEquals(
		"Unexpected source",
		"void foo(Z.Inner<Object> inner) {\n" +
		"  }",
		method.getSource());
}
public void testGeneric8() throws JavaModelException {
	IMethod method = this.genericType.getMethod("foo", new String[] {"Lgeneric.AType<Ljava.lang.Object;>;"});
	assertSourceEquals(
		"Unexpected source",
		"void foo(AType<Object> t) {\n" +
		"  }",
		method.getSource());
}
/**
 * Ensures that name ranges exists for BinaryMembers that have
 * mapped source.
 */
public void testGetNameRange01() throws JavaModelException {
	IClassFile classFile = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	IMethod method = classFile.getType().getMethod("foo", null);
	assertSourceEquals("Unexpected name source", "foo", getNameSource(classFile.getSource(), method));
}
/**
 * Ensures that name ranges exists for BinaryMembers that have
 * mapped source.
 */
public void testGetNameRange02() throws JavaModelException {
	IClassFile classFile = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	assertSourceEquals("Unexpected name source", "A", getNameSource(classFile.getSource(), classFile.getType()));
}
/*
 * Ensure that the name range for a constructor of a binary member type is correct.
 * (regression test for bug 119249 codeResolve, search, etc. don't work on constructor of binary inner class)
 */
public void testGetNameRange03() throws JavaModelException {
	IClassFile classFile = this.innerClasses.getClassFile("X$V.class");
	IMethod constructor = classFile.getType().getMethod("V", new String[] {"Linner.X;", "Ljava.lang.String;"});
	assertSourceEquals("Unexpected name source", "V", getNameSource(classFile.getSource(), constructor));
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=108784
public void testGetNameRange04() throws JavaModelException {
	IClassFile classFile = this.innerClasses.getClassFile("X$Inner.class");
	IMethod[] methods = classFile.getType().getMethods();
	for (int i = 0; i < methods.length; i++) {
		IMethod iMethod = methods[i];
		ISourceRange nameRange = iMethod.getNameRange();
		assertTrue("Unexpected name range", nameRange.getOffset() != -1);
		assertTrue("Unexpected name range", nameRange.getLength() != 0);
	}
}
/**
 * Retrieves the source attachment paths for jar root.
 */
public void testGetSourceAttachmentPath() throws JavaModelException {
	IPath saPath= this.pkgFragmentRoot.getSourceAttachmentPath();
	assertEquals("Source attachment path not correct for root " + this.pkgFragmentRoot, "/AttachSourceTests/attachsrc.zip", saPath.toString());
	assertEquals("Source attachment root path should be empty", new Path(""), this.pkgFragmentRoot.getSourceAttachmentRootPath());
}
/**
 * Ensures that a source range exists for the class file that has
 * mapped source.
 */
public void testGetSourceRange() throws JavaModelException {
	IClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	ISourceRange sourceRange = cf.getSourceRange();
	assertTrue("Class file should have associated source range", sourceRange != null);
	assertEquals("Unexpected offset", 0, sourceRange.getOffset());
	assertEquals("Unexpected length", 100, sourceRange.getLength());
}
/**
 * Ensures that a source range exists for the (inner) class file that has
 * mapped source.
 */
public void testGetSourceRangeInnerClass() throws JavaModelException {
	IClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A$Inner.class");
	ISourceRange sourceRange = cf.getSourceRange();
	assertTrue("Inner class file should have associated source range", sourceRange != null);
	assertEquals("Unexpected offset", 0, sourceRange.getOffset());
	assertEquals("Unexpected length", 100, sourceRange.getLength());
}
/*
 * Ensures that the source of an inner class can be retrieved.
 */
public void testInnerClass1() throws JavaModelException {
	IType type = this.innerClasses.getClassFile("X.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"public class X {\n" +
		"  void foo() {\n" +
		"    new X() {};\n" +
		"    class Y {}\n" +
		"    new Y() {\n" +
		"      class Z {}\n" +
		"    };\n" +
		"    class W {\n" +
		"      void bar() {\n" +
		"        new W() {};\n" +
		"      }\n" +
		"    }\n" +
		"    new Object() {\n" +
		"      class U {\n" +
		"        U(String s) {\n" +
		"        }\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"  class V {\n" +
		"    V(String s) {\n" +
		"    }\n" +
		"  }\n" +
		"  class Inner {\n" + 
		"    Inner() {\n" + 
		"    }\n" + 
		"  }\n" + 
		"}",
		type.getSource());
}
/*
 * Ensures that the source of an inner class can be retrieved.
 */
public void testInnerClass2() throws JavaModelException {
	IType type = this.innerClasses.getClassFile("X$1.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"new X() {}",
		type.getSource());
}
/*
 * Ensures that the source of an inner class can be retrieved.
 */
public void testInnerClass3() throws JavaModelException {
	IType type = this.innerClasses.getClassFile("X$2.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"new Y() {\n" +
		"      class Z {}\n" +
		"    }",
		type.getSource());
}
/*
 * Ensures that the source of an inner class can be retrieved.
 */
public void testInnerClass4() throws JavaModelException {
	IType type = this.innerClasses.getClassFile("X$3.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"new W() {}",
		type.getSource());
}
/*
 * Ensures that the source of an inner class can be retrieved.
 */
public void testInnerClass5() throws JavaModelException {
	IType type = this.innerClasses.getClassFile("X$1$Y.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"class Y {}",
		type.getSource());
}
/*
 * Ensures that the source of an inner class can be retrieved.
 */
public void testInnerClass6() throws JavaModelException {
	IType type = this.innerClasses.getClassFile("X$1$W.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"class W {\n" +
		"      void bar() {\n" +
		"        new W() {};\n" +
		"      }\n" +
		"    }",
		type.getSource());
}
/*
 * Ensures that the source of an inner class can be retrieved.
 */
public void testInnerClass7() throws JavaModelException {
	IType type = this.innerClasses.getClassFile("X$2$Z.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"class Z {}",
		type.getSource());
}
/*
 * Ensures that the source of an inner class can be retrieved.
 */
public void testInnerClass8() throws JavaModelException {
	IType type = this.innerClasses.getClassFile("X$V.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"class V {\n" +
		"    V(String s) {\n" +
		"    }\n" +
		"  }",
		type.getSource());
}
/*
 * Ensures that the source of an inner class can be retrieved.
 * (regression test for bug 124611 IAE in Signature.createCharArrayTypeSignature)
 */
public void testInnerClass9() throws JavaModelException {
	IType type = this.innerClasses.getClassFile("X$4$U.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"class U {\n" +
		"        U(String s) {\n" +
		"        }\n" +
		"      }",
		type.getSource());
}

/**
 * Ensures that a source folder can be attached to a lib folder.
 */
public void testLibFolder() throws JavaModelException {
	IPackageFragmentRoot root = this.getPackageFragmentRoot("/AttachSourceTests/lib");
	attachSource(root, "/AttachSourceTests/srcLib", "");

	IClassFile cf = root.getPackageFragment("p").getClassFile("X.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p;\n" +
		"public class X {\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}",
		cf.getSource());
}
/**
 * Retrieves the source code for methods of class A.
 */
public void testMethodRetrieval() throws JavaModelException {
	IClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	IMethod[] methods = cf.getType().getMethods();
	for (int i = 0; i < methods.length; i++) {
		IMethod method = methods[i];
		assertTrue("source code does not exist for the method " + method, method.getSource() != null);
		assertTrue("method name range not correct", method.getNameRange().getOffset() != -1 && method.getNameRange().getLength() != 0);
	}
}
/**
 * Closes the jar, to ensure when it is re-opened the source
 * attachment still exists.
 */
public void testPersistence() throws JavaModelException {
	this.pkgFragmentRoot.close();
	testClassRetrieval();
	testMethodRetrieval();
}

/*
 * Ensures that having a project as a class folder and attaching its sources finds the source
 * of a class in a non-default package.
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=65186)
 */
public void testProjectAsClassFolder1() throws CoreException {
	try {
		createJavaProject("P1");
		createFolder("/P1/p");
		createFile(
			"/P1/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"}"
		);
		IProject p1 = getProject("P1");
		p1.build(IncrementalProjectBuilder.FULL_BUILD, null);
		IJavaProject javaProject = createJavaProject("P2", new String[]{""}, new String[]{"/P1"}, "");
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(p1);
		attachSource(root, "/P1", null);
		IClassFile cf = root.getPackageFragment("p").getClassFile("X.class");
		assertSourceEquals(
			"Unexpected source for class file P1/p/X.class",
			"package p;\n" +
			"public class X {\n" +
			"}",
			cf.getSource());
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}

/*
 * Ensures that having a project as a class folder and attaching its sources finds the source
 * of a class in the default package.
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=65186)
 */
public void testProjectAsClassFolder2() throws CoreException {
	try {
		createJavaProject("P1");
		createFile(
			"/P1/X.java",
			"public class X {\n" +
			"}"
		);
		IProject p1 = getProject("P1");
		p1.build(IncrementalProjectBuilder.FULL_BUILD, null);
		IJavaProject javaProject = createJavaProject("P2", new String[]{""}, new String[]{"/P1"}, "");
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(p1);
		attachSource(root, "/P1", null);
		IClassFile cf = root.getPackageFragment("").getClassFile("X.class");
		assertSourceEquals(
			"Unexpected source for class file P1/X.class",
			"public class X {\n" +
			"}",
			cf.getSource());
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}

/*
 * Ensures that having a project as source attachement finds the source
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=65186)
 */
public void testProjectAsSourceAttachment() throws CoreException {
	try {
		IJavaProject javaProject = createJavaProject("Test", new String[]{""}, new String[]{"/AttachSourceTests/test.jar"}, "");
		createFolder("/Test/test1");
		createFile("/Test/test1/Test.java",
			"package test1;\n" +
			"\n" +
			"public class Test {}");
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(getFile("/AttachSourceTests/test.jar"));
		attachSource(root, "/Test", null);
		IClassFile cf = root.getPackageFragment("test1").getClassFile("Test.class");
		assertSourceEquals(
			"Unexpected source for class file test1/Test.class",
			"package test1;\n" +
			"\n" +
			"public class Test {}",
			cf.getSource());
	} finally {
		deleteProject("Test");
	}
}

/*
 * Ensures that a source attached during a session is not taken into account on restart
 * if the entry has a source attachment.
 * (regression test for bug 183413 PDE can't find the source for plug-ins in the target)
 */
public void testRestart() throws Exception {
	try {
		this.pkgFragmentRoot.attachSource(new Path("/AttachSourceTests/attachsrc.new.zip"), new Path("")/*source root*/, null/*no progress*/);

		simulateExitRestart();
		JavaCore.initializeAfterLoad(null);

		// ensure source is correct
		IClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
		IMethod method = cf.getType().getMethod("foo", new String[] {});
		assertSourceEquals(
			"unexpected source for foo()",
			"public void foo() {\n" +
			"	}",
			method.getSource());
	} finally {
		this.pkgFragmentRoot.attachSource(null/*no source attachment*/, null/*no source root*/, null/*no progress*/);
	}
}

/**
 * Attaches a source zip to a jar.  The source zip has
 * a nested root structure and exists as a resource.  Tests that
 * the attachment is persisted as a server property for the jar.
 */
public void testRootPath() throws JavaModelException {
	IJavaProject project = getJavaProject("AttachSourceTests");
	IFile jar = (IFile) project.getProject().findMember("attach2.jar");
	IFile srcZip=(IFile) project.getProject().findMember("attach2src.zip");
	JarPackageFragmentRoot root = (JarPackageFragmentRoot) project.getPackageFragmentRoot(jar);
	root.attachSource(srcZip.getFullPath(), new Path("src/nested"), null);

	IClassFile cf = root.getPackageFragment("x.y").getClassFile("B.class");
	assertTrue("source code does not exist for the entire attached compilation unit", cf.getSource() != null);
	root.close();
	cf = root.getPackageFragment("x.y").getClassFile("B.class");
	assertTrue("source code does not exist for the entire attached compilation unit", cf.getSource() != null);

	IPath rootSAPath= root.getSourceAttachmentRootPath();
	assertEquals("Unexpected source attachment root path for " + root.getPath(), "src/nested", rootSAPath.toString());

	IPath saPath= root.getSourceAttachmentPath();
	assertEquals("Unexpected source attachment path for " + root.getPath(), "/AttachSourceTests/attach2src.zip", saPath.toString());

	root.close();
}
/**
 * Attaches a source zip to a jar specifying an invalid root path.
 * Ensures that the root path is just used as a hint, and that the source is still retrieved.
 */
public void testRootPath2() throws JavaModelException {
	IJavaProject project = getJavaProject("AttachSourceTests");
	IFile jar = (IFile) project.getProject().findMember("attach2.jar");
	IFile srcZip=(IFile) project.getProject().findMember("attach2src.zip");
	JarPackageFragmentRoot root = (JarPackageFragmentRoot) project.getPackageFragmentRoot(jar);
	root.attachSource(srcZip.getFullPath(), new Path(""), null);

	IClassFile cf = root.getPackageFragment("x.y").getClassFile("B.class");
	assertTrue("source code does not exist for the entire attached compilation unit", cf.getSource() != null);
	root.close();
}
/**
 * Attaches a sa source folder can be attached to a lib folder specifying an invalid root path.
 * Ensures that the root path is just used as a hint, and that the source is still retrieved.
 */
public void testRootPath3() throws JavaModelException {
	IPackageFragmentRoot root = this.getPackageFragmentRoot("/AttachSourceTests/lib");
	attachSource(root, "/AttachSourceTests/srcLib", "invalid");

	IClassFile cf = root.getPackageFragment("p").getClassFile("X.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p;\n" +
		"public class X {\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}",
		cf.getSource());
	root.close();
}
/**
 * Attach a jar with a source attachement that doesn't contain the source folders
 */
public void testRootPath4() throws JavaModelException {
	IJavaProject project = getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/test.jar"));
	attachSource(root, "/AttachSourceTests/src.zip", "invalid");

	IClassFile cf = root.getPackageFragment("test1").getClassFile("Test.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package test1;\n" +
		"\n" +
		"public class Test {}",
		cf.getSource());
	root.close();
}
/**
 * Attach a jar with a source attachement that doesn't contain the source folders
 */
public void testRootPath5() throws JavaModelException {
	IJavaProject project = getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/update.jar"));
	attachSource(root, "/AttachSourceTests/src.zip", "invalid");

	IClassFile cf = root.getPackageFragment("p1.p2").getClassFile("A.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p1.p2;\n" +
		"\n" +
		"public class A {}",
		cf.getSource());

	cf = root.getPackageFragment("").getClassFile("B.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"public class B {}",
		cf.getSource());

	attachSource(root, null, null); // detach source
	root.close();
}
/**
 * Attach a jar with a source attachement that doesn't contain the source folders
 */
public void testRootPath6() throws JavaModelException {
	IJavaProject project = getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/update.jar"));
	attachSource(root, "/AttachSourceTests/src.zip", null);

	IClassFile cf = root.getPackageFragment("p1.p2").getClassFile("A.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p1.p2;\n" +
		"\n" +
		"public class A {}",
		cf.getSource());

	cf = root.getPackageFragment("").getClassFile("B.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"public class B {}",
		cf.getSource());

	attachSource(root, null, null); // detach source
	root.close();
}
/**
 * Attach a jar with a source attachement that doesn't contain the source folders
 */
public void testRootPath7() throws JavaModelException {
	IJavaProject project = getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/full.jar"));
	attachSource(root, "/AttachSourceTests/src.zip", null);

	IClassFile cf = root.getPackageFragment("p1.p2").getClassFile("A.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p1.p2;\n" +
		"\n" +
		"public class A {}",
		cf.getSource());

	cf = root.getPackageFragment("").getClassFile("B.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"public class B {}",
		cf.getSource());

	cf = root.getPackageFragment("test1").getClassFile("Test.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package test1;\n" +
		"\n" +
		"public class Test {}",
		cf.getSource());

	attachSource(root, null, null); // detach source
	root.close();
}
/**
 * Attach a jar with a source attachement that contains the source folders
 */
public void testRootPath8() throws JavaModelException {
	IJavaProject project = getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/full.jar"));
	attachSource(root, "/AttachSourceTests/fullsrc.zip", null);

	IClassFile cf = root.getPackageFragment("p1.p2").getClassFile("A.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p1.p2;\n" +
		"\n" +
		"public class A {}",
		cf.getSource());

	cf = root.getPackageFragment("").getClassFile("B.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"public class B {}",
		cf.getSource());

	cf = root.getPackageFragment("test1").getClassFile("Test.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package test1;\n" +
		"\n" +
		"public class Test {}",
		cf.getSource());

	attachSource(root, null, null); // detach source
	root.close();
}
/**
 * Attach a jar with a source attachement that contains the source folders
 */
public void testRootPath9() throws JavaModelException {
	IJavaProject project = getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/full.jar"));
	attachSource(root, "/AttachSourceTests/fullsrc.zip", "invalid");

	IClassFile cf = root.getPackageFragment("p1.p2").getClassFile("A.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p1.p2;\n" +
		"\n" +
		"public class A {}",
		cf.getSource());

	cf = root.getPackageFragment("").getClassFile("B.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"public class B {}",
		cf.getSource());

	cf = root.getPackageFragment("test1").getClassFile("Test.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package test1;\n" +
		"\n" +
		"public class Test {}",
		cf.getSource());

	attachSource(root, null, null); // detach source
	root.close();
}
/**
 * Attach a jar with a source attachement that is itself
 */
public void testRootPath10() throws JavaModelException {
	IJavaProject project = getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/test2.jar"));
	attachSource(root, "/AttachSourceTests/test2.jar", null);

	IClassFile cf = root.getPackageFragment("p").getClassFile("X.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p;\n" +
		"\n" +
		"public class X {\n" +
		"\n" +
		"	public static void main(String[] args) {\n" +
		"	}\n" +
		"}",
		cf.getSource());
	attachSource(root, null, null); // detach source
	root.close();
}
/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=35965
 */
public void testRootPath11() throws JavaModelException {
	IJavaProject project = getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/test4.jar"));
	attachSource(root, "/AttachSourceTests/test4_src.zip", null);

	IClassFile cf = root.getPackageFragment("P1").getClassFile("D.class");
	assertSourceEquals(
		"Unexpected source for class file P1.D",
		"package P1;\n" +
		"\n" +
		"public class D {}",
		cf.getSource());

	cf = root.getPackageFragment("P1.p2").getClassFile("A.class");
	assertSourceEquals(
		"Unexpected source for class file P1.p2.A",
		"package P1.p2;\n" +
		"\n" +
		"public class A {}",
		cf.getSource());

	assertTrue("Not a binary root", root.getKind() == IPackageFragmentRoot.K_BINARY);
	assertEquals("wrong jdk level", ClassFileConstants.JDK1_2, Util.getJdkLevel(root.getResource()));
	attachSource(root, null, null); // detach source
	root.close();
}
/**
 * Attach a jar with a source attachement that is itself. The jar contains 2 root paths for the same class file.
 * (regression test for bug 74014 prefix path for source attachements - automatic detection does not seem to work)
 */
public void testRootPath12() throws JavaModelException {
	IJavaProject project = getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/test5.jar"));
	attachSource(root, "/AttachSourceTests/test5.jar", null);

	IClassFile cf = root.getPackageFragment("p1.p2").getClassFile("X.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p1.p2;\n" +
		"public class X {\n" +
		"}\n",
		cf.getSource());
	attachSource(root, null, null); // detach source
	root.close();
}
/**
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=110172"
 */
public void testBug110172() throws JavaModelException {
	IJavaProject project = getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/test6.jar"));
	assertTrue("Root doesn't exist", root.exists());
	attachSource(root, "/AttachSourceTests/test6src.zip", null);

	try {
		// check the javadoc source range in a class file
		IClassFile cf = root.getPackageFragment("p1.p2").getClassFile("X.class");
		assertNotNull(cf);
		final String source = cf.getSource();
		assertNotNull("No source", source);
		IJavaElement[] children = cf.getChildren();
		assertEquals("Wrong number of children", 1, children.length);
		IJavaElement element = children[0];
		assertTrue("Not a type", element instanceof IType);
		IType type = (IType) element;
		IJavaElement[] members = type.getChildren();
		final int length = members.length;
		assertEquals("Wrong number", 9, length);
		for (int i = 0; i < length; i++) {
			element = members[i];
			assertTrue(element instanceof IMember);
			final ISourceRange javadocRange = ((IMember) element).getJavadocRange();
			final String elementName = element.getElementName();
			if ("f".equals(elementName)) {
				assertNotNull("No javadoc source range", javadocRange);
				final int start = javadocRange.getOffset();
				final int end = javadocRange.getLength() + start - 1;
				String javadocSource = source.substring(start, end);
				assertTrue("Wrong javadoc", javadocSource.indexOf("field f") != -1);
			} else if ("foo".equals(elementName)) {
				assertNotNull("No javadoc source range", javadocRange);
				final int start = javadocRange.getOffset();
				final int end = javadocRange.getLength() + start - 1;
				String javadocSource = source.substring(start, end);
				assertTrue("Wrong javadoc", javadocSource.indexOf("method foo") != -1);
			} else if ("A".equals(elementName)) {
				assertNotNull("No javadoc source range", javadocRange);
				final int start = javadocRange.getOffset();
				final int end = javadocRange.getLength() + start - 1;
				String javadocSource = source.substring(start, end);
				assertTrue("Wrong javadoc", javadocSource.indexOf("member type A") != -1);
			} else if ("X".equals(elementName)) {
				// need treatment for the two constructors
				assertTrue("Not an IMethod", element instanceof IMethod);
				IMethod method = (IMethod) element;
				switch(method.getNumberOfParameters()) {
					case 0 :
						assertNull("Has a javadoc source range", javadocRange);
						break;
					case 1:
						assertNotNull("No javadoc source range", javadocRange);
						final int start = javadocRange.getOffset();
						final int end = javadocRange.getLength() + start - 1;
						String javadocSource = source.substring(start, end);
						assertTrue("Wrong javadoc", javadocSource.indexOf("constructor") != -1);
				}
			} else if ("f3".equals(elementName)) {
				assertNotNull("No javadoc source range", javadocRange);
				final int start = javadocRange.getOffset();
				final int end = javadocRange.getLength() + start - 1;
				String javadocSource = source.substring(start, end);
				assertTrue("Wrong javadoc", javadocSource.indexOf("Real") != -1);
			} else if ("f2".equals(elementName)) {
				assertNull("Has a javadoc source range", javadocRange);
			} else if ("foo2".equals(elementName)) {
				assertNull("Has a javadoc source range", javadocRange);
			} else if ("B".equals(elementName)) {
				assertNull("Has a javadoc source range", javadocRange);
			}
		}
	} finally {
		attachSource(root, null, null); // detach source
		root.close();
	}
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=242029
 */
public void testRootPath13() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(this.getFile("/AttachSourceTests/test7.jar"));
	attachSource(root, "/AttachSourceTests/test7src/", null);
	
	IClassFile cf = root.getPackageFragment("p1.p2").getClassFile("X.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p1.p2;\n" +
		"public class X {\n" +
		"}",
		cf.getSource());
	cf = root.getPackageFragment("tests.p1").getClassFile("TestforX.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package tests.p1;\n" +
		"public class TestforX {\n" +
		"}",
		cf.getSource());
	attachSource(root, null, null); // detach source
	root.close();
}
/**
 * @test bug 153133: [model] toggle breakpoint in constructor creates a class load breakpoint
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=153133"
 */
public void testBug153133() throws JavaModelException {
	IPackageFragmentRoot root = this.currentProject.getPackageFragmentRoot(getFile("/AttachSourceTests/b153133.jar"));
	assertTrue("Root doesn't exist", root.exists());

	try {
		// Get class file type from jar
		IClassFile cf = root.getPackageFragment("test").getClassFile("Test.class");
		assertNotNull(cf);
		final String source = cf.getSource();
		assertNotNull("No source", source);
		IJavaElement[] children = cf.getChildren();
		assertEquals("Wrong number of children", 1, children.length);
		IJavaElement element = children[0];
		assertTrue("Not a type", element instanceof IType);
		IType type = (IType) element;
		IJavaElement[] members = type.getChildren();
		final int length = members.length;
		assertEquals("Wrong number", 7, length);

		// Need to get type members constructors
		for (int i = 0; i < length; i++) {
			assertTrue(members[i] instanceof IMember);
			if (((IMember)members[i]).getElementType() == IJavaElement.TYPE) {
				IType typeMember = (IType) members[i];
				String typeName = typeMember.getElementName();
				IMethod[] methods = typeMember.getMethods();
				assertEquals("Expected only one constructor defined in type "+typeName, 1, methods.length);
				// Verify that source range is valid
				assertTrue("Expected a constructor instead of a method in type "+typeName, methods[0].isConstructor());
				IMethod constructor = methods[0];
				ISourceRange sourceRange = constructor.getSourceRange();
				assertTrue("Constructor "+constructor.getElementName()+" has invalid offset: "+sourceRange, sourceRange.getOffset() >= 0);
				assertTrue("Constructor "+constructor.getElementName()+" has invalid length: "+sourceRange, sourceRange.getLength() > 0);
			}
		}
	} finally {
		removeClasspathEntry(new Path("/JavaSearchBugs/lib/b148215.jar"));
		root.close();
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=267046
public void test267046() throws JavaModelException {
	IJavaProject project = getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/267046.jar"));
	attachSource(root, "/AttachSourceTests/267046_src.zip", null);

	IClassFile cf = root.getPackageFragment("test").getClassFile("Foo.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package test;\n" + 
		"\n" + 
		"public class Foo {\n" + 
		"	public static class Bar<A, B> {\n" + 
		"	}\n" + 
		"\n" + 
		"	public static void gotchaFunc1(Bar<byte[], Object> bar) {\n" + 
		"	}\n" + 
		"	public static void gotchaFunc2(Bar<boolean[], Object> bar) {\n" + 
		"	}\n" + 
		"	public static void gotchaFunc3(Bar<char[], Object> bar) {\n" + 
		"	}\n" + 
		"	public static void gotchaFunc4(Bar<double[], Object> bar) {\n" + 
		"	}\n" + 
		"	public static void gotchaFunc5(Bar<float[], Object> bar) {\n" + 
		"	}\n" + 
		"	public static void gotchaFunc6(Bar<int[], Object> bar) {\n" + 
		"	}\n" + 
		"	public static void gotchaFunc7(Bar<long[], Object> bar) {\n" + 
		"	}\n" + 
		"	public static void gotchaFunc8(Bar<short[], Object> bar) {\n" + 
		"	}\n" + 
		"}\n",
		cf.getSource());
	IType type = cf.getType();
	IMethod[] methods = type.getMethods();
	for (int i = 0, max = methods.length; i < max; i++) {
		IMethod currentMethod = methods[i];
		if (currentMethod.isConstructor()) continue;
		assertNotNull("No source for method : " + currentMethod.getElementName(), currentMethod.getSource());
	}

	attachSource(root, null, null); // detach source
	root.close();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88265
// Test to ensure availability and correctness of API SourceRange
public void test88265 () {
	org.eclipse.jdt.core.SourceRange one = new org.eclipse.jdt.core.SourceRange(10, 7);
	org.eclipse.jdt.core.SourceRange two = new org.eclipse.jdt.core.SourceRange(9, 13);
	assertTrue(two.getOffset() == 9);
	assertTrue(two.getLength() == 13);
	assertFalse(one.equals(two));
	SourceRange three = new org.eclipse.jdt.core.SourceRange(10, 7);
	assertTrue(one.equals(three));
	assertTrue(SourceRange.isAvailable(one));
	assertFalse(SourceRange.isAvailable(null));
	assertFalse(SourceRange.isAvailable(new SourceRange(-1, 0)));
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=285230
 */
public void testClassFileBuffer() throws JavaModelException {
	IClassFile classFile = this.innerClasses.getClassFile("X$V.class");
	IBuffer buffer = classFile.getBuffer();
	classFile = this.innerClasses.getClassFile("X.class");
	IBuffer buffer2 = classFile.getBuffer();
	assertTrue("Same buffer is not reused", buffer2 == buffer);
}
}
