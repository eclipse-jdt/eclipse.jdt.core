/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
 *     Vladimir Piskarev <pisv@1c.ru> - F_CONTENT sometimes lost when merging deltas - https://bugs.eclipse.org/520336
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class RenameTests extends CopyMoveTests {
	ICompilationUnit cu;
public RenameTests(String name) {
	super(name);
}
/**
 * Attempts to rename the elements with optional
 * forcing. The operation should fail with the failure code.
 */
public void renameNegative(IJavaElement[] elements ,String[] renamings, boolean force, int failureCode) {
	try {
		//rename
	 	getJavaModel().rename(elements, new IJavaElement[]{elements[0].getParent()}, renamings, force, null);
	} catch (JavaModelException jme) {
		assertTrue("Code not correct for JavaModelException: "  + jme, jme.getStatus().getCode() == failureCode);
		return;
	}
	assertTrue("The rename should have failed for multiple renaming", false);
	return;
}
/**
 * Attempts to rename the element with optional
 * forcing. The operation should fail with the failure code.
 */
public void renameNegative(IJavaElement element, String rename, boolean force, int failureCode) {
	try {
		//rename
		getJavaModel().rename(new IJavaElement[] {element}, new IJavaElement[] {element.getParent()}, new String[] {rename}, force, null);
	} catch (JavaModelException jme) {
		assertTrue("Code not correct for JavaModelException: " + jme, jme.getStatus().getCode() == failureCode);
		return;
	}
	assertTrue("The rename should have failed for: " + element, false);
	return;
}
/**
 * Renames the element to the container with optional
 * forcing. The operation should succeed, so any exceptions
 * encountered are thrown.
 */
public void renamePositive(IJavaElement[] elements, String[] names, boolean force) throws JavaModelException {
	renamePositive(elements, new IJavaElement[]{elements[0].getParent()}, names, force);
}
/**
 * Renames the element to the container with optional
 * forcing. The operation should succeed, so any exceptions
 * encountered are thrown.
 */
public void renamePositive(IJavaElement[] elements, IJavaElement[] destinations, String[] names, boolean force) throws JavaModelException {
	renamePositive(elements, destinations, names, force, false);
}
/**
 * Renames the element to the container with optional
 * forcing. The operation should succeed, so any exceptions
 * encountered are thrown.
 */
public void renamePositive(IJavaElement[] elements, IJavaElement[] destinations, String[] names, boolean force, boolean originalShouldExist) throws JavaModelException {
	renamePositive(elements, destinations, names, force, originalShouldExist, null);
}
/**
 * Renames the element to the container with optional
 * forcing. The operation should succeed, so any exceptions
 * encountered are thrown.
 */
public void renamePositive(IJavaElement[] elements, IJavaElement[] destinations, String[] names, boolean force, boolean originalShouldExist, IProgressMonitor monitor) throws JavaModelException {
	// if forcing, ensure that a name collision exists
	int i;
	if (force) {
		for (i = 0; i < elements.length; i++) {
			IJavaElement e = elements[i];
			IJavaElement collision = generateHandle(e, names[i], e.getParent());
			assertTrue("Collision does not exist", collision.exists());
		}
	}

	// rename
	getJavaModel().rename(elements, destinations, names, force, monitor);
	for (i = 0; i < elements.length; i++) {
		// generate the new element	handle
		IJavaElement e = elements[i];
		IJavaElement renamed = generateHandle(e, names[i], e.getParent());
		assertTrue("Renamed element should exist", renamed.exists());
		if (!originalShouldExist) {
			assertTrue("Original element should not exist", !e.exists());
		}
		IJavaElementDelta destDelta = this.deltaListener.getDeltaFor(renamed.getParent());
		if (isMainType(e, e.getParent())) {
			assertTrue("Renamed compilation unit as result of main type not added", destDelta != null && destDelta.getKind() == IJavaElementDelta.ADDED);
			assertTrue("Added children not correct for element copy", destDelta.getElement().equals(renamed.getParent()));
			assertTrue("flag should be F_MOVED_FROM", (destDelta.getFlags() & IJavaElementDelta.F_MOVED_FROM) > 0);
			assertTrue("moved from handle should be original", destDelta.getMovedFromElement().equals(e.getParent()));
		} else {
			assertTrue("Destination container not changed", destDelta != null && deltaChildrenChanged(destDelta));
			IJavaElementDelta[] deltas = force ? destDelta.getChangedChildren() : destDelta.getAddedChildren();
			assertTrue("Added children not correct for element rename", deltas.length > i && deltas[i].getElement().equals(renamed));
			assertTrue("kind should be K_ADDED", deltas[i].getKind() == (force? IJavaElementDelta.CHANGED : IJavaElementDelta.ADDED));
			deltas = destDelta.getRemovedChildren();
			assertTrue("Removed children not correct for element rename", deltas.length > i && deltas[i].getElement().equals(e));
			assertTrue("kind should be K_REMOVED", deltas[i].getKind() == IJavaElementDelta.REMOVED);
		}
	}
}
/**
 * Renames the element to the container with optional
 * forcing. The operation should succeed, so any exceptions
 * encountered are thrown.
 */
public void renamePositive(IJavaElement element, String rename, boolean force) throws JavaModelException {
	renamePositive(new IJavaElement[] {element}, new String[] {rename}, force);
}
/**
 * Setup for the next test.
 */
@Override
public void setUp() throws Exception {
	super.setUp();

	this.createJavaProject("P", new String[] {"src"}, "bin");
	this.createFile(
		"/P/src/X.java",
		"public class X {\n" +
		"  boolean other;\n" +
		"  int bar;\n" +
		"  {\n" +
		"    bar = 1;\n" +
		"  }\n" +
		"  X(int i) {\n" +
		"  }\n" +
		"  void otherMethod(String s) {\n" +
		"  }\n" +
		"  void foo(String s) {\n" +
		"  }\n" +
		"}"
	);
	this.cu = getCompilationUnit("/P/src/X.java");

	startDeltas();
}
@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();

	IJavaProject project = this.createJavaProject("BinaryProject", new String[] {"src"}, new String[] {"JCL18_LIB"}, "lib");
	this.createFile(
		"/BinaryProject/src/X.java",
		"public class X {\n" +
		"  int bar;\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}"
	);
	project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
	waitForAutoBuild();
	project.setRawClasspath(
		new IClasspathEntry[] {
			JavaCore.newLibraryEntry(new Path("/BinaryProject/lib"), null, null)
		},
		null
	);
}
// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
	// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		TESTS_PREFIX = "testCombineAccessRestrictions";
