/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.team.core.RepositoryProvider;


public class WorkingCopyTests extends ModifyingResourceTests {
	ICompilationUnit cu = null;
	ICompilationUnit copy = null;

	public static class TestWorkingCopyOwner extends WorkingCopyOwner {
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			return new TestBuffer(workingCopy);
		}
	}
	public WorkingCopyTests(String name) {
	super(name);
}

public static Test suite() {
	return buildModelTestSuite(WorkingCopyTests.class);
}
@Override
protected void setUp() throws Exception {
	super.setUp();

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
		this.copy = this.cu.getWorkingCopy(null);
	} catch (CoreException e) {
		e.printStackTrace();
		throw new RuntimeException(e.getMessage());
	}
}
@Override
protected void tearDown() throws Exception {
	if (this.copy != null) this.copy.discardWorkingCopy();
	this.deleteProject("P");
	super.tearDown();
}
/*
 * Ensures that cancelling a make consistent operation doesn't leave the working copy closed
 * (regression test for bug 61719 Incorrect fine grain delta after method copy-rename)
 */
public void testCancelMakeConsistent() throws JavaModelException {
	String newContents =
		"package x.y;\n" +
		"public class A {\n" +
		"  public void bar() {\n" +
		"  }\n" +
		"}";
	this.copy.getBuffer().setContents(newContents);
	NullProgressMonitor monitor = new NullProgressMonitor();
	monitor.setCanceled(true);
	try {
		this.copy.makeConsistent(monitor);
	} catch (OperationCanceledException e) {
		// got exception
	}
	assertTrue("Working copy should be opened", this.copy.isOpen());
}

public void testChangeContent() throws CoreException {
	String newContents =
		"package x.y;\n" +
		"public class A {\n" +
		"  public void bar() {\n" +
		"  }\n" +
		"}";
	this.copy.getBuffer().setContents(newContents);
	this.copy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertSourceEquals(
		"Unexpected working copy contents",
		newContents,
		this.copy.getBuffer().getContents());

	this.copy.commitWorkingCopy(true, null);
	assertSourceEquals(
		"Unexpected original cu contents",
		newContents,
		this.cu.getBuffer().getContents());
}

/*
 * Ensures that one cannot commit the contents of a working copy on a read only cu.
 */
public void testChangeContentOfReadOnlyCU1() throws CoreException {
	if (!Util.isReadOnlySupported()) {
		// Do not test if file system does not support read-only attribute
		return;
	}
	IResource resource = this.cu.getUnderlyingResource();
	boolean readOnlyFlag = Util.isReadOnly(resource);
	boolean didComplain = false;
	try {
		Util.setReadOnly(resource, true);
		this.copy.getBuffer().setContents("invalid");
		this.copy.commitWorkingCopy(true, null);
	} catch(JavaModelException e){
		didComplain = true;
	} finally {
		Util.setReadOnly(resource, readOnlyFlag);
	}
	assertTrue("Should have complained about modifying a read-only unit:", didComplain);
	assertTrue("ReadOnly buffer got modified:", !this.cu.getBuffer().getContents().equals("invalid"));
}

/*
 * Ensures that one can commit the contents of a working copy on a read only cu if a pessimistic repository
 * provider allows it.
 */
public void testChangeContentOfReadOnlyCU2() throws CoreException {
	if (!Util.isReadOnlySupported()) {
		// Do not test if file system does not support read-only attribute
		return;
	}
	String newContents =
		"package x.y;\n" +
		"public class A {\n" +
		"  public void bar() {\n" +
		"  }\n" +
		"}";
	IResource resource = this.cu.getUnderlyingResource();
	IProject project = resource.getProject();
	boolean readOnlyFlag = Util.isReadOnly(resource);
	try {
		RepositoryProvider.map(project, TestPessimisticProvider.NATURE_ID);
		TestPessimisticProvider.markWritableOnSave = true;
		Util.setReadOnly(resource, true);

		this.copy.getBuffer().setContents(newContents);
		this.copy.commitWorkingCopy(true, null);
		assertSourceEquals(
			"Unexpected original cu contents",
			newContents,
			this.cu.getBuffer().getContents());
	} finally {
		TestPessimisticProvider.markWritableOnSave = false;
		RepositoryProvider.unmap(project);
		Util.setReadOnly(resource, readOnlyFlag);
	}
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
	assertSourceEquals("source code of copy should still be original", originalSource, this.copy.getSource());
}

