/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.performance;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.builder.TestingEnvironment;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.Region;

public class RegionPerformanceTests extends TestCase {

	// Log file streams
	protected IProject project;
	private TestingEnvironment env = null;

	public RegionPerformanceTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(RegionPerformanceTests.class);
	}

	protected IJavaProject createJavaProject(final String projectName) throws Exception {
		IPath projectPath = this.env.addProject(projectName, "1.8");
		this.env.addExternalJars(projectPath, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		this.env.removePackageFragmentRoot(projectPath, "");
		this.env.addPackageFragmentRoot(projectPath, "src");
		this.env.setOutputFolder(projectPath, "bin");
		final IJavaProject javaProj = this.env.getJavaProject(projectPath);
		return javaProj;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		if (this.env == null) {
			this.env = new TestingEnvironment();
			this.env.openEmptyWorkspace();
		}
		this.env.resetWorkspace();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		this.env.resetWorkspace();
		JavaCore.setOptions(JavaCore.getDefaultOptions());
	}

	public void testRegion() throws Exception {
		IJavaProject jproj = createJavaProject("RegionTest");
		IProject proj = jproj.getProject();
		IPath projPath = proj.getFullPath();
		IPath root = projPath.append("src");

		for (int idx = 0; idx < 1000; idx++) {
			this.env.addClass(root, "test", "Foo" + idx, "package test;\n\n" + "public class Foo" + idx + " {\n" + "}");
		}

		this.env.fullBuild();

		for (int idx = 0; idx < 10; idx++) {
			startMeasuring();
			Region region = new Region();
			IPackageFragment[] fragments = jproj.getPackageFragments();

			for (IPackageFragment next : fragments) {
				IJavaElement[] children = next.getChildren();

				for (IJavaElement nextChild : children) {
					region.add(nextChild);
				}
			}
			stopMeasuring();
		}

		// Commit
		commitMeasurements();
		assertPerformance();
	}
}