//		TESTS_NAMES = new String[] {"testRenameFieldFragment"};
//		TESTS_NUMBERS = new int[] { 5, 6 };
//		TESTS_RANGE = new int[] { 21, 38 };
}
public static Test suite() {
	return buildModelTestSuite(RenameTests.class);
}
/**
 * Cleanup after the previous test.
 */
@Override
public void tearDown() throws Exception {
	stopDeltas();
	this.deleteProject("P");

	super.tearDown();
}
@Override
public void tearDownSuite() throws Exception {
	this.deleteProject("BinaryProject");
	super.tearDownSuite();
}
/**
 * Ensures that a binary field cannot be renamed.
 */
public void testRenameBinaryField() throws JavaModelException {
	IOrdinaryClassFile cf = getClassFile("BinaryProject", "lib", "", "X.class");
	IField binaryField = cf.getType().getField("bar");
	renameNegative(binaryField, "fred", false, IJavaModelStatusConstants.READ_ONLY);
}
/**
 * Ensures that a binary method cannot be renamed.
 */
public void testRenameBinaryMethod() throws JavaModelException {
	IOrdinaryClassFile cf = getClassFile("BinaryProject", "lib", "", "X.class");
	IMethod binaryMethod = cf.getType().getMethods()[0];
	renameNegative(binaryMethod, "fred", false, IJavaModelStatusConstants.READ_ONLY);
}
/**
 * Ensures that a binary type cannot be renamed.
 */
