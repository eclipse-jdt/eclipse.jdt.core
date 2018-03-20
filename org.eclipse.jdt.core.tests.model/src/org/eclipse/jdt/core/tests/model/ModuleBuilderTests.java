/*******************************************************************************
 * Copyright (c) 2016, 2018 IBM Corporation and others.
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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.util.JRTUtil;
import org.eclipse.jdt.internal.core.ClasspathAttribute;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.builder.ClasspathJrt;
import org.eclipse.jdt.internal.core.util.Messages;

import junit.framework.Test;

public class ModuleBuilderTests extends ModifyingResourceTests {
	public ModuleBuilderTests(String name) {
		super(name);
	}

	static {
//		 TESTS_NAMES = new String[] { "test_conflicting_packages" };
	}
	private String sourceWorkspacePath = null;
	protected ProblemRequestor problemRequestor;
	public static Test suite() {
		return buildModelTestSuite(ModuleBuilderTests.class, BYTECODE_DECLARATION_ORDER);
	}
	public String getSourceWorkspacePath() {
		return this.sourceWorkspacePath == null ? super.getSourceWorkspacePath() : this.sourceWorkspacePath;
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
		System.setProperty("modules.to.load", "java.base;java.desktop;java.rmi;java.sql;");
		this.currentProject = createJava9Project("P1");
		this.createFile("P1/src/module-info.java", "");
		this.createFolder("P1/src/com/greetings");
		this.createFile("P1/src/com/greetings/Main.java", "");
		waitForManualRefresh();
		waitForAutoBuild();
	}
	
	public void tearDownSuite() throws Exception {
		super.tearDownSuite();
		deleteProject("P1");
		System.setProperty("modules.to.load", "");
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
			assertMarkers("Unexpected markers",
					// just an API leak warning:
					"The type Connection from module java.sql may not be accessible to clients due to missing \'requires transitive\'",
					markers);
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
		IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
		IClasspathEntry projectEntry = JavaCore.newProjectEntry(new Path("/P1"), null, false,
			new IClasspathAttribute[] {modAttr},
			true);
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
		IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
		IClasspathEntry projectEntry = JavaCore.newProjectEntry(new Path("/P2"), null, false,
			new IClasspathAttribute[] {modAttr},
			true);
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
			assertEquals(project.getOption("org.eclipse.jdt.core.compiler.compliance", true), "9");
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
			assertStringsEqual("incorrect result", new String[]{"java.desktop", "java.rmi", "java.sql"}, modules);
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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
				"	requires transitive other.mod;\n" +
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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
	public void _test_ModuleSourcePath_update() throws CoreException {
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
					"The type org.astro.World is not accessible\n" +
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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

	public void test_Opens_Nonexistent_Package() throws CoreException {
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
			assertNoErrors();
		} finally {
			deleteProject("com.greetings");
		}
	}
	public void test_Opens_Alien_Package() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] src = new String[] {
				"src/module-info.java",
				"module org.astro {}",
				"src/org/astro/World.java",
				"package org.astro;\n" + 
				"public interface World {\n" + 
				"    public String name();\n" + 
				"}\n"
			};
			IJavaProject p1 = setupModuleProject("org.astro", src);
			src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	opens org.astro;\n" +
				"}",
				"src/test/Test.java",
				"package test;\n" +
				"public class Test {\n" +
				"	org.astro.World w = null;\n" +
				"}"
			};
			IClasspathAttribute modAttr = new ClasspathAttribute(IClasspathAttribute.MODULE, "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false, new IClasspathAttribute[] {modAttr}, false);
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] {dep});
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers",	
					"The type org.astro.World is not accessible\n" +
					"The package org.astro does not exist or is empty",  markers);
		} finally {
			deleteProject("org.astro");
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
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
	// test that two packages with the same name result in conflict if they are both
	// accessible to a module
	public void test_conflicting_packages() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module some.mod {\n" +
				"	exports org.astro;\n" +
				"}",
				"src/org/astro/Test.java",
				"package org.astro;\n" +
				"public class Test { }"
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
					// reported against both 'requires' directives & against the import:
					"The package org.astro is accessible from more than one module: org.astro, some.mod\n" +
					"The package org.astro is accessible from more than one module: org.astro, some.mod\n" +
					"The package org.astro is accessible from more than one module: org.astro, some.mod",
					markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("some.mod");
			deleteProject("com.greetings");
		}
	}
	// test that a package declared in a module conflicts with an accessible package
	// of the same name declared in another required module
	public void test_conflicting_packages_declaredvsaccessible() throws CoreException {
		if (!isJRE9) return;
		try {
			IClasspathEntry dep = JavaCore.newContainerEntry(new Path(JavaCore.MODULE_PATH_CONTAINER_ID));
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
			setupModuleProject("org.astro", sources, new IClasspathEntry[]{dep});
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	exports com.greetings;\n" +
				"}",
				"src/org/astro/Test.java",
				"package org.astro;\n" +
				"public class Test {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"public class MyWorld implements org.astro.World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", 
					"The package org.astro conflicts with a package accessible from another module: org.astro",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	// accessible package bundle.org contains type astro
	// accessible package bundle.org.astro contains type World
	// Type bundle.org.astro.World should not be resolved, because type
	// bundle.org.astro trumps package bundle.org.astro
	public void test_conflict_packagevstype() throws CoreException {
		if (!isJRE9) return;
		try {
			IClasspathEntry dep = JavaCore.newContainerEntry(new Path(JavaCore.MODULE_PATH_CONTAINER_ID));
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports bundle.org.astro;\n" + 
				"}",
				"src/bundle/org/astro/World.java",
				"package bundle.org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			setupModuleProject("org.astro", sources, new IClasspathEntry[]{dep});
			sources = new String[] {
					"src/module-info.java",
					"module other.mod {\n" +
					"	exports bundle.org;\n" + 
					"}",
					"src/bundle/org/astro.java",
					"package bundle.org;\n" +
					"public class astro {}"
				};
			setupModuleProject("other.mod", sources, new IClasspathEntry[]{dep});
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	requires other.mod;\n" +
				"	exports com.greetings;\n" +
				"}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"public class MyWorld implements bundle.org.astro.World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", 
					"bundle.org.astro.World cannot be resolved to a type",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("other.mod");
			deleteProject("com.greetings");
		}
	}
	// package bundle.org contains type astro, but the package is not accessible
	// accessible package bundle.org.astro contains type World
	// type bundle.org.astro.World should be resolved because type bundle.org.astro
	// cannot be seen
	// TODO - to be confirmed with spec
	public void test_noconflict_concealedtype_accessiblepackage() throws CoreException {
		if (!isJRE9) return;
		try {
			IClasspathEntry dep = JavaCore.newContainerEntry(new Path(JavaCore.MODULE_PATH_CONTAINER_ID));
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports bundle.org.astro;\n" + 
				"}",
				"src/bundle/org/astro/World.java",
				"package bundle.org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			setupModuleProject("org.astro", sources, new IClasspathEntry[]{dep});
			sources = new String[] {
					"src/module-info.java",
					"module other.mod {\n" +
					"}",
					"src/bundle/org/astro.java",
					"package bundle.org;\n" +
					"public class astro {}"
				};
			setupModuleProject("other.mod", sources, new IClasspathEntry[]{dep});
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	requires other.mod;\n" +
				"	exports com.greetings;\n" +
				"}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"public class MyWorld implements bundle.org.astro.World {\n" +
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
			deleteProject("other.mod");
			deleteProject("com.greetings");
		}
	}
	// test that two packages of the same name exported by two named modules result in
	// a conflict in the context of a non-modular project
	public void test_conflicting_packages_unnamed() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module some.mod {\n" +
				"	exports org.astro;\n" +
				"}",
				"src/org/astro/Test.java",
				"package org.astro;\n" +
				"public class Test { }"
			};
			IClasspathEntry dep = JavaCore.newContainerEntry(new Path(JavaCore.MODULE_PATH_CONTAINER_ID));
			IJavaProject p1 = setupModuleProject("some.mod", sources, new IClasspathEntry[]{dep});
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
			IJavaProject p2 = setupModuleProject("org.astro", sources, new IClasspathEntry[]{dep});
			String[] src = new String[] {
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"import org.astro.World;\n" +
				"public class MyWorld implements World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep1 = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
			IClasspathEntry dep2 = JavaCore.newProjectEntry(p2.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
			IJavaProject p3 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep1, dep2 });
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p3.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers", 
					"The package org.astro is accessible from more than one module: org.astro, some.mod",
					markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("some.mod");
			deleteProject("com.greetings");
		}
	}
	// test that a package declared in a non-modular project conflicts with a package with the same name
	// exported by a named module on it's build path
	public void test_conflict_unnamed_declaredvsexported() throws CoreException {
		if (!isJRE9) return;
		try {
			IClasspathEntry dep = JavaCore.newContainerEntry(new Path(JavaCore.MODULE_PATH_CONTAINER_ID));
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
			IJavaProject p1 = setupModuleProject("org.astro", sources, new IClasspathEntry[]{dep});
			String[] src = new String[] {
				"src/org/astro/Test.java",
				"package org.astro;\n" +
				"public class Test {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"public class MyWorld implements org.astro.World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep1 = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep1 });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", 
					"The package org.astro conflicts with a package accessible from another module: org.astro",
					markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	// test that a type in an accessible package trumps an accessible package with the same name
	// in the context of a non-modular project
	public void test_conflict_packagevstype_unnamed() throws CoreException {
		if (!isJRE9) return;
		try {
			IClasspathEntry dep = JavaCore.newContainerEntry(new Path(JavaCore.MODULE_PATH_CONTAINER_ID));
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports bundle.org.astro;\n" + 
				"}",
				"src/bundle/org/astro/World.java",
				"package bundle.org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources, new IClasspathEntry[]{dep});
			sources = new String[] {
					"src/module-info.java",
					"module other.mod {\n" +
					"	exports bundle.org;\n" + 
					"}",
					"src/bundle/org/astro.java",
					"package bundle.org;\n" +
					"public class astro {}"
				};
			IJavaProject p2 = setupModuleProject("other.mod", sources, new IClasspathEntry[]{dep});
			String[] src = new String[] {
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"public class MyWorld implements bundle.org.astro.World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep1 = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
			IClasspathEntry dep2 = JavaCore.newProjectEntry(p2.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
			IJavaProject p3 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep1, dep2 });
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p3.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", 
					"bundle.org.astro.World cannot be resolved to a type",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("other.mod");
			deleteProject("com.greetings");
		}
	}
	// test that a conflicting package does not cause an error when resolving a sub package name
	// when the sub package is accessible in the context of a non-modular project
	public void test_noconflict_subpkg_unnamed() throws CoreException {
		if (!isJRE9) return;
		try {
			IClasspathEntry dep = JavaCore.newContainerEntry(new Path(JavaCore.MODULE_PATH_CONTAINER_ID));
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports bundle.org.astro;\n" + 
				"}",
				"src/bundle/org/astro/World.java",
				"package bundle.org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources, new IClasspathEntry[]{dep});
			sources = new String[] {
					"src/module-info.java",
					"module other.mod {\n" +
					"	exports bundle.org;\n" + 
					"}",
					"src/bundle/org/astro.java",
					"package bundle.org;\n" +
					"public class astro {}"
				};
			IJavaProject p2 = setupModuleProject("other.mod", sources, new IClasspathEntry[]{dep});
			String[] src = new String[] {
				"src/bundle/org/Test.java",
				"package bundle.org;\n" +
				"public class Test {}",
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"public class MyWorld implements bundle.org.astro.World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep1 = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
			IClasspathEntry dep2 = JavaCore.newProjectEntry(p2.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
			IJavaProject p3 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep1, dep2 });
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p3.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", 
					"The package bundle.org conflicts with a package accessible from another module: other.mod\n" + 
					"bundle.org.astro.World cannot be resolved to a type",
					markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("other.mod");
			deleteProject("com.greetings");
		}
	}
	// test that a type in a non-accessible package does not conflict with an accessible package
	// in the context of a non-modular project
	public void test_noconflict_concealedtype_accessiblepackage_unnamed() throws CoreException {
		if (!isJRE9) return;
		try {
			IClasspathEntry dep = JavaCore.newContainerEntry(new Path(JavaCore.MODULE_PATH_CONTAINER_ID));
			String[] sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports bundle.org.astro;\n" + 
				"}",
				"src/bundle/org/astro/World.java",
				"package bundle.org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", sources, new IClasspathEntry[]{dep});
			sources = new String[] {
					"src/module-info.java",
					"module other.mod {\n" +
					"}",
					"src/bundle/org/astro.java",
					"package bundle.org;\n" +
					"public class astro {}"
				};
			IJavaProject p2 = setupModuleProject("other.mod", sources, new IClasspathEntry[]{dep});
			String[] src = new String[] {
				"src/com/greetings/MyWorld.java",
				"package com.greetings;\n" +
				"public class MyWorld implements bundle.org.astro.World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep1 = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
			IClasspathEntry dep2 = JavaCore.newProjectEntry(p2.getPath(), null, false,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
			IJavaProject p3 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep1, dep2 });
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p3.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",	"",  markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("other.mod");
			deleteProject("com.greetings");
		}
	}
	public void testBug512053() throws CoreException, IOException {
		if (!isJRE9) return;
		Hashtable<String, String> javaCoreOptions = JavaCore.getOptions();
		this.sourceWorkspacePath = super.getSourceWorkspacePath() + java.io.File.separator + "bug512053"; 
		try {
			setUpJavaProject("bundle.test.a.callable", "9");
			setUpJavaProject("bundle.test.a", "9");
			setUpJavaProject("bundle.test.b", "9");
			setUpJavaProject("jpms.test.a", "9");
			setUpJavaProject("jpms.test.b", "9");
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();
			//assertNoErrors(p2);
		} finally {
			this.deleteProject("bundle.test.a.callable");
			this.deleteProject("bundle.test.a");
			this.deleteProject("bundle.test.b");
			this.deleteProject("jpms.test.a");
			this.deleteProject("jpms.test.b");
			this.sourceWorkspacePath = null;
			 JavaCore.setOptions(javaCoreOptions);
		}
	}
	// basic test for automatic modules - external jars
	public void testBug518280() throws CoreException, IOException {
		if (!isJRE9) return;
		try {
			String libPath = "externalLib/test.jar";
			Util.createJar(
					new String[] {
						"test/src/org/astro/World.java", //$NON-NLS-1$
						"package org.astro;\n" +
						"public interface World {\n" +
						"	public String name();\n" +
						"}",
					},
					null,
					new HashMap<>(),
					null,
					getExternalResourcePath(libPath));
			String[] src = new String[] { 
					"src/module-info.java",
					"module com.greetings {\n" +
					"	requires test;\n" +
					"	exports com.greetings;\n" +
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
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newLibraryEntry(new Path(getExternalResourcePath(libPath)), null, null, ClasspathEntry.NO_ACCESS_RULES,
					new IClasspathAttribute[] {modAttr},
					false/*not exported*/);
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });

			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
			deleteExternalResource("externalLib");
			this.deleteProject("com.greetings");
		}
	}
	// basic test for automatic modules - workspace jars
	public void testBug518282() throws CoreException, IOException {
		if (!isJRE9) return;
		Hashtable<String, String> javaCoreOptions = JavaCore.getOptions();
		try {
			setUpJavaProject("test_automodules", "9");
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();
		} finally {
			this.deleteProject("test_automodules");
			JavaCore.setOptions(javaCoreOptions);
		}
	}
	// Only the project using a jar as an automatic module should be able to
	// resolve one as such
	public void testBug518282a() throws CoreException, IOException {
		if (!isJRE9) return;
		Hashtable<String, String> javaCoreOptions = JavaCore.getOptions();
		try {
			IJavaProject p1 = setUpJavaProject("test_automodules", "9");
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();
			String[] src = new String[] { 
					"src/module-info.java",
					"module com.greetings {\n" +
					"	requires junit; // This should not be resolved\n" +
					"	exports com.greetings;\n" +
					"}",
					"src/com/greetings/Test.java",
					"package com.greetings;\n" +
					"public class Test {\n" +
					"	public String name() {\n" +
					"		return \" My World!!\";\n" +
					"	}\n" +
					"}"
			};
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "false");
			IClasspathEntry dep = JavaCore.newLibraryEntry(p1.getProject().findMember("lib/junit.jar").getFullPath(), null, null,
					ClasspathEntry.NO_ACCESS_RULES,
					new IClasspathAttribute[] {modAttr},
					false/*not exported*/);
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });

			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", 
					"junit cannot be resolved to a module", markers);
		} finally {
			this.deleteProject("test_automodules");
			this.deleteProject("com.greetings");
			JavaCore.setOptions(javaCoreOptions);
		}
	}
	// A modular jar on the module path of a project should behave as a regular module and not
	// as an automatic module
	public void testBug518282b() throws CoreException, IOException {
		if (!isJRE9) return;
		Hashtable<String, String> javaCoreOptions = JavaCore.getOptions();
		String libPath = "externalLib/test.jar";
		try {
			String[] src = new String[] { 
					"src/module-info.java",
					"module com.greetings {\n" +
					"	exports com.greetings;\n" +
					"}",
					"src/com/greetings/Test.java",
					"package com.greetings;\n" +
					"public class Test {\n" +
					"	public String name() {\n" +
					"		return \" My World!!\";\n" +
					"	}\n" +
					"}"
			};
			IJavaProject p1 = setupModuleProject("test", src);
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			File rootDir = new File(p1.getProject().findMember("bin").getLocation().toString());
			Util.zip(rootDir, getExternalResourcePath(libPath));
			src = new String[] { 
					"src/module-info.java",
					"module test_automodules {\n" +
					"	requires com.greetings;\n" +
					"}",
					"src/test/Main.java",
					"package test;\n" +
					"import com.greetings.Test;\n" +
					"public class Main {\n" +
					"	public static void main(String[] args) {\n" +
					"		System.out.println(new Test().name());\n" +
					"	}\n" +
					"}"
			};
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newLibraryEntry(new Path(getExternalResourcePath(libPath)), null, null,
				ClasspathEntry.NO_ACCESS_RULES,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
			IJavaProject p2 = setupModuleProject("test_automodules", src, new IClasspathEntry[] {dep});
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
			this.deleteProject("test");
			this.deleteProject("test_automodules");
			deleteExternalResource(libPath);
			JavaCore.setOptions(javaCoreOptions);
		}
	}
	// A modular jar on the class path of a module project - shouldn't be
	// treated as a module and shouldn't be readable
	public void testBug518282c() throws CoreException, IOException {
		if (!isJRE9) return;
		Hashtable<String, String> javaCoreOptions = JavaCore.getOptions();
		String libPath = "externalLib/test.jar";
		try {
			String[] src = new String[] { 
					"src/module-info.java",
					"module test {\n" +
					"	exports com.greetings;\n" +
					"}",
					"src/com/greetings/Test.java",
					"package com.greetings;\n" +
					"public class Test {\n" +
					"	public String name() {\n" +
					"		return \" My World!!\";\n" +
					"	}\n" +
					"}"
			};
			IJavaProject p1 = setupModuleProject("test", src);
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			File rootDir = new File(p1.getProject().findMember("bin").getLocation().toString());
			Util.zip(rootDir, getExternalResourcePath(libPath));
			src = new String[] { 
					"src/module-info.java",
					"module test_automodules {\n" +
					"	requires test;\n" +
					"}",
					"src/test/Main.java",
					"package test;\n" +
					"import com.greetings.Test;\n" +
					"public class Main {\n" +
					"	public static void main(String[] args) {\n" +
					"		System.out.println(new Test().name());\n" +
					"	}\n" +
					"}"
			};
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "false");
			IClasspathEntry dep = JavaCore.newLibraryEntry(new Path(getExternalResourcePath(libPath)), null, null,
				ClasspathEntry.NO_ACCESS_RULES,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
			IJavaProject p2 = setupModuleProject("test_automodules", src, new IClasspathEntry[] {dep});
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertTrue("Compilation succeeds unexpectedly", markers.length > 0);
		} finally {
			this.deleteProject("test");
			this.deleteProject("test_automodules");
			deleteExternalResource(libPath);
			JavaCore.setOptions(javaCoreOptions);
		}
	}
	// An automatic module grants implied readability to all other automatic modules
	public void testBug518282d() throws CoreException, IOException {
		if (!isJRE9) return;
		Hashtable<String, String> javaCoreOptions = JavaCore.getOptions();
		String libPath = "externalLib/test.jar";
		try {
			String[] src = new String[] { 
					"src/org/astro/World.java",
					"package org.astro;\n" +
					"public interface World {\n" +
					"	public String name();\n" +
					"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", src);
			src = new String[] { 
				"src/org/greetings/Test.java",
				"package org.greetings;\n" +
				"import  org.astro.World;\n" +
				"public class Test implements World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			IJavaProject p2 = setupModuleProject("test", src, new IClasspathEntry[] {dep});
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			File rootDir = new File(p2.getProject().findMember("bin").getLocation().toString());
			Util.zip(rootDir, getExternalResourcePath(libPath));
			src = new String[] { 
				"src/module-info.java",
				"module test_automodules {\n" +
				"	requires test;\n" +
				"}",
				"src/test/Main.java",
				"package test;\n" +
				"import org.greetings.Test;\n" +
				"public class Main {\n" +
				"	public static void main(String[] args) {\n" +
				"		org.astro.World world = new Test();\n" +
				"		System.out.println(world.name());\n" +
				"	}\n" +
				"}"
			};
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			dep = JavaCore.newLibraryEntry(new Path(getExternalResourcePath(libPath)), null, null,
				ClasspathEntry.NO_ACCESS_RULES,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
			IClasspathEntry dep2 = JavaCore.newLibraryEntry(p1.getProject().findMember("bin").getFullPath(), null, null,
				ClasspathEntry.NO_ACCESS_RULES,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
			IJavaProject p3 = setupModuleProject("test_automodules", src, new IClasspathEntry[] {dep, dep2});
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p3.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
			this.deleteProject("test");
			this.deleteProject("test_automodules");
			this.deleteProject("org.astro");
			deleteExternalResource(libPath);
			JavaCore.setOptions(javaCoreOptions);
		}
	}
	// Automatic module should not allow access to other explicit modules without
	// requires
	public void testBug518282e() throws CoreException, IOException {
		if (!isJRE9) return;
		Hashtable<String, String> javaCoreOptions = JavaCore.getOptions();
		String libPath = "externalLib/test.jar";
		try {
			String[] src = new String[] { 
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
			IJavaProject p1 = setupModuleProject("org.astro", src);
			src = new String[] { 
				"src/com/greetings/Test.java",
				"package com.greetings;\n" +
				"import  org.astro.World;\n" +
				"public class Test implements World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			IJavaProject p2 = setupModuleProject("test", src, new IClasspathEntry[] {dep});
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			File rootDir = new File(p2.getProject().findMember("bin").getLocation().toString());
			Util.zip(rootDir, getExternalResourcePath(libPath));
			src = new String[] { 
				"src/module-info.java",
				"module test_automodules {\n" +
				"	requires test;\n" +
				"}",
				"src/test/Main.java",
				"package test;\n" +
				"import com.greetings.Test;\n" +
				"import org.astro.*;\n" +
				"public class Main {\n" +
				"	public static void main(String[] args) {\n" +
				"		World world = new Test();\n" +
				"		System.out.println(world.name());\n" +
				"	}\n" +
				"}"
			};
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			dep = JavaCore.newLibraryEntry(new Path(getExternalResourcePath(libPath)), null, null,
				ClasspathEntry.NO_ACCESS_RULES,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
			modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep2 = JavaCore.newProjectEntry(p1.getPath(), null, true,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
			IJavaProject p3 = setupModuleProject("test_automodules", src, new IClasspathEntry[] {dep, dep2});
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p3.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers", 
					"The package org.astro is not accessible\n" +
					"World cannot be resolved to a type", markers);
		} finally {
			this.deleteProject("test");
			this.deleteProject("test_automodules");
			this.deleteProject("org.astro");
			deleteExternalResource(libPath);
			JavaCore.setOptions(javaCoreOptions);
		}
	}
	// An automatic module shouldn't allow access to classpath
	public void testBug518282f() throws CoreException, IOException {
		if (!isJRE9) return;
		Hashtable<String, String> javaCoreOptions = JavaCore.getOptions();
		String libPath = "externalLib/test.jar";
		try {
			String[] src = new String[] { 
					"src/org/astro/World.java",
					"package org.astro;\n" +
					"public interface World {\n" +
					"	public String name();\n" +
					"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", src);
			src = new String[] { 
				"src/com/greetings/Test.java",
				"package com.greetings;\n" +
				"import  org.astro.World;\n" +
				"public class Test implements World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			IJavaProject p2 = setupModuleProject("test", src, new IClasspathEntry[] {dep});
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			File rootDir = new File(p2.getProject().findMember("bin").getLocation().toString());
			Util.zip(rootDir, getExternalResourcePath(libPath));
			src = new String[] { 
				"src/module-info.java",
				"module test_automodules {\n" +
				"	requires test;\n" +
				"}",
				"src/test/Main.java",
				"package test;\n" +
				"import com.greetings.Test;\n" +
				"public class Main {\n" +
				"	public static void main(String[] args) {\n" +
				"		org.astro.World world = new Test();\n" +
				"		System.out.println(world.name());\n" +
				"	}\n" +
				"}"
			};
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			dep = JavaCore.newLibraryEntry(new Path(getExternalResourcePath(libPath)), null, null,
				ClasspathEntry.NO_ACCESS_RULES,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
			modAttr = new ClasspathAttribute("module", "false");
			IClasspathEntry dep2 = JavaCore.newLibraryEntry(p1.getProject().findMember("bin").getFullPath(), null, null,
				ClasspathEntry.NO_ACCESS_RULES,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
			IJavaProject p3 = setupModuleProject("test_automodules", src, new IClasspathEntry[] {dep, dep2});
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p3.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"The project was not built since its build path is incomplete. Cannot find the class file for org.astro.World. Fix the build path then try building this project\n" + 
					"The type org.astro.World cannot be resolved. It is indirectly referenced from required .class files",
					markers);
		} finally {
			this.deleteProject("test");
			this.deleteProject("test_automodules");
			this.deleteProject("org.astro");
			deleteExternalResource(libPath);
			JavaCore.setOptions(javaCoreOptions);
		}
	}

	public void testUnnamedModule_bug519674() throws CoreException {
		if (!isJRE9) return;
		try {
			IJavaProject p1 = createJava9Project("Project1");
			createFolder("/Project1/src/pack1");
			createFile("/Project1/src/pack1/Class1.java",
					"package pack1;\n" +
					"public class Class1 {}\n");
			
			IJavaProject p2 = createJava9Project("Project2");
			{
				IClasspathEntry[] old = p2.getRawClasspath();
				IClasspathEntry[] newPath = new IClasspathEntry[old.length + 1];
				System.arraycopy(old, 0, newPath, 0, old.length);
				newPath[old.length] = JavaCore.newProjectEntry(p1.getPath());
				p2.setRawClasspath(newPath, null);
			}
			createFolder("/Project2/src/pack2");
			createFile("/Project2/src/pack2/Class2.java",
					"package pack2;\n" +
					"import pack1.Class1;\n" +
					"public class Class2 extends Class1 {}\n");
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
			this.deleteProject("Project1");
			this.deleteProject("Project2");
		}

	}
	public void testBug520246() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] src = new String[] { 
				"src/module-info.java",
				"module test_automodules {\n" +
				"	requires java.sql;\n" +
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"import some.pack.Type;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", src);
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "The import some cannot be resolved", markers);
		} finally {
			this.deleteProject("org.astro");
		}

	}	
	public void testBug520147() throws CoreException, IOException {
		if (!isJRE9) return;
		Hashtable<String, String> javaCoreOptions = JavaCore.getOptions();
		try {
			String[] src = new String[] { 
					"src/module-info.java",
					"module org.astro {\n" +
					"	exports org.astro;\n" +
					"}",
					"src/bundle/org/SomeClass.java",
					"package bundle.org;\n" +
					"public class SomeClass {}",
					"src/org/astro/World.java",
					"package org.astro;\n" +
					"public interface World {\n" +
					"	public String name();\n" +
					"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", src);
			src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
					"	requires org.astro;\n" +
					"	exports bundle.org;\n" +
					"}",
				"src/bundle/org/SomeWorld.java",
				"package bundle.org;\n" +
				"import  org.astro.World;\n" +
				"public class SomeWorld implements World {\n" +
				"	public String name() {\n" +
				"		return \" Some World!!\";\n" +
				"	}\n" +
				"}"
			};
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false, new IClasspathAttribute[] {modAttr}, false);
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] {dep});
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			src = new String[] { 
				"src/module-info.java",
				"module test {\n" +
				"	exports test;\n" +
				"	requires org.astro;\n" +
				"	requires com.greetings;\n" +
				"}",
				"src/test/Main.java",
				"package test;\n" +
				"import bundle.org.SomeWorld;\n" +
				"public class Main {\n" +
				"	public static void main(String[] args) {\n" +
				"		org.astro.World world = new SomeWorld();\n" +
				"		System.out.println(world.name());\n" +
				"	}\n" +
				"}"
			};
			IClasspathEntry dep2 = JavaCore.newProjectEntry(p2.getPath(), null, false, new IClasspathAttribute[] {modAttr}, false);
			IJavaProject p3 = setupModuleProject("test", src, new IClasspathEntry[] {dep, dep2});
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p3.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
			this.deleteProject("test");
			this.deleteProject("com.greetings");
			this.deleteProject("org.astro");
			JavaCore.setOptions(javaCoreOptions);
		}
	}	
	public void testBug520147a() throws CoreException, IOException {
		if (!isJRE9) return;
		Hashtable<String, String> javaCoreOptions = JavaCore.getOptions();
		try {
			String[] src = new String[] { 
					"src/module-info.java",
					"module org.astro {\n" +
					"	exports org.astro;\n" +
					"}",
					"src/bundle/org/SomeClass.java",
					"package bundle.org;\n" +
					"public class SomeClass {}",
					"src/org/astro/World.java",
					"package org.astro;\n" +
					"public interface World {\n" +
					"	public String name();\n" +
					"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", src);
			src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
					"	requires org.astro;\n" +
					"	exports bundle.org;\n" +
					"}",
				"src/bundle/org/SomeWorld.java",
				"package bundle.org;\n" +
				"import  org.astro.World;\n" +
				"public class SomeWorld implements World {\n" +
				"	public String name() {\n" +
				"		return \" Some World!!\";\n" +
				"	}\n" +
				"}"
			};
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false, new IClasspathAttribute[] {modAttr}, false);
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] {dep});
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			src = new String[] { 
				"src/module-info.java",
				"module test {\n" +
				"	exports test;\n" +
				"	requires com.greetings;\n" +
				"}",
				"src/test/Main.java",
				"package test;\n" +
				"import bundle.org.SomeWorld;\n" +
				"public class Main {\n" +
				"	public static void main(String[] args) {\n" +
				"		org.astro.World world = new SomeWorld();\n" +
				"		System.out.println(world.name());\n" +
				"	}\n" +
				"}"
			};
			IClasspathEntry dep2 = JavaCore.newProjectEntry(p2.getPath(), null, false, new IClasspathAttribute[] {modAttr}, false);
			IJavaProject p3 = setupModuleProject("test", src, new IClasspathEntry[] {dep, dep2});
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p3.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", 
					"The type org.astro.World is not accessible", markers);
		} finally {
			this.deleteProject("test");
			this.deleteProject("com.greetings");
			this.deleteProject("org.astro");
			JavaCore.setOptions(javaCoreOptions);
		}
	}
	public void testBug520147b() throws CoreException, IOException {
		if (!isJRE9) return;
		Hashtable<String, String> javaCoreOptions = JavaCore.getOptions();
		try {
			String[] src = new String[] { 
					"src/module-info.java",
					"module org.astro {\n" +
					"	exports org.astro;\n" +
					"	exports bundle.org to com.greetings;\n" +
					"}",
					"src/bundle/org/SomeClass.java",
					"package bundle.org;\n" +
					"public class SomeClass {}",
					"src/org/astro/World.java",
					"package org.astro;\n" +
					"public interface World {\n" +
					"	public String name();\n" +
					"}"
			};
			IJavaProject p1 = setupModuleProject("org.astro", src);
			src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
					"	requires org.astro;\n" +
					"	exports bundle.org;\n" +
					"}",
				"src/bundle/org/SomeWorld.java",
				"package bundle.org;\n" +
				"import  org.astro.World;\n" +
				"public class SomeWorld implements World {\n" +
				"	public String name() {\n" +
				"		return \" Some World!!\";\n" +
				"	}\n" +
				"}"
			};
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false, new IClasspathAttribute[] {modAttr}, false);
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] {dep});
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			src = new String[] { 
				"src/module-info.java",
				"module test {\n" +
				"	exports test;\n" +
				"	requires org.astro;\n" +
				"	requires com.greetings;\n" +
				"}",
				"src/test/Main.java",
				"package test;\n" +
				"import bundle.org.SomeWorld;\n" +
				"public class Main {\n" +
				"	public static void main(String[] args) {\n" +
				"		org.astro.World world = new SomeWorld();\n" +
				"		System.out.println(world.name());\n" +
				"	}\n" +
				"}"
			};
			IClasspathEntry dep2 = JavaCore.newProjectEntry(p2.getPath(), null, false, new IClasspathAttribute[] {modAttr}, false);
			IJavaProject p3 = setupModuleProject("test", src, new IClasspathEntry[] {dep, dep2});
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p3.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
			this.deleteProject("test");
			this.deleteProject("com.greetings");
			this.deleteProject("org.astro");
			JavaCore.setOptions(javaCoreOptions);
		}
	}
	public void testSourceFolders_Bug519673() throws CoreException {
		if (!isJRE9) return;
		try {
			// Setup project PSources1:
			String[] src = new String[] { 
					"src/module-info.java",
					"module PSources1 {\n" +
					"	//exports p.q;\n" +
					"}",
					"src2/p/q/SomeClass.java",
					"package p.q;\n" +
					"public class SomeClass {}",
			};
			IJavaProject p1 = setupModuleProject("PSources1", new String[] { "src", "src2" }, src, new IClasspathEntry[0]);
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

			// Edit PSources1/src/module-info.java:
			String infoSrc =
					"module PSources1 {\n" +
					"	exports p.q;\n" +
					"}";
			String infoPath = "/PSources1/src/module-info.java";
			editFile(infoPath, infoSrc);
			this.workingCopies = new ICompilationUnit[1];
			char[] sourceChars = src[1].toCharArray();
			this.problemRequestor.initialize(sourceChars);
			this.workingCopies[0] = getCompilationUnit(infoPath).getWorkingCopy(this.wcOwner, null);
			// was: ERROR: The package pkg does not exist or is empty
			assertProblems(
					"Unexpected problems",
					"----------\n" + 
					"----------\n",
					this.problemRequestor);

			// Setup project PClient2:
			String[] src2 = new String[] { 
					"src/module-info.java",
					"module PClient2 {\n" +
					"	requires PSources1;\n" +
					"}",
					"src/x/Client.java",
					"package x;\n" +
					"public class Client {\n" +
					"	p.q.SomeClass f;\n" +
					"\n}",
			};
			setupModuleProject("PClient2", src2);
			getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);

			// Edit PClient2/src/module-info.java:
			// was NPE in ModuleBinding.canAccess()
			char[] info2Chars = src2[2].toCharArray();
			this.problemRequestor.initialize(info2Chars);
			this.workingCopies[0] = getCompilationUnit("PClient2/src/module-info.java").getWorkingCopy(this.wcOwner, null);
			assertProblems(
					"Unexpected problems",
					"----------\n" + 
					"----------\n",
					this.problemRequestor);

			// Failed attempt to trigger NPE in ModuleBinding.isPackageExportedTo() by editing Client.java
			char[] source2Chars = src2[3].toCharArray();
			this.problemRequestor.initialize(source2Chars);
			this.workingCopies[0] = getCompilationUnit("PClient2/src/x/Client.java").getWorkingCopy(this.wcOwner, null);
			assertProblems(
					"Unexpected problems",
					"----------\n" + 
					"----------\n",
					this.problemRequestor);
		} finally {
			deleteProject("PSources1");
			deleteProject("PClient2");
		}
	}
	public void testPrivateMethod_Bug515985() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] src = new String[] {
					"src/module-info.java", 
					"module mod.one { \n" +
					"	exports pm;\n" +
					"}",
					"src/impl/Other.java", 
					"package impl;\n" +
					"public class Other {\n" +
					"    public void privateMethod() {}" + 
					"}\n",
					"src/pm/C1.java", 
					"package pm;\n" +
					"import impl.Other;\n" + 
					"public class C1 extends Other {\n" + 
					"}\n"
			};
			IJavaProject p1 = setupModuleProject("mod.one", src);
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
	
			String[] src2 = new String[] {
					"src/module-info.java", 
					"module mod.two { \n" +
					"	requires mod.one;\n" +
					"}",
					"src/po/Client.java", 
					"package po;\n" + 
					"import pm.C1;\n" + 
					"public class Client {\n" + 
					"    void test1(C1 one) {\n" + 
					"        one.privateMethod(); // ecj: The method privateMethod() is undefined for the type C1\n" + 
					"    }\n" + 
					"}\n"
			};
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false, new IClasspathAttribute[] {modAttr}, false);
			IJavaProject p2 = setupModuleProject("mod.two", src2, new IClasspathEntry[] {dep});
			getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
			deleteProject("mod.one");
			deleteProject("mod.two");
		}
	}
	public void testAddExports() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
					"src/module-info.java",
					"module morg.astro {\n" +
