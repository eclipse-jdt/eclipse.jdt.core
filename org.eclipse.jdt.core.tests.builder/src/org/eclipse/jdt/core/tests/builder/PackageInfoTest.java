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

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

public class PackageInfoTest extends Tests {
	
public PackageInfoTest(String name) {
	super(name);
}
	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
//	static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 31 };
//		TESTS_RANGE = new int[] { 21, 50 };
//	}
	public static Test suite() {
        return new TestSuite(PackageInfoTest.class);
	}
public void test001() throws JavaModelException {
    IPath projectPath = env.addProject("Project", "1.5"); //$NON-NLS-1$
    env.addExternalJars(projectPath, Util.getJavaClassLibs());
    fullBuild(projectPath);
    
    // remove old package fragment root so that names don't collide
    env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
    
    IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
    env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
    
    env.addClass(root, "pack", "Annot", //$NON-NLS-1$ //$NON-NLS-2$
        "package pack;\n"+ //$NON-NLS-1$
        "public @interface Annot {}" //$NON-NLS-1$
    );

    incrementalBuild(projectPath);
   
    IPath packageInfoPath = env.addFile(root, "pack/package-info.java", //$NON-NLS-1$ //$NON-NLS-2$
        "@Annot package p1" //$NON-NLS-1$
    );
        
    incrementalBuild(projectPath);
    expectingOnlyProblemsFor(packageInfoPath);
    final Problem[] problems = env.getProblems();
    assertNotNull(problems);
    final StringWriter stringWriter = new StringWriter();
    final PrintWriter writer = new PrintWriter(stringWriter);
    final int problemsLength = problems.length;
    if (problemsLength == 1) {
        writer.print(problems[0].getMessage());    
    } else {
        for (int i = 0; i < problemsLength - 1; i++) {
            writer.println(problems[i].getMessage());
        }
        writer.print(problems[problemsLength - 1].getMessage());
    }
    writer.close();
    final String expectedOutput =
        "The declared package does not match the expected package pack\n" + 
        "Syntax error on token \"p1\", ; expected after this token"; 
    assertSourceEquals("Different messages", expectedOutput, stringWriter.toString());
}
protected void assertSourceEquals(String message, String expected, String actual) {
    if (actual == null) {
        assertEquals(message, expected, null);
        return;
    }
    actual = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(actual);
    if (!actual.equals(expected)) {
        System.out.print(org.eclipse.jdt.core.tests.util.Util.displayString(actual.toString(), 0));
    }
    assertEquals(message, expected, actual);
}
public static Class testClass() {
	return PackageInfoTest.class;
}
}