public void testRenameBinaryType() throws JavaModelException {
	IOrdinaryClassFile cf = getClassFile("BinaryProject", "lib", "", "X.class");
	IType binaryType = cf.getType();
	renameNegative(binaryType, "Y", false, IJavaModelStatusConstants.READ_ONLY);
}

/**
 * This operation should fail as renaming a CU and a CU member at the
 * same time is not supported.
 */
public void testRenameCompilationUnitAndType() {
	renameNegative(
		new IJavaElement[] {this.cu, this.cu.getType("X")},
		new String[]{"Y.java", "Y"},
		false,
		IJavaModelStatusConstants.INVALID_ELEMENT_TYPES);
}
/**
 * Ensures that compilation units cannot be renamed to an existing cu name
 * unless the force flag is set.
 */
public void testRenameCompilationUnitResultingInCollision() throws CoreException{
	this.createFile(
		"/P/src/Y.java",
		"public class Y {\n" +
		"}"
	);

	renameNegative(this.cu, "Y.java", false, IJavaModelStatusConstants.NAME_COLLISION);
	renamePositive(this.cu, "Y.java", true);
}
/**
 * Ensures that compilation units can be renamed.
 * Verifies that the proper change deltas are generated as a side effect
 * of running the operation.
 */
public void testRenameCompilationUnitsCheckingDeltas() throws CoreException{
	this.createFile(
		"/P/src/Y.java",
		"public class Y {\n" +
		"}"
	);
	ICompilationUnit cu2 = this.getCompilationUnit("/P/src/Y.java");

	clearDeltas();
	renamePositive(
		new ICompilationUnit[] {this.cu, cu2},
		new String[] {"NewX.java", "NewY.java"},
		false);

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN}\n" +
		"		<default>[*]: {CHILDREN}\n" +
		"			NewX.java[+]: {MOVED_FROM(X.java [in <default> [in src [in P]]])}\n" +
		"			NewY.java[+]: {MOVED_FROM(Y.java [in <default> [in src [in P]]])}\n" +
		"			X.java[-]: {MOVED_TO(NewX.java [in <default> [in src [in P]]])}\n" +
		"			Y.java[-]: {MOVED_TO(NewY.java [in <default> [in src [in P]]])}"
	);
}
/**
 * Ensures that a compilation unit cannot be renamed to an invalid name.
 * The new name for a cu must end in .java and be a valid Java identifier.
 */
public void testRenameCompilationUnitWithInvalidName() {
	renameNegative(this.cu, "NewX", false, IJavaModelStatusConstants.INVALID_NAME);
	renameNegative(this.cu, "New X.java", false, IJavaModelStatusConstants.INVALID_NAME);
}
/**
 * This operation should fail as renaming a CU with a null name should throw
 * an <code>IllegalArgumentException</code>
 */
public void testRenameCompilationUnitWithNull() throws JavaModelException {
	try {
		this.cu.rename(null, false, null);
	} catch (IllegalArgumentException iae) {
		return;
	}
	assertTrue("Should not be able to rename a cu with a null name", false);
}
/**
 * Ensures that a construtor cannot be renamed using the
 * <code>RenameElementsOperation</code>.
 */
public void testRenameConstructor() {
	IMethod constructor = this.cu.getType("X").getMethod("X",  new String[] {"I"});
	renameNegative(constructor, "newName", false, IJavaModelStatusConstants.NAME_COLLISION);
}

