/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

import java.util.ArrayList;
import junit.framework.Test;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.BufferChangedEvent;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IBufferChangedListener;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.WorkingCopyOwner;

@SuppressWarnings({"rawtypes", "unchecked"})
public class BufferTests extends ModifyingResourceTests implements IBufferChangedListener {
	protected ArrayList events = null;
public BufferTests(String name) {
	super(name);
}
/**
 * Cache the event
 */
public void bufferChanged(BufferChangedEvent bufferChangedEvent) {
	this.events.add(bufferChangedEvent);
}
protected IBuffer createBuffer(String path, String content) throws CoreException {
	waitUntilIndexesReady(); // ensure that the indexer is not reading the file
	this.createFile(path, content);
	ICompilationUnit cu = this.getCompilationUnit(path);
	IBuffer buffer = cu.getBuffer();
	buffer.addBufferChangedListener(this);
	this.events = new ArrayList();
	return buffer;
}
protected void deleteBuffer(IBuffer buffer) throws CoreException {
	buffer.removeBufferChangedListener(this);
	IResource resource = buffer.getUnderlyingResource();
	if (resource != null) {
		deleteResource(resource);
	}
}
/**
 * @see AbstractJavaModelTests#setUpSuite()
 */
@Override
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
 * @see AbstractJavaModelTests#tearDownSuite()
 */
@Override
public void tearDownSuite() throws Exception {
	super.tearDownSuite();
	this.deleteProject("P");
}


public static Test suite() {
	return buildModelTestSuite(BufferTests.class);
}
/**
 * Tests appending to a buffer.
 */
public void testAppend() throws CoreException {
	IBuffer buffer = createBuffer(
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
		deleteBuffer(buffer);
	}
}
/**
 * Tests appending to a read-only buffer.
 */
public void testAppendReadOnly() throws CoreException {
	IBuffer buffer = null;
	try {
		createJavaProject("P1", new String[] {}, new String[] {"JCL18_LIB,JCL18_SRC,JCL_SRCROOT"}, "");
		IClassFile classFile = getClassFile("P1", getExternalJCLPathString(), "java.lang", "String.class");
		buffer = classFile.getBuffer();
		buffer.addBufferChangedListener(this);
		this.events = new ArrayList();
		buffer.append("\nclass B {}");
		assertTrue("unexpected event", this.events.isEmpty());
		assertSourceEquals(
			"unexpected buffer contents",
			"package java.lang;\n" +
			"\n" +
			"public class String implements Comparable<String>, CharSequence {\n" +
			"	public int length() { return 0; }\n" +
			"}\n",
			buffer.getContents()
		);
		assertTrue("should not have unsaved changes", !buffer.hasUnsavedChanges());
	} finally {
		if (buffer != null) {
			buffer.removeBufferChangedListener(this);
		}
		deleteProject("P1");
	}
}
public void testClose() throws CoreException {
	IBuffer buffer = createBuffer(
		"P/x/y/A.java",
		"package x.y;\n" +
		"public class A {\n" +
		"}"
	);
	try {
		buffer.close();
		assertBufferEvent(0, 0, null);
	} finally {
		deleteBuffer(buffer);
	}
}


/**
 * Tests getting the underlying resource of a buffer.
 */
public void testGetUnderlyingResource() throws CoreException {
	IBuffer buffer = createBuffer(
		"P/x/y/A.java",
		"package x.y;\n" +
		"public class A {\n" +
		"}"
	);
	ICompilationUnit copy = null;
	try {
		IFile file = getFile("P/x/y/A.java");
		assertEquals("Unexpected underlying resource", file, buffer.getUnderlyingResource());

		copy = this.getCompilationUnit("P/x/y/A.java").getWorkingCopy(null);
		assertEquals("Unexpected underlying resource 2", file, copy.getBuffer().getUnderlyingResource());
	} finally {
		deleteBuffer(buffer);
		if (copy != null) {
			copy.discardWorkingCopy();
		}
	}
}
/**
 * Tests deleting text at the beginning of a buffer.
 */
public void testDeleteBeginning() throws CoreException {
	IBuffer buffer = createBuffer(
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
		deleteBuffer(buffer);
	}
}
/**
 * Tests deleting text in the middle of a buffer.
 */
public void testDeleteMiddle() throws CoreException {
	IBuffer buffer = createBuffer(
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
		deleteBuffer(buffer);
	}
}
/**
 * Tests deleting text at the end of a buffer.
 */
public void testDeleteEnd() throws CoreException {
	IBuffer buffer = createBuffer(
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
		deleteBuffer(buffer);
	}
}
/**
 * Tests the buffer char retrieval via source position
 */
public void testGetChar() throws CoreException {
	IBuffer buffer = createBuffer(
		"P/x/y/A.java",
		"package x.y;\n" +
		"public class A {\n" +
		"}"
	);
	try {
		assertEquals("Unexpected char at position 17", 'i', buffer.getChar(17));
	} finally {
		deleteBuffer(buffer);
	}
}
/**
 * Tests the buffer char retrieval via source position doesn't throw an exception if the buffer is closed.
 * (regression test for bug 46040 NPE in Eclipse console)
 */
public void testGetChar2() throws CoreException {
	IBuffer buffer = createBuffer(
		"P/x/y/A.java",
		"package x.y;\n" +
		"public class A {\n" +
		"}"
	);
	buffer.close();
	try {
		assertEquals("Unexpected char at position 17", Character.MIN_VALUE, buffer.getChar(17));
	} finally {
		deleteBuffer(buffer);
	}
}
/**
 * Tests the buffer getLength()
 */
public void testGetLength() throws CoreException {
	IBuffer buffer = createBuffer(
		"P/x/y/A.java",
		"package x.y;\n" +
		"public class A {\n" +
		"}"
	);
	try {
		assertEquals("Unexpected length", 31, buffer.getLength());
	} finally {
		deleteBuffer(buffer);
	}
}
/**
 * Tests the buffer text retrieval via source position
 */
public void testGetText() throws CoreException {
	IBuffer buffer = createBuffer(
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
		deleteBuffer(buffer);
	}
}
/**
 * Tests inserting text at the beginning of a buffer.
 */
public void testInsertBeginning() throws CoreException {
	IBuffer buffer = createBuffer(
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
		deleteBuffer(buffer);
	}
}
/**
 * Tests replacing text at the beginning of a buffer.
 */
public void testReplaceBeginning() throws CoreException {
	IBuffer buffer = createBuffer(
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
		deleteBuffer(buffer);
	}
}
/**
 * Tests replacing text in the middle of a buffer.
 */
public void testReplaceMiddle() throws CoreException {
	IBuffer buffer = createBuffer(
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
		deleteBuffer(buffer);
	}
}
/**
 * Tests replacing text at the end of a buffer.
 */
public void testReplaceEnd() throws CoreException {
	IBuffer buffer = createBuffer(
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
		deleteBuffer(buffer);
	}
}
/*
 * Ensure that saving a buffer on a non-yet existing resource and using the UTF-8 encoding doesn't throw an exception
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=234307 )
 */
public void testSaveNonExistingUTF8() throws Exception {
	IBuffer buffer = (new WorkingCopyOwner() {}).createBuffer(getCompilationUnit("/P/X234307.java"));
	buffer.setContents(
		"public class X234307 {\n" +
		"}"
	);
	IProject project = getProject("P");
	String defaultCharset = project.getDefaultCharset();
	try {
		project.setDefaultCharset("UTF-8", null);
		String newContents =
			"public interface X234307 {\n" +
			"}";
		buffer.setContents(newContents);
		buffer.save(null, false/*don't force*/);
		char[] contents = org.eclipse.jdt.internal.compiler.util.Util.getFileCharContent(getFile("/P/X234307.java").getLocation().toFile(), null);
		assertSourceEquals("Unexpected source", newContents, new String(contents));
	} finally {
		deleteBuffer(buffer); // this deletes the file as well
		project.setDefaultCharset(defaultCharset, null);
	}
}
/**
 * Tests inserting text in the middle of a buffer.
 */
public void testInsertMiddle() throws CoreException {
	IBuffer buffer = createBuffer(
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
		deleteBuffer(buffer);
	}
}
/**
 * Tests inserting text at the end of a buffer.
 */
public void testInsertEnd() throws CoreException {
	IBuffer buffer = createBuffer(
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
		deleteBuffer(buffer);
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
		copy = this.getCompilationUnit("P/x/y/A.java").getWorkingCopy(null);
		buffer = copy.getBuffer();
		buffer.addBufferChangedListener(this);
		this.events = new ArrayList();
		copy.createImport("java.io.IOException", null, null);
		assertBufferEvents(
			"(12, 0) import java.io.IOException;\n" +
			"(12, 0) \n" +
			"\n"
		); // A.java has a \n line delimiter
	} finally {
		if (buffer != null) {
			buffer.removeBufferChangedListener(this);
		}
		if (copy != null) {
			copy.discardWorkingCopy();
		}
		deleteResource(file);
	}
}
/**
 * Verify the buffer changed event.
 * The given text must contain '\n' line separators.
 */
protected void assertBufferEvent(int offset, int length, String text) {
	assertTrue("events should not be null", this.events != null);
	assertTrue("events should not be empty", !this.events.isEmpty());
	BufferChangedEvent event = (BufferChangedEvent) this.events.get(0);
	assertEquals("unexpected offset", offset, event.getOffset());
	assertEquals("unexpected length", length, event.getLength());
	if (text == null) {
		assertTrue("text should be null", event.getText() == null);
	} else {
		assertSourceEquals("unexpected text", text, event.getText());
	}
}

protected void assertBufferEvents(String expected) {
	StringBuilder buffer = new StringBuilder();
	if (this.events == null)
		buffer.append("<null>");
	else {
		for (int i = 0, length = this.events.size(); i < length; i++) {
			BufferChangedEvent event = (BufferChangedEvent) this.events.get(i);
			buffer.append('(');
			buffer.append(event.getOffset());
			buffer.append(", ");
			buffer.append(event.getLength());
			buffer.append(") ");
			buffer.append(event.getText());
			if (i < length-1)
				buffer.append("\n");
		}
	}
	assertSourceEquals("Unexpected buffer events", expected, buffer.toString());
}
}
