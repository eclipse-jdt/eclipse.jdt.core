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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.tests.util.Util;

public class IncrementalTests extends Tests {

	public IncrementalTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(IncrementalTests.class);
	}

	public void testDefaultPackage() {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		env.setOutputFolder(projectPath, ""); //$NON-NLS-1$

		env.addClass(projectPath, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {}"); //$NON-NLS-1$

		env.addClass(projectPath, "", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"public class B {}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(projectPath, "", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"public class B {A a;}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testDefaultPackage2() {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(projectPath, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {}"); //$NON-NLS-1$

		env.addClass(projectPath, "", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"public class B {}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(projectPath, "", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"public class B {A a;}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=17329
	 */
	public void testRenameMainType() {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		/* A.java */
		IPath pathToA = env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class A {}"); //$NON-NLS-1$

		/* B.java */
		IPath pathToB = env.addClass(root, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class B extends A {}"); //$NON-NLS-1$

		/* C.java */
		IPath pathToC = env.addClass(root, "p", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class C extends B {}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		/* Touch both A and C, removing A main type */
		pathToA = env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class _A {}"); //$NON-NLS-1$

		pathToC = env.addClass(root, "p", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class C extends B { }"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingProblemsFor(new IPath[]{ pathToA, pathToB, pathToC });
		expectingSpecificProblemFor(pathToA, new Problem("_A", "The public type _A must be defined in its own file", pathToA)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(pathToB, new Problem("B", "A cannot be resolved or is not a valid superclass", pathToB)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingSpecificProblemFor(pathToC, new Problem("C", "The hierarchy of the type C is inconsistent", pathToC)); //$NON-NLS-1$ //$NON-NLS-2$

		/* Touch both A and C, removing A main type */
		pathToA = env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class A {}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=17807
	 * case 1
	 */
	public void testRemoveSecondaryType() {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class AA {}	\n"+ //$NON-NLS-1$
			"class AZ {}"); //$NON-NLS-1$

		IPath pathToAB = env.addClass(root, "p", "AB", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class AB extends AZ {}"); //$NON-NLS-1$

		env.addClass(root, "p", "BB", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class BB {	\n"+ //$NON-NLS-1$
			"	void foo(){	\n" + //$NON-NLS-1$
			"		System.out.println(new AB());	\n" + //$NON-NLS-1$
			"		System.out.println(new ZA());	\n" + //$NON-NLS-1$
			"	}	\n" + //$NON-NLS-1$
			"}"); //$NON-NLS-1$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class ZZ {}	\n"+ //$NON-NLS-1$
			"class ZA {}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		/* Remove AZ and touch BB */
		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class AA {}"); //$NON-NLS-1$

		env.addClass(root, "p", "BB", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class BB {	\n"+ //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"		System.out.println(new AB());	\n" + //$NON-NLS-1$
			"		System.out.println(new ZA());	\n" + //$NON-NLS-1$
			"	}	\n" + //$NON-NLS-1$
			"}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingProblemsFor(new IPath[]{ pathToAB });
		expectingSpecificProblemFor(pathToAB, new Problem("AB", "AZ cannot be resolved or is not a valid superclass", pathToAB)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class AA {}	\n"+ //$NON-NLS-1$
			"class AZ {}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=17807
	 * case 2
	 */
	public void testRemoveSecondaryType2() {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class AA {}	\n"+ //$NON-NLS-1$
			"class AZ {}"); //$NON-NLS-1$

		env.addClass(root, "p", "AB", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class AB extends AZ {}"); //$NON-NLS-1$

		IPath pathToBB = env.addClass(root, "p", "BB", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class BB {	\n"+ //$NON-NLS-1$
			"	void foo(){	\n" + //$NON-NLS-1$
			"		System.out.println(new AB());	\n" + //$NON-NLS-1$
			"		System.out.println(new ZA());	\n" + //$NON-NLS-1$
			"	}	\n" + //$NON-NLS-1$
			"}"); //$NON-NLS-1$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class ZZ {}	\n"+ //$NON-NLS-1$
			"class ZA {}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		/* Remove ZA and touch BB */
		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class ZZ {}"); //$NON-NLS-1$

		pathToBB = env.addClass(root, "p", "BB", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class BB {	\n"+ //$NON-NLS-1$
			"	void foo() {	\n" + //$NON-NLS-1$
			"		System.out.println(new AB());	\n" + //$NON-NLS-1$
			"		System.out.println(new ZA());	\n" + //$NON-NLS-1$
			"	}	\n" + //$NON-NLS-1$
			"}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingProblemsFor(new IPath[]{ pathToBB });
		expectingSpecificProblemFor(pathToBB, new Problem("BB.foo()", "ZA cannot be resolved or is not a type", pathToBB)); //$NON-NLS-1$ //$NON-NLS-2$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;	\n"+ //$NON-NLS-1$
			"public class ZZ {}	\n"+ //$NON-NLS-1$
			"class ZA {}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMoveSecondaryType() {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class AA {} \n"+ //$NON-NLS-1$
			"class AZ {}"); //$NON-NLS-1$

		env.addClass(root, "p", "AB", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class AB extends AZ {}"); //$NON-NLS-1$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class ZZ {}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		/* Move AZ from AA to ZZ */
		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class AA {}"); //$NON-NLS-1$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class ZZ {} \n"+ //$NON-NLS-1$
			"class AZ {}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingNoProblems();

		/* Move AZ from ZZ to AA */
		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class AA {} \n"+ //$NON-NLS-1$
			"class AZ {}"); //$NON-NLS-1$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class ZZ {}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMoveMemberType() {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class AA {} \n"+ //$NON-NLS-1$
			"class AZ {static class M{}}"); //$NON-NLS-1$

		env.addClass(root, "p", "AB", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"import p.AZ.*; \n"+ //$NON-NLS-1$
			"import p.ZA.*; \n"+ //$NON-NLS-1$
			"public class AB extends M {}"); //$NON-NLS-1$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class ZZ {} \n"+ //$NON-NLS-1$
			"class ZA {}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingOnlySpecificProblemsFor(
			root, 
			new Problem[]{ 
				new Problem("", "The import p.ZA is never used", new Path("/Project/src/p/AB.java")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			});

		/* Move M from AA to ZZ */
		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class AA {} \n"+ //$NON-NLS-1$
			"class AZ {}"); //$NON-NLS-1$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class ZZ {} \n"+ //$NON-NLS-1$
			"class ZA {static class M{}}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingOnlySpecificProblemsFor(
			root, 
			new Problem[]{ 
				new Problem("", "The import p.AZ is never used", new Path("/Project/src/p/AB.java")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			});

		/* Move M from ZZ to AA */
		env.addClass(root, "p", "AA", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class AA {} \n"+ //$NON-NLS-1$
			"class AZ {static class M{}}"); //$NON-NLS-1$

		env.addClass(root, "p", "ZZ", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class ZZ {} \n"+ //$NON-NLS-1$
			"class ZA {}"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingOnlySpecificProblemsFor(
			root, 
			new Problem[]{ 
				new Problem("", "The import p.ZA is never used", new Path("/Project/src/p/AB.java")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			});
	}

	public void testMemberTypeFromClassFile() {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class A extends Z {M[] m;}"); //$NON-NLS-1$

		env.addClass(root, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class B {A a; E e; \n"+ //$NON-NLS-1$
			"void foo() { System.out.println(a.m); }}"); //$NON-NLS-1$

		env.addClass(root, "p", "E", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class E extends Z { \n"+ //$NON-NLS-1$
			"void foo() { System.out.println(new M()); }}"); //$NON-NLS-1$

		env.addClass(root, "p", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class Z {static class M {}}"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class B {A a; E e; \n"+ //$NON-NLS-1$
			"void foo( ) { System.out.println(a.m); }}"); //$NON-NLS-1$

		env.addClass(root, "p", "E", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class E extends Z { \n"+ //$NON-NLS-1$
			"void foo( ) { System.out.println(new M()); }}"); //$NON-NLS-1$

		env.addClass(root, "p", "Z", //$NON-NLS-1$ //$NON-NLS-2$
			"package p; \n"+ //$NON-NLS-1$
			"public class Z { static class M {} }"); //$NON-NLS-1$

		int previous = org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE;
		org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = 1; // reduce the lot size
		incrementalBuild(projectPath);
		org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = previous;
		expectingNoProblems();
	}
}