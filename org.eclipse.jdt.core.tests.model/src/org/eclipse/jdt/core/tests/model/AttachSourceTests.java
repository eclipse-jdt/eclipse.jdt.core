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
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;

/**
 * TO DO:
 * - source attachment on external jar.
 * - don't use assertTrue where assertEquals should be used
 * - don't hardcode positions
*/
public class AttachSourceTests extends ModifyingResourceTests {

/**
 * Attaches a source zip to the given jar package fragment root.
 */
protected void attachSource(IPackageFragmentRoot root, String sourcePath, String sourceRoot) throws JavaModelException {
	IJavaProject javaProject = root.getJavaProject();
	IClasspathEntry[] entries = (IClasspathEntry[])javaProject.getRawClasspath().clone();
	for (int i = 0; i < entries.length; i++){
		IClasspathEntry entry = entries[i];
		if (entry.getPath().toOSString().toLowerCase().equals(root.getPath().toOSString().toLowerCase())) {
			entries[i] = JavaCore.newLibraryEntry(
				root.getPath(),
				sourcePath == null ? null : new Path(sourcePath),
				sourceRoot == null ? null : new Path(sourceRoot),
				false);
			break;
		}
	}
	javaProject.setRawClasspath(entries, null);
}
	private IPackageFragmentRoot root;
	
public AttachSourceTests(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = new Suite(AttachSourceTests.class.getName());
	suite.addTest(new AttachSourceTests("testAttachSource"));
	suite.addTest(new AttachSourceTests("testGetSourceAttachmentPath"));
	suite.addTest(new AttachSourceTests("testAttachSourceRetrievalClass"));
	suite.addTest(new AttachSourceTests("testAttachSourceRetrievalMethod"));
	suite.addTest(new AttachSourceTests("testAttachSourceSourceRange"));
	suite.addTest(new AttachSourceTests("testAttachSourceSourceRangeInnerClass"));
	suite.addTest(new AttachSourceTests("testAttachSourceNameRange"));
	suite.addTest(new AttachSourceTests("testClassFileGetElementAt"));
	suite.addTest(new AttachSourceTests("testAttachSourcePersisted"));
	suite.addTest(new AttachSourceTests("testChangeSourceAttachmentFile"));
	suite.addTest(new AttachSourceTests("testDetachSource"));
	suite.addTest(new AttachSourceTests("testAttachSourceWithRootPath"));
	suite.addTest(new AttachSourceTests("testAttachSourceToLibFolder"));
	return suite;

}
/**
 * Create project and set the jar placeholder.
 */
public void setUpSuite() throws Exception {
	super.setUpSuite();
	
	IJavaProject project = setUpJavaProject("AttachSourceTests");
	this.root = project.getPackageFragmentRoot("/AttachSourceTests/attach.jar");
}
/**
 * Reset the jar placeholder and delete project.
 */
public void tearDownSuite() throws Exception {
	this.root.close();
	this.root = null;
	this.deleteProject("AttachSourceTests");
	
	super.tearDown();
}

/**
 * Attaches a source zip to the classes.zip jar.
 */
public void testAttachSource() throws CoreException {
	this.attachSource(this.root, "/AttachSourceTests/attachsrc.zip", null);
}

/**
 * Ensures that name ranges exists for BinaryMembers that have
 * mapped source.
 */
public void testAttachSourceNameRange() throws JavaModelException {
	IClassFile cf = this.root.getPackageFragment("x.y").getClassFile("A.class");
	IMethod method = cf.getType().getMethod("foo", null);
	assertTrue("method name range not correct", method.getNameRange().getOffset() != -1 && method.getNameRange().getLength() != 0);

	IClassFile objectCF = this.root.getPackageFragment("x.y").getClassFile("A.class");
	ISourceRange range= objectCF.getType().getNameRange();
	int start, end;
	start= range.getOffset();
	end= start + range.getLength() - 1;

	assertTrue("source code does not exist for the entire attached compilation unit", start != -1 && end != -1);
	String source= objectCF.getSource().substring(start, end + 1);
	assertEquals("name should be 'A'", "A", source);
}
/**
 * Closes the jar, to ensure when it is re-opened the source
 * attachment still exists.
 */
public void testAttachSourcePersisted() throws JavaModelException {
	this.root.close();
	testAttachSourceRetrievalClass();
	testAttachSourceRetrievalMethod();
}
/**
 * Retrieves the source code for methods of class A.
 */
public void testAttachSourceRetrievalMethod() throws JavaModelException {
	IClassFile cf = this.root.getPackageFragment("x.y").getClassFile("A.class");
	IMethod[] methods = cf.getType().getMethods();
	for (int i = 0; i < methods.length; i++) {
		IMethod method = methods[i];
		assertTrue("source code does not exist for the method " + method, method.getSource() != null);
		assertTrue("method name range not correct", method.getNameRange().getOffset() != -1 && method.getNameRange().getLength() != 0);
	}
}
/**
 * Retrieves the source code for "A.class", which is
 * the entire CU for "A.java".
 */
public void testAttachSourceRetrievalClass() throws JavaModelException {
	IClassFile objectCF = this.root.getPackageFragment("x.y").getClassFile("A.class");
	assertTrue("source code does not exist for the entire attached compilation unit", objectCF.getSource() != null);
}
/**
 * Ensures that a source range exists for the class file that has
 * mapped source.
 */
public void testAttachSourceSourceRange() throws JavaModelException {
	IClassFile cf = this.root.getPackageFragment("x.y").getClassFile("A.class");
	assertTrue("Class file source range not correct", cf.getSourceRange().getOffset() == 0 && cf.getSourceRange().getLength() != 0);
}
/**
 * Ensures that a source range exists for the (inner) class file that has
 * mapped source.
 */
public void testAttachSourceSourceRangeInnerClass() throws JavaModelException {
	IClassFile cf = this.root.getPackageFragment("x.y").getClassFile("A$Inner.class");
	assertTrue("Inner Class file source range not correct", cf.getSourceRange().getOffset() == 0 && cf.getSourceRange().getLength() != 0);
}
/**
 * Ensures that a source folder can be attached to a lib folder.
 */
public void testAttachSourceToLibFolder() throws JavaModelException {
	IPackageFragmentRoot root = this.getPackageFragmentRoot("/AttachSourceTests/lib");
	this.attachSource(root, "/AttachSourceTests/srcLib", "");
	
	IClassFile cf = root.getPackageFragment("p").getClassFile("X.class");
	String lineSeparator = System.getProperty("line.separator");
	assertEquals(
		"Unexpected source for class file",
		"package p;" + lineSeparator +		"public class X {" + lineSeparator +		"	public void foo() {" + lineSeparator +		"	}" + lineSeparator +		"}" + lineSeparator,
		cf.getSource());
}
/**
 * Attaches a source zip to a jar.  The source zip has
 * a nested root structure and exists as a resource.  Tests that
 * the attachment is persisted as a server property for the jar.
 */
public void testAttachSourceWithRootPath() throws JavaModelException {
	IJavaProject project = getJavaProject("AttachSourceTests");
	IFile jar = (IFile) project.getProject().findMember("attach2.jar");
	IFile srcZip=(IFile) project.getProject().findMember("attach2src.zip");
	JarPackageFragmentRoot root = (JarPackageFragmentRoot) project.getPackageFragmentRoot(jar);
	root.attachSource(srcZip.getFullPath(), new Path("src/nested"), null);

	IClassFile cf = root.getPackageFragment("x.y").getClassFile("B.class");
	assertTrue("source code does not exist for the entire attached compilation unit", cf.getSource() != null);
	root.close();
	cf = root.getPackageFragment("x.y").getClassFile("B.class");
	assertTrue("source code does not exist for the entire attached compilation unit", cf.getSource() != null);

	IPath rootSAPath= root.getSourceAttachmentRootPath();
	assertEquals("Unexpected source attachment root path for " + root.getPath(), "src/nested", rootSAPath.toString());

	IPath saPath= root.getSourceAttachmentPath();
	assertEquals("Unexpected source attachment path for " + root.getPath(), "/AttachSourceTests/attach2src.zip", saPath.toString());
	
	root.close();
}
/**
 * Ensure that a class file with an attached source can retrieve its children given a source index.
 */
public void testClassFileGetElementAt() throws JavaModelException {
	IClassFile cf = this.root.getPackageFragment("x.y").getClassFile("A.class");
	IJavaElement elt = null;
	
	elt = cf.getElementAt(15);
	assertTrue("should have found \"A\"",
		elt != null &&
		elt.getElementType() == IJavaElement.TYPE &&
		elt.getElementName().equals("A"));
	
	elt = cf.getElementAt(53);
	assertTrue("should have found \"public A()\"",
		elt != null &&
		elt.getElementType() == IJavaElement.METHOD &&
		elt.getElementName().equals("A"));

	elt = cf.getElementAt(72);
	assertTrue("should have found \"public void foo()\"",
		elt != null &&
		elt.getElementType() == IJavaElement.METHOD &&
		elt.getElementName().equals("foo"));
}
/**
 * Changing the source attachment file should update the java model.
 * (regression test for bug 23292 Must restart Eclipse after debug of source in .zip is updated)
 */
public void testChangeSourceAttachmentFile() throws CoreException {
	IClassFile cf = this.root.getPackageFragment("x.y").getClassFile("A.class");
	IMethod method = cf.getType().getMethod("foo", new String[] {});
	String lineSeparator = System.getProperty("line.separator");
	
	// check initial source
	assertEquals(
		"unexpected initial source for foo()",
		"public void foo() {" + lineSeparator +
		"	}",
		method.getSource());

	// replace source attachment file
	this.swapFiles("AttachSourceTests/attachsrc.zip", "AttachSourceTests/attachsrc.new.zip");
	assertEquals(
		"unexpected source for foo() after replacement",
		"public void foo() {" + lineSeparator +
		"		System.out.println(\"foo\");" + lineSeparator +
		"	}",
		method.getSource());
		
	// delete source attachment file
	this.deleteFile("AttachSourceTests/attachsrc.zip");
	assertEquals(
		"unexpected source for foo() after deletion",
		null,
		method.getSource());
		
	// add source attachment file back
	this.moveFile("AttachSourceTests/attachsrc.new.zip", "AttachSourceTests/attachsrc.zip");
	assertEquals(
		"unexpected source for foo() after addition",
		"public void foo() {" + lineSeparator +
		"	}",
		method.getSource());
}
/**
 * Removes the source attachment from the jar.
 */
public void testDetachSource() throws JavaModelException {
	this.attachSource(this.root, null, null);
	IClassFile cf = this.root.getPackageFragment("x.y").getClassFile("A.class");
	assertTrue("source code should no longer exist for A", cf.getSource() == null);
	assertTrue("name range should no longer exist for A", cf.getType().getNameRange().getOffset() == -1);
	assertTrue("source range should no longer exist for A", cf.getType().getSourceRange().getOffset() == -1);
	assertTrue("Source attachment path should be null", null == this.root.getSourceAttachmentPath());
	assertTrue("Source attachment root path should be null", null ==this.root.getSourceAttachmentRootPath());
}
/**
 * Retrieves the source attachment paths for jar root.
 */
public void testGetSourceAttachmentPath() throws JavaModelException {
	IPath saPath= this.root.getSourceAttachmentPath();
	assertEquals("Source attachment path not correct for root " + this.root, "/AttachSourceTests/attachsrc.zip", saPath.toString());
	assertEquals("Source attachment root path should be empty", new Path(""), this.root.getSourceAttachmentRootPath());
}

}
