/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;

import junit.framework.Test;

public class CreatePackageTests extends ModifyingResourceTests {
public CreatePackageTests(String name) {
	super(name);
}
public static Test suite() {
	return buildModelTestSuite(CreatePackageTests.class);
}
@Override
public void setUp() throws Exception {
	super.setUp();
	createJavaProject("P");
	createFolder("/P/p");
	startDeltas();
}
@Override
public void tearDown() throws Exception {
	stopDeltas();
	deleteProject("P");
	super.tearDown();
}
/**
 * Ensures that a package fragment can be created in a package
 * fragment root.
 * Verifies that the proper change deltas are generated as a side effect
 * of running the operation.
 */
public void testCreatePackageFragment1() throws JavaModelException {
	IPackageFragmentRoot root= getPackageFragmentRoot("P", "");
	IPackageFragment frag= root.createPackageFragment("one.two.three", false, null);
	assertCreation(frag);
	assertTrue("Fragment return not correct", frag.getElementName().equals("one.two.three"));
	assertDeltas(
		"Unexpected delta",
		"P[*]: {CHILDREN}\n" +
		"	<project root>[*]: {CHILDREN}\n" +
		"		one.two.three[+]: {}\n" +
		"		one.two[+]: {}\n" +
		"		one[+]: {}"
	);
}
/**
 * Ensures that a package fragment that shares a prefix path with
 * an existing package fragment can be created in a package
 * fragment root.
 * Verifies that the proper change deltas are generated as a side effect
 * of running the operation.
 */
public void testCreatePackageFragment2() throws JavaModelException {
	IPackageFragmentRoot root = getPackageFragmentRoot("P", "");
	IPackageFragment frag= root.createPackageFragment("one.two.three.four", false, null);
	assertCreation(frag);
	assertDeltas(
		"Unexpected delta",
		"P[*]: {CHILDREN}\n" +
		"	<project root>[*]: {CHILDREN}\n" +
		"		one.two.three.four[+]: {}\n" +
		"		one.two.three[+]: {}\n" +
		"		one.two[+]: {}\n" +
		"		one[+]: {}"
	);
}
/**
 * Ensures that a package fragment can be created for the default package
 */
public void testCreatePackageFragment3() throws JavaModelException {
	IPackageFragmentRoot root= getPackageFragmentRoot("P", "");
	IPackageFragment frag= root.createPackageFragment("", false, null);
	assertCreation(frag);
	assertTrue("Fragment return not correct", frag.getElementName().equals(""));
}
/**
 * Ensures that a package fragment can be created even if its name is unconventional.
 * (regression test for 9479 exception on package creation (discouraged name))
 */
public void testCreatePackageFragment4() throws JavaModelException {
	IPackageFragmentRoot root= getPackageFragmentRoot("P", "");
	IPackageFragment frag= root.createPackageFragment("A", false, null);
	assertCreation(frag);
	assertTrue("Fragment return not correct", frag.getElementName().equals("A"));
}
/*
 * Ensures that a package fragment can be created inside an operation with a
 * package fragment root scheduling rule.
 * (regression test for 95288 [model] CreatePackageFragmentOperation runs with wrong ISchedulingRule)
 */
public void testCreatePackageFragment5() throws CoreException {
	IWorkspaceRunnable runnable = new IWorkspaceRunnable(){
		public void run(IProgressMonitor monitor) throws CoreException {
			IPackageFragmentRoot root= getPackageFragmentRoot("P", "");
			IPackageFragment frag= root.createPackageFragment("p", false, monitor);
			assertCreation(frag);
		}
	};
	getWorkspace().run(runnable, getProject("P"), IResource.NONE, null);
}
/**
 * Ensures that a package fragment that already exists is not duplicated in the package
 * fragment root.
 */
public void testDuplicatePackageFragment() throws JavaModelException {
	IPackageFragmentRoot root= getPackageFragmentRoot("P", "");
	IPackageFragment frag= root.createPackageFragment("p",  false, null);
	assertCreation(frag);
	assertDeltas(
		"Unexpected delta",
		""
	);
}
/**
 * Ensures that a package fragment cannot be created with an invalid parameters.
 */
public void testInvalidPackageFragment() throws CoreException {
	createFile("/P/p/other", "");
	IPackageFragmentRoot root = getPackageFragmentRoot("P", "");
	try {
		root.createPackageFragment(null,  false, null);
	} catch (JavaModelException jme) {
		assertTrue("Incorrect JavaModelException thrown for creating an package fragment with invalid name", jme.getStatus().getCode() == IJavaModelStatusConstants.INVALID_NAME);
		try {
			root.createPackageFragment("java.jfg.",  false, null);
		} catch (JavaModelException jme2) {
			assertTrue("Incorrect JavaModelException thrown for creating a package fragment with invalid name", jme2.getStatus().getCode() == IJavaModelStatusConstants.INVALID_NAME);
			try {
				root.createPackageFragment("p.other", false, null);
			} catch (JavaModelException jme3) {
				assertTrue("Incorrect JavaModelException thrown for creating a package fragment that collides with a file", jme3.getStatus().getCode() == IJavaModelStatusConstants.NAME_COLLISION);
				return;
			}
		}
	}
	assertTrue("No JavaModelException thrown for creating a package fragment with an invalid parameters", false);
}
}
