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

import java.util.Hashtable;

import junit.framework.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

/**
 * Basic tests of the image builder.
 */
public class BasicBuildTests extends Tests {
	public BasicBuildTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		return new TestSuite(BasicBuildTests.class);
	}
	
	public void testBuild() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		
		env.addClass(root, "p1", "Hello", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class Hello {\n"+ //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(\"Hello world\");\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		incrementalBuild(projectPath);
	}
	
	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=23894
	 */
	// TODO (frederic) broken with changes to https://bugs.eclipse.org/bugs/show_bug.cgi?id=91716
	public void _testToDoMarker() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo"); //$NON-NLS-1$
		
		JavaCore.setOptions(newOptions);
		
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath pathToA = env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"//todo nothing\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingOnlySpecificProblemFor(pathToA, new Problem("A", "todo nothing", pathToA)); //$NON-NLS-1$ //$NON-NLS-2$
		
		JavaCore.setOptions(options);
	}
}
