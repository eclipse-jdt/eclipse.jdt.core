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

public class PackageTests extends Tests {
	private static String[] EXCLUDED_TESTS = {
	};
	
	public PackageTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new FilteredTestSuite(EXCLUDED_TESTS);
		suite.addTestSuite(PackageTests.class);
		return suite;
	}
	
	/**
	 * Bugs 6564
	 */
	public void testPackageProblem(){
		//----------------------------
		//           Step 1
		//----------------------------
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());
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

