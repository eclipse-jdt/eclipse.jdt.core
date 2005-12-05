/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import junit.framework.*;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.resources.IContainer;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.builder.JavaBuilder;

/**
 * Basic tests of the image builder.
 */
public class CleanOutputFolderTests extends Tests {

	public CleanOutputFolderTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(CleanOutputFolderTests.class);
	}

	public void test0001() throws JavaModelException {
		IPath projectPath = env.addProject("P"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		env.addPackageFragmentRoot(projectPath, "src1", null, "bin1"); //$NON-NLS-1$ //$NON-NLS-2$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		JavaProject p = (JavaProject) env.getJavaProject(projectPath);
		IContainer[][] folders = JavaBuilder.computeCleanedFolders(p);
		IContainer[] allContent = folders[0];
		assertTrue("found output folders", allContent.length == 1 //$NON-NLS-1$
			&& allContent[0].getFullPath().equals(projectPath.append("bin1"))); //$NON-NLS-1$
		IContainer[] onlyClassFiles = folders[1];
		assertTrue("found output folders", onlyClassFiles.length == 0); //$NON-NLS-1$
	}

	public void test0002() throws JavaModelException {
		IPath projectPath = env.addProject("P"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		env.addPackageFragmentRoot(projectPath, "src1", null, "bin1"); //$NON-NLS-1$ //$NON-NLS-2$
		env.addPackageFragmentRoot(projectPath, "src2", null, "src2"); //$NON-NLS-1$ //$NON-NLS-2$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		JavaProject p = (JavaProject) env.getJavaProject(projectPath);
		IContainer[][] folders = JavaBuilder.computeCleanedFolders(p);
		IContainer[] allContent = folders[0];
		assertTrue("found output folders", allContent.length == 1 //$NON-NLS-1$
			&& allContent[0].getFullPath().equals(projectPath.append("bin1"))); //$NON-NLS-1$
		IContainer[] onlyClassFiles = folders[1];
		assertTrue("found output folders", onlyClassFiles.length == 1 //$NON-NLS-1$
			&& onlyClassFiles[0].getFullPath().equals(projectPath.append("src2"))); //$NON-NLS-1$
	}

	public void test0003() throws JavaModelException {
		IPath projectPath = env.addProject("P"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		env.addPackageFragmentRoot(projectPath, "src/f1", null, null); //$NON-NLS-1$
		env.addPackageFragmentRoot(projectPath, "src", new IPath[]{new Path("f1/")}, null); //$NON-NLS-1$ //$NON-NLS-2$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		JavaProject p = (JavaProject) env.getJavaProject(projectPath);
		IContainer[][] folders = JavaBuilder.computeCleanedFolders(p);
		IContainer[] allContent = folders[0];
		assertTrue("found output folders", allContent.length == 1 //$NON-NLS-1$
			&& allContent[0].getFullPath().equals(projectPath.append("bin"))); //$NON-NLS-1$
		IContainer[] onlyClassFiles = folders[1];
		assertTrue("found output folders", onlyClassFiles.length == 0); //$NON-NLS-1$
	}
}
