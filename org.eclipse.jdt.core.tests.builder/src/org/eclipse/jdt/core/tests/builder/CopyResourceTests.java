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

/**
 * Basic tests of the image builder.
 */
public class CopyResourceTests extends Tests {
	private static String[] EXCLUDED_TESTS = {};
	
	public CopyResourceTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new FilteredTestSuite(EXCLUDED_TESTS);
		suite.addTestSuite(CopyResourceTests.class);
		return suite;
	}

	public void testSimpleProject() {
		IPath projectPath = env.addProject("P1"); //$NON-NLS-1$
		IPath src = env.getPackageFragmentRootPath(projectPath, ""); //$NON-NLS-1$
		env.setOutputFolder(projectPath, ""); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		env.addFile(src, "z.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(projectPath.append("z.txt")); //$NON-NLS-1$

		env.removeFile(src.append("z.txt")); //$NON-NLS-1$
		IPath p = env.addFolder(src, "p"); //$NON-NLS-1$
		env.addFile(p, "p.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		incrementalBuild();
		expectingNoProblems();
		expectingNoPresenceOf(projectPath.append("z.txt")); //$NON-NLS-1$
		expectingPresenceOf(p.append("p.txt")); //$NON-NLS-1$
	}

	public void testProjectWithBin() {
		IPath projectPath = env.addProject("P2"); //$NON-NLS-1$
		IPath src = env.getPackageFragmentRootPath(projectPath, ""); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		env.addFile(src, "z.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("z.txt"), //$NON-NLS-1$
			projectPath.append("bin/z.txt") //$NON-NLS-1$
		});

		env.removeFile(src.append("z.txt")); //$NON-NLS-1$
		IPath p = env.addFolder(src, "p"); //$NON-NLS-1$
		env.addFile(p, "p.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		incrementalBuild();
		expectingNoProblems();
		expectingNoPresenceOf(new IPath[] {
			projectPath.append("z.txt"), //$NON-NLS-1$
			projectPath.append("bin/z.txt") //$NON-NLS-1$
		});
		expectingPresenceOf(new IPath[] {
			projectPath.append("p/p.txt"), //$NON-NLS-1$
			projectPath.append("bin/p/p.txt") //$NON-NLS-1$
		});
	}

	public void testProjectWithSrcBin() {
		IPath projectPath = env.addProject("P3"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		env.addFile(src, "z.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("src/z.txt"), //$NON-NLS-1$
			projectPath.append("bin/z.txt") //$NON-NLS-1$
		});

		env.removeFile(src.append("z.txt")); //$NON-NLS-1$
		env.addFile(src, "zz.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		incrementalBuild();
		expectingNoProblems();
		expectingNoPresenceOf(new IPath[] {
			projectPath.append("src/z.txt"), //$NON-NLS-1$
			projectPath.append("bin/z.txt") //$NON-NLS-1$
		});
		expectingPresenceOf(new IPath[] {
			projectPath.append("src/zz.txt"), //$NON-NLS-1$
			projectPath.append("bin/zz.txt") //$NON-NLS-1$
		});
	}

	public void testProjectWith2SrcBin() {
		IPath projectPath = env.addProject("P4"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1"); //$NON-NLS-1$
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		env.addFile(src1, "z.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$
		env.addFile(src2, "zz.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("src1/z.txt"), //$NON-NLS-1$
			projectPath.append("bin/z.txt"), //$NON-NLS-1$
			projectPath.append("src2/zz.txt"), //$NON-NLS-1$
			projectPath.append("bin/zz.txt") //$NON-NLS-1$
		});

		env.removeFile(src2.append("zz.txt")); //$NON-NLS-1$
		IPath p = env.addFolder(src2, "p"); //$NON-NLS-1$
		env.addFile(p, "p.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		incrementalBuild();
		expectingNoProblems();
		expectingNoPresenceOf(new IPath[] {
			projectPath.append("src2/zz.txt"), //$NON-NLS-1$
			projectPath.append("bin/zz.txt") //$NON-NLS-1$
		});
		expectingPresenceOf(new IPath[] {
			projectPath.append("src2/p/p.txt"), //$NON-NLS-1$
			projectPath.append("bin/p/p.txt") //$NON-NLS-1$
		});
	}

	public void testProjectWith2SrcAsBin() {
		IPath projectPath = env.addProject("P5"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1"); //$NON-NLS-1$
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "src2"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());

		env.addFile(src1, "z.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$
		env.addFile(src2, "zz.txt", ""); //$NON-NLS-1$ //$NON-NLS-2$

		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(new IPath[] {
			projectPath.append("src1/z.txt"), //$NON-NLS-1$
			projectPath.append("src2/zz.txt"), //$NON-NLS-1$
		});
		expectingNoPresenceOf(new IPath[] {
			projectPath.append("src2/z.txt"), //$NON-NLS-1$
			projectPath.append("bin") //$NON-NLS-1$
		});
	}
}