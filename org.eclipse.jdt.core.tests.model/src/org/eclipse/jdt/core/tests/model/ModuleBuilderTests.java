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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JrtPackageFragmentRoot;

import junit.framework.Test;

public class ModuleBuilderTests extends ModifyingResourceTests {
	public ModuleBuilderTests(String name) {
		super(name);
	}

	static {
//		 TESTS_NAMES = new String[] { "test_ModuleSourcePathContainer" };
	}
	private static boolean isJRE9 = false;
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

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		System.setProperty("modules.to.load", "java.baserequires java.desktop;java.rmi;java.sql;");
		this.currentProject = createJava9Project("P1");
		this.createFile("P1/src/module-info.java", "");
		this.createFolder("P1/src/com/greetings");
		this.createFile("P1/src/com/greetings/Main.java", "");
		waitForManualRefresh();
		waitForAutoBuild();
	}
	private IJavaProject createJava9Project(String name) throws CoreException {
		String bootModPath = System.getProperty("java.home") + File.separator +"jrt-fs.jar";
		IClasspathEntry jrtEntry = JavaCore.newLibraryEntry(new Path(bootModPath), null, null, null, null, false);
		IJavaProject project = this.createJavaProject(name, new String[] { "src" }, new String[0],
				new String[0], "bin", "9");
		IClasspathEntry[] old = project.getRawClasspath();
		IClasspathEntry[] newPath = new IClasspathEntry[old.length +1];
		System.arraycopy(old, 0, newPath, 0, old.length);
		newPath[old.length] = jrtEntry;
		project.setRawClasspath(newPath, null);
		return project;
	}
	// Test that the java.base found as a module package fragment root in the project 
	public void test001() throws CoreException {
		if (!isJRE9) return;
		try {
			IPackageFragmentRoot[] roots = this.currentProject.getPackageFragmentRoots();
			IPackageFragmentRoot base = null;
			for (IPackageFragmentRoot iRoot : roots) {
				if (iRoot.getElementName().equals("java.base")) {
					base = iRoot;
					break;
				}
			}
			assertNotNull("Java.base module should not null", base);
			assertTrue("Java.base should be a module package fragment root", (base instanceof JrtPackageFragmentRoot));
			assertMarkers("Unexpected markers", "", this.currentProject);
		} finally {
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
					"		System.out.println(\"Hello\");\n" +
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
				"		System.out.println(\"Hello\");\n" +
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
	public void test007() throws CoreException {
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
			assertMarkers("Unexpected markers", 
					"The import com cannot be resolved\n" + 
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
	public void test008() throws CoreException {
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
			assertMarkers("Unexpected markers", 
					"Main cannot be resolved\n" + 
					"The import com cannot be resolved", 
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
	public void test009() throws CoreException {
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
	public void _test010() throws CoreException {
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
	public void test011() throws CoreException {
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
			assertMarkers("Unexpected markers",
					"The import com cannot be resolved\n" +
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
	public void test012() throws CoreException {
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
			assertMarkers("Unexpected markers", 
					"The import com cannot be resolved\n" + 
					"Main cannot be resolved", 
					markers);
		} finally {
			deleteProject("P2");
			deleteProject("P3");
		}
	}
	/*
	 * Three Java projects, each with one module. Project P3 depends on P2, which depends on P1.
	 * Module M1 exports a package (to all), M2 requires 'public' M1 and M3 requires M2. Usage of types from
	 * M1 in M3 should be allowed.
	 */
	public void test013() throws CoreException {
		if (!isJRE9) return;
		try {
			IJavaProject p2 = setupP2();
			IJavaProject p3 = setupP3();
			this.editFile("P2/src/module-info.java",
					"module M2 {\n" +
					"	exports org.astro;\n" +
					"	requires public M1;\n" +
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
	public void test014() throws CoreException {
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
					"	requires public M1;\n" +
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
					"	requires public M1;\n" +
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
					"	requires public M1;\n" +
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
			assertMarkers("Unexpected markers",
					"The import com cannot be resolved\n" + 
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
					"	requires public M1;\n" +
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
			assertMarkers("Unexpected markers",
					"The import com cannot be resolved\n" + 
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
					"	requires public M1;\n" +
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
			IModuleDescription mod = JavaCore.createModuleFromPackageRoot(null, project);
			assertEquals("incorrect value", "ConvertToModule", mod.getElementName());
			IModuleDescription.IPackageExport[] exports = mod.getExportedPackages();
			assertEquals("incorrect value", 2, exports.length);
			assertEquals("incorrect value", "org.eclipse.jdt.test", exports[0].getPackageName());
			assertEquals("incorrect value", "org.eclipse.test", exports[1].getPackageName());
			
			IModuleDescription.IModuleReference[] mods = mod.getRequiredModules();
			assertEquals("incorrect value", 4, mods.length);
			assertEquals("incorrect value", "java.base", mods[0].getModuleName());
			assertEquals("incorrect value", "java.desktop", mods[1].getModuleName());
			assertEquals("incorrect value", "java.rmi", mods[2].getModuleName());
			assertEquals("incorrect value", "java.sql", mods[3].getModuleName());

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
					"The service implementation com.greetings.MyWorld does not have a no-arg constructor",  markers);
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
					"Duplicate service entry: provides org.astro.World with com.greetings.MyWorld",  markers);
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
					"The exported package com.greetings does not exist or is empty",  markers);
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
					"Duplicate exports entry: com.greetings",  markers);
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
			assertMarkers("Unexpected markers",	
					"The import org cannot be resolved\n" +
					"World cannot be resolved to a type",
					markers);
		} finally {
			deleteProject("some.mod");
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	// Report an error when the target module of a targeted exports statement
	// cannot be resolved
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
			assertMarkers("Unexpected markers",	"some.mod cannot be resolved to a module",  markers);
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
	// Implicit module dependencies via the 'requires public' directive should be
	// resolved via the module path container
	public void test_ModuleSourcePath_implicitdeps() throws CoreException {
		if (!isJRE9) return;
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"module some.mod {\n" +
				"	requires public org.astro;\n" +
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
				"	requires public org.astro;\n" +
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
			assertMarkers("Unexpected markers", 
					"The import org cannot be resolved\n" +
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
				"	requires public org.astro;\n" +
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
				"	requires com.greetings;\n" +
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
				"}"
			};
			
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
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
				"module some.mod {\n" +
				"	requires public org.astro;\n" +
				"}"
			};
			IClasspathEntry dep = JavaCore.newContainerEntry(new Path(JavaCore.MODULE_PATH_CONTAINER_ID));
			setupModuleProject("some.mod", sources, new IClasspathEntry[]{dep});
			sources = new String[] {
				"src/module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" +
				"	requires public com.greetings;\n" +
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
				"}"
			};
			
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			assertTrue("Should detect cycle", p2.hasClasspathCycle(null));
		} finally {
			deleteProject("org.astro");
			deleteProject("some.mod");
			deleteProject("com.greetings");
		}
	}
	private IJavaProject setupModuleProject(String name, String[] sources) throws CoreException {
		return setupModuleProject(name, sources, null);
	}
	private IJavaProject setupModuleProject(String name, String[] sources, IClasspathEntry[] deps) throws CoreException {
		IJavaProject project = createJava9Project(name);
		IProgressMonitor monitor = new NullProgressMonitor();
		for (int i = 0; i < sources.length; i+= 2) {
			IPath path = new Path(sources[i]);
			IPath parentPath = path.removeLastSegments(1);
			IFolder folder = project.getProject().getFolder(parentPath);
			if (!folder.exists())
				this.createFolder(folder.getFullPath());
			IFile file = project.getProject().getFile(new Path(sources[i]));
			file.create(new ByteArrayInputStream(sources[i+1].getBytes()), true, monitor);
		}
		if (deps != null) {
			IClasspathEntry[] old = project.getRawClasspath();
			IClasspathEntry[] newPath = new IClasspathEntry[old.length + deps.length];
			System.arraycopy(old, 0, newPath, 0, old.length);
			System.arraycopy(deps, 0, newPath, old.length, deps.length);
			project.setRawClasspath(newPath, null);
		}
		return project;
	}
	public void tearDownSuite() throws Exception {
		super.tearDownSuite();
		deleteProject("P1");
		System.setProperty("modules.on.demand", "");
	}
}
