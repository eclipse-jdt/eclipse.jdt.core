/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import junit.framework.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.tests.util.Util;

public class CompilationUnitLocationTests extends Tests {
	private static String[] EXCLUDED_TESTS = {
		"CompilationUnitLocationTests", "testWrongCompilationUnitLocation"
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
	public void testWrongCompilationUnitLocation() {
		//----------------------------
		//           Step 1
		//----------------------------
		IPath projectPath = env.addProject("Project");
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		env.removePackageFragmentRoot(projectPath, "");
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		IPath bin = env.setOutputFolder(projectPath, "bin");
		IPath x = env.addClass(root, "", "X",
			"public class X {\n"+
			"}\n"
			);

		
		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(bin.append("X.class"));
		
		//----------------------------
		//           Step 2
		//----------------------------
		env.addClass(root, "", "X",
			"package p1;\n"+
			"public class X {\n"+
			"}\n"
			);
			
		incrementalBuild();
		expectingProblemsFor(x);
		expectingNoPresenceOf(bin.append("X.class"));
	}
}

