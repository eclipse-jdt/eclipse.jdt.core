/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Hashtable;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IModularClassFile;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IOrdinaryClassFile;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.ExternalFoldersManager;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.util.Util;

import junit.framework.Test;

/**
 * TO DO:
 * - source attachment on external jar.
 * - don't use assertTrue where assertEquals should be used
 * - don't hardcode positions
*/
public class AttachSourceTests extends ModifyingResourceTests {
	static {
//		TESTS_NAMES = new String[] { "testConstructorAccess" };
//		TESTS_NUMBERS = new int[] { 5 };
//		TESTS_RANGE = new int[] { 169, 180 };
	}

	public static Test suite() {
		String javaVersion = System.getProperty("java.version");
		if (javaVersion.length() > 3) {
			javaVersion = javaVersion.substring(0, 3);
		}
		long jdkLevel = CompilerOptions.versionToJdkLevel(javaVersion);
		if (jdkLevel >= ClassFileConstants.JDK9) {
			isJRE9 = true;
		}
		return buildModelTestSuite(AttachSourceTests.class);
	}

	/** @deprecated using deprecated code */
	private static final int AST_INTERNAL_JLS2 = AST.JLS2;

	private IPackageFragmentRoot pkgFragmentRoot;

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
@Override
protected void setUp() throws Exception {
	super.setUp();
	attachSource(this.pkgFragmentRoot, "/AttachSourceTests/attachsrc.zip", "");
}
/**
 * Create project and set the jar placeholder.
 */
@Override
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
		"}",
		"Container.java",
		"public class Container {\n" +
		"	class Inner<S> {\n" +
		"		Inner(String st, Class<S> s) {\n" +
		"			super();\n" +
		"		}\n" +
		"	}\n" +
		"}"
	};
	addLibrary("generic.jar", "genericsrc.zip", pathAndContents, JavaCore.VERSION_1_5);
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
		"    class WW {\n" +
		"      WW() {}\n" +
		"      class WWW {\n" +
		"        WWW() {}\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}"
	};
	addLibrary("innerClasses.jar", "innerClassessrc.zip", pathAndContents, JavaCore.VERSION_1_4);
}
@Override
protected void tearDown() throws Exception {
	IPackageFragmentRoot[] roots = this.currentProject.getAllPackageFragmentRoots();
	for (int i = 0; i < roots.length; i++) {
		IPackageFragmentRoot root = roots[i];
		if (root.getKind() == IPackageFragmentRoot.K_BINARY) {
			attachSource(root, null, null); // detach source
		}
	}
	super.tearDown();
}
/**
 * Reset the jar placeholder and delete project.
 */
@Override
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
	IClassFile classFile = this.pkgFragmentRoot.getPackageFragment("x.y").getOrdinaryClassFile("A.class");
	ASTNode node = runConversion(classFile, true);
	assertNotNull("No node", node);
	attachSource(this.pkgFragmentRoot, null, null);
	IOrdinaryClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getOrdinaryClassFile("A.class");
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
	IClassFile classFile = this.pkgFragmentRoot.getPackageFragment("x.y").getOrdinaryClassFile("A.class");
	ASTNode node = runConversion(classFile, false);
	assertNotNull("No node", node);
	attachSource(this.pkgFragmentRoot, null, null);
	IOrdinaryClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getOrdinaryClassFile("A.class");
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
	IOrdinaryClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getOrdinaryClassFile("A.class");
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
	((JavaProject)this.currentProject).resetResolvedClasspath();
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
	IClassFile classFile = this.pkgFragmentRoot.getPackageFragment("x.y").getOrdinaryClassFile("A.class");
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
	IClassFile classFile = this.pkgFragmentRoot.getPackageFragment("x.y").getOrdinaryClassFile("A.class");
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
	IClassFile classFile = this.pkgFragmentRoot.getPackageFragment("x.y").getOrdinaryClassFile("A.class");
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
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/innerClasses.jar"));
	attachSource(root, "/AttachSourceTests/innerClassessrc.zip", null);
	IPackageFragment fragment = root.getPackageFragment("inner");

	IClassFile classFile = fragment.getOrdinaryClassFile("X$V.class");
	String source = classFile.getSource();
	IJavaElement element = classFile.getElementAt(source.indexOf("V(String s)"));
	assertElementExists(
		"Unexpected element",
		"V(inner.X, java.lang.String) [in V [in X$V.class [in inner [in innerClasses.jar [in AttachSourceTests]]]]]",
		element);
	attachSource(root, null, null); // detach source
}
/*
 * Ensures that the source of a .class file is implicetely attached when prj=src=bin
 * (regression test for bug 41444 [navigation] error dialog on opening class file)
 *
 * Note: The test case is being modified as part of fix for bug
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=398490
 */
public void testClassFileInOutput() throws CoreException {
	IClassFile classFile = getClassFile("AttachSourceTests/src/A.class"); // the file content is not used
	IResource resource = classFile.getResource();
	assertTrue(resource.exists());
	String source = classFile.getSource();
	assertNull("Unexpected source", source);
}
/**
 * Retrieves the source code for "A.class", which is
 * the entire CU for "A.java".
 */