public void testRenameCU1() throws CoreException {
	this.cu.rename("NewX.java", false, null);
	assertTrue("Original CU should not exist", !this.cu.exists());

	ICompilationUnit newCU = getCompilationUnit("/P/src/NewX.java");
	assertTrue("New CU should exist", newCU.exists());

	assertTypesEqual(
		"Unexpected types",
		"NewX\n",
		newCU.getAllTypes());

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN}\n" +
		"		<default>[*]: {CHILDREN}\n" +
		"			NewX.java[+]: {MOVED_FROM(X.java [in <default> [in src [in P]]])}\n" +
		"			X.java[-]: {MOVED_TO(NewX.java [in <default> [in src [in P]]])}"
	);
}
public void testRenameCU2() throws CoreException {
	this.createFile(
		"/P/src/Y.java",
		"public class Y {\n" +
		"}"
	);

	clearDeltas();
	this.cu.rename("Y.java", true, null);

	IFile file = (IFile)this.cu.getResource();
	ICompilationUnit destCU = getCompilationUnit("/P/src/Y.java");
	IFile destFile = (IFile)destCU.getResource();

	assertTrue("Original CU should not exist", !this.cu.exists());
	assertTrue("Original file should not exist", !file.exists());
	assertTrue("Destination CU should exist", destCU.exists());
	assertTrue("Destination file should exist", destFile.exists());

	assertTypesEqual(
		"Unexpected types",
		"Y\n",
		destCU.getAllTypes());

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
				"	src[*]: {CHILDREN}\n" +
				"		<default>[*]: {CHILDREN}\n" +
				"			X.java[-]: {MOVED_TO(Y.java [in <default> [in src [in P]]])}\n" +
				"			Y.java[*]: {CHILDREN | CONTENT | FINE GRAINED | PRIMARY RESOURCE}\n" +
				"				Y[+]: {MOVED_FROM(X [in X.java [in <default> [in src [in P]]]])}"
	);
}
/*
 * Ensures that the correct scheduling rule is used when running ICompilationUnit.rename(...)
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=142990 )
 */
public void testRenameCU3() throws Exception {
	IWorkspaceRunnable runnable = new IWorkspaceRunnable(){
		public void run(IProgressMonitor monitor) throws CoreException {
			RenameTests.this.cu.rename("NewX.java", false, null);
			assertTrue("Original CU should not exist", !RenameTests.this.cu.exists());
		}
	};
	getWorkspace().run(runnable, getFolder("/P/src"), IResource.NONE, null);
}
/*
 * Ensures that the correct scheduling rule is used when running ICompilationUnit.rename(...)
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=142990 )
 */
public void testRenameCU4() throws Exception {
	createFolder("/P/src/p1");
	createFile("/P/src/p1/X.java", "package p1; public class X {}");
	IWorkspaceRunnable runnable = new IWorkspaceRunnable(){
		public void run(IProgressMonitor monitor) throws CoreException {
			ICompilationUnit x = getCompilationUnit("/P/src/p1/X.java");
			x.rename("NewX.java", false, null);
			assertTrue("Original CU should not exist", !x.exists());
		}
	};
	getWorkspace().run(runnable, getFolder("/P/src/p1"), IResource.NONE, null);
}
/*
 * Ensures that renaming an empty package fragment doesn't make it disappear
 * (regression test for bug 24505 Refactoring an empty package makes it disappears)
 */
public void testRenameEmptyPF() throws CoreException {
	this.createFolder("/P/src/x/y/z");

	clearDeltas();
	getPackage("/P/src/x/y/z").rename("x.y", false, null);

	IPackageFragment newFrag = getPackage("/P/src/x/y");
	assertTrue("New package should exist", newFrag.exists());
	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN}\n" +
		"		x.y.z[-]: {MOVED_TO(x.y [in src [in P]])}\n" +
		"		x.y[+]: {MOVED_FROM(x.y.z [in src [in P]])}"
	);
}
/*
 * Ensures that renaming an enum also renames the constructors of this enum.
 * (regression test for bug 83593 Rename of enum type does not update constructor / throws ClassCastException)
 */
public void testRenameEnum() throws CoreException {
	try {
		createJavaProject("P15", new String[] {""}, new String[] {"JCL18_LIB"}, "", CompilerOptions.getFirstSupportedJavaVersion());
		createFile(
			"/P15/En.java",
			"public enum En {\n" +
			"  ;\n" +
			"  En() {\n" +
			"  }\n" +
			"}"
		);
		ICompilationUnit enumCU = getCompilationUnit("/P15/En.java");
		enumCU.rename("OtherEnum.java", false, null);
		ICompilationUnit renamedCu = getCompilationUnit("/P15/OtherEnum.java");
		assertSourceEquals(
			"Unexpected source after rename",
			"public enum OtherEnum {\n" +
			"  ;\n" +
			"  OtherEnum() {\n" +
			"  }\n" +
			"}",
			renamedCu.getSource()
		);
	} finally {
		deleteProject("P15");
	}
}
/*
 * Ensures that renaming an enum that contain an enum constant with a body doesn't throw a ClassCastExeption.
 * (regression test for bug 83593 Rename of enum type does not update constructor / throws ClassCastException)
 */
