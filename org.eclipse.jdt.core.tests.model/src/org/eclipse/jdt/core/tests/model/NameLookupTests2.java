/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.NameLookup;

import junit.framework.Test;

/**
 * These test ensure that modifications in Java projects are correctly reported as
 * IJavaEllementDeltas.
 */
public class NameLookupTests2 extends ModifyingResourceTests {
	
public NameLookupTests2(String name) {
	super(name);
}



public static Test suite() {
	return new Suite(NameLookupTests2.class);
}
private NameLookup getNameLookup(JavaProject project) throws JavaModelException {
	return project.newNameLookup((WorkingCopyOwner)null);
}
public void testAddPackageFragmentRootAndPackageFrament() throws CoreException {
	try {
		IJavaProject p1 = createJavaProject("P1", new String[] {"src1"}, "bin");
		IJavaProject p2 = createJavaProject("P2", new String[] {}, "");
		IClasspathEntry[] classpath = 
			new IClasspathEntry[] {
				JavaCore.newProjectEntry(new Path("/P1"))
			};
		p2.setRawClasspath(classpath, null);
		
		IPackageFragment[] res = getNameLookup((JavaProject)p2).findPackageFragments("p1", false);
		assertTrue("Should get no package fragment", res == null);
		
		IClasspathEntry[] classpath2 = 
			new IClasspathEntry[] {
				JavaCore.newSourceEntry(new Path("/P1/src1")),
				JavaCore.newSourceEntry(new Path("/P1/src2"))
			};
		p1.setRawClasspath(classpath2, null);
		createFolder("/P1/src2/p1");
		
		res = getNameLookup((JavaProject)p2).findPackageFragments("p1", false);
		assertTrue(
			"Should get 'p1' package fragment",
			res != null &&
			res.length == 1 &&
			res[0].getElementName().equals("p1"));

	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
public void testAddPackageFragment() throws CoreException {
	try {
		createJavaProject("P1", new String[] {"src1"}, "bin");
		IJavaProject p2 = createJavaProject("P2", new String[] {}, "");
		IClasspathEntry[] classpath = 
			new IClasspathEntry[] {
				JavaCore.newProjectEntry(new Path("/P1"))
			};
		p2.setRawClasspath(classpath, null);
		
		IPackageFragment[] res = getNameLookup((JavaProject)p2).findPackageFragments("p1", false);
		assertTrue("Should get no package fragment", res == null);
		
		createFolder("/P1/src1/p1");
		
		res = getNameLookup((JavaProject)p2).findPackageFragments("p1", false);
		assertTrue(
			"Should get 'p1' package fragment",
			res != null &&
			res.length == 1 &&
			res[0].getElementName().equals("p1"));

	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/*
 * Resolve, add pkg, resolve again: new pkg should be accessible
 * (regression test for bug 37962 Unexpected transient problem during reconcile
 */
public void testAddPackageFragment2() throws CoreException {
	try {
		JavaProject project = (JavaProject)createJavaProject("P", new String[] {"src"}, "bin");
		createFolder("/P/src/p1");
		
		IPackageFragment[] pkgs = getNameLookup(project).findPackageFragments("p1", false);
		assertElementsEqual(
			"Didn't find p1",
			"p1 [in src [in P]]",
			pkgs);
		
		createFolder("/P/src/p2");
	
		pkgs = getNameLookup(project).findPackageFragments("p2", false);
		assertElementsEqual(
			"Didn't find p2",
			"p2 [in src [in P]]",
			pkgs);
	} finally {
		deleteProject("P");
	}
}
/*
 * Find a default package fragment in a non-default root by its path.
 * (regression test for bug 63245 findPackageFragment won't return default package)
 */
public void testFindDefaultPackageFragmentInNonDefaultRoot() throws CoreException {
	try {
		JavaProject project = (JavaProject)createJavaProject("P", new String[] {"src"}, "bin");
		
		IPackageFragment pkg = getNameLookup(project).findPackageFragment(new Path("/P/src"));
		assertElementsEqual(
			"Didn't find default package",
			"<default> [in src [in P]]",
			new IJavaElement[] {pkg});
		
	} finally {
		deleteProject("P");
	}
}
}