public void testClassRetrieval() throws JavaModelException {
	IClassFile objectCF = this.pkgFragmentRoot.getPackageFragment("x.y").getOrdinaryClassFile("A.class");
	assertTrue("source code does not exist for the entire attached compilation unit", objectCF.getSource() != null);
}
/**
 * Removes the source attachment from the jar.
 */
public void testDetachSource() throws JavaModelException {
	attachSource(this.pkgFragmentRoot, null, null);
	IOrdinaryClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getOrdinaryClassFile("A.class");
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
		IType type = root.getPackageFragment("p").getOrdinaryClassFile("X.class").getType();
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
		IType type = root.getPackageFragment("p").getOrdinaryClassFile("X.class").getType();
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
		IType type = root.getPackageFragment("p").getOrdinaryClassFile("X.class").getType();
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
		IType type = root.getPackageFragment("p").getOrdinaryClassFile("X.class").getType();
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
		IJavaProject javaProject = null;
		try {
			javaProject = createJavaProject("P", new String[0], new String[] {externalLib}, "");
		}
		catch (Exception e) {
			IFolder folder = getFolder(externalLib);
			System.out.println("----------  This information is logged for debugging purposes as this test fails sporadically.---------");
			System.out.println("Failing when creating Link folder for: " + externalFolder);
			System.out.println("Existing? " + folder.exists());
			IProject externalFolderProject = JavaModelManager.getExternalManager().getExternalFoldersProject();
			IFile externalProjectFile = externalFolderProject.getFile(".project");
			if (externalProjectFile.exists()) {
				System.out.println("External Folder Project exists with following content:");
				BufferedInputStream bs = new BufferedInputStream(externalProjectFile.getContents());
				int available = 0;
				while ((available = bs.available()) > 0) {
					byte[] contents = new byte[available];
					bs.read(contents);
					System.out.println(new String(contents));
				}
				bs.close();
			}
			else {
				System.out.println("External folders project doesn't exist.");
			}
			System.out.println("----------  Debug information ends ---------");
			throw e;
		}
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(externalLib);
		attachSource(root, externalFolder + "/src228639", "");
		IType type = root.getPackageFragment("p").getOrdinaryClassFile("X.class").getType();
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
		IType type = root.getPackageFragment("p").getOrdinaryClassFile("X.class").getType();
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
		IType type = root.getPackageFragment("p").getOrdinaryClassFile("X.class").getType();
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
		IType type = root.getPackageFragment("p").getOrdinaryClassFile("X.class").getType();
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
		IType type = root.getPackageFragment("p").getOrdinaryClassFile("X.class").getType();
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
 * Test that a source path must have at least one segment. Set source path
 * to the current workspace location root. The test runs on Windows only, so
 * the path is usually "C:\" or "D:\".
 */
public void test264301() throws CoreException {
	String os = Platform.getOS();
	if (!Platform.OS_WIN32.equals(os)) {
		return;
	}

	try {
		IJavaProject javaProject = createJavaProject("Test", new String[]{""}, new String[]{"/AttachSourceTests/test.jar"}, "");
		createFolder("/Test/test1");
		createFile("/Test/test1/Test.java",
			"package test1;\n" +
			"\n" +
			"public class Test {}");
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(getFile("/AttachSourceTests/test.jar"));
		String dev = ResourcesPlugin.getWorkspace().getRoot().getLocation().getDevice() + IPath.SEPARATOR;
		try {
			attachSource(root, dev, null);
			assertTrue("Should not be here", false);
		} catch(JavaModelException e) {
			// expected exception when source path overlaps with the current workspace location
		}
	} finally {
		deleteProject("Test");
	}
}
/*
 * Ensures that the source of a generic method can be retrieved.
 */
public void testGeneric1() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/generic.jar"));
	attachSource(root, "/AttachSourceTests/genericsrc.zip", null);
	IType type = root.getPackageFragment("generic").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo", new String[] {"QX<QT;>;"});
	assertSourceEquals(
		"Unexpected source",
		"void foo(X<T> x) {\n" +
		"  }",
		method.getSource());
	attachSource(root, null, null); // detach source
}
/*
 * Ensures that the source of a generic method can be retrieved.
 */
public void testGeneric2() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/generic.jar"));
	attachSource(root, "/AttachSourceTests/genericsrc.zip", null);
	IType type = root.getPackageFragment("generic").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo", new String[] {"QK;", "QV;"});
	assertSourceEquals(
		"Unexpected source",
		"<K, V> V foo(K key, V value) {\n" +
		"    return value;\n" +
		"  }",
		method.getSource());
	attachSource(root, null, null); // detach source
}
/*
 * Ensures that the source of a generic method can be retrieved.
 * (regression test for bug 129317 Outline view inconsistent with code
 */
