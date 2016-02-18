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

import java.io.File;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.ModulePackageFragmentRoot;

import junit.framework.Test;

public class ModuleBuilderTests extends ModifyingResourceTests {
	public ModuleBuilderTests(String name) {
		super(name);
	}

	static {
		 //TESTS_NAMES = new String[] { "test003" };
	}
	private static boolean isJRE9 = false;
	public static Test suite() {
		String specVersion = System.getProperty("java.specification.version");
		if (CompilerOptions.VERSION_1_9.equals(specVersion)) {
			isJRE9 = true;
		}
		return buildModelTestSuite(ModuleBuilderTests.class, BYTECODE_DECLARATION_ORDER);
	}

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.currentProject = createJava9Project("P1");
		this.createFile("P1/src/module-info.java", "");
		this.createFolder("P1/src/com/greetings");
		this.createFile("P1/src/com/greetings/Main.java", "");
		waitForManualRefresh();
		waitForAutoBuild();
	}
	private IJavaProject createJava9Project(String name) throws CoreException {
		String bootModPath = System.getProperty("java.home") +File.separator +"lib/modules/bootmodules.jimage";
		IClasspathEntry jimageEntry = JavaCore.newLibraryEntry(new Path(bootModPath), null, null, null, null, false);
		IJavaProject project = this.createJavaProject(name, new String[] { "src" }, new String[0],
				new String[0], "bin", "1.9");
		IClasspathEntry[] old = project.getRawClasspath();
		IClasspathEntry[] newPath = new IClasspathEntry[old.length +1];
		System.arraycopy(old, 0, newPath, 0, old.length);
		newPath[old.length] = jimageEntry;
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
			assertTrue("Java.base should be a module package fragment root", (base instanceof ModulePackageFragmentRoot));
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
					"The import java.sql.Connection cannot be resolved\n" + 
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
	public void test006() throws CoreException {
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
				"}");
		this.editFile("P1/src/com/greetings/Main.java",
				"package com.greetings;\n" +
				"public class Main {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(\"Hello\");\n" +
				"	}\n" +
				"}");
		//waitForManualRefresh();
		//this.currentProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
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
					"The import com.greetings.Main cannot be resolved\n" + 
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
	public void test010() throws CoreException {
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
					"Main cannot be resolved\n" +
					"The import com.greetings cannot be resolved",
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
					"The import com.greetings cannot be resolved\n" + 
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
	public void tearDownSuite() throws Exception {
		super.tearDownSuite();
		deleteProject("P1");
	}

}
