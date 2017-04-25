/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.internal.compiler.env.IModule.IPackageExport;
import org.eclipse.jdt.internal.core.BinaryModule;

import junit.framework.Test;

public class Java9ElementTests extends AbstractJavaModelTests {

	static {
//		TESTS_NAMES = new String[] {"testBug510339_002"};
	}

	public Java9ElementTests(String name) {
		super(name);
	}
	public static Test suite() {
		return buildModelTestSuite(AbstractCompilerTest.F_9, Java9ElementTests.class);
	}
	public void test001() throws Exception {
		try {
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "1.9");
			project.open(null);
				String fileContent =  "module my.mod{}\n";
				createFile(	"/Java9Elements/src/module-info.java",	fileContent);

				ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
				IModuleDescription mod = unit.getModule();
				assertNotNull("Module should not be null", mod);
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void test002() throws Exception {
		try {
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "1.9");
			project.open(null);
				String fileContent =  "module my.mod{\n" +
						 "	exports p.q.r;" +
						 "	exports a.b.c;\n" +
						 "	requires java.sql;\n" +
						 "	requires transitive java.desktop;\n" +
						 "}";
				createFile(	"/Java9Elements/src/module-info.java",	fileContent);

				ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
				IModuleDescription mod = unit.getModule();
				assertNotNull("Module should not be null", mod);
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void test003() throws Exception {
		try {
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "1.9");
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
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "1.9");
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
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void test005() throws Exception {
		try {
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "1.9");
			project.open(null);
				String fileContent =  
						"module my.mod{\n" +
						 "	uses com.socket.spi.NetworkSocketProvider;\n" +
						 "}";
				createFile(	"/Java9Elements/src/module-info.java",	fileContent);

				ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
				IModuleDescription mod = unit.getModule();
				assertNotNull("Module should not be null", mod);
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void test006() throws Exception {
		try {
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "1.9");
			project.open(null);
			String fileContent =  
					"module my.mod{\n" +
					"	exports p.q.r;" +
					"	exports a.b.c;\n" +
					"	requires java.sql;\n" +
					"	requires transitive java.desktop;\n" +
					"}";
			createFile(	"/Java9Elements/src/module-info.java",	fileContent);

			project = createJavaProject("Java9Elements2", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "1.9");
			project.open(null);
			fileContent =  "module your.mod{\n" +
					"	requires my.mod;\n" +
					"	requires transitive java.desktop;\n" +
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
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "1.9");
			project.open(null);
			String fileContent =  
					"module my.mod{\n" +
					"	exports p.q.r;" +
					"	exports a.b.c;\n" +
					"}";
			createFile(	"/Java9Elements/src/module-info.java",	fileContent);

			project = createJavaProject("Java9Elements2", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "1.9");
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
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "1.9");
			project.open(null);
			String fileContent =  "module my.mod {\n" +
					"	exports p.q.r to your.mod;" +
					"}";
			createFolder("/Java9Elements/src/p/q/r");
			createFile(	"/Java9Elements/src/module-info.java",	fileContent);
			int start = fileContent.indexOf("your.mod");

			project = createJavaProject("Java9Elements2", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "1.9");
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
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "1.9");
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
	public void test010() throws Exception {
		try {
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "1.9");
			project.open(null);
			String fileContent =  "// A very simple module" +
					 "module my.mod {\n" +
					"	exports p.q.r;" +
					"}";
			createFolder("/Java9Elements/src/p/q/r");
			createFile("/Java9Elements/src/module-info.java",	fileContent);
			int start = fileContent.lastIndexOf("module");

			ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");

			IJavaElement[] elements = unit.codeSelect(start, "module".length());
			assertEquals("Incorrect no of elements", 0, elements.length);
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void test011() throws Exception {
		try {
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "1.9");
			project.open(null);
				String fileContent =  "module my.mod{" +
									"	provides a.b.C with a.b.CImpl, a.b.DImpl;\n" + 
									"	opens a.b;" +
									"}\n";
				createFolder("/Java9Elements/src/a/b");
				createFile("/Java9Elements/src/a/b/C.java",
						"package a.b;\n" + 
						"public interface C {}");
				createFile("/Java9Elements/src/a/b/CImpl.java",
						"package a.b;\n" + 
						"public class CImpl implements C {}");
				createFile(	"/Java9Elements/src/module-info.java",	fileContent);

				ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
				IModuleDescription mod = unit.getModule();
				assertNotNull("Module should not be null", mod);
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void test012() throws Exception {
		try {
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "1.9");
			project.open(null);
				String fileContent =  "module my.mod{" +
									"	provides a.b.C with a.b.CImpl, a.b.DImpl;\n" + 
									"}\n";
				createFolder("/Java9Elements/src/a/b");
				createFile("/Java9Elements/src/a/b/C.java",
						"package a.b;\n" + 
						"public interface C {}");
				createFile("/Java9Elements/src/a/b/CImpl.java",
						"package a.b;\n" + 
						"public class CImpl implements C {}");
				createFile(	"/Java9Elements/src/module-info.java",	fileContent);

				ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
				IModuleDescription mod = unit.getModule();
				assertNotNull("Module should not be null", mod);
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void test013() throws Exception {
		try {
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "1.9");
			project.open(null);
				String fileContent =  "module my.mod{" +
									"	opens a.b to java.base, java.sql;" +
									"}\n";
				createFolder("/Java9Elements/src/a/b");
				createFile(	"/Java9Elements/src/module-info.java",	fileContent);

				ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
				IModuleDescription mod = unit.getModule();
				assertNotNull("Module should not be null", mod);
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void test014() throws Exception {
		try {
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "1.9");
			project.open(null);
				String fileContent =  "module my.mod{" +
									"	exports a.b to java.base, java.sql;" +
									"}\n";
				createFolder("/Java9Elements/src/a/b");
				createFile(	"/Java9Elements/src/module-info.java",	fileContent);

				ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
				IModuleDescription mod = unit.getModule();
				assertNotNull("Module should not be null", mod);
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void testBug510339_001_since_9() throws Exception {
		try {
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "1.9");
			addClasspathEntry(project, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			project.open(null);
			String fileContent =
				"module first {\n" +
				"    exports pack1 to second;\n" +
				"}\n";
			createFile("/Java9Elements/src/module-info.java",	fileContent);
			int start = fileContent.lastIndexOf("pack1");
			createFolder("/Java9Elements/src/pack1");
			createFile("/Java9Elements/src/pack1/X11.java",
					"package pack1;\n" +
					"public class X11 {}\n");

			ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");

			IJavaElement[] elements = unit.codeSelect(start, "pack1".length());
			assertEquals("Incorrect no of elements", 1, elements.length);
			IPackageFragment fragment = (IPackageFragment) elements[0];
			assertEquals("pack1", fragment.getElementName());
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void testBug510339_002_since_9() throws Exception {
		try {

			IJavaProject project1 = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
			project1.open(null);
			addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String fileContent =
				"module first {\n" +
				"    exports pack1 to second;\n" +
				"}\n";
			createFile("/Java9Elements/src/module-info.java",	fileContent);
			String selection = "second";
			int start = fileContent.lastIndexOf(selection);
			createFolder("/Java9Elements/src/pack1");
			createFile("/Java9Elements/src/pack1/X11.java",
					"package pack1;\n" +
					"public class X11 {}\n");

			IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
			project2.open(null);
			addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String secondFile =
					"module second {\n" +
					"    requires first;\n" +
					"}\n";
			createFile("/second/src/module-info.java",	secondFile);

			addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
			project1.close(); // sync
			project2.close();
			project2.open(null);
			project1.open(null);

			ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
			IJavaElement[] elements = unit.codeSelect(start, selection.length());
			assertEquals("Incorrect no of elements", 1, elements.length);
			IModuleDescription ref = (IModuleDescription) elements[0];
			assertEquals("second", ref.getElementName());
		}
		finally {
			deleteProject("Java9Elements");
			deleteProject("second");
		}
	}
	public void testBug510339_003_since_9() throws Exception {
		try {
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "1.9");
			addClasspathEntry(project, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			project.open(null);
			String fileContent =
				"module first {\n" +
				"    opens pack1 to second;\n" +
				"}\n";
			createFile("/Java9Elements/src/module-info.java",	fileContent);
			int start = fileContent.lastIndexOf("pack1");
			createFolder("/Java9Elements/src/pack1");
			createFile("/Java9Elements/src/pack1/X11.java",
					"package pack1;\n" +
					"public class X11 {}\n");

			ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");

			IJavaElement[] elements = unit.codeSelect(start, "pack1".length());
			assertEquals("Incorrect no of elements", 1, elements.length);
			IPackageFragment fragment = (IPackageFragment) elements[0];
			assertEquals("pack1", fragment.getElementName());
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void testBug510339_004_since_9() throws Exception {
		try {

			IJavaProject project1 = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
			project1.open(null);
			addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String fileContent =
				"module first {\n" +
				"    exports pack1 to second;\n" +
				"}\n";
			createFile("/Java9Elements/src/module-info.java",	fileContent);
			createFolder("/Java9Elements/src/pack1");
			createFile("/Java9Elements/src/pack1/X11.java",
					"package pack1;\n" +
					"public class X11 {}\n");

			IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
			project2.open(null);
			addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String secondFile =
					"module second {\n" +
					"    requires first;\n" +
					"}\n";
			createFile("/second/src/module-info.java",	secondFile);

			addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
			project1.close(); // sync
			project2.close();
			project2.open(null);
			project1.open(null);

			ICompilationUnit unit = getCompilationUnit("/second/src/module-info.java");
			String selection = "first";
			int start = secondFile.lastIndexOf(selection);
			IJavaElement[] elements = unit.codeSelect(start, selection.length());
			assertEquals("Incorrect no of elements", 1, elements.length);
			IModuleDescription ref = (IModuleDescription) elements[0];
			assertEquals("first", ref.getElementName());
		}
		finally {
			deleteProject("Java9Elements");
			deleteProject("second");
		}
	}
	public void testBug510339_005_since_9() throws Exception {
		try {

			IJavaProject project1 = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
			project1.open(null);
			addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String fileContent =
				"module first {\n" +
				"    requires second;\n" +
				"    provides pack22.I22 with pack11.X11;\n" +
				"}\n";
			createFile("/Java9Elements/src/module-info.java", fileContent);
			createFolder("/Java9Elements/src/pack11");
			createFile("/Java9Elements/src/pack11/X11.java",
					"package pack11;\n" +
					"public class X11 implements pack22.I22 {}\n");

			IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
			project2.open(null);
			addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String secondFile =
					"module second {\n" +
					"    exports pack22 to first;\n" +
					"}\n";
			createFile("/second/src/module-info.java",	secondFile);
			createFolder("/second/src/pack22");
			createFile("/second/src/pack22/I22.java",
					"package pack22;\n" +
					"public interface I22 {}\n");

			addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
			project1.close(); // sync
			project2.close();
			project2.open(null);
			project1.open(null);

			ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
			String selection = "pack22";
			int start = fileContent.lastIndexOf(selection);
			IJavaElement[] elements = unit.codeSelect(start, selection.length());
			assertEquals("Incorrect no of elements", 1, elements.length);
			IPackageFragment fragment = (IPackageFragment) elements[0];
			assertEquals("pack22", fragment.getElementName());
		}
		finally {
			deleteProject("Java9Elements");
			deleteProject("second");
		}
	}
	public void testBug510339_006_since_9() throws Exception {
		try {

			IJavaProject project1 = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
			project1.open(null);
			addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String fileContent =
				"module first {\n" +
				"    requires second;\n" +
				"    provides pack22.I22 with pack11.X11;\n" +
				"}\n";
			createFile("/Java9Elements/src/module-info.java", fileContent);
			createFolder("/Java9Elements/src/pack11");
			createFile("/Java9Elements/src/pack11/X11.java",
					"package pack11;\n" +
					"public class X11 implements pack22.I22 {}\n");

			IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
			project2.open(null);
			addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String secondFile =
					"module second {\n" +
					"    exports pack22 to first;\n" +
					"}\n";
			createFile("/second/src/module-info.java",	secondFile);
			createFolder("/second/src/pack22");
			createFile("/second/src/pack22/I22.java",
					"package pack22;\n" +
					"public interface I22 {}\n");

			addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
			project1.close(); // sync
			project2.close();
			project2.open(null);
			project1.open(null);

			ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
			String selection = "pack11";
			int start = fileContent.lastIndexOf(selection);
			IJavaElement[] elements = unit.codeSelect(start, selection.length());
			assertEquals("Incorrect no of elements", 1, elements.length);
			IPackageFragment fragment = (IPackageFragment) elements[0];
			assertEquals("pack11", fragment.getElementName());
		}
		finally {
			deleteProject("Java9Elements");
			deleteProject("second");
		}
	}
	public void testBug510339_007_since_9() throws Exception {
		try {

			IJavaProject project1 = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
			project1.open(null);
			addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String fileContent =
				"module first {\n" +
				"    requires second;\n" +
				"    uses pack11.X11;\n" +
				"}\n";
			createFile("/Java9Elements/src/module-info.java", fileContent);
			createFolder("/Java9Elements/src/pack11");
			createFile("/Java9Elements/src/pack11/X11.java",
					"package pack11;\n" +
					"public class X11 implements pack22.I22 {}\n");

			project1.close(); // sync
			project1.open(null);

			ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
			String selection = "pack11";
			int start = fileContent.lastIndexOf(selection);
			IJavaElement[] elements = unit.codeSelect(start, selection.length());
			assertEquals("Incorrect no of elements", 1, elements.length);
			IPackageFragment fragment = (IPackageFragment) elements[0];
			assertEquals("pack11", fragment.getElementName());
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void testBug510339_008_since_9() throws Exception {
		try {

			IJavaProject project1 = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
			project1.open(null);
			addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String fileContent =
				"module first {\n" +
				"    requires second;\n" +
				"    provides pack22.I22 with pack11.X11;\n" +
				"}\n";
			createFile("/Java9Elements/src/module-info.java", fileContent);
			createFolder("/Java9Elements/src/pack11");
			createFile("/Java9Elements/src/pack11/X11.java",
					"package pack11;\n" +
					"public class X11 implements pack22.I22 {}\n");

			IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
			project2.open(null);
			addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String secondFile =
					"module second {\n" +
					"    exports pack22 to first;\n" +
					"}\n";
			createFile("/second/src/module-info.java",	secondFile);
			createFolder("/second/src/pack22");
			createFile("/second/src/pack22/I22.java",
					"package pack22;\n" +
					"public interface I22 {}\n");

			addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
			project1.close(); // sync
			project2.close();
			project2.open(null);
			project1.open(null);

			ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
			String selection = "X11";
			int start = fileContent.lastIndexOf(selection);
			IJavaElement[] elements = unit.codeSelect(start, selection.length());
			assertEquals("Incorrect no of elements", 1, elements.length);
			IType type = (IType) elements[0];
			assertEquals("X11", type.getElementName());
		}
		finally {
			deleteProject("Java9Elements");
			deleteProject("second");
		}
	}
	public void testBug510339_009_since_9() throws Exception {
		try {

			IJavaProject project1 = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
			project1.open(null);
			addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String fileContent =
				"module first {\n" +
				"    requires second;\n" +
				"    uses pack11.X11;\n" +
				"}\n";
			createFile("/Java9Elements/src/module-info.java", fileContent);
			createFolder("/Java9Elements/src/pack11");
			createFile("/Java9Elements/src/pack11/X11.java",
					"package pack11;\n" +
					"public class X11 implements pack22.I22 {}\n");

			project1.close(); // sync
			project1.open(null);

			ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
			String selection = "X11";
			int start = fileContent.lastIndexOf(selection);
			IJavaElement[] elements = unit.codeSelect(start, selection.length());
			assertEquals("Incorrect no of elements", 1, elements.length);
			IType type = (IType) elements[0];
			assertEquals("X11", type.getElementName());
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void testBug510339_010_since_9() throws Exception {
		try {
			IJavaProject project1 = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
			project1.open(null);
			addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String fileContent =
				"module first {\n" +
				"    requires second;\n" +
				"    uses pack11.X11;\n" +
				"}\n";
			createFile("/Java9Elements/src/module-info.java", fileContent);
			createFolder("/Java9Elements/src/pack11");
			createFile("/Java9Elements/src/pack11/X11.java",
					"package pack11;\n" +
					"public class X11 implements pack22.I22 {}\n");

			project1.close(); // sync
			project1.open(null);

			ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
			String selection = "X11";
			int start = fileContent.lastIndexOf(selection);
			IJavaElement[] elements = unit.codeSelect(start, selection.length());
			assertEquals("Incorrect no of elements", 1, elements.length);
			IType type = (IType) elements[0];
			assertEquals("X11", type.getElementName());
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void testSystemLibAsJMod() throws Exception {
		try {
			IJavaProject project1 = createJava9Project("Java9Elements", new String[] {"src"});
			project1.open(null);
			IClasspathEntry[] rawClasspath = project1.getRawClasspath();
			for (int i = 0; i < rawClasspath.length; i++) {
				IPath path = rawClasspath[i].getPath();
				if (path.lastSegment().equals("jrt-fs.jar")) {
					path = path.removeLastSegments(2).append("jmods").append("java.base.jmod");
					IClasspathEntry newEntry = JavaCore.newLibraryEntry(path, rawClasspath[i].getSourceAttachmentPath(), new Path("java.base"));
					rawClasspath[i] = newEntry;
				}
			}
			project1.setRawClasspath(rawClasspath, null);
			String fileContent =
					"module first {\n" +
					"    requires java.base;\n" +
					"    uses pack11.X11;\n" +
					"}\n";
				createFile("/Java9Elements/src/module-info.java", fileContent);

			ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
			String selection = "java.base";
			int start = fileContent.lastIndexOf(selection);
			IJavaElement[] elements = unit.codeSelect(start, selection.length());
			assertEquals("Incorrect no of elements", 1, elements.length);
			assertTrue("Invalid selection result", (elements[0] instanceof BinaryModule));
			BinaryModule mod = (BinaryModule) elements[0];
			IPackageExport[] exportedPackages = mod.getExportedPackages();
			assertNotNull("missing package exports", exportedPackages);
			assertTrue("missing package exports", exportedPackages.length > 0);
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void test515342a() throws Exception {
		try {
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "1.9");
			project.open(null);
				String fileContent =  "module my.mod{}\n";
				createFile(	"/Java9Elements/src/module-info.java",	fileContent);

				ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
				IModuleDescription mod = unit.getModule();
				String id = mod.getHandleIdentifier();
				assertEquals("incorrect id", "=Java9Elements/src<{module-info.java`my.mod", id);
				IJavaElement element = JavaCore.create(id);
				assertEquals("incorrect element type", IJavaElement.JAVA_MODULE, element.getElementType());
				assertEquals("incorrect module name", "my.mod", element.getElementName());
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void test515342b() throws Exception {
		try {
			IJavaProject project1 = createJava9Project("Java9Elements", new String[] {"src"});
			project1.open(null);
			IClasspathEntry[] rawClasspath = project1.getRawClasspath();
			for (int i = 0; i < rawClasspath.length; i++) {
				IPath path = rawClasspath[i].getPath();
				if (path.lastSegment().equals("jrt-fs.jar")) {
					path = path.removeLastSegments(2).append("jmods").append("java.base.jmod");
					IClasspathEntry newEntry = JavaCore.newLibraryEntry(path, rawClasspath[i].getSourceAttachmentPath(), new Path("java.base"));
					rawClasspath[i] = newEntry;
				}
			}
			project1.setRawClasspath(rawClasspath, null);
			String fileContent =
					"module first {\n" +
							"    requires java.base;\n" +
							"    uses pack11.X11;\n" +
							"}\n";
			createFile("/Java9Elements/src/module-info.java", fileContent);

			ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
			String selection = "java.base";
			int start = fileContent.lastIndexOf(selection);
			IJavaElement[] elements = unit.codeSelect(start, selection.length());
			assertEquals("Incorrect no of elements", 1, elements.length);
			assertTrue("Invalid selection result", (elements[0] instanceof BinaryModule));
			IModuleDescription mod = (IModuleDescription) elements[0];
			String id = mod.getHandleIdentifier();
			assertEquals("incorrect id", "=Java9Elements/C:\\/Java\\/jdk-9-ea+160\\/jmods\\/java.base.jmod<(module-info.class`java.base", id);
			IJavaElement element = JavaCore.create(id);
			assertEquals("incorrect element type", IJavaElement.JAVA_MODULE, element.getElementType());
			assertEquals("incorrect module name", "java.base", element.getElementName());
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
}