/**
 * Test creating a working copy on a class file with a customized buffer.
 * As of https://bugs.eclipse.org/337935 this test is no longer valid.
 */
public void _testOnClassFile() throws JavaModelException, IOException {
	// ensure the external JCL is copied
	setupExternalJCL("jclMin");

	attachSource(this.getPackageFragmentRoot("P", this.getExternalJCLPathString()), this.getExternalJCLSourcePath().toOSString(), "src");
	IClassFile classFile = this.getClassFile("P", this.getExternalJCLPathString(), "java.lang", "Object.class");
	WorkingCopyOwner owner = new TestWorkingCopyOwner();
	ICompilationUnit customizedCopy = classFile.getWorkingCopy(owner, null);
	try {
		IBuffer buffer = customizedCopy.getBuffer();
		assertTrue("Unexpected buffer", buffer instanceof TestBuffer);
		assertTrue("Buffer should be initialized with source", buffer.getCharacters().length > 0);
	} finally {
		customizedCopy.discardWorkingCopy();
	}
}
/**
 * Create the compilation unit place holder for the working copy tests.
 */
public void testCreation() {
	assertTrue("Failed to create X.java compilation unit", this.cu != null && this.cu.exists());
	assertTrue("Failed to create working copy on X.java", this.copy != null && this.copy.exists());
}

/**
 * Test creating a working copy with a customized buffer.
 */
public void testCustomizedBuffer() throws JavaModelException {
	WorkingCopyOwner owner = new TestWorkingCopyOwner();
	ICompilationUnit customizedCopy = this.cu.getWorkingCopy(owner, null);
	try {
		assertTrue("Unexpected buffer", customizedCopy.getBuffer() instanceof TestBuffer);
	} finally {
		customizedCopy.discardWorkingCopy();
	}
}
/**
 * Test closing then reopening a working copy with a customized buffer.
 */
