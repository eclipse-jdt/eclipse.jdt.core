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

import java.util.Hashtable;

import junit.framework.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
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
	
	public void testBuild() {
		IPath projectPath = env.addProject("Project");
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		fullBuild(projectPath);
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");
		
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");
		
		env.addClass(root, "p1", "Hello",
			"package p1;\n"+
			"public class Hello {\n"+
			"   public static void main(String args[]) {\n"+
			"      System.out.println(\"Hello world\");\n"+
			"   }\n"+
			"}\n"
			);
			
		incrementalBuild(projectPath);
	}
	
	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=23894
	 */
	public void testToDoMarker() {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_TASK_TAGS, "todo");
		
		JavaCore.setOptions(newOptions);
		
		IPath projectPath = env.addProject("Project");
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		IPath pathToA = env.addClass(root, "p", "A",
			"package p; \n"+
			"//todo nothing\n"+
			"public class A {\n"+
			"}");

		fullBuild(projectPath);
		expectingOnlySpecificProblemFor(pathToA, new Problem("A", "todo nothing", pathToA));
		
		JavaCore.setOptions(options);
	}
}