public void testGeneric3() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/generic.jar"));
	attachSource(root, "/AttachSourceTests/genericsrc.zip", null);
	IType type = root.getPackageFragment("generic").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo", new String[] {"I", "Lgeneric.X<[Ljava.lang.Object;>;"});
	assertSourceEquals(
		"Unexpected source",
		"void foo(int i, X<Object[]> x) {\n" +
		"  }",
		method.getSource());
	attachSource(root, null, null); // detach source
}
public void testGeneric4() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/generic.jar"));
	attachSource(root, "/AttachSourceTests/genericsrc.zip", null);
	IType type = root.getPackageFragment("generic").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo", new String[] {"Z", "Lgeneric.X<+Lgeneric.X;>;"});
	assertSourceEquals(
		"Unexpected source",
		"void foo(boolean b, X<? extends X> x) {\n" +
		"  }",
		method.getSource());
	attachSource(root, null, null); // detach source
}
public void testGeneric5() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/generic.jar"));
	attachSource(root, "/AttachSourceTests/genericsrc.zip", null);
	IType type = root.getPackageFragment("generic").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo", new String[] {"F", "Lgeneric.X<*>;"});
	assertSourceEquals(
		"Unexpected source",
		"void foo(float f, X<?> x) {\n" +
		"  }",
		method.getSource());
	attachSource(root, null, null); // detach source
}
public void testGeneric6() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/generic.jar"));
	attachSource(root, "/AttachSourceTests/genericsrc.zip", null);
	IType type = root.getPackageFragment("generic").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo", new String[] {"Lgeneric.Y<+Ljava.lang.Integer;+Ljava.lang.Object;>;"});
	assertSourceEquals(
		"Unexpected source",
		"void foo(Y<? extends Integer, ? extends Object> y) {\n" +
		"  }",
		method.getSource());
	attachSource(root, null, null); // detach source
}
public void testGeneric7() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/generic.jar"));
	attachSource(root, "/AttachSourceTests/genericsrc.zip", null);
	IType type = root.getPackageFragment("generic").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo", new String[] {"Lgeneric.Z.Inner<Ljava.lang.Object;>;"});
	assertSourceEquals(
		"Unexpected source",
		"void foo(Z.Inner<Object> inner) {\n" +
		"  }",
		method.getSource());
	attachSource(root, null, null); // detach source
}
public void testGeneric8() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/generic.jar"));
	attachSource(root, "/AttachSourceTests/genericsrc.zip", null);
	IType type = root.getPackageFragment("generic").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo", new String[] {"Lgeneric.AType<Ljava.lang.Object;>;"});
	assertSourceEquals(
		"Unexpected source",
		"void foo(AType<Object> t) {\n" +
		"  }",
		method.getSource());
	attachSource(root, null, null); // detach source
}
/**
 * Ensures that name ranges exists for BinaryMembers that have
 * mapped source.
 */
public void testGetNameRange01() throws JavaModelException {
	IOrdinaryClassFile classFile = this.pkgFragmentRoot.getPackageFragment("x.y").getOrdinaryClassFile("A.class");
	IMethod method = classFile.getType().getMethod("foo", null);
	assertSourceEquals("Unexpected name source", "foo", getNameSource(classFile.getSource(), method));
}
/**
 * Ensures that name ranges exists for BinaryMembers that have
 * mapped source.
 */
public void testGetNameRange02() throws JavaModelException {
	IOrdinaryClassFile classFile = this.pkgFragmentRoot.getPackageFragment("x.y").getOrdinaryClassFile("A.class");
	assertSourceEquals("Unexpected name source", "A", getNameSource(classFile.getSource(), classFile.getType()));
}
/*
 * Ensure that the name range for a constructor of a binary member type is correct.
 * (regression test for bug 119249 codeResolve, search, etc. don't work on constructor of binary inner class)
 */
public void testGetNameRange03() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/innerClasses.jar"));
	attachSource(root, "/AttachSourceTests/innerClassessrc.zip", null);
	IPackageFragment fragment = root.getPackageFragment("inner");

	IOrdinaryClassFile classFile = fragment.getOrdinaryClassFile("X$V.class");
	IMethod constructor = classFile.getType().getMethod("V", new String[] {"Linner.X;", "Ljava.lang.String;"});
	assertSourceEquals("Unexpected name source", "V", getNameSource(classFile.getSource(), constructor));

	attachSource(root, null, null); // detach source
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=108784
public void testGetNameRange04() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/innerClasses.jar"));
	attachSource(root, "/AttachSourceTests/innerClassessrc.zip", null);
	IPackageFragment fragment = root.getPackageFragment("inner");

	IOrdinaryClassFile classFile = fragment.getOrdinaryClassFile("X$Inner.class");
	IMethod[] methods = classFile.getType().getMethods();
	for (int i = 0; i < methods.length; i++) {
		IMethod iMethod = methods[i];
		ISourceRange nameRange = iMethod.getNameRange();
		assertTrue("Unexpected name range", nameRange.getOffset() != -1);
		assertTrue("Unexpected name range", nameRange.getLength() != 0);
	}

	attachSource(root, null, null); // detach source
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
	IOrdinaryClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getOrdinaryClassFile("A.class");
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
	IOrdinaryClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getOrdinaryClassFile("A$Inner.class");
	ISourceRange sourceRange = cf.getSourceRange();
	assertTrue("Inner class file should have associated source range", sourceRange != null);
	assertEquals("Unexpected offset", 0, sourceRange.getOffset());
	assertEquals("Unexpected length", 100, sourceRange.getLength());
}
/*
 * Ensures that the source of an inner class can be retrieved.
 */
