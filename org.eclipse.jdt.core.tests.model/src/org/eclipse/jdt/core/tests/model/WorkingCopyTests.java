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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import junit.framework.Test;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;



public class WorkingCopyTests extends ModifyingResourceTests {
	ICompilationUnit cu = null;
	ICompilationUnit copy = null;
	
	public class BufferFactory implements IBufferFactory {
		/*
		 * @see IBufferFactory#createBuffer(IOpenable)
		 */
		public IBuffer createBuffer(IOpenable owner) {
			return new Buffer(owner);
		}

	}
	public class Buffer implements IBuffer {
		IOpenable owner;
		ArrayList changeListeners;
		char[] contents = null;
		public Buffer(IOpenable owner) {
			this.owner = owner;	
		}
		/*
		 * @see IBuffer#addBufferChangedListener(IBufferChangedListener)
		 */
		public void addBufferChangedListener(IBufferChangedListener listener) {
			if (this.changeListeners == null) {
				this.changeListeners = new ArrayList(5);
			}
			if (!this.changeListeners.contains(listener)) {
				this.changeListeners.add(listener);
			}
		}

			/*
		 * @see IBuffer#append(char[])
		 */
		public void append(char[] text) {
		}

		/*
		 * @see IBuffer#append(String)
		 */
		public void append(String text) {
		}

		/*
		 * @see IBuffer#close()
		 */
		public void close() {
			this.contents = null; // mark as closed
			if (this.changeListeners != null) {
				BufferChangedEvent event = null;
				event = new BufferChangedEvent(this, 0, 0, null);
				for (int i = 0, size = this.changeListeners.size(); i < size; ++i) {
					IBufferChangedListener listener = (IBufferChangedListener) this.changeListeners.get(i);
					listener.bufferChanged(event);
				}
				this.changeListeners = null;
			}
	}

		/*
		 * @see IBuffer#getChar(int)
		 */
		public char getChar(int position) {
			return 0;
		}

		/*
		 * @see IBuffer#getCharacters()
		 */
		public char[] getCharacters() {
			return contents;
		}

		/*
		 * @see IBuffer#getContents()
		 */
		public String getContents() {
			return new String(contents);
		}

		/*
		 * @see IBuffer#getLength()
		 */
		public int getLength() {
			return contents.length;
		}

		/*
		 * @see IBuffer#getOwner()
		 */
		public IOpenable getOwner() {
			return this.owner;
		}

		/*
		 * @see IBuffer#getText(int, int)
		 */
		public String getText(int offset, int length) {
			return null;
		}

		/*
		 * @see IBuffer#getUnderlyingResource()
		 */
		public IResource getUnderlyingResource() {
			return null;
		}

		/*
		 * @see IBuffer#hasUnsavedChanges()
		 */
		public boolean hasUnsavedChanges() {
			return false;
		}

		/*
		 * @see IBuffer#isClosed()
		 */
		public boolean isClosed() {
			return this.contents == null;
		}

		/*
		 * @see IBuffer#isReadOnly()
		 */
		public boolean isReadOnly() {
			return false;
		}

		/*
		 * @see IBuffer#removeBufferChangedListener(IBufferChangedListener)
		 */
		public void removeBufferChangedListener(IBufferChangedListener listener) {
			if (this.changeListeners != null) {
				this.changeListeners.remove(listener);
				if (this.changeListeners.size() == 0) {
					this.changeListeners = null;
				}
			}
		}

		/*
		 * @see IBuffer#replace(int, int, char[])
		 */
		public void replace(int position, int length, char[] text) {
		}

		/*
		 * @see IBuffer#replace(int, int, String)
		 */
		public void replace(int position, int length, String text) {
		}

		/*
		 * @see IBuffer#save(IProgressMonitor, boolean)
		 */
		public void save(IProgressMonitor progress, boolean force)
			throws JavaModelException {
		}

		/*
		 * @see IBuffer#setContents(char[])
		 */
		public void setContents(char[] characters) {
			contents = characters;
		}

