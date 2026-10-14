/*******************************************************************************
* Copyright (c) 2026 Advantest Europe GmbH and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
*
* Contributors:
*     Srikanth Sankaran - initial implementation
*******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import junit.framework.Test;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public class IncrementalTests28 extends BuilderTests {

	public IncrementalTests28(String name) {
		super(name);
	}

	public static Test suite() {
		return AbstractCompilerTest.buildUniqueComplianceTestSuite(IncrementalTests28.class, ClassFileConstants.JDK28);
	}

	private IPath setupPreviewProject() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "28");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		IJavaProject javaProject = env.getJavaProject(projectPath);
		javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");
		return projectPath;
	}

	// Turning an identity class into a value class must cause dependents that
	// synchronize on its instances to be recompiled and flagged.
	public void testIdentityToValueClass_SynchronizedDependent() throws JavaModelException {
		IPath projectPath = setupPreviewProject();
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "", "X",
				"public class X {\n" +
				"}\n");
		env.addClass(root, "", "Y",
				"public class Y {\n" +
				"	void foo(X x) {\n" +
				"		synchronized (x) {\n" +
				"		}\n" +
				"	}\n" +
				"}\n");

		fullBuild(projectPath);
		expectingNoProblems();

		// X becomes a value class - only X.java is touched
		env.addClass(root, "", "X",
				"public value class X {\n" +
				"}\n");

		incrementalBuild(projectPath);
		expectingOnlySpecificProblemFor(
				root.append("Y.java"),
				new Problem("", "Illegal attempt to synchronize on an instance of a value class",
						root.append("Y.java"), 50, 51, 180, org.eclipse.core.resources.IMarker.SEVERITY_ERROR));
		expectingNoProblemsFor(root.append("X.java"));

		// Revert: X back to identity class, Y must be clean again
		env.addClass(root, "", "X",
				"public class X {\n" +
				"}\n");
		incrementalBuild(projectPath);
		expectingNoProblems();
	}
}