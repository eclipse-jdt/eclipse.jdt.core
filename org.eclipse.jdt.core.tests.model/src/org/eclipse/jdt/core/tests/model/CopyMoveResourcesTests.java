/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.util.Util;

public class CopyMoveResourcesTests extends CopyMoveTests {
/**
 */
public CopyMoveResourcesTests(String name) {
	super(name);
}
/**
 * Copies the element to the container with optional rename
 * and forcing. The operation should succeed, so any exceptions
 * encountered are thrown.
 */
public IJavaElement copyPositive(IJavaElement element, IJavaElement container, IJavaElement sibling, String rename, boolean force) throws JavaModelException {
	try {
		startDeltas();

		// if forcing, ensure that a name collision exists
		if (force) {
			IJavaElement collision = generateHandle(element, rename, container);
			assertTrue("Collision does not exist", collision.exists());
		}

		// copy
	 	((ISourceManipulation) element).copy(container, sibling, rename, force, null);

		// ensure the original element still exists
		assertTrue("The original element must still exist", element.exists());

		// generate the new element	handle
		IJavaElement copy = generateHandle(element, rename, container);
		assertTrue("Copy should exist", copy.exists());
		//ensure correct position
		if (element.getElementType() > IJavaElement.COMPILATION_UNIT) {
			ensureCorrectPositioning((IParent) container, sibling, copy);
		} else if (container.getElementType() != IJavaElement.PACKAGE_FRAGMENT_ROOT) {
			// ensure package name is correct
			if (container.getElementName().equals("")) {
				// default package - should be no package decl
				IJavaElement[] children = ((ICompilationUnit) copy).getChildren();
				boolean found = false;
				for (int i = 0; i < children.length; i++) {
					if (children[i] instanceof IPackageDeclaration) {
						found = true;
					}
				}
				assertTrue("Should not find package decl", !found);
			} else {
				IJavaElement[] children = ((ICompilationUnit) copy).getChildren();
				boolean found = false;
				for (int i = 0; i < children.length; i++) {
					if (children[i] instanceof IPackageDeclaration) {
						assertTrue("package declaration incorrect", ((IPackageDeclaration) children[i]).getElementName().equals(container.getElementName()));
						found = true;
					}
				}
				assertTrue("Did not find package decl", found);
			}
		}
		IJavaElementDelta destDelta = this.deltaListener.getDeltaFor(container, true);
		assertTrue("Destination container not changed", destDelta != null && destDelta.getKind() == IJavaElementDelta.CHANGED);
		IJavaElementDelta[] deltas = destDelta.getAddedChildren();
		// FIXME: not strong enough
		boolean found = false;
		for (int i = 0; i < deltas.length; i++) {
			if (deltas[i].getElement().equals(copy))
				found = true;
		}
		assertTrue("Added children not correct for element copy", found);
		return copy;
	} finally {
		stopDeltas();
	}
}

/**
 * Moves the elements to the containers with optional renaming
 * and forcing. The operation should succeed, so any exceptions
 * encountered are thrown.
 */
public void movePositive(IJavaElement[] elements, IJavaElement[] destinations, IJavaElement[] siblings, String[] names, boolean force, IProgressMonitor monitor) throws JavaModelException {
	try {
		startDeltas();

		// if forcing, ensure that a name collision exists
		int i;
		if (force) {
			for (i = 0; i < elements.length; i++) {
				IJavaElement e = elements[i];
				IJavaElement collision = null;
				if (names == null) {
					collision = generateHandle(e, null, destinations[i]);
				} else {
					collision = generateHandle(e, names[i], destinations[i]);
				}
				assertTrue("Collision does not exist", collision.exists());
			}
		}

		// move
		getJavaModel().move(elements, destinations, siblings, names, force, monitor);

		for (i = 0; i < elements.length; i++) {
			IJavaElement element = elements[i];
			IJavaElement moved = null;
			if (names == null) {
				moved = generateHandle(element, null, destinations[i]);
			} else {
				moved = generateHandle(element, names[i], destinations[i]);
			}
			// ensure the original element no longer exists, unless moving within the same container, or moving a primary working copy
			if (!destinations[i].equals(element.getParent())) {
				if (element.getElementType() != IJavaElement.COMPILATION_UNIT || !((ICompilationUnit) element).isWorkingCopy())
					assertTrue("The original element must not exist", !element.exists());
			}
			assertTrue("Moved element should exist", moved.exists());

			IJavaElement container = destinations[i];
			if (container.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
				if (container.getElementName().equals("")) {
					// default package
					if (moved.getElementType() == IJavaElement.COMPILATION_UNIT) {
						IJavaElement[] children = ((ICompilationUnit) moved).getChildren();
						for (int j = 0; j < children.length; j++) {
							if (children[j].getElementType() == IJavaElement.PACKAGE_DECLARATION) {
								assertTrue("Should not find package decl", false);
							}
						}
					}
				} else {
					IJavaElement[] children = ((ICompilationUnit) moved).getChildren();
					boolean found = false;
					for (int j = 0; j < children.length; j++) {
						if (children[j] instanceof IPackageDeclaration) {
							assertTrue("package declaration incorrect", ((IPackageDeclaration) children[j]).getElementName().equals(container.getElementName()));
							found = true;
							break;
						}
					}
					assertTrue("Did not find package decl", found);
				}
			}
			IJavaElementDelta destDelta = null;
			if (isMainType(element, destinations[i]) && names != null && names[i] != null) { //moved/renamed main type to same cu
				destDelta = this.deltaListener.getDeltaFor(moved.getParent());
				assertTrue("Renamed compilation unit as result of main type not added", destDelta != null && destDelta.getKind() == IJavaElementDelta.ADDED);
				IJavaElementDelta[] deltas = destDelta.getAddedChildren();
				assertTrue("Added children not correct for element copy", deltas[0].getElement().equals(moved));
				assertTrue("flag should be F_MOVED_FROM", (deltas[0].getFlags() & IJavaElementDelta.F_MOVED_FROM) > 0);
				assertTrue("moved from handle should be original", deltas[0].getMovedFromElement().equals(element));
			} else {
				destDelta = this.deltaListener.getDeltaFor(destinations[i], true);
				assertTrue("Destination container not changed", destDelta != null && destDelta.getKind() == IJavaElementDelta.CHANGED);
				IJavaElementDelta[] deltas = destDelta.getAddedChildren();
				for (int j = 0; j < deltas.length - 1; j++) {
					// side effect packages added
					IJavaElement pkg = deltas[j].getElement();
					assertTrue("Side effect child should be a package fragment", pkg.getElementType() == IJavaElement.PACKAGE_FRAGMENT);
					assertTrue("Side effect child should be an enclosing package", element.getElementName().startsWith(pkg.getElementName()));
				}
				IJavaElementDelta pkgDelta = deltas[deltas.length - 1];
				assertTrue("Added children not correct for element copy", pkgDelta.getElement().equals(moved));
				assertTrue("flag should be F_MOVED_FROM", (pkgDelta.getFlags() & IJavaElementDelta.F_MOVED_FROM) > 0);
				assertTrue("moved from handle shoud be original", pkgDelta.getMovedFromElement().equals(element));
				IJavaElementDelta sourceDelta = this.deltaListener.getDeltaFor(element, true);
				assertTrue("moved to handle should be original", sourceDelta.getMovedToElement().equals(moved));
			}
		}
	} finally {
		stopDeltas();
	}
}
/**
 * Setup for the next test.
 */
public void setUp() throws Exception {
	super.setUp();

	this.createJavaProject("P", new String[] {"src", "src2"}, "bin");
}
static {
//	TESTS_NAMES = new String[] { "testCopyWorkingCopyDestination"};
}
public static Test suite() {
	return buildModelTestSuite(CopyMoveResourcesTests.class);
}
/**
 * Cleanup after the previous test.
 */
public void tearDown() throws Exception {
	this.deleteProject("P");

	super.tearDown();
}
/**
 * Ensures that a CU can be copied to a different package.
 */
public void testCopyCU01() throws CoreException {
	this.createFolder("/P/src/p1");
	this.createFile(
		"/P/src/p1/X.java",
		"package p1;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");

	this.createFolder("/P/src/p2");
	IPackageFragment pkgDest = getPackage("/P/src/p2");

	copyPositive(cuSource, pkgDest, null, null, false);

	ICompilationUnit cu= pkgDest.getCompilationUnit("X.java");
	assertTrue("Package declaration not updated for copied cu", cu.getPackageDeclaration("p2").exists());
}
/**
 * This operation should fail as copying a CU and a CU member at the
 * same time is not supported.
 */
public void testCopyCU02() throws CoreException {
	this.createFolder("/P/src/p1");
	this.createFile(
		"/P/src/p1/X.java",
		"package p1;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");

	copyNegative(
		new IJavaElement[]{cuSource, cuSource.getType("X")},
		new IJavaElement[]{cuSource.getParent(), cuSource},
		null,
		new String[]{"Y.java", "Y"},
		false,
		IJavaModelStatusConstants.INVALID_ELEMENT_TYPES);
}
/**
 * Ensures that a CU can be copied to a different package, replacing an existing CU.
 */
public void testCopyCU03() throws CoreException {
	this.createFolder("/P/src/p1");
	this.createFile(
		"/P/src/p1/X.java",
		"package p1;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");

	this.createFolder("/P/src/p2");
	this.createFile(
		"/P/src/p2/X.java",
		"package p2;\n" +
		"public class X {\n" +
		"}"
	);
	IPackageFragment pkgDest = getPackage("/P/src/p2");

	copyPositive(cuSource, pkgDest, null, null, true);
}
/**
 * Ensures that a CU can be copied from a default package to a non-default package.
 */
public void testCopyCU04() throws CoreException {
	createFile(
		"/P/src/X.java",
		"public class X {\n" +
		"}"
	);
	ICompilationUnit cuSource = getCompilationUnit("/P/src/X.java");

	createFolder("/P/src/p");
	IPackageFragment pkgDest = getPackage("/P/src/p");

	copyPositive(cuSource, pkgDest, null, null, false);

	ICompilationUnit cu= pkgDest.getCompilationUnit("X.java");
	assertTrue("Package declaration not updated for copied cu", cu.getPackageDeclaration("p").exists());
}
/**
 * Ensures that a CU can be copied to a different package,
 * and be renamed.
 */
public void testCopyCU05() throws CoreException {
	this.createFolder("/P/src/p1");
	this.createFile(
		"/P/src/p1/X.java",
		"package p1;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");

	this.createFolder("/P/src/p2");
	IPackageFragment pkgDest = getPackage("/P/src/p2");

	copyPositive(cuSource, pkgDest, null, "Y.java", false);
}
/**
 * Ensures that a read-only CU can be copied to a different package.
 */
public void testCopyCU06() throws CoreException {
	if (!Util.isReadOnlySupported()) {
		// Do not test if file system does not support read-only attribute
		return;
	}
	IFile file = null;
	IFile file2 = null;
	try {
		this.createFolder("/P/src/p1");
		file = this.createFile(
			"/P/src/p1/X.java",
			"package p1;\n" +
			"public class X {\n" +
			"}"
		);
		Util.setReadOnly(file, true);
		ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");

		this.createFolder("/P/src/p2");
		IPackageFragment pkgDest = getPackage("/P/src/p2");

		copyPositive(cuSource, pkgDest, null, null, false);

		file2 = getFile("/P/src/p2/X.java");
		assertTrue("Destination cu should be read-only", file2.isReadOnly());
	} finally {
		if (file != null) {
			Util.setReadOnly(file, false);
		}
		if (file2 != null) {
			Util.setReadOnly(file2, false);
		}
		deleteFolder("/P/src/p1");
		deleteFolder("/P/src/p2");
	}
}
/**
 * Ensures that a CU can be copied to a different package,
 * and be renamed, overwriting an existing CU
 */
public void testCopyCU07() throws CoreException {
	this.createFolder("/P/src/p1");
	this.createFile(
		"/P/src/p1/X.java",
		"package p1;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");

	this.createFolder("/P/src/p2");
	this.createFile(
		"/P/src/p2/Y.java",
		"package p2;\n" +
		"public class Y {\n" +
		"}"
	);
	IPackageFragment pkgDest = getPackage("/P/src/p2");

	copyPositive(cuSource, pkgDest, null, "Y.java", true);
}
/**
 * Ensures that a CU cannot be copied to a different package,over an existing CU when no force.
 */
public void testCopyCU08() throws CoreException {
	this.createFolder("/P/src/p1");
	this.createFile(
		"/P/src/p1/X.java",
		"package p1;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");

	this.createFolder("/P/src/p2");
	this.createFile(
		"/P/src/p2/X.java",
		"package p2;\n" +
		"public class X {\n" +
		"}"
	);
	IPackageFragment pkgDest = getPackage("/P/src/p2");

	copyNegative(cuSource, pkgDest, null, null, false, IJavaModelStatusConstants.NAME_COLLISION);
}
/**
 * Ensures that a CU cannot be copied to an invalid destination
 */
public void testCopyCU09() throws CoreException {
	this.createFolder("/P/src/p1");
	this.createFile(
		"/P/src/p1/X.java",
		"package p1;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");

	copyNegative(cuSource, cuSource, null, null, false, IJavaModelStatusConstants.INVALID_DESTINATION);
}
/**
 * Ensures that a CU can be copied to a null container
 */
public void testCopyCU10() throws CoreException {
	this.createFolder("/P/src/p1");
	this.createFile(
		"/P/src/p1/X.java",
		"package p1;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");

	try {
		cuSource.copy(null, null, null, false, null);
	} catch (IllegalArgumentException iae) {
		return;
	}
	assertTrue("Should not be able to move a cu to a null container", false);
}
/**
 * Ensures that a CU can be copied to along with its server properties.
 * (Regression test for PR #1G56QT9)
 */
public void testCopyCU11() throws CoreException {
	this.createFolder("/P/src/p1");
	this.createFile(
		"/P/src/p1/X.java",
		"package p1;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");

	QualifiedName qualifiedName = new QualifiedName("x.y.z", "a property");
	cuSource.getUnderlyingResource().setPersistentProperty(
		qualifiedName,
		"some value");

	this.createFolder("/P/src/p2");
	IPackageFragment pkgDest = getPackage("/P/src/p2");

	copyPositive(cuSource, pkgDest, null, null, false);
	ICompilationUnit cu= pkgDest.getCompilationUnit("X.java");
	String propertyValue = cu.getUnderlyingResource().getPersistentProperty(qualifiedName);
	assertEquals(
		"Server property should be copied with cu",
		"some value",
		propertyValue
	);
}
/*
 * Ensures that the correct scheduling rule is used when copying a CU
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=142990 )
 */
public void testCopyCU12() throws Exception {
	createFolder("/P/src/p1");
	createFile("/P/src/p1/X.java", "package p1; public class X {}");
	this.createFolder("/P/src/p2");

	IWorkspaceRunnable runnable = new IWorkspaceRunnable(){
		public void run(IProgressMonitor monitor) throws CoreException {
			ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");
			IPackageFragment pkgDest = getPackage("/P/src/p2");
			cuSource.copy(pkgDest, null, null, false, null);
			assertTrue("/P/src/p2/X.java should exist", getCompilationUnit("/P/src/p2/X.java").exists());
		}
	};
	getWorkspace().run(runnable, getFolder("/P/src/p2"), IResource.NONE, null);
}
/**
 * Ensures that a package fragment can be copied to a different package fragment root.
 */
public void testCopyPackageFragment() throws CoreException {
	this.createFolder("/P/src/p1");
	this.createFile(
		"/P/src/p1/X.java",
		"package p1;\n" +
		"public class X {\n" +
		"}"
	);
	IPackageFragment pkgSource = getPackage("/P/src/p1");

	IPackageFragmentRoot rootDest= getPackageFragmentRoot("P", "src2");

	copyPositive(pkgSource, rootDest, null, null, false);
}
/**
 * Ensures that a package fragment can be copied to a different package fragment root.
 */
public void testCopyReadOnlyPackageFragment() throws CoreException {
	if (!Util.isReadOnlySupported()) {
		// Do not test if file system does not support read-only attribute
		return;
	}
	IPackageFragment pkgSource = null;
	IPackageFragment pkg2 = null;
	try {
		this.createFolder("/P/src/p1/p2/p3");
		this.createFile(
			"/P/src/p1/p2/p3/X.java",
			"package p1.p2.p3;\n" +
			"public class X {\n" +
			"}"
		);
		Util.setReadOnly(getFile("/P/src/p1/p2/p3/X.java"), true);
		pkgSource = getPackage("/P/src/p1");
		Util.setReadOnly(pkgSource.getResource(), true);
		pkg2 = getPackage("/P/src/p1/p2/p3");
		Util.setReadOnly(pkg2.getResource(), true);

		IPackageFragmentRoot rootDest= getPackageFragmentRoot("P", "src2");

		copyPositive(pkg2, rootDest, null, null, false);

		assertTrue("Not readOnly", Util.isReadOnly(getPackage("/P/src2/p1").getResource()));
		assertTrue("Is readOnly", !Util.isReadOnly(getPackage("/P/src2/p1/p2").getResource()));
		assertTrue("Not readOnly", Util.isReadOnly(getPackage("/P/src2/p1/p2/p3").getResource()));
		assertTrue("Is readOnly", Util.isReadOnly(getFile("/P/src2/p1/p2/p3/X.java")));
	} finally {
		IFile xSrcFile = getFile("/P/src/p1/p2/p3/X.java");
		if (xSrcFile != null) {
			Util.setReadOnly(xSrcFile, false);
		}
		if (pkg2 != null) {
			Util.setReadOnly(pkg2.getResource(), false);
		}
		if (pkgSource != null) {
			Util.setReadOnly(pkgSource.getResource(), false);
		}
		IPackageFragment p1Fragment = getPackage("/P/src2/p1");
		if (p1Fragment != null) {
			Util.setReadOnly(p1Fragment.getResource(), false);
		}
		IPackageFragment p3Fragment = getPackage("/P/src2/p1/p2/p3");
		if (p3Fragment != null) {
			Util.setReadOnly(p3Fragment.getResource(), false);
		}
		IFile xFile = getFile("/P/src2/p1/p2/p3/X.java");
		if (xFile != null) {
			Util.setReadOnly(xFile, false);
		}
		deleteFolder("/P/src/p1");
	}
}
/**
 * Ensures that a WorkingCopy can be copied to a different package.
 */
public void testCopyWorkingCopy() throws CoreException {
	ICompilationUnit copy = null;
	try {
		this.createFolder("/P/src/p1");
		this.createFile(
			"/P/src/p1/X.java",
			"package p1;\n" +
			"public class X {\n" +
			"}"
		);
		ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");
		copy = cuSource.getWorkingCopy(null);

		this.createFolder("/P/src/p2");
		IPackageFragment pkgDest = getPackage("/P/src/p2");

		copyPositive(copy, pkgDest, null, null, false);
	} finally {
		if (copy != null) copy.discardWorkingCopy();
	}
}
/*
 * Ensures that a CU can be copied over an existing primary working copy in a different package.
 * (regression test for bug 117282 Package declaration inserted on wrong CU while copying class if names collide and editor opened)
 */
public void testCopyWorkingCopyDestination() throws CoreException {
	ICompilationUnit copy = null;
	try {
		createFolder("/P/src/p1");
		createFile(
			"/P/src/p1/X.java",
			"package p1;\n" +
			"public class X {\n" +
			"  void foo() {}\n" +
			"}"
		);
		ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");

		createFolder("/P/src/p2");
		IPackageFragment pkgDest = getPackage("/P/src/p2");
		createFile(
			"/P/src/p2/X.java",
			"\n" +
			"package p1;\n" +
			"public class X {\n" +
			"}"
		);
		copy = getCompilationUnit("/P/src/p2/X.java");
		copy.becomeWorkingCopy(null);

		copyPositive(cuSource, pkgDest, null, null, true/*force*/);
	} finally {
		if (copy != null) copy.discardWorkingCopy();
	}
}
/**
 * Ensures that a WorkingCopy can be copied to a different package, replacing an existing WorkingCopy.
 */
public void testCopyWorkingCopyForce() throws CoreException {
	ICompilationUnit copy = null;
	try {
		this.createFolder("/P/src/p1");
		this.createFile(
			"/P/src/p1/X.java",
			"package p1;\n" +
			"public class X {\n" +
			"}"
		);
		ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");
		copy = cuSource.getWorkingCopy(null);

		this.createFolder("/P/src/p2");
		this.createFile(
			"/P/src/p2/X.java",
			"package p2;\n" +
			"public class X {\n" +
			"}"
		);
		IPackageFragment pkgDest = getPackage("/P/src/p2");

		copyPositive(copy, pkgDest, null, null, true);
	} finally {
		if (copy != null) copy.discardWorkingCopy();
	}
}
/**
 * Ensures that a WorkingCopy can be copied to a different package,
 * and be renamed.
 */
public void testCopyWorkingCopyRename() throws CoreException {
	ICompilationUnit copy = null;
	try {
		this.createFolder("/P/src/p1");
		this.createFile(
			"/P/src/p1/X.java",
			"package p1;\n" +
			"public class X {\n" +
			"}"
		);
		ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");
		copy = cuSource.getWorkingCopy(null);

		this.createFolder("/P/src/p2");
		IPackageFragment pkgDest = getPackage("/P/src/p2");

		copyPositive(copy, pkgDest, null, "Y.java", false);
	} finally {
		if (copy != null) copy.discardWorkingCopy();
	}
}
/**
 * Ensures that a WorkingCopy can be copied to a different package,
 * and be renamed, overwriting an existing WorkingCopy
 */
public void testCopyWorkingCopyRenameForce() throws CoreException {
	ICompilationUnit copy = null;
	try {
		this.createFolder("/P/src/p1");
		this.createFile(
			"/P/src/p1/X.java",
			"package p1;\n" +
			"public class X {\n" +
			"}"
		);
		ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");
		copy = cuSource.getWorkingCopy(null);

		this.createFolder("/P/src/p2");
		this.createFile(
			"/P/src/p2/Y.java",
			"package p2;\n" +
			"public class Y {\n" +
			"}"
		);
		IPackageFragment pkgDest = getPackage("/P/src/p2");

		copyPositive(copy, pkgDest, null, "Y.java", true);
	} finally {
		if (copy != null) copy.discardWorkingCopy();
	}
}
/**
 * Ensures that a WorkingCopy cannot be copied to a different package,over an existing WorkingCopy when no force.
 */
public void testCopyWorkingCopyWithCollision() throws CoreException {
	ICompilationUnit copy = null;
	try {
		this.createFolder("/P/src/p1");
		this.createFile(
			"/P/src/p1/X.java",
			"package p1;\n" +
			"public class X {\n" +
			"}"
		);
		ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");
		copy = cuSource.getWorkingCopy(null);

		this.createFolder("/P/src/p2");
		this.createFile(
			"/P/src/p2/X.java",
			"package p2;\n" +
			"public class X {\n" +
			"}"
		);
		IPackageFragment pkgDest = getPackage("/P/src/p2");

		copyNegative(copy, pkgDest, null, null, false, IJavaModelStatusConstants.NAME_COLLISION);
	} finally {
		if (copy != null) copy.discardWorkingCopy();
	}
}
/**
 * Ensures that a WorkingCopy cannot be copied to an invalid destination
 */
public void testCopyWorkingCopyWithInvalidDestination() throws CoreException {
	ICompilationUnit copy = null;
	try {
		this.createFolder("/P/src/p1");
		this.createFile(
			"/P/src/p1/X.java",
			"package p1;\n" +
			"public class X {\n" +
			"}"
		);
		ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");
		copy = cuSource.getWorkingCopy(null);

		copyNegative(copy, cuSource, null, null, false, IJavaModelStatusConstants.INVALID_DESTINATION);
	} finally {
		if (copy != null) copy.discardWorkingCopy();
	}
}
/**
 * Ensures that a CU can be moved to a different package.
 */
public void testMoveCU01() throws CoreException {
	this.createFolder("/P/src/p1");
	this.createFile(
		"/P/src/p1/X.java",
		"package p1;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");

	this.createFolder("/P/src/p2");
	IPackageFragment pkgDest = getPackage("/P/src/p2");

	movePositive(cuSource, pkgDest, null, null, false);

	ICompilationUnit cu= pkgDest.getCompilationUnit("X.java");
	assertTrue("Package declaration not updated for copied cu", cu.getPackageDeclaration("p2").exists());
}
/**
 * This operation should fail as moving a CU and a CU member at the
 * same time is not supported.
 */
public void testMoveCU02() throws CoreException {
	this.createFolder("/P/src/p1");
	this.createFile(
		"/P/src/p1/X.java",
		"package p1;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");

	moveNegative(
		new IJavaElement[]{cuSource, cuSource.getType("X")},
		new IJavaElement[]{cuSource.getParent(), cuSource},
		null,
		new String[]{"Y.java", "Y"},
		false,
		IJavaModelStatusConstants.INVALID_ELEMENT_TYPES);
}
/**
 * Ensures that a CU can be moved to a different package, replacing an
 * existing CU.
 */
public void testMoveCU03() throws CoreException {
	this.createFolder("/P/src/p1");
	this.createFile(
		"/P/src/p1/X.java",
		"package p1;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");

	this.createFolder("/P/src/p2");
	this.createFile(
		"/P/src/p2/X.java",
		"package p2;\n" +
		"public class X {\n" +
		"}"
	);
	IPackageFragment pkgDest = getPackage("/P/src/p2");

	movePositive(cuSource, pkgDest, null, null, true);
}
/**
 * Ensures that a CU can be moved to a different package,
 * be renamed
 */
public void testMoveCU04() throws CoreException {
	this.createFolder("/P/src/p1");
	this.createFile(
		"/P/src/p1/X.java",
		"package p1;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");

	this.createFolder("/P/src/p2");
	IPackageFragment pkgDest = getPackage("/P/src/p2");

	movePositive(cuSource, pkgDest, null, "Y.java", false);
}
/**
 * Ensures that a CU can be moved to a different package,
 * be renamed, overwriting an existing resource.
 */
public void testMoveCU05() throws CoreException {
	this.createFolder("/P/src/p1");
	this.createFile(
		"/P/src/p1/X.java",
		"package p1;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");

	this.createFolder("/P/src/p2");
	this.createFile(
		"/P/src/p2/Y.java",
		"package p2;\n" +
		"public class Y {\n" +
		"}"
	);
	IPackageFragment pkgDest = getPackage("/P/src/p2");

	movePositive(cuSource, pkgDest, null, "Y.java", true);
}
/**
 * Ensures that a CU cannot be moved to a different package, replacing an
 * existing CU when not forced.
 */
public void testMoveCU06() throws CoreException {
	this.createFolder("/P/src/p1");
	this.createFile(
		"/P/src/p1/X.java",
		"package p1;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");

	this.createFolder("/P/src/p2");
	this.createFile(
		"/P/src/p2/X.java",
		"package p2;\n" +
		"public class X {\n" +
		"}"
	);
	IPackageFragment pkgDest = getPackage("/P/src/p2");

	moveNegative(cuSource, pkgDest, null, null, false, IJavaModelStatusConstants.NAME_COLLISION);
}
/**
 * Ensures that a CU cannot be moved to an invalid destination.
 */
public void testMoveCU07() throws CoreException {
	this.createFolder("/P/src/p1");
	this.createFile(
		"/P/src/p1/X.java",
		"package p1;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");

	moveNegative(cuSource, cuSource, null, null, false, IJavaModelStatusConstants.INVALID_DESTINATION);
}
/**
 * Ensures that a CU cannot be moved to a null container
 */
public void testMoveCU08() throws CoreException {
	this.createFolder("/P/src/p1");
	this.createFile(
		"/P/src/p1/X.java",
		"package p1;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");

	try {
		cuSource.move(null, null, null, false, null);
	} catch (IllegalArgumentException iae) {
		return;
	}
	assertTrue("Should not be able to move a cu to a null container", false);
}
/*
 * Ensures that the correct scheduling rule is used when moving a CU
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=142990 )
 */
public void testMoveCU09() throws Exception {
	createFolder("/P/src/p1");
	createFile("/P/src/p1/X.java", "package p1; public class X {}");
	this.createFolder("/P/src/p2");

	IWorkspaceRunnable runnable = new IWorkspaceRunnable(){
		public void run(IProgressMonitor monitor) throws CoreException {
			ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");
			IPackageFragment pkgDest = getPackage("/P/src/p2");
			cuSource.move(pkgDest, null, null, false, null);
			assertFalse("/P/src/p1/X.java should no longer exist", cuSource.exists());
		}
	};
	getWorkspace().run(runnable, getFolder("/P/src"), IResource.NONE, null);
}
/*
 * Ensures that the first block comment is not lost if moving a cu to the default package
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=247757 )
 */
public void testMoveCU10() throws CoreException {
	createFolder("/P/src/p1");
	createFile(
		"/P/src/p1/X.java",
		"/* some comment */\n" +
		"package p1;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");
	IPackageFragment pkgDest = getPackage("/P/src");
	cuSource.move(pkgDest, null/*no sibling*/, null/*no rename*/, false/*don't replace*/, null/*no progress*/);
	
	ICompilationUnit cuDest = getCompilationUnit("/P/src/X.java");
	assertSourceEquals(
		"Unexpected source",
		"/* some comment */\n" +
		"\n" +
		"public class X {\n" +
		"}",
		cuDest.getSource());
}
/*
 * Ensures that the first block comments are not lost if moving a cu to the default package
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=247757 )
 */
public void testMoveCU11() throws CoreException {
	createFolder("/P/src/p1");
	createFile(
		"/P/src/p1/X.java",
		"/* some comment */\n" +
		"/* other comment */\n" +
		"package p1;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");
	IPackageFragment pkgDest = getPackage("/P/src");
	cuSource.move(pkgDest, null/*no sibling*/, null/*no rename*/, false/*don't replace*/, null/*no progress*/);
	
	ICompilationUnit cuDest = getCompilationUnit("/P/src/X.java");
	assertSourceEquals(
		"Unexpected source",
		"/* some comment */\n" +
		"/* other comment */\n" +
		"\n" +
		"public class X {\n" +
		"}",
		cuDest.getSource());
}
/*
 * Ensures that the Javadoc comment is not lost if moving a cu to the default package
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=247757 )
 */
public void testMoveCU12() throws CoreException {
	createFolder("/P/src/p1");
	createFile(
		"/P/src/p1/X.java",
		"/** some Javadoc */\n" +
		"package p1;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");
	IPackageFragment pkgDest = getPackage("/P/src");
	cuSource.move(pkgDest, null/*no sibling*/, null/*no rename*/, false/*don't replace*/, null/*no progress*/);
	
	ICompilationUnit cuDest = getCompilationUnit("/P/src/X.java");
	assertSourceEquals(
		"Unexpected source",
		"/** some Javadoc */\n" + 
		"\n" + 
		"public class X {\n" + 
		"}",
		cuDest.getSource());
}
/*
 * Ensures that the Javadoc comment is not lost if moving a cu to the default package
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=247757 )
 */
public void testMoveCU13() throws CoreException {
	createFolder("/P/src/p1");
	createFile(
		"/P/src/p1/X.java",
		"/** some Javadoc */\n" +
		"// some line comment\n" +
		"package p1;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");
	IPackageFragment pkgDest = getPackage("/P/src");
	cuSource.move(pkgDest, null/*no sibling*/, null/*no rename*/, false/*don't replace*/, null/*no progress*/);
	
	ICompilationUnit cuDest = getCompilationUnit("/P/src/X.java");
	assertSourceEquals(
		"Unexpected source",
		"/** some Javadoc */\n" + 
		"// some line comment\n" +
		"\n" + 
		"public class X {\n" + 
		"}",
		cuDest.getSource());
}
/**
 * Ensures that a package fragment can be moved to a different package fragment root.
 */
public void testMovePackageFragment() throws CoreException {
	this.createFolder("/P/src/p1");
	this.createFile(
		"/P/src/p1/X.java",
		"package p1;\n" +
		"public class X {\n" +
		"}"
	);
	IPackageFragment pkgSource = getPackage("/P/src/p1");

	IPackageFragmentRoot rootDest= getPackageFragmentRoot("P", "src2");

	movePositive(pkgSource, rootDest, null, null, false);
}
/**
 * Ensures that a package fragment can be copied to a different package fragment root.
 */
public void testMoveReadOnlyPackageFragment() throws CoreException {
	if (!Util.isReadOnlySupported()) {
		// Do not test if file system does not support read-only attribute
		return;
	}
	IPackageFragment pkgSource = null;
	IPackageFragment pkg2 = null;
	try {
		this.createFolder("/P/src/p1/p2/p3");
		this.createFile(
			"/P/src/p1/p2/p3/X.java",
			"package p1.p2.p3;\n" +
			"public class X {\n" +
			"}"
		);
		Util.setReadOnly(getFile("/P/src/p1/p2/p3/X.java"), true);
		pkgSource = getPackage("/P/src/p1");
		Util.setReadOnly(pkgSource.getResource(), true);
		pkg2 = getPackage("/P/src/p1/p2/p3");
		Util.setReadOnly(pkg2.getResource(), true);

		IPackageFragmentRoot rootDest= getPackageFragmentRoot("P", "src2");

		movePositive(pkg2, rootDest, null, null, false);

		assertTrue("Not readOnly", Util.isReadOnly(getPackage("/P/src2/p1").getResource()));
		assertTrue("Is readOnly", !Util.isReadOnly(getPackage("/P/src2/p1/p2").getResource()));
		assertTrue("Not readOnly", Util.isReadOnly(getPackage("/P/src2/p1/p2/p3").getResource()));
		assertTrue("Is readOnly", Util.isReadOnly(getFile("/P/src2/p1/p2/p3/X.java")));
	} finally {
		IFile xSrcFile = getFile("/P/src/p1/p2/p3/X.java");
		if (xSrcFile != null) {
			Util.setReadOnly(xSrcFile, false);
		}
		if (pkg2 != null) {
			Util.setReadOnly(pkg2.getResource(), false);
		}
		if (pkgSource != null) {
			Util.setReadOnly(pkgSource.getResource(), false);
		}
		IPackageFragment p1Fragment = getPackage("/P/src2/p1");
		if (p1Fragment != null) {
			Util.setReadOnly(p1Fragment.getResource(), false);
		}
		IPackageFragment p3Fragment = getPackage("/P/src2/p1/p2/p3");
		if (p3Fragment != null) {
			Util.setReadOnly(p3Fragment.getResource(), false);
		}
		IFile xFile = getFile("/P/src2/p1/p2/p3/X.java");
		if (xFile != null) {
			Util.setReadOnly(xFile, false);
		}
		deleteFolder("/P/src/p1");
	}
}
/**
 * Ensures that a WorkingCopy cannot be moved to a different package.
 */
public void testMoveWorkingCopy() throws CoreException {
	ICompilationUnit copy = null;
	try {
		this.createFolder("/P/src/p1");
		this.createFile(
			"/P/src/p1/X.java",
			"package p1;\n" +
			"public class X {\n" +
			"}"
		);
		ICompilationUnit cuSource = getCompilationUnit("/P/src/p1/X.java");
		copy = cuSource.getWorkingCopy(null);

		this.createFolder("/P/src/p2");
		IPackageFragment pkgDest = getPackage("/P/src/p2");

		moveNegative(copy, pkgDest, null, null, false, IJavaModelStatusConstants.INVALID_ELEMENT_TYPES);
	} finally {
		if (copy != null) copy.discardWorkingCopy();
	}
}

/*
 * Ensures that a primary working copy can be moved to a different package
 * and that its buffer doesn't contain unsaved changed after the move.
 * (regression test for bug 83599 CU dirty after move refactoring)
 */
public void testMoveWorkingCopy2() throws CoreException {
	ICompilationUnit copy = null;
	try {
		this.createFolder("/P/src/p1");
		this.createFile(
			"/P/src/p1/X.java",
			"package p1;\n" +
			"public class X {\n" +
			"}"
		);
		copy = getCompilationUnit("/P/src/p1/X.java");
		copy.becomeWorkingCopy(null);

		this.createFolder("/P/src/p2");
		IPackageFragment pkgDest = getPackage("/P/src/p2");

		movePositive(copy, pkgDest, null, null, false);
		assertTrue("Should not have unsaved changes", !copy.getBuffer().hasUnsavedChanges());
	} finally {
		if (copy != null) copy.discardWorkingCopy();
	}
}
}
