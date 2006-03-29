/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

public class PackageTests extends BuilderTests {
	
	public PackageTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		return buildTestSuite(PackageTests.class);
	}
	
	/**
	 * Bugs 6564
	 */
	public void testPackageProblem() throws JavaModelException {
		//----------------------------
		//           Step 1
		//----------------------------
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		
		env.addClass(src, "pack", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package pack;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		env.addClass(src2, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		env.addClass(src2, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class Y extends p1.X {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		env.addClass(src2, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class Z extends p2.Y {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		
		fullBuild();
		expectingNoProblems();
		
		//----------------------------
		//           Step 2
		//----------------------------
		env.removeClass(env.getPackagePath(src, "pack"), "X"); //$NON-NLS-1$ //$NON-NLS-2$
		env.removePackage(src2, "p3"); //$NON-NLS-1$
			
		incrementalBuild();
		expectingNoProblems();
	}
}