		/*
		 * @see IBuffer#setContents(String)
		 */
		public void setContents(String characters) {
			contents = characters.toCharArray();
		}

}
	
public WorkingCopyTests(String name) {
	super(name);
}

public static Test suite() {
	return new Suite(WorkingCopyTests.class);
}
protected void setUp() {
	try {
		this.createJavaProject(
			"P", 
			new String[] {"src"}, 
			new String[] {this.getExternalJCLPathString(), "lib"}, 
			"bin");
		this.createFolder("P/src/x/y");
		this.createFile("P/src/x/y/A.java", 
			"package x.y;\n" +
			"import java.io.File;\n" +
			"public class A {\n" +
			"  public class Inner {\n" +
			"    public class InnerInner {\n" +
			"    }\n" +
			"    int innerField;\n" +
			"    void innerMethod() {\n" +
			"    }\n" +
			"  }\n" +
			"  static String FIELD;\n" +
			"  {\n" +
			"    FIELD = File.pathSeparator;\n" +
			"  }\n" +
			"  int field1;\n" +
			"  boolean field2;\n" +
			"  public void foo() {\n" +
			"  }\n" +
			"}");
		this.cu = this.getCompilationUnit("P/src/x/y/A.java");
		this.copy = (ICompilationUnit) cu.getWorkingCopy();
	} catch (CoreException e) {
		e.printStackTrace();
		throw new RuntimeException(e.getMessage());
	}
}
protected void tearDown() throws Exception {
	if (this.copy != null) this.copy.destroy();
	this.deleteProject("P");
}
/**
 */
public void testChangeContent() throws CoreException {
	String newContents =
		"package x.y;\n" +
		"public class A {\n" +
		"  public void bar() {\n" +
		"  }\n" +
		"}";
	this.copy.getBuffer().setContents(newContents);
	this.copy.reconcile();
	assertSourceEquals(
		"Unexpected working copy contents",
		newContents,
		this.copy.getBuffer().getContents());
	
	this.copy.commit(true, null);
	assertSourceEquals(
		"Unexpected original cu contents",
		newContents,
		this.cu.getBuffer().getContents());
}
/**
 */
public void testChangeContentOfReadOnlyCU() throws CoreException {
	IResource resource = this.cu.getUnderlyingResource();
	boolean readOnlyFlag = resource.isReadOnly();
	boolean didComplain = false;
	try {
		resource.setReadOnly(true);
		this.copy.getBuffer().setContents("invalid");
		this.copy.commit(true, null);
	} catch(JavaModelException e){
		didComplain = true;
	} finally {
		resource.setReadOnly(readOnlyFlag);
	}
	assertTrue("Should have complained about modifying a read-only unit:", didComplain);
	assertTrue("ReadOnly buffer got modified:", !this.cu.getBuffer().getContents().equals("invalid"));
}

/**
 * Ensures that the source contents of a working copy are
 * not altered by changes to the source of the original compilation
 * unit.
 */
public void testContents() throws CoreException {
	String originalSource = this.cu.getSource();
	IType type = this.cu.getType("A");
	assertDeletion(type);
	assertTrue("source code of copy should still be original", this.copy.getSource().equals(originalSource));
}

/**
 * Test creating a working copy on a class file with a customized buffer.
 */
public void testOnClassFile() throws JavaModelException {
	this.attachSource(this.getPackageFragmentRoot("P", this.getExternalJCLPathString()), this.getExternalJCLSourcePath().toOSString(), "src");
	IClassFile classFile = this.getClassFile("P", this.getExternalJCLPathString(), "java.lang", "Object.class");
	IBufferFactory factory = new BufferFactory();
	IJavaElement customizedCopy = classFile.getWorkingCopy(null, factory);
	try {
		assertTrue("Should be an IOpenable", customizedCopy instanceof ICompilationUnit);
		IBuffer buffer = ((ICompilationUnit)customizedCopy).getBuffer();
		assertTrue("Unexpected buffer", buffer instanceof Buffer);	
		assertTrue("Buffer should be initialized with source", buffer.getCharacters().length > 0);
	} finally {
		if (customizedCopy instanceof IWorkingCopy) {
			((IWorkingCopy)customizedCopy).destroy();
		}
	}
}
/**
 * Create the compilation unit place holder for the working copy tests.
 */
public void testCreation() throws JavaModelException {
	assertTrue("Failed to create X.java compilation unit", this.cu != null && this.cu.exists());
	assertTrue("Failed to create working copy on X.java", this.copy != null && this.copy.exists());
}

/**
 * Test creating a working copy with a customized buffer.
 */
public void testCustomizedBuffer() throws JavaModelException {
	IBufferFactory factory = new BufferFactory();
	IWorkingCopy customizedCopy = (IWorkingCopy)this.cu.getWorkingCopy(null, factory, null);
	try {
		assertTrue("Should be an IOpenable", customizedCopy instanceof IOpenable);
		assertTrue("Unexpected buffer", ((IOpenable)customizedCopy).getBuffer() instanceof Buffer);
	} finally {
		customizedCopy.destroy();
	}
}
/**
 * Test closing then reopening a working copy with a customized buffer.
 */
public void testCustomizedBuffer2() throws JavaModelException {
	IBufferFactory factory = new BufferFactory();
	IWorkingCopy customizedCopy = (IWorkingCopy)this.cu.getWorkingCopy(null, factory, null);
	try {
		assertTrue("Should be an IOpenable", customizedCopy instanceof IOpenable);
		IOpenable openableCopy = (IOpenable)customizedCopy;
		openableCopy.close();
		openableCopy.open(null);
		assertTrue("Unexpected buffer", openableCopy.getBuffer() instanceof Buffer);		
	} finally {
		customizedCopy.destroy();
	}
}
/*
 * Test that deleting 2 fields in a JavaCore.run() operation reports the correct delta.
 * (regression test for bug 32225 incorrect delta after deleting 2 fields)
 */
public void testDelete2Fields() throws CoreException {
	try {
		startDeltas();
		JavaCore.run(
			new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					IType type = copy.getType("A");
					IField field1 = type.getField("field1");
					IField field2 = type.getField("field2");
					field1.delete(false, monitor);
					field2.delete(false, monitor);
				}
			},
			null);
		assertDeltas(
			"Unexpected delta",
			"A[*]: {CHILDREN | FINE GRAINED}\n" + 
			"	field1[-]: {}\n" + 
			"	field2[-]: {}"
		);
	} finally {
		stopDeltas();
	}
}
/**
 * Tests the general functionality of a working copy:<ul>
 * <li>ensures that the copy and original compilation unit are not equal</li>
 * <li>ensures the correct retrieval of the original element</li>
 * <li>closing the package of the compilation unit does not close the copy</li>
 * <li>ensures that working copies are unique
 * <li>ensures committing changes from working copies 
 */