public void testRenameEnum2() throws CoreException {
	try {
		createJavaProject("P15", new String[] {""}, new String[] {"JCL18_LIB"}, "", CompilerOptions.getFirstSupportedJavaVersion());
		createFile(
			"/P15/En.java",
			"public enum En {\n" +
			"  CONST() {\n" +
			"  }\n" +
			"}"
		);
		ICompilationUnit enumCU = getCompilationUnit("/P15/En.java");
		enumCU.rename("OtherEnum.java", false, null);
		ICompilationUnit renamedCu = getCompilationUnit("/P15/OtherEnum.java");
		assertSourceEquals(
			"Unexpected source after rename",
			"public enum OtherEnum {\n" +
			"  CONST() {\n" +
			"  }\n" +
			"}",
			renamedCu.getSource()
		);
	} finally {
		deleteProject("P15");
	}
}
/**
 * Ensures that fields can be renamed.
 * Verifies that the proper change deltas are generated as a side effect
 * of running the operation. As well ensures that the fields are
 * positioned in the same location as before the rename.
 */
public void testRenameFieldsCheckingDeltasAndPositions() throws JavaModelException {
	IType type = this.cu.getType("X");
	IField field = type.getField("bar");
	renamePositive(field, "fred", false);

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN}\n" +
		"		<default>[*]: {CHILDREN}\n" +
		"			X.java[*]: {CHILDREN | FINE GRAINED | PRIMARY RESOURCE}\n" +
		"				X[*]: {CHILDREN | FINE GRAINED}\n" +
		"					bar[-]: {}\n" +
		"					fred[+]: {}"
	);
	ensureCorrectPositioning(type, type.getField("fred"), type.getField("other"));
}
/*
 * Ensures that renaming a field fragment renames the right fragment
 * (regression test for bug 121076 Wrong field gets renamed)
 */
public void testRenameFieldFragment() throws Exception {
     this.createFile(
            "/P/src/Y.java",
            "public class Y {\n" +
            "  int int1, int2, int3;\n" +
            "}"
    );
    ICompilationUnit c = getCompilationUnit("/P/src/Y.java");
    IType type = c.getType("Y");
    IField field = type.getField("int2");
    renamePositive(field, "int2_renamed", false);
}
/**
 * Ensures that fields can be renamed even if one of the renamings fails.
 */
public void testRenameFieldsMultiStatus() throws CoreException {
	String addition = "multiStatus";
	IType type = this.cu.getType("X");
	IField[] iFields = type.getFields();
	String[] newNames = new String[iFields.length];
	int i;
	for (i = 0; i < iFields.length; i++) {
		IField f = iFields[i];
		newNames[i] = addition + f.getElementName();
	}
	newNames[1] = ";"; //invalid name

	boolean e = false;
	try {
		type.getJavaModel().rename(iFields, new IJavaElement[] {type}, newNames, false, null);
	} catch (JavaModelException jme) {
		assertTrue("Should not be multistatus (only one failure)", !jme.getStatus().isMultiStatus());
		assertTrue("Should be an invalid destination", jme.getStatus().getCode() == IJavaModelStatusConstants.INVALID_NAME);
		e = true;
	}
	assertTrue("Should have been an exception", e);

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN}\n" +
		"		<default>[*]: {CHILDREN}\n" +
		"			X.java[*]: {CHILDREN | FINE GRAINED | PRIMARY RESOURCE}\n" +
		"				X[*]: {CHILDREN | FINE GRAINED}\n" +
		"					multiStatusother[+]: {}\n" +
		"					other[-]: {}"
	);

	IJavaElement copy = generateHandle(iFields[0], newNames[0], type);
	assertTrue("Copy should exist", copy.exists());
}
/**
 * Ensures that fields cannot be renamed to the same name.
 */
