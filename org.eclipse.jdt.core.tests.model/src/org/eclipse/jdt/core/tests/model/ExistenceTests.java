/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;

public class ExistenceTests extends ModifyingResourceTests {
public ExistenceTests(String name) {
	super(name);
}



public static Test suite() {
	return new Suite(ExistenceTests.class);
}
protected void assertCorrespondingResourceFails(IJavaElement element) {
	boolean gotException = false;
	try {
		element.getCorrespondingResource();
	} catch (JavaModelException e) {
		if (e.isDoesNotExist()) {
			gotException = true;
		}
	}
	assertTrue("Should not be able to get corresponding resource", gotException);
}
protected void assertOpenFails(IOpenable openable) {
	boolean gotException = false;
	try {
		openable.open(null);
	} catch (JavaModelException e) {
		if (e.isDoesNotExist()) {
			gotException = true;
		}
	}
	assertTrue("Should not be able to open element", gotException);
}
protected void assertUnderlyingResourceFails(IJavaElement element) {
	boolean gotException = false;
	try {
		element.getUnderlyingResource();
	} catch (JavaModelException e) {
		if (e.isDoesNotExist()) {
			gotException = true;
		}
	}
	assertTrue("Should not be able to get underlying resource", gotException);
}
public void testClassFileInBinary() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		this.createFile("P/bin/X.class", "");
		IClassFile classFile = this.getClassFile("P/bin/X.class");
		assertTrue(!classFile.exists());
	} finally {
		this.deleteProject("P");
	}
}
public void testClassFileInLibrary() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {}, new String[] {"lib"}, "bin");
		this.createFile("P/lib/X.class", "");
		IClassFile classFile = this.getClassFile("P/lib/X.class");
		assertTrue(classFile.exists());
	} finally {
		this.deleteProject("P");
	}
}
public void testClassFileInSource() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		this.createFile("P/src/X.class", "");
		IClassFile classFile = this.getClassFile("P/src/X.class");
		// for now, we don't check the kind (source or library), 
		// so class file can exist in source folder
		assertTrue("Class file should exist", classFile.exists()); 
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensure that a non-existing class file cannot be opened.
 */
public void testNonExistingClassFile() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, new String[] {"lib"}, "bin");
		IClassFile classFile = getClassFile("/P/lib/X.class");
		assertOpenFails(classFile);
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensure that a non-existing compilation unit cannot be opened.
 */
public void testNonExistingCompilationUnit() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		ICompilationUnit cu = getCompilationUnit("/P/src/X.java");
		assertOpenFails(cu);
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensure that a non-existing package fragment cannot be opened.
 */
public void testNonExistingPackageFragment() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		IPackageFragment pkg = this.getPackage("/P/src/x");
		assertOpenFails(pkg);
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that a package fragment root that is not on the classpath cannot be opened. */
public void testPkgFragmentRootNotInClasspath() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {"src"}, "bin");
		IFolder folder = this.createFolder("/P/otherRoot");
		IPackageFragmentRoot root = project.getPackageFragmentRoot(folder);
		assertTrue("Root should not exist", !root.exists());
		boolean gotException = false;
		try {
			root.open(null);
		} catch (JavaModelException e) {
			if (e.isDoesNotExist()) {
				gotException = true;
			}
		}
		assertTrue("Should not be able to open root", gotException);
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that one cannot get the corresponding resource of a non-existing class file.
 */
public void testCorrespondingResourceNonExistingClassFile() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, new String[] {"lib"}, "bin");
		IClassFile classFile = this.getClassFile("/P/lib/X.class");
		assertCorrespondingResourceFails(classFile);
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that one cannot get the corresponding resource of a non-existing compilation unit.
 */
public void testCorrespondingResourceNonExistingCompilationUnit() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		/* TODO: Re-enable when we don't allow to open non existing cus
		ICompilationUnit compilationUnit = this.getCompilationUnit("/P/src/X.java");
		assertCorrespondingResourceFails(compilationUnit);
		*/
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that one cannot get the corresponding resource of a non-existing jar package fragment root.
 */