public void testGeneral() throws JavaModelException, CoreException, IOException {

	assertTrue("copy and actual should not be equal", !this.copy.equals(this.cu));

	IType copyType= this.copy.getType("A");

	assertTrue("can't get original from original", this.cu.getOriginalElement() == null);

	assertTrue("getting working copy from a copy should yield original copy", this.copy.getWorkingCopy() == this.copy);

	boolean ex= false;
	assertDeletion(copyType);

	// closing the package should not close the copy
	((IOpenable)this.cu.getParent()).close();
	assertTrue("copy should still be open", this.copy.isOpen());
	
	// verify original still present
	assertTrue("actual type should still be present", this.cu.getType("A").exists());

	// getAnother working copy
	ICompilationUnit copy2= (ICompilationUnit)this.cu.getWorkingCopy();
	try {
		assertTrue("working copies should be unique ", !(this.copy.equals(copy2)));
	
		// delete a method from the 2nd working copy.
		IMethod method= copy2.getType("A").getMethod("foo", null);
	
		assertDeletion(method);
		IMethod originalMethod= this.cu.getType("A").getMethod("foo", null);
		assertTrue("method should still be present in original", originalMethod.exists());
	
		// commit the changes from the 2nd copy.
		copy2.commit(false, null);
	
		assertTrue("copy always has unsaved changes", copy2.hasUnsavedChanges());
		
		// original method should now be gone
		assertTrue("method should no longer be present in original after commit", !originalMethod.exists());
	
		// commit the changes from the 1st copy - should fail
		try {
			this.copy.commit(false, null);
			assertTrue("commit should have failed", ex);
		} catch (JavaModelException jme) {
		}
		
	
		// now force the update
		try {
			this.copy.commit(true, null);
		} catch (JavaModelException jme) {
			assertTrue("commit should work", false);
		}
	
		// now the type should be gone.
		assertTrue("original type should no longer be present", !this.cu.getType("A").exists());
	
	
		this.copy.close();
		ex= false;
		try {
			this.copy.open(null);
		} catch (JavaModelException e) {
			ex= true;
		}
		assertTrue("should be able to open working copy a 2nd time", !ex);
	
		// now destroy the handle
		this.copy.destroy();
		ex= false;
		try {
			this.copy.open(null);
		} catch (JavaModelException e) {
			ex= true;
		}
		assertTrue("should not be able to open working copy again", ex);
	} finally {
		copy2.destroy();
	}
}
/**
 * Ensures that no original element can be retrieved on a binary element.
 * unit.
 */