public void testRenameFieldsResultingInCollision() throws JavaModelException {
	String addition = "new";
	IType type= this.cu.getType("X");
	IField[] iFields = type.getFields();
	String[] newNames = new String[iFields.length];
	int i;
	for (i = 0; i < iFields.length; i++) {
		IField f = iFields[i];
		newNames[i] = addition + f.getElementName();
	}
	//set two fields to have the same new name
	newNames[i-1]= newNames[i-2];

	renameNegative(iFields, newNames, false, IJavaModelStatusConstants.NAME_COLLISION);
}
/**
 * Ensures that renaming can be canceled.
 */
public void testRenameFieldsWithCancel() throws CoreException {
	boolean isCanceled = false;
	String addition = "new1";
	IType type = RenameTests.this.cu.getType("X");
	IField[] iFields = type.getFields();
	String[] newNames = new String[iFields.length];
	int i;
	for (i = 0; i < iFields.length; i++) {
		IField f = iFields[i];
		newNames[i] = addition + f.getElementName();
	}
	try {
		TestProgressMonitor monitor = TestProgressMonitor.getInstance();
		monitor.setCancelledCounter(1);
		renamePositive(iFields, new IJavaElement[] {type}, newNames, false, false, monitor);
	} catch (OperationCanceledException e) {
		isCanceled = true;
	}
	assertTrue("Operation should have thrown an operation canceled exception", isCanceled);
}
/**
 * Ensures that an initializer cannot be renamed.
 */
public void testRenameInitializer() {
	IType typeSource= this.cu.getType("X");
	IInitializer initializerSource= typeSource.getInitializer(1);

	renameNegative(initializerSource, "someName", false, IJavaModelStatusConstants.INVALID_ELEMENT_TYPES);
}
/**
 * Ensures that main types can be renamed. As a side effect
 * of renaming the main types, the types' enclosing compilation unit
 * are renamed as well.
 */
public void testRenameMainTypes() throws CoreException {
	this.createFile(
		"/P/src/Y.java",
		"public class Y {\n" +
		"  public Y() {\n" +
		"  }\n" +
		"}"
	);
	ICompilationUnit cu2 = getCompilationUnit("/P/src/Y.java");
	IPackageFragment pkg = (IPackageFragment) this.cu.getParent();

	IJavaElement[] types = new IJavaElement[] {
		this.cu.getType("X"),
		cu2.getType("Y")
	};
	String[] newNames = new String[] {"NewX", "NewY"};
	renamePositive(
		new IJavaElement[] {
			this.cu.getType("X"),
			cu2.getType("Y")
		},
		new IJavaElement[] {
			types[0].getParent(),
			types[1].getParent()},
		newNames,
		false);

	//test that both the compilation unit, main type and constructors have been renamed.
	ICompilationUnit renamedCU1= pkg.getCompilationUnit("NewX.java");
	ICompilationUnit renamedCU2= pkg.getCompilationUnit("NewY.java");
	IType newType1 = renamedCU1.getType("NewX");
	IType newType2 = renamedCU2.getType("NewY");
	assertTrue("NewX should be present", newType1.exists());
	assertTrue("NewY should be present", newType2.exists());

	IMethod constructor1 = newType1.getMethod("NewX", new String[] {"I"});
	IMethod constructor2 = newType2.getMethod("NewY", new String[] {});
	assertTrue("NewX(int) should be present", constructor1.exists());
	assertTrue("NewY() should be present", constructor2.exists());
}
/**
 * Ensures that main types can be renamed as well as a child of a main type. As a side effect
 * of renaming the main types, the types enclosing compilation unit
 * are renamed as well.
 * @see "1FTKMBD: ITPJCORE:ALL - JM- Cannot rename parent and child with the same operation"
 */
