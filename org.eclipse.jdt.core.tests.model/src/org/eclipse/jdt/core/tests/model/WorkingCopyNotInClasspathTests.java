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

import junit.framework.Test;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.util.Util;

public class WorkingCopyNotInClasspathTests extends ModifyingResourceTests {

	private ICompilationUnit workingCopy;

public WorkingCopyNotInClasspathTests(String name) {
	super(name);
}

public static Test suite() {
	return buildModelTestSuite(WorkingCopyNotInClasspathTests.class);
}

@Override
public void setUp() throws Exception {
	super.setUp();
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		this.createFolder("P/txt");
		IFile file = this.createFile("P/txt/X.java",
			"public class X {\n" +
			"}");
		ICompilationUnit cu = (ICompilationUnit)JavaCore.create(file);
		this.workingCopy = cu.getWorkingCopy(null);
	} catch (CoreException e) {
		e.printStackTrace();
	}
}

@Override
public void tearDown() throws Exception {
	try {
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
		this.deleteProject("P");
	} catch (CoreException e) {
		e.printStackTrace();
	}
	super.tearDown();
}

public void testCommitWorkingCopy1() throws CoreException {
	ICompilationUnit primary = this.workingCopy.getPrimary();
	assertTrue("Primary element should not be null", primary != null);

	IBuffer workingCopyBuffer = this.workingCopy.getBuffer();
	assertTrue("Working copy buffer should not be null", workingCopyBuffer != null);

	String newContents =
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}";
	workingCopyBuffer.setContents(newContents);
	this.workingCopy.commitWorkingCopy(false, null);

	IFile originalFile = (IFile)primary.getResource();
	assertSourceEquals(
		"Unexpected contents",
		newContents,
		new String(Util.getResourceContentsAsCharArray(originalFile)));
}
/*
 * Ensures that commiting a non-primary working copy that is inside a folder that is excluded doesn't throw a JavaModelException
 * (regression test for bug 52355 Not present exception trying to create a class in excluded package)
 */
public void testCommitWorkingCopy2() throws CoreException {
	ICompilationUnit copy = null;
	try {
		createJavaProject( "P2", new String[] {"src"}, null, null, null, "bin", null, null, new String[][] {new String[] {"p1/"}}, "1.4");
		createFolder("/P2/src/p1/p2");
		createFile("/P2/src/p1/p2/X.java", "");
		copy = getCompilationUnit("P2", "src", "p1.p2", "X.java").getWorkingCopy(null);
		copy.getBuffer().setContents("public class X {}");
		copy.makeConsistent(null);
		copy.commitWorkingCopy(false, null);
	} finally {
		if (copy != null) {
			copy.discardWorkingCopy();
		}
		deleteProject("P2");
	}
}
/*
 * Ensure that a working copy outside the classpath does not exist
 * (but can still be opened).
 */
public void testExistence()  {
	assertTrue("Working copy should exist", this.workingCopy.exists());
}
public void testGetSource() throws CoreException {
	ICompilationUnit copy = null;
	try {
		this.createJavaProject("P1", new String[] {}, "bin");
		this.createFolder("/P1/src/junit/test");
		String source =
			"package junit.test;\n" +
			"public class X {\n" +
			"}";
		IFile file = this.createFile("/P1/src/junit/test/X.java", source);
		ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
		copy = cu.getWorkingCopy(null);
		assertEquals(
			"Unexpected source",
			source,
			copy.getSource());
	} finally {
		if (copy != null) copy.discardWorkingCopy();
		this.deleteProject("P1");
	}
}
public void testParentExistence() {
	assertTrue("Working copy's parent should not exist", !this.workingCopy.getParent().exists());
}
/*
 * Ensures that a working copy created on a non-existing project can be reconciled.
 * (regression test for bug 40322 Error creating new Java projects)
 */
