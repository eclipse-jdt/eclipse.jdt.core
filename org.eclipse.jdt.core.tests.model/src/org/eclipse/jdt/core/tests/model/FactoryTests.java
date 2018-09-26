/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.*;
import junit.framework.Test;
public class FactoryTests extends ModifyingResourceTests {
public FactoryTests(String name) {
	super(name);
}

public static Test suite() {
	return buildModelTestSuite(FactoryTests.class);
}
/**
 * Ensures that a Java model element can be created from a IFile that
 * is a class file.
 */
public void testCreateBinaryToolObject() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {}, new String[] {"lib"}, "bin");
		IFile file = this.createFile("/P/lib/X.class", "");

		IJavaElement object = JavaCore.create(file);
		assertTrue("tooling object not created", object != null);
		assertTrue("class file does not exist", object.exists());
		assertTrue("wrong object created", object instanceof IClassFile);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Ensures that a Java model element can be created from a IFile that
 * is a Java file.  Ensures that any two model elements created will share
 * the same project.
 */
public void testCreateCompilationUnits() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		this.createFolder("/P/src/x/y/z");
		IFile fileA = this.createFile(
			"/P/src/x/y/z/A.java",
			"package x.y.z;\n" +
			"public class A {\n" +
			"}"
		);
		IFile fileB = this.createFile(
			"/P/src/x/y/B.java",
			"package x.y;\n" +
			"public class B {\n" +
			"}"
		);

		IJavaElement objectA = JavaCore.create(fileA);
		assertTrue("tooling object A not created", objectA != null);
		assertTrue("wrong object A created", objectA instanceof ICompilationUnit);
		assertTrue("compilation unit A does not exist", objectA.exists());

		IJavaElement objectB = JavaCore.create(fileB);
		assertTrue("tooling object B not created", objectB != null);
		assertTrue("wrong object B created", objectB instanceof ICompilationUnit);
		assertTrue("compilation unit B does not exist", objectB.exists());

		assertEquals("should share project", ((ICompilationUnit)objectA).getJavaProject(), ((ICompilationUnit)objectB).getJavaProject());
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Ensures that a Java model element can be created from a IFile that
 * is a Java file. Even if not on the classpath (in this case it should not exist).
 */
public void testCreateCompilationUnitsNotOnClasspath() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		this.createFolder("/P/other/nested");
		IFile fileA = this.createFile("/P/other/A.java", "public class A {}");
		IFile fileB = this.createFile("/P/other/nested/B.java", "public class B {}");
		IFile fileC = this.createFile("/P/C.java", "public class C {}");

		IJavaElement objectA = JavaCore.create(fileA);
		assertTrue("tooling object A not created", objectA != null);
		assertTrue("wrong object A created", objectA instanceof ICompilationUnit);
		assertTrue("compilation unit A should not exist", !objectA.exists());

		IJavaElement objectB = JavaCore.create(fileB);
		assertTrue("tooling object B not created", objectB != null);
		assertTrue("wrong object B created", objectB instanceof ICompilationUnit);
		assertTrue("compilation unit B should not exist", !objectB.exists());

		assertEquals("should share project", ((ICompilationUnit)objectA).getJavaProject(), ((ICompilationUnit)objectB).getJavaProject());

		IJavaElement objectC = JavaCore.create(fileC);
		assertTrue("tooling object C not created", objectC != null);
		assertTrue("wrong object C created", objectC instanceof ICompilationUnit);
		assertTrue("compilation unit C should not exist", !objectC.exists());

		IPackageFragment pkg= (IPackageFragment)objectA.getParent() ;
		IPackageFragmentRoot root= (IPackageFragmentRoot)pkg.getParent();
		assertEquals("pkg should be default", "", pkg.getElementName());
		assertEquals("unexpected parent's folder", this.getFolder("/P/other"), pkg.getResource());
		assertEquals("unexpected root", this.getFolder("/P/other"), root.getResource());
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Ensures that a Java model element can be created from a IFolder.
 * Test that no elements are created if there is no classpath.
 * Ensure that the correct Java model element is created based on the
 * classpath.
 */
public void testCreateFolderToolObjects() throws CoreException {
	try {
		IJavaProject javaProject = this.createJavaProject("P", new String[] {}, "bin");
		this.createFolder("/P/src/x/y/z");

		IFolder src =this.getFolder("/P/src");
		IFolder res = src.getFolder("x");
		IJavaElement object = JavaCore.create(res);
		assertTrue("tooling object 1 should not be created", object == null);

		//set a classpath
		IClasspathEntry[] classpath= new IClasspathEntry[] {JavaCore.newSourceEntry(src.getFullPath())};
		javaProject.setRawClasspath(classpath, null);

		//test with a class path
		object = JavaCore.create(src);
		assertTrue("tooling object 2 should be created", object != null);
		assertTrue("tooling object 2 should be a IPackageFragmentRoot", object instanceof IPackageFragmentRoot);
		assertEquals("IPackageFragmentRoot 2 name is incorrect", "src", object.getElementName());
		assertTrue("root 'src' does not exist", object.exists());

		object = JavaCore.create(res);
		assertTrue("tooling object 3 should be created", object != null);
		assertTrue("tooling object 3 should be a IPackageFragment", object instanceof IPackageFragment);
		assertEquals("IPackageFragment 3 name is incorrect", "x", object.getElementName());
		assertTrue("package 'com' does not exist", object.exists());

		IFolder subFolder= res.getFolder("y");
		object= JavaCore.create(subFolder);
		assertTrue("tooling object 'x.y' should be created", object != null);
		assertTrue("tooling object 'x.y' should be a IPackageFragment", object instanceof IPackageFragment);
		assertEquals("IPackageFragment 'x.y' name is incorrect", "x.y", object.getElementName());
		assertTrue("package 'x.y' does not exist", object.exists());

		//not on or below the class path
		IFolder bin = this.getFolder("/P/bin");
		object = JavaCore.create(bin);
		assertTrue("tooling object 4 should not be created", object == null);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Ensures that the factory correctly handles empty java files
 */
public void testCreateFromEmptyJavaFile() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		IFile file = this.createFile("/P/src/X.java", "");

		IJavaElement cu = JavaCore.create(file);
		assertTrue("does not handle empty Java files", cu != null);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Ensures that the factory correctly handles files without extensions
 */
public void testCreateFromFileWithoutExtension() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		IFile file = this.createFile("/P/src/FileWithoutExtension", "public class X {}");

		IJavaElement cu = JavaCore.create(file);
		assertTrue("invalid file not detected", cu == null);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Ensures that factory correctly handles invalid mementos.
 */
public void testCreateFromInvalidMemento()  {
	assertTrue("invalid parameter not detected", JavaCore.create((String) null) == null);
	assertTrue("should return the java model", JavaCore.create("") != null);
}
/**
 * Ensures that a Java model element can be created from a IFile
 * that is a zip or jar.
 */
public void testCreateJarToolObject() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {}, new String[] {"/P/lib.jar"}, "");
		IFile file = this.createFile("/P/lib.jar", "");

		IJavaElement jar = JavaCore.create(file);
		assertTrue("tooling object not created", jar != null);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Ensures that a Java model element can be created from a class folder library that is in the project's output.
 * (regression test for bug 25538 Conflict of classfolder and outputfolder not reported)
*/
public void testCreateLibInOutput() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {}, new String[] {"/P/lib"}, "");
		IFolder folder = this.createFolder("/P/lib");

		IJavaElement lib = JavaCore.create(folder);
		assertTrue("tooling object not created", lib != null);
	} finally {
		this.deleteProject("P");
	}
}
}
