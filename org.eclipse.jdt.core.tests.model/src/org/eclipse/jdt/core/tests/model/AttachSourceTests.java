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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;

/**
 * TO DO:
 * - source attachment on external jar.
 * - don't use assertTrue where assertEquals should be used
 * - don't hardcode positions
*/
public class AttachSourceTests extends AbstractJavaModelTests {

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
	private IPackageFragmentRoot jarRoot;
	
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
	suite.addTest(new AttachSourceTests("testDetachSource"));
	suite.addTest(new AttachSourceTests("testAttachSourceWithRootPath"));
	return suite;

}
/**
 * Create project and set the jar placeholder.
 */
public void setUpSuite() throws Exception {
	super.setUpSuite();
	
	IJavaProject project = setUpJavaProject("AttachSourceTests");
	this.jarRoot = project.getPackageFragmentRoot("/AttachSourceTests/attach.jar");
}
/**
 * Reset the jar placeholder and delete project.
 */
public void tearDownSuite() throws Exception {
	this.jarRoot.close();
	this.jarRoot = null;
	this.deleteProject("AttachSourceTests");
	
	super.tearDown();
}

/**
 * Attaches a source zip to the classes.zip jar.
 */
public void testAttachSource() {
	try {
		this.attachSource(this.jarRoot, "/AttachSourceTests/attachsrc.zip", null);
	} catch (JavaModelException jme) {
		fail("Attach source operation creation failed");
	}
}

/**
 * Ensures that name ranges exists for BinaryMembers that have
 * mapped source.
 */
public void testAttachSourceNameRange() throws JavaModelException {
	IClassFile cf = this.jarRoot.getPackageFragment("x.y").getClassFile("A.class");
	IMethod method = cf.getType().getMethod("foo", null);
	assertTrue("method name range not correct", method.getNameRange().getOffset() != -1 && method.getNameRange().getLength() != 0);

	IClassFile objectCF = this.jarRoot.getPackageFragment("x.y").getClassFile("A.class");
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
	this.jarRoot.close();
	testAttachSourceRetrievalClass();
	testAttachSourceRetrievalMethod();
}
/**
 * Retrieves the source code for "String#equals(Object)".
 */
public void testAttachSourceRetrievalMethod() throws JavaModelException {
	IClassFile cf = this.jarRoot.getPackageFragment("x.y").getClassFile("A.class");
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
	IClassFile objectCF = this.jarRoot.getPackageFragment("x.y").getClassFile("A.class");
	assertTrue("source code does not exist for the entire attached compilation unit", objectCF.getSource() != null);
}
/**
 * Ensures that a source range exists for the class file that has
 * mapped source.
 */
public void testAttachSourceSourceRange() throws JavaModelException {
	IClassFile cf = this.jarRoot.getPackageFragment("x.y").getClassFile("A.class");
	assertTrue("Class file source range not correct", cf.getSourceRange().getOffset() == 0 && cf.getSourceRange().getLength() != 0);

}
/**
 * Ensures that a source range exists for the (inner) class file that has
 * mapped source.
 */
public void testAttachSourceSourceRangeInnerClass() throws JavaModelException {
	IClassFile cf = this.jarRoot.getPackageFragment("x.y").getClassFile("A$Inner.class");
	assertTrue("Inner Class file source range not correct", cf.getSourceRange().getOffset() == 0 && cf.getSourceRange().getLength() != 0);

}
/**
 * Attaches a source zip to the Minimal.zip jar.  The source zip has
 * a nested root structure and exists as a resource.  Tests that
 * the attachment is persisted as a server property for the jar.
 */
public void testAttachSourceWithRootPath() throws JavaModelException {
	try {
		IJavaProject project = getJavaProject("AttachSourceTests");
		IFile jar = (IFile) project.getProject().findMember("attach2.jar");
		IFile srcZip=(IFile) project.getProject().findMember("attach2src.zip");
		JarPackageFragmentRoot jarRoot = (JarPackageFragmentRoot) project.getPackageFragmentRoot(jar);
		jarRoot.attachSource(srcZip.getFullPath(), new Path("src/nested"), null);

		IClassFile cf = jarRoot.getPackageFragment("x.y").getClassFile("B.class");
		assertTrue("source code does not exist for the entire attached compilation unit", cf.getSource() != null);
		jarRoot.close();
		cf = jarRoot.getPackageFragment("x.y").getClassFile("B.class");
		assertTrue("source code does not exist for the entire attached compilation unit", cf.getSource() != null);

		IPath rootSAPath= jarRoot.getSourceAttachmentRootPath();
		assertEquals("Unexpected source attachment root path for " + jarRoot.getPath(), "src/nested", rootSAPath.toString());

		IPath saPath= jarRoot.getSourceAttachmentPath();
		assertEquals("Unexpected source attachment path for " + jarRoot.getPath(), "/AttachSourceTests/attach2src.zip", saPath.toString());
		
		jarRoot.close();
	} catch (JavaModelException jme) {
		fail("Attach source operation creation failed");
	}
}
/**
 * Ensure that a class file with an attached source can retrieve its children given a source index.
 */
public void testClassFileGetElementAt() throws JavaModelException {
	IClassFile cf = this.jarRoot.getPackageFragment("x.y").getClassFile("A.class");
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
 * Removes the source attachment from the jar.
 */
public void testDetachSource() throws JavaModelException {
	try {
		this.attachSource(this.jarRoot, null, null);
		IClassFile cf = this.jarRoot.getPackageFragment("x.y").getClassFile("A.class");
		assertTrue("source code should no longer exist for A", cf.getSource() == null);
		assertTrue("name range should no longer exist for A", cf.getType().getNameRange().getOffset() == -1);
		assertTrue("source range should no longer exist for A", cf.getType().getSourceRange().getOffset() == -1);
		assertTrue("Source attachment path should be null", null == this.jarRoot.getSourceAttachmentPath());
		assertTrue("Source attachment root path should be null", null ==this.jarRoot.getSourceAttachmentRootPath());
	} catch (JavaModelException jme) {
		fail("Source Detach Failed");
	}
}
/**
 * Retrieves the source attachment paths for jar root.
 */
public void testGetSourceAttachmentPath() throws JavaModelException {
	IPath saPath= this.jarRoot.getSourceAttachmentPath();
	assertEquals("Source attachment path not correct for root " + this.jarRoot, "/AttachSourceTests/attachsrc.zip", saPath.toString());
	assertEquals("Source attachment root path should be empty", new Path(""), this.jarRoot.getSourceAttachmentRootPath());
}

}