public void testInnerClass1() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/innerClasses.jar"));
	attachSource(root, "/AttachSourceTests/innerClassessrc.zip", null);
	IPackageFragment fragment = root.getPackageFragment("inner");

	IType type = fragment.getOrdinaryClassFile("X.class").getType();
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
		"    class WW {\n" +
		"      WW() {}\n" +
		"      class WWW {\n" +
		"        WWW() {}\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}",
		type.getSource());
	attachSource(root, null, null); // detach source
}
/*
 * Ensures that the source of an inner class can be retrieved.
 */
public void testInnerClass2() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/innerClasses.jar"));
	attachSource(root, "/AttachSourceTests/innerClassessrc.zip", null);
	IPackageFragment fragment = root.getPackageFragment("inner");

	IType type = fragment.getOrdinaryClassFile("X$1.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"new X() {}",
		type.getSource());
	attachSource(root, null, null); // detach source
}
/*
 * Ensures that the source of an inner class can be retrieved.
 */
public void testInnerClass3() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/innerClasses.jar"));
	attachSource(root, "/AttachSourceTests/innerClassessrc.zip", null);
	IPackageFragment fragment = root.getPackageFragment("inner");

	IType type = fragment.getOrdinaryClassFile("X$2.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"new Y() {\n" +
		"      class Z {}\n" +
		"    }",
		type.getSource());
	attachSource(root, null, null); // detach source
}
/*
 * Ensures that the source of an inner class can be retrieved.
 */
public void testInnerClass4() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/innerClasses.jar"));
	attachSource(root, "/AttachSourceTests/innerClassessrc.zip", null);
	IPackageFragment fragment = root.getPackageFragment("inner");

	IType type = fragment.getOrdinaryClassFile("X$3.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"new W() {}",
		type.getSource());
	attachSource(root, null, null); // detach source
}
/*
 * Ensures that the source of an inner class can be retrieved.
 */
public void testInnerClass5() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/innerClasses.jar"));
	attachSource(root, "/AttachSourceTests/innerClassessrc.zip", null);
	IPackageFragment fragment = root.getPackageFragment("inner");

	IType type = fragment.getOrdinaryClassFile("X$1$Y.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"class Y {}",
		type.getSource());
	attachSource(root, null, null); // detach source
}
/*
 * Ensures that the source of an inner class can be retrieved.
 */
public void testInnerClass6() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/innerClasses.jar"));
	attachSource(root, "/AttachSourceTests/innerClassessrc.zip", null);
	IPackageFragment fragment = root.getPackageFragment("inner");

	IType type = fragment.getOrdinaryClassFile("X$1$W.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"class W {\n" +
		"      void bar() {\n" +
		"        new W() {};\n" +
		"      }\n" +
		"    }",
		type.getSource());
	attachSource(root, null, null); // detach source
}
/*
 * Ensures that the source of an inner class can be retrieved.
 */
public void testInnerClass7() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/innerClasses.jar"));
	attachSource(root, "/AttachSourceTests/innerClassessrc.zip", null);
	IPackageFragment fragment = root.getPackageFragment("inner");

	IType type = fragment.getOrdinaryClassFile("X$2$Z.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"class Z {}",
		type.getSource());
	attachSource(root, null, null); // detach source
}
/*
 * Ensures that the source of an inner class can be retrieved.
 */
public void testInnerClass8() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/innerClasses.jar"));
	attachSource(root, "/AttachSourceTests/innerClassessrc.zip", null);
	IPackageFragment fragment = root.getPackageFragment("inner");

	IType type = fragment.getOrdinaryClassFile("X$V.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"class V {\n" +
		"    V(String s) {\n" +
		"    }\n" +
		"  }",
		type.getSource());
	attachSource(root, null, null); // detach source
}
/*
 * Ensures that the source of an inner class can be retrieved.
 * (regression test for bug 124611 IAE in Signature.createCharArrayTypeSignature)
 */
