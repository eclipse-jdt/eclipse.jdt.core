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

/**
 * Basic tests of the image builder.
 */
public class OutputFolderTests extends Tests {
	private static String[] EXCLUDED_TESTS = {};

	public OutputFolderTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new FilteredTestSuite(EXCLUDED_TESTS);
		suite.addTestSuite(OutputFolderTests.class);
		return suite;
	}

	public void testChangeOutputFolder() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		IPath bin1 = env.setOutputFolder(projectPath, "bin1"); //$NON-NLS-1$

		env.addClass(root, "p", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;\n" + //$NON-NLS-1$
			"public class Test {}" //$NON-NLS-1$
		);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(bin1.append("p/Test.class")); //$NON-NLS-1$

		IPath bin2 = env.setOutputFolder(projectPath, "bin2"); //$NON-NLS-1$
		incrementalBuild();
		expectingNoProblems();
		expectingPresenceOf(bin2.append("p/Test.class")); //$NON-NLS-1$
	}

	public void testDeleteOutputFolder() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		
		IPath root = env.getPackageFragmentRootPath(projectPath, ""); //$NON-NLS-1$
		IPath bin = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"public class Test {}" //$NON-NLS-1$
		);
		env.addFile(root, "Test.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[]{
			bin.append("Test.class"), //$NON-NLS-1$
			bin.append("Test.txt") //$NON-NLS-1$
		});

		env.removeFolder(bin);
//		incrementalBuild(); currently not detected by the incremental builder... should it?
		fullBuild();
		expectingPresenceOf(new IPath[]{
			bin.append("Test.class"), //$NON-NLS-1$
			bin.append("Test.txt") //$NON-NLS-1$
		});
	}

	public void testSimpleProject() throws JavaModelException {
		IPath projectPath = env.addProject("P1"); //$NON-NLS-1$
		IPath bin = env.setOutputFolder(projectPath, ""); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		env.addClass(projectPath, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {}" //$NON-NLS-1$
			);
		env.addClass(projectPath, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class B {}" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			bin.append("A.class"), //$NON-NLS-1$
			bin.append("p/B.class") //$NON-NLS-1$
		});
	}

	public void testProjectWithBin() throws JavaModelException {
		IPath projectPath = env.addProject("P2"); //$NON-NLS-1$
		IPath src = env.getPackageFragmentRootPath(projectPath, ""); //$NON-NLS-1$
		IPath bin = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		env.addClass(src, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {}" //$NON-NLS-1$
			);
		env.addClass(src, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class B {}" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			bin.append("A.class"), //$NON-NLS-1$
			bin.append("p/B.class") //$NON-NLS-1$
		});
	}

	public void testProjectWithSrcBin() throws JavaModelException {
		IPath projectPath = env.addProject("P3"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		IPath bin = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		env.addClass(src, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {}" //$NON-NLS-1$
			);
		env.addClass(src, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class B {}" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			bin.append("A.class"), //$NON-NLS-1$
			bin.append("p/B.class") //$NON-NLS-1$
		});
	}

	public void testProjectWith2SrcBin() throws JavaModelException {
		IPath projectPath = env.addProject("P4"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1"); //$NON-NLS-1$
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2"); //$NON-NLS-1$
		IPath bin = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		env.addClass(src1, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {}" //$NON-NLS-1$
			);
		env.addClass(src2, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class B {}" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			bin.append("A.class"), //$NON-NLS-1$
			bin.append("p/B.class") //$NON-NLS-1$
		});
	}

	public void testProjectWith2SrcAsBin() throws JavaModelException {
		IPath projectPath = env.addProject("P5"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1", null, "src1"); //$NON-NLS-1$ //$NON-NLS-2$
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2", null, "src2"); //$NON-NLS-1$ //$NON-NLS-2$
		IPath bin = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		env.addClass(src1, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {}" //$NON-NLS-1$
			);
		env.addClass(src2, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class B {}" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			src1.append("A.class"), //$NON-NLS-1$
			src2.append("p/B.class") //$NON-NLS-1$
		});
	}

	public void testProjectWith2Src2Bin() throws JavaModelException {
		IPath projectPath = env.addProject("P6"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1", null, "bin1"); //$NON-NLS-1$ //$NON-NLS-2$
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2", null, "bin2"); //$NON-NLS-1$ //$NON-NLS-2$
		env.setOutputFolder(projectPath, "bin1"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		env.addClass(src1, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {}" //$NON-NLS-1$
			);
		env.addClass(src2, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class B {}" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("bin1/A.class"), //$NON-NLS-1$
			projectPath.append("bin2/p/B.class") //$NON-NLS-1$
		});
	}

	public void testProjectWith3Src2Bin() throws JavaModelException {
		IPath projectPath = env.addProject("P6"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1", null, "bin1"); //$NON-NLS-1$ //$NON-NLS-2$
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2", null, "bin2"); //$NON-NLS-1$ //$NON-NLS-2$
		IPath src3 = env.addPackageFragmentRoot(projectPath, "src3", null, "bin2"); //$NON-NLS-1$ //$NON-NLS-2$
		env.setOutputFolder(projectPath, "bin1"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		env.addClass(src1, "", "A", //$NON-NLS-1$ //$NON-NLS-2$
			"public class A {}" //$NON-NLS-1$
			);
		env.addClass(src2, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class B {}" //$NON-NLS-1$
			);
		env.addClass(src3, "", "C", //$NON-NLS-1$ //$NON-NLS-2$
			"public class C {}" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("bin1/A.class"), //$NON-NLS-1$
			projectPath.append("bin2/p/B.class"), //$NON-NLS-1$
			projectPath.append("bin2/C.class") //$NON-NLS-1$
		});
	}

	public void test2ProjectWith1Bin() throws JavaModelException {
		IPath projectPath = env.addProject("P7"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		IPath bin = env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		IPath projectPath2 = env.addProject("P8"); //$NON-NLS-1$
		IPath binLocation = env.getProject(projectPath).getFolder("bin").getLocation(); //$NON-NLS-1$
		env.setExternalOutputFolder(projectPath2, "externalBin", binLocation); //$NON-NLS-1$
		env.addExternalJar(projectPath2, Util.getJavaClassLib());
		env.addRequiredProject(projectPath2, projectPath);

		env.addClass(projectPath2, "p", "B", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class B {}" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(bin.append("p/B.class")); //$NON-NLS-1$
	}
}