public void testGetOriginalBinaryElement() throws CoreException {
	/* Evaluate the following in a scrapbook:
	 org.eclipse.jdt.tests.core.ModifyingResourceTests.generateClassFile(
		"A",
		"public class A {\n" +
		"}")
	*/
	byte[] bytes = new byte[] {
		-54, -2, -70, -66, 0, 3, 0, 45, 0, 10, 1, 0, 1, 65, 7, 0, 1, 1, 0, 16, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 7, 0, 3, 1, 0, 6, 60, 105, 110, 105, 116, 62, 1, 0, 3, 40, 41, 86, 1, 0, 4, 67, 111, 100, 101, 12, 0, 5, 0, 6, 10, 0, 4, 0, 8, 0, 33, 0, 2, 0, 4, 0, 0, 0, 
		0, 0, 1, 0, 1, 0, 5, 0, 6, 0, 1, 0, 7, 0, 0, 0, 17, 0, 1, 0, 1, 0, 0, 0, 5, 42, -73, 0, 9, -79, 0, 0, 0, 0, 0, 0, 
	};
	this.createFile("P/lib/A.class", new String(bytes));
	IClassFile cf = this.getClassFile("P/lib/A.class");
	IJavaElement original= this.copy.getOriginal(cf);
	assertTrue("Element should not be found", original == null);
}
/**
 * Ensures that the original cu can be retrieved.
 */
public void testGetOriginalCU() throws JavaModelException {
	IJavaElement original= this.copy.getOriginal(copy);
	assertTrue("Element is not a cu", original instanceof ICompilationUnit && !((ICompilationUnit)original).isWorkingCopy());
	assertTrue("Element should exist", original.exists());
}
/**
 * Ensures that getting the original element from a different compilation unit returns null.
 */
public void testGetOriginalElementNotInWorkingCopy() throws CoreException {
	ICompilationUnit copy2 = null;
	try {
		this.createFile(
			"P/src/x/y/B.java", 
			"package x.y;\n" +
			"public class B {\n" +
			"}");
		ICompilationUnit cu2 = this.getCompilationUnit("P/src/x/y/B.java");
		copy2 = (ICompilationUnit)cu2.getWorkingCopy();
	
		IPackageDeclaration pkg= copy2.getPackageDeclaration("x.y");
		IJavaElement original= this.copy.getOriginal(pkg);
		assertTrue("Element should not be found as from a different working copy", original == null);
	} finally {
		if (copy2 != null) copy2.destroy();
	}
}
/**
 * Ensures that the original field can be retrieved.
 */
public void testGetOriginalField() throws JavaModelException {
	IType type = this.copy.getType("A");
	IJavaElement original = this.copy.getOriginal(type.getField("FIELD"));
	assertTrue("Element is not a method", original instanceof IField && !((ICompilationUnit)original.getParent().getParent()).isWorkingCopy());
	assertTrue("Element should exist", original.exists());
}
/**
 * Ensures that the original import declaration can be retrieved.
 */