public void testReconcileNonExistingProject() throws CoreException {
	ICompilationUnit wc = null;
	try {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = root.getProject("NonExisting").getFile("A.java");
		wc = JavaCore.createCompilationUnitFrom(file).getWorkingCopy(null);
		wc.reconcile(ICompilationUnit.NO_AST, false, null, null);
	} finally {
		if (wc != null) {
			wc.discardWorkingCopy();
		}
	}
}
/*
 * Ensures that a working copy created in a simple project can be reconciled.
 * (regression test for bug 55421 Cannot save a .java file in a non-java project anymore)
 */
public void testReconcileSimpleProject() throws CoreException {
	ICompilationUnit wc = null;
	try {
	    IProject project = createProject("SimpleProject");
		IFile file = project.getFile("A.java");
		wc = JavaCore.createCompilationUnitFrom(file);
		ReconcilerTests.ProblemRequestor pbRequestor = new ReconcilerTests.ProblemRequestor();
		wc.becomeWorkingCopy(null);
		wc.getBuffer().setContents("public class A {}");
		WorkingCopyOwner owner = newWorkingCopyOwner(pbRequestor);
		wc.reconcile(ICompilationUnit.NO_AST, true/*force problem detection*/, owner, null);
	} finally {
		if (wc != null) {
			wc.discardWorkingCopy();
		}
		deleteProject("SimpleProject");
	}
}
/*
 * Ensures that a working copy created in a simple project can be reconciled when it is already
 * consistent but force problem detection is on.
 * (regression test for bug 104486 newNotPresentException when reconciling CU in a non-java project)
 */
public void testReconcileSimpleProject2() throws CoreException {
	ICompilationUnit wc = null;
	try {
	    IProject project = createProject("SimpleProject");
		IFile file = project.getFile("A.java");
		wc = JavaCore.createCompilationUnitFrom(file);
		ReconcilerTests.ProblemRequestor pbRequestor = new ReconcilerTests.ProblemRequestor();
		wc.becomeWorkingCopy(null);
		wc.reconcile(ICompilationUnit.NO_AST, true/*force problem detection*/, newWorkingCopyOwner(pbRequestor), null);
	} finally {
		if (wc != null) {
			wc.discardWorkingCopy();
		}
		deleteProject("SimpleProject");
	}
}
/*
 * Ensure that a working copy created on a .java file in a simple project can be opened.
 * (regression test for bug 33748 Cannot open working copy on .java file in simple project)
 */
public void testSimpleProject() throws CoreException {
	IParent copy = null;
	try {
		createProject("SimpleProject");
		IFile file = createFile(
			"/SimpleProject/X.java",
			"public class X {\n" +
			"}"
		);
		ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
		copy = cu.getWorkingCopy(null);
		try {
			copy.getChildren();
		} catch (JavaModelException e) {
			assertTrue("Should not get JavaModelException", false);
		}
	} finally {
		if (copy != null) {
			((ICompilationUnit)copy).discardWorkingCopy();
		}
		deleteProject("SimpleProject");
	}
}

/*
 * Ensure that a primary cu (which is outside the classpath) does not exist.
 */
public void testPrimaryExistence() {
	ICompilationUnit primary = this.workingCopy.getPrimary();
	assertTrue(
		"Primary compilation unit should not exist",
		!primary.exists());
}
public void testPrimaryParentExistence() {
	assertTrue(
		"Primary compilation unit's parent should not exist",
		!this.workingCopy.getPrimary().getParent().exists());
}
public void testIsOpen() {
	assertTrue("Working copy should be open", this.workingCopy.isOpen());
}
/*
 * Ensure that a primary cu (which is outside the classpath) is not opened.
 */
