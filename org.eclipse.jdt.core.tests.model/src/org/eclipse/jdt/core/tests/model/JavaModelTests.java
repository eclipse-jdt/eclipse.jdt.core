/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;

import junit.framework.Test;
/**
 * Tests IJavaModel API.
 */
public class JavaModelTests extends ModifyingResourceTests {

public static Test suite() {
	return new Suite(JavaModelTests.class);
}
public JavaModelTests(String name) {
	super(name);
}
protected int indexOf(String projectName, IJavaProject[] projects) {
	for (int i = 0, length = projects.length; i < length; i++) {
		if (projects[i].getElementName().equals(projectName)) {
			return i;
		}
	}
	return -1;
}
/*
 * Ensure that a java project is not added to the list of known java project
 * when a file is added to a non-java project.
 * (regression test for bug 18698 Seeing non-java projects in package view)
 */
public void testAddFileToNonJavaProject() throws CoreException {
	IJavaModel model = this.getJavaModel();
	IJavaProject[] projects = model.getJavaProjects();
	assertTrue(
		"Project P should not be present already",
		this.indexOf("P", projects) == -1
	);
	try {
		this.createProject("P");
		this.createFile("/P/toto.txt", "");
		projects = model.getJavaProjects();
		assertTrue(
			"Project P should not be present",
			this.indexOf("P", projects) == -1
		);
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensure that a resource belonging to the Java model is known to be contained in the Java model.
 * Case of non-accessible resources
 */
public void testContains1() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");

		// .java file
		IFile file = this.getFile("/P/X.java");
		assertTrue("/P/X.java should be in model", getJavaModel().contains(file));

		// .class file
		file = this.getFile("/P/X.class");
		assertTrue("/P/X.class should not be in model", !getJavaModel().contains(file));

		// non-Java resource
		file = this.getFile("/P/read.txt");
		assertTrue("/P/read.txt should be in model", getJavaModel().contains(file));

		// package
		IFolder folder = this.getFolder("/P/p");
		assertTrue("/P/p should be in model", getJavaModel().contains(folder));
		
		// resource in closed project
		file = this.createFile("/P/X.java", "");
		project.getProject().close(null);
		assertTrue("/P/X.java should be in model (even if project is closed)", getJavaModel().contains(file));
		
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensure that a resource belonging to the Java model is known to be contained in the Java model.
 * Case of projects
 */
public void testContains2() throws CoreException {
	try {
		// Java project
		IProject project = this.createJavaProject("P1", new String[] {""}, "").getProject();
		assertTrue("/P1 should be in model", getJavaModel().contains(project));

		// non-Java project
		project = this.createProject("P2");
		assertTrue("/P2 should be in model", getJavaModel().contains(project));
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/*
 * Ensure that a resource belonging to the Java model is known to be contained in the Java model.
 * Case of prj=src=bin
 */
public void testContains3() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {""}, "");

		// .java file
		IFile file = this.createFile("/P/X.java", "");
		assertTrue("/P/X.java should be in model", getJavaModel().contains(file));

		// .class file
		file = this.createFile("/P/X.class", "");
		assertTrue("/P/X.class should not be in model", !getJavaModel().contains(file));

		// non-Java resource
		file = this.createFile("/P/read.txt", "");
		assertTrue("/P/read.txt should be in model", getJavaModel().contains(file));

		// package
		IFolder folder = this.createFolder("/P/p");
		assertTrue("/P/p should be in model", getJavaModel().contains(folder));
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensure that a resource belonging to the Java model is known to be contained in the Java model.
 * Case of empty classpath.
 */
public void testContains4() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {}, "bin");

		// .java file
		IFile file = this.createFile("/P/X.java", "");
		assertTrue("/P/X.java should be in model", getJavaModel().contains(file));
		
		// .class file
		file = this.createFile("/P/X.class", "");
		assertTrue("/P/X.class should be in model", getJavaModel().contains(file));

		// non-Java resource file
		file = this.createFile("/P/read.txt", "");
		assertTrue("/P/read.txt should be in model", getJavaModel().contains(file));

		// non-Java resource folder
		IFolder folder = this.createFolder("/P/p");
		assertTrue("/P/p should be in model", getJavaModel().contains(folder));
		
		// bin folder
		folder = this.getFolder("/P/bin");
		assertTrue("/P/bin should not be in model", !getJavaModel().contains(folder));
		
		// classfile in bin folder
		file = this.createFile("/P/bin/X.class", "");
		assertTrue("/P/bin/X.class should not be in model", !getJavaModel().contains(file));
		
		// resource file in bin folder
		this.createFolder("/P/bin/image");
		file = this.createFile("/P/bin/image/ok.gif", "");
		assertTrue("/P/bin/image/ok.gif should not be in model", !getJavaModel().contains(file));
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensure that a resource belonging to the Java model is known to be contained in the Java model.
 * Case of src != bin
 */
public void testContains5() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");

		// .java file
		IFile file = this.createFile("/P/src/X.java", "");
		assertTrue("/P/src/X.java should be in model", getJavaModel().contains(file));

		// resource file in src
		this.createFolder("/P/src/image");
		file = this.createFile("/P/src/image/ok.gif", "");
		assertTrue("/P/src/image/ok.gif should not be in model", getJavaModel().contains(file));
		
		// .class file in bin
		file = this.createFile("/P/bin/X.class", "");
		assertTrue("/P/bin/X.class should not be in model", !getJavaModel().contains(file));

		// resource file in bin
		this.createFolder("/P/bin/image");
		file = this.createFile("/P/bin/image/ok.gif", "");
		assertTrue("/P/bin/image/ok.gif should not be in model", !getJavaModel().contains(file));

		// .class file in src
		file = this.createFile("/P/src/X.class", "");
		assertTrue("/P/src/X.class should not be in model", !getJavaModel().contains(file));

		// non-Java resource
		file = this.createFile("/P/src/read.txt", "");
		assertTrue("/P/src/read.txt should be in model", getJavaModel().contains(file));

		// package
		IFolder folder = this.createFolder("/P/src/p");
		assertTrue("/P/src/p should be in model", getJavaModel().contains(folder));
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensure that a resource belonging to the Java model is known to be contained in the Java model.
 * Case of prj==src and separate bin
 */
public void testContains6() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {""}, "bin");

		// .java file
		IFile file = this.createFile("/P/X.java", "");
		assertTrue("/P/X.java should be in model", getJavaModel().contains(file));

		// resource file in src
		this.createFolder("/P/image");
		file = this.createFile("/P/image/ok.gif", "");
		assertTrue("/P/image/ok.gif should not be in model", getJavaModel().contains(file));
		
		// .class file in bin
		file = this.createFile("/P/bin/X.class", "");
		assertTrue("/P/bin/X.class should not be in model", !getJavaModel().contains(file));

		// resource file in bin
		this.createFolder("/P/bin/image");
		file = this.createFile("/P/bin/image/ok.gif", "");
		assertTrue("/P/bin/image/ok.gif should not be in model", !getJavaModel().contains(file));

		// .class file in src
		file = this.createFile("/P/X.class", "");
		assertTrue("/P/X.class should not be in model", !getJavaModel().contains(file));

		// non-Java resource
		file = this.createFile("/P/read.txt", "");
		assertTrue("/P/read.txt should be in model", getJavaModel().contains(file));

		// package
		IFolder folder = this.createFolder("/P/p");
		assertTrue("/P/p should be in model", getJavaModel().contains(folder));
	} finally {
		this.deleteProject("P");
	}
}

