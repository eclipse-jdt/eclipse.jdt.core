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
}