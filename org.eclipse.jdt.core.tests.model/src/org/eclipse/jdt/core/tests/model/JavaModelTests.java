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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.model.*;

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
/**
 * Test that a model has no project.
 */
public void testGetJavaProject() throws JavaModelException {
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

}

