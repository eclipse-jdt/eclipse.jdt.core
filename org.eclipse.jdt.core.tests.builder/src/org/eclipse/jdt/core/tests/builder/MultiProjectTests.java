/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.Util;


public class MultiProjectTests extends Tests {
	
	public MultiProjectTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		if (false) {
			TestSuite suite = new TestSuite(MultiProjectTests.class.getName());
			suite.addTest(new MultiProjectTests("testIgnoreIfBetterNonAccessibleRule1"));
			return suite;
		}
		return new TestSuite(MultiProjectTests.class);
	}
	
	public void testCompileOnlyDependent() throws JavaModelException {
		//----------------------------
		//           Step 1
		//----------------------------
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, ""); //$NON-NLS-1$
		env.addClass(root1, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
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
		env.addExternalJars(project3Path, Util.getJavaClassLibs());
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

	// 14103 - avoid recompiling unaffected sources in dependent projects
	public void testCompileOnlyStructuralDependent() throws JavaModelException {
		//----------------------------
		//           Step 1
		//----------------------------
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, ""); //$NON-NLS-1$
		env.addClass(root1, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.addClass(root1, "", "Unreferenced", //$NON-NLS-1$ //$NON-NLS-2$
			"public class Unreferenced {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
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
		env.addExternalJars(project3Path, Util.getJavaClassLibs());
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
		// non-structural change should not fool dependent projcts
		env.addClass(root1, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {\n"+ //$NON-NLS-1$
			"   // add comment (non-structural change)\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		// structural change, but no actual dependents
		env.addClass(root1, "", "Unreferenced", //$NON-NLS-1$ //$NON-NLS-2$
			"public class Unreferenced {\n"+ //$NON-NLS-1$
			"   int x; //structural change\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		incrementalBuild();
		expectingCompiledClasses(new String[]{"A", "Unreferenced"}); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void testRemoveField() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.IGNORE); //$NON-NLS-1$
		JavaCore.setOptions(options);

		//----------------------------
		//           Step 1
		//----------------------------
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
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
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
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
		expectingSpecificProblemFor(b, new Problem("B.foo()", "x cannot be resolved or is not a field", b, 61, 62, CategorizedProblem.CAT_MEMBER)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void testCompileOrder() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.CORE_CIRCULAR_CLASSPATH, JavaCore.WARNING); //$NON-NLS-1$
		
		JavaCore.setOptions(newOptions);
		
		//----------------------------
		//         Project1
		//----------------------------
		IPath p1 = env.addProject("P1"); //$NON-NLS-1$
		env.addExternalJars(p1, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p1, ""); //$NON-NLS-1$
		IPath root1 = env.addPackageFragmentRoot(p1, "src"); //$NON-NLS-1$
		env.setOutputFolder(p1, "bin"); //$NON-NLS-1$
		
		IPath c1 = env.addClass(root1, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"  W w;\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
		//----------------------------
		//         Project2
		//----------------------------
		IPath p2 = env.addProject("P2"); //$NON-NLS-1$
		env.addExternalJars(p2, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p2, ""); //$NON-NLS-1$
		IPath root2 = env.addPackageFragmentRoot(p2, "src"); //$NON-NLS-1$
		env.setOutputFolder(p2, "bin"); //$NON-NLS-1$
		
		IPath c2 = env.addClass(root2, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class Y {\n"+ //$NON-NLS-1$
			"  W w;\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		//----------------------------
		//         Project3
		//----------------------------
		IPath p3 = env.addProject("P3"); //$NON-NLS-1$
		env.addExternalJars(p3, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p3, ""); //$NON-NLS-1$
		IPath root3 = env.addPackageFragmentRoot(p3, "src"); //$NON-NLS-1$
		env.setOutputFolder(p3, "bin"); //$NON-NLS-1$
		
		IPath c3 = env.addClass(root3, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class Z {\n"+ //$NON-NLS-1$
			"  W w;\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
		env.setBuildOrder(new String[]{"P1", "P3", "P2"});//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		fullBuild();
		
		expectingCompilingOrder(new String[]{"p1.X", "p3.Z", "p2.Y"}); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		IPath workspaceRootPath = env.getWorkspaceRootPath();
		expectingOnlySpecificProblemsFor(workspaceRootPath,new Problem[]{
				new Problem("p3", "W cannot be resolved to a type", c3, 31, 32, CategorizedProblem.CAT_TYPE),//$NON-NLS-1$ //$NON-NLS-2$
				new Problem("p2", "W cannot be resolved to a type", c2, 31, 32, CategorizedProblem.CAT_TYPE),//$NON-NLS-1$ //$NON-NLS-2$
				new Problem("p1", "W cannot be resolved to a type", c1, 31, 32, CategorizedProblem.CAT_TYPE)//$NON-NLS-1$ //$NON-NLS-2$
		});	
		JavaCore.setOptions(options);
	}
	
	public void testCycle1() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.CORE_CIRCULAR_CLASSPATH, JavaCore.WARNING); //$NON-NLS-1$
		
		JavaCore.setOptions(newOptions);
		
		//----------------------------
		//         Project1
		//----------------------------
		IPath p1 = env.addProject("P1"); //$NON-NLS-1$
		env.addExternalJars(p1, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p1, ""); //$NON-NLS-1$
		IPath root1 = env.addPackageFragmentRoot(p1, "src"); //$NON-NLS-1$
		env.setOutputFolder(p1, "bin"); //$NON-NLS-1$
		
		env.addClass(root1, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"import p2.Y;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"  public void bar(Y y){\n"+ //$NON-NLS-1$
			"    y.zork();\n"+ //$NON-NLS-1$
			"  }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
		//----------------------------
		//         Project2
		//----------------------------
		IPath p2 = env.addProject("P2"); //$NON-NLS-1$
		env.addExternalJars(p2, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p2, ""); //$NON-NLS-1$
		IPath root2 = env.addPackageFragmentRoot(p2, "src"); //$NON-NLS-1$
		env.setOutputFolder(p2, "bin"); //$NON-NLS-1$
		
		env.addClass(root2, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.X;\n"+ //$NON-NLS-1$
			"import p3.Z;\n"+ //$NON-NLS-1$
			"public class Y extends Z{\n"+ //$NON-NLS-1$
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
		env.addExternalJars(p3, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p3, ""); //$NON-NLS-1$
		IPath root3 = env.addPackageFragmentRoot(p3, "src"); //$NON-NLS-1$
		env.setOutputFolder(p3, "bin"); //$NON-NLS-1$
		
		env.addClass(root3, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
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

		try {
			env.setBuildOrder(new String[]{"P1", "P2", "P3"});//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			fullBuild();
			
			expectingCompilingOrder(new String[]{"p1.X", "p2.Y", "p3.Z", "p1.X", "p2.Y", "p3.Z", "p1.X"});//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$//$NON-NLS-6$//$NON-NLS-7$
			expectingOnlySpecificProblemFor(p1,new Problem("p1", "A cycle was detected in the build path of project: P1", p1, -1, -1, -1));//$NON-NLS-1$ //$NON-NLS-2$
			expectingOnlySpecificProblemFor(p2,new Problem("p2", "A cycle was detected in the build path of project: P2", p2, -1, -1, -1));//$NON-NLS-1$ //$NON-NLS-2$
			expectingOnlySpecificProblemFor(p3,new Problem("p3", "A cycle was detected in the build path of project: P3", p3, -1, -1, -1));//$NON-NLS-1$ //$NON-NLS-2$
			
			JavaCore.setOptions(options);
		} finally {
			env.setBuildOrder(null);
		}
	}
	
	public void testCycle2() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.CORE_CIRCULAR_CLASSPATH, JavaCore.WARNING); //$NON-NLS-1$
		
		JavaCore.setOptions(newOptions);
		
		//----------------------------
		//         Project1
		//----------------------------
		IPath p1 = env.addProject("P1"); //$NON-NLS-1$
		env.addExternalJars(p1, Util.getJavaClassLibs());
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
		env.addExternalJars(p2, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p2, ""); //$NON-NLS-1$
		IPath root2 = env.addPackageFragmentRoot(p2, "src"); //$NON-NLS-1$
		env.setOutputFolder(p2, "bin"); //$NON-NLS-1$
		
		IPath c2 = env.addClass(root2, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.X;\n"+ //$NON-NLS-1$
			"import p3.Z;\n"+ //$NON-NLS-1$
			"public class Y extends Z{\n"+ //$NON-NLS-1$
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
		env.addExternalJars(p3, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p3, ""); //$NON-NLS-1$
		IPath root3 = env.addPackageFragmentRoot(p3, "src"); //$NON-NLS-1$
		env.setOutputFolder(p3, "bin"); //$NON-NLS-1$
		
		env.addClass(root3, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
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

		try {
			env.setBuildOrder(new String[]{"P1", "P2", "P3"});//$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ 
			fullBuild();
			
			expectingCompilingOrder(new String[]{"p1.X", "p2.Y", "p3.Z", "p1.X", "p2.Y", "p3.Z", "p1.X"});//$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$//$NON-NLS-5$ //$NON-NLS-6$//$NON-NLS-7$
			expectingOnlySpecificProblemFor(p1,new Problem("p1", "A cycle was detected in the build path of project: P1", p1, -1, -1, -1));//$NON-NLS-1$ //$NON-NLS-2$
			expectingOnlySpecificProblemsFor(p2,new Problem[]{
					new Problem("p2", "The method bar(Y, int) in the type X is not applicable for the arguments (Y)", c2, 106, 109, CategorizedProblem.CAT_MEMBER),//$NON-NLS-1$ //$NON-NLS-2$
					new Problem("p2", "A cycle was detected in the build path of project: P2", p2, -1, -1, -1)//$NON-NLS-1$ //$NON-NLS-2$
			});
			expectingOnlySpecificProblemFor(p3,new Problem("p3", "A cycle was detected in the build path of project: P3", p3, -1, -1, -1));//$NON-NLS-1$ //$NON-NLS-2$
			
			JavaCore.setOptions(options);
		} finally {
			env.setBuildOrder(null);
		}
	}
	
	public void testCycle3() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.CORE_CIRCULAR_CLASSPATH, JavaCore.WARNING); //$NON-NLS-1$
		
		JavaCore.setOptions(newOptions);
		
		//----------------------------
		//         Project1
		//----------------------------
		IPath p1 = env.addProject("P1"); //$NON-NLS-1$
		env.addExternalJars(p1, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p1, ""); //$NON-NLS-1$
		IPath root1 = env.addPackageFragmentRoot(p1, "src"); //$NON-NLS-1$
		env.setOutputFolder(p1, "bin"); //$NON-NLS-1$
		
		env.addClass(root1, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"import p2.Y;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"  public void bar(Y y){\n"+ //$NON-NLS-1$
			"    y.zork();\n"+ //$NON-NLS-1$
			"  }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
		//----------------------------
		//         Project2
		//----------------------------
		IPath p2 = env.addProject("P2"); //$NON-NLS-1$
		env.addExternalJars(p2, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p2, ""); //$NON-NLS-1$
		IPath root2 = env.addPackageFragmentRoot(p2, "src"); //$NON-NLS-1$
		env.setOutputFolder(p2, "bin"); //$NON-NLS-1$
		
		IPath c2 = env.addClass(root2, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.X;\n"+ //$NON-NLS-1$
			"import p3.Z;\n"+ //$NON-NLS-1$
			"public class Y extends Z{\n"+ //$NON-NLS-1$
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
		env.addExternalJars(p3, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p3, ""); //$NON-NLS-1$
		IPath root3 = env.addPackageFragmentRoot(p3, "src"); //$NON-NLS-1$
		env.setOutputFolder(p3, "bin"); //$NON-NLS-1$
		
		env.addClass(root3, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
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

		try {
			env.setBuildOrder(new String[]{"P1", "P2", "P3"});//$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ 
			fullBuild();
			
			expectingCompilingOrder(new String[]{"p1.X", "p2.Y", "p3.Z", "p1.X", "p2.Y", "p3.Z", "p1.X"});//$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$//$NON-NLS-5$ //$NON-NLS-6$//$NON-NLS-7$
			expectingOnlySpecificProblemFor(p1,new Problem("p1", "A cycle was detected in the build path of project: P1", p1, -1, -1, -1));//$NON-NLS-1$ //$NON-NLS-2$
			expectingOnlySpecificProblemFor(p2,new Problem("p2", "A cycle was detected in the build path of project: P2", p2, -1, -1, -1));//$NON-NLS-1$ //$NON-NLS-2$
			expectingOnlySpecificProblemFor(p3,new Problem("p3", "A cycle was detected in the build path of project: P3", p3, -1, -1, -1));//$NON-NLS-1$ //$NON-NLS-2$
			
			env.addClass(root1, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
				"package p1;\n"+ //$NON-NLS-1$
				"import p2.Y;\n"+ //$NON-NLS-1$
				"public class X {\n"+ //$NON-NLS-1$
				"  public void bar(Y y, int i){\n"+ //$NON-NLS-1$
				"    y.zork();\n"+ //$NON-NLS-1$
				"  }\n"+ //$NON-NLS-1$
				"}\n" //$NON-NLS-1$
				);
			incrementalBuild();
			
			expectingCompilingOrder(new String[]{"p1.X", "p2.Y", "p3.Z"}); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ 
			expectingOnlySpecificProblemFor(p1,new Problem("p1", "A cycle was detected in the build path of project: P1", p1, -1, -1, -1));//$NON-NLS-1$ //$NON-NLS-2$
			expectingOnlySpecificProblemsFor(p2,new Problem[]{
					new Problem("p2", "The method bar(Y, int) in the type X is not applicable for the arguments (Y)", c2, 106, 109, CategorizedProblem.CAT_MEMBER),//$NON-NLS-1$ //$NON-NLS-2$
					new Problem("p2", "A cycle was detected in the build path of project: P2", p2, -1, -1, -1)//$NON-NLS-1$ //$NON-NLS-2$
			});
			expectingOnlySpecificProblemFor(p3,new Problem("p3", "A cycle was detected in the build path of project: P3", p3, -1, -1, -1));//$NON-NLS-1$ //$NON-NLS-2$
	
			JavaCore.setOptions(options);
		} finally {
			env.setBuildOrder(null);
		}
	}
	public void testCycle4() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.CORE_CIRCULAR_CLASSPATH, JavaCore.WARNING); //$NON-NLS-1$
		
		JavaCore.setOptions(newOptions);
		
		//----------------------------
		//         Project1
		//----------------------------
		IPath p1 = env.addProject("P1"); //$NON-NLS-1$
		env.addExternalJars(p1, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p1, ""); //$NON-NLS-1$
		IPath root1 = env.addPackageFragmentRoot(p1, "src"); //$NON-NLS-1$
		env.setOutputFolder(p1, "bin"); //$NON-NLS-1$
		
		//----------------------------
		//         Project2
		//----------------------------
		IPath p2 = env.addProject("P2"); //$NON-NLS-1$
		env.addExternalJars(p2, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p2, ""); //$NON-NLS-1$
		IPath root2 = env.addPackageFragmentRoot(p2, "src"); //$NON-NLS-1$
		env.setOutputFolder(p2, "bin"); //$NON-NLS-1$
		
		IPath c2 = env.addClass(root2, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.X;\n"+ //$NON-NLS-1$
			"import p3.Z;\n"+ //$NON-NLS-1$
			"public class Y extends Z{\n"+ //$NON-NLS-1$
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
		env.addExternalJars(p3, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p3, ""); //$NON-NLS-1$
		IPath root3 = env.addPackageFragmentRoot(p3, "src"); //$NON-NLS-1$
		env.setOutputFolder(p3, "bin"); //$NON-NLS-1$
		
		IPath c3 = env.addClass(root3, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
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

		try {
			env.setBuildOrder(new String[]{"P1", "P2", "P3"});//$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ 
			fullBuild();
			
			expectingCompilingOrder(new String[]{"p2.Y", "p3.Z", "p2.Y"});//$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
			expectingOnlySpecificProblemFor(p1,new Problem("p1", "A cycle was detected in the build path of project: P1", p1, -1, -1, -1));//$NON-NLS-1$ //$NON-NLS-2$
			expectingOnlySpecificProblemsFor(p2,new Problem[]{
				new Problem("p2", "The import p1 cannot be resolved", c2, 19, 21, CategorizedProblem.CAT_IMPORT),//$NON-NLS-1$ //$NON-NLS-2$
				new Problem("p2", "X cannot be resolved to a type", c2, 73, 74, CategorizedProblem.CAT_TYPE),//$NON-NLS-1$ //$NON-NLS-2$
				new Problem("p2", "X cannot be resolved to a type", c2, 87, 88, CategorizedProblem.CAT_TYPE),//$NON-NLS-1$ //$NON-NLS-2$
				new Problem("p2", "The method foo() is undefined for the type Y", c2, 93, 96, CategorizedProblem.CAT_MEMBER),//$NON-NLS-1$ //$NON-NLS-2$
				new Problem("p2", "A cycle was detected in the build path of project: P2", p2, -1, -1, -1)//$NON-NLS-1$ //$NON-NLS-2$
			});
			expectingOnlySpecificProblemsFor(p3,new Problem[]{
				new Problem("p3", "X cannot be resolved to a type", c3, 51, 52, CategorizedProblem.CAT_TYPE),//$NON-NLS-1$ //$NON-NLS-2$
				new Problem("p3", "The import p1 cannot be resolved", c3, 19, 21, CategorizedProblem.CAT_IMPORT),//$NON-NLS-1$ //$NON-NLS-2$
				new Problem("p3", "A cycle was detected in the build path of project: P3", p3, -1, -1, -1)//$NON-NLS-1$ //$NON-NLS-2$
			});
	
			env.addClass(root1, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
				"package p1;\n"+ //$NON-NLS-1$
				"import p2.Y;\n"+ //$NON-NLS-1$
				"public class X {\n"+ //$NON-NLS-1$
				"  public void bar(Y y){\n"+ //$NON-NLS-1$
				"    y.zork();\n"+ //$NON-NLS-1$
				"  }\n"+ //$NON-NLS-1$
				"}\n" //$NON-NLS-1$
				);
			incrementalBuild();
			expectingCompilingOrder(new String[]{"p1.X", "p2.Y", "p3.Z", "p1.X", "p2.Y"}); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$//$NON-NLS-5$ 
			expectingOnlySpecificProblemFor(p1,new Problem("p1", "A cycle was detected in the build path of project: P1", p1, -1, -1, -1));//$NON-NLS-1$ //$NON-NLS-2$
			expectingOnlySpecificProblemFor(p2,new Problem("p2", "A cycle was detected in the build path of project: P2", p2, -1, -1, -1));//$NON-NLS-1$ //$NON-NLS-2$
			expectingOnlySpecificProblemFor(p3,new Problem("p3", "A cycle was detected in the build path of project: P3", p3, -1, -1, -1));//$NON-NLS-1$ //$NON-NLS-2$
	
			JavaCore.setOptions(options);
		} finally {
			env.setBuildOrder(null);
		}
	}
	
	public void testCycle5() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.CORE_CIRCULAR_CLASSPATH, JavaCore.WARNING); //$NON-NLS-1$
		
		JavaCore.setOptions(newOptions);
		
		//----------------------------
		//         Project1
		//----------------------------
		IPath p1 = env.addProject("P1"); //$NON-NLS-1$
		env.addExternalJars(p1, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p1, ""); //$NON-NLS-1$
		IPath root1 = env.addPackageFragmentRoot(p1, "src"); //$NON-NLS-1$
		env.setOutputFolder(p1, "bin"); //$NON-NLS-1$
		
		IPath c1 = env.addClass(root1, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"import p2.*;\n"+ //$NON-NLS-1$
			"import p22.*;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"  Y y;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		//----------------------------
		//         Project2
		//----------------------------
		IPath p2 = env.addProject("P2"); //$NON-NLS-1$
		env.addExternalJars(p2, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p2, ""); //$NON-NLS-1$
		IPath root2 = env.addPackageFragmentRoot(p2, "src"); //$NON-NLS-1$
		env.setOutputFolder(p2, "bin"); //$NON-NLS-1$
		
		IPath c2 = env.addClass(root2, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"import p11.*;\n"+ //$NON-NLS-1$
			"public class Y {\n"+ //$NON-NLS-1$
			"  X x;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);


		// for Project1
		env.addRequiredProject(p1, p2);
		// for Project2
		env.addRequiredProject(p2, p1);

		try {
			env.setBuildOrder(new String[]{"P1", "P2"});//$NON-NLS-1$ //$NON-NLS-2$
			fullBuild();
			
			expectingCompilingOrder(new String[]{"p1.X", "p2.Y", "p1.X", "p2.Y"});//$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ 
			expectingOnlySpecificProblemsFor(p1,new Problem[]{
				new Problem("p1", "The import p22 cannot be resolved", c1, 32, 35, CategorizedProblem.CAT_IMPORT),//$NON-NLS-1$ //$NON-NLS-2$
				new Problem("p1", "A cycle was detected in the build path of project: P1", p1, -1, -1, -1)//$NON-NLS-1$ //$NON-NLS-2$
			});
			expectingOnlySpecificProblemsFor(p2,new Problem[]{
				new Problem("p2", "The import p11 cannot be resolved", c2, 32, 35, CategorizedProblem.CAT_IMPORT),//$NON-NLS-1$ //$NON-NLS-2$
				new Problem("p2", "A cycle was detected in the build path of project: P2", p2, -1, -1, -1)//$NON-NLS-1$ //$NON-NLS-2$
			});
			
			env.addClass(root1, "p11", "XX", //$NON-NLS-1$ //$NON-NLS-2$
				"package p11;\n"+ //$NON-NLS-1$
				"public class XX {\n"+ //$NON-NLS-1$
				"}\n" //$NON-NLS-1$
				);
			env.addClass(root2, "p22", "YY", //$NON-NLS-1$ //$NON-NLS-2$
				"package p22;\n"+ //$NON-NLS-1$
				"public class YY {\n"+ //$NON-NLS-1$
				"}\n" //$NON-NLS-1$
				);
				
			incrementalBuild();
			
			expectingCompilingOrder(new String[]{"p11.XX", "p22.YY", "p2.Y", "p1.X"});//$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
			expectingOnlySpecificProblemsFor(p1,new Problem[]{
				new Problem("p1", "The import p22 is never used", c1, 32, 35, CategorizedProblem.CAT_UNNECESSARY_CODE),//$NON-NLS-1$ //$NON-NLS-2$
				new Problem("p1", "A cycle was detected in the build path of project: P1", p1, -1, -1, -1)//$NON-NLS-1$ //$NON-NLS-2$
			});
			expectingOnlySpecificProblemsFor(p2,new Problem[]{
				new Problem("p2", "The import p11 is never used", c2, 32, 35, CategorizedProblem.CAT_UNNECESSARY_CODE),//$NON-NLS-1$ //$NON-NLS-2$
				new Problem("p2", "A cycle was detected in the build path of project: P2", p2, -1, -1, -1)//$NON-NLS-1$ //$NON-NLS-2$
			});
			
			JavaCore.setOptions(options);
		} finally {
			env.setBuildOrder(null);
		}
	}
	
	/*
	 * Full buid case
	 */
	public void testExcludePartOfAnotherProject1() throws JavaModelException {
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, ""); //$NON-NLS-1$
		env.addClass(root1, "p.api", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.api;\n" + //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.addClass(root1, "p.internal", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.internal;\n" + //$NON-NLS-1$
			"public class B {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProject(project2Path, project1Path, new IPath[] {}, new IPath[] {new Path("**/internal/")}, false);
		IPath root2 = env.getPackageFragmentRootPath(project2Path, ""); //$NON-NLS-1$
		env.addClass(root2, "", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"public class C extends p.api.A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		IPath d = env.addClass(root2, "", "D", //$NON-NLS-1$ //$NON-NLS-2$
			"public class D extends p.internal.B {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
		fullBuild();
		expectingSpecificProblemFor(project2Path, new Problem("", "Access restriction: The type B is not accessible due to restriction on required project Project1", d, 23, 35, CategorizedProblem.CAT_RESTRICTION)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/*
	 * Incremental buid case
	 */
	public void testExcludePartOfAnotherProject2() throws JavaModelException {
		//----------------------------
		//           Step 1
		//----------------------------
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, ""); //$NON-NLS-1$
		env.addClass(root1, "p.api", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.api;\n" + //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.addClass(root1, "p.internal", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.internal;\n" + //$NON-NLS-1$
			"public class B {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProject(project2Path, project1Path, new IPath[] {}, new IPath[] {new Path("**/internal/")}, false);
		IPath root2 = env.getPackageFragmentRootPath(project2Path, ""); //$NON-NLS-1$
		env.addClass(root2, "", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"public class C extends p.api.A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
		fullBuild();
		expectingNoProblems();
		
		//----------------------------
		//           Step 2
		//----------------------------
		IPath d = env.addClass(root2, "", "D", //$NON-NLS-1$ //$NON-NLS-2$
			"public class D extends p.internal.B {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		incrementalBuild();
		expectingSpecificProblemFor(project2Path, new Problem("", "Access restriction: The type B is not accessible due to restriction on required project Project1", d, 23, 35, CategorizedProblem.CAT_RESTRICTION)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/*
	 * Fix access restriction problem
	 */
	public void testExcludePartOfAnotherProject3() throws JavaModelException {
		//----------------------------
		//           Step 1
		//----------------------------
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, ""); //$NON-NLS-1$
		env.addClass(root1, "p.api", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.api;\n" + //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.addClass(root1, "p.internal", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.internal;\n" + //$NON-NLS-1$
			"public class B {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProject(project2Path, project1Path, new IPath[] {}, new IPath[] {new Path("**/internal/")}, false);
		IPath root2 = env.getPackageFragmentRootPath(project2Path, ""); //$NON-NLS-1$
		env.addClass(root2, "", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"public class C extends p.api.A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		IPath d = env.addClass(root2, "", "D", //$NON-NLS-1$ //$NON-NLS-2$
			"public class D extends p.internal.B {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
		fullBuild();
		expectingSpecificProblemFor(project2Path, new Problem("", "Access restriction: The type B is not accessible due to restriction on required project Project1", d, 23, 35, CategorizedProblem.CAT_RESTRICTION)); //$NON-NLS-1$ //$NON-NLS-2$
		
		//----------------------------
		//           Step 2
		//----------------------------
		env.removeRequiredProject(project2Path, project1Path);
		env.addRequiredProject(project2Path, project1Path, new IPath[] {}, new IPath[] {}, false);
		
		incrementalBuild();
		expectingNoProblems();
	}
		
	/*
	 * Full buid case
	 */
	public void testIncludePartOfAnotherProject1() throws JavaModelException {
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, ""); //$NON-NLS-1$
		env.addClass(root1, "p.api", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.api;\n" + //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.addClass(root1, "p.internal", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.internal;\n" + //$NON-NLS-1$
			"public class B {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProject(project2Path, project1Path, new IPath[] {new Path("**/api/")}, new IPath[] {new Path("**")}, false);
		IPath root2 = env.getPackageFragmentRootPath(project2Path, ""); //$NON-NLS-1$
		env.addClass(root2, "", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"public class C extends p.api.A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		IPath d = env.addClass(root2, "", "D", //$NON-NLS-1$ //$NON-NLS-2$
			"public class D extends p.internal.B {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
		fullBuild();
		expectingSpecificProblemFor(project2Path, new Problem("", "Access restriction: The type B is not accessible due to restriction on required project Project1", d, 23, 35, CategorizedProblem.CAT_RESTRICTION)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/*
	 * Incremental buid case
	 */
	public void testIncludePartOfAnotherProject2() throws JavaModelException {
		//----------------------------
		//           Step 1
		//----------------------------
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, ""); //$NON-NLS-1$
		env.addClass(root1, "p.api", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.api;\n" + //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.addClass(root1, "p.internal", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.internal;\n" + //$NON-NLS-1$
			"public class B {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProject(project2Path, project1Path, new IPath[] {new Path("**/api/")}, new IPath[] {new Path("**")}, false);
		IPath root2 = env.getPackageFragmentRootPath(project2Path, ""); //$NON-NLS-1$
		env.addClass(root2, "", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"public class C extends p.api.A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
		fullBuild();
		expectingNoProblems();
		
		//----------------------------
		//           Step 2
		//----------------------------
		IPath d = env.addClass(root2, "", "D", //$NON-NLS-1$ //$NON-NLS-2$
			"public class D extends p.internal.B {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		incrementalBuild();
		expectingSpecificProblemFor(project2Path, new Problem("", "Access restriction: The type B is not accessible due to restriction on required project Project1", d, 23, 35, CategorizedProblem.CAT_RESTRICTION)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/*
	 * Fix access restriction problem
	 */
	public void testIncludePartOfAnotherProject3() throws JavaModelException {
		//----------------------------
		//           Step 1
		//----------------------------
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, ""); //$NON-NLS-1$
		env.addClass(root1, "p.api", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.api;\n" + //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		env.addClass(root1, "p.internal", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p.internal;\n" + //$NON-NLS-1$
			"public class B {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProject(project2Path, project1Path, new IPath[] {new Path("**/api/")}, new IPath[] {new Path("**")}, false);
		IPath root2 = env.getPackageFragmentRootPath(project2Path, ""); //$NON-NLS-1$
		env.addClass(root2, "", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"public class C extends p.api.A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		IPath d = env.addClass(root2, "", "D", //$NON-NLS-1$ //$NON-NLS-2$
			"public class D extends p.internal.B {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
		fullBuild();
		expectingSpecificProblemFor(project2Path, new Problem("", "Access restriction: The type B is not accessible due to restriction on required project Project1", d, 23, 35, CategorizedProblem.CAT_RESTRICTION)); //$NON-NLS-1$ //$NON-NLS-2$
		
		//----------------------------
		//           Step 2
		//----------------------------
		env.removeRequiredProject(project2Path, project1Path);
		env.addRequiredProject(project2Path, project1Path, new IPath[] {}, new IPath[] {}, false);
		
		incrementalBuild();
		expectingNoProblems();
	}
	
	/*
	 * Ensures that a type matching a ignore-if-better non-accessible rule is further found when accessible
	 * on another classpath entry.
	 * (regression test for bug 98127 Access restrictions started showing up after switching to bundle)
	 */
	public void testIgnoreIfBetterNonAccessibleRule1() throws JavaModelException {
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, ""); //$NON-NLS-1$
		env.addClass(root1, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n" + //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		IPath root2 = env.getPackageFragmentRootPath(project2Path, ""); //$NON-NLS-1$
		env.addClass(root2, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n" + //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
			//----------------------------
			//         Project3
			//----------------------------
		IPath project3Path = env.addProject("Project3"); //$NON-NLS-1$
		env.addExternalJars(project3Path, Util.getJavaClassLibs());
		env.addRequiredProject(project3Path, project1Path, new Path("**/p/"), IAccessRule.K_NON_ACCESSIBLE | IAccessRule.IGNORE_IF_BETTER);
		env.addRequiredProject(project3Path, project2Path, new Path("**/p/A"), IAccessRule.K_ACCESSIBLE);
		IPath root3 = env.getPackageFragmentRootPath(project3Path, ""); //$NON-NLS-1$
		env.addClass(root3, "p3", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n" + //$NON-NLS-1$
			"public class B extends p.A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();
	}
	
	/*
	 * Ensures that a type matching a ignore-if-better non-accessible rule is further found when accessible
	 * on another classpath entry.
	 * (regression test for bug 98127 Access restrictions started showing up after switching to bundle)
	 */
	public void testIgnoreIfBetterNonAccessibleRule2() throws JavaModelException {
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		IPath root1 = env.getPackageFragmentRootPath(project1Path, ""); //$NON-NLS-1$
		env.addClass(root1, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n" + //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		IPath root2 = env.getPackageFragmentRootPath(project2Path, ""); //$NON-NLS-1$
		env.addClass(root2, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n" + //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
			//----------------------------
			//         Project3
			//----------------------------
		IPath project3Path = env.addProject("Project3"); //$NON-NLS-1$
		env.addExternalJars(project3Path, Util.getJavaClassLibs());
		env.addRequiredProject(project3Path, project1Path, new Path("**/p/"), IAccessRule.K_NON_ACCESSIBLE | IAccessRule.IGNORE_IF_BETTER);
		env.addRequiredProject(project3Path, project2Path, new Path("**/p/A"), IAccessRule.K_DISCOURAGED);
		IPath root3 = env.getPackageFragmentRootPath(project3Path, ""); //$NON-NLS-1$
		IPath b = env.addClass(root3, "p3", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n" + //$NON-NLS-1$
			"public class B extends p.A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		fullBuild();
		expectingSpecificProblemFor(project3Path, new Problem("", "Discouraged access: The type A is not accessible due to restriction on required project Project2", b, 35, 38, CategorizedProblem.CAT_RESTRICTION)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void testMissingRequiredBinaries() throws JavaModelException {
		
		IPath p1 = env.addProject("P1"); //$NON-NLS-1$
		IPath p2 = env.addProject("P2"); //$NON-NLS-1$
		IPath p3 = env.addProject("P3"); //$NON-NLS-1$

		env.addExternalJars(p1, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p1, ""); //$NON-NLS-1$
		IPath root1 = env.addPackageFragmentRoot(p1, "src"); //$NON-NLS-1$
		env.addRequiredProject(p1, p2);
		env.setOutputFolder(p1, "bin"); //$NON-NLS-1$

		env.addExternalJars(p2, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p2, ""); //$NON-NLS-1$
		IPath root2 = env.addPackageFragmentRoot(p2, "src"); //$NON-NLS-1$
		env.addRequiredProject(p2, p3);
		env.setOutputFolder(p2, "bin"); //$NON-NLS-1$
		
		env.addExternalJars(p3, Util.getJavaClassLibs());
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(p3, ""); //$NON-NLS-1$
		IPath root3 = env.addPackageFragmentRoot(p3, "src"); //$NON-NLS-1$
		env.setOutputFolder(p3, "bin"); //$NON-NLS-1$
		
		IPath x = env.addClass(root1, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"import p2.*;\n"+ //$NON-NLS-1$
			"public class X extends Y{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		env.addClass(root2, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p3.*;\n"+ //$NON-NLS-1$
			"public class Y extends Z {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		env.addClass(root3, "p3", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class Z {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		try {
			fullBuild();
			
			expectingOnlySpecificProblemsFor(p1,new Problem[]{
				new Problem("p1", "The type p3.Z cannot be resolved. It is indirectly referenced from required .class files", x, 48, 49, CategorizedProblem.CAT_BUILDPATH),//$NON-NLS-1$ //$NON-NLS-2$
				new Problem("p1", "The project was not built since its build path is incomplete. Cannot find the class file for p3.Z. Fix the build path then try building this project", p1, -1, -1, -1)//$NON-NLS-1$ //$NON-NLS-2$
			});
		} finally {
			env.setBuildOrder(null);
		}
	}
	
}
