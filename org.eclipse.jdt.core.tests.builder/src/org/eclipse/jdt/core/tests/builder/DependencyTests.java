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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;

public class DependencyTests extends Tests {
	public DependencyTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(DependencyTests.class);
	}

	public void testAbstractMethod() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$
		
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		
		env.addClass(root, "p1", "Indicted", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public abstract class Indicted {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		IPath collaboratorPath =  env.addClass(root, "p2", "Collaborator", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class Collaborator extends Indicted{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
		
		fullBuild(projectPath);
		expectingNoProblems();
		
		env.addClass(root, "p1", "Indicted", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public abstract class Indicted {\n"+ //$NON-NLS-1$
			"   public abstract void foo();\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(collaboratorPath);
		expectingOnlySpecificProblemFor(collaboratorPath, new Problem("Collaborator", "The type Collaborator must implement the inherited abstract method Indicted.foo()", collaboratorPath)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testExactMethodDeleting() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	
		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public int i(int i) {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath cPath =  env.addClass(root, "p3", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class C extends p2.B{\n"+ //$NON-NLS-1$
			"	int j = i(1);\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath dPath =  env.addClass(root, "p3", "D", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class D extends p2.B{\n"+ //$NON-NLS-1$
			"	public class M {\n"+ //$NON-NLS-1$
			"		int j = i(1);\n"+ //$NON-NLS-1$
			"	}\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath xPath =  env.addClass(root, "p4", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p4;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	int foo(p3.C c) { return c.i(1); }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, dPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "The method i(int) is undefined for the type C", cPath)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(dPath, new Problem("D", "The method i(int) is undefined for the type D.M", dPath)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(xPath, new Problem("X", "The method i(int) is undefined for the type C", xPath)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"	protected int i(long l) throws Exception {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, dPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "Default constructor cannot handle exception type Exception thrown by implicit super constructor. Must define an explicit constructor", cPath)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(dPath, new Problem("D", "Default constructor cannot handle exception type Exception thrown by implicit super constructor. Must define an explicit constructor", dPath)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(xPath, new Problem("X", "The method i(long) from the type B is not visible", xPath)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public int i(int i) {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testExactMethodVisibility() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	
		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public int i() {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath cPath =  env.addClass(root, "p3", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class C extends p2.B{\n"+ //$NON-NLS-1$
			"	int j = i();\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath dPath =  env.addClass(root, "p3", "D", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class D extends p2.B{\n"+ //$NON-NLS-1$
			"	public class M {\n"+ //$NON-NLS-1$
			"		int j = i();\n"+ //$NON-NLS-1$
			"	}\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath xPath =  env.addClass(root, "p4", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p4;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	int foo(p3.C c) { return c.i(); }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	int i() {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, dPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "The method i() from the type A is not visible", cPath)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(dPath, new Problem("D", "The method i() from the type A is not visible", dPath)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(xPath, new Problem("X", "The method i() from the type A is not visible", xPath)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	protected int i() {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {xPath});
		expectingSpecificProblemFor(xPath, new Problem("X", "The method i() from the type A is not visible", xPath)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public int i() {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testExternalJarChanged() throws CoreException, java.io.IOException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		IPath root = env.getPackageFragmentRootPath(projectPath, ""); //$NON-NLS-1$
		IPath classTest = env.addClass(root, "p", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n"+ //$NON-NLS-1$
			"public class X {\n" + //$NON-NLS-1$
			"  void foo() {\n" + //$NON-NLS-1$
			"    new q.Y().bar();\n" + //$NON-NLS-1$
			"  }\n" + //$NON-NLS-1$
			"}" //$NON-NLS-1$
		);
		String externalJar = Util.getOutputDirectory() + java.io.File.separator + "test.jar"; //$NON-NLS-1$
		Util.createJar(
			new String[] {
				"q/Y.java", //$NON-NLS-1$
				"package q;\n" + //$NON-NLS-1$
				"public class Y {\n" + //$NON-NLS-1$
				"}" //$NON-NLS-1$
			},
			new java.util.HashMap(),
			externalJar
		);
		long lastModified = new java.io.File(externalJar).lastModified();
		env.addExternalJar(projectPath, externalJar);

		// build -> expecting problems
		fullBuild();
		expectingProblemsFor(classTest);

		try {
			Thread.sleep(1000);
		} catch(InterruptedException e) {
		}
		// fix jar
		Util.createJar(
			new String[] {
				"q/Y.java", //$NON-NLS-1$
				"package q;\n" + //$NON-NLS-1$
				"public class Y {\n" + //$NON-NLS-1$
				"  public void bar() {\n" + //$NON-NLS-1$
				"  }\n" + //$NON-NLS-1$
				"}" //$NON-NLS-1$
			},
			new java.util.HashMap(),
			externalJar
		);
		new java.io.File(externalJar).setLastModified(lastModified + 1000); // to be sure its different
		// add new class to trigger an incremental build
		env.getProject(projectPath).touch(null);

		// incremental build should notice jar file has changed & do a full build
		incrementalBuild();
		expectingNoProblems();
	}

	public void testFieldDeleting() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	
		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public int i;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath cPath =  env.addClass(root, "p3", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class C extends p2.B{\n"+ //$NON-NLS-1$
			"	int j = i;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath xPath =  env.addClass(root, "p4", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p4;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	int foo(p3.C c) { return c.i; }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "i cannot be resolved", cPath)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(xPath, new Problem("X", "c.i cannot be resolved or is not a field", xPath)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public int i;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testFieldVisibility() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	
		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public int i;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath cPath =  env.addClass(root, "p3", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class C extends p2.B{\n"+ //$NON-NLS-1$
			"	int j = i;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath xPath =  env.addClass(root, "p4", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p4;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	int foo(p3.C c) { return c.i; }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	int i;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "The field A.i is not visible", cPath)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(xPath, new Problem("X", "The field A.i is not visible", xPath)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	protected int i;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {xPath});
		expectingSpecificProblemFor(xPath, new Problem("X", "The field A.i is not visible", xPath)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public int i;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	// 77272
	public void testInterfaceDeleting() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "Vehicle", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public interface Vehicle {}\n" //$NON-NLS-1$
		);

		env.addClass(root, "p1", "Car", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public interface Car extends Vehicle {}\n" //$NON-NLS-1$
		);

		env.addClass(root, "p1", "CarImpl", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class CarImpl implements Car {}\n" //$NON-NLS-1$
		);

		IPath testPath = env.addClass(root, "p1", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class Test { public Vehicle createVehicle() { return new CarImpl(); } }\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "Car", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public interface Car {}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(testPath);
		expectingSpecificProblemFor(testPath, new Problem("Test", "Type mismatch: cannot convert from CarImpl to Vehicle", testPath)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "Car", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public interface Car extends Vehicle {}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMemberTypeDeleting() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	
		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public class M { public int i; };\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath cPath =  env.addClass(root, "p3", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class C extends p2.B{\n"+ //$NON-NLS-1$
			"	M m;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath xPath =  env.addClass(root, "p4", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p4;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	int foo(p3.C.M m) { return m.i; }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "M cannot be resolved to a type", cPath)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(xPath, new Problem("X", "p3.C.M cannot be resolved to a type", xPath)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public class M { public int i; };\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMemberTypeVisibility() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	
		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public class M { public int i; };\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath cPath =  env.addClass(root, "p3", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class C extends p2.B{\n"+ //$NON-NLS-1$
			"	M m;\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath xPath =  env.addClass(root, "p4", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p4;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	int foo(p3.C.M m) { return m.i; }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	class M { public int i; };\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "The type M is not visible", cPath)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(xPath, new Problem("X", "The type p3.C.M is not visible", xPath)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	protected class M { public int i; };\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {xPath});
		expectingSpecificProblemFor(xPath, new Problem("X", "The type p3.C.M is not visible", xPath)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public class M { public int i; };\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMethodDeleting() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	
		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public int i(A a) {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath cPath =  env.addClass(root, "p3", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class C extends p2.B{\n"+ //$NON-NLS-1$
			"	int j = i(this);\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath dPath =  env.addClass(root, "p3", "D", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class D extends p2.B{\n"+ //$NON-NLS-1$
			"	public class M {\n"+ //$NON-NLS-1$
			"		int j = i(new D());\n"+ //$NON-NLS-1$
			"	}\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath xPath =  env.addClass(root, "p4", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p4;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	int foo(p3.C c) { return c.i(c); }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, dPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "The method i(C) is undefined for the type C", cPath)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(dPath, new Problem("D", "The method i(D) is undefined for the type D.M", dPath)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(xPath, new Problem("X", "The method i(C) is undefined for the type C", xPath)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"	public int i(B b) {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public int i(A a) {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMethodVisibility() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	
		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public int i(A a) {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath cPath =  env.addClass(root, "p3", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class C extends p2.B{\n"+ //$NON-NLS-1$
			"	int j = i(this);\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath dPath =  env.addClass(root, "p3", "D", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class D extends p2.B{\n"+ //$NON-NLS-1$
			"	public class M {\n"+ //$NON-NLS-1$
			"		int j = i(new D());\n"+ //$NON-NLS-1$
			"	}\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		IPath xPath =  env.addClass(root, "p4", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p4;\n"+ //$NON-NLS-1$
			"public class X {\n"+ //$NON-NLS-1$
			"	int foo(p3.C c) { return c.i(c); }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	int i(A a) {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {cPath, dPath, xPath});
		expectingSpecificProblemFor(cPath, new Problem("C", "The method i(A) from the type A is not visible", cPath)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(dPath, new Problem("D", "The method i(A) from the type A is not visible", dPath)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(xPath, new Problem("X", "The method i(A) from the type A is not visible", xPath)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"	protected int i(B b) {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {xPath});
		expectingSpecificProblemFor(xPath, new Problem("X", "The method i(B) from the type B is not visible", xPath)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n"+ //$NON-NLS-1$
			"	public int i(A a) {return 1;};\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"import p1.*;\n"+ //$NON-NLS-1$
			"public class B extends A{\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	// 72468
	public void testTypeDeleting() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	
		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {}\n" //$NON-NLS-1$
		);

		IPath bPath = env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class B extends p1.A{}\n" //$NON-NLS-1$
		);

		IPath cPath = env.addClass(root, "p3", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class C extends p2.B{}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"class Deleted {}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {bPath, cPath});
		expectingSpecificProblemFor(bPath, new Problem("B", "p1.A cannot be resolved to a type", bPath)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(cPath, new Problem("C", "The hierarchy of the type C is inconsistent", cPath)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class B {}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	// 72468
	public void testTypeVisibility() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	
		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {}\n" //$NON-NLS-1$
		);

		IPath bPath = env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class B extends p1.A{}\n" //$NON-NLS-1$
		);

		IPath cPath = env.addClass(root, "p3", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n"+ //$NON-NLS-1$
			"public class C extends p2.B{}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"class A {}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {bPath, cPath});
		expectingSpecificProblemFor(bPath, new Problem("B", "The type p1.A is not visible", bPath)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(cPath, new Problem("C", "The hierarchy of the type C is inconsistent", cPath)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class B {}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class B extends p1.A{}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {bPath, cPath});
		expectingSpecificProblemFor(bPath, new Problem("B", "The type p1.A is not visible", bPath)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(cPath, new Problem("C", "The hierarchy of the type C is inconsistent", cPath)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	// 79163
	public void testTypeVisibility2() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath aPath = env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {\n" +  //$NON-NLS-1$
			"	void foo() { p2.FooFactory.createFoo().foo(); }\n" +  //$NON-NLS-1$
			"	void foos() { p2.FooFactory.createFoos().clone(); }\n" +  //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		// Foo & Foos are not public to get visibility problems
		env.addClass(root, "p2", "Foo", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"class Foo { public void foo() {} }\n" //$NON-NLS-1$
		);
		env.addClass(root, "p2", "Foos", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"class Foos {}\n" //$NON-NLS-1$
		);

		env.addClass(root, "p2", "FooFactory", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class FooFactory {\n" +  //$NON-NLS-1$
			"	public static Foo createFoo() { return null; }\n" +  //$NON-NLS-1$
			"	public static Foos[] createFoos() { return null; }\n" +  //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {aPath});
		expectingSpecificProblemFor(aPath, new Problem("A", "The type Foo is not visible", aPath)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(aPath, new Problem("A", "The type Foos is not visible", aPath)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p2", "Foo", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class Foo { public void foo() {} }\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {aPath});
		expectingSpecificProblemFor(aPath, new Problem("A", "The type Foos is not visible", aPath)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p2", "Foos", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class Foos { }\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p2", "Foo", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"class Foo { public void foo() {} }\n" //$NON-NLS-1$
		);
		env.addClass(root, "p2", "Foos", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"class Foos {}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {aPath});
		expectingSpecificProblemFor(aPath, new Problem("A", "The type Foo is not visible", aPath)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(aPath, new Problem("A", "The type Foos is not visible", aPath)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testTypeVariable() throws JavaModelException {
		if ((AbstractCompilerTest.getPossibleComplianceLevels() & AbstractCompilerTest.F_1_5) == 0) return;

		IPath projectPath = env.addProject("Project", "1.5"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
	
		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A<T> {}\n" //$NON-NLS-1$
		);

		IPath bPath = env.addClass(root, "p2", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class B<T> extends p1.A<T> {}\n" //$NON-NLS-1$
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A {}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {bPath});
		expectingSpecificProblemFor(bPath, new Problem("B", "The type A is not generic; it cannot be parameterized with arguments <T>", bPath)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A<T extends Comparable> {}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingOnlyProblemsFor(new IPath[] {bPath});
		expectingSpecificProblemFor(bPath, new Problem("B", "Bound mismatch: The type T is not a valid substitute for the bounded parameter <T extends Comparable> of the type A<T>", bPath)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class A<T> {}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}
}
