package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;

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
	
	IBuffer originalBuffer = original.getBuffer();
	assertTrue("Original buffer should not be null", originalBuffer != null);
	
	assertEquals(
		"Unexpected contents", 
		newContents, 
		new String(originalBuffer.getCharacters()));
}

/*
 * Ensure that a working copy outside the classpath does not exist 
 * (but can still be opened).
 */
public void testExistence() throws CoreException {
	assertTrue("Working copy should exist", this.workingCopy.exists());
}
public void testParentExistence() throws CoreException {
	assertTrue("Working copy's parent should not exist", !this.workingCopy.getParent().exists());
}

/*
 * Ensure that a original cu (which is outside the classpath) does not exist 
 * (but can still be opened).
 */
public void testOriginalExistence() throws CoreException {
	ICompilationUnit original = (ICompilationUnit)this.workingCopy.getOriginalElement();
	assertTrue(
		"Original compilation unit should exist", 
		original.exists());
}
public void testOriginalParentExistence() throws CoreException {
	assertTrue(
		"Original compilation unit's parent should not exist", 
		!this.workingCopy.getOriginalElement().getParent().exists());
}

public void testIsOpen() throws CoreException {
	assertTrue("Working copy should be open", this.workingCopy.isOpen());
}
public void testOriginalIsOpen() throws CoreException {
	ICompilationUnit original = (ICompilationUnit)this.workingCopy.getOriginalElement();
	assertTrue(
		"Original compilation should be open", 
		original.isOpen());
}



}