/**
 * Test that a model has no project.
 */
public void testGetJavaProject() {
	IJavaModel model= getJavaModel();
	assertTrue("project should be null", model.getJavaProject() == null);
}
/*
 * Ensure that a java project that is added appears in the list of known java project,
 * and that it is removed from this list when deleted.
 */
public void testGetJavaProjects1() throws CoreException {
	IJavaModel model = this.getJavaModel();
	IJavaProject[] projects = model.getJavaProjects();
	assertTrue(
		"Project P should not be present already",
		this.indexOf("P", projects) == -1
	);
	try {
		this.createJavaProject("P", new String[] {}, "");
		projects = model.getJavaProjects();
		assertTrue(
			"Project P should be present",
			this.indexOf("P", projects) != -1
		);
		this.deleteProject("P");
		projects = model.getJavaProjects();
		assertTrue(
			"Project P should not be present any longer",
			this.indexOf("P", projects) == -1
		);
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensure that a non-java project that is added does not appears in the list of known java project.
 */
public void testGetJavaProjects2() throws CoreException {
	IJavaModel model = this.getJavaModel();
	IJavaProject[] projects = model.getJavaProjects();
	assertTrue(
		"Project P should not be present already",
		this.indexOf("P", projects) == -1
	);
	try {
		this.createProject("P");
		projects = model.getJavaProjects();
		assertTrue(
			"Project P should not be present",
			this.indexOf("P", projects) == -1
		);
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Test retrieving non-Java projects.
 */
public void testGetNonJavaResources() throws CoreException {
	try {
		IJavaModel model = this.getJavaModel();

		this.createJavaProject("JP", new String[]{}, "");
		assertResourcesEqual(
			"Unexpected non-Java resources",
			"",
			model.getNonJavaResources());

		this.createProject("SP1");
		assertResourcesEqual(
			"Unexpected non-Java resources after creation of SP1",
			"SP1",
			model.getNonJavaResources());
		
		this.createProject("SP2");
		assertResourcesEqual(
			"Unexpected non-Java resources after creation of SP2",
			"SP1\n" +
			"SP2",
			model.getNonJavaResources());

		this.deleteProject("SP1");
		assertResourcesEqual(
			"Unexpected non-Java resources after deletion of SP1",
			"SP2",
			model.getNonJavaResources());
	} finally {
		this.deleteProject("SP1");
		this.deleteProject("SP2");
		this.deleteProject("JP");
	}
}
/*
 * Ensures that the right scheduling rule is returned for a Java project
 */
public void testGetSchedulingRule1() {
	IJavaProject project = getJavaProject("P");
	assertEquals(
		"Unexpected scheduling rule",
		project.getResource(),
		project.getSchedulingRule());
}
/*
 * Ensures that the right scheduling rule is returned for a source package fragment root
 */
public void testGetSchedulingRule2() {
	IResource folder = getFolder("/P/src");
	IPackageFragmentRoot root = getJavaProject("P").getPackageFragmentRoot(folder);
	assertEquals(
		"Unexpected scheduling rule",
		root.getResource(),
		root.getSchedulingRule());
}
/*
 * Ensures that the right scheduling rule is returned for an external jar package fragment root
 */
public void testGetSchedulingRule3() {
	IPackageFragmentRoot root1 = getJavaProject("P1").getPackageFragmentRoot("c:\\some.jar");
	ISchedulingRule rule1 = root1.getSchedulingRule();
	IPackageFragmentRoot root2 = getJavaProject("P2").getPackageFragmentRoot("c:\\some.jar");
	ISchedulingRule rule2 = root2.getSchedulingRule();
	assertTrue("Rule 1 should contain rule 2", rule1.contains(rule2));
	assertTrue("Rule 1 should conflict with rule 2", rule1.isConflicting(rule2));
	assertTrue("Rule 2 should contain rule 1", rule2.contains(rule1));
	assertTrue("Rule 2 should conflict with rule 1", rule2.isConflicting(rule1));
}
/*
 * Ensures that the right scheduling rule is returned for a source package fragment
 */
public void testGetSchedulingRule4() {
	IResource folder = getFolder("/P/src");
	IPackageFragment pkg = getJavaProject("P").getPackageFragmentRoot(folder).getPackageFragment("p");
	assertEquals(
		"Unexpected scheduling rule",
		pkg.getResource(),
		pkg.getSchedulingRule());
}
/*
 * Ensures that JavaCore#initializeAfterLoad() can be called on startup
 */
public void testInitializeAfterLoad() throws CoreException {
	simulateExitRestart();
	JavaCore.initializeAfterLoad(null);
}
}

