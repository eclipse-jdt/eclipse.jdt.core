package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.model.*;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests IJavaModel API.
 */
public class JavaModelTests extends ModifyingResourceTests {

public static Test suite() {
	TestSuite suite = new Suite(JavaModelTests.class.getName());
	suite.addTest(new JavaModelTests("testGetJavaProject"));
	suite.addTest(new JavaModelTests("testGetJavaProjects1"));
	suite.addTest(new JavaModelTests("testGetJavaProjects2"));
	suite.addTest(new JavaModelTests("testAddFileToNonJavaProject"));
	return suite;
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

}

