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
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

import java.util.*;

public class ClasspathTests extends Tests {
	private static String[] EXCLUDED_TESTS = {};

	public ClasspathTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new FilteredTestSuite(EXCLUDED_TESTS);
		suite.addTestSuite(ClasspathTests.class);
		return suite;
	}

	public void testClosedProject() throws JavaModelException {
		IPath project1Path = env.addProject("CP1"); //$NON-NLS-1$
		env.addExternalJar(project1Path, Util.getJavaClassLib());
		IPath jarPath = env.addInternalJar(project1Path, "temp.jar", new byte[] {0}); //$NON-NLS-1$

		IPath project2Path = env.addProject("CP2"); //$NON-NLS-1$
		env.addExternalJar(project2Path, Util.getJavaClassLib());
		env.addRequiredProject(project2Path, project1Path);

		IPath project3Path = env.addProject("CP3"); //$NON-NLS-1$
		env.addExternalJar(project3Path, Util.getJavaClassLib());
		env.addExternalJar(project3Path, jarPath.toString());

		fullBuild();
		expectingNoProblems();

		//----------------------------
		//           Step 2
		//----------------------------
		env.closeProject(project1Path);

		incrementalBuild();
		expectingOnlyProblemsFor(new IPath[] {project2Path, project3Path});
		expectingOnlySpecificProblemsFor(project2Path,
			new Problem[] {
				new Problem("", "The project was not built due to classpath errors (incomplete or involved in cycle).", project2Path), //$NON-NLS-1$ //$NON-NLS-2$
				new Problem("Build path", "Missing required Java project: CP1.", project2Path) //$NON-NLS-1$ //$NON-NLS-2$
			}
		);
		expectingOnlySpecificProblemsFor(project3Path,
			new Problem[] {
				new Problem("", "The project was not built due to classpath errors (incomplete or involved in cycle).", project3Path), //$NON-NLS-1$ //$NON-NLS-2$
				new Problem("Build path", "Missing required library: 'CP1/temp.jar'.", project3Path) //$NON-NLS-1$ //$NON-NLS-2$
			}
		);

		env.openProject(project1Path);
		incrementalBuild();
		expectingNoProblems();

		//----------------------------
		//           Step 3
		//----------------------------
		Hashtable options = JavaCore.getOptions();
		options.put(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH, JavaCore.IGNORE);
		JavaCore.setOptions(options);
		env.closeProject(project1Path);

		incrementalBuild();
		expectingOnlyProblemsFor(new IPath[] {project2Path, project3Path});
		expectingOnlySpecificProblemFor(project2Path,
			new Problem("Build path", "Missing required Java project: CP1.", project2Path) //$NON-NLS-1$ //$NON-NLS-2$
		);
		expectingOnlySpecificProblemFor(project3Path,
			new Problem("Build path", "Missing required library: 'CP1/temp.jar'.", project3Path) //$NON-NLS-1$ //$NON-NLS-2$
		);

		env.openProject(project1Path);
		incrementalBuild();
		expectingNoProblems();

		options.put(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH, JavaCore.ABORT);
		JavaCore.setOptions(options);
	}

	public void testMissingProject() throws JavaModelException {
		IPath project1Path = env.addProject("MP1"); //$NON-NLS-1$
		env.addExternalJar(project1Path, Util.getJavaClassLib());

		IPath project2Path = env.addProject("MP2"); //$NON-NLS-1$
		env.addExternalJar(project2Path, Util.getJavaClassLib());
		env.addRequiredProject(project2Path, project1Path);

		fullBuild();
		expectingNoProblems();

		//----------------------------
		//           Step 2
		//----------------------------
		env.removeProject(project1Path);

		incrementalBuild();
		expectingOnlyProblemsFor(project2Path);
		expectingOnlySpecificProblemsFor(project2Path,
			new Problem[] {
				new Problem("", "The project was not built due to classpath errors (incomplete or involved in cycle).", project2Path), //$NON-NLS-1$ //$NON-NLS-2$
				new Problem("Build path", "Missing required Java project: MP1.", project2Path) //$NON-NLS-1$ //$NON-NLS-2$
			}
		);

		project1Path = env.addProject("MP1"); //$NON-NLS-1$
		env.addExternalJar(project1Path, Util.getJavaClassLib());

		incrementalBuild();
		expectingNoProblems();

		//----------------------------
		//           Step 3
		//----------------------------
		Hashtable options = JavaCore.getOptions();
		options.put(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH, JavaCore.IGNORE);
		JavaCore.setOptions(options);
		env.removeProject(project1Path);

		incrementalBuild();
		expectingOnlyProblemsFor(project2Path);
		expectingOnlySpecificProblemFor(project2Path,
			new Problem("Build path", "Missing required Java project: MP1.", project2Path) //$NON-NLS-1$ //$NON-NLS-2$
		);

		project1Path = env.addProject("MP1"); //$NON-NLS-1$
		env.addExternalJar(project1Path, Util.getJavaClassLib());

		incrementalBuild();
		expectingNoProblems();

		options.put(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH, JavaCore.ABORT);
		JavaCore.setOptions(options);
	}

	public void testMissingLibrary1() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		IPath bin = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		IPath classTest1 = env.addClass(root, "p1", "Test1", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class Test1 {}" //$NON-NLS-1$
		);

		fullBuild();
		expectingOnlyProblemsFor(new IPath[] {projectPath, classTest1});
		expectingOnlySpecificProblemsFor(projectPath,
			new Problem[] {
				new Problem("", "The project was not built since its classpath is incomplete. Cannot find the class file for java.lang.Object. Fix the classpath then try rebuilding this project.", projectPath), //$NON-NLS-1$ //$NON-NLS-2$
				new Problem("p1", "This compilation unit indirectly references the missing type java.lang.Object (typically some required class file is referencing a type outside the classpath)", classTest1) //$NON-NLS-1$ //$NON-NLS-2$
			}
		);

		//----------------------------
		//           Step 2
		//----------------------------	
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		incrementalBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[]{
			bin.append("p1").append("Test1.class"), //$NON-NLS-1$ //$NON-NLS-2$
		});
	}
	
	public void testMissingLibrary2() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		IPath bin = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		IPath classTest1 = env.addClass(root, "p1", "Test1", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class Test1 {}" //$NON-NLS-1$
		);
		IPath classTest2 = env.addClass(root, "p2", "Test2", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class Test2 {}" //$NON-NLS-1$
		);
		IPath classTest3 = env.addClass(root, "p2", "Test3", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n"+ //$NON-NLS-1$
			"public class Test3 {}" //$NON-NLS-1$
		);

		fullBuild();
		expectingSpecificProblemFor(
			projectPath,
			new Problem("", "The project was not built since its classpath is incomplete. Cannot find the class file for java.lang.Object. Fix the classpath then try rebuilding this project.", projectPath)); //$NON-NLS-1$ //$NON-NLS-2$
		
		Problem[] prob1 = env.getProblemsFor(classTest1);
		Problem[] prob2 = env.getProblemsFor(classTest2);
		Problem[] prob3 = env.getProblemsFor(classTest3);
		assertEquals("too much problems", prob1.length + prob2.length + prob3.length,1);
		if(prob1.length == 1) {
			expectingSpecificProblemFor(classTest1, new Problem("p1", "This compilation unit indirectly references the missing type java.lang.Object (typically some required class file is referencing a type outside the classpath)", classTest1)); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (prob2.length == 1) {
			expectingSpecificProblemFor(classTest2, new Problem("p2", "This compilation unit indirectly references the missing type java.lang.Object (typically some required class file is referencing a type outside the classpath)", classTest2)); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			expectingSpecificProblemFor(classTest3, new Problem("p2", "This compilation unit indirectly references the missing type java.lang.Object (typically some required class file is referencing a type outside the classpath)", classTest3)); //$NON-NLS-1$ //$NON-NLS-2$
		}

		//----------------------------
		//           Step 2
		//----------------------------	
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		incrementalBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[]{
			bin.append("p1").append("Test1.class"), //$NON-NLS-1$ //$NON-NLS-2$
			bin.append("p2").append("Test2.class"), //$NON-NLS-1$ //$NON-NLS-2$
			bin.append("p2").append("Test3.class") //$NON-NLS-1$ //$NON-NLS-2$
		});
	}
}