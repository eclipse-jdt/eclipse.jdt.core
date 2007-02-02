/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.builder.JavaBuilder;


/**
 * Basic errors tests of the image builder.
 */
public class ErrorsTests extends BuilderTests {
	public ErrorsTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		return buildTestSuite(ErrorsTests.class);
	}
	
	public void testErrors() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);
		
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
		expectingOnlySpecificProblemFor(collaboratorPath, new Problem("Collaborator", "The type Collaborator must implement the inherited abstract method Indicted.foo()", collaboratorPath, 38, 50, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/*
	 * Regression test for bug 2857 Renaming .java class with errors to .txt leaves errors in Task list (1GK06R3)
	 */
	public void testRenameToNonJava() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath,""); //$NON-NLS-1$
		
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		
		IPath cuPath = env.addClass(root, "p1", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class X extends Y {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);
			
		fullBuild(projectPath);
		expectingOnlyProblemsFor(cuPath);
		expectingOnlySpecificProblemFor(cuPath, new Problem("X", "Y cannot be resolved to a type", cuPath, 35, 36, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		
		
		env.renameCU(root.append("p1"), "X.java", "X.txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		incrementalBuild(projectPath);
		expectingNoProblems();
	}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=158611
// Checking the GENERATED_BY attribute
public void test0100() throws JavaModelException {
	IPath projectPath = env.addProject("Project");
	env.addExternalJars(projectPath, Util.getJavaClassLibs());	
	env.removePackageFragmentRoot(projectPath, "");
	IPath root = env.addPackageFragmentRoot(projectPath, "src");
	IPath classTest1 = env.addClass(root, "p1", "Test1",
		"package p1;\n" +
		"public class Test1 extends Test2 {}"
	);
	fullBuild();
	Problem[] prob1 = env.getProblemsFor(classTest1);
	expectingSpecificProblemFor(classTest1, new Problem("p1", "Test2 cannot be resolved to a type", classTest1, 39, 44, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));
	assertEquals(JavaBuilder.SOURCE_ID, prob1[0].getSourceId());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=158611
// Checking the GENERATED_BY attribute
public void test0101() throws JavaModelException {
	IPath projectPath = env.addProject("Project");
	env.addExternalJars(projectPath, Util.getJavaClassLibs());	
	env.removePackageFragmentRoot(projectPath, "");
	IPath root = env.addPackageFragmentRoot(projectPath, "src");
	IPath classTest1 = env.addClass(root, "p1", "Test1",
		"package p1;\n" +
		"public class Test1 extends {}"
	);
	fullBuild();
	Problem[] prob1 = env.getProblemsFor(classTest1);
	expectingSpecificProblemFor(classTest1, new Problem("p1", "Syntax error on token \"extends\", Type expected after this token", classTest1, 31, 38, CategorizedProblem.CAT_SYNTAX, IMarker.SEVERITY_ERROR));
	assertEquals(JavaBuilder.SOURCE_ID, prob1[0].getSourceId());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=158611
// Checking the GENERATED_BY attribute
public void test0102() throws JavaModelException {
	IPath projectPath = env.addProject("Project");
	env.addExternalJars(projectPath, Util.getJavaClassLibs());	
	env.removePackageFragmentRoot(projectPath, "");
	IPath root = env.addPackageFragmentRoot(projectPath, "src");
	IPath classTest1 = env.addClass(root, "p1", "Test1",
		"package p1;\n" +
		"public class Test1 {\n" +
		"  private static int i;\n" +
		"  int j = i;\n" +
		"}\n" +
		"class Test2 {\n" +
		"  static int i = Test1.i;\n" +
		"}\n"
	);
	fullBuild();
	Problem[] prob1 = env.getProblemsFor(classTest1);
	expectingSpecificProblemFor(classTest1, new Problem("p1", "The field Test1.i is not visible", classTest1, 109, 110, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR));
	assertEquals(JavaBuilder.SOURCE_ID, prob1[0].getSourceId());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=158611
// Checking the GENERATED_BY attribute
public void test0103() throws JavaModelException {
	IPath projectPath = env.addProject("Project");
	env.addExternalJars(projectPath, Util.getJavaClassLibs());	
	env.removePackageFragmentRoot(projectPath, "");
	IPath root = env.addPackageFragmentRoot(projectPath, "src");
	IPath classTest1 = env.addClass(root, "p1", "Test1",
		"package p1;\n" +
		"public class Test1 {\n" +
		"  // TODO: marker only\n" +
		"}\n"
	);
	fullBuild();
	Problem[] prob1 = env.getProblemsFor(classTest1);
	expectingSpecificProblemFor(classTest1, new Problem("p1", "TODO : marker only", classTest1, 38, 55, -1, IMarker.SEVERITY_ERROR));
	assertEquals(JavaBuilder.SOURCE_ID, prob1[0].getSourceId());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=158611
// Checking the GENERATED_BY attribute
public void test0104() throws JavaModelException {
	IPath projectPath = env.addProject("Project");
	env.removePackageFragmentRoot(projectPath, "");
	IPath root = env.addPackageFragmentRoot(projectPath, "src");
	IPath classTest1 = env.addClass(root, "p1", "Test1",
		"package p1;\n" +
		"public class Test1 {}"
	);
	fullBuild();
	Problem[] prob1 = env.getProblemsFor(classTest1);
	expectingSpecificProblemFor(classTest1, 
		new Problem("p1", "The type java.lang.Object cannot be resolved. It is indirectly referenced from required .class files", classTest1, 0, 1, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR));
	assertEquals(JavaBuilder.SOURCE_ID, prob1[0].getSourceId());
}
}
