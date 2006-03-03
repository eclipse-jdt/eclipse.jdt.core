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

import junit.framework.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.Util;

public class AbstractMethodTests extends BuilderTests {
	
	public AbstractMethodTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		return buildTestSuite(AbstractMethodTests.class);
	}

	/**
	 * Check behavior in 1.2 target mode (NO generated default abstract method)
	 */
	public void test001() throws JavaModelException {
		//----------------------------
		//           Step 1
		//----------------------------
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(project1Path, ""); //$NON-NLS-1$
		
		env.setOutputFolder(project1Path, "bin"); //$NON-NLS-1$
		IPath root1 = env.addPackageFragmentRoot(project1Path, "src"); //$NON-NLS-1$
		
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
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
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
		expectingOnlySpecificProblemFor(classX, new Problem("X.foo(I__X)", "I__X cannot be resolved to a type", classX, 84, 88, CategorizedProblem.CAT_TYPE)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingOnlySpecificProblemFor(classY, new Problem("Y", "The type Y must implement the inherited abstract method IX.foo(IX)", classY, 38, 39, CategorizedProblem.CAT_MEMBER)); //$NON-NLS-1$ //$NON-NLS-2$
		
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
	
	/**
	 * Check behavior in 1.1 target mode (generated default abstract method)
	 */
	public void test002() throws JavaModelException {
		//----------------------------
		//           Step 1
		//----------------------------
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		env.getJavaProject(project1Path).setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_1); // need default abstract method
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(project1Path, ""); //$NON-NLS-1$
		
		env.setOutputFolder(project1Path, "bin"); //$NON-NLS-1$
		IPath root1 = env.addPackageFragmentRoot(project1Path, "src"); //$NON-NLS-1$
		
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
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
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
		expectingOnlySpecificProblemFor(classX, new Problem("X.foo(I__X)", "I__X cannot be resolved to a type", classX, 84, 88, CategorizedProblem.CAT_TYPE)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingOnlySpecificProblemFor(classY, new Problem("Y", "The type Y must implement the inherited abstract method X.foo(IX)", classY, 38, 39, CategorizedProblem.CAT_MEMBER)); //$NON-NLS-1$ //$NON-NLS-2$
		
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
