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

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=17329
	 */
	public void testRenameMainType() {
		IPath projectPath = env.addProject("Project");
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		/* A.java */
		IPath pathToA = env.addClass(root, "p", "A",
			"package p;	\n"+
			"public class A {}");

		/* B.java */
		IPath pathToB = env.addClass(root, "p", "B",
			"package p;	\n"+
			"public class B extends A {}");

		/* C.java */
		IPath pathToC = env.addClass(root, "p", "C",
			"package p;	\n"+
			"public class C extends B {}");

		fullBuild(projectPath);
		expectingNoProblems();

		/* Touch both A and C, removing A main type */
		pathToA = env.addClass(root, "p", "A",
			"package p;	\n"+
			"public class _A {}");

		pathToC = env.addClass(root, "p", "C",
			"package p;	\n"+
			"public class C extends B { }");

		incrementalBuild(projectPath);
		expectingProblemsFor(new IPath[]{ pathToA, pathToB, pathToC });
		expectingSpecificProblemFor(pathToA, new Problem("_A", "The public type _A must be defined in its own file", pathToA));
		expectingSpecificProblemFor(pathToB, new Problem("B", "A cannot be resolved or is not a valid superclass", pathToB));
		expectingSpecificProblemFor(pathToC, new Problem("C", "The hierarchy of the type C is inconsistent", pathToC));

		/* Touch both A and C, removing A main type */
		pathToA = env.addClass(root, "p", "A",
			"package p;	\n"+
			"public class A {}");

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=17807
	 * case 1
	 */
	public void testRemoveSecondaryType() {
		IPath projectPath = env.addProject("Project");
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "AA",
			"package p;	\n"+
			"public class AA {}	\n"+
			"class AZ {}");

		IPath pathToAB = env.addClass(root, "p", "AB",
			"package p;	\n"+
			"public class AB extends AZ {}");

		env.addClass(root, "p", "BB",
			"package p;	\n"+
			"public class BB {	\n"+
			"	void foo(){	\n" +
			"		System.out.println(new AB());	\n" +
			"		System.out.println(new ZA());	\n" +
			"	}	\n" +
			"}");

		env.addClass(root, "p", "ZZ",
			"package p;	\n"+
			"public class ZZ {}	\n"+
			"class ZA {}");

		fullBuild(projectPath);
		expectingNoProblems();

		/* Remove AZ and touch BB */
		env.addClass(root, "p", "AA",
			"package p;	\n"+
			"public class AA {}");

		env.addClass(root, "p", "BB",
			"package p;	\n"+
			"public class BB {	\n"+
			"	void foo() {	\n" +
			"		System.out.println(new AB());	\n" +
			"		System.out.println(new ZA());	\n" +
			"	}	\n" +
			"}");

		incrementalBuild(projectPath);
		expectingProblemsFor(new IPath[]{ pathToAB });
		expectingSpecificProblemFor(pathToAB, new Problem("AB", "AZ cannot be resolved or is not a valid superclass", pathToAB));

		env.addClass(root, "p", "AA",
			"package p;	\n"+
			"public class AA {}	\n"+
			"class AZ {}");

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=17807
	 * case 2
	 */
	public void testRemoveSecondaryType2() {
		IPath projectPath = env.addProject("Project");
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "AA",
			"package p;	\n"+
			"public class AA {}	\n"+
			"class AZ {}");

		env.addClass(root, "p", "AB",
			"package p;	\n"+
			"public class AB extends AZ {}");

		IPath pathToBB = env.addClass(root, "p", "BB",
			"package p;	\n"+
			"public class BB {	\n"+
			"	void foo(){	\n" +
			"		System.out.println(new AB());	\n" +
			"		System.out.println(new ZA());	\n" +
			"	}	\n" +
			"}");

		env.addClass(root, "p", "ZZ",
			"package p;	\n"+
			"public class ZZ {}	\n"+
			"class ZA {}");

		fullBuild(projectPath);
		expectingNoProblems();

		/* Remove ZA and touch BB */
		env.addClass(root, "p", "ZZ",
			"package p;	\n"+
			"public class ZZ {}");

		pathToBB = env.addClass(root, "p", "BB",
			"package p;	\n"+
			"public class BB {	\n"+
			"	void foo() {	\n" +
			"		System.out.println(new AB());	\n" +
			"		System.out.println(new ZA());	\n" +
			"	}	\n" +
			"}");

		incrementalBuild(projectPath);
		expectingProblemsFor(new IPath[]{ pathToBB });
		expectingSpecificProblemFor(pathToBB, new Problem("BB.foo()", "ZA cannot be resolved or is not a type", pathToBB));

		env.addClass(root, "p", "ZZ",
			"package p;	\n"+
			"public class ZZ {}	\n"+
			"class ZA {}");

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMoveSecondaryType() {
		IPath projectPath = env.addProject("Project");
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "AA",
			"package p; \n"+
			"public class AA {} \n"+
			"class AZ {}");

		env.addClass(root, "p", "AB",
			"package p; \n"+
			"public class AB extends AZ {}");

		env.addClass(root, "p", "ZZ",
			"package p; \n"+
			"public class ZZ {}");

		fullBuild(projectPath);
		expectingNoProblems();

		/* Move AZ from AA to ZZ */
		env.addClass(root, "p", "AA",
			"package p; \n"+
			"public class AA {}");

		env.addClass(root, "p", "ZZ",
			"package p; \n"+
			"public class ZZ {} \n"+
			"class AZ {}");

		incrementalBuild(projectPath);
		expectingNoProblems();

		/* Move AZ from ZZ to AA */
		env.addClass(root, "p", "AA",
			"package p; \n"+
			"public class AA {} \n"+
			"class AZ {}");

		env.addClass(root, "p", "ZZ",
			"package p; \n"+
			"public class ZZ {}");

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testMoveMemberType() {
		IPath projectPath = env.addProject("Project");
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "AA",
			"package p; \n"+
			"public class AA {} \n"+
			"class AZ {static class M{}}");

		env.addClass(root, "p", "AB",
			"package p; \n"+
			"import p.AZ.*; \n"+
			"import p.ZA.*; \n"+
			"public class AB extends M {}");

		env.addClass(root, "p", "ZZ",
			"package p; \n"+
			"public class ZZ {} \n"+
			"class ZA {}");

		fullBuild(projectPath);
		expectingOnlySpecificProblemsFor(
			root, 
			new Problem[]{ 
				new Problem("", "The import p.ZA is never used", new Path("/Project/src/p/AB.java")),
			});

		/* Move M from AA to ZZ */
		env.addClass(root, "p", "AA",
			"package p; \n"+
			"public class AA {} \n"+
			"class AZ {}");

		env.addClass(root, "p", "ZZ",
			"package p; \n"+
			"public class ZZ {} \n"+
			"class ZA {static class M{}}");

		incrementalBuild(projectPath);
		expectingOnlySpecificProblemsFor(
			root, 
			new Problem[]{ 
				new Problem("", "The import p.AZ is never used", new Path("/Project/src/p/AB.java")),
			});

		/* Move M from ZZ to AA */
		env.addClass(root, "p", "AA",
			"package p; \n"+
			"public class AA {} \n"+
			"class AZ {static class M{}}");

		env.addClass(root, "p", "ZZ",
			"package p; \n"+
			"public class ZZ {} \n"+
			"class ZA {}");

		incrementalBuild(projectPath);
		expectingOnlySpecificProblemsFor(
			root, 
			new Problem[]{ 
				new Problem("", "The import p.ZA is never used", new Path("/Project/src/p/AB.java")),
			});
	}

	public void testMemberTypeFromClassFile() {
		IPath projectPath = env.addProject("Project");
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "A",
			"package p; \n"+
			"public class A extends Z {M[] m;}");

		env.addClass(root, "p", "B",
			"package p; \n"+
			"public class B {A a; E e; \n"+
			"void foo() { System.out.println(a.m); }}");

		env.addClass(root, "p", "E",
			"package p; \n"+
			"public class E extends Z { \n"+
			"void foo() { System.out.println(new M()); }}");

		env.addClass(root, "p", "Z",
			"package p; \n"+
			"public class Z {static class M {}}");

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p", "B",
			"package p; \n"+
			"public class B {A a; E e; \n"+
			"void foo( ) { System.out.println(a.m); }}");

		env.addClass(root, "p", "E",
			"package p; \n"+
			"public class E extends Z { \n"+
			"void foo( ) { System.out.println(new M()); }}");

		env.addClass(root, "p", "Z",
			"package p; \n"+
			"public class Z { static class M {} }");

		int previous = org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE;
		org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = 1; // reduce the lot size
		incrementalBuild(projectPath);
		org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = previous;
		expectingNoProblems();
	}
}