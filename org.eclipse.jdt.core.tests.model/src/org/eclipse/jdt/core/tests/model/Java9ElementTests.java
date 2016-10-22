/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IModuleDescription.IModuleReference;
import org.eclipse.jdt.core.IModuleDescription.IPackageExport;
import org.eclipse.jdt.core.IModuleDescription.IProvidedService;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;

import junit.framework.Test;

public class Java9ElementTests extends AbstractJavaModelTests { 

	static {
//		TESTS_NAMES = new String[] {"test009"};
	}

	public Java9ElementTests(String name) {
		super(name);
	}
	public static Test suite() {
		return buildModelTestSuite(AbstractCompilerTest.F_9, Java9ElementTests.class);
	}
	public void test001() throws Exception {
		try {
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.9");
			project.open(null);
				String fileContent =  "module my.mod{}\n";
				createFile(	"/Java9Elements/src/module-info.java",	fileContent);

				ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
				IModuleDescription mod = unit.getModule();
				assertNotNull("Module should not be null", mod);
				IPackageExport[] exportedPackages = mod.getExportedPackages();
				assertNotNull("should not be null", exportedPackages);
				assertEquals("Incorrect no of exports", 0, exportedPackages.length);
				IModuleReference[] requiredModules = mod.getRequiredModules();
				assertNotNull("should not be null", requiredModules);
				assertEquals("Incorrect no of required modules", 0, requiredModules.length);
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void test002() throws Exception {
		try {
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.9");
			project.open(null);
				String fileContent =  "module my.mod{\n" +
						 "	exports p.q.r;" +
						 "	exports a.b.c;\n" +
						 "	requires java.sql;\n" +
						 "	requires public java.desktop;\n" +
						 "}";
				createFile(	"/Java9Elements/src/module-info.java",	fileContent);

				ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
				IModuleDescription mod = unit.getModule();
				assertNotNull("Module should not be null", mod);
				IPackageExport[] exportedPackages = mod.getExportedPackages();
				assertNotNull("should not be null", exportedPackages);
				assertEquals("Incorrect no of exports", 2, exportedPackages.length);
				IPackageExport export = exportedPackages[0];
				assertEquals("Incorrect package name", "p.q.r", export.getPackageName());
				export = exportedPackages[1];
				assertEquals("Incorrect package name", "a.b.c", export.getPackageName());
				IModuleReference[] requiredModules = mod.getRequiredModules();
				assertNotNull("should not be null", requiredModules);
				assertEquals("Incorrect no of required modules", 2, requiredModules.length);
				IModuleReference ref = requiredModules[0];
				assertEquals("Incorrect package name", "java.sql", ref.getModuleName());
				assertFalse("Module requires should not be public", ref.isPublic());
				ref = requiredModules[1];
				assertEquals("Incorrect package name", "java.desktop", ref.getModuleName());
				assertTrue("Module requires should be public", ref.isPublic());
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void test003() throws Exception {
		try {
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.9");
			project.open(null);
				String fileContent =  
						"module my.mod{\n" +
						 "	exports p.q.r;" +
						 "	exports a.b.c;\n" +
						 "}";
				createFile(	"/Java9Elements/src/module-info.java",	fileContent);

				createFolder("/Java9Elements/src/p/q/r");
				createFolder("/Java9Elements/src/a/b/c");
				createFile(	"/Java9Elements/src/p/q/package-info.java",	
						"/** Javadoc for package p.q */"
						+ "package p.q;");
				createFile(	"/Java9Elements/src/a/b/c/package-info.java",	
						"/** Javadoc for package a.b.c */"
						+ "package a.b.c;");

				ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
				
				int start = fileContent.indexOf("p.q");
				IJavaElement[] elements = unit.codeSelect(start, 3);
				assertEquals("Incorrect no of elements", 1, elements.length);
				assertEquals("Incorrect element type", IJavaElement.PACKAGE_FRAGMENT, elements[0].getElementType());
				assertElementEquals("Incorrect Java element", "p.q [in src [in Java9Elements]]", elements[0]);

				start = fileContent.indexOf("a.b.c");
				elements = unit.codeSelect(start, 5);
				assertEquals("Incorrect no of elements", 1, elements.length);
				assertEquals("Incorrect element type", IJavaElement.PACKAGE_FRAGMENT, elements[0].getElementType());
				assertElementEquals("Incorrect Java element", "a.b.c [in src [in Java9Elements]]", elements[0]);
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void test004() throws Exception {
		try {
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.9");
			project.open(null);
				String fileContent =  
						"module my.mod{\n" +
						 "	provides com.socket.spi.NetworkSocketProvider\n" +
						 "      with org.fastsocket.FastNetworkSocketProvider;\n" +
						 "}";
				createFile(	"/Java9Elements/src/module-info.java",	fileContent);

				ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
				IModuleDescription mod = unit.getModule();
				assertNotNull("Module should not be null", mod);
				IPackageExport[] exportedPackages = mod.getExportedPackages();
				assertNotNull("should not be null", exportedPackages);
				assertEquals("Incorrect no of exports", 0, exportedPackages.length);
				IModuleReference[] requiredModules = mod.getRequiredModules();
				assertNotNull("should not be null", requiredModules);
				assertEquals("Incorrect no of required modules", 0, requiredModules.length);
				IProvidedService[] providedServices = mod.getProvidedServices();
				assertNotNull("should not be null", providedServices);
				assertEquals("Incorrect no of services", 1, providedServices.length);
				IProvidedService service = providedServices[0];
				assertEquals("Incorrect value", "com.socket.spi.NetworkSocketProvider", service.getServiceName());
				assertEquals("Incorrect value", "org.fastsocket.FastNetworkSocketProvider", service.getImplementationName());
				String[] usedServices = mod.getUsedServices();
				assertNotNull("should not be null", usedServices);
				assertEquals("Incorrect no of required modules", 0, usedServices.length);
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void test005() throws Exception {
		try {
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.9");
			project.open(null);
				String fileContent =  
						"module my.mod{\n" +
						 "	uses com.socket.spi.NetworkSocketProvider;\n" +
						 "}";
				createFile(	"/Java9Elements/src/module-info.java",	fileContent);

				ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
				IModuleDescription mod = unit.getModule();
				assertNotNull("Module should not be null", mod);
				IPackageExport[] exportedPackages = mod.getExportedPackages();
				assertNotNull("should not be null", exportedPackages);
				assertEquals("Incorrect no of exports", 0, exportedPackages.length);
				IModuleReference[] requiredModules = mod.getRequiredModules();
				assertNotNull("should not be null", requiredModules);
				assertEquals("Incorrect no of required modules", 0, requiredModules.length);
				IProvidedService[] providedServices = mod.getProvidedServices();
				assertNotNull("should not be null", providedServices);
				assertEquals("Incorrect no of services", 0, providedServices.length);
				String[] usedServices = mod.getUsedServices();
				assertNotNull("should not be null", usedServices);
				assertEquals("Incorrect no of services", 1, usedServices.length);
				assertEquals("incorrect value", "com.socket.spi.NetworkSocketProvider", usedServices[0]);
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void test006() throws Exception {
		try {
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.9");
			project.open(null);
			String fileContent =  
					"module my.mod{\n" +
					"	exports p.q.r;" +
					"	exports a.b.c;\n" +
					"	requires java.sql;\n" +
					"	requires public java.desktop;\n" +
					"}";
			createFile(	"/Java9Elements/src/module-info.java",	fileContent);

			project = createJavaProject("Java9Elements2", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.9");
			project.open(null);
			fileContent =  "module your.mod{\n" +
					"	requires my.mod;\n" +
					"	requires public java.desktop;\n" +
					"}";
			createFile(	"/Java9Elements2/src/module-info.java",	fileContent);

			ICompilationUnit unit = getCompilationUnit("/Java9Elements2/src/module-info.java");
			int start = fileContent.indexOf("y.mod");
			IJavaElement[] elements = unit.codeSelect(start, 0);
			assertEquals("Incorrect no of elements", 1, elements.length);
			assertEquals("Incorrect element type", IJavaElement.JAVA_MODULE, elements[0].getElementType());
			assertEquals("incorrect element name", "my.mod", elements[0].getElementName());
		}
		finally {
			deleteProject("Java9Elements");
			deleteProject("Java9Elements2");
		}
	}
	public void test007() throws Exception {
		try {
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.9");
			project.open(null);
			String fileContent =  
					"module my.mod{\n" +
					"	exports p.q.r;" +
					"	exports a.b.c;\n" +
					"}";
			createFile(	"/Java9Elements/src/module-info.java",	fileContent);

			project = createJavaProject("Java9Elements2", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.9");
			project.open(null);
			fileContent =  
					"module your.mod{\n" +
					"	requires my.mod;\n" +
					"}";
			createFile(	"/Java9Elements2/src/module-info.java",	fileContent);

			ICompilationUnit unit = getCompilationUnit("/Java9Elements2/src/module-info.java");
			int start = fileContent.lastIndexOf(".mod");
			IJavaElement[] elements = unit.codeSelect(start, 0);
			assertEquals("Incorrect no of elements", 1, elements.length);
			assertEquals("Incorrect element type", IJavaElement.JAVA_MODULE, elements[0].getElementType());
			assertEquals("incorrect element name", "my.mod", elements[0].getElementName());
		}
		finally {
			deleteProject("Java9Elements");
			deleteProject("Java9Elements2");
		}	
	}
	public void test008() throws Exception {
		try {
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.9");
			project.open(null);
			String fileContent =  "module my.mod {\n" +
					"	exports p.q.r to your.mod;" +
					"}";
			createFolder("/Java9Elements/src/p/q/r");
			createFile(	"/Java9Elements/src/module-info.java",	fileContent);
			int start = fileContent.indexOf("your.mod");

			project = createJavaProject("Java9Elements2", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.9");
			project.open(null);
			fileContent =  "module your.mod{\n" +
					"	requires my.mod;\n" +
					"}";
			createFile(	"/Java9Elements2/src/module-info.java",	fileContent);

			ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");

			IJavaElement[] elements = unit.codeSelect(start, 0);
			assertEquals("Incorrect no of elements", 1, elements.length);
			assertEquals("Incorrect element type", IJavaElement.JAVA_MODULE, elements[0].getElementType());
			assertEquals("incorrect element name", "your.mod", elements[0].getElementName());
		}
		finally {
			deleteProject("Java9Elements");
			deleteProject("Java9Elements2");
		}	
	}
	public void test009() throws Exception {
		try {
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.9");
			project.open(null);
			String fileContent =  "module my.mod {\n" +
					"	exports p.q.r;" +
					"}";
			createFolder("/Java9Elements/src/p/q/r");
			createFile("/Java9Elements/src/package-info.java",
					"package p.q.r;");
			createFile("/Java9Elements/src/module-info.java",	fileContent);
			int start = fileContent.indexOf("r;");

			ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");

			IJavaElement[] elements = unit.codeSelect(start, 0);
			assertEquals("Incorrect no of elements", 1, elements.length);
			assertEquals("Incorrect element type", IJavaElement.PACKAGE_FRAGMENT, elements[0].getElementType());
			assertEquals("incorrect element name", "p.q.r", elements[0].getElementName());
		}
		finally {
			deleteProject("Java9Elements");
		}	
	}
}
