/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.*;
import junit.framework.Test;
import junit.framework.TestSuite;

public class BufferTests extends ModifyingResourceTests implements IBufferChangedListener {
	protected BufferChangedEvent event= null;
public BufferTests(String name) {
	super(name);
}
/**
 * Cache the event
 */
public void bufferChanged(BufferChangedEvent event) {
	this.event= event;
}
protected IBuffer createBuffer(String path, String content) throws CoreException {
	this.createFile(path, content);
	ICompilationUnit cu = this.getCompilationUnit(path);
	IBuffer buffer = cu.getBuffer();
	buffer.addBufferChangedListener(this);
	this.event = null;
	return buffer;
}
protected void deleteBuffer(IBuffer buffer) throws CoreException {
	buffer.removeBufferChangedListener(this);
	IResource resource = buffer.getUnderlyingResource();
	if (resource != null) {
		resource.delete(true, null);
	}
}
/**
 * @see RegressionTestSuite#setUpSuite()
 */
public void setUpSuite() throws Exception {
	super.setUpSuite();
	try {
		this.createJavaProject("P", new String[] {""}, "");
		this.createFolder("P/x/y");
	} catch (CoreException e) {
		e.printStackTrace();
	}
}

/**
 * @see TestCase#tearDownSuite()
 */
public void tearDownSuite() throws Exception {
	super.tearDownSuite();
	this.deleteProject("P");
}


public static Test suite() {
	TestSuite suite = new Suite(BufferTests.class.getName());

	suite.addTest(new BufferTests("testAppend"));
	suite.addTest(new BufferTests("testAppendReadOnly"));
	suite.addTest(new BufferTests("testClose"));
	suite.addTest(new BufferTests("testGetChar"));
	suite.addTest(new BufferTests("testGetLength"));
	suite.addTest(new BufferTests("testGetText"));
	suite.addTest(new BufferTests("testGetUnderlyingResource"));
	suite.addTest(new BufferTests("testInsertBeginning"));
	suite.addTest(new BufferTests("testInsertMiddle"));
	suite.addTest(new BufferTests("testInsertEnd"));
	suite.addTest(new BufferTests("testReplaceBeginning"));
	suite.addTest(new BufferTests("testReplaceMiddle"));
	suite.addTest(new BufferTests("testReplaceEnd"));
	suite.addTest(new BufferTests("testDeleteBeginning"));
	suite.addTest(new BufferTests("testDeleteMiddle"));
	suite.addTest(new BufferTests("testDeleteEnd"));

	suite.addTest(new BufferTests("testCreateImport"));
	
	return suite;
}
/**
 * Tests appending to a buffer.
 */
public void testAppend() throws CoreException {
	IBuffer buffer = this.createBuffer(
		"P/x/y/A.java",
		"package x.y;\n" +
		"public class A {\n" +
		"}"
	);
	try {
		int oldLength= buffer.getLength();
		buffer.append("\nclass B {}");
		assertBufferEvent(oldLength, 0, "\nclass B {}");
		assertSourceEquals(
			"unexpected buffer contents",
			"package x.y;\n" +
			"public class A {\n" +
			"}\n" +
			"class B {}",
			buffer.getContents()
		);
		assertTrue("should have unsaved changes", buffer.hasUnsavedChanges());
	} finally {
		this.deleteBuffer(buffer);
	}
}
/**
 * Tests appending to a read-only buffer.
 */
public void testAppendReadOnly() throws CoreException {
	IBuffer buffer = this.createBuffer(
		"P/x/y/A.java",
		"package x.y;\n" +
		"public class A {\n" +
		"}"
	);
	try {
		buffer.getUnderlyingResource().setReadOnly(true);
		buffer.append("\nclass B {}");
		assertTrue("unexpected event", this.event == null);
		assertSourceEquals(
			"unexpected buffer contents",
			"package x.y;\n" +
			"public class A {\n" +
			"}",
			buffer.getContents()
		);
		assertTrue("should not have unsaved changes", !buffer.hasUnsavedChanges());
	} finally {
		buffer.getUnderlyingResource().setReadOnly(false);
		this.deleteBuffer(buffer);
	}
}
public void testClose() throws CoreException {
	IBuffer buffer = this.createBuffer(
		"P/x/y/A.java",
		"package x.y;\n" +
		"public class A {\n" +
		"}"
	);
	try {
		buffer.close();
		assertBufferEvent(0, 0, null);
	} finally {
		this.deleteBuffer(buffer);
	}
}


/**
 * Tests getting the underlying resource of a buffer.
 */
public void testGetUnderlyingResource() throws CoreException {
	IBuffer buffer = this.createBuffer(
		"P/x/y/A.java",
		"package x.y;\n" +
		"public class A {\n" +
		"}"
	);
	ICompilationUnit copy = null;
	try {
		IFile file = this.getFile("P/x/y/A.java");
		assertEquals("Unexpected underlying resource", file, buffer.getUnderlyingResource());
		
		copy = (ICompilationUnit)this.getCompilationUnit("P/x/y/A.java").getWorkingCopy();
		assertEquals("Unexpected underlying resource 2", null, copy.getBuffer().getUnderlyingResource());
	} finally {
		this.deleteBuffer(buffer);
		if (copy != null) {
			copy.destroy();
		}
	}
}
/**
 * Tests deleting text at the beginning of a buffer.
 */
public void testDeleteBeginning() throws CoreException {
	IBuffer buffer = this.createBuffer(
		"P/x/y/A.java",
		"package x.y;\n" +
		"public class A {\n" +
		"}"
	);
	try {
		buffer.replace(0, 13, "");
		assertBufferEvent(0, 13, null);
		assertSourceEquals(
			"unexpected buffer contents",
			"public class A {\n" +
			"}",
			buffer.getContents()
		);
		assertTrue("should have unsaved changes", buffer.hasUnsavedChanges());
	} finally {
		this.deleteBuffer(buffer);
	}
}
/**
 * Tests deleting text in the middle of a buffer.
 */
public void testDeleteMiddle() throws CoreException {
	IBuffer buffer = this.createBuffer(
		"P/x/y/A.java",
		"package x.y;\n" +
		"public class A {\n" +
		"}"
	);
	try {
		// delete "public "
		buffer.replace(13, 7, "");
		assertBufferEvent(13, 7, null);
		assertSourceEquals(
			"unexpected buffer contents",
			"package x.y;\n" +
			"class A {\n" +
			"}",
			buffer.getContents()
		);
		assertTrue("should have unsaved changes", buffer.hasUnsavedChanges());
	} finally {
		this.deleteBuffer(buffer);
	}
}
/**
 * Tests deleting text at the end of a buffer.
 */
public void testDeleteEnd() throws CoreException {
	IBuffer buffer = this.createBuffer(
		"P/x/y/A.java",
		"package x.y;\n" +
		"public class A {\n" +
		"}"
	);
	try {
		// delete "public class A {\n}"
		buffer.replace(13, 18, "");
		assertBufferEvent(13, 18, null);
		assertSourceEquals(
			"unexpected buffer contents",
			"package x.y;\n",
			buffer.getContents()
		);
		assertTrue("should have unsaved changes", buffer.hasUnsavedChanges());
	} finally {
		this.deleteBuffer(buffer);
	}
}
/**
 * Tests the buffer char retrieval via source position 
 */
public void testGetChar() throws CoreException {
	IBuffer buffer = this.createBuffer(
		"P/x/y/A.java",
		"package x.y;\n" +
		"public class A {\n" +
		"}"
	);
	try {
		assertEquals("Unexpected char at position 17", 'i', buffer.getChar(17));
	} finally {
		this.deleteBuffer(buffer);
	}
}
/**
 * Tests the buffer getLength() 
 */
public void testGetLength() throws CoreException {
	IBuffer buffer = this.createBuffer(
		"P/x/y/A.java",
		"package x.y;\n" +
		"public class A {\n" +
		"}"
	);
	try {
		assertEquals("Unexpected length", 31, buffer.getLength());
	} finally {
		this.deleteBuffer(buffer);
	}
}
/**
 * Tests the buffer text retrieval via source position 
 */
public void testGetText() throws CoreException {
	IBuffer buffer = this.createBuffer(
		"P/x/y/A.java",
		"package x.y;\n" +
		"public class A {\n" +
		"}"
	);
	try {
		assertSourceEquals("Unexpected text (1)", "p", buffer.getText(0, 1));
		assertSourceEquals("Unexpected text (2)", "public", buffer.getText(13, 6));
		assertSourceEquals("Unexpected text (3)", "", buffer.getText(10, 0));
	} finally {
		this.deleteBuffer(buffer);
	}
}
/**
 * Tests inserting text at the beginning of a buffer.
 */
public void testInsertBeginning() throws CoreException {
	IBuffer buffer = this.createBuffer(
		"P/x/y/A.java",
		"package x.y;\n" +
		"public class A {\n" +
		"}"
	);
	try {
		buffer.replace(0, 0, "/* copyright mycompany */\n");
		assertBufferEvent(0, 0, "/* copyright mycompany */\n");
		assertSourceEquals(
			"unexpected buffer contents",
			"/* copyright mycompany */\n" +
			"package x.y;\n" +
			"public class A {\n" +
			"}",
			buffer.getContents()
		);
		assertTrue("should have unsaved changes", buffer.hasUnsavedChanges());
	} finally {
		this.deleteBuffer(buffer);
	}
}
/**
 * Tests replacing text at the beginning of a buffer.
 */
public void testReplaceBeginning() throws CoreException {
	IBuffer buffer = this.createBuffer(
		"P/x/y/A.java",
		"package x.y;\n" +
		"public class A {\n" +
		"}"
	);
	try {
		buffer.replace(0, 13, "package other;\n");
		assertBufferEvent(0, 13, "package other;\n");
		assertSourceEquals(
			"unexpected buffer contents",
			"package other;\n" +
			"public class A {\n" +
			"}",
			buffer.getContents()
		);
		assertTrue("should have unsaved changes", buffer.hasUnsavedChanges());
	} finally {
		this.deleteBuffer(buffer);
	}
}
/**
 * Tests replacing text in the middle of a buffer.
 */
public void testReplaceMiddle() throws CoreException {
	IBuffer buffer = this.createBuffer(
		"P/x/y/A.java",
		"package x.y;\n" +
		"public class A {\n" +
		"}"
	);
	try {
		// replace "public class A" after the \n of package statement
		buffer.replace(13, 14, "public class B");
		assertBufferEvent(13, 14, "public class B");
		assertSourceEquals(
			"unexpected buffer contents",
			"package x.y;\n" +
			"public class B {\n" +
			"}",
			buffer.getContents()
		);
		assertTrue("should have unsaved changes", buffer.hasUnsavedChanges());
	} finally {
		this.deleteBuffer(buffer);
	}
}
/**
 * Tests replacing text at the end of a buffer.
 */
public void testReplaceEnd() throws CoreException {
	IBuffer buffer = this.createBuffer(
		"P/x/y/A.java",
		"package x.y;\n" +
		"public class A {\n" +
		"}"
	);
	try {
		// replace "}" at the end of cu with "}\n"
		int end = buffer.getLength();
		buffer.replace(end-1, 1, "}\n");
		assertBufferEvent(end-1, 1, "}\n");
		assertSourceEquals(
			"unexpected buffer contents",
			"package x.y;\n" +
			"public class A {\n" +
			"}\n",
			buffer.getContents()
		);
		assertTrue("should have unsaved changes", buffer.hasUnsavedChanges());
	} finally {
		this.deleteBuffer(buffer);
	}
}
/**
 * Tests inserting text in the middle of a buffer.
 */
public void testInsertMiddle() throws CoreException {
	IBuffer buffer = this.createBuffer(
		"P/x/y/A.java",
		"package x.y;\n" +
		"public class A {\n" +
		"}"
	);
	try {
		// insert after the \n of package statement
		buffer.replace(13, 0, "/* class comment */\n");
		assertBufferEvent(13, 0, "/* class comment */\n");
		assertSourceEquals(
			"unexpected buffer contents",
			"package x.y;\n" +
			"/* class comment */\n" +
			"public class A {\n" +
			"}",
			buffer.getContents()
		);
		assertTrue("should have unsaved changes", buffer.hasUnsavedChanges());
	} finally {
		this.deleteBuffer(buffer);
	}
}
/**
 * Tests inserting text at the end of a buffer.
 */
public void testInsertEnd() throws CoreException {
	IBuffer buffer = this.createBuffer(
		"P/x/y/A.java",
		"package x.y;\n" +
		"public class A {\n" +
		"}"
	);
	try {
		// insert after the \n of package statement
		int end = buffer.getLength();
		buffer.replace(end, 0, "\nclass B {}");
		assertBufferEvent(end, 0, "\nclass B {}");
		assertSourceEquals(
			"unexpected buffer contents",
			"package x.y;\n" +
			"public class A {\n" +
			"}\n" +
			"class B {}",
			buffer.getContents()
		);
		assertTrue("should have unsaved changes", buffer.hasUnsavedChanges());
	} finally {
		this.deleteBuffer(buffer);
	}
}

/**
 * Tests replacing text within a buffer using a create import
 * (regression test for PR #1G7A0WI).
 */
public void testCreateImport() throws CoreException {
	IFile file = this.createFile(
		"P/x/y/A.java",
		"package x.y;\n" +
		"public class A {\n" +
		"}"
	);
	ICompilationUnit copy = null;
	IBuffer buffer = null;
	try {
		copy = (ICompilationUnit)this.getCompilationUnit("P/x/y/A.java").getWorkingCopy();
		buffer = copy.getBuffer();
		buffer.addBufferChangedListener(this);
		copy.createImport("java.io.IOException", null, null);
		assertBufferEvent(13, 0, "import java.io.IOException;" + "\n"); // A.java has a \n line delimiter
	} finally {
		if (buffer != null) {
			buffer.removeBufferChangedListener(this);
		}
		if (copy != null) {
			copy.destroy();
		}
		file.delete(true, null);
	}
}
/**
 * Verify the buffer changed event.
 * The given text must contain '\n' line separators.
 */
protected void assertBufferEvent(int offset, int length, String text) {
	assertTrue("event should not be null", this.event != null);
	assertEquals("unexpected offset", offset, this.event.getOffset());
	assertEquals("unexpected length", length, this.event.getLength());
	if (text == null) {
		assertTrue("text should be null", this.event.getText() == null);
	} else {
		assertSourceEquals("unexpected text", text, this.event.getText());
	}
}
}