public void testPrimaryIsOpen() {
	ICompilationUnit original = this.workingCopy.getPrimary();
	assertTrue(
		"Primary compilation should not be opened",
		!original.isOpen());
}
// 31799 - asking project options on non-Java project populates the perProjectInfo cache incorrectly
public void testIsOnClasspath() throws CoreException {
	ICompilationUnit copy = null;
	try {
		createProject("SimpleProject");
		this.createFolder("/SimpleProject/src/junit/test");
		String source =
			"package junit.test;\n" +
			"public class X {\n" +
			"}";
		IFile file = this.createFile("/SimpleProject/src/junit/test/X.java", source);
		ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
		copy = cu.getWorkingCopy(null);

		// working creation will cause it to open, and thus request project options
		boolean isOnClasspath = copy.getJavaProject().isOnClasspath(copy);
		assertTrue("working copy shouldn't answer to isOnClasspath", !isOnClasspath);
	} finally {
		if (copy != null) copy.discardWorkingCopy();
		this.deleteProject("SimpleProject");
	}
}

// 42281
public void testReconcileAndCommit1() throws CoreException {
	ICompilationUnit copy = null;
	try {
		this.createJavaProject("JavaProject", new String[] {"src"}, "bin");
		this.createFolder("/JavaProject/src/native.1");
		String source =
			"class X {}";
		IFile file = this.createFile("/JavaProject/src/native.1/X.java", source);
		ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
		copy = cu.getWorkingCopy(null);

		IBuffer workingCopyBuffer = copy.getBuffer();
		assertTrue("Working copy buffer should not be null", workingCopyBuffer != null);
		String newContents =
			"public class X {\n" +
			"  public void foo() {\n" +
			"  }\n" +
			"}";

		workingCopyBuffer.setContents(newContents);
		copy.reconcile(ICompilationUnit.NO_AST, true, null, null);
		copy.commitWorkingCopy(true, null);

		IFile originalFile = (IFile)cu.getResource();
		assertSourceEquals(
			"Unexpected contents",
			newContents,
			new String(Util.getResourceContentsAsCharArray(originalFile)));
	} catch(JavaModelException e) {
		e.printStackTrace();
		assertTrue("No exception should have occurred: "+ e.getMessage(), false);
	} finally {
		if (copy != null) copy.discardWorkingCopy();
		this.deleteProject("JavaProject");
	}
}

