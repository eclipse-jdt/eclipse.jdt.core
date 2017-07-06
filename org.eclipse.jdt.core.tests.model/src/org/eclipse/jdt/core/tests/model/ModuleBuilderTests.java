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
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.core.util.Messages;

import junit.framework.Test;

public class ModuleBuilderTests extends ModifyingResourceTests {
	public ModuleBuilderTests(String name) {
		super(name);
	}

	static {
//		 TESTS_NAMES = new String[] { "test_ReconcilerModuleLookup" };
	}
	private static boolean isJRE9 = false;
	protected ProblemRequestor problemRequestor;
	public static Test suite() {
		String javaVersion = System.getProperty("java.version");
		if (javaVersion.length() > 3) {
			javaVersion = javaVersion.substring(0, 3);
		}
		long jdkLevel = CompilerOptions.versionToJdkLevel(javaVersion);
		if (jdkLevel >= ClassFileConstants.JDK9) {
			isJRE9 = true;
		}
		return buildModelTestSuite(ModuleBuilderTests.class, BYTECODE_DECLARATION_ORDER);
	}
	public void setUp() throws Exception {
		super.setUp();
		this.problemRequestor =  new ProblemRequestor();
		this.wcOwner = new WorkingCopyOwner() {
			public IProblemRequestor getProblemRequestor(ICompilationUnit unit) {
				return ModuleBuilderTests.this.problemRequestor;
			}
		};
	}
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		System.setProperty("modules.to.load", "java.base,java.desktop;java.rmi;java.sql;");
		this.currentProject = createJava9Project("P1");
		this.createFile("P1/src/module-info.java", "");
		this.createFolder("P1/src/com/greetings");
		this.createFile("P1/src/com/greetings/Main.java", "");
		waitForManualRefresh();
		waitForAutoBuild();
	}
	// Test that the java.base found as a module package fragment root in the project 
	public void test001() throws CoreException {
		if (!isJRE9) return;
		try {
			IJavaProject project = createJava9Project("Test01", new String[]{"src"});
			this.createFile("Test01/src/module-info.java", "");
			this.createFolder("Test01/src/com/greetings");
			this.createFile("Test01/src/com/greetings/Main.java", "");
			waitForManualRefresh();
			waitForAutoBuild();
			project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
			IPackageFragmentRoot base = null;
			for (IPackageFragmentRoot iRoot : roots) {
				IModuleDescription moduleDescription = iRoot.getModuleDescription();
				if (moduleDescription != null && moduleDescription.getElementName().equals("java.base")) {
					base = iRoot;
					break;
				}
			}
			assertNotNull("Java.base module should not null", base);
			assertMarkers("Unexpected markers", "", project);
		} finally {
			deleteProject("Test01");
		}
	}
	// Test the project compiles without errors with a simple module-info.java
	public void test002() throws CoreException {
		if (!isJRE9) return;
		try {
			this.editFile("P1/src/module-info.java",
							"module M1 {\n" +
							"	exports com.greetings;\n" +
							"	requires java.base;\n" +
							"}");
			waitForManualRefresh();
			this.currentProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertMarkers("Unexpected markers", "", this.currentProject);
		} finally {
		}
	}
	// Test that types from java.base module are seen by the compiler
	// even without an explicit 'requires java.base' declaration.
	public void test003() throws CoreException {
		if (!isJRE9) return;
		try {
			this.editFile("P1/src/module-info.java",
					"module M1 {\n" +
							"	exports com.greetings;\n" +
					"}");
			this.editFile("P1/src/com/greetings/Main.java",
					"package com.greetings;\n" +
					"public class Main {\n" +
					"	public static void main(String[] args) {\n" +
					"	}\n" +
					"}");
			waitForManualRefresh();
			this.currentProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = this.currentProject.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
		}
	}
	// Test that a type that is present in the JDK, but not observable to the source module,
	// is reported as a compilation error.
	public void test004() throws CoreException {
		if (!isJRE9) return;
		try {
			this.editFile("P1/src/module-info.java",
					"module M1 {\n" +
					"	exports com.greetings;\n" +
					"	requires java.base;\n" +
					"}");
			this.editFile("P1/src/com/greetings/Main.java",
					"package com.greetings;\n" +
					"import java.sql.Connection;\n" +
					"public class Main {\n" +
					"	public Connection con = null;\n" +
					"}");
			waitForManualRefresh();
			this.currentProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = this.currentProject.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers", 
					"The import java.sql cannot be resolved\n" + 
					"Connection cannot be resolved to a type", markers);
		} finally {
		}
	}
	// Test that a type that is outside java.base module is available to the compiler
	// when the module is specified as 'requires'.
	public void test005() throws CoreException {
		if (!isJRE9) return;
		try {
			this.editFile("P1/src/module-info.java",
					"module M1 {\n" +
					"	exports com.greetings;\n" +
					"	requires java.base;\n" +
					"	requires java.sql;\n" +
					"}");
			waitForManualRefresh();
			this.currentProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = this.currentProject.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
		}
	}
	// Test that a module that doesn't exist but specified as requires in module-info
	// doesn't affect rest of the compilation.
	public void _test006() throws CoreException {
		if (!isJRE9) return;
		try {
			this.editFile("P1/src/module-info.java",
					"module M1 {\n" +
					"	exports com.greetings;\n" +
					"	requires java.base;\n" +
					"	requires java.sql;\n" +
					"	requires java.idontexist;\n" +
					"}");
			this.editFile("P1/src/com/greetings/Main.java",
					"package com.greetings;\n" +
					"public class Main {\n" +
					"}");
			waitForManualRefresh();
			this.currentProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = this.currentProject.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
		}
	}
	private IJavaProject setupP2() throws CoreException {
		IJavaProject project = createJava9Project("P2");
		IClasspathEntry projectEntry = 
				JavaCore.newProjectEntry(new Path("/P1"), true);
		IClasspathEntry[] old = project.getRawClasspath();
		IClasspathEntry[] newPath = new IClasspathEntry[old.length +1];
		System.arraycopy(old, 0, newPath, 0, old.length);
		newPath[old.length] = projectEntry;
		project.setRawClasspath(newPath, null);
		this.editFile("P1/src/module-info.java",
				"module M1 {\n" +
				"	exports com.greetings;\n" +
				"	requires java.base;\n" +
				"}");
		this.editFile("P1/src/com/greetings/Main.java",
				"package com.greetings;\n" +
				"public class Main {\n" +
				"	public static void main(String[] args) {\n" +
				"	}\n" +
				"}");
		this.createFile("P2/src/module-info.java",
				"module M2 {\n" +
				"	exports org.astro;\n" +
				"	requires M1;\n" +
				"}");
		this.createFolder("P2/src/org/astro");
		this.createFile("P2/src/org/astro/Test.java",
				"package org.astro;\n" +
				"import com.greetings.Main;\n" +
				"public class Test {\n" +
				"	public static void main(String[] args) {\n" +
				"		Main.main(args);\n" +
				"	}\n" +
				"}");
		return project;
	}
	/*
	 * Two Java projects, each with one module. P2 has P1 in its build path but
	 * module M2 has no 'requires' M1. Should report unresolved type, import etc.  
	 *
	 */
	public void test007() throws Exception {
		if (!isJRE9) return;
		try {
			IJavaProject project = setupP2();
			this.editFile("P1/src/module-info.java",
					"module M1 {\n" +
					"	exports com.greetings;\n" +
					"}");
			this.editFile("P2/src/module-info.java",
					"module M2 {\n" +
					"	exports org.astro;\n" +
					"	//requires M1;\n" +
					"}");
			waitForManualRefresh();
			project.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = project.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers", 
					"The import com.greetings cannot be resolved\n" + 
					"Main cannot be resolved", 
					markers);
		} finally {
			deleteProject("P2");
		}
	}
	/*
	 * Two Java project, each with one module. P2 has P1 in its build path and
	 * module M2 'requires' M1. Should report unresolved type, import etc. But M1
	 * does not export the package that is used by M2. Test that M2 does not see
	 * the types in unexported packages.
	 *
	 */
	public void test008() throws Exception {
		if (!isJRE9) return;
		try {
			IJavaProject project = setupP2();
			this.editFile("P1/src/module-info.java",
					"module M1 {\n" +
					"	//exports com.greetings;\n" +
					"}");
			this.editFile("P2/src/module-info.java",
					"module M2 {\n" +
					"	exports org.astro;\n" +
					"	requires M1;\n" +
					"}");
			waitForManualRefresh();
			project.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = project.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers", 
					"The type com.greetings.Main is not accessible\n" + 
					"Main cannot be resolved", 
					markers);
		} finally {
			deleteProject("P2");
		}
	}
	/*
	 * Two Java projects, each with one module. P2 has P1 in its build path.
	 * Module M2 has "requires M1" in module-info and all packages used by M2 
	 * are exported by M1. No errors expected. 
	 */
	public void test009() throws Exception {
		if (!isJRE9) return;
		try {
			IJavaProject project = setupP2();
			this.editFile("P1/src/module-info.java",
					"module M1 {\n" +
					"	exports com.greetings;\n" +
					"}");
			this.editFile("P2/src/module-info.java",
					"module M2 {\n" +
					"	exports org.astro;\n" +
					"	requires M1;\n" +
					"}");
			waitForManualRefresh();
			project.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = project.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", 
					"", markers);
		} finally {
			deleteProject("P2");
		}
	}
	/*
	 * Two Java projects, each with a module. Project P2 depends on P1.
	 * Module M1 exports a package to a specific module, which is not M2.
	 * Usage of types from M1 in M2 should be reported.
	 */
	public void _test010() throws Exception {
		if (!isJRE9) return;
		try {
			IJavaProject project = setupP2();
			this.editFile("P1/src/module-info.java",
					"module M1 {\n" +
					"	exports com.greetings to org.main;\n" +
					"}");
			this.editFile("P2/src/module-info.java",
					"module M2 {\n" +
					"	exports org.astro;\n" +
					"	requires M1;\n" +
					"}");
			waitForManualRefresh();
			project.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = project.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers", 
					"The import com.greetings.Main cannot be resolved\n" + 
					"Main cannot be resolved", 
					markers);
		} finally {
			deleteProject("P2");
		}
	}
	private IJavaProject setupP3() throws CoreException {
		IJavaProject project = createJava9Project("P3");
		IClasspathEntry projectEntry = 
				JavaCore.newProjectEntry(new Path("/P2"), true);
		IClasspathEntry[] old = project.getRawClasspath();
		IClasspathEntry[] newPath = new IClasspathEntry[old.length +1];
		System.arraycopy(old, 0, newPath, 0, old.length);
		newPath[old.length] = projectEntry;
		project.setRawClasspath(newPath, null);
		this.createFile("P3/src/module-info.java",
				"module M3 {\n" +
				"	exports org.main;\n" +
				"	requires M2;\n" +
				"}");
		this.createFolder("P3/src/org/main");
		this.createFile("P3/src/org/main/TestMain.java",
				"package org.main;\n" +
				"import com.greetings.*;\n" +
				"public class TestMain {\n" +
				"	public static void main(String[] args) {\n" +
				"		Main.main(args);\n" +
				"	}\n" +
				"}");
		return project;
	}
	/*
	 * Three Java projects, each with one module. Project P3 depends on P2, which depends on P1.
	 * Module M1 exports a package (to all), M2 requires M1 and M3 requires M2. Usage of types from
	 * M1 in M3 should be reported as errors.
	 */
	public void test011() throws Exception {
		if (!isJRE9) return;
		try {
			this.editFile("P1/src/module-info.java",
					"module M1 {\n" +
					"	exports com.greetings;\n" +
					"}");
			IJavaProject p2 = setupP2();
			IJavaProject p3 = setupP3();
			waitForManualRefresh();
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p3.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers",
					"The package com.greetings is not accessible\n" +
					"Main cannot be resolved",
					markers);
		} finally {
			deleteProject("P2");
			deleteProject("P3");
		}
	}
	/*
	 * Three Java projects, each with one module. Project P3 depends on P2, which depends on P1.
	 * Module M1 exports a package only to M2, M2 requires M1 and M3 requires M2. Usage of types from
	 * M1 in M3 should not be allowed.
	 */
	public void test012() throws Exception {
		if (!isJRE9) return;
		try {
			this.editFile("P1/src/module-info.java",
					"module M1 {\n" +
					"	exports com.greetings to M2;\n" +
					"}");
			IJavaProject p2 = setupP2();
			IJavaProject p3 = setupP3();
			this.editFile("P2/src/module-info.java",
					"module M2 {\n" +
					"	exports org.astro;\n" +
					"	requires M1;\n" +
					"}");
			waitForManualRefresh();
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "",  markers);
			markers = p3.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers", 
					"The package com.greetings is not accessible\n" + 
					"Main cannot be resolved", 
					markers);
		} finally {
			deleteProject("P2");
			deleteProject("P3");
		}
	}
	/*
	 * Three Java projects, each with one module. Project P3 depends on P2, which depends on P1.
	 * Module M1 exports a package (to all), M2 requires 'transitive' M1 and M3 requires M2. Usage of types from
	 * M1 in M3 should be allowed.
	 */
	public void test013() throws Exception {
		if (!isJRE9) return;
		try {
			IJavaProject p2 = setupP2();
			IJavaProject p3 = setupP3();
			this.editFile("P2/src/module-info.java",
					"module M2 {\n" +
					"	exports org.astro;\n" +
					"	requires transitive M1;\n" +
					"}");
			waitForManualRefresh();
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p3.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "",  markers);
		} finally {
			deleteProject("P2");
			deleteProject("P3");
		}
	}
	/*
	 * Three Java projects, each with one module. Project P3 depends on P2, which depends on P1.
	 * Module M1 exports a package only to M2, M2 requires 'public' M1 and M3 requires M2. Usage of types from
	 * M1 in M3 should be allowed. And no errors reported on M2.
	 */
	public void test014() throws Exception {
		if (!isJRE9) return;
		try {
			this.editFile("P1/src/module-info.java",
					"module M1 {\n" +
					"	exports com.greetings to M2;\n" +
					"}");
			IJavaProject p2 = setupP2();
			IJavaProject p3 = setupP3();
			this.editFile("P2/src/module-info.java",
					"module M2 {\n" +
					"	exports org.astro;\n" +
					"	requires transitive M1;\n" +
					"}");
			waitForManualRefresh();
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "",  markers);
			markers = p3.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "",  markers);
		} finally {
			deleteProject("P2");
			deleteProject("P3");
		}
	}
	public void test015() throws CoreException, IOException {
		if (!isJRE9) return;
		try {
			this.editFile("P1/src/module-info.java",
					"module M1 {\n" +
					"	exports com.greetings to M2;\n" +
					"}");
			IJavaProject p2 = setupP2();
			this.editFile("P2/src/module-info.java",
					"module M2 {\n" +
					"	exports org.astro;\n" +
					"	requires transitive M1;\n" +
					"}");
			waitForManualRefresh();
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IFolder folder = getFolder("P1/src");
			assertNotNull("Should be a module", this.currentProject.getModuleDescription());
			folder = getFolder("P2/src");
			folder = getFolder("P1/bin");
			IPath jarPath = p2.getResource().getLocation().append("m0.jar");
			org.eclipse.jdt.core.tests.util.Util.zip(new File(folder.getLocation().toOSString()), jarPath.toOSString());
			IClasspathEntry[] old = p2.getRawClasspath();
			for (int i = 0; i < old.length; i++) {
				if (old[i].isExported()) {
					old[i] = JavaCore.newLibraryEntry(new Path("/P2/m0.jar"), null, null);
					break;
				}
			}
			p2.setRawClasspath(old, null);
			p2.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNotNull("Should be a module", p2.getModuleDescription());
		} finally {
			deleteProject("P2");
			deleteProject("P3");
		}
	}
	/*
	 * Change the module-info and wait for autobuild to 
	 * report expected errors.
	 */
	public void test016() throws CoreException, IOException {
		if (!isJRE9) return;
		try {
			IJavaProject p2 = setupP2();
			this.editFile("P2/src/module-info.java",
					"module M2 {\n" +
					"	exports org.astro;\n" +
					"	requires java.base;\n" +
					"	requires transitive M1;\n" +
					"}");
			waitForManualRefresh();
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "",  markers);
			this.editFile("P2/src/module-info.java",
					"module M2 {\n" +
					"	exports org.astro;\n" +
					"}");
			waitForManualRefresh();
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
			markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers",
					"The import com.greetings cannot be resolved\n" + 
					"Main cannot be resolved",  markers);
		} finally {
			deleteProject("P2");
		}
	}
	/*
	 * Change the module-info of a required module and wait for autobuild to 
	 * report expected errors.
	 */
	public void test017() throws CoreException, IOException {
		if (!isJRE9) return;
		try {
			IJavaProject p2 = setupP2();
			this.editFile("P2/src/module-info.java",
					"module M2 {\n" +
					"	exports org.astro;\n" +
					"	requires java.base;\n" +
					"	requires transitive M1;\n" +
					"}");
			waitForManualRefresh();
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "",  markers);
			this.editFile("P1/src/module-info.java",
					"module M1 {\n" +
					"	requires java.base;\n" +
					"}");
			waitForManualRefresh();
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
			markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers",
					"The type com.greetings.Main is not accessible\n" + 
					"Main cannot be resolved",  markers);
		} finally {
			deleteProject("P2");
			this.editFile("P1/src/module-info.java",
					"module M1 {\n" +
					"	exports com.greetings;\n" +
					"	requires java.base;\n" +
					"}");
		}
	}
	/*
	 * Change the module-info of a required module and wait for autobuild to 
	 * report expected errors.
	 */
	public void test018() throws CoreException, IOException {
		if (!isJRE9) return;
		try {
			String wkspEncoding = System.getProperty("file.encoding");
			final String encoding = "UTF-8".equals(wkspEncoding) ? "Cp1252" : "UTF-8";
			IJavaProject p2 = setupP2();
			this.editFile("P2/src/module-info.java",
					"module M2 {\n" +
					"	exports org.astro;\n" +
					"	requires java.base;\n" +
					"	requires transitive M1;\n" +
					"}");
			waitForManualRefresh();
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "",  markers);
			IFile bin = getFile("P1/bin/com/greetings/Main.class");
			long old = bin.getLocalTimeStamp();
			IFile file = getFile("P1/src/module-info.java");
			file.setCharset(encoding, null);
			waitForManualRefresh();
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
			markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"",  markers);
			long latest = getFile("P1/bin/com/greetings/Main.class").getLocalTimeStamp();
			assertTrue("Should not have been recompiled", old == latest);
		} finally {
			deleteProject("P2");
		}
	}
	/*
	 * Test that adding or removing java.base does not result in
	 * re-compilation of module.
	 */
	public void _test019() throws CoreException, IOException {
		if (!isJRE9) return;
		try {
			this.editFile("P1/src/module-info.java",
					"module M1 {\n" +
					"	exports com.greetings;\n" +
					"	requires java.base;\n" +
					"}");
			waitForManualRefresh();
			this.currentProject.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = this.currentProject.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "",  markers);
			IFile bin = getFile("P1/bin/com/greetings/Main.class");
			long old = bin.getLocalTimeStamp();
			waitForManualRefresh();
			this.editFile("P1/src/module-info.java",
					"module M1 {\n" +
					"	exports com.greetings;\n" +
					"}");
			this.currentProject.getProject().getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
			long latest = getFile("P1/bin/com/greetings/Main.class").getLocalTimeStamp();
			assertTrue("Should not have been recompiled", old == latest);
		} finally {
			deleteProject("P2");
			this.editFile("P1/src/module-info.java",
					"module M1 {\n" +
					"	exports com.greetings;\n" +
					"	requires java.base;\n" +
					"}");
		}
	}
	public void testConvertToModule() throws CoreException, IOException {
		if (!isJRE9) return;
		Hashtable<String, String> javaCoreOptions = JavaCore.getOptions();
		try {
			IJavaProject project = setUpJavaProject("ConvertToModule", "9");
			if (!project.getOption("org.eclipse.jdt.core.compiler.compliance", true).equals("9")) {
				return;
			}
			project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
			IPackageFragmentRoot theRoot = null;
			for (IPackageFragmentRoot root : roots) {
				if (root.getElementName().equals("jdt.test")) {
					theRoot = root;
					break;
				}
			}
			assertNotNull("should not be null", theRoot);
			String[] modules = JavaCore.getReferencedModules(project);
			assertStringsEqual("incorrect result", new String[]{"java.base", "java.desktop", "java.rmi", "java.sql"}, modules);
		} finally {
			this.deleteProject("ConvertToModule");
			 JavaCore.setOptions(javaCoreOptions);
		}
	}
	public void test_services_abstractImpl() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] { 
					"src/module-info.java",
					"module org.astro {\n" +
					"	exports org.astro;\n" +
					"}",
					"src/org/astro/World.java",
					"package org.astro;\n" +
					"public interface World {\n" +
					"	public String name();\n" +
					"}" 
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] { 
					"src/module-info.java",
					"module com.greetings {\n" +
					"	requires org.astro;\n" +
					"	exports com.greetings;\n" +
					"	provides org.astro.World with com.greetings.MyWorld;\n" +
					"}",
					"src/com/greetings/MyWorld.java",
					"package com.greetings;\n" +
					"import org.astro.World;\n"	+
					"public abstract class MyWorld implements World { }\n"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"Invalid service implementation, the type com.greetings.MyWorld is abstract", markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void test_services_invalidImpl() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] { 
					"src/module-info.java",
					"module org.astro {\n" +
					"	exports org.astro;\n" +
					"}",
					"src/org/astro/World.java",
					"package org.astro;\n" +
					"public interface World {\n" +
					"	public String name();\n" +
					"}" 
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] { 
					"src/module-info.java",
					"module com.greetings {\n" +
					"	requires org.astro;\n" +
					"	exports com.greetings;\n" +
					"	provides org.astro.World with com.greetings.MyWorld;\n" +
					"}",
					"src/com/greetings/MyWorld.java",
					"package com.greetings;\n" +
					"public class MyWorld { }\n"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"Type mismatch: cannot convert from MyWorld to World", markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void test_services_NoDefaultConstructor() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	exports com.greetings;\n" +
				"	provides org.astro.World with com.greetings.MyWorld;\n" +
				"}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"import org.astro.World;\n" +
				"public class MyWorld implements World {\n" +
				"	public MyWorld(String name) { }\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"The service implementation com.greetings.MyWorld must define a public static provider method or a no-arg constructor",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void test_services_DefaultConstructorNotVisible() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	exports com.greetings;\n" +
				"	provides org.astro.World with com.greetings.MyWorld;\n" +
				"}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"import org.astro.World;\n" +
				"public class MyWorld implements World {\n" +
				"	MyWorld() { }\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"The no-arg constructor of service implementation com.greetings.MyWorld is not public",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void test_services_DuplicateEntries() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	exports com.greetings;\n" +
				"	provides org.astro.World with com.greetings.MyWorld;\n" +
				"	provides org.astro.World with com.greetings.MyWorld;\n" +
				"}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"import org.astro.World;\n" +
				"public class MyWorld implements World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"Duplicate service entry: org.astro.World",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void test_services_NestedClass() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	exports com.greetings;\n" +
				"	provides org.astro.World with com.greetings.MyWorld.Nested;\n" +
				"}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"import org.astro.World;\n" +
				"public class MyWorld {\n" +
				"	public static class Nested implements World {\n" +
				"		public String name() {\n" +
				"			return \" My World!!\";\n" +
				"		}\n" +
				"	}\n" +
				"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",	"",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void test_services_NonStatic_NestedClass() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	exports com.greetings;\n" +
				"	provides org.astro.World with com.greetings.MyWorld.Nested;\n" +
				"}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"import org.astro.World;\n" +
				"public class MyWorld {\n" +
				"	public class Nested implements World {\n" +
				"		public String name() {\n" +
				"			return \" My World!!\";\n" +
				"		}\n" +
				"	}\n" +
				"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"Invalid service implementation, the type com.greetings.MyWorld.Nested is an inner class",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void test_services_ImplDefinedInAnotherModule() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}",
				"src/org/astro/AstroWorld.java",
				"package org.astro;\n" +
				"public class AstroWorld implements World{\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	provides org.astro.World with org.astro.AstroWorld;\n" +
				"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"Service implementation org.astro.AstroWorld is not defined in the module with the provides directive",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void test_services_ProviderMethod() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	exports com.greetings;\n" +
				"	provides org.astro.World with com.greetings.MyImpl;\n" +
				"}",
				"src/com/greetings/MyImpl.java",
				"package com.greetings;\n" +
				"public class MyImpl {\n" +
				"	public static MyWorld provider() {\n" +
				"		return new MyWorld(\"Name\");\n" +
				"	}\n" +
				"}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"import org.astro.World;\n" +
				"public class MyWorld implements World {\n" +
				"	public MyWorld(String name) { }\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",	"",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void test_services_ProviderMethod_ReturnTypeFromAnotherModule() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			setupModuleProject("org.astro", sources, true);
			sources = new String[] {
				"src/module-info.java",
				"module other.mod {\n" +
				"	requires org.astro;\n" + 
				"	exports org.impl;\n" + 
				"}",
				"src/org/impl/MyWorld.java",
				"package org.impl;\n" +
				"import org.astro.World;\n" +
				"public class MyWorld implements World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			setupModuleProject("other.mod", sources, true);
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	requires other.mod;\n" +
				"	exports com.greetings;\n" +
				"	provides org.astro.World with com.greetings.MyImpl;\n" +
				"}",
				"src/com/greetings/MyImpl.java",
				"package com.greetings;\n" +
				"import org.impl.MyWorld;\n" +
				"public class MyImpl {\n" +
				"	public static MyWorld provider() {\n" +
				"		return new MyWorld();\n" +
				"	}\n" +
				"}"
			};
			IJavaProject p3 = setupModuleProject("com.greetings", src, true);
			p3.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p3.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",	"",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("other.mod");
			deleteProject("com.greetings");
		}
	}
	public void test_services_ProviderMethod_ReturnTypeInvisible() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			setupModuleProject("org.astro", sources, true);
			sources = new String[] {
				"src/module-info.java",
				"module other.mod {\n" +
				"	requires org.astro;\n" + 
				"}",
				"src/org/impl/MyWorld.java",
				"package org.impl;\n" +
				"import org.astro.World;\n" +
				"public class MyWorld implements World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			setupModuleProject("other.mod", sources, true);
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	requires other.mod;\n" +
				"	exports com.greetings;\n" +
				"	provides org.astro.World with com.greetings.MyImpl;\n" +
				"}",
				"src/com/greetings/MyImpl.java",
				"package com.greetings;\n" +
				"import org.impl.MyWorld;\n" +
				"public class MyImpl {\n" +
				"	public static MyWorld provider() {\n" +
				"		return new MyWorld();\n" +
				"	}\n" +
				"}"
			};
			IJavaProject p3 = setupModuleProject("com.greetings", src, true);
			p3.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p3.getProject().getFile(new Path("src/module-info.java")).findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",	"MyWorld cannot be resolved to a type",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("other.mod");
			deleteProject("com.greetings");
		}
	}
	public void test_services_ProviderMethod_InvalidReturnType() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	exports com.greetings;\n" +
				"	provides org.astro.World with com.greetings.MyImpl;\n" +
				"}",
				"src/com/greetings/MyImpl.java",
				"package com.greetings;\n" +
				"public class MyImpl {\n" +
				"	public static MyWorld provider() {\n" +
				"		return new MyWorld(\"Name\");\n" +
				"	}\n" +
				"}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"public class MyWorld {\n" +
				"	public MyWorld(String name) { }\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",	"Type mismatch: cannot convert from MyWorld to World",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void test_services_DuplicateImplEntries() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	exports com.greetings;\n" +
				"	provides org.astro.World with com.greetings.MyWorld, com.greetings.MyWorld;\n" +
				"}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"import org.astro.World;\n" +
				"public class MyWorld implements World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"Duplicate service entry: com.greetings.MyWorld",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void test_services_InvalidIntfType() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	exports com.greetings;\n" +
				"	provides com.greetings.MyEnum with com.greetings.MyEnum;\n" +
				"}",
				"src/com/greetings/MyEnum.java",
				"package com.greetings;\n" +
				"public enum MyEnum {}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src);
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers",
					"Invalid service interface com.greetings.MyEnum, must be a class, interface or annotation type\n" +
					"Invalid service implementation com.greetings.MyEnum, must be a public class or interface type",  markers);
		} finally {
			deleteProject("com.greetings");
		}
	}
	public void test_services_InvalidImplType() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	exports com.greetings;\n" +
				"	provides org.astro.World with com.greetings.MyEnum;\n" +
				"}",
				"src/com/greetings/MyEnum.java",
				"package com.greetings;\n" +
				"public enum MyEnum implements org.astro.World {}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"Invalid service implementation com.greetings.MyEnum, must be a public class or interface type",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void test_services_nonPublicImpl() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	exports com.greetings;\n" +
				"	provides org.astro.World with com.greetings.MyWorld;\n" +
				"}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"import org.astro.World;\n" +
				"class MyWorld implements World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"The type com.greetings.MyWorld is not visible",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void test_Exports_Error() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	exports com.greetings;\n" +
				"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src);
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",	
					"The package com.greetings does not exist or is empty",  markers);
		} finally {
			deleteProject("com.greetings");
		}
	}
	public void test_DuplicateExports() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" + 
				"	exports org.astro;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			p1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",	
					"Duplicate exports entry: org.astro",  markers);
		} finally {
			deleteProject("org.astro");
		}
	}
	public void test_TargetedExports_Duplicates() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro to com.greetings, com.greetings;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	exports com.greetings;\n" +
				"}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"import org.astro.World;\n" +
				"public class MyWorld implements World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",	
					"Duplicate module name: com.greetings",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	// Types from source module should be resolved in target module
	// when package is exported specifically to the target module
	public void test_TargetedExports() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro to com.greetings;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	exports com.greetings;\n" +
				"}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"import org.astro.World;\n" +
				"public class MyWorld implements World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",	"",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	// Types in one module should not be visible in target module when
	// source module exports packages to a specific module which is not
	// the same as the target module
	public void test_TargetedExports_Error() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module some.mod { }",
			};
			setupModuleProject("some.mod", sources);
			sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro to some.mod;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	exports com.greetings;\n" +
				"}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"import org.astro.World;\n" +
				"public class MyWorld implements World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers",	
					"The type org.astro.World is not accessible\n" +
					"World cannot be resolved to a type",
					markers);
		} finally {
			deleteProject("some.mod");
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	// It is permitted for the to clause of an exports or opens statement to 
	// specify a module which is not observable
	public void test_TargetedExports_Unresolved() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro to some.mod;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			p1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",	"",  markers);
		} finally {
			deleteProject("org.astro");
		}
	}
	// Target module of an exports statement should be resolved without having an explicit
	// dependency to the project that defines the module
	public void test_TargetedExports_Resolution() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module some.mod {\n" +
				"}"
			};
			setupModuleProject("some.mod", sources);
			sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro to some.mod;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			p1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("some.mod");
		}
	}
	// Make sure modules in the workspace are resolved via the module source path container
	// without needing to add a dependency to the project explicitly
	public void test_ModuleSourcePathContainer() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			setupModuleProject("org.astro", sources);
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	exports com.greetings;\n" +
				"}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"import org.astro.World;\n" +
				"public class MyWorld implements World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			IClasspathEntry dep = JavaCore.newContainerEntry(new Path(JavaCore.MODULE_PATH_CONTAINER_ID));
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	// Make sure module path container picks up changes to module-info
	public void test_ModuleSourcePath_update() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module some.mod {\n" +
				"}"
			};
			setupModuleProject("some.mod", sources);
			sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			setupModuleProject("org.astro", sources);
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	exports com.greetings;\n" +
				"}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"import org.astro.World;\n" +
				"public class MyWorld implements World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			IClasspathEntry dep = JavaCore.newContainerEntry(new Path(JavaCore.MODULE_PATH_CONTAINER_ID));
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			this.editFile("com.greetings/src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	requires some.mod;\n" +
				"	exports com.greetings;\n" +
				"}");
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("some.mod");
			deleteProject("com.greetings");
		}
	}
	// Implicit module dependencies via the 'requires transitive' directive should be
	// resolved via the module path container
	public void test_ModuleSourcePath_implicitdeps() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IClasspathEntry dep = JavaCore.newContainerEntry(new Path(JavaCore.MODULE_PATH_CONTAINER_ID));
			setupModuleProject("org.astro", sources, new IClasspathEntry[]{dep});
			sources = new String[] {
				"src/module-info.java",
				"module some.mod {\n" +
				"	requires transitive org.astro;\n" +
				"}"
			};
			setupModuleProject("some.mod", sources, new IClasspathEntry[]{dep});
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires some.mod;\n" +
				"	exports com.greetings;\n" +
				"}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"import org.astro.World;\n" +
				"public class MyWorld implements World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("some.mod");
			deleteProject("com.greetings");
		}
	}
	// Changes to implicit dependencies should be reflected
	public void test_ModuleSourcePath_implicitdeps2() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module some.mod {\n" +
				"	requires transitive org.astro;\n" +
				"}"
			};
			IClasspathEntry dep = JavaCore.newContainerEntry(new Path(JavaCore.MODULE_PATH_CONTAINER_ID));
			setupModuleProject("some.mod", sources, new IClasspathEntry[]{dep});
			sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			setupModuleProject("org.astro", sources, new IClasspathEntry[]{dep});
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires some.mod;\n" +
				"	exports com.greetings;\n" +
				"}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"import org.astro.World;\n" +
				"public class MyWorld implements World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			this.editFile("some.mod/src/module-info.java",
				"module some.mod {\n" +
				"	requires org.astro;\n" +
				"}");
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers", 
					"The import org.astro.World cannot be resolved\n" +
					"World cannot be resolved to a type",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("some.mod");
			deleteProject("com.greetings");
		}
	}
	// Changes to implicit dependencies should be reflected
	//TODO enable once we know how to update project cache
	public void _test_ModuleSourcePath_implicitdeps3() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module some.mod {\n" +
				"	requires org.astro;\n" +
				"}"
			};
			IClasspathEntry dep = JavaCore.newContainerEntry(new Path(JavaCore.MODULE_PATH_CONTAINER_ID));
			setupModuleProject("some.mod", sources, new IClasspathEntry[]{dep});
			sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			setupModuleProject("org.astro", sources, new IClasspathEntry[]{dep});
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires some.mod;\n" +
				"	exports com.greetings;\n" +
				"}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"import org.astro.World;\n" +
				"public class MyWorld implements World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			this.editFile("some.mod/src/module-info.java",
				"module some.mod {\n" +
				"	requires transitive org.astro;\n" +
				"}");
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("some.mod");
			deleteProject("com.greetings");
		}
	}
	public void test_Cycle_In_Module_Dependency() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" +
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IClasspathEntry dep = JavaCore.newContainerEntry(new Path(JavaCore.MODULE_PATH_CONTAINER_ID));
			setupModuleProject("org.astro", sources, new IClasspathEntry[]{dep});
			sources = new String[] {
				"src/module-info.java",
				"module some.mod {\n" +
				"	requires org.astro;\n" +
				"}"
			};
			setupModuleProject("some.mod", sources, new IClasspathEntry[]{dep});
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires some.mod;\n" +
				"	exports com.greetings;\n" +
				"}"
			};
			
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			editFile("org.astro/src/module-info.java", 
					"module org.astro {\n" +
					"	exports org.astro;\n" +
					"	requires com.greetings;\n" +
					"}");
			waitForAutoBuild();
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertTrue("Should detect cycle", p2.hasClasspathCycle(null));
		} finally {
			deleteProject("org.astro");
			deleteProject("some.mod");
			deleteProject("com.greetings");
		}
	}
	public void test_Cycle_In_Implicit_Module_Dependency() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" +
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IClasspathEntry dep = JavaCore.newContainerEntry(new Path(JavaCore.MODULE_PATH_CONTAINER_ID));
			setupModuleProject("org.astro", sources, new IClasspathEntry[]{dep});
			sources = new String[] {
				"src/module-info.java",
				"module some.mod {\n" +
				"	requires transitive org.astro;\n" +
				"}"
			};
			setupModuleProject("some.mod", sources, new IClasspathEntry[]{dep});
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires some.mod;\n" +
				"	exports com.greetings;\n" +
				"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			editFile("org.astro/src/module-info.java", 
				"module org.astro {\n" +
				"	exports org.astro;\n" +
				"	requires transitive com.greetings;\n" +
				"}");
			waitForAutoBuild();
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertTrue("Should detect cycle", p2.hasClasspathCycle(null));
		} finally {
			deleteProject("org.astro");
			deleteProject("some.mod");
			deleteProject("com.greetings");
		}
	}
	public void test_bug506479() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
					"src/module-info.java",
					"module org.astro {\n" +
					"	exports org.astro;\n" +
					"}",
					"src/org/astro/World.java",
					"package org.astro;\n" +
					"public interface World {\n" +
					"	public String name();\n" +
					"}"
				};
			IClasspathEntry dep = JavaCore.newContainerEntry(new Path(JavaCore.MODULE_PATH_CONTAINER_ID));
			IJavaProject p1 = setupModuleProject("org.astro", sources, new IClasspathEntry[]{dep});
			IWorkspaceDescription desc = p1.getProject().getWorkspace().getDescription();
			desc.setAutoBuilding(false);
			p1.getProject().getWorkspace().setDescription(desc);
			this.deleteFile("org.astro/src/module-info.java");
			this.createFile(
					"org.astro/src/module-info.java",
					"module org.astro {\n" +
					"	exports org.astro;\n" +
					"}");
			p1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
		} finally {
			deleteProject("org.astro");
		}
	}
	public void test_Multiple_SourceFolders() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}",
				"othersrc/org/astro/OtherWorld.java",
				"package org.astro;\n" +
				"import org.astro.World;\n" +
				"public interface OtherWorld {\n" +
				"	default public String name() {\n" +
				"		return \" Other World!!\";\n" +
				"	}\n" +
				"}"
			};
			setupModuleProject("org.astro", new String[]{"src", "othersrc"}, sources, null);
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	exports com.greetings;\n" +
				"}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"import org.astro.World;\n" +
				"public class MyWorld implements World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}",
				"othersrc/com/greetings/AnotherWorld.java",
				"package com.greetings;\n" +
				"import org.astro.OtherWorld;\n" +
				"public class AnotherWorld implements OtherWorld {\n" +
				"	public String name() {\n" +
				"		return \" Another World!!\";\n" +
				"	}\n" +
				"}"
			};
			IClasspathEntry dep = JavaCore.newContainerEntry(new Path(JavaCore.MODULE_PATH_CONTAINER_ID));
			IJavaProject p2 = setupModuleProject("com.greetings", new String[]{"src", "othersrc"}, src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void test_Multiple_SourceFolders_WithModuleInfo() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}",
				"othersrc/org/astro/OtherWorld.java",
				"package org.astro;\n" +
				"import org.astro.World;\n" +
				"public interface OtherWorld {\n" +
				"	default public String name() {\n" +
				"		return \" Other World!!\";\n" +
				"	}\n" +
				"}"
			};
			setupModuleProject("org.astro", new String[]{"src", "othersrc"}, sources, null);
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	exports com.greetings;\n" +
				"}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"import org.astro.World;\n" +
				"public class MyWorld implements World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}",
				"othersrc/module-info.java",
				"module com.greetings1 {\n" +
				"	requires org.astro;\n" +
				"	exports com.greetings;\n" +
				"}",
				"othersrc/com/greetings/AnotherWorld.java",
				"package com.greetings;\n" +
				"import org.astro.OtherWorld;\n" +
				"public class AnotherWorld implements OtherWorld {\n" +
				"	public String name() {\n" +
				"		return \" Another World!!\";\n" +
				"	}\n" +
				"}"
			};
			IClasspathEntry dep = JavaCore.newContainerEntry(new Path(JavaCore.MODULE_PATH_CONTAINER_ID));
			IJavaProject p2 = setupModuleProject("com.greetings", new String[]{"src", "othersrc"}, src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_ZERO);
			assertEquals(1, markers.length);
			String msg = markers[0].getAttribute(IMarker.MESSAGE, "");
			String expected = Messages.bind(Messages.classpath_duplicateEntryPath, TypeConstants.MODULE_INFO_FILE_NAME_STRING, p2.getElementName());
			assertTrue("Unexpected result", msg.indexOf(expected) != -1);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void test_Multiple_SourceFolders_addModuleInfo() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}",
				"othersrc/org/astro/OtherWorld.java",
				"package org.astro;\n" +
				"import org.astro.World;\n" +
				"public interface OtherWorld {\n" +
				"	default public String name() {\n" +
				"		return \" Other World!!\";\n" +
				"	}\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", new String[]{"src", "othersrc"}, sources, null);
			this.createFile("org.astro/othersrc/module-info.java", 
					"module org.astro1 {\n" +
					"	exports org.astro;\n" + 
					"}");
			waitForAutoBuild();
			p1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_ZERO);
			assertEquals(1, markers.length);
			String msg = markers[0].getAttribute(IMarker.MESSAGE, "");
			String expected = Messages.bind(Messages.classpath_duplicateEntryPath, TypeConstants.MODULE_INFO_FILE_NAME_STRING, p1.getElementName());
			assertTrue("Unexpected result", msg.indexOf(expected) != -1);
		} finally {
			deleteProject("org.astro");
		}
	}
	public void test_Multiple_SourceFolders_removeModuleInfo() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}",
				"othersrc/module-info.java",
				"module org.astro1 {\n" +
				"	exports org.astro;\n" + 
				"}",
				"othersrc/org/astro/OtherWorld.java",
				"package org.astro;\n" +
				"import org.astro.World;\n" +
				"public interface OtherWorld {\n" +
				"	default public String name() {\n" +
				"		return \" Other World!!\";\n" +
				"	}\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", new String[]{"src", "othersrc"}, sources, null);
			waitForAutoBuild();
			this.deleteFile("org.astro/othersrc/module-info.java");
			waitForAutoBuild();
			p1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_ZERO);
			assertEquals(0, markers.length);
		} finally {
			deleteProject("org.astro");
		}
	}
	public void test_services_multipleImpl() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] { 
					"src/module-info.java",
					"module org.astro {\n" +
					"	exports org.astro;\n" +
					"}",
					"src/org/astro/World.java",
					"package org.astro;\n" +
					"public interface World {\n" +
					"	public String name();\n" +
					"}" 
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] { 
					"src/module-info.java",
					"import org.astro.World;\n" +
					"import com.greetings.*;\n" +
					"module com.greetings {\n" +
					"	requires org.astro;\n" +
					"	exports com.greetings;\n" +
					"	provides World with MyWorld, AnotherWorld;\n" +
					"}",
					"src/com/greetings/MyWorld.java",
					"package com.greetings;\n" +
					"import org.astro.World;\n"	+
					"public class MyWorld implements World {\n" +
					"	public String name() {\n" +
					"		return \" My World!!\";\n" +
					"	}\n" +
					"}",
					"src/com/greetings/AnotherWorld.java",
					"package com.greetings;\n" +
					"import org.astro.World;\n"	+
					"public class AnotherWorld implements World {\n" +
					"	public String name() {\n" +
					"		return \" Another World!!\";\n" +
					"	}\n" +
					"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void test_imports_in_moduleinfo() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] { 
					"src/module-info.java",
					"module org.astro {\n" +
					"	exports org.astro;\n" +
					"}",
					"src/org/astro/World.java",
					"package org.astro;\n" +
					"public interface World {\n" +
					"	public String name();\n" +
					"}" 
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] { 
					"src/module-info.java",
					"import org.astro.World;\n" +
					"module com.greetings {\n" +
					"	requires org.astro;\n" +
					"	exports com.greetings;\n" +
					"	provides World with com.greetings.MyWorld;\n" +
					"}",
					"src/com/greetings/MyWorld.java",
					"package com.greetings;\n" +
					"import org.astro.World;\n"	+
					"public class MyWorld implements World {\n" +
					"	public String name() {\n" +
					"		return \" My World!!\";\n" +
					"	}\n" +
					"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}

	public void test_Opens_Error() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	opens com.greetings;\n" +
				"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src);
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",	
					"The package com.greetings does not exist or is empty",  markers);
		} finally {
			deleteProject("com.greetings");
		}
	}
	public void test_DuplicateOpens() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	opens org.astro;\n" + 
				"	opens org.astro;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			p1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",	
					"Duplicate opens entry: org.astro",  markers);
		} finally {
			deleteProject("org.astro");
		}
	}
	public void test_TargetedOpens_Duplicates() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	opens org.astro to com.greetings, com.greetings;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	exports com.greetings;\n" +
				"}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"import org.astro.World;\n" +
				"public class MyWorld implements World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",	
					"Duplicate module name: com.greetings",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	// It is permitted for the to clause of an exports or opens statement to 
	// specify a module which is not observable
	public void test_TargetedOpens_Unresolved() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	opens org.astro to some.mod;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			p1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",	"",  markers);
		} finally {
			deleteProject("org.astro");
		}
	}
	// It is a compile-time error if an opens statement appears in the declaration of an open module. 
	public void test_OpensStatment_in_OpenModule() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"open module org.astro {\n" +
				"	opens org.astro to some.mod;\n" + 
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			p1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",	
				"opens statement is not allowed, as module org.astro is declared open",  markers);
		} finally {
			deleteProject("org.astro");
		}
	}
	public void test_uses_DuplicateEntries() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" +
				"	uses org.astro.World;\n" +
				"	uses org.astro.World;\n" +
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			p1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"Duplicate uses entry: org.astro.World",  markers);
		} finally {
			deleteProject("org.astro");
		}
	}
	public void test_uses_InvalidIntfType() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	exports com.greetings;\n" +
				"	uses com.greetings.MyEnum;\n" +
				"}",
				"src/com/greetings/MyEnum.java",
				"package com.greetings;\n" +
				"public enum MyEnum {}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src);
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers",
					"Invalid service interface com.greetings.MyEnum, must be a class, interface or annotation type",  markers);
		} finally {
			deleteProject("com.greetings");
		}
	}
	public void test_ReconcilerModuleLookup1() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires java.sql;\n" +
				"}"};
			setupModuleProject("com.greetings", src);
			this.workingCopies = new ICompilationUnit[1];
			char[] sourceChars = src[1].toCharArray();
			this.problemRequestor.initialize(sourceChars);
			this.workingCopies[0] = getCompilationUnit("/com.greetings/src/module-info.java").getWorkingCopy(this.wcOwner, null);
			assertProblems(
					"Unexpected problems",
					"----------\n" + 
					"----------\n",
					this.problemRequestor);
		} finally {
			deleteProject("com.greetings");
		}
	}
	public void test_ReconcilerModuleLookup2() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires java.sq;\n" +
				"}"};
			setupModuleProject("com.greetings", src);
			this.workingCopies = new ICompilationUnit[1];
			char[] sourceChars = src[1].toCharArray();
			this.problemRequestor.initialize(sourceChars);
			this.workingCopies[0] = getCompilationUnit("/com.greetings/src/module-info.java").getWorkingCopy(this.wcOwner, null);
			assertProblems(
					"Unexpected problems",
					"----------\n" + 
					"1. ERROR in /com.greetings/src/module-info.java (at line 2)\n" + 
					"	requires java.sq;\n" + 
					"	         ^^^^^^^\n" + 
					"java.sq cannot be resolved to a module\n" + 
					"----------\n",
					this.problemRequestor);
		} finally {
			deleteProject("com.greetings");
		}
	}
	public void testSystemLibAsJMod() throws CoreException {
		if (!isJRE9) return;
		try {
			IJavaProject project = createJava9Project("Test01", new String[]{"src"});
			IClasspathEntry[] rawClasspath = project.getRawClasspath();
			for (int i = 0; i < rawClasspath.length; i++) {
				IPath path = rawClasspath[i].getPath();
				if (path.lastSegment().equals("jrt-fs.jar")) {
					path = path.removeLastSegments(2).append("jmods").append("java.base.jmod");
					IClasspathEntry newEntry = JavaCore.newLibraryEntry(path, rawClasspath[i].getSourceAttachmentPath(), new Path("java.base"));
					rawClasspath[i] = newEntry;
				}
			}
			project.setRawClasspath(rawClasspath, null);
			this.createFile("Test01/src/module-info.java", "");
			this.createFolder("Test01/src/com/greetings");
			this.createFile("Test01/src/com/greetings/Main.java", "");
			waitForManualRefresh();
			waitForAutoBuild();
			project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
			IPackageFragmentRoot base = null;
			for (IPackageFragmentRoot iRoot : roots) {
				IModuleDescription moduleDescription = iRoot.getModuleDescription();
				if (moduleDescription != null) {
					base = iRoot;
					break;
				}
			}
			assertNotNull("Java.base module should not null", base);
			assertMarkers("Unexpected markers", "", project);
		} finally {
			deleteProject("Test01");
		}
	}
	public void testBug510617() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] src = new String[] {
				"src/module-info.java",
				"module Test {\n" +
				"	exports p;\n" +
				"	requires java.sql;\n" + 
				"	provides java.sql.Driver with p.C;\n" +
				"}",
				"src/p/C.java",
				"package p;\n" + 
				"import java.lang.SecurityManager;\n" + 
				"import java.sql.Connection;\n" + 
				"import java.sql.Driver;\n" + 
				"import java.sql.DriverPropertyInfo;\n" + 
				"import java.sql.SQLException;\n" + 
				"import java.sql.SQLFeatureNotSupportedException;\n" + 
				"import java.util.Properties;\n" + 
				"import java.util.logging.Logger;\n" + 
				"public class C implements Driver {\n" + 
				"	SecurityManager s;\n" + 
				"	@Override\n" + 
				"	public boolean acceptsURL(String arg0) throws SQLException {\n" + 
				"		return false;\n" + 
				"	}\n" + 
				"	@Override\n" + 
				"	public Connection connect(String arg0, Properties arg1) throws SQLException {\n" + 
				"		return null;\n" + 
				"	}\n" + 
				"	@Override\n" + 
				"	public int getMajorVersion() {\n" + 
				"		return 0;\n" + 
				"	}\n" + 
				"	@Override\n" + 
				"	public int getMinorVersion() {\n" + 
				"		return 0;\n" + 
				"	}\n" + 
				"	@Override\n" + 
				"	public Logger getParentLogger() throws SQLFeatureNotSupportedException {\n" + 
				"		return null;\n" + 
				"	}\n" + 
				"	@Override\n" + 
				"	public DriverPropertyInfo[] getPropertyInfo(String arg0, Properties arg1) throws SQLException {\n" + 
				"		return null;\n" + 
				"	}\n" + 
				"	@Override\n" + 
				"	public boolean jdbcCompliant() {\n" + 
				"		return false;\n" + 
				"	} \n" + 
				"}"
			};
			setupModuleProject("Test", src);
			this.workingCopies = new ICompilationUnit[1];
			char[] sourceChars = src[1].toCharArray();
			this.problemRequestor.initialize(sourceChars);
			this.workingCopies[0] = getCompilationUnit("/Test/src/module-info.java").getWorkingCopy(this.wcOwner, null);
			assertProblems(
				"Unexpected problems",
				"----------\n" + 
				"----------\n",
				this.problemRequestor);
		} finally {
			deleteProject("Test");
		}
	}
	public void test_annotations_in_moduleinfo() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] { 
					"src/module-info.java",
					"module org.astro {\n" +
					"	exports org.astro;\n" +
					"}",
					"src/org/astro/World.java",
					"package org.astro;\n" +
					"public interface World {\n" +
					"	public String name();\n" +
					"}",
					"src/org/astro/Foo.java",
					"package org.astro;\n" +
					"public @interface Foo {}" 
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] { 
					"src/module-info.java",
					"import org.astro.Foo;\n" +
					"import org.astro.World;\n" +
					"@Foo\n" +
					"module com.greetings {\n" +
					"	requires org.astro;\n" +
					"	exports com.greetings;\n" +
					"	provides World with com.greetings.MyWorld;\n" +
					"}",
					"src/com/greetings/MyWorld.java",
					"package com.greetings;\n" +
					"import org.astro.World;\n"	+
					"public class MyWorld implements World {\n" +
					"	public String name() {\n" +
					"		return \" My World!!\";\n" +
					"	}\n" +
					"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void test_unresolved_annotations() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] { 
					"src/module-info.java",
					"module org.astro {\n" +
					"	exports org.astro;\n" +
					"}",
					"src/org/astro/World.java",
					"package org.astro;\n" +
					"public interface World {\n" +
					"	public String name();\n" +
					"}",
					"src/org/astro/Foo.java",
					"package org.astro;\n" +
					"public @interface Foo {}" 
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] { 
					"src/module-info.java",
					"import org.astro.Foo;\n" +
					"import org.astro.World;\n" +
					"@Foo @Bar\n" +
					"module com.greetings {\n" +
					"	requires org.astro;\n" +
					"	exports com.greetings;\n" +
					"	provides World with com.greetings.MyWorld;\n" +
					"}",
					"src/com/greetings/MyWorld.java",
					"package com.greetings;\n" +
					"import org.astro.World;\n"	+
					"public class MyWorld implements World {\n" +
					"	public String name() {\n" +
					"		return \" My World!!\";\n" +
					"	}\n" +
					"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", 
					"Bar cannot be resolved to a type", markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void test_illegal_modifiers() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] { 
					"src/module-info.java",
					"module org.astro {\n" +
					"	exports org.astro;\n" +
					"}",
					"src/org/astro/World.java",
					"package org.astro;\n" +
					"public interface World {\n" +
					"	public String name();\n" +
					"}",
					"src/org/astro/Foo.java",
					"package org.astro;\n" +
					"public @interface Foo {}" 
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] { 
					"src/module-info.java",
					"import org.astro.Foo;\n" +
					"import org.astro.World;\n" +
					"@Foo\n" +
					"private static module com.greetings {\n" +
					"	requires org.astro;\n" +
					"	exports com.greetings;\n" +
					"	provides World with com.greetings.MyWorld;\n" +
					"}",
					"src/com/greetings/MyWorld.java",
					"package com.greetings;\n" +
					"import org.astro.World;\n"	+
					"public class MyWorld implements World {\n" +
					"	public String name() {\n" +
					"		return \" My World!!\";\n" +
					"	}\n" +
					"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", 
					"Illegal modifier for module com.greetings; only open is permitted", markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void test_annotations_with_target() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] { 
					"src/module-info.java",
					"module org.astro {\n" +
					"	exports org.astro;\n" +
					"}",
					"src/org/astro/World.java",
					"package org.astro;\n" +
					"public interface World {\n" +
					"	public String name();\n" +
					"}",
					"src/org/astro/Foo.java",
					"package org.astro;\n" +
					"import java.lang.annotation.ElementType;\n" +
					"import java.lang.annotation.Target;\n" +
					"@Target(ElementType.MODULE)\n" +
					"public @interface Foo {}" 
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] { 
					"src/module-info.java",
					"import org.astro.Foo;\n" +
					"import org.astro.World;\n" +
					"@Foo\n" +
					"module com.greetings {\n" +
					"	requires org.astro;\n" +
					"	exports com.greetings;\n" +
					"	provides World with com.greetings.MyWorld;\n" +
					"}",
					"src/com/greetings/MyWorld.java",
					"package com.greetings;\n" +
					"import org.astro.World;\n"	+
					"public class MyWorld implements World {\n" +
					"	public String name() {\n" +
					"		return \" My World!!\";\n" +
					"	}\n" +
					"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void test_annotations_with_wrong_target() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] { 
					"src/module-info.java",
					"module org.astro {\n" +
					"	exports org.astro;\n" +
					"}",
					"src/org/astro/World.java",
					"package org.astro;\n" +
					"public interface World {\n" +
					"	public String name();\n" +
					"}",
					"src/org/astro/Foo.java",
					"package org.astro;\n" +
					"import java.lang.annotation.ElementType;\n" +
					"import java.lang.annotation.Target;\n" +
					"@Target({ElementType.TYPE_PARAMETER, ElementType.TYPE})\n" +
					"public @interface Foo {}" 
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] { 
					"src/module-info.java",
					"import org.astro.Foo;\n" +
					"import org.astro.World;\n" +
					"@Foo\n" +
					"module com.greetings {\n" +
					"	requires org.astro;\n" +
					"	exports com.greetings;\n" +
					"	provides World with com.greetings.MyWorld;\n" +
					"}",
					"src/com/greetings/MyWorld.java",
					"package com.greetings;\n" +
					"import org.astro.World;\n"	+
					"public class MyWorld implements World {\n" +
					"	public String name() {\n" +
					"		return \" My World!!\";\n" +
					"	}\n" +
					"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", 
					"The annotation @Foo is disallowed for this location", markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void testBug518334() throws CoreException, IOException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] { 
					"src/module-info.java",
					"module org.astro {\n" +
					"	requires java.sql;\n" +
					"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			waitForAutoBuild();
			// set options
			Map<String, String> options = new HashMap<>();
			options.put(CompilerOptions.OPTION_Compliance, "1.8");
			options.put(CompilerOptions.OPTION_Source, "1.8");
			options.put(CompilerOptions.OPTION_TargetPlatform, "1.8");
			p1.setOptions(options);
//			waitForAutoBuild();
			p1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertTrue("Module declaration incorrectly accepted below 9", markers.length > 0);
		} finally {
			deleteProject("org.astro");
		}
	}
	public void testBug518334a() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] { 
					"src/module-info.java",
					"module org.astro {\n" +
					"	exports org.astro;\n" +
					"}",
					"src/org/astro/World.java",
					"package org.astro;\n" +
					"public interface World {\n" +
					"	public String name();\n" +
					"}" 
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			String[] src = new String[] { 
					"src/module-info.java",
					"module com.greetings {\n" +
					"	requires org.astro;\n" +
					"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			waitForAutoBuild();
			// set options
			Map<String, String> options = new HashMap<>();
			options.put(CompilerOptions.OPTION_Compliance, "1.8");
			options.put(CompilerOptions.OPTION_Source, "1.8");
			options.put(CompilerOptions.OPTION_TargetPlatform, "1.8");
			p1.setOptions(options);

			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", 
					"org.astro cannot be resolved to a module", markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}

	public void test_api_leak_1() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources1 = {
								"src/module-info.java", 
								"module mod.one { \n" +
								"	exports pm;\n" +
								"}",
								"src/impl/Other.java", 
								"package impl;\n" +
								"public class Other {\n" +
								"}\n",
								"src/pm/C1.java", 
								"package pm;\n" +
								"import impl.Other;\n" + 
								"public class C1 extends Other {\n" +
								"	public void m1(Other o) {}\n" + 
								"}\n"
							};
			IJavaProject p1 = setupModuleProject("mod.one", sources1);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			p1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

			String[] sources2 = {
								"src/module-info.java", 
								"module mod.two { \n" +
								"	requires mod.one;\n" +
								"}",
								"src/impl/Other.java", 
								"package impl;\n" +
								"public class Other {\n" +
								"}\n",
								"src/po/Client.java", 
								"package po;\n" + 
								"import pm.C1;\n" + 
								"public class Client {\n" + 
								"    void test1(C1 one) {\n" + 
								"        one.m1(one);\n" + 
								"    }\n" + 
								"}\n"
							};
			IJavaProject p2 = setupModuleProject("mod.two", sources2, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);

			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
			deleteProject("mod.one");
			deleteProject("mod.two");
		}
	}

	/**
	 * Same-named classes should not conflict, since one is not accessible.
	 * Still a sub class of the inaccessible class can be accessed and used for a method argument.
	 */
	public void test_api_leak_2() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources1 = {
						"src/module-info.java", 
						"module mod.one { \n" +
						"	exports pm;\n" +
						"}",
						"src/impl/SomeImpl.java", 
						"package impl;\n" +
								"public class SomeImpl {\n" +
						"}\n",
						"src/pm/C1.java", 
						"package pm;\n" +
						"import impl.SomeImpl;\n" + 
						"public class C1 {\n" +
						"	public void m1(SomeImpl o) {}\n" + 
						"}\n",
						"src/pm/Other.java", 
						"package pm;\n" +
								"import impl.SomeImpl;\n" + 
								"public class Other extends SomeImpl {\n" +
						"}\n"
					};
			IJavaProject p1 = setupModuleProject("mod.one", sources1);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			p1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

			String[] sources2 = {
						"src/module-info.java",
						"module mod.two { \n" +
						"	requires mod.one;\n" +
						"}",
						"src/impl/SomeImpl.java", 
						"package impl;\n" +
								"public class SomeImpl {\n" + // pseudo-conflict to same named, but inaccessible class from mod.one
						"}\n",
						"src/po/Client.java", 
						"package po;\n" + 
						"import pm.C1;\n" + 
						"import pm.Other;\n" +
						"import impl.SomeImpl;\n" + 
						"public class Client {\n" + 
						"    void test1(C1 one) {\n" +
						"		 SomeImpl impl = new SomeImpl();\n" + // our own version 
						"        one.m1(impl);\n" + // incompatible to what's required 
						"		 one.m1(new Other());\n" + // OK
						"    }\n" + 
						"}\n",
					};
			String expectedError = "The method m1(impl.SomeImpl) in the type C1 is not applicable for the arguments (impl.SomeImpl)";
			IJavaProject p2 = setupModuleProject("mod.two", sources2, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", expectedError, markers);

			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", expectedError, markers);
		} finally {
			deleteProject("mod.one");
			deleteProject("mod.two");
		}
	}
	
	public void testNonPublic1() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources1 = {
					"src/module-info.java", 
					"module mod.one { \n" +
					"	exports pm;\n" +
					"}",
					"src/pm/C1.java", 
					"package pm;\n" +
					"class C1 {\n" +
					"	public void test() {}\n" +
					"}\n"
			};
			IJavaProject p1 = setupModuleProject("mod.one", sources1);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			p1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

			String[] sources2 = {
					"src/module-info.java", 
					"module mod.two { \n" +
					"	requires mod.one;\n" +
					"}",
					"src/pm/sub/C2.java", 
					"package pm.sub;\n" +
					"class C2 {\n" +
					"	public void foo() {}\n" +
					"}\n",
					"src/po/Client.java", 
					"package po;\n" + 
					"import pm.*;\n" + // package is exported but type C1 is not public
					"public class Client {\n" + 
					"    void test1(C1 one) {\n" +
					"        one.test();\n" + 
					"    }\n" + 
					"}\n"
			};

			IJavaProject p2 = setupModuleProject("mod.two", sources2, new IClasspathEntry[] { dep });
			this.workingCopies = new ICompilationUnit[3];
			this.workingCopies[0] = getCompilationUnit("/mod.two/src/module-info.java").getWorkingCopy(this.wcOwner, null);
			this.workingCopies[1] = getCompilationUnit("/mod.two/src/pm/sub/C2.java").getWorkingCopy(this.wcOwner, null);
			this.problemRequestor.initialize(sources2[5].toCharArray());
			this.workingCopies[2] = getCompilationUnit("/mod.two/src/po/Client.java").getWorkingCopy(this.wcOwner, null);
			assertProblems(
					"Unexpected problems",
					"----------\n" + 
					"1. ERROR in /mod.two/src/po/Client.java (at line 4)\n" + 
					"	void test1(C1 one) {\n" + 
					"	           ^^\n" + 
					"The type C1 is not visible\n" + 
					"----------\n" + 
					"2. ERROR in /mod.two/src/po/Client.java (at line 5)\n" + 
					"	one.test();\n" + 
					"	^^^\n" + 
					"The type C1 is not visible\n" + 
					"----------\n",
					this.problemRequestor);

			String expectedError = "The type C1 is not visible\n" + 
									"The type C1 is not visible";
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", expectedError, markers);

			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", expectedError, markers);
		} finally {
			deleteProject("mod.one");
			deleteProject("mod.two");
		}
	}

	// sort by CHAR_START
	protected void sortMarkers(IMarker[] markers) {
		Arrays.sort(markers, (a,b) -> a.getAttribute(IMarker.CHAR_START, 0) - b.getAttribute(IMarker.CHAR_START, 0)); 
	}
	public void tearDownSuite() throws Exception {
		super.tearDownSuite();
		deleteProject("P1");
		System.setProperty("modules.on.demand", "");
	}
}
