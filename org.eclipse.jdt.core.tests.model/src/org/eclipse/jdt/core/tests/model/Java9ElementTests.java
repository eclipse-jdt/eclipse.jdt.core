/*******************************************************************************
 * Copyright (c) 2017, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.provisional.JavaModelAccess;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.IAttributeNamesConstants;
import org.eclipse.jdt.core.util.IClassFileAttribute;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.IModuleMainClassAttribute;
import org.eclipse.jdt.core.util.IModulePackagesAttribute;
import org.eclipse.jdt.internal.compiler.env.IModule.IPackageExport;
import org.eclipse.jdt.internal.core.BinaryModule;

import junit.framework.Test;

import static org.eclipse.jdt.core.IJavaElement.*;

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
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
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
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
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
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
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
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
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
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
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
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
			project.open(null);
			String fileContent =  
					"module my.mod{\n" +
					"	exports p.q.r;" +
					"	exports a.b.c;\n" +
					"	requires java.sql;\n" +
					"	requires transitive java.desktop;\n" +
					"}";
			createFile(	"/Java9Elements/src/module-info.java",	fileContent);

			project = createJavaProject("Java9Elements2", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
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
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
			project.open(null);
			String fileContent =  
					"module my.mod{\n" +
					"	exports p.q.r;" +
					"	exports a.b.c;\n" +
					"}";
			createFile(	"/Java9Elements/src/module-info.java",	fileContent);

			project = createJavaProject("Java9Elements2", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
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
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
			project.open(null);
			String fileContent =  "module my.mod {\n" +
					"	exports p.q.r to your.mod;" +
					"}";
			createFolder("/Java9Elements/src/p/q/r");
			createFile(	"/Java9Elements/src/module-info.java",	fileContent);
			int start = fileContent.indexOf("your.mod");

			project = createJavaProject("Java9Elements2", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
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
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
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
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
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
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
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
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
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
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
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
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
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
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
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
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
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
			assertTrue("incorrect id", id.matches("=Java9Elements/.*"+Pattern.quote("\\/jmods\\/java.base.jmod<'`java.base")));
			IJavaElement element = JavaCore.create(id);
			assertEquals("incorrect element type", IJavaElement.JAVA_MODULE, element.getElementType());
			assertEquals("incorrect module name", "java.base", element.getElementName());
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void test_binary_module_bug520651() throws Exception {
		try {
			IJavaProject project = createJavaProject("Java9Elements", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
			project.open(null);
			ITypeRoot classFile = null;
			IModuleDescription moduleDescription = null;
			for (IPackageFragmentRoot root : project.getAllPackageFragmentRoots()) {
				moduleDescription = root.getModuleDescription();
				if (moduleDescription != null && moduleDescription.getElementName().equals("java.base")) {
					try {
						classFile = root.getPackageFragment("").getOrdinaryClassFile("module-info.class");
						fail("getOrdinaryClassFile() should not answer module-info.class");
					} catch (IllegalArgumentException iae) {
						// expected
					}
					classFile = root.getPackageFragment("").getModularClassFile();
					break;
				}
			}
			assertNotNull("classfile should not be null", classFile);
			assertEquals("same module", moduleDescription, classFile.getModule());
			IJavaElement[] children = classFile.getChildren();
			assertEquals("number of children", 1, children.length);
			IJavaElement child = children[0];
			assertTrue("type of child", child instanceof BinaryModule);
			assertEquals("module name", "java.base", child.getElementName());
			BinaryModule mod = (BinaryModule) child;
			assertEquals("# mod children", 0, mod.getChildren().length);
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void test_module_in_classfolder_bug520651() throws Exception {
		try {
			IJavaProject libPrj= createJavaProject("Java9Lib", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
			createFile("Java9Lib/src/module-info.java", "module java9.lib {}\n");
			libPrj.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			
			IJavaProject project = createJavaProject("Java9Elements",
					new String[] {"src"},
					new String[] {"JCL19_LIB", "/Java9Lib/bin"},
					"bin", "9");
			project.open(null);
			IModuleDescription moduleDescription = null;
			for (IPackageFragmentRoot root : project.getAllPackageFragmentRoots()) {
				if (root.getPath().toString().equals("/Java9Lib/bin")) {
					moduleDescription = root.getModuleDescription();
					assertEquals("module name", "java9.lib", moduleDescription.getElementName());
					return;
				}
			}
			fail("class folder not found");
		}
		finally {
			deleteProject("Java9Lib");
			deleteProject("Java9Elements");
		}
	}
	public void testFindModule1() throws CoreException, IOException {
		try {
			createJavaProject("mod.zero", new String[]{"src"}, null, "bin", JavaCore.VERSION_9);
			createFolder("/mod.zero/src/test0");
			createFile("/mod.zero/src/test0/Test.java",
				"package test0;\n" +
				"\n" +
				"public class Test {}");
			createFile("/mod.zero/src/module-info.java",
				"module mod.zero {\n" +
				"	exports test0;\n" +
				"}\n");

			IJavaProject javaProject = createJavaProject("Test", new String[]{"src"}, null, new String[] {"/mod.zero"}, "bin", JavaCore.VERSION_9);
			createFolder("/Test/src/test1");
			createFile("/Test/src/test1/Test.java",
				"package test1;\n" +
				"\n" +
				"public class Test {}");
			createFile("/Test/src/module-info.java",
				"module test {\n" +
				"	requires mod.one;\n" +
				"	exports test1;\n" +
				"}\n");

			String modOneSrc = 
				"\n" +
				"/** The no. one module. */\n" +
				"module mod.one {\n" +
				"  exports m.o.p;\n" +
				"}\n";
			String[] pathAndContents = new String[] {
				"module-info.java",
				modOneSrc,
				"m/o/p/C.java",
				"package m.o.p;\n" +
				"public class C {\n" +
				"}"
			};
			addLibrary(javaProject, "mod.one.jar", "mod.onesrc.zip", pathAndContents, JavaCore.VERSION_9);

			// search self module:
			IModuleDescription modTest = javaProject.findModule("test", null);
			assertNotNull("module", modTest);
			assertEquals("module name", "test", modTest.getElementName());
			IJavaElement root = parentChain(modTest, new int[] { COMPILATION_UNIT, PACKAGE_FRAGMENT, PACKAGE_FRAGMENT_ROOT });
			String rootPath = ((IPackageFragmentRoot) root).getPath().toString();
			assertEquals("package fragment root path", "/Test/src", rootPath);

			// search source module in project dependency:
			IModuleDescription modZero = javaProject.findModule("mod.zero", null);
			assertNotNull("module", modZero);
			assertEquals("module name", "mod.zero", modZero.getElementName());
			root = parentChain(modZero, new int[] { COMPILATION_UNIT, PACKAGE_FRAGMENT, PACKAGE_FRAGMENT_ROOT });
			rootPath = ((IPackageFragmentRoot) root).getPath().toString();
			assertEquals("package fragment root path", "/mod.zero/src", rootPath);

			// search binary module in jar dependency:
			IModuleDescription modOne = javaProject.findModule("mod.one", null);
			assertNotNull("module", modOne);
			assertEquals("module name", "mod.one", modOne.getElementName());
			root = parentChain(modOne, new int[] { CLASS_FILE, PACKAGE_FRAGMENT, PACKAGE_FRAGMENT_ROOT });
			rootPath = ((IPackageFragmentRoot) root).getPath().toString();
			assertEquals("package fragment root path", "/Test/mod.one.jar", rootPath);

			IModuleDescription notSuchModule = javaProject.findModule("does.not.exist", null);
			assertNull("inexistent module", notSuchModule);
		} finally {
			deleteProject("Test");
			deleteProject("mod.zero");
		}
	}

	private IJavaElement parentChain(IJavaElement element, int[] elementTypes) {
		IJavaElement current = element;
		for (int i = 0; i < elementTypes.length; i++) {
			current = current.getParent();
			assertEquals("Parent type at level "+i, elementTypes[i], current.getElementType());
		}
		return current;
	}
	/*
	 * Test finding module elements with similarly named types in the environment
	 */
	public void testBug521287a() throws CoreException, IOException {
		try {
			createJavaProject("mod.zero", new String[]{"src"}, null, "bin", JavaCore.VERSION_9);
			createFolder("/mod.zero/src/test0");
			createFile("/mod.zero/src/test0/ABCD.java",
				"package test0;\n" +
				"\n" +
				"public class ABCD {}");
			createFile("/mod.zero/src/module-info.java",
				"module ABCD {\n" +
				"	exports test0 to PQRS;\n" +
				"}\n");

			createJavaProject("Test", new String[]{"src"}, null, new String[] {"/mod.zero"}, "bin", JavaCore.VERSION_9);
			createFolder("/Test/src/test1");
			createFile("/Test/src/test1/Test.java",
				"package test1;\n" +
				"\n" +
				"public class Test {}");
			String content = "module PQRS {\n" +
								"	exports test1;\n" +
								"	requires ABCD;\n" +
								"}\n";
			createFile("/Test/src/module-info.java",
				content);

			ICompilationUnit unit = getCompilationUnit("/Test/src/module-info.java");
			
			int start = content.indexOf("ABCD");
			IJavaElement[] elements = unit.codeSelect(start, 4);
			assertEquals("Incorrect no of elements", 1, elements.length);
			assertEquals("Incorrect element type", IJavaElement.JAVA_MODULE, elements[0].getElementType());
			assertElementEquals("Incorrect Java element", 
					"ABCD [in module-info.java [in <default> [in src [in mod.zero]]]]", elements[0]);
	
		} finally {
			deleteProject("Test");
			deleteProject("mod.zero");
		}
	}
	/*
	 * Test finding module elements with similarly named types in the environment
	 */
	public void testBug521287b() throws CoreException, IOException {
		try {
			createJavaProject("mod.zero", new String[]{"src"}, null, "bin", JavaCore.VERSION_9);
			createFolder("/mod.zero/src/test0");
			createFile("/mod.zero/src/test0/PQRS.java",
							"package test0;\n" +
							"\n" +
							"public class PQRS {}");
			String content = 	"module ABCD {\n" +
								"	exports test0 to PQRS;\n" +
								"}\n";
			createFile("/mod.zero/src/module-info.java",
				content);

			createJavaProject("Test", new String[]{"src"}, null, new String[] {"/mod.zero"}, "bin", JavaCore.VERSION_9);
			createFolder("/Test/src/test1");
			createFile("/Test/src/test1/Test.java",
				"package test1;\n" +
				"\n" +
				"public class Test {}");
			createFile("/Test/src/module-info.java",
							"module PQRS {\n" +
							"	exports test1;\n" +
							"	requires ABCD;\n" +
							"}\n");

			ICompilationUnit unit = getCompilationUnit("/mod.zero/src/module-info.java");
			
			int start = content.indexOf("PQRS");
			IJavaElement[] elements = unit.codeSelect(start, 4);
			assertEquals("Incorrect no of elements", 1, elements.length);
			assertEquals("Incorrect element type", IJavaElement.JAVA_MODULE, elements[0].getElementType());
			assertElementEquals("Incorrect Java element", 
					"PQRS [in module-info.java [in <default> [in src [in Test]]]]", elements[0]);
	
		} finally {
			deleteProject("Test");
			deleteProject("mod.zero");
		}
	}

	// using classpath attribute
	public void testModuleAttributes1() throws Exception {
		try {
			IJavaProject javaProject = createJava9Project("mod.zero");
			IClasspathAttribute[] cpMainAttribute = {JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE_MAIN_CLASS, "test0.PQRS")};
			IClasspathEntry src2 = JavaCore.newSourceEntry(new Path("/mod.zero/src2"), null, null, new Path("/mod.zero/bin"), cpMainAttribute);
			addClasspathEntry(javaProject, src2);
			createFolder("/mod.zero/src/test0");
			createFile("/mod.zero/src/test0/PQRS.java",
							"package test0;\n" +
							"\n" +
							"public class PQRS {}");
			createFolder("/mod.zero/src/test1");
			String content = 	"module mod.zero {\n" +
								"	exports test0;\n" +
								"}\n";
			createFolder("/mod.zero/src2");
			createFile("/mod.zero/src2/module-info.java", content);

			javaProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

			String classFile = javaProject.getProject().getLocation().toString()+"/bin/module-info.class";
			IClassFileReader cfr = ToolFactory.createDefaultClassFileReader(classFile, IClassFileReader.ALL);
			assertNotNull("Error reading class bytes", cfr);
			IClassFileAttribute attr = Arrays.stream(cfr.getAttributes())
					.filter(e -> new String(e.getAttributeName()).equals("ModuleMainClass"))
					.findFirst()
					.orElse(null);
			assertNotNull("ModuleMainClass attribute not found", attr);
			IModuleMainClassAttribute mainAttribute = (IModuleMainClassAttribute) attr;
			assertEquals("main attribute value", "test0/PQRS", String.valueOf(mainAttribute.getMainClassName()));

		} finally {
			deleteProject("mod.zero");
		}
	}

	// using dedicated API
	public void testModuleAttributes2() throws Exception {
		try {
			IJavaProject javaProject = createJava9Project("mod.zero");

			createFolder("/mod.zero/src/test0");
			createFile("/mod.zero/src/test0/SPQR.java",
							"package test0;\n" +
							"\n" +
							"public class SPQR {}");

			createFolder("/mod.zero/src/test1");
			createFile("/mod.zero/src/test1/Service.java",
							"package test1;\n" +
							"\n" +
							"public interface Service {}");

			createFolder("/mod.zero/src/test2");
			createFile("/mod.zero/src/test2/Impl.java",
							"package test2;\n" +
							"\n" +
							"public class Impl implements test1.Service {}");

			createFolder("/mod.zero/src/testDont");
			createFile("/mod.zero/src/testDont/Show.java",
							"package testDont;\n" +
							"\n" +
							"public class Show {}");

			String content = 	"module mod.zero {\n" +
								"	exports test0;\n" +
								"	opens test1;\n" +
								"	provides test1.Service with test2.Impl;\n" +
								"}\n";
			createFile("/mod.zero/src/module-info.java", content);

			javaProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

			ICompilationUnit unit = getCompilationUnit("/mod.zero/src/module-info.java");
			IModuleDescription module = unit.getModule();

			Map<String,String> attributes = new HashMap<>();
			attributes.put(String.valueOf(IAttributeNamesConstants.MODULE_MAIN_CLASS), "test0.SPQR");
			attributes.put(String.valueOf(IAttributeNamesConstants.MODULE_PACKAGES), "");

			byte[] bytes = JavaCore.compileWithAttributes(module, attributes);

			InputStream byteStream = new ByteArrayInputStream(bytes);
			IClassFileReader cfr = ToolFactory.createDefaultClassFileReader(byteStream, IClassFileReader.ALL);
			assertNotNull("Error reading class bytes", cfr);
			IClassFileAttribute attr = Arrays.stream(cfr.getAttributes())
					.filter(e -> new String(e.getAttributeName()).equals("ModuleMainClass"))
					.findFirst()
					.orElse(null);
			assertNotNull("Module attribute not found", attr);

			assertNotNull("main attribute", attr);
			IModuleMainClassAttribute mainAttribute = (IModuleMainClassAttribute) attr;
			assertEquals("main attribute value", "test0/SPQR", String.valueOf(mainAttribute.getMainClassName()));

			attr = Arrays.stream(cfr.getAttributes())
					.filter(e -> new String(e.getAttributeName()).equals("ModulePackages"))
					.findFirst()
					.orElse(null);
			assertNotNull("ModulePackages attribute not found", attr);
			IModulePackagesAttribute packagesAttribute = (IModulePackagesAttribute) attr;
			String[] packageNames = CharOperation.toStrings(packagesAttribute.getPackageNames());
			assertEquals("main attribute value", "test0,test1,test2", String.join(",", packageNames));

			// now include testDont in ModulePackages:
			attributes.put(String.valueOf(IAttributeNamesConstants.MODULE_PACKAGES), "testDont");
			bytes = JavaCore.compileWithAttributes(module, attributes);

			byteStream = new ByteArrayInputStream(bytes);
			cfr = ToolFactory.createDefaultClassFileReader(byteStream, IClassFileReader.ALL);
			assertNotNull("Error reading class bytes", cfr);
			attr = Arrays.stream(cfr.getAttributes())
					.filter(e -> new String(e.getAttributeName()).equals("ModulePackages"))
					.findFirst()
					.orElse(null);
			assertNotNull("ModulePackages attribute not found", attr);
			packagesAttribute = (IModulePackagesAttribute) attr;
			packageNames = CharOperation.toStrings(packagesAttribute.getPackageNames());
			assertEquals("main attribute value", "testDont,test0,test1,test2", String.join(",", packageNames));

		} finally {
			deleteProject("mod.zero");
		}
	}
	public void testModuleAttributes_disassembler_508889_001() throws Exception {
		try {
			IJavaProject javaProject = createJava9Project("mod.zero");

			createFolder("/mod.zero/src/test0");
			createFile("/mod.zero/src/test0/SPQR.java",
							"package test0;\n" +
							"\n" +
							"public class SPQR {}");

			createFolder("/mod.zero/src/test1");
			createFile("/mod.zero/src/test1/Service.java",
							"package test1;\n" +
							"\n" +
							"public interface Service {}");

			createFolder("/mod.zero/src/test2");
			createFile("/mod.zero/src/test2/Impl.java",
							"package test2;\n" +
							"\n" +
							"public class Impl implements test1.Service {}");

			createFolder("/mod.zero/src/testDont");
			createFile("/mod.zero/src/testDont/Show.java",
							"package testDont;\n" +
							"\n" +
							"public class Show {}");

			String content = 	"module mod.zero {\n" +
								"	exports test0;\n" +
								"	opens test1;\n" +
								"	provides test1.Service with test2.Impl;\n" +
								"}\n";
			createFile("/mod.zero/src/module-info.java", content);

			javaProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

			ICompilationUnit unit = getCompilationUnit("/mod.zero/src/module-info.java");
			IModuleDescription module = unit.getModule();

			Map<String,String> attributes = new HashMap<>();
			attributes.put(String.valueOf(IAttributeNamesConstants.MODULE_MAIN_CLASS), "test0.SPQR");
			attributes.put(String.valueOf(IAttributeNamesConstants.MODULE_PACKAGES), "");

			byte[] bytes = JavaCore.compileWithAttributes(module, attributes);

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			String result = disassembler.disassemble(bytes, "\n", ClassFileBytesDisassembler.DETAILED);
			String expectedOutput = "// Compiled from module-info.java (version 9 : 53.0, no super bit)\n" +
					" module mod.zero  {\n" +
					"  // Version: \n" +
					"\n" +
					"  requires java.base;\n" +
					"\n" +
					"  exports test0;\n" +
					"\n" +
					"  opens test1;\n" +
					"\n" +
					"  provides test1.Service with test2.Impl;\n" +
					"  \n" +
					"  Module packages:\n" +
					"    test0\n" +
					"    test1\n" +
					"    test2\n" +
					"\n" +
					"  Module main class:\n" +
					"    test0.SPQR\n" +
					"\n" +
					"}";
			int index = result.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(result, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, result);
			}
		} finally {
			deleteProject("mod.zero");
		}
	}
	public void testAutoModule1() throws Exception {
		try {
			IJavaProject project1 = createJavaProject("my_mod", new String[] {"src"}, new String[] {"JCL19_LIB"}, "bin", "9");
			project1.open(null);
			createFolder("/my_mod/src/p/q");
			createFile("/my_mod/src/p/q/R.java", 
					"package p.q;\n" +
					"public class R {\n" +
					"}");

			IJavaProject project2 = createJava9Project("your.mod", new String[] {"src"});
			IClasspathAttribute[] attrs = { JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true") };
			IClasspathEntry dep = JavaCore.newProjectEntry(project1.getPath(), null, false, attrs, false);
			addClasspathEntry(project2, dep);
			project2.open(null);
			createFile("/your.mod/src/module-info.java",
					"module your.mod{\n" +
					"	requires my.mod;\n" +
					"}");

			IModuleDescription mod1 = JavaModelAccess.getAutomaticModuleDescription(project1);
			assertNotNull("auto module not found via project", mod1);

			IPackageFragmentRoot fragmentRoot = project2.getPackageFragmentRoot(project1.getResource());
			IModuleDescription mod2 = JavaModelAccess.getAutomaticModuleDescription(fragmentRoot);
			assertNotNull("auto module not found via package fragment root", mod2);

			assertEquals("names of module descriptions should be equal", mod1.getElementName(), mod2.getElementName());

			for (IModuleDescription m : new IModuleDescription[] {mod1, mod2}) {
				assertFalse(m.exists()); // exists would imply: included in getParent().getChildren()
				assertTrue(m.getParent().exists());
				assertNull(m.getClassFile());
				assertNull(m.getCompilationUnit());
				assertNull(m.getDeclaringType());
				assertNull(m.getTypeRoot());
				assertEquals(0, m.getChildren().length);
				assertEquals(IJavaElement.JAVA_MODULE, m.getElementType());
				assertEquals(0, m.getFlags());
				assertEquals(m.getParent(), m.getOpenable());
// these throw exceptions, which is OK after exists() answers false:
//				assertNull(m.getCorrespondingResource());
//				assertNull(m.getJavadocRange());
//				assertNull(m.getSourceRange());
			}
			assertEquals(project1, mod1.getParent());
			assertEquals(fragmentRoot, mod2.getParent());
		}
		finally {
			deleteProject("Java9Elements");
			deleteProject("Java9Elements2");
		}	
	}
	public void test526761a() throws Exception {
		try {
			IJavaProject project1 = createJava9Project("Java9Elements", new String[] {"work/src/java"});
			project1.open(null);
			createFolder("/Java9Elements/work/src/java/test");
			String fileContent =
					"package test;\n" +
					"public class Test {}";
			createFile("/Java9Elements/work/src/java/test/Test.java", fileContent);

			ICompilationUnit unit = getCompilationUnit("/Java9Elements/work/src/java/test/Test.java");
			IJavaElement parent = unit.getParent();
			IPackageFragment pkg = (IPackageFragment) parent;
			IPackageFragmentRoot root = (IPackageFragmentRoot) pkg.getParent();
			String id = root.getHandleIdentifier();
			System.out.println(id);
			assertTrue("incorrect id", id.matches("=Java9Elements/work\\\\/src\\\\/java"));
			IJavaElement element = JavaCore.create(id);
			assertEquals("incorrect element type", IJavaElement.PACKAGE_FRAGMENT_ROOT, element.getElementType());
			id = "=Java9Elements/work/src/java";
			IJavaElement element2 = JavaCore.create(id);
			assertEquals("incorrect element type", IJavaElement.PACKAGE_FRAGMENT_ROOT, element2.getElementType());
			assertEquals("roots should be same", element, element2);
		} finally {
			deleteProject("Java9Elements");
		}	
	}
	public void test528058() throws Exception {
		try {
			IJavaProject project1 = createJava9Project("Java9Elements", new String[] {"work/src/java"});
			project1.open(null);
			IJavaElement object = project1.findElement(new Path("java/lang/Object.class"));
			String id = object.getHandleIdentifier();
			IJavaElement object2 = JavaCore.create(id);
			assertEquals("elements should be the same", object, object2);
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	private IJavaProject createJavaProjectWithBaseSql() throws CoreException {
		IJavaProject project1 = createJava9Project("Java9Elements", new String[] {"src"});
		project1.open(null);
		IClasspathEntry[] rawClasspath = project1.getRawClasspath();
		IClasspathEntry[] newClasspath = new IClasspathEntry[rawClasspath.length + 1];
		for (int i = 0; i < rawClasspath.length; i++) {
			IPath path = rawClasspath[i].getPath();
			if (path.lastSegment().equals("jrt-fs.jar")) {
				path = path.removeLastSegments(2).append("jmods").append("java.base.jmod");
				IClasspathEntry newEntry = JavaCore.newLibraryEntry(path, rawClasspath[i].getSourceAttachmentPath(), new Path("java.base"));
				newClasspath[i] = newEntry;
				path = path.removeLastSegments(2).append("jmods").append("java.sql.jmod");
				newEntry = JavaCore.newLibraryEntry(path, rawClasspath[i].getSourceAttachmentPath(), new Path("java.sql"));
				newClasspath[rawClasspath.length] = newEntry;
			} else {
				newClasspath[i] = rawClasspath[i];
			}
		}
		project1.setRawClasspath(rawClasspath, null);
		return project1;
	}
	public void test526326a() throws Exception {
		try {
			IJavaProject project = createJavaProjectWithBaseSql();
			project.open(null);
				String fileContent = 
						"import java.sql.Driver;\n" +
						"import p.q.Main;\n" +
						"module my.mod{\n"  +
						 "	exports p.q;" +
						 "	requires java.sql;\n" +
						 "	provides Driver with Main;\n" +
						 "}";
				createFile(	"/Java9Elements/src/module-info.java",	fileContent);
				createFolder("/Java9Elements/src/p/q");
				createFile("/Java9Elements/src/p/q/Main.java",
						"package p.q;\n" +
						"import java.sql.Connection;\n" +
						"import java.sql.Driver;\n" +
						"import java.sql.DriverPropertyInfo;\n" +
						"import java.sql.SQLException;\n" +
						"import java.sql.SQLFeatureNotSupportedException;\n" +
						"import java.util.Properties;\n" +
						"import java.util.logging.Logger;\n" +
						"public class Main implements Driver {\n" +
						"	public boolean acceptsURL(String arg0) throws SQLException { return false; }\n" +
						"	public Connection connect(String arg0, Properties arg1) throws SQLException { return null; }\n" +
						"	public int getMajorVersion() { return 0; }\n" +
						"	public int getMinorVersion() { return 0;}\n" +
						"	public Logger getParentLogger() throws SQLFeatureNotSupportedException { return null; }\n" +
						"	public DriverPropertyInfo[] getPropertyInfo(String arg0, Properties arg1) throws SQLException { return null; }\n" +
						"	public boolean jdbcCompliant() { return false; }\n" +
						"}");
				ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
				int start = fileContent.lastIndexOf("Driver");
				IJavaElement[] elements = unit.codeSelect(start, "Driver".length());
				assertEquals("Incorrect no of elements", 1, elements.length);
				assertEquals("Incorrect element type", IJavaElement.TYPE, elements[0].getElementType());
				assertElementEquals("Incorrect Java element", 
						"Driver [in Driver.class [in java.sql [in <module:java.sql>]]]", elements[0]);
				start = fileContent.lastIndexOf("Main");
				elements = unit.codeSelect(start, "Main".length());
				assertEquals("Incorrect no of elements", 1, elements.length);
				assertEquals("Incorrect element type", IJavaElement.TYPE, elements[0].getElementType());
				assertElementEquals("Incorrect Java element", 
						"Main [in Main.java [in p.q [in src [in Java9Elements]]]]", elements[0]);
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void test526326b() throws Exception {
		try {
			IJavaProject project = createJavaProjectWithBaseSql();
			project.open(null);
				String fileContent = 
						"import java.sql.Driver;\n" +
						"import p.q.Main;\n" +
						"module my.mod{\n"  +
						 "	exports p.q;" +
						 "	requires java.sql;\n" +
						 "	provides java.sql.Driver with p.q.Main;\n" +
						 "}";
				createFile(	"/Java9Elements/src/module-info.java",	fileContent);
				createFolder("/Java9Elements/src/p/q");
				createFile("/Java9Elements/src/p/q/Main.java",
						"package p.q;\n" +
						"import java.sql.Connection;\n" +
						"import java.sql.Driver;\n" +
						"import java.sql.DriverPropertyInfo;\n" +
						"import java.sql.SQLException;\n" +
						"import java.sql.SQLFeatureNotSupportedException;\n" +
						"import java.util.Properties;\n" +
						"import java.util.logging.Logger;\n" +
						"public class Main implements Driver {\n" +
						"	public boolean acceptsURL(String arg0) throws SQLException { return false; }\n" +
						"	public Connection connect(String arg0, Properties arg1) throws SQLException { return null; }\n" +
						"	public int getMajorVersion() { return 0; }\n" +
						"	public int getMinorVersion() { return 0;}\n" +
						"	public Logger getParentLogger() throws SQLFeatureNotSupportedException { return null; }\n" +
						"	public DriverPropertyInfo[] getPropertyInfo(String arg0, Properties arg1) throws SQLException { return null; }\n" +
						"	public boolean jdbcCompliant() { return false; }\n" +
						"}");
				ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
				int start = fileContent.lastIndexOf("Driver");
				IJavaElement[] elements = unit.codeSelect(start, "Driver".length());
				assertEquals("Incorrect no of elements", 1, elements.length);
				assertEquals("Incorrect element type", IJavaElement.TYPE, elements[0].getElementType());
				assertElementEquals("Incorrect Java element", 
						"Driver [in Driver.class [in java.sql [in <module:java.sql>]]]", elements[0]);
				start = fileContent.lastIndexOf("Main");
				elements = unit.codeSelect(start, "Main".length());
				assertEquals("Incorrect no of elements", 1, elements.length);
				assertEquals("Incorrect element type", IJavaElement.TYPE, elements[0].getElementType());
				assertElementEquals("Incorrect Java element", 
						"Main [in Main.java [in p.q [in src [in Java9Elements]]]]", elements[0]);
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
	public void test526326c() throws Exception {
		try {
			IJavaProject project = createJavaProjectWithBaseSql();
			project.open(null);
				String fileContent = 
						"module my.mod{\n"  +
						 "	exports p.q;" +
						 "	requires java.sql;\n" +
						 "	provides java.sql.Driver with p.q.Main;\n" +
						 "}";
				createFile(	"/Java9Elements/src/module-info.java",	fileContent);
				createFolder("/Java9Elements/src/p/q");
				createFile("/Java9Elements/src/p/q/Main.java",
						"package p.q;\n" +
						"import java.sql.Connection;\n" +
						"import java.sql.Driver;\n" +
						"import java.sql.DriverPropertyInfo;\n" +
						"import java.sql.SQLException;\n" +
						"import java.sql.SQLFeatureNotSupportedException;\n" +
						"import java.util.Properties;\n" +
						"import java.util.logging.Logger;\n" +
						"public class Main implements Driver {\n" +
						"	public boolean acceptsURL(String arg0) throws SQLException { return false; }\n" +
						"	public Connection connect(String arg0, Properties arg1) throws SQLException { return null; }\n" +
						"	public int getMajorVersion() { return 0; }\n" +
						"	public int getMinorVersion() { return 0;}\n" +
						"	public Logger getParentLogger() throws SQLFeatureNotSupportedException { return null; }\n" +
						"	public DriverPropertyInfo[] getPropertyInfo(String arg0, Properties arg1) throws SQLException { return null; }\n" +
						"	public boolean jdbcCompliant() { return false; }\n" +
						"}");
				ICompilationUnit unit = getCompilationUnit("/Java9Elements/src/module-info.java");
				int start = fileContent.lastIndexOf("Driver");
				IJavaElement[] elements = unit.codeSelect(start, "Driver".length());
				assertEquals("Incorrect no of elements", 1, elements.length);
				assertEquals("Incorrect element type", IJavaElement.TYPE, elements[0].getElementType());
				assertElementEquals("Incorrect Java element", 
						"Driver [in Driver.class [in java.sql [in <module:java.sql>]]]", elements[0]);
				start = fileContent.lastIndexOf("Main");
				elements = unit.codeSelect(start, "Main".length());
				assertEquals("Incorrect no of elements", 1, elements.length);
				assertEquals("Incorrect element type", IJavaElement.TYPE, elements[0].getElementType());
				assertElementEquals("Incorrect Java element", 
						"Main [in Main.java [in p.q [in src [in Java9Elements]]]]", elements[0]);
		}
		finally {
			deleteProject("Java9Elements");
		}
	}
}
