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
import org.eclipse.jdt.core.JavaModelException;
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
	
	public void testCompileOnlyDependent() throws JavaModelException {
		//----------------------------
		//           Step 1
		//----------------------------
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJar(project1Path, Util.getJavaClassLib());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, ""); //$NON-NLS-1$
		env.addClass(root1, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJar(project2Path, Util.getJavaClassLib());
		env.addRequiredProject(project2Path, project1Path);
		IPath root2 = env.getPackageFragmentRootPath(project2Path, ""); //$NON-NLS-1$
		env.addClass(root2, "", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"public class B extends A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
			//----------------------------
			//         Project3
			//----------------------------
		IPath project3Path = env.addProject("Project3"); //$NON-NLS-1$
		env.addExternalJar(project3Path, Util.getJavaClassLib());
		IPath root3 = env.getPackageFragmentRootPath(project3Path, ""); //$NON-NLS-1$
		env.addClass(root3, "", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"public class C {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
		fullBuild();
		expectingNoProblems();
		
		//----------------------------
		//           Step 2
		//----------------------------
		env.addClass(root1, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {\n"+ //$NON-NLS-1$
			"   int x;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		incrementalBuild();
		expectingCompiledClasses(new String[]{"A", "B"}); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void testRemoveField() throws JavaModelException {
		//----------------------------
		//           Step 1
		//----------------------------
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJar(project1Path, Util.getJavaClassLib());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, ""); //$NON-NLS-1$
		env.addClass(root1, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {\n"+ //$NON-NLS-1$
			"   public int x;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJar(project2Path, Util.getJavaClassLib());
		env.addRequiredProject(project2Path, project1Path);
		IPath root2 = env.getPackageFragmentRootPath(project2Path, ""); //$NON-NLS-1$
		IPath b = env.addClass(root2, "", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"public class B {\n"+ //$NON-NLS-1$
			"   public void foo(){\n"+ //$NON-NLS-1$
			"      int x = new A().x;\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
		fullBuild();
		expectingNoProblems();
		
		//----------------------------
		//           Step 2
		//----------------------------
		env.addClass(root1, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		incrementalBuild();
		expectingSpecificProblemFor(b, new Problem("B.foo()", "x cannot be resolved or is not a field", b)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void _testCycle1() throws JavaModelException {
		//----------------------------
		//         Project1
		//----------------------------
		IPath p1 = env.addProject("P1"); //$NON-NLS-1$
		env.addExternalJar(p1, Util.getJavaClassLib());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p1, ""); //$NON-NLS-1$
		IPath root1 = env.addPackageFragmentRoot(p1, "src"); //$NON-NLS-1$
		env.setOutputFolder(p1, "bin"); //$NON-NLS-1$
		
		env.addClass(root1, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"import p2.Y;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"  public void bar(Y y, int i){\n"+ //$NON-NLS-1$
			"    y.zork();\n"+ //$NON-NLS-1$
			"  }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
		//----------------------------
		//         Project2
		//----------------------------
		IPath p2 = env.addProject("P2"); //$NON-NLS-1$
		env.addExternalJar(p2, Util.getJavaClassLib());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p2, ""); //$NON-NLS-1$
		IPath root2 = env.addPackageFragmentRoot(p2, "src"); //$NON-NLS-1$
		env.setOutputFolder(p2, "bin"); //$NON-NLS-1$
		
		env.addClass(root2, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.X\n"+ //$NON-NLS-1$
			"import p3.Z;\n"+ //$NON-NLS-1$
			"public class Y {\n"+ //$NON-NLS-1$
			"  public X zork(){\n"+ //$NON-NLS-1$
			"    X x = foo();\n"+ //$NON-NLS-1$
			"    x.bar(this);\n"+ //$NON-NLS-1$
			"    return x;\n"+ //$NON-NLS-1$
			"  }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		//----------------------------
		//         Project3
		//----------------------------
		IPath p3 = env.addProject("P3"); //$NON-NLS-1$
		env.addExternalJar(p3, Util.getJavaClassLib());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p3, ""); //$NON-NLS-1$
		IPath root3 = env.addPackageFragmentRoot(p3, "src"); //$NON-NLS-1$
		env.setOutputFolder(p3, "bin"); //$NON-NLS-1$
		
		env.addClass(root1, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"import p1.X;\n"+ //$NON-NLS-1$
			"public class Z {\n"+ //$NON-NLS-1$
			"  public X foo(){\n"+ //$NON-NLS-1$
			"    return null;\n"+ //$NON-NLS-1$
			"  }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
		// for Project1
		env.addRequiredProject(p1, p2);
		env.addRequiredProject(p1, p3);
		// for Project2
		env.addRequiredProject(p2, p1);
		env.addRequiredProject(p2, p3);
		// for Project3
		env.addRequiredProject(p3, p1);

		fullBuild();
		expectingNoProblems();
	}
}