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
 * Basic execution tests of the image builder.
 */
public class ExecutionTests extends Tests {
	public ExecutionTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		return new TestSuite(ExecutionTests.class);
	}
	
	public void testSuccess() {
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
		expectingNoProblems();
		executeClass(projectPath, "p1.Hello", "Hello world\r\n", "");
	}
	
	public void testFailure() {
		IPath projectPath = env.addProject("Project");
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		fullBuild(projectPath);
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");
		
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");
		
		IPath helloPath = env.addClass(root, "p1", "Hello",
			"package p1;\n"+
			"public class Hello {\n"+
			"   public static void main(String args[]) {\n"+
			"      System.out.println(\"Hello world\")\n"+
			"   }\n"+
			"}\n"
			);
		// public static void main(String args[]) {
		//    System.out.println("Hello world") <-- missing ";"
		// }
			
		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(helloPath);
		executeClass(projectPath, "p1.Hello", "",
			"java.lang.Error: Unresolved compilation problem: \n" + 
			"	Syntax error on token \"}\", \"++\", \"--\" expected\n"
		);
	}
}