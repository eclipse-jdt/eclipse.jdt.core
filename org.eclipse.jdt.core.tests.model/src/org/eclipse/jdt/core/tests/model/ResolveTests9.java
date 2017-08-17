/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation and others.
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

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
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
	public void testModuleInfo_serviceReference_OK() throws CoreException {
		IFile modInfo = null;
		try {
			getWorkingCopy(
					"/Resolve/src/test/ITest.java",
					"public interface ITest {}\n");
			getWorkingCopy(
					"/Resolve/src/test/TestClass.java",
					"public class TestClass implements ITest {}\n");
		
			modInfo = createFile("/Resolve/src/module-info.java", ""); // TODO: can this be avoided? see https://bugs.eclipse.org/500941
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
		
			modInfo = createFile("/Resolve/src/module-info.java", ""); // TODO: can this be avoided? see https://bugs.eclipse.org/500941
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
		
			modInfo = createFile("/Resolve/src/module-info.java", ""); // TODO: can this be avoided? see https://bugs.eclipse.org/500941
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
}
