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


public class MultiProjectTests extends Tests {
	private static String[] EXCLUDED_TESTS = {};
	
	public MultiProjectTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new FilteredTestSuite(EXCLUDED_TESTS);
		suite.addTestSuite(MultiProjectTests.class);
		return suite;
	}
	
	public void testCompileOnlyDependent() {
		//----------------------------
		//           Step 1
		//----------------------------
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1");
		env.addExternalJar(project1Path, Util.getJavaClassLib());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, "");
		env.addClass(root1, "", "A",
			"public class A {\n"+
			"}\n"
			);
			
			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2");
		env.addExternalJar(project2Path, Util.getJavaClassLib());
		env.addRequiredProject(project2Path, project1Path);
		IPath root2 = env.getPackageFragmentRootPath(project2Path, "");
		env.addClass(root2, "", "B",
			"public class B extends A {\n"+
			"}\n"
			);
			
			//----------------------------
			//         Project3
			//----------------------------
		IPath project3Path = env.addProject("Project3");
		env.addExternalJar(project3Path, Util.getJavaClassLib());
		IPath root3 = env.getPackageFragmentRootPath(project3Path, "");
		env.addClass(root3, "", "C",
			"public class C {\n"+
			"}\n"
			);
		
		fullBuild();
		expectingNoProblems();
		
		//----------------------------
		//           Step 2
		//----------------------------
		env.addClass(root1, "", "A",
			"public class A {\n"+
			"   int x;\n"+
			"}\n"
			);
			
		incrementalBuild();
		expectingCompiledClasses(new String[]{"A", "B"});
	}
	
	public void testRemoveField() {
		//----------------------------
		//           Step 1
		//----------------------------
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1");
		env.addExternalJar(project1Path, Util.getJavaClassLib());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, "");
		env.addClass(root1, "", "A",
			"public class A {\n"+
			"   public int x;\n"+
			"}\n"
			);
			
			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2");
		env.addExternalJar(project2Path, Util.getJavaClassLib());
		env.addRequiredProject(project2Path, project1Path);
		IPath root2 = env.getPackageFragmentRootPath(project2Path, "");
		IPath b = env.addClass(root2, "", "B",
			"public class B {\n"+
			"   public void foo(){\n"+
			"      int x = new A().x;\n"+
			"   }\n"+
			"}\n"
			);
		
		fullBuild();
		expectingNoProblems();
		
		//----------------------------
		//           Step 2
		//----------------------------
		env.addClass(root1, "", "A",
			"public class A {\n"+
			"}\n"
			);
			
		incrementalBuild();
		expectingSpecificProblemFor(b, new Problem("B.foo()", "x cannot be resolved or is not a field", b));
	}
}