// 41583
public void testReconcileAndCommit2() throws CoreException {
	ICompilationUnit copy = null;
	try {
		createProject("SimpleProject");
		this.createFolder("/SimpleProject/src/native.1");
		String source =
			"class X {}";
		IFile file = this.createFile("/SimpleProject/src/native.1/X.java", source);
		ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
		copy = cu.getWorkingCopy(null);

		IBuffer workingCopyBuffer = copy.getBuffer();
		assertTrue("Working copy buffer should not be null", workingCopyBuffer != null);
		String newContents =
			"public class X {\n" +
			"  public void foo() {\n" +
			"  }\n" +
			"}";

		workingCopyBuffer.setContents(newContents);
		copy.reconcile(ICompilationUnit.NO_AST, true, null, null);
		copy.commitWorkingCopy(true, null);
		IFile originalFile = (IFile)cu.getResource();
		assertSourceEquals(
			"Unexpected contents",
			newContents,
			new String(Util.getResourceContentsAsCharArray(originalFile)));

		assertTrue("buffer should not have been saved successfully", workingCopyBuffer.hasUnsavedChanges());
	} catch(JavaModelException e) {
		e.printStackTrace();
		assertTrue("No exception should have occurred: "+ e.getMessage(), false);
	} finally {
		if (copy != null) copy.discardWorkingCopy();
		this.deleteProject("SimpleProject");
	}
}
// 43879 - variation on 41583 (using primary working copy)
public void testReconcileAndCommit3() throws CoreException {
	ICompilationUnit primary = null;
	try {
		createProject("SimpleProject");
		this.createFolder("/SimpleProject/src/native.1");
		String source =
			"class X {}";
		IFile file = this.createFile("/SimpleProject/src/native.1/X.java", source);
		primary = JavaCore.createCompilationUnitFrom(file);
		primary.becomeWorkingCopy(null);

		IBuffer workingCopyBuffer = primary.getBuffer();
		assertTrue("Working copy buffer should not be null", workingCopyBuffer != null);
		String newContents =
			"public class X {\n" +
			"  public void foo() {\n" +
			"  }\n" +
			"}";

		workingCopyBuffer.setContents(newContents);
		primary.reconcile(ICompilationUnit.NO_AST, true, null, null);
		primary.commitWorkingCopy(true, null);
		IFile originalFile = (IFile)primary.getResource();
		assertSourceEquals(
			"Unexpected contents",
			newContents,
			new String(Util.getResourceContentsAsCharArray(originalFile)));

		assertTrue("buffer should have been saved successfully", !workingCopyBuffer.hasUnsavedChanges());
	} catch(JavaModelException e) {
		e.printStackTrace();
		assertTrue("No exception should have occurred: "+ e.getMessage(), false);
	} finally {
		if (primary != null) primary.discardWorkingCopy();
		this.deleteProject("SimpleProject");
	}
}
// 44580 - invalid unit name
public void testReconcileAndCommit4() throws CoreException {
	ICompilationUnit primary = null;
	try {
		createProject("SimpleProject");
		this.createFolder("/SimpleProject/src/native.1");
		String source =
			"class X {}";
		IFile file = this.createFile("/SimpleProject/src/native.1/some invalid name.java", source);
		primary = JavaCore.createCompilationUnitFrom(file);
		primary.becomeWorkingCopy(null);

		IBuffer workingCopyBuffer = primary.getBuffer();
		assertTrue("Working copy buffer should not be null", workingCopyBuffer != null);
		String newContents =
			"public class X {\n" +
			"  public void foo() {\n" +
			"  }\n" +
			"}";

		workingCopyBuffer.setContents(newContents);
		primary.reconcile(ICompilationUnit.NO_AST, true, null, null);
		primary.commitWorkingCopy(true, null);
		IFile originalFile = (IFile)primary.getResource();
		assertSourceEquals(
			"Unexpected contents",
			newContents,
			new String(Util.getResourceContentsAsCharArray(originalFile)));

		assertTrue("buffer should have been saved successfully", !workingCopyBuffer.hasUnsavedChanges());
	} catch(JavaModelException e) {
		e.printStackTrace();
		assertTrue("No exception should have occurred: "+ e.getMessage(), false);
	} finally {
		if (primary != null) primary.discardWorkingCopy();
		this.deleteProject("SimpleProject");
	}
}

// 44580 - invalid unit name
public void testReconcileAndCommit5() throws CoreException {
	ICompilationUnit copy = null;
	try {
		this.createJavaProject("JavaProject", new String[] {"src"}, "bin");
		this.createFolder("/JavaProject/src/p");
		String source =
			"package p; \n" +
			"public class X {}";
		IFile file = this.createFile("/JavaProject/src/invalid unit name.java", source);
		ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
		copy = cu.getWorkingCopy(null);

		IBuffer workingCopyBuffer = copy.getBuffer();
		assertTrue("Working copy buffer should not be null", workingCopyBuffer != null);
		String newContents =
			"public class X {\n" +
			"  public void foo() {\n" +
			"  }\n" +
			"}";

		workingCopyBuffer.setContents(newContents);
		copy.reconcile(ICompilationUnit.NO_AST, true, null, null);
		copy.commitWorkingCopy(true, null);

		IFile originalFile = (IFile)cu.getResource();
		assertSourceEquals(
			"Unexpected contents",
			newContents,
			new String(Util.getResourceContentsAsCharArray(originalFile)));
	} catch(JavaModelException e) {
		e.printStackTrace();
		assertTrue("No exception should have occurred: "+ e.getMessage(), false);
	} finally {
		if (copy != null) copy.discardWorkingCopy();
		this.deleteProject("JavaProject");
	}
}
}

