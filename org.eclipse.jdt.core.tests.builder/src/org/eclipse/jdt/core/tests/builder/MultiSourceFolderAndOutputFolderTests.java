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
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

/**
 * Basic tests of the image builder.
 */
public class MultiSourceFolderAndOutputFolderTests extends Tests {
	private static String[] EXCLUDED_TESTS = {};

	public MultiSourceFolderAndOutputFolderTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new FilteredTestSuite(EXCLUDED_TESTS);
		suite.addTestSuite(MultiSourceFolderAndOutputFolderTests.class);
		return suite;
	}

	public void test0001() throws JavaModelException {
		IPath projectPath = env.addProject("P"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1", null, "bin1"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		
		env.addClass(src1, "", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"public class X {}" //$NON-NLS-1$
			);
		
		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(projectPath.append("bin1/X.class")); //$NON-NLS-1$
		expectingNoPresenceOf(projectPath.append("bin/X.class")); //$NON-NLS-1$
	}

	public void test0002() throws JavaModelException {
		IPath projectPath = env.addProject("P"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath src1 = env.addPackageFragmentRoot(projectPath, "src1", null, "bin1"); //$NON-NLS-1$
		IPath src2 = env.addPackageFragmentRoot(projectPath, "src2"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		
		env.addClass(src1, "p", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class X {}" //$NON-NLS-1$
			);
			
		env.addClass(src2, "p", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class Y {}" //$NON-NLS-1$
			);
		
		fullBuild();
		expectingNoProblems();
		expectingPresenceOf(projectPath.append("bin1/p/X.class")); //$NON-NLS-1$
		expectingPresenceOf(projectPath.append("bin/p/Y.class")); //$NON-NLS-1$
		expectingNoPresenceOf(projectPath.append("bin/p/X.class")); //$NON-NLS-1$
		expectingNoPresenceOf(projectPath.append("bin1/p/Y.class")); //$NON-NLS-1$
	}
	
	public void test0003() throws JavaModelException {
		try {
			IPath projectPath = env.addProject("P"); //$NON-NLS-1$
			env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
			env.addPackageFragmentRoot(projectPath, "src", null, null); //$NON-NLS-1$
			env.addPackageFragmentRoot(projectPath, "src/f1", null, null); //$NON-NLS-1$
			env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
			env.addExternalJar(projectPath, Util.getJavaClassLib());
			
			fullBuild();
			expectingNoProblems();
			
			assertTrue("JavaModelException", false); //$NON-NLS-1$
		} catch (JavaModelException e) {
			assertEquals(
				"Cannot nest '/P/src/f1' inside '/P/src'. " + //$NON-NLS-1$
				"To enable the nesting exclude 'f1/' from '/P/src'.", //$NON-NLS-1$
				e.getMessage()
			); 
		}
	}
		
	public void test0004() throws JavaModelException {
		try {
			IPath projectPath = env.addProject("P"); //$NON-NLS-1$
			env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
			env.addPackageFragmentRoot(projectPath, "src/f1", null, null); //$NON-NLS-1$
			env.addPackageFragmentRoot(projectPath, "src", new IPath[]{new Path("f1")}, null); //$NON-NLS-1$
			env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
			env.addExternalJar(projectPath, Util.getJavaClassLib());
			
			fullBuild();
			expectingNoProblems();
			
			assertTrue("JavaModelException", false); //$NON-NLS-1$
		} catch (JavaModelException e) {
			assertEquals(
				"End exclusion filter 'f1' with / to fully exclude '/P/src/f1'.", //$NON-NLS-1$
				e.getMessage()
			); 
		}
	}
	
	public void test0005() throws JavaModelException {
		IPath projectPath = env.addProject("P"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		env.addPackageFragmentRoot(projectPath, "src/f1", null, null); //$NON-NLS-1$
		env.addPackageFragmentRoot(projectPath, "src", new IPath[]{new Path("f1/")}, null); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		
		fullBuild();
		expectingNoProblems();
	}
	
	public void test0006() throws JavaModelException {
		IPath projectPath = env.addProject("P"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath srcF1 = env.addPackageFragmentRoot(projectPath, "src/f1", null, null); //$NON-NLS-1$
		IPath src = env.addPackageFragmentRoot(projectPath, "src", new IPath[]{new Path("f1/")}, null); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		
		env.addClass(src, "p", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class X extends p2.Y{}" //$NON-NLS-1$
			);
			
		env.addClass(srcF1, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;"+ //$NON-NLS-1$
			"public class Y {}" //$NON-NLS-1$
			);
			
		fullBuild();
		expectingNoProblems();
	}
	
	public void test0007() throws JavaModelException {
		IPath projectPath = env.addProject("P"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath srcF1 = env.addPackageFragmentRoot(projectPath, "src/f1", null, null); //$NON-NLS-1$
		IPath src = env.addPackageFragmentRoot(projectPath, "src", new IPath[]{new Path("f1/")}, null); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		
		IPath xPath = env.addClass(src, "p", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class X extends f1.p2.Y{}" //$NON-NLS-1$
			);
			
		env.addClass(srcF1, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;"+ //$NON-NLS-1$
			"public class Y {}" //$NON-NLS-1$
			);
			
		fullBuild();
		expectingOnlyProblemsFor(xPath);
	}
	
	public void test0008() throws JavaModelException {
		IPath projectPath = env.addProject("P"); //$NON-NLS-1$
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath srcF1 = env.addPackageFragmentRoot(projectPath, "src/f1", null, null); //$NON-NLS-1$
		IPath src = env.addPackageFragmentRoot(projectPath, "src", new IPath[]{new Path("f1/")}, null); //$NON-NLS-1$
		env.addExternalJar(projectPath, Util.getJavaClassLib());
		
		IPath xPath = env.addClass(src, "p", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p;"+ //$NON-NLS-1$
			"public class X extends p2.Y{}" //$NON-NLS-1$
			);
			
		env.addClass(srcF1, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;"+ //$NON-NLS-1$
			"public abstract class Y {"+ //$NON-NLS-1$
			"  abstract void foo();"+ //$NON-NLS-1$
			"}" //$NON-NLS-1$
			);
			
		fullBuild();
		expectingOnlyProblemsFor(xPath);
		
		env.addClass(srcF1, "p2", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;"+ //$NON-NLS-1$
			"public class Y {}" //$NON-NLS-1$
			);
		
		incrementalBuild();
		
		expectingNoProblems();
	}
}