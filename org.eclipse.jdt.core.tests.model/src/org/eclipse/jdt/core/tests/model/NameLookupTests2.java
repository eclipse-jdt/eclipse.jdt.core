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
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaProject;
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

public void testAddPackageFragmentRootAndPackageFrament() throws CoreException {
	try {
		IJavaProject p1 = this.createJavaProject("P1", new String[] {"src1"}, "bin");
		IJavaProject p2 = this.createJavaProject("P2", new String[] {}, "");
		IClasspathEntry[] classpath = 
			new IClasspathEntry[] {
				JavaCore.newProjectEntry(new Path("/P1"))
			};
		p2.setRawClasspath(classpath, null);
		
		IPackageFragment[] res = ((JavaProject)p2).getNameLookup().findPackageFragments("p1", false);
		assertTrue("Should get no package fragment", res == null);
		
		IClasspathEntry[] classpath2 = 
			new IClasspathEntry[] {
				JavaCore.newSourceEntry(new Path("/P1/src1")),
				JavaCore.newSourceEntry(new Path("/P1/src2"))
			};
		p1.setRawClasspath(classpath2, null);
		this.createFolder("/P1/src2/p1");
		
		res = ((JavaProject)p2).getNameLookup().findPackageFragments("p1", false);
		assertTrue(
			"Should get 'p1' package fragment",
			res != null &&
			res.length == 1 &&
			res[0].getElementName().equals("p1"));

	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
public void testAddPackageFragment() throws CoreException {
	try {
		this.createJavaProject("P1", new String[] {"src1"}, "bin");
		IJavaProject p2 = this.createJavaProject("P2", new String[] {}, "");
		IClasspathEntry[] classpath = 
			new IClasspathEntry[] {
				JavaCore.newProjectEntry(new Path("/P1"))
			};
		p2.setRawClasspath(classpath, null);
		
		IPackageFragment[] res = ((JavaProject)p2).getNameLookup().findPackageFragments("p1", false);
		assertTrue("Should get no package fragment", res == null);
		
		this.createFolder("/P1/src1/p1");
		
		res = ((JavaProject)p2).getNameLookup().findPackageFragments("p1", false);
		assertTrue(
			"Should get 'p1' package fragment",
			res != null &&
			res.length == 1 &&
			res[0].getElementName().equals("p1"));

	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/*
 * Resolve, add pkg, resolve again: new pkg should be accessible
 * (regression test for bug 37962 Unexpected transient problem during reconcile
 */
public void testAddPackageFragment2() throws CoreException {
	try {
		JavaProject project = (JavaProject)this.createJavaProject("P", new String[] {"src"}, "bin");
		this.createFolder("/P/src/p1");
		
		IPackageFragment[] pkgs = project.getNameLookup().findPackageFragments("p1", false);
		assertElementsEqual(
			"Didn't find p1",
			"p1 [in src [in P]]",
			pkgs);
		
		this.createFolder("/P/src/p2");
	
		pkgs = project.getNameLookup().findPackageFragments("p2", false);
		assertElementsEqual(
			"Didn't find p2",
			"p2 [in src [in P]]",
			pkgs);
	} finally {
		this.deleteProject("P");
	}
}
}