public void testRenameMainTypesAndAChild() throws CoreException {
	this.createFile(
		"/P/src/Y.java",
		"public class Y {\n" +
		"  public Y() {\n" +
		"  }\n" +
		"}"
	);
	ICompilationUnit cu2 = getCompilationUnit("/P/src/Y.java");

	String[] newNames = new String[] {
		"newBar",
		"NewX",
		"NewY"
	};
	IJavaElement[] elements = new IJavaElement[] {
		this.cu.getType("X").getField("bar"),
		this.cu.getType("X"),
		cu2.getType("Y")
	};
	IJavaElement[] destinations = new IJavaElement[elements.length];
	for (int i = 0; i < elements.length; i++) {
		destinations[i] = elements[i].getParent();
	}

	getJavaModel().rename(elements, destinations, newNames, false, null);

	//test that both the compilation unit and the main type have been renamed.
	IPackageFragment pkg = (IPackageFragment) this.cu.getParent();
	ICompilationUnit renamedCU1= pkg.getCompilationUnit("NewX.java");
	IType newX = renamedCU1.getType("NewX");
	assertTrue("NewX should be present", newX.exists());

	ICompilationUnit renamedCU2= pkg.getCompilationUnit("NewY.java");
	IType newY = renamedCU2.getType("NewY");
	assertTrue("NewY should be present", newY.exists());

	IField newBar = newX.getField("newBar");
	assertTrue("Renamed field should exist", newBar.exists());
}
/**
 * Ensures that a method can be renamed.
 */
public void testRenameMethod() throws JavaModelException {
	IType type = this.cu.getType("X");
	IMethod method = type.getMethod("foo", new String[] {"QString;"});
	renamePositive(method, "newFoo", false);

	//ensure that the method did not move as a result of the rename
	ensureCorrectPositioning(
		type,
		type.getMethod("newFoo", new String[] {"QString;"}),
		type.getMethod("otherMethod", new String[] {"QString;"}));
}
/**
 * Ensures that a method cannot be renamed to an existing method name.
 */
public void testRenameMethodResultingInCollision() {
	IType type = this.cu.getType("X");
	IMethod method = type.getMethod("foo", new String[] {"QString;"});
	renameNegative(method, "otherMethod", false, IJavaModelStatusConstants.NAME_COLLISION);
}
/**
 * Ensures that a method cannot be renamed to an invalid method name
 */
public void testRenameMethodsWithInvalidName() {
	IMethod method = this.cu.getType("X").getMethod("foo", new String[] {"QString;"});
	renameNegative(method, "%%someInvalidName", false, IJavaModelStatusConstants.INVALID_NAME);
}
public void testRenamePF() throws CoreException {
	this.createFolder("/P/src/x/y/z");
	this.createFile(
		"/P/src/x/y/z/A.java",
		"package x.y.z;\n" +
		"public class A {\n" +
		"}"
	);

	IPackageFragment frag = getPackage("/P/src/x/y/z");
	clearDeltas();
	frag.rename("x.y.newZ", false, null);

	IPackageFragment newFrag = getPackage("/P/src/x/y/newZ");
	assertTrue("Old package should not exist", !frag.exists());
	assertTrue("New package should exist", newFrag.exists());

	ICompilationUnit compilationUnit = newFrag.getCompilationUnit("A.java");
	assertTrue("A.java should exits in new package", compilationUnit.exists());

	IType[] types = compilationUnit.getTypes();
	assertTrue(types != null && types.length == 1);
	IType mainType = types[0];
	assertEquals(
		"Unexpected A.java's main type'",
		"x.y.newZ.A",
		mainType.getFullyQualifiedName());

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN}\n" +
		"		x.y.newZ[+]: {MOVED_FROM(x.y.z [in src [in P]])}\n" +
		"		x.y.z[-]: {MOVED_TO(x.y.newZ [in src [in P]])}"
	);
}
/*
 * Ensures that renaming to a name containing spaces works
 * (regression test for bug 21957 'refactor rename' allows subpackage name to start with a space)
 */
