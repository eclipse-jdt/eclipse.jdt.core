/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.tests.model;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;

import junit.framework.Test;

public class ResolveTests9 extends AbstractJavaModelTests {
	ICompilationUnit wc = null;

	static {
//		 TESTS_NAMES = new String[] { "testModuleInfo_" };
//		 TESTS_NUMBERS = new int[] { 124 };
//		 TESTS_RANGE = new int[] { 16, -1 };
	}
	public static Test suite() {
		return buildModelTestSuite(ResolveTests9.class);
	}
	public ResolveTests9(String name) {
		super(name);
	}
	public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
		return super.getWorkingCopy(path, source, this.wcOwner);
	}
	public void setUpSuite() throws Exception {
		super.setUpSuite();
	
		System.setProperty("modules.to.load", "java.base;java.desktop;java.rmi;java.sql;");
	
		IJavaProject project = setUpJavaProject("Resolve", "9", true);
	
		String bootModPath = System.getProperty("java.home") + File.separator +"jrt-fs.jar";
		IClasspathEntry jrtEntry = JavaCore.newLibraryEntry(new Path(bootModPath), null, null, null, null, false);
		IClasspathEntry[] old = project.getRawClasspath();
		IClasspathEntry[] newPath = new IClasspathEntry[old.length +1];
		System.arraycopy(old, 0, newPath, 0, old.length);
		newPath[old.length] = jrtEntry;
		project.setRawClasspath(newPath, null);
	
		waitUntilIndexesReady();
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		this.wcOwner = new WorkingCopyOwner(){};
	}
	public void tearDownSuite() throws Exception {
		deleteProject("Resolve");
	
		super.tearDownSuite();
	}
	
	protected void tearDown() throws Exception {
		if (this.wc != null) {
			this.wc.discardWorkingCopy();
		}
		super.tearDown();
	}
	public void testModuleInfo_serviceImplementation_OK() throws CoreException {
		IFile modInfo = null;
		try {
			getWorkingCopy(
					"/Resolve/src/test/ITest.java",
					"public interface ITest {}\n");
			getWorkingCopy(
					"/Resolve/src/test/TestClass.java",
					"public class TestClass implements ITest {}\n");
		
			this.wc = getWorkingCopy(
					"/Resolve/src/module-info.java",
					"module com.test {\n" +
					"  provides p1.Y with ResolveInterface;\n" +
					"}\n");
		
			String str = this.wc.getSource();
			String selection = "ResolveInterface";
			int start = str.indexOf(selection);
			int length = selection.length();
		
			IJavaElement[] elements = this.wc.codeSelect(start, length);
			assertElementsEqual(
				"Unexpected elements",
				"ResolveInterface [in ResolveInterface.java [in <default> [in src [in Resolve]]]]",
				elements
			);
		} finally {
			if (modInfo != null)
				deleteResource(modInfo);
		}
	}
	public void testModuleInfo_serviceInterface_OK() throws CoreException {
		IFile modInfo = null;
		IFolder testFolder = null;
		try {
			testFolder = createFolder("/Resolve/src/test");
			createFile(
					"/Resolve/src/test/ITest.java",
					"public interface ITest {}\n");
			createFile(
					"/Resolve/src/test/TestClass.java",
					"public class TestClass implements ITest {}\n");
		
			this.wc = getWorkingCopy(
					"/Resolve/src/module-info.java",
					"module com.test {\n" +
					"  provides test.ITest with test.TestClass;\n" +
					"}\n");
		
			String str = this.wc.getSource();
			String selection = "ITest";
			int start = str.indexOf(selection);
			int length = selection.length();
		
			IJavaElement[] elements = this.wc.codeSelect(start, length);
			assertElementsEqual(
				"Unexpected elements",
				"ITest [in ITest.java [in test [in src [in Resolve]]]]",
				elements
			);
		} finally {
			if (modInfo != null)
				deleteResource(modInfo);
			if (testFolder != null)
				deleteResource(testFolder);
		}
	}
	public void testModuleInfo_noReferenceAtKeyword() throws CoreException {
		IFile providesFile = createFile("/Resolve/src/provides.java", "public class provides {}");
		IFile modInfo = null;
		try {
			getWorkingCopy(
					"/Resolve/src/test/ITest.java",
					"public interface ITest {}\n");
			getWorkingCopy(
					"/Resolve/src/test/TestClass.java",
					"public class TestClass implements ITest {}\n");
		
			this.wc = getWorkingCopy(
					"/Resolve/src/module-info.java",
					"module com.test {\n" +
					"  provides p1.Y with ResolveInterface;\n" +
					"}\n");
		
			String str = this.wc.getSource();
			String selection = "provides";
			int start = str.indexOf(selection);
			int length = selection.length();
		
			IJavaElement[] elements = this.wc.codeSelect(start, length);
			assertElementsEqual(
				"Unexpected elements",
				"",
				elements
			);
		} finally {
			deleteResource(providesFile);
			if (modInfo != null)
				deleteResource(modInfo);
		}
	}
	public void testModuleInfo_referenceAtKeywordInNonKWPosition() throws CoreException {
		IFile providesFile = createFile("/Resolve/src/provides.java", "public class provides implements p1.Y {}");
		IFile modInfo = null;
		try {
			getWorkingCopy(
					"/Resolve/src/test/ITest.java",
					"public interface ITest {}\n");
			getWorkingCopy(
					"/Resolve/src/test/TestClass.java",
					"public class TestClass implements ITest {}\n");
		
			this.wc = getWorkingCopy(
					"/Resolve/src/module-info.java",
					"module com.test {\n" + 
					"  provides p1.Y with provides;\n" + 
					"}\n");
		
			String str = this.wc.getSource();
			String selection = "provides";
			int start = str.lastIndexOf(selection);
			int length = selection.length();
		
			IJavaElement[] elements = this.wc.codeSelect(start, length);
			assertElementsEqual(
				"Unexpected elements",
				"provides [in provides.java [in <default> [in src [in Resolve]]]]",
				elements
			);
		} finally {
			deleteResource(providesFile);
			if (modInfo != null)
				deleteResource(modInfo);
		}
	}

	public void testClassFileInModule1() throws CoreException, IOException {
		if (!isJRE9) {
			System.err.println("Test "+getName()+" requires a JRE 9");
			return;
		}
		IJavaProject javaProject = createJava9Project("Test");
		try {
			IType type = javaProject.findType("java.util.zip.ZipFile");
			IClassFile classFile = type.getClassFile();
			String contents = classFile.getBuffer().getContents();
			int start = contents.indexOf("this(");
			IJavaElement[] selected = classFile.codeSelect(start, 4);
			assertElementsEqual(
					"Unexpected elements",
					"ZipFile(java.io.File, int) [in ZipFile [in ZipFile.class [in java.util.zip [in <module:java.base>]]]]",
					selected);
		} finally {
			deleteProject(javaProject);
		}
	}

	public void testUnnamedNamedConflict() throws CoreException, IOException {
		if (!isJRE9) {
			System.err.println("Test "+getName()+" requires a JRE 9");
			return;
		}
		IJavaProject mod = null;
		IJavaProject test = null;
		try {
			mod = createJava9Project("mod");
			createFolder("mod/src/p1/p2");
			createFile("mod/src/p1/p2/C.java", "package p1.p2;\n public class C {}\n");
			createFile("mod/src/module-info.java",
					"module mod {\n" +
					"	exports p1.p2;\n" +
					"}\n");
			mod.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

			test = createJava9Project("Test");
			IClasspathAttribute[] attributes = { JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true") };
			addClasspathEntry(test, JavaCore.newProjectEntry(new Path("/mod"), null, false, attributes, false));
			createFolder("Test/src/p1/p2");
			createFile("Test/src/p1/p2/C1.java", "package p1.p2;\n public class C1 {}\n");
			String source =
					"package q;\n" +
					"public class Main {\n" +
					"	p1.p2.C c;\n" +
					"	p1.p2.C1 c1;\n" +
					"}\n";
			createFolder("Test/src/q");
			createFile("Test/src/q/Main.java", source);
			test.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = test.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers", "The package p1.p2 conflicts with a package accessible from another module: mod", markers);

			ICompilationUnit unit = getCompilationUnit("Test/src/q/Main.java");

			// test that we can select both types despite the package conflict:
			int start = source.indexOf("C c");
			IJavaElement[] selected = unit.codeSelect(start, 2);
			assertElementsEqual(
					"Unexpected elements",
					"C [in C.java [in p1.p2 [in src [in mod]]]]",
					selected);

			start = source.indexOf("C1");
			selected = unit.codeSelect(start, 2);
			assertElementsEqual(
					"Unexpected elements",
					"C1 [in C1.java [in p1.p2 [in src [in Test]]]]",
					selected);
		} finally {
			deleteProject(test);
			deleteProject(mod);
		}
	}
}
