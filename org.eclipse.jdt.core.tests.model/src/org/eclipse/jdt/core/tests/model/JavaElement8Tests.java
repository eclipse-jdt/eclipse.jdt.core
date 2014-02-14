/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;

public class JavaElement8Tests extends AbstractJavaModelTests { 

	static {
//		TESTS_NAMES = new String[] {"testBug428178"};
	}

	public JavaElement8Tests(String name) {
		super(name);
		this.endChar = "";
	}
	public static Test suite() {
		if (TESTS_PREFIX != null || TESTS_NAMES != null || TESTS_NUMBERS!=null || TESTS_RANGE !=null) {
			return buildModelTestSuite(JavaElement8Tests.class);
		}
		TestSuite suite = new Suite(JavaElement8Tests.class.getName());
		suite.addTest(new JavaElement8Tests("testBug428178"));
		suite.addTest(new JavaElement8Tests("testBug428178a"));
		return suite;
	}
	public void testBug428178() throws Exception {
		try {
			IJavaProject project = createJavaProject("Bug428178", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
				String fileContent =  "package p;\n" +
						 "public interface Test {\n" +
						 "	static void main(String[] args) {\n" +
						 "		System.out.println(\"Hello\");\n" +
						 "	}\n" +
						 "}";
				createFolder("/Bug428178/src/p");
				createFile(	"/Bug428178/src/p/Test.java",	fileContent);

				ICompilationUnit unit = getCompilationUnit("/Bug428178/src/p/Test.java");
				IMethod method = unit.getTypes()[0].getMethods()[0];
				assertNotNull("Method should not be null", method);
				assertTrue("Should be a main method", method.isMainMethod());
		}
		finally {
			deleteProject("Bug428178");
		}
	}
	public void testBug428178a() throws Exception {
		try {
			IJavaProject project = createJavaProject("Bug428178", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
			String fileContent =  "package p;\n" +
					 "public interface Test {\n" +
					 "	static void main(String[] args) {\n" +
					 "		System.out.println(\"Hello\");\n" +
					 "	}\n" +
					 "}";
			addLibrary(project, 
							"lib.jar", 
							"src.zip", new 
							String[] {"p/Test.java", fileContent},
							JavaCore.VERSION_1_8);
				IType type = getPackageFragmentRoot("Bug428178", "lib.jar").getPackageFragment("p").getClassFile("Test.class").getType();
				IMethod method = type.getMethods()[0];
				assertNotNull("Method should not be null", method);
				assertTrue("Should be a main method", method.isMainMethod());
		}
		finally {
			deleteProject("Bug428178");
		}
	}
}
