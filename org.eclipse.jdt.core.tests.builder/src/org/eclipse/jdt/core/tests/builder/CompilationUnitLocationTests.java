/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import junit.framework.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

public class CompilationUnitLocationTests extends Tests {
	private static String[] EXCLUDED_TESTS = {
		"CompilationUnitLocationTests", "testWrongCompilationUnitLocation" //$NON-NLS-1$ //$NON-NLS-2$
	};
	
	public CompilationUnitLocationTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new FilteredTestSuite(EXCLUDED_TESTS);
		suite.addTestSuite(CompilationUnitLocationTests.class);
		return suite;
	}
	
	/**
	 * Bugs 6461 
	 */
	public void testWrongCompilationUnitLocation() throws JavaModelException {
		//----------------------------
		//           Step 1
		//----------------------------
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		IPath bin = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		IPath x = env.addClass(root, "", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"public class X {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		
		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(bin.append("X.class")); //$NON-NLS-1$
		
		//----------------------------
		//           Step 2
		//----------------------------
		env.addClass(root, "", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		incrementalBuild();
		expectingProblemsFor(x);
		expectingNoPresenceOf(bin.append("X.class")); //$NON-NLS-1$
	}
}

