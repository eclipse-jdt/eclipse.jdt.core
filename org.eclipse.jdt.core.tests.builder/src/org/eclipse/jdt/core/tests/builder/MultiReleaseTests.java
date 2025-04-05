/*******************************************************************************
 * Copyright (c) 2025 IBM Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christoph Läubrich - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import junit.framework.Test;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

public class MultiReleaseTests extends BuilderTests{

	public MultiReleaseTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(MultiReleaseTests.class);
	}

	public void testMultiReleaseCompile() throws JavaModelException {
		IPath projectPath = env.addProject("P"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath defaultSrc = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		IClasspathAttribute[] extraAttributes = new IClasspathAttribute[] {JavaCore.newClasspathAttribute(IClasspathAttribute.RELEASE, "9")} ;
		IPath release9Src = env.addPackageFragmentRoot(projectPath, "src9",extraAttributes); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(defaultSrc, "p", "MultiReleaseType", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class MultiReleaseType {}" //$NON-NLS-1$
			);

		env.addClass(release9Src, "p", "MultiReleaseType", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class MultiReleaseType {}" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();
		IPath defaultReleaseClass = projectPath.append("bin/p/MultiReleaseType.class");
		IPath java9ReleaseClass = projectPath.append("bin/META-INF/versions/9/p/MultiReleaseType.class");
		expectingPresenceOf(defaultReleaseClass);
		expectingPresenceOf(java9ReleaseClass);
	}

}