public void testCorrespondingResourceNonExistingJarPkgFragmentRoot() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {"src"}, "bin");
		IPackageFragmentRoot root = project.getPackageFragmentRoot("/nonExisting.jar");
		assertCorrespondingResourceFails(root);
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that one cannot get the corresponding resource of a non-existing package fragment.
 */
public void testCorrespondingResourceNonExistingPkgFragment() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		IPackageFragment pkg = this.getPackage("/P/src/nonExisting");
		assertCorrespondingResourceFails(pkg);
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that one cannot get the corresponding resource of a non-existing package fragment root.
 */
public void testCorrespondingResourceNonExistingPkgFragmentRoot() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {"src"}, "bin");
		IFolder folder = this.createFolder("/P/nonExistingRoot");
		IPackageFragmentRoot root = project.getPackageFragmentRoot(folder);
		assertCorrespondingResourceFails(root);
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that one cannot get the corresponding resource of a non-existing java project.
 */
public void testCorrespondingResourceNonExistingProject() throws CoreException {
	IProject nonExistingProject = ResourcesPlugin.getWorkspace().getRoot().getProject("NonExisting");
	IJavaProject javaProject = JavaCore.create(nonExistingProject);
	assertCorrespondingResourceFails(javaProject);
}
/*
 * Ensures that one cannot get the corresponding resource of a non-existing type.
 */
public void testCorrespondingResourceNonExistingType() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		this.createFile(
			"/P/src/X.java",
			"public class X{\n" +			"}"
		);
		IType type = getCompilationUnit("/P/src/X.java").getType("NonExisting");
		assertCorrespondingResourceFails(type);
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that one cannot get the underlying resource of a non-existing class file.
 */
public void testUnderlyingResourceNonExistingClassFile() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, new String[] {"lib"}, "bin");
		IClassFile classFile = this.getClassFile("/P/lib/X.class");
		assertUnderlyingResourceFails(classFile);
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that one cannot get the underlying resource of a non-existing compilation unit.
 */
public void testUnderlyingResourceNonExistingCompilationUnit() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		/* TODO: Re-enable when we don't allow to open non existing cus
		ICompilationUnit compilationUnit = this.getCompilationUnit("/P/src/X.java");
		assertUnderlyingResourceFails(compilationUnit);
		*/
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that one cannot get the underlying resource of a non-existing jar package fragment root.
 */
public void testUnderlyingResourceNonExistingJarPkgFragmentRoot() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {"src"}, "bin");
		IPackageFragmentRoot root = project.getPackageFragmentRoot("/nonExisting.jar");
		assertUnderlyingResourceFails(root);
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that one cannot get the underlying resource of a non-existing package fragment.
 */
public void testUnderlyingResourceNonExistingPkgFragment() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		IPackageFragment pkg = this.getPackage("/P/src/nonExisting");
		assertUnderlyingResourceFails(pkg);
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that one cannot get the underlying resource of a non-existing package fragment root.
 */
public void testUnderlyingResourceNonExistingPkgFragmentRoot() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {"src"}, "bin");
		IFolder folder = this.createFolder("/P/nonExistingRoot");
		IPackageFragmentRoot root = project.getPackageFragmentRoot(folder);
		assertUnderlyingResourceFails(root);
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that one cannot get the underlying resource of a non-existing java project.
 */
public void testUnderlyingResourceNonExistingProject() throws CoreException {
	IProject nonExistingProject = ResourcesPlugin.getWorkspace().getRoot().getProject("NonExisting");
	IJavaProject javaProject = JavaCore.create(nonExistingProject);
	assertUnderlyingResourceFails(javaProject);
}
/*
 * Ensures that one cannot get the underlying resource of a non-existing type.
 */
public void testUnderlyingResourceNonExistingType() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		this.createFile(
			"/P/src/X.java",
			"public class X{\n" +
			"}"
		);
		IType type = getCompilationUnit("/P/src/X.java").getType("NonExisting");
		assertUnderlyingResourceFails(type);
	} finally {
		this.deleteProject("P");
	}
}
}