//					"	exports org.astro to com.greetings;\n" + // this export will be added via add-exports
					"}",
					"src/org/astro/World.java",
					"package org.astro;\n" +
					"public interface World {\n" +
					"	public String name();\n" +
					"}"
			};
			IJavaProject p1 = setupModuleProject("morg.astro", sources);
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathAttribute addExports = new ClasspathAttribute(IClasspathAttribute.ADD_EXPORTS, "morg.astro/org.astro=com.greetings");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
															new IClasspathAttribute[] {modAttr, addExports},
															false/*not exported*/);
			String[] src = new String[] {
					"src/module-info.java",
					"module com.greetings {\n" +
					"	requires morg.astro;\n" +
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
			deleteProject("morg.astro");
			deleteProject("com.greetings");
		}
	}
	public void testAddExports2() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
					"src/module-info.java",
					"module morg.astro {\n" +
//					"	exports org.astro to com.greetings;\n" + // this export will be added via add-exports
//					"	exports org.eclipse to com.greetings;\n" + // this export will be added via add-exports
					"}",
					"src/org/astro/World.java",
					"package org.astro;\n" +
					"public interface World {\n" +
					"	public String name();\n" +
					"}",
					"src/org/eclipse/Planet.java",
					"package org.eclipse;\n" +
					"public class Planet {}\n"
			};
			IJavaProject p1 = setupModuleProject("morg.astro", sources);
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathAttribute addExports = new ClasspathAttribute(IClasspathAttribute.ADD_EXPORTS,
						"morg.astro/org.astro=com.greetings:morg.astro/org.eclipse=com.greetings");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
															new IClasspathAttribute[] {modAttr, addExports},
															false/*not exported*/);
			String[] src = new String[] {
					"src/module-info.java",
					"module com.greetings {\n" +
					"	requires morg.astro;\n" +
					"	exports com.greetings;\n" +
					"}",
					"src/com/greetings/MyWorld.java",
					"package com.greetings;\n" +
					"import org.astro.World;\n" +
					"public class MyWorld implements World {\n" +
					"	org.eclipse.Planet planet;\n" +
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
			deleteProject("morg.astro");
			deleteProject("com.greetings");
		}
	}
	public void testAddReads() throws CoreException, IOException {
		if (!isJRE9) return;
		try {
			// org.astro defines the "real" org.astro.World:
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
			IJavaProject p = setupModuleProject("org.astro", sources);

			// build mod.one with a private copy of org.astro.World:
			String[] src1 = new String[] {
					"src/module-info.java",
					"module mod.one {\n" +
					"	exports one.p;\n" +
					"}\n",
					"src/org/astro/World.java",
					"package org.astro;\n" +
					"public interface World { public String name(); }\n",
					"src/one/p/C.java",
					"package one.p;\n" +
					"public class C {\n" +
					"	public void test(org.astro.World w) {\n" +
					"		System.out.println(w.name());\n" +
					"	}\n" +
					"}\n"
			};
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p.getPath(), null, false,
															new IClasspathAttribute[] { modAttr },
															false/*not exported*/);
			IJavaProject p1 = setupModuleProject("mod.one", src1, new IClasspathEntry[] { dep });
			p1.setOption(JavaCore.COMPILER_PB_API_LEAKS, JavaCore.IGNORE); // the stub org.astro.World is not exported but used in API
			p1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",	"",  markers);

			// jar-up without the private copy:
			deleteFile("/mod.one/src/org/astro/World.java");
			deleteFile("/mod.one/bin/org/astro/World.class");
			String modOneJarPath = getWorkspacePath()+File.separator+"mod.one.jar";
			Util.zip(new File(getWorkspacePath()+"/mod.one/bin"), modOneJarPath);

			// com.greetings depends on both other modules:
			String[] src2 = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires org.astro;\n" +
				"	requires mod.one;\n" +
				"}",
				"src/com/greetings/MyTest.java",
				"package com.greetings;\n" +
				"public class MyTest {\n" +
				"	public void test(one.p.C c, org.astro.World w) {\n" +
				"		c.test(w);\n" +
				"	}\n" +
				"}"
			};
			IClasspathEntry dep1 = JavaCore.newProjectEntry(p.getPath(), null, false,
															new IClasspathAttribute[] {new ClasspathAttribute("module", "true")},
															false/*not exported*/);
			// need to re-wire dependency mod.one -> org.astro for resolving one.p.C:
			IClasspathEntry dep2 = JavaCore.newLibraryEntry(new Path(modOneJarPath), null, null, null,
															new IClasspathAttribute[] {
																	new ClasspathAttribute("module", "true"),
																	new ClasspathAttribute(IClasspathAttribute.ADD_READS, "mod.one=org.astro")
															},
															false/*not exported*/);
			IJavaProject p2 = setupModuleProject("com.greetings", src2, new IClasspathEntry[] { dep1, dep2 });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",	"",  markers);

			// check that reconcile respects --add-reads, too:
			this.problemRequestor.reset();
			ICompilationUnit cu = getCompilationUnit("/com.greetings/src/com/greetings/MyTest.java");
			cu.getWorkingCopy(this.wcOwner, null);
			assertProblems(
				"Unexpected problems",
				"----------\n" +
				"----------\n",
				this.problemRequestor);

		} finally {
			deleteProject("mod.one");
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void testBug520147c() throws CoreException, IOException {
		if (!isJRE9) return;
		Hashtable<String, String> javaCoreOptions = JavaCore.getOptions();
		try {
			String[] src = new String[] { 
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
			IJavaProject p1 = setupModuleProject("org.astro", src);
			src = new String[] {
				"src/org/eclipse/pack1/SomeWorld.java",
				"package org.eclipse.pack1;\n" +
				"import  org.astro.World;\n" +
				"public class SomeWorld implements World {\n" +
				"	public String name() {\n" +
				"		return \" Some World!!\";\n" +
				"	}\n" +
				"}"
			};
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false, new IClasspathAttribute[] {modAttr}, false);
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] {dep});
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			src = new String[] { 
				"src/module-info.java",
				"module test {\n" +
				"	exports test;\n" +
				"	requires com.greetings;\n" +
				"	requires org.astro;\n" +
				"}",
				"src/test/Main.java",
				"package test;\n" +
				"import org.eclipse.pack1.SomeWorld;\n" +
				"public class Main {\n" +
				"	public static void main(String[] args) {\n" +
				"		org.astro.World world = new SomeWorld();\n" +
				"		System.out.println(world.name());\n" +
				"	}\n" +
				"}"
			};
			IClasspathEntry dep2 = JavaCore.newProjectEntry(p2.getPath(), null, false, new IClasspathAttribute[] {modAttr}, false);
			IJavaProject p3 = setupModuleProject("test", src, new IClasspathEntry[] {dep, dep2});
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p3.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
			this.deleteProject("test");
			this.deleteProject("com.greetings");
			this.deleteProject("org.astro");
			JavaCore.setOptions(javaCoreOptions);
		}
	}
	@Deprecated
	public void testBug519935() throws CoreException, IOException {
		if (!isJRE9) return;
		Hashtable<String, String> javaCoreOptions = JavaCore.getOptions();
		try {
			String[] src = new String[] { 
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
			IJavaProject p1 = setupModuleProject("org.astro", src);
			src = new String[] { 
				"src/org/eclipse/pack/Test.java",
				"package org.eclipse.pack;\n" +
				"import org.astro.World;\n" +
				"public class Test implements World {\n" +
				"	public String name() {\n" +
				"		return \" My World!!\";\n" +
				"	}\n" +
				"}"
			};
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath());
			IJavaProject p2 = setupModuleProject("test", src, new IClasspathEntry[] {dep});
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			src = new String[] { 
				"src/module-info.java",
				"module test_automodules {\n" +
				"	requires bin;\n" +
				"	requires org.astro;\n" +
				"}",
				"src/test/Main.java",
				"package test;\n" +
				"import org.eclipse.pack.Test;\n" +
				"import org.astro.*;\n" +
				"public class Main {\n" +
				"	public static void main(String[] args) {\n" +
				"		World world = new Test();\n" +
				"		System.out.println(world.name());\n" +
				"	}\n" +
				"}"
			};
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			dep = JavaCore.newLibraryEntry(p2.getProject().findMember("bin").getFullPath(), null, null,
				ClasspathEntry.NO_ACCESS_RULES,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
			modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep2 = JavaCore.newProjectEntry(p1.getPath(), null, true,
				new IClasspathAttribute[] {modAttr},
				false/*not exported*/);
			setupModuleProject("testSOE", src, new IClasspathEntry[] {dep, dep2});
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getCompilationUnit("/testSOE/src/test/Main.java").getWorkingCopy(this.wcOwner, null);
			this.problemRequestor.initialize(src[3].toCharArray());
			CompilationUnit unit = this.workingCopies[0].reconcile(AST.JLS9, true, this.wcOwner, null);
			assertNotNull("Could not reconcile", unit);
		} finally {
			this.deleteProject("test");
			this.deleteProject("testSOE");
			this.deleteProject("org.astro");
			JavaCore.setOptions(javaCoreOptions);
		}
	}
	@Deprecated
	public void testBug520310() throws CoreException, IOException {
		if (!isJRE9) return;
		try {
			String[] src = new String[] { 
				"src/module-info.java",
				"module mod.one {\n" +
//				"	requires mod.two;\n" +
				"	exports org.astro;\n" +
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public interface World {\n" +
				"	public String name();\n" +
				"}"
			};
			IClasspathEntry modDep = JavaCore.newContainerEntry(new Path(JavaCore.MODULE_PATH_CONTAINER_ID));
			IJavaProject p1 = setupModuleProject("mod.one", src, new IClasspathEntry[] {modDep});

			src = new String[] { 
					"src/module-info.java",
					"module mod.two {\n" +
					"	requires mod.one;\n" +
					"	exports test;\n" +
					"}",
					"src/test/Test.java",
					"package test;\n" +
					"import org.astro.World;\n" +
					"public class Test implements World {\n" +
					"	public String name() {\n" +
					"		return \" My World!!\";\n" +
					"	}\n" +
					"}"
			};
			IJavaProject p2 = setupModuleProject("mod.two", src, new IClasspathEntry[] {modDep});
			
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers in mod.one", "", markers);
			markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers in mod.two", "", markers);
			
			editFile("/mod.one/src/module-info.java",
				"module mod.one {\n" +
				"	requires mod.two;\n" + // added
				"	exports org.astro;\n" +
				"}");
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null); // modules see each other only on 2nd attempt, don't ask me...
			markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers in mod.one", 
					"Cycle exists in module dependencies, Module mod.one requires itself via mod.two",
					markers);
			markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers in mod.two", 
					"The import org cannot be resolved\n" + // cannot use cyclic requires 
					"Cycle exists in module dependencies, Module mod.two requires itself via mod.one\n" + 
					"World cannot be resolved to a type",
					markers);

			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getCompilationUnit("/mod.two/src/module-info.java").getWorkingCopy(this.wcOwner, null);
			this.problemRequestor.initialize(src[1].toCharArray());
			CompilationUnit unit = this.workingCopies[0].reconcile(AST.JLS9, true, this.wcOwner, null);
			assertNotNull("Could not reconcile", unit);
		} finally {
			this.deleteProject("mod.one");
			this.deleteProject("mod.two");
		}
	}
	public void testBug521346() throws CoreException, IOException {
		if (!isJRE9) return;
		IJavaProject javaProject = null;
		try {
			String src =
					"import java.*;\n" + 
					"public class X {\n" + 
					"    public static void main(String[] args) {\n" + 
					"        System.out.println(true);\n" + 
					"    }\n" + 
					"}";
			javaProject = createJava9Project("Test");
			this.problemRequestor.initialize(src.toCharArray());
			getWorkingCopy("/Test/src/X.java", src, true);
			assertProblems("should have not problems",
					"----------\n" + 
					"1. WARNING in /Test/src/X.java (at line 1)\n" + 
					"	import java.*;\n" + 
					"	       ^^^^\n" + 
					"The import java is never used\n" + 
					"----------\n",
					this.problemRequestor);
		} finally {
			if (javaProject != null)
				deleteProject(javaProject);
		}
	}
	public void testAutoModule1() throws Exception {
		if (!isJRE9) return;
		IJavaProject javaProject = null;
		try {
			String[] sources = {
				"p/a/X.java",
				"package p.a;\n" +
				"public class X {}\n;"
			};
			String outputDirectory = Util.getOutputDirectory();
	
			String jarPath = outputDirectory + File.separator + "lib-x.jar";
			Util.createJar(sources, jarPath, "1.8");
			
			javaProject = createJava9Project("mod.one", new String[] {"src"});
			IClasspathAttribute[] attributes = { JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true") };
			addClasspathEntry(javaProject, JavaCore.newLibraryEntry(new Path(jarPath), null, null, null, attributes, false));

			String srcMod =
				"module mod.one { \n" +
				"	requires lib.x;\n" + // lib.x is derived from lib-x.jar
				"}";
			createFile("/mod.one/src/module-info.java", 
				srcMod);
			createFolder("mod.one/src/q");
			String srcX =
				"package q;\n" +
				"public class X {\n" +
				"	p.a.X f;\n" +
				"}";
			createFile("/mod.one/src/q/X.java", srcX);
			
			this.problemRequestor.initialize(srcMod.toCharArray());
			getWorkingCopy("/mod.one/module-info.java", srcMod, true);
			assertProblems("module-info should have no problems",
					"----------\n" + 
					"----------\n",
					this.problemRequestor);

			this.problemRequestor.initialize(srcX.toCharArray());
			getWorkingCopy("/mod.one/src/q/X.java", srcX, true);
			assertProblems("X should have no problems",
					"----------\n" + 
					"----------\n",
					this.problemRequestor);

			javaProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();
		} finally {
			if (javaProject != null)
				deleteProject(javaProject);
		}
	}
	public void testAutoModule2() throws Exception {
		if (!isJRE9) return;
		IJavaProject javaProject = null;
		try {
			String[] sources = {
				"p/a/X.java",
				"package p.a;\n" +
				"public class X {}\n;",
			};
			String[] mfSource = {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" + 
				"Automatic-Module-Name: org.eclipse.lib.x\n"
			};
			String outputDirectory = Util.getOutputDirectory();

			String jarPath = outputDirectory + File.separator + "lib-x.jar";
			Util.createJar(sources, mfSource, jarPath, "1.8");
			
			javaProject = createJava9Project("mod.one", new String[] {"src"});
			IClasspathAttribute[] attributes = { JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true") };
			addClasspathEntry(javaProject, JavaCore.newLibraryEntry(new Path(jarPath), null, null, null, attributes, false));

			String srcMod =
				"module mod.one { \n" +
				"	requires org.eclipse.lib.x;\n" + // from jar attribute
				"}";
			createFile("/mod.one/src/module-info.java", 
				srcMod);
			createFolder("mod.one/src/q");
			String srcX =
				"package q;\n" +
				"public class X {\n" +
				"	p.a.X f;\n" +
				"}";
			createFile("/mod.one/src/q/X.java", srcX);

			this.problemRequestor.initialize(srcMod.toCharArray());
			getWorkingCopy("/mod.one/module-info.java", srcMod, true);
			assertProblems("module-info should have no problems",
					"----------\n" + 
					"----------\n",
					this.problemRequestor);

			this.problemRequestor.initialize(srcX.toCharArray());
			getWorkingCopy("/mod.one/src/q/X.java", srcX, true);
			assertProblems("X should have no problems",
					"----------\n" + 
					"----------\n",
					this.problemRequestor);

			javaProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();
		} finally {
			if (javaProject != null)
				deleteProject(javaProject);
		}
	}
	public void testAutoModule3() throws Exception {
		if (!isJRE9) return;
		IJavaProject javaProject = null, auto = null;
		try {
			auto = createJava9Project("auto", new String[] {"src"});
			createFolder("auto/src/p/a");
			createFile("auto/src/p/a/X.java",
				"package p.a;\n" +
				"public class X {}\n;");
			createFolder("auto/META-INF");
			createFile("auto/META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" + 
				"Automatic-Module-Name: org.eclipse.lib.x\n");

			javaProject = createJava9Project("mod.one", new String[] {"src"});
			IClasspathAttribute[] attributes = { JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true") };
			addClasspathEntry(javaProject, JavaCore.newProjectEntry(auto.getPath(), null, false, attributes, false));

			String srcMod =
				"module mod.one { \n" +
				"	requires org.eclipse.lib.x;\n" + // from manifest attribute
				"}";
			createFile("/mod.one/src/module-info.java", 
				srcMod);
			createFolder("mod.one/src/q");
			String srcX =
				"package q;\n" +
				"public class X {\n" +
				"	p.a.X f;\n" +
				"}";
			createFile("/mod.one/src/q/X.java", srcX);
			auto.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

			this.problemRequestor.initialize(srcMod.toCharArray());
			getWorkingCopy("/mod.one/module-info.java", srcMod, true);
			assertProblems("module-info should have no problems",
					"----------\n" + 
					"----------\n",
					this.problemRequestor);

			this.problemRequestor.initialize(srcX.toCharArray());
			getWorkingCopy("/mod.one/src/q/X.java", srcX, true);
			assertProblems("X should have no problems",
					"----------\n" + 
					"----------\n",
					this.problemRequestor);

			javaProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();
		} finally {
			if (javaProject != null)
				deleteProject(javaProject);
			if (auto != null)
				deleteProject(auto);
		}
	}

	public void testAutoModule4() throws Exception {
		if (!isJRE9) return;
		IJavaProject javaProject = null;
		IJavaProject javaProject2 = null;
		try {
			// auto module as jar:
			String[] sources = {
				"p/a/X.java",
				"package p.a;\n" +
				"public class X {}\n;",
			};
			String[] mfSource = {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" + 
				"Automatic-Module-Name: org.eclipse.lib.x\n" // automatic module reads all (incl. mod.one below)
			};
			String outputDirectory = Util.getOutputDirectory();

			String jarPath = outputDirectory + File.separator + "lib-x.jar";
			Util.createJar(sources, mfSource, jarPath, "1.8");
			
			// first source module:
			javaProject = createJava9Project("mod.one", new String[] {"src"});
			IClasspathAttribute[] attributes = { JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true") };
			addClasspathEntry(javaProject, JavaCore.newLibraryEntry(new Path(jarPath), null, null, null, attributes, false));

			String srcMod =
				"module mod.one { \n" +
				"	requires org.eclipse.lib.x;\n" + // creates cycle mod.one -> org.eclipse.lib.x -> mod.one
				"	exports p.q.api;\n" +
				"}";
			createFile("/mod.one/src/module-info.java", srcMod);
			createFolder("mod.one/src/p/q/api");
			String srcX =
				"package p.q.api;\n" +
				"public class X {\n" +
				"	p.a.X f;\n" +
				"}";
			createFile("/mod.one/src/p/q/api/X.java", srcX);

			// second source module:
			javaProject2 = createJava9Project("mod.two", new String[] {"src"});
			addClasspathEntry(javaProject2, JavaCore.newProjectEntry(new Path("/mod.one"), null, false, attributes, false));
			addClasspathEntry(javaProject2, JavaCore.newLibraryEntry(new Path(jarPath), null, null, null, attributes, false));
			String srcMod2 =
				"module mod.two { \n" +
				"	requires mod.one;\n" +
				"}";
			createFile("/mod.two/src/module-info.java", srcMod2);
			createFolder("mod.two/src/p/q");
			String srcY =
				"package p.q;\n" +
				"import p.q.api.X;\n" + // here we saw "The package p.q.api is accessible from more than one module: mod.one, mod.one"
				"public class Y {\n" +
				"	X f;\n" +
				"}";
			createFile("/mod.two/src/p/q/Y.java", srcY);

			this.problemRequestor.initialize(srcMod.toCharArray());
			getWorkingCopy("/mod.one/module-info.java", srcMod, true);
			assertProblems("module-info should have no problems",
					"----------\n" + 
					"----------\n",
					this.problemRequestor);

			this.problemRequestor.initialize(srcX.toCharArray());
			getWorkingCopy("/mod.one/src/p/q/api/X.java", srcX, true);
			assertProblems("X should have no problems",
					"----------\n" + 
					"----------\n",
					this.problemRequestor);

			this.problemRequestor.initialize(srcY.toCharArray());
			getWorkingCopy("/mod.two/src/p/q/Y.java", srcY, true);
			assertProblems("Y should have no problems",
					"----------\n" + 
					"----------\n",
					this.problemRequestor);

			javaProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertMarkers("markers in mod.one", "", javaProject);
			
			javaProject2.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertMarkers("markers in mod.two", "", javaProject2);

			javaProject.getProject().getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
			assertNoErrors();
		} finally {
			if (javaProject != null)
				deleteProject(javaProject);
			if (javaProject2 != null)
				deleteProject(javaProject2);
		}
	}
	// like testAutoModule3 without name derived from project, not manifest
	public void testAutoModule5() throws Exception {
		if (!isJRE9) return;
		IJavaProject javaProject = null, auto = null;
		try {
			auto = createJava9Project("auto", new String[] {"src"});
			createFolder("auto/src/p/a");
			createFile("auto/src/p/a/X.java",
				"package p.a;\n" +
				"public class X {}\n;");

			javaProject = createJava9Project("mod.one", new String[] {"src"});
			IClasspathAttribute[] attributes = { JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true") };
			addClasspathEntry(javaProject, JavaCore.newProjectEntry(auto.getPath(), null, false, attributes, false));

			String srcMod =
				"module mod.one { \n" +
				"	requires auto;\n" +
				"}";
			createFile("/mod.one/src/module-info.java", 
				srcMod);
			createFolder("mod.one/src/q");
			String srcX =
				"package q;\n" +
				"public class X {\n" +
				"	p.a.X f;\n" +
				"}";
			createFile("/mod.one/src/q/X.java", srcX);
			auto.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

			this.problemRequestor.initialize(srcMod.toCharArray());
			getWorkingCopy("/mod.one/module-info.java", srcMod, true);
			assertProblems("module-info should have no problems",
					"----------\n" + 
					"----------\n",
					this.problemRequestor);

			this.problemRequestor.initialize(srcX.toCharArray());
			getWorkingCopy("/mod.one/src/q/X.java", srcX, true);
			assertProblems("X should have no problems",
					"----------\n" + 
					"----------\n",
					this.problemRequestor);

			javaProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();
		} finally {
			if (javaProject != null)
				deleteProject(javaProject);
			if (auto != null)
				deleteProject(auto);
		}
	}

	// patch can see unexported type from host (and package accessible method), but not vice versa
	public void testPatch1() throws CoreException, IOException {
		if (!isJRE9) return;
		try {
			IJavaProject mainProject = createJava9Project("org.astro");
			String[] sources = { 
				"src/module-info.java",
				"module org.astro {\n" + // no exports
				"}",
				"src/org/astro/World.java",
				"package org.astro;\n" +
				"public class World {\n" +
				"	public String name() { return \"world\"; }\n" +
				"	void internalTest() { }\n" +
				"	public org.astro.test.WorldTest test;\n" +
				"}",
			};
			createSourceFiles(mainProject, sources);

			IJavaProject patchProject = createJava9Project("org.astro.patch");
			IClasspathAttribute[] attributes = {
						JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true"),
						JavaCore.newClasspathAttribute(IClasspathAttribute.PATCH_MODULE, "org.astro")
					};
			addClasspathEntry(patchProject, JavaCore.newProjectEntry(new Path("/org.astro"), null, false, attributes, false));
			String[] patchSources = {
				"src/org/astro/test/WorldTest.java",
				"package org.astro.test;\n" +
				"import org.astro.*;\n" +
				"public class WorldTest {\n" +
				"	void testWorld(World w) {\n" +
				"		w.name();\n" +
				"	}\n" +
				"}\n",
				"src/org/astro/Test2.java",
				"package org.astro;\n" +
				"class Test2 {\n" +
				"	void test(World w) {\n" +
				"		w.internalTest();\n" + // package access
				"	}\n" +
				"}\n"
			};
			createSourceFiles(patchProject, patchSources);
			
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = mainProject.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"org.astro.test cannot be resolved to a type", // missing reverse dependency
					markers);

			this.problemRequestor.reset();
			ICompilationUnit cu = getCompilationUnit("/org.astro.patch/src/org/astro/test/WorldTest.java");
			cu.getWorkingCopy(this.wcOwner, null);
			assertProblems(
				"Unexpected problems",
				"----------\n" +
				"----------\n",
				this.problemRequestor);

			this.problemRequestor.reset();
			cu = getCompilationUnit("/org.astro/src/org/astro/World.java");
			cu.getWorkingCopy(this.wcOwner, null);
			assertProblems(
				"Unexpected problems",
				"----------\n" + 
				"1. ERROR in /org.astro/src/org/astro/World.java\n" + 
				"org.astro.test cannot be resolved to a type\n" +
				"----------\n",
				this.problemRequestor);

		} finally {
			this.deleteProject("org.astro");
			this.deleteProject("org.astro.patch");
		}
	}

	// patch can see unexported type from host - JRE patched from two source folders
	public void testPatch2() throws CoreException, IOException {
		if (!isJRE9) return;
		try {
			IClasspathAttribute[] attributes = {
					JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true"),
					JavaCore.newClasspathAttribute(IClasspathAttribute.PATCH_MODULE, "java.base")
			};
			IJavaProject patchProject = createJava9ProjectWithJREAttributes("org.astro.patch", new String[]{"src", "src2"}, attributes);

			String[] patchSources = {
				"src/org/astro/Test2.java",
				"package org.astro;\n" +
				"class Test2 {\n" +
				"	int test(jdk.internal.misc.Unsafe unsafe) {\n" +
				"		return unsafe.addressSize();\n" +
				"	}\n" +
				"}\n",
				"src2/jdk/internal/misc/Test3.java",
				"package jdk.internal.misc;\n" +
				"class Test3 {\n" +
				"	Signal.NativeHandler handler;\n" + // package access
				"}\n"
			};
			createSourceFiles(patchProject, patchSources);
			
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();

			this.problemRequestor.reset();
			ICompilationUnit cu = getCompilationUnit("/org.astro.patch/src/org/astro/Test2.java");
			cu.getWorkingCopy(this.wcOwner, null);
			assertProblems(
				"Unexpected problems",
				"----------\n" +
				"----------\n",
				this.problemRequestor);

		} finally {
			this.deleteProject("org.astro.patch");
		}
	}

	// patch can share a package with its host - jar
	public void testPatch3() throws CoreException, IOException {
		if (!isJRE9) return;
		try {
			String[] sources = {
				"p/a/X.java",
				"package p.a;\n" +
				"class X {}\n;", // package access
				"module-info.java",
				"module mod.one {\n" + // no exports
				"}\n"
			};
			String outputDirectory = Util.getOutputDirectory();

			String jarPath = outputDirectory + File.separator + "mod-one.jar";
			Util.createJar(sources, jarPath, "9");

			IJavaProject patchProject = createJava9Project("mod.one.patch");			
			IClasspathAttribute[] attributes = {
					JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true"),
					JavaCore.newClasspathAttribute(IClasspathAttribute.PATCH_MODULE, "mod.one")
			};
			addClasspathEntry(patchProject, JavaCore.newLibraryEntry(new Path(jarPath), null, null, null, attributes, false));

			String[] patchSources = {
				"src/p/a/Test2.java",
				"package p.a;\n" +
				"class Test2 extends X {\n" +
				"}\n"
			};
			createSourceFiles(patchProject, patchSources);
			
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();

			this.problemRequestor.reset();
			ICompilationUnit cu = getCompilationUnit("/mod.one.patch/src/p/a/Test2.java");
			cu.getWorkingCopy(this.wcOwner, null);
			assertProblems(
				"Unexpected problems",
				"----------\n" +
				"----------\n",
				this.problemRequestor);

		} finally {
			this.deleteProject("mod.one.patch");
		}
	}
	public void testLimitModules1() throws CoreException, IOException {
		if (!isJRE9) return;
		String save = System.getProperty("modules.to.load");
		// allow for a few more than we are using via limit-modules:
		System.setProperty("modules.to.load", "java.base,java.desktop,java.datatransfer,java.rmi,java.sql,java.prefs,java.xml");
		JRTUtil.reset();
		ClasspathJrt.resetCaches();
		try {
			IClasspathAttribute[] attributes = {
					JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true"),
					JavaCore.newClasspathAttribute(IClasspathAttribute.LIMIT_MODULES, "java.base,java.desktop")
			};
			IJavaProject project = createJava9ProjectWithJREAttributes("org.astro", new String[]{"src", "src2"}, attributes);

			String[] sources = {
				"src/module-info.java",
				"module org.astro {\n" +
				"	requires java.base;\n" +
				"	requires java.desktop;\n" +
				"	requires java.datatransfer;\n" + // within the closure of java.desktop
				"	requires java.sql;\n" + // not included
				"}\n",
				"src/org/astro/Test2.java",
				"package org.astro;\n" +
				"class Test2 {\n" +
				"	java.awt.Window window;\n" +
				"}\n",
				"src2/org/astro/Test3.java",
				"package org.astro;\n" +
				"class Test3 {\n" +
				"	java.awt.datatransfer.Clipboard clippy;\n" +
				"}\n"
			};
			createSourceFiles(project, sources);
			
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = project.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"java.sql cannot be resolved to a module", // outside limited scope
					markers);

			this.problemRequestor.reset();
			ICompilationUnit cu = getCompilationUnit("/org.astro/src/module-info.java");
			cu.getWorkingCopy(this.wcOwner, null);
			assertProblems(
				"Unexpected problems",
				"----------\n" +
				"1. ERROR in /org.astro/src/module-info.java\n" + 
				"java.sql cannot be resolved to a module\n" +
				"----------\n",
				this.problemRequestor);

			this.problemRequestor.reset();
			cu = getCompilationUnit("/org.astro/src/org/astro/Test2.java");
			cu.getWorkingCopy(this.wcOwner, null);
			assertProblems(
				"Unexpected problems",
				"----------\n" +
				"----------\n",
				this.problemRequestor);

			this.problemRequestor.reset();
			cu = getCompilationUnit("/org.astro/src/org/astro/Test3.java");
			cu.getWorkingCopy(this.wcOwner, null);
			assertProblems(
				"Unexpected problems",
				"----------\n" +
				"----------\n",
				this.problemRequestor);

		} finally {
			this.deleteProject("org.astro");
			System.setProperty("modules.to.load", save);
			JRTUtil.reset();
			ClasspathJrt.resetCaches();
		}
	}
	public void testLimitModules2() throws CoreException, IOException {
		if (!isJRE9) return;
		String save = System.getProperty("modules.to.load");
		// allow all
		System.setProperty("modules.to.load", "");
		JRTUtil.reset();
		ClasspathJrt.resetCaches();
		try {
			IClasspathAttribute[] attributes = {
					JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true"),
					JavaCore.newClasspathAttribute(IClasspathAttribute.LIMIT_MODULES, "java.se") // test transitive closure
			};
			IJavaProject project = createJava9ProjectWithJREAttributes("org.astro", new String[]{"src", "src2"}, attributes);

			String[] sources = {
				"src/module-info.java",
				"module org.astro {\n" +
				"	requires java.base;\n" +
				"	requires java.desktop;\n" +
				"	requires java.datatransfer;\n" +
				"	requires java.sql;\n" +
				"}\n",
				"src/org/astro/Test2.java",
				"package org.astro;\n" +
				"class Test2 {\n" +
				"	java.awt.Window window;\n" +
				"}\n",
				"src2/org/astro/Test3.java",
				"package org.astro;\n" +
				"class Test3 {\n" +
				"	java.awt.datatransfer.Clipboard clippy;\n" +
				"}\n"
			};
			createSourceFiles(project, sources);
			
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = project.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"",
					markers);

			this.problemRequestor.reset();
			ICompilationUnit cu = getCompilationUnit("/org.astro/src/module-info.java");
			cu.getWorkingCopy(this.wcOwner, null);
			assertProblems(
				"Unexpected problems",
				"----------\n" +
				"----------\n",
				this.problemRequestor);

			this.problemRequestor.reset();
			cu = getCompilationUnit("/org.astro/src/org/astro/Test2.java");
			cu.getWorkingCopy(this.wcOwner, null);
			assertProblems(
				"Unexpected problems",
				"----------\n" +
				"----------\n",
				this.problemRequestor);

			this.problemRequestor.reset();
			cu = getCompilationUnit("/org.astro/src/org/astro/Test3.java");
			cu.getWorkingCopy(this.wcOwner, null);
			assertProblems(
				"Unexpected problems",
				"----------\n" +
				"----------\n",
				this.problemRequestor);

		} finally {
			this.deleteProject("org.astro");
			System.setProperty("modules.to.load", save);
			JRTUtil.reset();
			ClasspathJrt.resetCaches();
		}
	}
	public void testDefaultRootModules() throws CoreException, IOException {
		if (!isJRE9) return;
		String save = System.getProperty("modules.to.load");
		// need to see all modules:
		System.setProperty("modules.to.load", "");
		JRTUtil.reset();
		ClasspathJrt.resetCaches();
		try {

			IJavaProject project = createJava9Project("org.astro", new String[]{"src"});

			String[] sources = {
				"src/org/astro/ProblemWithPostConstruct.java",
				"package org.astro;\n" +
				"import javax.annotation.PostConstruct;\n" + 
				"\n" + 
				"public class ProblemWithPostConstruct {\n" +
				"	@PostConstruct void init() {}\n" + 
				"}\n"
			};
			createSourceFiles(project, sources);
			
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = project.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers",
					"The import javax.annotation.PostConstruct cannot be resolved\n" + 
					"PostConstruct cannot be resolved to a type", // not in default root modules: java.xml.ws.annotation
					markers);
			
			this.problemRequestor.reset();
			ICompilationUnit cu = getCompilationUnit("/org.astro/src/org/astro/ProblemWithPostConstruct.java");
			cu.getWorkingCopy(this.wcOwner, null);
			assertProblems(
				"Unexpected problems",
				"----------\n" + 
				"1. ERROR in /org.astro/src/org/astro/ProblemWithPostConstruct.java\n" + 
				"The import javax.annotation.PostConstruct cannot be resolved\n" + 
				"----------\n" + 
				"2. ERROR in /org.astro/src/org/astro/ProblemWithPostConstruct.java\n" + 
				"PostConstruct cannot be resolved to a type\n" + 
				"----------\n",
				this.problemRequestor);
		} finally {
			this.deleteProject("org.astro");
			System.setProperty("modules.to.load", save);
			JRTUtil.reset();
			ClasspathJrt.resetCaches();
		}
	}
	public void testBug522398() throws CoreException {
		if (!isJRE9) return;
		String save = System.getProperty("modules.to.load");
		System.setProperty("modules.to.load", "java.base;java.desktop;java.rmi;java.sql;java.xml");
		JRTUtil.reset();
		ClasspathJrt.resetCaches();
		try {

			String[] sources = new String[] {
				"src/javax/xml/mysubpackage/MyClass.java",
				"package javax.xml.mysubpackage;\n" +
				"\n" +
				"public class MyClass {\n" +
				"}\n" +
				"\n" +
				"",
				"src/nonmodular/UsesMySubPackage.java",
				"package nonmodular;\n" +
				"\n" +
				"import javax.xml.mysubpackage.MyClass;\n" +
				"\n" +
				"public class UsesMySubPackage {\n" +
				"	public MyClass field;\n" +
				"}\n" +
				"",
			};
			IJavaProject p1 = setupModuleProject("nonmodular1", sources);
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {},
				false/*not exported*/);
			String[] src = new String[] {
				"src/nonmodular2/Problem.java",
				"package nonmodular2;\n" +
				"\n" +
				"import javax.xml.XMLConstants;\n" +
				"\n" +
				"import nonmodular.UsesMySubPackage;\n" +
				"\n" +
				"public class Problem extends nonmodular.UsesMySubPackage {\n" +
				"	String s = XMLConstants.NULL_NS_URI;\n" +
				"}\n" +
				"",
			};
			IJavaProject p2 = setupModuleProject("nonmodular2", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();
		} finally {
			System.setProperty("modules.to.load", save);
			JRTUtil.reset();
			ClasspathJrt.resetCaches();
			deleteProject("nonmodular1");
			deleteProject("nonmodular2");
		}
	}
	public void testBug522330() throws CoreException, IOException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/javax/net/ServerSocketFactory1.java",
				"package javax.net;\n" +
				"\n" +
				"public class ServerSocketFactory1 {\n" +
				"}\n" +
				"\n" +
				"",
			};
			IJavaProject p1 = setupModuleProject("nonmodular1", sources);
			p1.setOption(JavaCore.COMPILER_COMPLIANCE, "1.8"); // compile with 1.8 compliance to avoid error about package conflict
			
			IClasspathEntry dep = JavaCore.newProjectEntry(p1.getPath(), null, false,
				new IClasspathAttribute[] {},
				false/*not exported*/);
			String[] src = new String[] {
				"src/nonmodular2/Problem.java",
				"package nonmodular2;\n" +
				"\n" +
				"import javax.net.ServerSocketFactory;\n" +
				"\n" +
				"public class Problem  {\n" +
				"	Object o = ServerSocketFactory.getDefault();\n" +
				"} \n" +
				"",
			};
			IJavaProject p2 = setupModuleProject("nonmodular2", src, new IClasspathEntry[] { dep });
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();
		} finally {
			deleteProject("nonmodular1");
			deleteProject("nonmodular2");
		}
	}

	public void testBug522503() throws Exception {
		if (!isJRE9) return;
		try {
			IJavaProject p1 = setupModuleProject("mod.one",
				new String[] {
					"src/module-info.java",
					"module mod.one {\n" +
					"	exports p1;\n" +
					"}\n",
					"src/p1/API.java",
					"package p1;\n" +
					"public class API {}\n"
				});
			IClasspathAttribute[] attr = { JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true") };
			IClasspathEntry[] deps = { JavaCore.newLibraryEntry(p1.getOutputLocation(), null, null, null, attr, false) };
			String[] sources2 = new String[] {
				"src/module-info.java",
				"module mod.two {\n" +
				"	requires mod.one;\n" +
				"}\n",
				"src/client/Client.java",
				"package client;\n" +
				"import p1.API;\n" +
				"public class Client {\n" +
				"	API api;\n" +
				"}\n"
			};
			IJavaProject p2 = setupModuleProject("mod.two", sources2, deps);
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();
	
			this.problemRequestor.reset();
			ICompilationUnit cu = getCompilationUnit("/mod.two/src/client/Client.java");
			cu.getWorkingCopy(this.wcOwner, null);
			assertProblems(
				"Unexpected problems",
				"----------\n" + 
				"----------\n",
				this.problemRequestor);
		} finally {
			deleteProject("mod.one");			
			deleteProject("mod.two");			
		}
	}

	public void testBug526054() throws Exception {
		if (!isJRE9) return;
		String save = System.getProperty("modules.to.load");
		System.setProperty("modules.to.load", ""); // load all
		JRTUtil.reset();
		ClasspathJrt.resetCaches();
		try {
			IJavaProject javaProject = createJava9Project("mod1", new String[] {"src"});

			String srcMod =
				"module mod1 {\n" + 
				"	exports com.mod1.pack1;\n" + 
				"	requires java.xml.ws.annotation;\n" + 
				"}";
			createFile("/mod1/src/module-info.java", 
				srcMod);
			createFolder("/mod1/src/com/mod1/pack1");
			String srcX =
				"package com.mod1.pack1;\n" +
				"@javax.annotation.Generated(\"com.acme.generator.CodeGen\")\n" +
				"public class Dummy {\n" +
				"}";
			createFile("/mod1/src/com/mod1/pack1/Dummy.java", srcX);

			this.problemRequestor.initialize(srcMod.toCharArray());
			getWorkingCopy("/mod1/src/module-info.java", srcMod, true);
			assertProblems("module-info should have no problems",
					"----------\n" + 
					"----------\n",
					this.problemRequestor);

			this.problemRequestor.initialize(srcX.toCharArray());
			getWorkingCopy("/mod1/src/com/mod1/pack1/Dummy.java", srcX, true);
			assertProblems("Dummy should have no problems",
					"----------\n" + 
					"----------\n",
					this.problemRequestor);

			javaProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();
		} finally {
			System.setProperty("modules.to.load", save);
			JRTUtil.reset();
			ClasspathJrt.resetCaches();
			deleteProject("mod1");
		}
	}

	public void testBug525603() throws Exception {
		if (!isJRE9) return;
		IJavaProject javaProject = null;
		try {
			String[] sources = {
				"com/automod1/pack/DummyA.java",
				"package com.automod1.pack;\n" +
				"public class DummyA {}\n;"
			};
			String outputDirectory = Util.getOutputDirectory();
	
			String jarPath = outputDirectory + File.separator + "automod.jar";
			Util.createJar(sources, jarPath, "1.8");
			
			javaProject = createJava9Project("mod1", new String[] {"src"});
			IClasspathAttribute[] attributes = { JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true") };
			addClasspathEntry(javaProject, JavaCore.newLibraryEntry(new Path(jarPath), null, null, null, attributes, false));

			String srcMod =
				"module mod1 {\n" + 
				"	exports com.mod1.pack1;\n" + 
				"	requires automod;\n" + 
				"}";
			createFile("/mod1/src/module-info.java", 
				srcMod);
			createFolder("/mod1/src/com/mod1/pack1");
			String srcX =
				"package com.mod1.pack1;\n" +
				"public class Dummy {\n" +
				"}";
			createFile("/mod1/src/com/mod1/pack1/Dummy.java", srcX);
			
			this.problemRequestor.initialize(srcMod.toCharArray());
			getWorkingCopy("/mod1/src/module-info.java", srcMod, true);
			assertProblems("module-info should have no problems",
					"----------\n" + 
					"----------\n",
					this.problemRequestor);

			this.problemRequestor.initialize(srcX.toCharArray());
			getWorkingCopy("/mod1/src/com/mod1/pack1/Dummy.java", srcX, true);
			assertProblems("X should have no problems",
					"----------\n" + 
					"----------\n",
					this.problemRequestor);

			javaProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();
		} finally {
			if (javaProject != null)
				deleteProject(javaProject);
		}
	}

	public void testBug522670() throws Exception {
		if (!isJRE9) return;
		Hashtable<String, String> javaCoreOptions = JavaCore.getOptions();
		try {
			Hashtable<String, String> newOptions=new Hashtable<>(javaCoreOptions);
			newOptions.put(CompilerOptions.OPTION_Store_Annotations, JavaCore.ENABLED);
			JavaCore.setOptions(newOptions);
			IJavaProject p1 = setupModuleProject("util",
				new String[] {
					"src/module-info.java",
					"module util {\n" +
					"	exports my.util;\n" +
					"}\n" +
					"",
					"src/my/util/Data.java",
					"package my.util;\n" +
					"public class Data {\n" +
					"}\n" +
					"",
					"src/my/util/AnnotatedInModule.java",
					"package my.util;\n" +
					"import static java.lang.annotation.ElementType.TYPE_USE;\n" +
					"import java.lang.annotation.Target;\n" +
					"@Target(TYPE_USE)\n" +
					"@interface Y {\n" +
					"}\n" +
					"public abstract class AnnotatedInModule {\n" +
					"	abstract public @Y Data getTime();\n" +
					"}\n" +
					"",
				});
			IJavaProject p2 = setupModuleProject("util2",
				new String[] {
					"src/module-info.java",
					"module util2 {\n" +
					"	exports my.util.nested;\n" +
					"}\n" +
					"",
					"src/my/util/nested/Unrelated.java",
					"package my.util.nested;\n" +
					"class Unrelated {\n" +
					"}\n" +
					"",
				});
			String[] sources3 = {
				"src/a/other/AnnotatedInOtherNonModule.java",
				"package a.other;\n" +
				"import static java.lang.annotation.ElementType.TYPE_USE;\n" +
				"import java.lang.annotation.Target;\n" +
				"import my.util.Data;\n" +
				"@Target(TYPE_USE)\n" +
				"@interface X {\n" +
				"}\n" +
				"public class AnnotatedInOtherNonModule {\n" +
				"	@X\n" +
				"	Data generationDate;\n" +
				"}\n" +
				"",
			};
			IClasspathAttribute[] attr = { JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true") };
			// modulepath
			IClasspathEntry[] deps3 = { JavaCore.newProjectEntry(p1.getPath(), null, false, attr, false) };
			IJavaProject p3 = setupModuleProject("other", sources3, deps3);
	
			String[] sources4 = {
				"src/test/Test.java",
				"package test;\n" +
				"\n" +
				"import a.other.AnnotatedInOtherNonModule;\n" +
				"import my.util.AnnotatedInModule;\n" +
				"import my.util.Data;\n" +
				"\n" +
				"public class Test extends AnnotatedInOtherNonModule {\n" +
				"	public Data f(AnnotatedInModule calendar) {\n" +
				"		return calendar.getTime();\n" +
				"	}\n" +
				"}\n" +
				"",
			};
			IClasspathEntry[] deps4 = { //
					// modulepath (with split package my.util)
					JavaCore.newProjectEntry(p1.getPath(), null, false, attr, false), //
					JavaCore.newProjectEntry(p2.getPath(), null, false, attr, false), //
					// classpath
					JavaCore.newProjectEntry(p3.getPath()) //
			};
			IJavaProject p4 = setupModuleProject("test", sources4, deps4);
			p4.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
	
			assertNoErrors();
		} finally {
			JavaCore.setOptions(javaCoreOptions);
			deleteProject("util");
			deleteProject("util2");
			deleteProject("other");
			deleteProject("test");
		}
	}

	public void testBug525522() throws Exception {
		if (!isJRE9) return;
		String save = System.getProperty("modules.to.load");
		System.setProperty("modules.to.load", "java.base;java.desktop;java.rmi;java.sql;java.jnlp");
		JRTUtil.reset();
		ClasspathJrt.resetCaches();
		try {
			// non-modular substitute for java.jnlp:
			IClasspathAttribute[] jreAttribs = { JavaCore.newClasspathAttribute(IClasspathAttribute.LIMIT_MODULES, "java.base,java.desktop,java.rmi,java.sql") };
			IJavaProject jnlp = createJava9ProjectWithJREAttributes("jnlp", new String[] {"src"}, jreAttribs);
			createFolder("jnlp/src/javax/jnlp");
			createFile("jnlp/src/javax/jnlp/UnavailableServiceException.java",
						"package javax.jnlp;\n" +
						"@SuppressWarnings(\"serial\")\n" +
						"public class UnavailableServiceException extends Exception {\n" +
						"}\n");
			createFile("jnlp/src/javax/jnlp/ServiceManager.java",
						"package javax.jnlp;\n" +
						"public class ServiceManager {\n" +
						"	public static void lookup(String s) throws UnavailableServiceException {}\n" +
						"}\n");

			// non-modular project consuming the non-modular jnlp, instead of the module from the JRE: 
			IJavaProject p1 = createJava9ProjectWithJREAttributes("nonmod1", new String[] {"src"}, jreAttribs);
			addClasspathEntry(p1, JavaCore.newProjectEntry(jnlp.getPath()));

			createFolder("nonmod1/src/test");
			createFile("nonmod1/src/test/Test.java",
						"package test;\n" + 
						"import javax.jnlp.ServiceManager;\n" + 
						"import javax.jnlp.UnavailableServiceException;\n" + 
						"\n" + 
						"public class Test {\n" + 
						"\n" + 
						"    void init() {\n" + 
						"        try {\n" + 
						"            ServiceManager.lookup(\"\");\n" + 
						"        } catch (final UnavailableServiceException e) {\n" + 
						"            e.printStackTrace();\n" + 
						"        }\n" + 
						"\n" + 
						"    }\n" + 
						"}\n");
			p1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();

			this.problemRequestor.reset();
			ICompilationUnit cu = getCompilationUnit("/nonmod1/src/test/Test.java");
			cu.getWorkingCopy(this.wcOwner, null);
			assertProblems(
				"Unexpected problems",
				"----------\n" + 
				"----------\n",
				this.problemRequestor);

		} finally {
			System.setProperty("modules.to.load", save);
			JRTUtil.reset();
			ClasspathJrt.resetCaches();
			deleteProject("jnlp");
			deleteProject("nonmod1");
		}
	}

	protected void assertNoErrors() throws CoreException {
		for (IProject p : getWorkspace().getRoot().getProjects()) {
			int maxSeverity = p.findMaxProblemSeverity(null, true, IResource.DEPTH_INFINITE);
			if (maxSeverity == IMarker.SEVERITY_ERROR) {
				for (IMarker marker : p.findMarkers(null, true, IResource.DEPTH_INFINITE))
					System.err.println("Marker "+ marker.toString());
			}
			assertFalse("Unexpected errors in project " + p.getName(), maxSeverity == IMarker.SEVERITY_ERROR);
		}
	}
	// sort by CHAR_START
	protected void sortMarkers(IMarker[] markers) {
		Arrays.sort(markers, (a,b) -> a.getAttribute(IMarker.CHAR_START, 0) - b.getAttribute(IMarker.CHAR_START, 0)); 
	}
}
