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

/**
 * Basic tests of the image builder.
 */
public class JCLTests extends Tests {
	private static String[] EXCLUDED_TESTS = {
		"JCLTests", "testNewJCL" //$NON-NLS-1$ //$NON-NLS-2$
	};
	
	public JCLTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new FilteredTestSuite(EXCLUDED_TESTS);
		suite.addTestSuite(JCLTests.class);
		return suite;
	}
	
	
	public void testNewJCL() {
		//----------------------------
		//           Step 1
		//----------------------------
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$

		IPath root = env.getPackageFragmentRootPath(projectPath, ""); //$NON-NLS-1$
		fullBuild();
		expectingNoProblems();
		
		//----------------------------
		//           Step 2
		//----------------------------
		IPath object = env.addClass(root, "java.lang", "Object", //$NON-NLS-1$ //$NON-NLS-2$
			"package java.lang;\n" + //$NON-NLS-1$
			"public class Object {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			

		incrementalBuild();
		expectingSpecificProblemFor(object, new Problem("java.lang", "This compilation unit indirectly references the missing type java.lang.Throwable (typically some required class file is referencing a type outside the classpath)", object)); //$NON-NLS-1$ //$NON-NLS-2$
		
		//----------------------------
		//           Step 3
		//----------------------------
		IPath throwable = env.addClass(root, "java.lang", "Throwable", //$NON-NLS-1$ //$NON-NLS-2$
			"package java.lang;\n" + //$NON-NLS-1$
			"public class Throwable {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			

		incrementalBuild();
		expectingSpecificProblemFor(object, new Problem("java.lang", "This compilation unit indirectly references the missing type java.lang.RuntimeException (typically some required class file is referencing a type outside the classpath)", object)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(throwable, new Problem("java.lang", "This compilation unit indirectly references the missing type java.lang.RuntimeException (typically some required class file is referencing a type outside the classpath)", throwable)); //$NON-NLS-1$ //$NON-NLS-2$
	}
}