public void testRenamePF2() throws CoreException {
	this.createFolder("/P/src/x/y/z");
	this.createFile(
		"/P/src/x/y/z/A.java",
		"package x.y.z;\n" +
		"public class A {\n" +
		"}"
	);

	IPackageFragment frag = getPackage("/P/src/x/y/z");
	clearDeltas();
	frag.rename("x.y. z2", false, null);

	IPackageFragment newFrag = getPackage("/P/src/x/y/z2");
	assertTrue("Old package should not exist", !frag.exists());
	assertTrue("New package should exist", newFrag.exists());

	ICompilationUnit compilationUnit = newFrag.getCompilationUnit("A.java");
	assertTrue("A.java should exits in new package", compilationUnit.exists());

	IType[] types = compilationUnit.getTypes();
	assertTrue(types != null && types.length == 1);
	IType mainType = types[0];
	assertEquals(
		"Unexpected A.java's main type'",
		"x.y.z2.A",
		mainType.getFullyQualifiedName());

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN}\n" +
		"		x.y.z2[+]: {MOVED_FROM(x.y.z [in src [in P]])}\n" +
		"		x.y.z[-]: {MOVED_TO(x.y.z2 [in src [in P]])}"
	);
}
/*
 * Ensure that renaming a package to a sub package and using IJavaProject#findType(...) in a IWorskpaceRunnable
 * finds the right type
 * (regression test for bug 83646 NPE renaming package)
 */
public void testRenamePF3() throws CoreException {
	createFolder("/P/src/x");
	createFile(
		"/P/src/x/A.java",
		"package x;\n" +
		"public class A {\n" +
		"}"
	);
	IWorkspaceRunnable runnable = new IWorkspaceRunnable(){
		public void run(IProgressMonitor monitor) throws CoreException {
			getPackage("/P/src/x").rename("x.y", false, null);
			IType type = getJavaProject("P").findType("x.y.A");
			assertTypesEqual(
				"Unepected type",
				"x.y.A\n",
				new IType[] {type});
		}
	};
	getWorkspace().run(runnable, null);
}
public void testRenamePFWithSubPackages() throws CoreException {
	this.createFolder("/P/src/x/y/z");
	this.createFile(
		"/P/src/x/y/z/A.java",
		"package x.y.z;\n" +
		"public class A {\n" +
		"}"
	);

	clearDeltas();
	getPackage("/P/src/x").rename("newX", false, null);

	IPackageFragment oldFrag = getPackage("/P/src/x/y/z");
	assertTrue("Old inner package should still exist", oldFrag.exists());

	IPackageFragment newFrag = getPackage("/P/src/newX");
	assertTrue("New package should exist", newFrag.exists());

	ICompilationUnit compilationUnit = oldFrag.getCompilationUnit("A.java");
	assertTrue("A.java should exits in old inner package", compilationUnit.exists());

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN}\n" +
		"		newX[+]: {}"
	);
}
/**
 * Ensures that a method can be renamed if it contains syntax errors.
 */
public void testRenameSyntaxErrorMethod() throws CoreException {
	this.createFile(
		"/P/src/Y.java",
		"public class Y {\n" +
		"  void foo( {\n" + // syntax error
		"  }\n" +
		"}"
	);
	IMethod method = getCompilationUnit("/P/src/Y.java").getType("Y").getMethod("foo", null);
	renamePositive(method, "newFoo", false);
}
/**
 * Ensures that attempting to rename with an incorrect number of renamings fails
 */
public void testRenameWithInvalidRenamings() {
	IMethod method = getCompilationUnit("/P/src/X.java").getType("X").getMethod("foo", null);

	renameNegative(
		new IJavaElement[] {method},
		new String[]{"", ""},
		false,
		IJavaModelStatusConstants.INDEX_OUT_OF_BOUNDS);

	renameNegative(
		new IJavaElement[] {method},
		null,
		false,
		IJavaModelStatusConstants.NULL_NAME);
}
/**
 * Ensures that a working copy cannot be renamed.
 */
public void testRenameWorkingCopy() throws JavaModelException {
	ICompilationUnit copy = null;
	try {
		copy = this.cu.getWorkingCopy(null);
		renameNegative(copy, "NewX", false, IJavaModelStatusConstants.INVALID_ELEMENT_TYPES);
	} finally {
		if (copy != null) copy.discardWorkingCopy();
	}
}

}