public void testGetOriginalImportDeclaration() throws JavaModelException {
	IImportDeclaration imprt = copy.getImport("java.io.File");
	IJavaElement original= this.copy.getOriginal(imprt);
	assertTrue("Element should exist", original.exists());
}
/**
 * Ensures that the original import container can be retrieved.
 */
public void testGetOriginalImportContainer() throws JavaModelException {
	IImportContainer container = this.copy.getImportContainer();
	IJavaElement original = this.copy.getOriginal(container);
	assertTrue("Element should not be null", original != null);
	assertTrue("Element should exist", original.exists());
}
/**
 * Ensures that the original initializer can be retrieved.
 */
public void testGetOriginalInitializer() throws JavaModelException {
	IType type= copy.getType("A");
	IJavaElement original= copy.getOriginal(type.getInitializer(1));
	assertTrue("Element should exist", original.exists());
}
/**
 */
public void testGetOriginalInnerField() throws JavaModelException {
	IType innerType = this.copy.getType("A").getType("Inner");
	IJavaElement original = this.copy.getOriginal(innerType.getField("innerField"));
	assertTrue("Element is not a field", original instanceof IField);
	assertTrue("Element should exist", original.exists());
}
/**
 */
public void testGetOriginalInnerMethod() throws JavaModelException {
	IType innerType = this.copy.getType("A").getType("Inner");
	IJavaElement original = copy.getOriginal(innerType.getMethods()[0]);
	assertTrue("Element is not a method", original instanceof IMethod);
	assertTrue("Element should exist", original.exists());
}
/**
 */
public void testGetOriginalInnerType() throws JavaModelException {
	IType innerInnerType = this.copy.getType("A").getType("Inner").getType("InnerInner");
	IJavaElement original = this.copy.getOriginal(innerInnerType);
	assertTrue("Element is not a method", original instanceof IType);
	assertTrue("Element should exist", original.exists());

	Vector hierarchy = new Vector(5);
	IJavaElement parent= original.getParent();
	while (parent.getElementType() > IJavaElement.COMPILATION_UNIT) {
		hierarchy.addElement(parent);
		parent = parent.getParent();
	}
	hierarchy.addElement(parent);
	assertTrue("Compilation Unit should not be a working copy", !((ICompilationUnit)hierarchy.lastElement()).isWorkingCopy());
}
/**
 * Ensures that the original method can be retrieved.
 */
public void testGetOriginalMethod() throws JavaModelException {
	IType type = this.copy.getType("A");
	IJavaElement original= copy.getOriginal(type.getMethods()[0]);
	assertTrue("Element is not a method", original instanceof IMethod);
	assertTrue("Element should exist", original.exists());
}
/**
 * Ensures that renaming a method of a working copy does
 * not alter the source of the original compilation
 * unit.
 */
public void testRenameMethod() throws JavaModelException {
	IType type = this.copy.getType("A");
	IMethod method = type.getMethods()[0];
	IJavaElement original= copy.getOriginal(method);
	method.rename("bar", false, null);
	assertEquals("Invalid name of working copy method", "bar", type.getMethods()[0].getElementName());
	assertEquals("Invalid name of original method", "foo", original.getElementName());
}
/**
 * Ensures that the original package declaration can be retrieved.
 */
public void testGetOriginalPackageDeclaration() throws JavaModelException {
	IPackageDeclaration pkg = this.copy.getPackageDeclaration("x.y");
	IJavaElement original = this.copy.getOriginal(pkg);
	assertTrue("Element should exist", original.exists());
}
/**
 * Ensures that the original type can be retrieved.
 */
public void testGetOriginalType() throws JavaModelException {
	IType type = this.copy.getType("A");
	IJavaElement original= copy.getOriginal(type);
	assertTrue("Element should exist", original.exists());
}
/**
 * Ensures that a type can be moved to another working copy.
 * (regression test for bug 7881 IType.move() clobbers editing buffer of destination element)
 */
