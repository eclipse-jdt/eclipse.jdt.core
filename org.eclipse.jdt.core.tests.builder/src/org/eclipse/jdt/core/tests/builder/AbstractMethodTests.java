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
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJar(project1Path, Util.getJavaClassLib());
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(project1Path, ""); //$NON-NLS-1$
		
		IPath root1 = env.addPackageFragmentRoot(project1Path, "src"); //$NON-NLS-1$
		env.setOutputFolder(project1Path, "bin"); //$NON-NLS-1$
		
		env.addClass(root1, "p1", "IX", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public interface IX {\n" + //$NON-NLS-1$
			"   public abstract void foo(IX x);\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		IPath classX = env.addClass(root1, "p2", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n" + //$NON-NLS-1$
			"import p1.*;\n" + //$NON-NLS-1$
			"public abstract class X implements IX {\n" + //$NON-NLS-1$
			"   public void foo(IX x){}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJar(project2Path, Util.getJavaClassLib());
		env.addRequiredProject(project2Path, project1Path);
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(project2Path, ""); //$NON-NLS-1$
		
		IPath root2 = env.addPackageFragmentRoot(project2Path, "src"); //$NON-NLS-1$
		env.setOutputFolder(project2Path, "bin"); //$NON-NLS-1$
			
		IPath classY =env.addClass(root2, "p3", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n" + //$NON-NLS-1$
			"import p2.*;\n" + //$NON-NLS-1$
			"public class Y extends X{\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		fullBuild();
		expectingNoProblems();
		
		//----------------------------
		//           Step 2
		//----------------------------
		env.addClass(root1, "p2", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n" + //$NON-NLS-1$
			"import p1.*;\n" + //$NON-NLS-1$
			"public abstract class X implements IX {\n" + //$NON-NLS-1$
			"   public void foo(I__X x){}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		incrementalBuild();
		expectingOnlySpecificProblemFor(classX, new Problem("X.foo(I__X)", "I__X cannot be resolved (or is not a valid type) for the argument x of the method foo", classX)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingOnlySpecificProblemFor(classY, new Problem("Y", "Class must implement the inherited abstract method X.foo(IX)", classY)); //$NON-NLS-1$ //$NON-NLS-2$
		
		//----------------------------
		//           Step 3
		//----------------------------
		env.addClass(root1, "p2", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n" + //$NON-NLS-1$
			"import p1.*;\n" + //$NON-NLS-1$
			"public abstract class X implements IX {\n" + //$NON-NLS-1$
			"   public void foo(IX x){}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
		incrementalBuild();
		expectingNoProblems();
	}
}