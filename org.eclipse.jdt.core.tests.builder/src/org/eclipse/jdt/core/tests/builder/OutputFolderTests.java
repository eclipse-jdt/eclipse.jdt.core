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

/**
 * Basic tests of the image builder.
 */
public class OutputFolderTests extends Tests {
	private static String[] EXCLUDED_TESTS = {};
	
	public OutputFolderTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new FilteredTestSuite(EXCLUDED_TESTS);
		suite.addTestSuite(OutputFolderTests.class);
		return suite;
	}
	
	public void testDeleteOutputFolder() {
		//----------------------------
		//           Step 1: Setup project and full build
		//----------------------------
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		
		IPath root = env.getPackageFragmentRootPath(projectPath, ""); //$NON-NLS-1$
		IPath bin = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"public class Test {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		env.addFile(root, "Test.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$
		
			
		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[]{
			bin,
			bin.append("Test.class"), //$NON-NLS-1$
			bin.append("Test.txt") //$NON-NLS-1$
		});
		
		//----------------------------
		//           Step 2: Incremental build
		//----------------------------
		/* Disabled as the incremental builder doesn't detect the removal of the output folder
		env.removeFolder(bin);
		
		incrementalBuild();
		expectingPresenceOf(new IPath[]{
			bin,
			bin.append("Test.class"), //$NON-NLS-1$
			bin.append("Test.txt") //$NON-NLS-1$
		});
		*/
		
		//----------------------------
		//           Step 3: Full build
		//----------------------------
		env.removeFolder(bin);
		
		fullBuild();
		expectingPresenceOf(new IPath[]{
			bin,
			bin.append("Test.class"), //$NON-NLS-1$
			bin.append("Test.txt") //$NON-NLS-1$
		});
	}
	
	public void testChangeOutputFolder() {
		//----------------------------
		//           Step 1: Setup project and full build
		//----------------------------
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		IPath bin1 = env.setOutputFolder(projectPath, "bin1"); //$NON-NLS-1$

		env.addClass(root, "p", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n" + //$NON-NLS-1$
			"public class Test {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[]{
			bin1,
			bin1.append("p").append("Test.class"), //$NON-NLS-1$ //$NON-NLS-2$
		});
		
		//----------------------------
		//           Step 2: Incremental build
		//----------------------------
		/* Disabled as the incremental builder doesn't detect the change of output folder
		IPath bin2 = env.setOutputFolder(projectPath, "bin2"); //$NON-NLS-1$
		
		incrementalBuild();
		
		expectingNoProblems();
		expectingPresenceOf(new IPath[]{
			bin2,
			bin2.append("p").append("Test.class"), //$NON-NLS-1$ //$NON-NLS-2$
		});
		*/
		
		//----------------------------
		//           Step 3: Full build
		//----------------------------
		IPath bin3 = env.setOutputFolder(projectPath, "bin3"); //$NON-NLS-1$
		
		fullBuild();
		
		expectingNoProblems();
		expectingPresenceOf(new IPath[]{
			bin3,
			bin3.append("p").append("Test.class"), //$NON-NLS-1$ //$NON-NLS-2$
		});
	}
}