public void testMoveTypeToAnotherWorkingCopy() throws CoreException {
	this.createFile(
		"P/src/x/y/B.java",
		"package x.y;\n" +
		"public class B {\n" +
		"}");
	ICompilationUnit cu2 = this.getCompilationUnit("P/src/x/y/B.java");
	ICompilationUnit copy2 = (ICompilationUnit)cu2.getWorkingCopy();
	try {
		IType classA = this.copy.getType("A");
		IType classB = copy2.getType("B");
		classA.move(classB, null, null, false, null);
		assertTrue("A should not exist", !classA.exists());
		assertTrue("B.A should exist", classB.getType("A").exists());
		assertTrue("Buffer for A should not be null", this.copy.getBuffer() != null);
		assertSourceEquals("Invalid content for A", 
			"package x.y;\n" +
			"import java.io.File;\n",
			this.copy.getBuffer().getContents());
		assertTrue("Buffer for B should not be null", copy2.getBuffer() != null);
		assertSourceEquals("Invalid content for B", 
			"package x.y;\n" +
			"public class B {\n" +
			"public class A {\n" +
			"  public class Inner {\n" +
			"    public class InnerInner {\n" +
			"    }\n" +
			"    int innerField;\n" +
			"    void innerMethod() {\n" +
			"    }\n" +
			"  }\n" +
			"  static String FIELD;\n" +
			"  {\n" +
			"    FIELD = File.pathSeparator;\n" +
			"  }\n" +
			"  int field1;\n" +
			"  boolean field2;\n" +
			"  public void foo() {\n" +
			"  }\n" +
			"}\n" +
			"}",
			copy2.getBuffer().getContents());
	} finally {
		copy2.destroy();
	}
}
/**
 * Test creating a shared working copy.
 */
public void testShared1() throws JavaModelException {
	IJavaElement shared = this.cu.getSharedWorkingCopy(null, null, null);
	try {
		assertTrue("Should be an IWorkingCopy", shared instanceof IWorkingCopy);
		assertTrue("Original element should have shared working copy", this.cu.findSharedWorkingCopy(null) == shared);
	} finally {
		if (shared instanceof IWorkingCopy) {
			((IWorkingCopy)shared).destroy();
		}
	}
	assertTrue("Original element should not have shared working copy", this.cu.findSharedWorkingCopy(null) == null);
}
/**
 * Test several call to creating shared working copy.
 */
public void testShared2() throws JavaModelException {
	IWorkingCopy shared = (IWorkingCopy)this.cu.getSharedWorkingCopy(null, null, null);
	try {
		IWorkingCopy shared2 = (IWorkingCopy)this.cu.getSharedWorkingCopy(null, null, null);
		assertTrue("Second working copy should be identical to first one", shared2 == shared);
	} finally {
		shared.destroy();
		try {
			assertTrue("Original element should still have shared working copy", this.cu.findSharedWorkingCopy(null) == shared);
		} finally {
			shared.destroy();
		}
	}
	assertTrue("Original element should not have shared working copy", this.cu.findSharedWorkingCopy(null) == null);
}
/**
 * Tests that multiple commits are possible with the same working copy.
 */
public void testMultipleCommit() throws JavaModelException, CoreException, IOException {

	// Add a method to the working copy
	IType gp = this.copy.getType("A");
	try {
		gp.createMethod("public void anotherMethod() {}\n",null, false, null);
	} catch (JavaModelException jme) {
		assertTrue("creation failed", false);
	}
	
	// commit the changes from the copy.
	try {
		this.copy.commit(false, null);
	} catch (JavaModelException t) {
		assertTrue("commit failed", false);
	}
	
	// new method added
	assertTrue("method should exist after commit", 
		this.cu.getType("A").getMethod("anotherMethod", new String[]{}).exists());

	//add another method
	try {
		gp.createMethod("public void anotherAnotherMethod() {}\n", null, false, null);
	} catch (JavaModelException x) {
		assertTrue("Creation failed 2", false);
	}

	//commit the new method
	try {
		this.copy.commit(false, null);
	} catch (JavaModelException t) {
		assertTrue("commit2 failed", false);
	}

	// new method added
	assertTrue("second method added should exist after commit", 
		this.cu.getType("A").getMethod("anotherAnotherMethod", new String[]{}).exists());
}
/**
 * Creates a working copy on a non-existing compilation unit.
 * (regression test for bug 8921  DCR - Need a way to create a working copy ignoring existing files)
 */
