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

public class AbstractMethodTests extends Tests {
	private static String[] EXCLUDED_TESTS = {};
	
	public AbstractMethodTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new FilteredTestSuite(EXCLUDED_TESTS);
		suite.addTestSuite(AbstractMethodTests.class);
		return suite;
	}

	public void test001() {
		//----------------------------
		//           Step 1
		//----------------------------
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1");
		env.addExternalJar(project1Path, Util.getJavaClassLib());
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(project1Path, "");
		
		IPath root1 = env.addPackageFragmentRoot(project1Path, "src");
		env.setOutputFolder(project1Path, "bin");
		
		env.addClass(root1, "p1", "IX",
			"package p1;\n" +
			"public interface IX {\n" +
			"   public abstract void foo(IX x);\n" +
			"}\n"
			);
			
		IPath classX = env.addClass(root1, "p2", "X",
			"package p2;\n" +
			"import p1.*;\n" +
			"public abstract class X implements IX {\n" +
			"   public void foo(IX x){}\n" +
			"}\n"
			);
		
			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2");
		env.addExternalJar(project2Path, Util.getJavaClassLib());
		env.addRequiredProject(project2Path, project1Path);
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(project2Path, "");
		
		IPath root2 = env.addPackageFragmentRoot(project2Path, "src");
		env.setOutputFolder(project2Path, "bin");
			
		IPath classY =env.addClass(root2, "p3", "Y",
			"package p3;\n" +
			"import p2.*;\n" +
			"public class Y extends X{\n" +
			"}\n"
			);
			
		fullBuild();
		expectingNoProblems();
		
		//----------------------------
		//           Step 2
		//----------------------------
		env.addClass(root1, "p2", "X",
			"package p2;\n" +
			"import p1.*;\n" +
			"public abstract class X implements IX {\n" +
			"   public void foo(I__X x){}\n" +
			"}\n"
			);
			
		incrementalBuild();
		expectingOnlySpecificProblemFor(classX, new Problem("X.foo(I__X)", "I__X cannot be resolved (or is not a valid type) for the argument x of the method foo", classX));
		expectingOnlySpecificProblemFor(classY, new Problem("Y", "Class must implement the inherited abstract method X.foo(IX)", classY));
		
		//----------------------------
		//           Step 3
		//----------------------------
		env.addClass(root1, "p2", "X",
			"package p2;\n" +
			"import p1.*;\n" +
			"public abstract class X implements IX {\n" +
			"   public void foo(IX x){}\n" +
			"}\n"
			);
		
		incrementalBuild();
		expectingNoProblems();
	}
}