public void testInnerClass9() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/innerClasses.jar"));
	attachSource(root, "/AttachSourceTests/innerClassessrc.zip", null);
	IPackageFragment fragment = root.getPackageFragment("inner");

	IType type = fragment.getOrdinaryClassFile("X$4$U.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"class U {\n" +
		"        U(String s) {\n" +
		"        }\n" +
		"      }",
		type.getSource());
	attachSource(root, null, null); // detach source
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=476304
public void testInnerClass10() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/innerClasses.jar"));
	attachSource(root, "/AttachSourceTests/innerClassessrc.zip", null);
	IPackageFragment fragment = root.getPackageFragment("inner");

	IType type = fragment.getOrdinaryClassFile("X$Inner$WW.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"class WW {\n" +
		"      WW() {}\n" +
		"      class WWW {\n" +
		"        WWW() {}\n" +
		"      }\n" +
		"    }",
		type.getSource());
	type = fragment.getOrdinaryClassFile("X$Inner$WW$WWW.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"class WWW {\n" +
		"        WWW() {}\n" +
		"      }",
		type.getSource());
	attachSource(root, null, null); // detach source
}
/**
 * Ensures that a source folder can be attached to a lib folder.
 */
public void testLibFolder() throws JavaModelException {
	IPackageFragmentRoot root = this.getPackageFragmentRoot("/AttachSourceTests/lib");
	attachSource(root, "/AttachSourceTests/srcLib", "");

	IOrdinaryClassFile cf = root.getPackageFragment("p").getOrdinaryClassFile("X.class");
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
	IOrdinaryClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getOrdinaryClassFile("A.class");
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
		IOrdinaryClassFile cf = root.getPackageFragment("p").getOrdinaryClassFile("X.class");
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
		IOrdinaryClassFile cf = root.getPackageFragment("").getOrdinaryClassFile("X.class");
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
 * Ensures that having a project as source attachment finds the source
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
		IOrdinaryClassFile cf = root.getPackageFragment("test1").getOrdinaryClassFile("Test.class");
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
		IOrdinaryClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getOrdinaryClassFile("A.class");
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

	IOrdinaryClassFile cf = root.getPackageFragment("x.y").getOrdinaryClassFile("B.class");
	assertTrue("source code does not exist for the entire attached compilation unit", cf.getSource() != null);
	root.close();
	cf = root.getPackageFragment("x.y").getOrdinaryClassFile("B.class");
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

	IOrdinaryClassFile cf = root.getPackageFragment("x.y").getOrdinaryClassFile("B.class");
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

	IOrdinaryClassFile cf = root.getPackageFragment("p").getOrdinaryClassFile("X.class");
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
 * Attach a jar with a source attachment that doesn't contain the source folders
 */
public void testRootPath4() throws JavaModelException {
	IJavaProject project = getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/test.jar"));
	attachSource(root, "/AttachSourceTests/src.zip", "invalid");

	IOrdinaryClassFile cf = root.getPackageFragment("test1").getOrdinaryClassFile("Test.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package test1;\n" +
		"\n" +
		"public class Test {}",
		cf.getSource());
	root.close();
}
/**
 * Attach a jar with a source attachment that doesn't contain the source folders
 */
public void testRootPath5() throws JavaModelException {
	IJavaProject project = getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/update.jar"));
	attachSource(root, "/AttachSourceTests/src.zip", "invalid");

	IOrdinaryClassFile cf = root.getPackageFragment("p1.p2").getOrdinaryClassFile("A.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p1.p2;\n" +
		"\n" +
		"public class A {}",
		cf.getSource());

	cf = root.getPackageFragment("").getOrdinaryClassFile("B.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"public class B {}",
		cf.getSource());

	attachSource(root, null, null); // detach source
}
/**
 * Attach a jar with a source attachment that doesn't contain the source folders
 */
public void testRootPath6() throws JavaModelException {
	IJavaProject project = getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/update.jar"));
	attachSource(root, "/AttachSourceTests/src.zip", null);

	IOrdinaryClassFile cf = root.getPackageFragment("p1.p2").getOrdinaryClassFile("A.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p1.p2;\n" +
		"\n" +
		"public class A {}",
		cf.getSource());

	cf = root.getPackageFragment("").getOrdinaryClassFile("B.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"public class B {}",
		cf.getSource());

	attachSource(root, null, null); // detach source
}
/**
 * Attach a jar with a source attachment that doesn't contain the source folders
 */
public void testRootPath7() throws JavaModelException {
	IJavaProject project = getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/full.jar"));
	attachSource(root, "/AttachSourceTests/src.zip", null);

	IOrdinaryClassFile cf = root.getPackageFragment("p1.p2").getOrdinaryClassFile("A.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p1.p2;\n" +
		"\n" +
		"public class A {}",
		cf.getSource());

	cf = root.getPackageFragment("").getOrdinaryClassFile("B.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"public class B {}",
		cf.getSource());

	cf = root.getPackageFragment("test1").getOrdinaryClassFile("Test.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package test1;\n" +
		"\n" +
		"public class Test {}",
		cf.getSource());

	attachSource(root, null, null); // detach source
}
/**
 * Attach a jar with a source attachment that contains the source folders
 */
public void testRootPath8() throws JavaModelException {
	IJavaProject project = getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/full.jar"));
	attachSource(root, "/AttachSourceTests/fullsrc.zip", null);

	IOrdinaryClassFile cf = root.getPackageFragment("p1.p2").getOrdinaryClassFile("A.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p1.p2;\n" +
		"\n" +
		"public class A {}",
		cf.getSource());

	cf = root.getPackageFragment("").getOrdinaryClassFile("B.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"public class B {}",
		cf.getSource());

	cf = root.getPackageFragment("test1").getOrdinaryClassFile("Test.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package test1;\n" +
		"\n" +
		"public class Test {}",
		cf.getSource());

	attachSource(root, null, null); // detach source
}
/**
 * Attach a jar with a source attachment that contains the source folders
 */
public void testRootPath9() throws JavaModelException {
	IJavaProject project = getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/full.jar"));
	attachSource(root, "/AttachSourceTests/fullsrc.zip", "invalid");

	IOrdinaryClassFile cf = root.getPackageFragment("p1.p2").getOrdinaryClassFile("A.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p1.p2;\n" +
		"\n" +
		"public class A {}",
		cf.getSource());

	cf = root.getPackageFragment("").getOrdinaryClassFile("B.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"public class B {}",
		cf.getSource());

	cf = root.getPackageFragment("test1").getOrdinaryClassFile("Test.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package test1;\n" +
		"\n" +
		"public class Test {}",
		cf.getSource());

	attachSource(root, null, null); // detach source
}
/**
 * Attach a jar with a source attachment that is itself
 */
public void testRootPath10() throws JavaModelException {
	IJavaProject project = getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/test2.jar"));
	attachSource(root, "/AttachSourceTests/test2.jar", null);

	IOrdinaryClassFile cf = root.getPackageFragment("p").getOrdinaryClassFile("X.class");
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
}
/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=35965
 */
public void testRootPath11() throws JavaModelException {
	IJavaProject project = getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/test4.jar"));
	attachSource(root, "/AttachSourceTests/test4_src.zip", null);

	IOrdinaryClassFile cf = root.getPackageFragment("P1").getOrdinaryClassFile("D.class");
	assertSourceEquals(
		"Unexpected source for class file P1.D",
		"package P1;\n" +
		"\n" +
		"public class D {}",
		cf.getSource());

	cf = root.getPackageFragment("P1.p2").getOrdinaryClassFile("A.class");
	assertSourceEquals(
		"Unexpected source for class file P1.p2.A",
		"package P1.p2;\n" +
		"\n" +
		"public class A {}",
		cf.getSource());

	assertTrue("Not a binary root", root.getKind() == IPackageFragmentRoot.K_BINARY);
	assertEquals("wrong jdk level", ClassFileConstants.JDK1_2, Util.getJdkLevel(root.getResource()));
	attachSource(root, null, null); // detach source
}
/**
 * Attach a jar with a source attachment that is itself. The jar contains 2 root paths for the same class file.
 * (regression test for bug 74014 prefix path for source attachments - automatic detection does not seem to work)
 */
public void testRootPath12() throws JavaModelException {
	IJavaProject project = getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/test5.jar"));
	attachSource(root, "/AttachSourceTests/test5.jar", null);

	IOrdinaryClassFile cf = root.getPackageFragment("p1.p2").getOrdinaryClassFile("X.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p1.p2;\n" +
		"public class X {\n" +
		"}\n",
		cf.getSource());
	attachSource(root, null, null); // detach source
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
		IOrdinaryClassFile cf = root.getPackageFragment("p1.p2").getOrdinaryClassFile("X.class");
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
	}
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=242029
 */
public void testRootPath13() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(this.getFile("/AttachSourceTests/test7.jar"));
	attachSource(root, "/AttachSourceTests/test7src/", null);

	IOrdinaryClassFile cf = root.getPackageFragment("p1.p2").getOrdinaryClassFile("X.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p1.p2;\n" +
		"public class X {\n" +
		"}",
		cf.getSource());
	cf = root.getPackageFragment("tests.p1").getOrdinaryClassFile("TestforX.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package tests.p1;\n" +
		"public class TestforX {\n" +
		"}",
		cf.getSource());
	attachSource(root, null, null); // detach source
}
/**
 * bug 153133: [model] toggle breakpoint in constructor creates a class load breakpoint
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=153133"
 */
public void testBug153133() throws JavaModelException {
	IPackageFragmentRoot root = this.currentProject.getPackageFragmentRoot(getFile("/AttachSourceTests/b153133.jar"));
	assertTrue("Root doesn't exist", root.exists());

	try {
		// Get class file type from jar
		IOrdinaryClassFile cf = root.getPackageFragment("test").getOrdinaryClassFile("Test.class");
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
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=267046
public void test267046() throws JavaModelException {
	IJavaProject project = getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/267046.jar"));
	attachSource(root, "/AttachSourceTests/267046_src.zip", null);

	IOrdinaryClassFile cf = root.getPackageFragment("test").getOrdinaryClassFile("Foo.class");
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
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/innerClasses.jar"));
	attachSource(root, "/AttachSourceTests/innerClassessrc.zip", null);
	IPackageFragment fragment = root.getPackageFragment("inner");

	IClassFile classFile = fragment.getOrdinaryClassFile("X$V.class");
	IBuffer buffer = classFile.getBuffer();
	classFile = fragment.getOrdinaryClassFile("X.class");
	IBuffer buffer2 = classFile.getBuffer();
	assertTrue("Same buffer is not reused", buffer2 == buffer);
	attachSource(root, null, null); // detach source
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=242029
 */
public void testConstructorAccess() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/AttachSourceTests/generic.jar"));
	attachSource(root, "/AttachSourceTests/genericsrc.zip", null);

	IOrdinaryClassFile cf = root.getPackageFragment("").getOrdinaryClassFile("Container$Inner.class");
	final IType type = cf.getType();
	final IMethod[] methods = type.getMethods();
	assertEquals("wrong size", 1, methods.length);
	assertTrue("Not a constructor", methods[0].isConstructor());
	assertSourceEquals(
		"Unexpected source for generic constructor",
		"Inner(String st, Class<S> s) {\n" +
		"			super();\n" +
		"		}",
		methods[0].getSource());
	attachSource(root, null, null); // detach source
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=336046
 */
public void testBug336046() throws Exception {
	String externalSourceLocation = getExternalFolder() + File.separator + "336046src";
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	Hashtable<String, String> javaCoreOptions = JavaCore.getOptions();
	IJavaProject importedProject = null;
	try {

		String classpathContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<classpath>\n" +
		"    <classpathentry kind=\"lib\" path=\"attach.jar\"/>\n" +
		"    <classpathentry kind=\"lib\" path=\"attach2.jar\"/>\n" +
		"    <classpathentry kind=\"lib\" path=\"test.jar\"/>\n" +
		"    <classpathentry kind=\"lib\" path=\"update.jar\"/>\n" +
		"    <classpathentry kind=\"lib\" path=\"full.jar\"/>\n" +
		"    <classpathentry kind=\"lib\" path=\"test2.jar\"/>\n" +
		"    <classpathentry kind=\"lib\" path=\"test4.jar\"/>  \n" +
		"    <classpathentry kind=\"lib\" path=\"test5.jar\"/>  \n" +
		"    <classpathentry kind=\"lib\" path=\"test6.jar\"/>\n" +
		"    <classpathentry kind=\"lib\" path=\"test7.jar\"/>\n" +
		"    <classpathentry kind=\"lib\" path=\"267046.jar\"/>\n" +
		"    <classpathentry kind=\"lib\" path=\"bug336046.jar\" sourcepath=\"" + externalSourceLocation + "\"/>\n" +
		"    <classpathentry kind=\"lib\" path=\"lib\"/>\n" +
		"    <classpathentry kind=\"src\" path=\"src\" output=\"src\"/>\n" +
		"    <classpathentry kind=\"var\" path=\"JCL_LIB\"/>\n" +
		"    <classpathentry kind=\"output\" path=\"bin\"/>\n" +
		"</classpath>";
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		IPath sourceLocation = project.getProject().getLocation();
		IPath destination = new Path(getExternalFolder()).append("ImportedProject");
		String classpathLocation = destination.append(".classpath").toString();
		File srcFolder = destination.append("336046src").toFile();
		copyDirectory(new File(sourceLocation.toString()), new File(destination.toString()));
		project.getProject().close(null);

		FileOutputStream fos = new FileOutputStream(classpathLocation);
		fos.write(classpathContent.getBytes());
		assertTrue(srcFolder.renameTo(new File(getExternalFolder() + File.separator + "336046src")));
		fos.close();

		IProject newProject = workspace.getRoot().getProject("ImportedProject");
		URI uri=  URIUtil.toURI(destination);
		IProjectDescription desc = workspace.newProjectDescription(newProject.getName());
		desc.setLocationURI(uri);
		newProject.create(desc, null);
		if (!newProject.isOpen()) {
			newProject.open(null);
		}
		importedProject = JavaCore.create(newProject);
		importedProject.setOptions(project.getOptions(false));

		((JavaProject)importedProject).resolveClasspath(importedProject.getRawClasspath());
		IFolder linkedFolder = ExternalFoldersManager.getExternalFoldersManager().getFolder(new Path(getExternalFolder() + File.separator + "336046src"));
		assertNotNull(linkedFolder);
	}
	finally {
		if (importedProject != null)
			importedProject.getProject().delete(true, true, null);
		project.getProject().open(null);
		JavaCore.setOptions(javaCoreOptions);
	}
}
public void testModule1() throws CoreException, IOException {
	if (!isJRE9) {
		System.err.println(this.getClass().getName()+'.'+getName()+" needs a Java 9 JRE - skipped");
		return;
	}
	try {
		IJavaProject javaProject = createJavaProject("Test", new String[]{"src"}, null, "bin", JavaCore.VERSION_9);
		createFolder("/Test/src/test1");
		createFile("/Test/src/test1/Test.java",
			"package test1;\n" +
			"\n" +
			"public class Test {}");
		createFile("/Test/src/module-info.java",
			"module test {\n" +
			"	requires mod.one;\n" +
			"	exports test1;\n" +
			"}\n");

		String modOneSrc =
			"\n" +
			"/** The no. one module. */\n" +
			"module mod.one {\n" +
			"  exports m.o.p;\n" +
			"}\n";
		String[] pathAndContents = new String[] {
			"module-info.java",
			modOneSrc,
			"m/o/p/C.java",
			"package m.o.p;\n" +
			"public class C {\n" +
			"}"
		};
		addLibrary(javaProject, "mod.one.jar", "mod.onesrc.zip", pathAndContents, JavaCore.VERSION_9);
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(getFile("/Test/mod.one.jar"));
		IModularClassFile cf = root.getPackageFragment("").getModularClassFile();
		assertSourceEquals(
			"Unexpected source for class file mod.one/module-info.class",
			modOneSrc,
			cf.getSource());
		IModuleDescription module = cf.getModule();
		ISourceRange javadocRange = module.getJavadocRange();
		String srcJavadoc = modOneSrc.substring(javadocRange.getOffset(), javadocRange.getOffset()+javadocRange.getLength());
		assertEquals("javadoc from source", "/** The no. one module. */", srcJavadoc);
		ISourceRange sourceRange = module.getSourceRange();
		assertEquals("source start", 1, sourceRange.getOffset()); // start after initial '\n'
		assertEquals("source end", modOneSrc.length()-2, sourceRange.getLength()); // end before terminal '\n'
	} finally {
		deleteProject("Test");
	}
}
public void testModule2() throws CoreException, IOException {
	if (!isJRE9) {
		System.err.println(this.getClass().getName()+'.'+getName()+" needs a Java 9 JRE - skipped");
		return;
	}
	if (isJRE12) return;
	try {
		IJavaProject prj = createJava9Project("Test", new String[]{"src"});
		String moduleSrc =
			"module test {\n" +
			"	requires oracle.net;\n" +
			"}\n";
		createFile("/Test/src/module-info.java", moduleSrc);
		prj.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = prj.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		if (markers.length == 1 && markers[0].toString().contains("oracle.net cannot be resolved to a module")) {
			System.out.println("Skipping "+getClass().getName()+".testModule2() because module oracle.net is unavailable");
			return; // oracle.net is missing from openjdk builds
		}
		ICompilationUnit unit = getCompilationUnit("/Test/src/module-info.java");
		int start = moduleSrc.indexOf("oracle.net");
		int length = "oracle.net".length();
		IJavaElement[] elements = unit.codeSelect(start, length);
		assertEquals("expected #elements", 1, elements.length);

		IModuleDescription oracleNet = (IModuleDescription) elements[0];
		IModularClassFile cf = (IModularClassFile) oracleNet.getClassFile();
		assertSourceEquals(
			"Unexpected source for class file oracle.net/module-info.class",
			null,
			cf.getSource());
		ISourceRange javadocRange = oracleNet.getJavadocRange();
		assertEquals("javadoc from source", null, javadocRange);
	} finally {
		deleteProject("Test");
	}
}
public void testModule2b() throws CoreException, IOException {
	if (!isJRE9) {
		System.err.println(this.getClass().getName()+'.'+getName()+" needs a Java 9 JRE - skipped");
		return;
	}
	try {
		// create project with incomplete source attachment:
		String javaHome = System.getProperty("java.home") + File.separator;
		Path bootModPath = new Path(javaHome +"/lib/jrt-fs.jar");
		createSourceZip(
				new String[] {
					"java.base/module-info.java",
					"module java.base {}\n",
					"java.se.ee/module-info.java",
					"module java.se.ee {}\n"
				},
				getWorkspacePath()+"/Test/src.zip");
		Path sourceAttachment = new Path("/Test/src.zip");
		IClasspathEntry jrtEntry;
		jrtEntry = JavaCore.newLibraryEntry(bootModPath, sourceAttachment, null, null, null, false);
		IJavaProject project = this.createJavaProject("Test", new String[] {"src"}, new String[0],
				new String[0], "bin", "9");
		IClasspathEntry[] old = project.getRawClasspath();
		IClasspathEntry[] newPath = new IClasspathEntry[old.length +1];
		System.arraycopy(old, 0, newPath, 0, old.length);
		newPath[old.length] = jrtEntry;
		project.setRawClasspath(newPath, null);
		//
		String moduleSrc =
			"module test {\n" +
			"	requires java.desktop;\n" +
			"}\n";
		createFile("/Test/src/module-info.java", moduleSrc);
		ICompilationUnit unit = getCompilationUnit("/Test/src/module-info.java");
		int start = moduleSrc.indexOf("java.desktop");
		int length = "java.desktop".length();
		IJavaElement[] elements = unit.codeSelect(start, length);
		assertEquals("expected #elements", 1, elements.length);

		IModuleDescription javaDesktop = (IModuleDescription) elements[0];
		IModularClassFile cf = (IModularClassFile) javaDesktop.getClassFile();
		assertSourceEquals(
			"Unexpected source for class file java.desktop/module-info.class",
			null,
			cf.getSource());
		ISourceRange javadocRange = javaDesktop.getJavadocRange();
		assertEquals("javadoc from source", null, javadocRange);
	} finally {
		deleteProject("Test");
	}
}
}