public void testNonExistingCU() throws JavaModelException {
	ICompilationUnit cu = this.getCompilationUnit("P/src/x/y/NonExisting.java");
	IWorkingCopy copy = null;
	try {
		// getBuffer()
		copy = (IWorkingCopy)cu.getWorkingCopy();
		assertSourceEquals("Buffer should be empty", "", ((IOpenable)copy).getBuffer().getContents());
		
		// exists()
		assertTrue("Working copy should exists", ((IJavaElement)copy).exists());
		
		// getCorrespondingResource()
		assertEquals("Corresponding resource should be null", null, ((IJavaElement)copy).getCorrespondingResource());
		
		// getOriginalElement()
		assertEquals("Unexpected orginal element", cu, copy.getOriginalElement());
		
		// getPath()
		assertEquals("Unexpected path", new Path("/P/src/x/y/NonExisting.java"), ((IJavaElement)copy).getPath());
		
		// getResource()
		assertEquals("Unexpected resource", null, ((IJavaElement)copy).getResource());
		
		// isConsistent()
		assertTrue("Working copy should be consistent", ((IOpenable)copy).isConsistent());
		
		// restore()
		boolean exception = false;
		try {
			copy.restore();
		} catch (JavaModelException e) {
			exception = true;
		}
		assertTrue("Should not be able to restore from original element", exception);
		
		// makeConsistent()
		((IOpenable)copy).getBuffer().setContents(
			"public class X {\n" +
			"}");
		assertTrue("Working copy should not be consistent", !((IOpenable)copy).isConsistent());
		((IOpenable)copy).makeConsistent(null);
		assertTrue("Working copy should be consistent", ((IOpenable)copy).isConsistent());
		
		// save()
		((IOpenable)copy).getBuffer().setContents(
			"public class Y {\n" +
			"}");
		((IOpenable)copy).save(null, false);
		assertTrue("Working copy should be consistent after save", ((IOpenable)copy).isConsistent());
		assertTrue("Original cu should not exist", !cu.exists());
		
		// commit()
		copy.commit(false, null);
		assertTrue("Original cu should exist", cu.exists());

		// isBasedOn()
		assertTrue("Working copy should not be based on original resource", !copy.isBasedOn(cu.getResource()));
		
	} finally {
		if (copy != null) {
			copy.destroy();
		}
		if (cu.exists()) {
			cu.delete(true, null);
		}
	}
}
/**
 * Tests the general functionality of a operations working with working copies:<ul>
 * <li>ensures that the copy cannot be renamed</li>
 * <li>ensures that the copy cannot be moved to the same location as the original cu</li>
 * <li>ensures that the copy can be copied to a different location as the original cu</li>
 */
public void testOperations() throws JavaModelException {
	// rename working copy
	boolean ex= false;
	try {
		this.copy.rename("someName.java", false, null);
	} catch (JavaModelException jme) {
		assertTrue("Incorrect status code for attempting to rename working copy", jme.getStatus().getCode() == IJavaModelStatusConstants.INVALID_ELEMENT_TYPES);
		ex= true;
	}
	assertTrue("renaming a working copy should fail", ex);
	
	// move to same location as original cu
	ex= false;
	try {
		this.copy.move((IPackageFragment)this.cu.getParent(), null, "someName.java", false, null);
	} catch (JavaModelException jme) {
		assertTrue("Incorrect status code for attempting to move working copy", jme.getStatus().getCode() == IJavaModelStatusConstants.INVALID_ELEMENT_TYPES);
		ex= true;
	}
	assertTrue("moving a working copy should fail", ex);

	// copy working copy to default package
	IPackageFragment pkg= getPackageFragment("P", "src", "");
	this.copy.copy(pkg, null, "someName.java", false, null);
	assertCreation(this.copy);	
}
}
