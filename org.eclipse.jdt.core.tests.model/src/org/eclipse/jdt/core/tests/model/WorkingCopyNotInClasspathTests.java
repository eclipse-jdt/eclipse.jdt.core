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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.Util;

public class WorkingCopyNotInClasspathTests extends ModifyingResourceTests {

	private ICompilationUnit workingCopy;

public WorkingCopyNotInClasspathTests(String name) {
	super(name);
}



public static Test suite() {
	return new Suite(WorkingCopyNotInClasspathTests.class);
}

public void setUp() throws Exception {
	super.setUp();
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		this.createFolder("P/txt");
		IFile file = this.createFile("P/txt/X.java",
			"public class X {\n" +
			"}");
		ICompilationUnit cu = (ICompilationUnit)JavaCore.create(file);	
		this.workingCopy = (ICompilationUnit)cu.getWorkingCopy();
	} catch (CoreException e) {
		e.printStackTrace();
	}
}

public void tearDown() throws Exception {
	try {
		if (this.workingCopy != null) {
			this.workingCopy.destroy();
			this.workingCopy = null;
		}
		this.deleteProject("P");
	} catch (CoreException e) {
		e.printStackTrace();
	}
	super.tearDown();
}

public void testCommit() throws CoreException {
	ICompilationUnit original = (ICompilationUnit)this.workingCopy.getOriginalElement();
	assertTrue("Original element should not be null", original != null);

	IBuffer workingCopyBuffer = this.workingCopy.getBuffer();
	assertTrue("Working copy buffer should not be null", workingCopyBuffer != null);

	String newContents = 
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}";
	workingCopyBuffer.setContents(newContents);
	this.workingCopy.commit(false, null);
	
	IFile originalFile = (IFile)original.getResource();
	assertSourceEquals(
		"Unexpected contents", 
		newContents, 
		new String(Util.getResourceContentsAsCharArray(originalFile)));
}

/*
 * Ensure that a working copy outside the classpath does not exist 
 * (but can still be opened).
 */
public void testExistence() throws CoreException {
	assertTrue("Working copy should exist", this.workingCopy.exists());
}
public void testGetSource() throws CoreException {
	ICompilationUnit workingCopy = null;
	try {
		this.createJavaProject("P1", new String[] {}, "bin");
		this.createFolder("/P1/src/junit/test");
		String source = 
			"package junit.test;\n" +
			"public class X {\n" +
			"}";
		IFile file = this.createFile("/P1/src/junit/test/X.java", source);
		ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
		workingCopy = (ICompilationUnit) cu.getWorkingCopy();
		assertEquals(
			"Unexpected source",
			source,
			workingCopy.getSource());
	} finally {
		if (workingCopy != null) workingCopy.destroy();
		this.deleteProject("P1");
	}
}
public void testParentExistence() throws CoreException {
	assertTrue("Working copy's parent should not exist", !this.workingCopy.getParent().exists());
}
/*
 * Ensure that a working copy created on a .java file in a simple project can be opened.
 * (regression test for bug 33748 Cannot open working copy on .java file in simple project)
 */
public void testSimpleProject() throws CoreException {
	IParent workingCopy = null;
	try {
		createProject("SimpleProject");
		IFile file = createFile(
			"/SimpleProject/X.java",
			"public class X {\n" +
			"}"
		);
		ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
		workingCopy = (IParent)cu.getWorkingCopy();
		try {
			workingCopy.getChildren();
		} catch (JavaModelException e) {
			assertTrue("Should not get JavaModelException", false);
		}
	} finally {
		if (workingCopy != null) {
			((IWorkingCopy)workingCopy).destroy();
		}
		deleteProject("SimpleProject");
	}
}

/*
 * Ensure that a original cu (which is outside the classpath) does not exist.
 */
public void testOriginalExistence() throws CoreException {
	ICompilationUnit original = (ICompilationUnit)this.workingCopy.getOriginalElement();
	if (CompilationUnit.FIX_BUG25184) {
		assertTrue(
			"Original compilation unit should not exist", 
			!original.exists());
	} else {
		assertTrue(
			"Original compilation unit should exist", 
			original.exists());
	}
}
public void testOriginalParentExistence() throws CoreException {
	assertTrue(
		"Original compilation unit's parent should not exist", 
		!this.workingCopy.getOriginalElement().getParent().exists());
}

public void testIsOpen() throws CoreException {
	assertTrue("Working copy should be open", this.workingCopy.isOpen());
}
/*
 * Ensure that a original cu (which is outside the classpath) is not opened.
 */
public void testOriginalIsOpen() throws CoreException {
	ICompilationUnit original = (ICompilationUnit)this.workingCopy.getOriginalElement();
	if (CompilationUnit.FIX_BUG25184) {
		assertTrue(
			"Original compilation should not be opened", 
			!original.isOpen());
	} else {
		assertTrue(
			"Original compilation should be opened", 
			original.isOpen());
	}
}
// 31799 - asking project options on non-Java project populates the perProjectInfo cache incorrectly
public void testIsOnClasspath() throws CoreException {
	ICompilationUnit workingCopy = null;
	try {
		this.createProject("SimpleProject");
		this.createFolder("/SimpleProject/src/junit/test");
		String source = 
			"package junit.test;\n" +
			"public class X {\n" +
			"}";
		IFile file = this.createFile("/SimpleProject/src/junit/test/X.java", source);
		ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
		workingCopy = (ICompilationUnit) cu.getWorkingCopy();
		
		// working creation will cause it to open, and thus request project options
		boolean isOnClasspath = workingCopy.getJavaProject().isOnClasspath(workingCopy);
		assertTrue("working copy shouldn't answer to isOnClasspath", !isOnClasspath);
	} finally {
		if (workingCopy != null) workingCopy.destroy();
		this.deleteProject("SimpleProject");
	}
}


}