public void testCustomizedBuffer2() throws JavaModelException {
	WorkingCopyOwner owner = new TestWorkingCopyOwner();
	ICompilationUnit customizedCopy = this.cu.getWorkingCopy(owner, null);
	try {
		customizedCopy.close();
		customizedCopy.open(null);
		assertTrue("Unexpected buffer", customizedCopy.getBuffer() instanceof TestBuffer);
	} finally {
		customizedCopy.discardWorkingCopy();
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
					IType type = WorkingCopyTests.this.copy.getType("A");
					IField field1 = type.getField("field1");
					IField field2 = type.getField("field2");
					field1.delete(false, monitor);
					field2.delete(false, monitor);
				}
			},
			null);
		assertWorkingCopyDeltas(
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
 * </ul>
 */
public void testGeneral() throws JavaModelException, CoreException {

	assertTrue("copy and actual should not be equal", !this.copy.equals(this.cu));

	IType copyType= this.copy.getType("A");

	assertEquals("primary should be the samel", this.cu, this.cu.getPrimary());

	assertEquals("getting working copy from a copy should yield original copy", this.copy, this.copy.getWorkingCopy(null));

	boolean ex= false;
	assertDeletion(copyType);

	// closing the package should not close the copy
	((IOpenable)this.cu.getParent()).close();
	assertTrue("copy should still be open", this.copy.isOpen());

	// verify original still present
	assertTrue("actual type should still be present", this.cu.getType("A").exists());

	// getAnother working copy
	ICompilationUnit copy2= this.cu.getWorkingCopy(null);
	try {
		assertTrue("working copies should be unique ", !(this.copy.equals(copy2)));

		// delete a method from the 2nd working copy.
		IMethod method= copy2.getType("A").getMethod("foo", null);

		assertDeletion(method);
		IMethod originalMethod= this.cu.getType("A").getMethod("foo", null);
		assertTrue("method should still be present in original", originalMethod.exists());

		// commit the changes from the 2nd copy.
		copy2.commitWorkingCopy(false, null);

		assertTrue("copy always has unsaved changes", copy2.hasUnsavedChanges());

		// original method should now be gone
		assertTrue("method should no longer be present in original after commit", !originalMethod.exists());

		// commit the changes from the 1st copy - should fail
		try {
			this.copy.commitWorkingCopy(false, null);
			assertTrue("commit should have failed", ex);
		} catch (JavaModelException jme) {
		}


		// now force the update
		try {
			this.copy.commitWorkingCopy(true, null);
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

		// now discard the working copy
		this.copy.discardWorkingCopy();
		ex= false;
		try {
			this.copy.open(null);
		} catch (JavaModelException e) {
			ex= true;
		}
		assertTrue("should not be able to open working copy again", ex);
	} finally {
		copy2.discardWorkingCopy();
	}
}
/**
 * Ensures that the primary element of a binary element is itself.
 */
public void testGetPrimaryBinaryElement() throws CoreException {
	/* Evaluate the following in a scrapbook:
	 org.eclipse.jdt.core.tests.model.ModifyingResourceTests.generateClassFile(
		"A",
		"public class A {\n" +
		"}")
	*/
	byte[] bytes = new byte[] {
		-54, -2, -70, -66, 0, 3, 0, 45, 0, 10, 1, 0, 1, 65, 7, 0, 1, 1, 0, 16, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 7, 0, 3, 1, 0, 6, 60, 105, 110, 105, 116, 62, 1, 0, 3, 40, 41, 86, 1, 0, 4, 67, 111, 100, 101, 12, 0, 5, 0, 6, 10, 0, 4, 0, 8, 0, 33, 0, 2, 0, 4, 0, 0, 0,
		0, 0, 1, 0, 1, 0, 5, 0, 6, 0, 1, 0, 7, 0, 0, 0, 17, 0, 1, 0, 1, 0, 0, 0, 5, 42, -73, 0, 9, -79, 0, 0, 0, 0, 0, 0,
	};
	this.createFile("P/lib/A.class", bytes);
	IClassFile cf = this.getClassFile("P/lib/A.class");
	IJavaElement primary = cf.getPrimaryElement();
	assertEquals("Primary element should be the same", cf, primary);
}
/**
 * Ensures that the primary cu can be retrieved.
 */
public void testGetPrimaryCU() {
	IJavaElement primary= this.copy.getPrimaryElement();
	assertTrue("Element is not a cu", primary instanceof ICompilationUnit && !((ICompilationUnit)primary).isWorkingCopy());
	assertTrue("Element should exist", primary.exists());
}
/**
 * Ensures that the primary field can be retrieved.
 */
public void testGetPrimaryField() {
	IType type = this.copy.getType("A");
	IJavaElement primary = type.getField("FIELD").getPrimaryElement();
	assertTrue("Element is not a field", primary instanceof IField && !((ICompilationUnit)primary.getParent().getParent()).isWorkingCopy());
	assertTrue("Element should exist", primary.exists());
}
/**
 * Ensures that the primary import declaration can be retrieved.
 */
public void testGetPrimaryImportDeclaration()  {
	IImportDeclaration imprt = this.copy.getImport("java.io.File");
	IJavaElement primary = imprt.getPrimaryElement();
	assertTrue("Element should exist", primary.exists());
}
/**
 * Ensures that the primary import container can be retrieved.
 */
public void testGetPrimaryImportContainer() {
	IImportContainer container = this.copy.getImportContainer();
	IJavaElement primary = container.getPrimaryElement();
	assertTrue("Element should not be null", primary != null);
	assertTrue("Element should exist", primary.exists());
}
/**
 * Ensures that the primary initializer can be retrieved.
 */
public void testGetPrimaryInitializer() {
	IType type= this.copy.getType("A");
	IJavaElement primary= type.getInitializer(1).getPrimaryElement();
	assertTrue("Element should exist", primary.exists());
}
public void testGetPrimaryInnerField() {
	IType innerType = this.copy.getType("A").getType("Inner");
	IJavaElement primary = innerType.getField("innerField").getPrimaryElement();
	assertTrue("Element is not a field", primary instanceof IField);
	assertTrue("Element should exist", primary.exists());
}
public void testGetPrimaryInnerMethod() throws JavaModelException {
	IType innerType = this.copy.getType("A").getType("Inner");
	IJavaElement primary = innerType.getMethods()[0].getPrimaryElement();
	assertTrue("Element is not a method", primary instanceof IMethod);
	assertTrue("Element should exist", primary.exists());
}
public void testGetPrimaryInnerType() {
	IType innerInnerType = this.copy.getType("A").getType("Inner").getType("InnerInner");
	IJavaElement primary = innerInnerType.getPrimaryElement();
	assertTrue("Element is not a method", primary instanceof IType);
	assertTrue("Element should exist", primary.exists());

	List<IJavaElement> hierarchy = new ArrayList<>(5);
	IJavaElement parent= primary.getParent();
	while (parent.getElementType() > IJavaElement.COMPILATION_UNIT) {
		hierarchy.add(parent);
		parent = parent.getParent();
	}
	hierarchy.add(parent);
	assertTrue("Compilation Unit should not be a working copy", !((ICompilationUnit)hierarchy.get(hierarchy.size() - 1)).isWorkingCopy());
}
/**
 * Ensures that the primary method can be retrieved.
 */
public void testGetPrimaryMethod() throws JavaModelException {
	IType type = this.copy.getType("A");
	IJavaElement primary= type.getMethods()[0].getPrimaryElement();
	assertTrue("Element is not a method", primary instanceof IMethod);
	assertTrue("Element should exist", primary.exists());
}
/**
 * Ensures that renaming a method of a working copy does
 * not alter the source of the primary compilation
 * unit.
 */
public void testRenameMethod() throws JavaModelException {
	IType type = this.copy.getType("A");
	IMethod method = type.getMethods()[0];
	IJavaElement primary= method.getPrimaryElement();
	method.rename("bar", false, null);
	assertEquals("Invalid name of working copy method", "bar", type.getMethods()[0].getElementName());
	assertEquals("Invalid name of primary method", "foo", primary.getElementName());
}
/**
 * Ensures that the primary package declaration can be retrieved.
 */
public void testGetPrimaryPackageDeclaration() {
	IPackageDeclaration pkg = this.copy.getPackageDeclaration("x.y");
	IJavaElement primary = pkg.getPrimaryElement();
	assertTrue("Element should exist", primary.exists());
}
/**
 * Ensures that the primary type can be retrieved.
 */
public void testGetPrimaryType() {
	IType type = this.copy.getType("A");
	IJavaElement primary= type.getPrimaryElement();
	assertTrue("Element should exist", primary.exists());
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
	ICompilationUnit copy2 = cu2.getWorkingCopy(null);
	try {
		IType classA = this.copy.getType("A");
		IType classB = copy2.getType("B");
		classA.move(classB, null, null, false, null);
		assertTrue("A should not exist", !classA.exists());
		assertTrue("B.A should exist", classB.getType("A").exists());
		assertTrue("Buffer for A should not be null", this.copy.getBuffer() != null);
		assertSourceEquals("Invalid content for A",
			"package x.y;\n" +
			"import java.io.File;",
			this.copy.getBuffer().getContents());
		assertTrue("Buffer for B should not be null", copy2.getBuffer() != null);
		assertSourceEquals("Invalid content for B",
			"package x.y;\n" +
			"public class B {\n" +
			"\n" +
			"	public class A {\n" +
			"	  public class Inner {\n" +
			"	    public class InnerInner {\n" +
			"	    }\n" +
			"	    int innerField;\n" +
			"	    void innerMethod() {\n" +
			"	    }\n" +
			"	  }\n" +
			"	  static String FIELD;\n" +
			"	  {\n" +
			"	    FIELD = File.pathSeparator;\n" +
			"	  }\n" +
			"	  int field1;\n" +
			"	  boolean field2;\n" +
			"	  public void foo() {\n" +
			"	  }\n" +
			"	}\n" +
			"}",
			copy2.getBuffer().getContents());
	} finally {
		copy2.discardWorkingCopy();
	}
}
/**
 * Test creating a shared working copy.
 */
public void testShared1() throws JavaModelException {
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	ICompilationUnit shared = this.cu.getWorkingCopy(owner, null);
	try {
		assertTrue("Should find shared working copy", this.cu.findWorkingCopy(owner) == shared);
	} finally {
		shared.discardWorkingCopy();
	}
	assertTrue("Should not find cu with same owner", this.cu.findWorkingCopy(owner) == null);
}
/**
 * Test several call to creating shared working copy.
 */
public void testShared2() throws JavaModelException {
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	ICompilationUnit shared = this.cu.getWorkingCopy(owner, null);
	try {
		ICompilationUnit shared2 = this.cu.getWorkingCopy(owner, null);
		assertTrue("Second working copy should be identical to first one", shared2 == shared);
	} finally {
		shared.discardWorkingCopy();
		try {
			assertTrue("Should find shared working copy", this.cu.findWorkingCopy(owner) == shared);
		} finally {
			shared.discardWorkingCopy();
		}
	}
	assertTrue("Should not find cu with same owner", this.cu.findWorkingCopy(owner) == null);
}
/**
 * Tests that multiple commits are possible with the same working copy.
 */
public void testMultipleCommit() {

	// Add a method to the working copy
	IType gp = this.copy.getType("A");
	try {
		gp.createMethod("public void anotherMethod() {}\n",null, false, null);
	} catch (JavaModelException jme) {
		assertTrue("creation failed", false);
	}

	// commit the changes from the copy.
	try {
		this.copy.commitWorkingCopy(false, null);
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
		this.copy.commitWorkingCopy(false, null);
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
	ICompilationUnit nonExistingCU = this.getCompilationUnit("P/src/x/y/NonExisting.java");
	ICompilationUnit workingCopy = null;
	try {
		// getBuffer()
		workingCopy = nonExistingCU.getWorkingCopy(null);
		assertSourceEquals("Buffer should be empty", "", workingCopy.getBuffer().getContents());

		// exists()
		assertTrue("Working copy should exists", workingCopy.exists());

		// getCorrespondingResource()
		assertEquals("Corresponding resource should be null", null, workingCopy.getCorrespondingResource());

		// getPrimaryElement()
		assertEquals("Unexpected orginal element", nonExistingCU, workingCopy.getPrimaryElement());

		// getPath()
		assertEquals("Unexpected path", new Path("/P/src/x/y/NonExisting.java"), workingCopy.getPath());

		// getResource()
		assertEquals("Unexpected resource", nonExistingCU.getResource(), workingCopy.getResource());

		// isConsistent()
		assertTrue("Working copy should be consistent", workingCopy.isConsistent());

		// restore()
		boolean exception = false;
		try {
			workingCopy.restore();
		} catch (JavaModelException e) {
			exception = true;
		}
		assertTrue("Should not be able to restore from primary element", exception);

		// makeConsistent()
		workingCopy.getBuffer().setContents(
			"public class X {\n" +
			"}");
		assertTrue("Working copy should not be consistent", !workingCopy.isConsistent());
		workingCopy.makeConsistent(null);
		assertTrue("Working copy should be consistent", workingCopy.isConsistent());

		// save()
		workingCopy.getBuffer().setContents(
			"public class Y {\n" +
			"}");
		workingCopy.save(null, false);
		assertTrue("Working copy should be consistent after save", workingCopy.isConsistent());
		assertTrue("Primary cu should not exist", !nonExistingCU.exists());

		// commitWorkingCopy()
		workingCopy.commitWorkingCopy(false, null);
		assertTrue("Primary cu should exist", nonExistingCU.exists());

		// hasResourceChanged()
		assertTrue("Working copy's resource should now not mark as changed", !workingCopy.hasResourceChanged());

	} finally {
		if (workingCopy != null) {
			workingCopy.discardWorkingCopy();
		}
		if (nonExistingCU.exists()) {
			nonExistingCU.delete(true, null);
		}
	}
}
/**
 * Tests the general functionality of a operations working with working copies:<ul>
 * <li>ensures that the copy cannot be renamed</li>
 * <li>ensures that the copy cannot be moved to the same location as the primary cu</li>
 * <li>ensures that the copy can be copied to a different location as the primary cu</li>
 * </ul>
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

	// move to same location as primary cu
	ex= false;
	try {
		this.copy.move(this.cu.getParent(), null, "someName.java", false, null);
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
