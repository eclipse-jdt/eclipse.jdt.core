/*******************************************************************************
 * Copyright (c) 2016, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
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
//		 TESTS_NAMES = new String[] { "testReleaseOption8" };
	}
	private String sourceWorkspacePath = null;
	protected ProblemRequestor problemRequestor;
	public static Test suite() {
		if (!isJRE9) {
			// almost empty suite, since we need JRE9+
			Suite suite = new Suite(ModuleBuilderTests.class.getName());
			suite.addTest(new ModuleBuilderTests("thisSuiteRunsOnJRE9plus"));
			return suite;
		}
		return buildModelTestSuite(ModuleBuilderTests.class, BYTECODE_DECLARATION_ORDER);
	}
	public void thisSuiteRunsOnJRE9plus() {}

	@Override
	public String getSourceWorkspacePath() {
		return this.sourceWorkspacePath == null ? super.getSourceWorkspacePath() : this.sourceWorkspacePath;
	}
	@Override
	public void setUp() throws Exception {
		super.setUp();
		this.problemRequestor =  new ProblemRequestor();
		this.wcOwner = new WorkingCopyOwner() {
			public IProblemRequestor getProblemRequestor(ICompilationUnit unit) {
				return ModuleBuilderTests.this.problemRequestor;
			}
		};
	}
	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.currentProject = createJava9Project("P1");
		this.createFile("P1/src/module-info.java", "");
		this.createFolder("P1/src/com/greetings");
		this.createFile("P1/src/com/greetings/Main.java", "");
		waitForManualRefresh();
		waitForAutoBuild();
	}

	@Override
	public void tearDownSuite() throws Exception {
		super.tearDownSuite();
		deleteProject("P1");
	}

	// Test that the java.base found as a module package fragment root in the project
	public void test001() throws CoreException {
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
			assertProblemMarkers("Unexpected markers", "", project.getProject());
		} finally {
			deleteProject("Test01");
		}
	}
	// Test the project compiles without errors with a simple module-info.java
	public void test002() throws CoreException {
		try {
			this.editFile("P1/src/module-info.java",
							"module M1 {\n" +
							"	exports com.greetings;\n" +
							"	requires java.base;\n" +
							"}");
			this.createFile("P1/src/com/greetings/Greet.java", "package com.greetings; public class Greet {}\n");
			waitForManualRefresh();
			this.currentProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertProblemMarkers("Unexpected markers", "", this.currentProject.getProject());
		} finally {
			deleteFile("P1/src/com/greetings/Greet.java");
		}
	}
	// Test that types from java.base module are seen by the compiler
	// even without an explicit 'requires java.base' declaration.
	public void test003() throws CoreException {
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
					"The type java.sql.Connection is not accessible\n" +
					"Connection cannot be resolved to a type", markers);
		} finally {
		}
	}
	// Test that a type that is outside java.base module is available to the compiler
	// when the module is specified as 'requires'.
	public void test005() throws CoreException {
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
					"The type com.greetings.Main is not accessible\n" +
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
					"The type com.greetings.Main is not accessible\n" +
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
		Hashtable<String, String> javaCoreOptions = JavaCore.getOptions();
		try {
			IJavaProject project = setUpJavaProject("ConvertToModule");
			Map<String, String> options = new HashMap<>();
			// Make sure the new options map doesn't reset.
			options.put(CompilerOptions.OPTION_Compliance, "10");
			options.put(CompilerOptions.OPTION_Source, "10");
			options.put(CompilerOptions.OPTION_TargetPlatform, "10");
			options.put(CompilerOptions.OPTION_Release, "enabled");
			project.setOptions(options);
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
			if (isJRE12)
				assertStringsEqual("incorrect result", new String[]{"java.desktop", "java.rmi", "java.sql"}, modules);
			else if (isJRE11)
				assertStringsEqual("incorrect result", new String[]{"java.datatransfer", "java.desktop", "java.net.http", "java.rmi", "java.sql"}, modules);
			else if (isJRE10)
				assertStringsEqual("incorrect result", new String[]{"java.datatransfer", "java.desktop", "java.rmi", "java.sql"}, modules);
			else // 9
				assertStringsEqual("incorrect result", new String[]{"java.desktop", "java.rmi", "java.sql"}, modules);
		} finally {
			this.deleteProject("ConvertToModule");
			 JavaCore.setOptions(javaCoreOptions);
		}
	}
	public void testConvertToModuleWithRelease9() throws CoreException, IOException {
		Hashtable<String, String> javaCoreOptions = JavaCore.getOptions();
		try {
			IJavaProject project = setUpJavaProject("ConvertToModule");
			Map<String, String> options = new HashMap<>();
			// Make sure the new options map doesn't reset.
			options.put(CompilerOptions.OPTION_Compliance, "9");
			options.put(CompilerOptions.OPTION_Source, "9");
			options.put(CompilerOptions.OPTION_TargetPlatform, "9");
			options.put(CompilerOptions.OPTION_Release, "enabled");
			project.setOptions(options);
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
			if (isJRE12)
				assertStringsEqual("incorrect result", new String[]{"java.desktop", "java.rmi", "java.sql"}, modules);
			else if (isJRE11)
				assertStringsEqual("incorrect result", new String[]{"java.datatransfer", "java.desktop", "java.net.http", "java.rmi", "java.sql"}, modules);
			else if (isJRE10)
				assertStringsEqual("incorrect result", new String[]{"java.datatransfer", "java.desktop", "java.rmi", "java.sql"}, modules);
			else // 9
				assertStringsEqual("incorrect result", new String[]{"java.desktop", "java.rmi", "java.sql"}, modules);
		} finally {
			this.deleteProject("ConvertToModule");
			 JavaCore.setOptions(javaCoreOptions);
		}
	}
	public void test_services_abstractImpl() throws CoreException {
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
	public void test_Exports_foreign_package1() throws CoreException {
		try {
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	exports java.util;\n" +
				"}"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src);
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"Cannot export the package java.util which belongs to module java.base",  markers);
		} finally {
			deleteProject("com.greetings");
		}
	}
	public void test_Exports_foreign_package2() throws CoreException {
		try {
			String[] src = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	exports java.util;\n" +
				"}",
				"src/java/util/Wrong.java",
				"package java.util;\n" +
				"public class Wrong {}\n"
			};
			IJavaProject p2 = setupModuleProject("com.greetings", src);
			p2.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers",
					"The package java.util conflicts with a package accessible from another module: java.base",
					markers);
		} finally {
			deleteProject("com.greetings");
		}
	}
	public void test_DuplicateExports() throws CoreException {
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
	// Changes to implicit dependencies should be reflected // FIXME: container JavaCore.MODULE_PATH_CONTAINER_ID is unreliable
	public void _test_ModuleSourcePath_implicitdeps2() throws CoreException {
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

			String duplicatedModuleFile = new Path("othersrc").append(TypeConstants.MODULE_INFO_FILE_NAME_STRING).toOSString();
			IMarker[] markers = p2.getProject().getFile(duplicatedModuleFile).findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_ZERO);
			assertEquals(1, markers.length);
			String msg = markers[0].getAttribute(IMarker.MESSAGE, "");
			String expected = Messages.build_duplicateModuleInfo;
			assertEquals("Unexpected problem reported", expected, msg);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void test_Multiple_SourceFolders_addModuleInfo() throws CoreException {
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
					"}");
			waitForAutoBuild();
			p1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			String duplicatedModuleFile = new Path("othersrc").append(TypeConstants.MODULE_INFO_FILE_NAME_STRING).toOSString();
			IMarker[] markers = p1.getProject().getFile(duplicatedModuleFile).findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_ZERO);
			assertEquals(1, markers.length);
			String msg = markers[0].getAttribute(IMarker.MESSAGE, "");
			String expected = Messages.build_duplicateModuleInfo;
			assertEquals("Unexpected problem reported", expected, msg);
		} finally {
			deleteProject("org.astro");
		}
	}
	public void test_Multiple_SourceFolders_removeModuleInfo() throws CoreException {
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
			assertProblemMarkers("Unexpected markers", "", project.getProject());
		} finally {
			deleteProject("Test01");
		}
	}
	public void testSystemLibAsJMod_2() throws CoreException {
		try {
			IJavaProject project = createJava9Project("Test01", new String[]{"src"});
			IClasspathEntry[] rawClasspath = project.getRawClasspath();
			IClasspathEntry[] newClasspath = new IClasspathEntry[rawClasspath.length + 1];
			IClasspathEntry desktop = null;
			for (int i = 0; i < rawClasspath.length; i++) {
				IPath path = rawClasspath[i].getPath();
				if (path.lastSegment().equals("jrt-fs.jar")) {
					path = path.removeLastSegments(2).append("jmods").append("java.base.jmod");
					IClasspathAttribute[] attributes = {
							JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true") };
					IClasspathEntry newEntry = JavaCore.newLibraryEntry(path, rawClasspath[i].getSourceAttachmentPath(),
							new Path("java.base"), null, attributes, rawClasspath[i].isExported());
					newClasspath[i] = newEntry;
					path = path.removeLastSegments(2).append("jmods").append("java.desktop.jmod");
					desktop = JavaCore.newLibraryEntry(path, rawClasspath[i].getSourceAttachmentPath(),
							new Path("java.desktop"), null, attributes, rawClasspath[i].isExported());
				} else {
					newClasspath[i] = rawClasspath[i];
				}
			}
			newClasspath[rawClasspath.length] = desktop;
			project.setRawClasspath(newClasspath, null);
			this.createFile("Test01/src/module-info.java",
					"module org.eclipse {\n" +
					"	requires java.desktop;\n" +
					"	requires java.base;\n" +
					"}");
			waitForManualRefresh();
			waitForAutoBuild();
			project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			assertMarkers("unexpected markers", "", markers);

			// Check the reconciler
			ICompilationUnit cu = getCompilationUnit("/Test01/src/module-info.java");
			cu.getWorkingCopy(this.wcOwner, null);
			markers = project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			editFile("Test01/src/module-info.java",
					"//Just touching \n" +
					"module org.eclipse {\n" +
					"	requires java.desktop;\n" +
					"	requires java.base;\n" +
					"}");
			project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			markers = project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			assertMarkers("unexpected markers", "", markers);
		} finally {
			deleteProject("Test01");
		}
	}
	public void testBug510617() throws CoreException {
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
					"The package org.astro is accessible from more than one module: org.astro, some.mod\n" +
					"World cannot be resolved to a type",
					markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("some.mod");
			deleteProject("com.greetings");
		}
	}

	// test that the compilation of a class using same package defined in the java.util module
	// works if a special option is given
	public void test_no_conflicting_packages_for_debugger_global() throws CoreException {
		Hashtable<String, String> javaCoreOptions = JavaCore.getOptions();
		try {
			Hashtable<String, String> newOptions=new Hashtable<>(javaCoreOptions);
			newOptions.put(CompilerOptions.OPTION_JdtDebugCompileMode, JavaCore.ENABLED);
			JavaCore.setOptions(newOptions);
			String[] sources = new String[] {
					"src/java/util/Map___.java",
					"package java.util;\n" +
					"abstract class Map___ implements java.util.Map {\n" +
					"  Map___() {\n" +
					"    super();\n" +
					"  }\n" +
					"  Object[] ___run() throws Throwable {\n" +
					"    return entrySet().toArray();\n" +
					"  }\n" +
					"}"
			};
			IClasspathEntry dep = JavaCore.newContainerEntry(new Path(JavaCore.MODULE_PATH_CONTAINER_ID));
			IJavaProject p1= setupModuleProject("debugger_project", sources, new IClasspathEntry[]{dep});
			p1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();

			assertNull("Option should not be stored", JavaCore.getOption(CompilerOptions.OPTION_JdtDebugCompileMode));
		} finally {
			deleteProject("debugger_project");
			JavaCore.setOptions(javaCoreOptions);
		}
	}

	// test that the special OPTION_JdtDebugCompileMode cannot be persisted on a project
	public void test_no_conflicting_packages_for_debugger_project() throws CoreException {
		try {
			String[] sources = new String[] {
					"src/java/util/Map___.java",
					"package java.util;\n" +
					"abstract class Map___ implements java.util.Map {\n" +
					"  Map___() {\n" +
					"    super();\n" +
					"  }\n" +
					"  Object[] ___run() throws Throwable {\n" +
					"    return entrySet().toArray();\n" +
					"  }\n" +
					"}"
			};
			IClasspathEntry dep = JavaCore.newContainerEntry(new Path(JavaCore.MODULE_PATH_CONTAINER_ID));
			IJavaProject p1= setupModuleProject("debugger_project", sources, new IClasspathEntry[]{dep});
			p1.setOption(CompilerOptions.OPTION_JdtDebugCompileMode, JavaCore.ENABLED);
			assertNull("Option should not be stored", p1.getOption(CompilerOptions.OPTION_JdtDebugCompileMode, false));
			p1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers",
					"The package java.util conflicts with a package accessible from another module: java.base\n" +
					"The package java.util is accessible from more than one module: <unnamed>, java.base\n" +
					"The method entrySet() is undefined for the type Map___",
					markers);
		} finally {
			deleteProject("debugger_project");
		}
	}

	// test that a package declared in a module conflicts with an accessible package
	// of the same name declared in another required module
	public void test_conflicting_packages_declaredvsaccessible() throws CoreException {
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
			sortMarkers(markers);
			assertMarkers("Unexpected markers",
					"The package org.astro conflicts with a package accessible from another module: org.astro\n" +
					"The package org.astro is accessible from more than one module: com.greetings, org.astro",
					markers);
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
					"The package org.astro is accessible from more than one module: org.astro, some.mod\n" +
					"World cannot be resolved to a type",
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
			sortMarkers(markers);
			assertMarkers("Unexpected markers",
					"The package org.astro conflicts with a package accessible from another module: org.astro\n" +
					"The package org.astro is accessible from more than one module: <unnamed>, org.astro",
					markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	// test that a type in an accessible package trumps an accessible package with the same name
	// in the context of a non-modular project
	public void test_conflict_packagevstype_unnamed() throws CoreException {
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
			sortMarkers(markers);
			assertMarkers("Unexpected markers",
					"The package bundle.org conflicts with a package accessible from another module: other.mod\n" +
					"The package bundle.org is accessible from more than one module: <unnamed>, other.mod",
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
			p2.setOption(JavaCore.COMPILER_PB_UNSTABLE_AUTO_MODULE_NAME, JavaCore.IGNORE);

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
			p3.setOption(JavaCore.COMPILER_PB_UNSTABLE_AUTO_MODULE_NAME, JavaCore.IGNORE);
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
			p3.setOption(JavaCore.COMPILER_PB_UNSTABLE_AUTO_MODULE_NAME, JavaCore.IGNORE);
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
					"Name of automatic module \'test\' is unstable, it is derived from the module\'s file name.\n" +
					"The type org.astro.World is not accessible",
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
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("markers on com.greetings",
					"The package bundle.org conflicts with a package accessible from another module: org.astro",
					markers);

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
			p3.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			markers = p3.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
			this.deleteProject("test");
			this.deleteProject("com.greetings");
			this.deleteProject("org.astro");
			JavaCore.setOptions(javaCoreOptions);
		}
	}
	public void testSourceFolders_Bug519673() throws CoreException {
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
	public void testAddExportsIllegal() throws CoreException {
		try {
			// need to simulate a system library container (provided otherwise by jdt.launching)
			ContainerInitializer.setInitializer(new TestContainerInitializer(IClasspathContainer.K_SYSTEM));

			IJavaProject p = createJava10Project("com.greetings", new String[] {"src"}); // compliance 10 ensures that --release is effective
			setClasspath(p, new IClasspathEntry[] {
				JavaCore.newContainerEntry(new Path(TestContainerInitializer.TEST_CONTAINER_NAME), null,
						new IClasspathAttribute[] {
								new ClasspathAttribute("module", "true"),
								new ClasspathAttribute(IClasspathAttribute.ADD_EXPORTS, "java.desktop/com.sun.imageio.plugins.png=ALL-UNNAMED") },
						false),
				JavaCore.newSourceEntry(new Path("/com.greetings/src"))
			});
			p.setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
			createSourceFiles(p, new String[] {
					"src/Foo.java",
					"import com.sun.imageio.plugins.png.PNGImageReader;\n" +
					"\n" +
					"public class Foo {\n" +
					"	PNGImageReader r;\n" +
					"}\n"
			});
			p.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			String expectedMarkers =
					"Exporting a package from system module \'java.desktop\' is not allowed with --release.\n" +
					"The project cannot be built until build path errors are resolved";
			assertMarkers("Unexpected markers", expectedMarkers, markers);
			// toggle to disabled should resolve the error:
			p.setOption(JavaCore.COMPILER_RELEASE, JavaCore.DISABLED);
			p.getProject().getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
			markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
			// toggle back to enabled should resurface the error
			p.setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
			p.getProject().getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
			markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers", expectedMarkers, markers);
		} finally {
			ContainerInitializer.setInitializer(null);
			deleteProject("com.greetings");
		}
	}
	public void testAddReads() throws CoreException, IOException {
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
			assertMarkers("Unexpected markers",
					"Name of automatic module \'com.greetings\' is unstable, it is derived from the module\'s file name.",
					markers);
		} finally {
			this.deleteProject("test");
			this.deleteProject("com.greetings");
			this.deleteProject("org.astro");
			JavaCore.setOptions(javaCoreOptions);
		}
	}
	@Deprecated
	public void testBug519935() throws CoreException, IOException {
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
					"The type org.astro.World is not accessible\n" + // cannot use cyclic requires
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
					"1. ERROR in /Test/src/X.java (at line 1)\n" +
					"	import java.*;\n" +
					"	       ^^^^\n" +
					"The package java is not accessible\n" +
					"----------\n",
					this.problemRequestor);
		} finally {
			if (javaProject != null)
				deleteProject(javaProject);
		}
	}
	public void testAutoModule1() throws Exception {
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
			assertProblems("module-info should have one warning",
					"----------\n" +
					"1. WARNING in /mod.one/module-info.java (at line 2)\n" +
					"	requires lib.x;\n" +
					"	         ^^^^^\n" +
					"Name of automatic module \'lib.x\' is unstable, it is derived from the module\'s file name.\n" +
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
			assertProblemMarkers("markers in mod.one", "", javaProject.getProject());

			javaProject2.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertProblemMarkers("markers in mod.two", "", javaProject2.getProject());

			javaProject.getProject().getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
			assertNoErrors();
		} finally {
			if (javaProject != null)
				deleteProject(javaProject);
			if (javaProject2 != null)
				deleteProject(javaProject2);
		}
	}
	// like testAutoModule3 without name derived from project, not manifest - warning suppressed
	public void testAutoModule5() throws Exception {
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
				"@SuppressWarnings(\"module\")\n" +
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
	// like testAutoModule5, warning configured as ERROR
	public void testAutoModule6() throws Exception {
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
			javaProject.setOption(JavaCore.COMPILER_PB_UNSTABLE_AUTO_MODULE_NAME, JavaCore.ERROR);

			String srcMod =
				"module mod.one { \n" +
				"	requires auto;\n" +
				"}";
			createFile("/mod.one/src/module-info.java",
				srcMod);
			auto.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

			this.problemRequestor.initialize(srcMod.toCharArray());
			getWorkingCopy("/mod.one/module-info.java", srcMod, true);
			assertProblems("module-info should have only one error",
					"----------\n" +
					"1. ERROR in /mod.one/module-info.java (at line 2)\n" +
					"	requires auto;\n" +
					"	         ^^^^\n" +
					"Name of automatic module \'auto\' is unstable, it is derived from the module\'s file name.\n" +
					"----------\n",
					this.problemRequestor);
		} finally {
			if (javaProject != null)
				deleteProject(javaProject);
			if (auto != null)
				deleteProject(auto);
		}
	}

	// patch can see unexported type from host (and package accessible method), but not vice versa
	public void testPatch1() throws CoreException, IOException {
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
		try {
			IClasspathAttribute[] attributes = {
					JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true"),
					JavaCore.newClasspathAttribute(IClasspathAttribute.PATCH_MODULE, "java.desktop=/missing.path::java.base=/org.astro.patch/src"+File.pathSeparator+"/org.astro.patch/src2")
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
					JavaCore.newClasspathAttribute(IClasspathAttribute.PATCH_MODULE, "mod.one=/mod.one.patch")
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
		}
	}
	public void testLimitModules2() throws CoreException, IOException {
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
		}
	}
	public void testDefaultRootModules() throws CoreException, IOException {
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
		}
	}
	public void testBug522398() throws CoreException {
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
			deleteProject("nonmodular1");
			deleteProject("nonmodular2");
		}
	}
	public void testBug522330() throws CoreException, IOException {
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
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("unexpected markers",
					"The package javax.net is accessible from more than one module: <unnamed>, java.base\n" +
					"ServerSocketFactory cannot be resolved",
					markers);
		} finally {
			deleteProject("nonmodular1");
			deleteProject("nonmodular2");
		}
	}

	public void testBug522503() throws Exception {
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
	public void testBug522671() throws Exception {
		try {
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
				"public abstract class AnnotatedInModule {\n" +
				"	abstract public Data getTime();\n" +
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
			"import my.util.Data;\n" +
			"public class AnnotatedInOtherNonModule {\n" +
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

		this.problemRequestor.reset();
		ICompilationUnit cu = getCompilationUnit("/test/src/test/Test.java");
		cu.getWorkingCopy(this.wcOwner, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n",
			this.problemRequestor);
		} finally {
			deleteProject("util");
			deleteProject("util2");
			deleteProject("other");
			deleteProject("test");
		}
	}
	public void testBug522671b() throws CoreException {
		try {
			String[] sources = new String[] {
				"src/nonmodular1/HasConstructorWithProperties.java",
				"package nonmodular1;\n" +
				"\n" +
				"import java.util.Properties;\n" +
				"\n" +
				"public class HasConstructorWithProperties {\n" +
				"\n" +
				"	public HasConstructorWithProperties(Properties loadedProperties) {\n" +
				"	}\n" +
				"\n" +
				"	protected Properties method() {\n" +
				"		return null;\n" +
				"	}\n" +
				"\n" +
				"}\n" +
				"",
				"src/nonmodular1/HasPropertiesField.java",
				"package nonmodular1;\n" +
				"\n" +
				"import java.util.Properties;\n" +
				"\n" +
				"public class HasPropertiesField {\n" +
				"	Properties properties;\n" +
				"}\n" +
				"",
			};
			IJavaProject p1 = setupModuleProject("nonmodular1", sources);

			String[] sources2 = new String[] {
					"src/java/util/dummy/Dummy.java",
					"package java.util.dummy;\n" +
					"\n" +
					"public class Dummy {\n" +
					"}\n" +
					"\n" +
					"",
				};
			IJavaProject p2 = setupModuleProject("nonmodular2", sources2);
			p2.setOption(JavaCore.COMPILER_COMPLIANCE, "1.8"); // compile with 1.8 compliance to avoid error about package conflict

			IClasspathEntry dep1 = JavaCore.newProjectEntry(p1.getPath(), null, false,
					new IClasspathAttribute[] {},
					false/*not exported*/);
			IClasspathEntry dep2 = JavaCore.newProjectEntry(p2.getPath(), null, false,
					new IClasspathAttribute[] {},
					false/*not exported*/);
			String[] src = new String[] {
				"src/test/a/UsesHasPropertiesField.java",
				"package test.a;\n" +
				"\n" +
				"import nonmodular1.HasPropertiesField;\n" +
				"\n" +
				"public class UsesHasPropertiesField extends HasPropertiesField {\n" +
				"}\n" +
				"",
				"src/test/b/Test.java",
				"package test.b;\n" +
				"\n" +
				"import java.util.Properties;\n" +
				"\n" +
				"import nonmodular1.HasConstructorWithProperties;\n" +
				"\n" +
				"public final class Test {\n" +
				"	public static Object test(Properties cassandraConf) {\n" +
				"		return new HasConstructorWithProperties(cassandraConf);\n" +
				"	}\n" +
				"}\n" +
				"",
			};
			IJavaProject p3 = setupModuleProject("nonmodular3", src, new IClasspathEntry[] { dep1, dep2 });
			p3.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();
		} finally {
			deleteProject("nonmodular1");
			deleteProject("nonmodular2");
			deleteProject("nonmodular3");
		}
	}

	public void testBug525522() throws Exception {
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
			deleteProject("jnlp");
			deleteProject("nonmod1");
		}
	}

	public void testBug525603() throws Exception {
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
			assertProblems("module-info should have exactly one warning",
					"----------\n" +
					"1. WARNING in /mod1/src/module-info.java (at line 3)\n" +
					"	requires automod;\n" +
					"	         ^^^^^^^\n" +
					"Name of automatic module \'automod\' is unstable, it is derived from the module\'s file name.\n" +
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

	public void testBug526054() throws Exception {
		// JDK 15 has removed the only module that was not part of module default root module jdk.rmic.
		// Hence, we no longer need this test for JDK 15 and above.
		if (!isJRE9 || isJRE15) return;
		ClasspathJrt.resetCaches();
		try {
			// jdk.rmic is not be visible to code in an unnamed module, but using requires we can see the module.
			// only, there's nothing exported from it (which is why JEP 261 hides it from unnamed), so we --add-reads:
			IClasspathAttribute[] attrs = new IClasspathAttribute[] {
				JavaCore.newClasspathAttribute(IClasspathAttribute.ADD_EXPORTS, "jdk.rmic/sun.rmi.rmic=mod1")
			};
			IJavaProject javaProject = createJava9ProjectWithJREAttributes("mod1", new String[] {"src"}, attrs);

			String srcMod =
				"module mod1 {\n" +
				"	exports com.mod1.pack1;\n" +
				"	requires jdk.rmic;\n" +
				"}";
			createFile("/mod1/src/module-info.java",
				srcMod);
			createFolder("/mod1/src/com/mod1/pack1");
			String srcX =
				"package com.mod1.pack1;\n" +
				"import sun.rmi.rmic.Main;\n" +
				"public class Dummy {\n" +
				"	String test() {\n" +
				"		return Main.getString(\"in\");\n" +
				"	}\n" +
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
			deleteProject("mod1");
		}
	}

	public void testBug526054b() throws Exception {
		// JDK 15 has removed the only module that was not part of module default root module jdk.rmic.
		// Hence, we no longer need this test for JDK 15 and above.
		if (!isJRE9 || isJRE15) return;
		ClasspathJrt.resetCaches();
		try {
			// one project can see jdk.rmic/sun.rmi.rmic
			IClasspathAttribute[] attrs = new IClasspathAttribute[] {
				JavaCore.newClasspathAttribute(IClasspathAttribute.ADD_EXPORTS, "jdk.rmic/sun.rmi.rmic=mod1")
			};
			createJava9ProjectWithJREAttributes("mod1", new String[] {"src"}, attrs);

			String srcMod1 =
				"module mod1 {\n" +
				"	exports com.mod1.pack1;\n" +
				"	requires jdk.rmic;\n" +
				"}";
			createFile("/mod1/src/module-info.java",
				srcMod1);
			createFolder("/mod1/src/com/mod1/pack1");
			String srcX1 =
				"package com.mod1.pack1;\n" +
				"import sun.rmi.rmic.Constants;\n" + // this should never be complained against due to above add-exports.
				"public class Dummy implements Constants {\n" +
				"}";
			createFile("/mod1/src/com/mod1/pack1/Dummy.java", srcX1);

			// second project cannot see jdk.rmic/sun.rmi.rmic:
			createJava9Project("mod2", new String[] {"src"});

			String srcMod2 =
				"module mod2 {\n" +
				"	exports com.mod2.pack1;\n" +
				"	requires jdk.rmic;\n" +
				"}";
			createFile("/mod2/src/module-info.java",
				srcMod2);
			createFolder("/mod2/src/com/mod2/pack1");
			String srcX2 =
				"package com.mod2.pack1;\n" +
				"import sun.rmi.rmic.Main;\n" +
				"public class Dummy {\n" +
				"	String test() {\n" +
				"		return Main.getString(\"in\");\n" +
				"	}\n" +
				"}";
			createFile("/mod2/src/com/mod2/pack1/Dummy.java", srcX2);

			// check first:
			this.problemRequestor.initialize(srcX1.toCharArray());
			getWorkingCopy("/mod1/src/com/mod1/pack1/Dummy.java", srcX1, true);
			assertProblems("Dummy in mod1 should have no problems",
					"----------\n" +
					"----------\n",
					this.problemRequestor);

			// check second:
			this.problemRequestor.initialize(srcX2.toCharArray());
			getWorkingCopy("/mod2/src/com/mod2/pack1/Dummy.java", srcX2, true);
			assertProblems("Dummy in mod2 should have problems",
					"----------\n" +
					"1. ERROR in /mod2/src/com/mod2/pack1/Dummy.java (at line 2)\n" +
					"	import sun.rmi.rmic.Main;\n" +
					"	       ^^^^^^^^^^^^^^^^^\n" +
					"The type sun.rmi.rmic.Main is not accessible\n" +
					"----------\n" +
					"2. ERROR in /mod2/src/com/mod2/pack1/Dummy.java (at line 5)\n" +
					"	return Main.getString(\"in\");\n" +
					"	       ^^^^\n" +
					"Main cannot be resolved\n" +
					"----------\n",
					this.problemRequestor);

			// check both in a combined build
			getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = getWorkspace().getRoot().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers",
					"The type sun.rmi.rmic.Main is not accessible\n" +
					"Main cannot be resolved",
					markers);
		} finally {
			deleteProject("mod1");
			deleteProject("mod2");
		}
	}

	public void testBug525918() throws CoreException {
		try {
			String[] sources = new String[] {
				"src/module-info.java",
				"import p.MyAbstractDriver;\n" +
				"import p.MyAbstractDriverWithProvider;\n" +
				"import p.MyDriverInf;\n" +
				"import p.MyInfWithProvider;\n" +
				"module test {\n" +
				"	requires java.sql;\n" +
				"	provides java.sql.Driver with MyDriverInf, MyAbstractDriver, MyInfWithProvider, MyAbstractDriverWithProvider;" +
				"}",
				"src/p/MyDriverInf.java",
				"package p;\n" +
				"public interface MyDriverInf extends java.sql.Driver { }",
				"src/p/MyAbstractDriver.java",
				"package p;\n" +
				"public abstract class MyAbstractDriver {\n" +
				"	public MyAbstractDriver() { }\n" +
				"}",
				"src/p/MyInfWithProvider.java",
				"package p;\n" +
				"public interface MyInfWithProvider {\n" +
				"	public static java.sql.Driver provider() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n",
				"src/p/MyAbstractDriverWithProvider.java",
				"package p;\n" +
				"public abstract class MyAbstractDriverWithProvider {\n" +
				"	public static java.sql.Driver provider() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}"
			};
			IJavaProject p1 = setupModuleProject("test", sources);
			p1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			super.sortMarkers(markers);
			assertMarkers("Unexpected markers",
				"Invalid service implementation, the type p.MyAbstractDriver is abstract\n" +
				"Invalid service implementation, the type p.MyDriverInf is abstract\n" +
				"Type mismatch: cannot convert from MyAbstractDriver to Driver"
				, markers);
		} finally {
			deleteProject("test");
		}
	}

	public void testBug527576() throws Exception {
		IJavaProject javaProject = null;
		try {

			javaProject = createJava9Project("mod1", new String[] {"src"});
			String[] sources = {
					"org/junit/Assert.java",
					"package org.junit;\n" +
					"public class Assert {}\n;"
				};

			Path jarPath = new Path('/' + javaProject.getProject().getName() + '/' + "localjunit.jar");
			Util.createJar(sources, javaProject.getProject().getWorkspace().getRoot().getFile(jarPath).getRawLocation().toOSString(), "1.8");
			javaProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);

			addClasspathEntry(javaProject, JavaCore.newLibraryEntry(jarPath, null, null, null, null, false));

			String srcMod =
				"module mod1 {\n" +
				"}";
			createFile("/mod1/src/module-info.java",
				srcMod);
			createFolder("/mod1/src/com/mod1/pack1");
			String srcX =
				"package com.mod1.pack1;\n" +
				"import org.junit.Assert;\n" +
				"public class Dummy extends Assert {\n" +
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
			assertProblems("X should have errors because Assert should not be visible",
					"----------\n" +
					"1. ERROR in /mod1/src/com/mod1/pack1/Dummy.java (at line 2)\n" +
					"	import org.junit.Assert;\n" +
					"	       ^^^^^^^^^^^^^^^^\n" +
					"The type org.junit.Assert is not accessible\n" +
					"----------\n" +
					"2. ERROR in /mod1/src/com/mod1/pack1/Dummy.java (at line 3)\n" +
					"	public class Dummy extends Assert {\n" +
					"	                           ^^^^^^\n" +
					"Assert cannot be resolved to a type\n" +
					"----------\n",
					this.problemRequestor);

			javaProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = javaProject.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers",
					"The type org.junit.Assert is not accessible\n" +
					"Assert cannot be resolved to a type",
					markers);
		} finally {
			if (javaProject != null)
				deleteProject(javaProject);
		}
	}
	public void testBug528467a() throws CoreException {
		IJavaProject p1 = createJava9Project("mod.one");
		try {
			IClasspathEntry[] rawClasspath = p1.getRawClasspath();
			String jrtPath = null;
			for (int i = 0; i < rawClasspath.length; i++) {
				IClasspathEntry iClasspathEntry = rawClasspath[i];
				if (iClasspathEntry.getEntryKind() == IClasspathEntry.CPE_LIBRARY &&
						iClasspathEntry.getPath().toString().endsWith("jrt-fs.jar")) {
					jrtPath = iClasspathEntry.getPath().toOSString();
					IAccessRule[] pathRules = new IAccessRule[1];
					pathRules[0] = JavaCore.newAccessRule(new Path("java/awt/**"), IAccessRule.K_NON_ACCESSIBLE);
					IClasspathEntry newEntry = JavaCore.newLibraryEntry(iClasspathEntry.getPath(),
							iClasspathEntry.getSourceAttachmentPath(),
							iClasspathEntry.getSourceAttachmentRootPath(),
								pathRules,
								iClasspathEntry.getExtraAttributes(),
								iClasspathEntry.isExported());
					rawClasspath[i] = newEntry;
					break;
				}
			}
			p1.setRawClasspath(rawClasspath, null);
			createFolder("/mod.one/src/p1");
			createFile("/mod.one/src/module-info.java",
					"module mod.one {\n" +
					"	exports p1;\n" +
					"	requires java.desktop;\n" +
					"}\n");
			createFile("/mod.one/src/p1/X.java",
					"package p1;\n" +
							"public class X {\n"
							+ "    java.awt.Image im = null;\n"
							+ "}\n");

			waitForManualRefresh();
			waitForAutoBuild();
			p1.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);

			assertMarkers("Unexpected markers", "Access restriction: The type 'Image' is not API (restriction on required library '"+
																							jrtPath + "')", markers);
		} finally {
			deleteProject(p1);
		}
	}
	public void testBug528467b() throws CoreException {
		IJavaProject p1 = createJava9Project("mod.one");
		try {
			IClasspathEntry[] rawClasspath = p1.getRawClasspath();
			String jrtPath = null;
			for (int i = 0; i < rawClasspath.length; i++) {
				IClasspathEntry iClasspathEntry = rawClasspath[i];
				if (iClasspathEntry.getEntryKind() == IClasspathEntry.CPE_LIBRARY &&
						iClasspathEntry.getPath().toString().endsWith("jrt-fs.jar")) {
					jrtPath = iClasspathEntry.getPath().toOSString();
					IAccessRule[] pathRules = new IAccessRule[1];
					pathRules[0] = JavaCore.newAccessRule(new Path("java/awt/Image"), IAccessRule.K_NON_ACCESSIBLE);
					IClasspathEntry newEntry = JavaCore.newLibraryEntry(iClasspathEntry.getPath(),
							iClasspathEntry.getSourceAttachmentPath(),
							iClasspathEntry.getSourceAttachmentRootPath(),
								pathRules,
								iClasspathEntry.getExtraAttributes(),
								iClasspathEntry.isExported());
					rawClasspath[i] = newEntry;
					break;
				}
			}
			p1.setRawClasspath(rawClasspath, null);
			createFolder("/mod.one/src/p1");
			createFile("/mod.one/src/module-info.java",
					"module mod.one {\n" +
					"	exports p1;\n" +
					"	requires java.desktop;\n" +
					"}\n");
			createFile("/mod.one/src/p1/X.java",
					"package p1;\n" +
					"import java.awt.*;\n" +
					"public abstract class X extends Image {\n" +
					"	public Graphics foo() {\n" +
					"		return getGraphics();\n" +
					"	}\n"
					+ "}\n");

			waitForManualRefresh();
			waitForAutoBuild();
			p1.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);

			assertMarkers("Unexpected markers",
					"Access restriction: The type \'Image\' is not API (restriction on required library '"+ jrtPath + "')\n" +
					"The type Graphics from module java.desktop may not be accessible to clients due to missing \'requires transitive\'\n" +
					"Access restriction: The method \'Image.getGraphics()\' is not API (restriction on required library '"+ jrtPath + "')", markers);
		} finally {
			deleteProject(p1);
		}
	}
	public void testBug528467c() throws CoreException {
		IJavaProject p1 = createJava9Project("unnamed");
		try {
			IClasspathEntry[] rawClasspath = p1.getRawClasspath();
			String jrtPath = null;
			for (int i = 0; i < rawClasspath.length; i++) {
				IClasspathEntry iClasspathEntry = rawClasspath[i];
				if (iClasspathEntry.getEntryKind() == IClasspathEntry.CPE_LIBRARY &&
						iClasspathEntry.getPath().toString().endsWith("jrt-fs.jar")) {
					jrtPath = iClasspathEntry.getPath().toOSString();
					IAccessRule[] pathRules = new IAccessRule[1];
					pathRules[0] = JavaCore.newAccessRule(new Path("java/awt/**"), IAccessRule.K_NON_ACCESSIBLE);
					IClasspathEntry newEntry = JavaCore.newLibraryEntry(iClasspathEntry.getPath(),
							iClasspathEntry.getSourceAttachmentPath(),
							iClasspathEntry.getSourceAttachmentRootPath(),
								pathRules,
								iClasspathEntry.getExtraAttributes(),
								iClasspathEntry.isExported());
					rawClasspath[i] = newEntry;
					break;
				}
			}
			p1.setRawClasspath(rawClasspath, null);
			createFolder("/unnamed/src/p1");
			createFile("/unnamed/src/p1/X.java",
					"package p1;\n" +
							"public class X {\n"
							+ "    java.awt.Image im = null;\n"
							+ "}\n");

			waitForManualRefresh();
			waitForAutoBuild();
			p1.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);

			assertMarkers("Unexpected markers", "Access restriction: The type 'Image' is not API (restriction on required library '"+
																							jrtPath + "')", markers);
		} finally {
			deleteProject(p1);
		}
	}
	// Bug 520713: allow test code to access code on the classpath
	public void testWithTestAttributeAndTestDependencyOnClassPath() throws CoreException, IOException {
		String outputDirectory = Util.getOutputDirectory();

		String jarPath = outputDirectory + File.separator + "mytestlib.jar";
		IJavaProject project1 = null;
		IJavaProject project2 = null;
		try {
			String[] sources = {
				"my/test/Test.java",
				"package my.test;\n" +
				"public class Test {}\n;"
			};
			Util.createJar(sources, jarPath, "1.8");

			project1 = createJava9Project("Project1", new String[] {"src"});
			addClasspathEntry(project1, JavaCore.newSourceEntry(new Path("/Project1/src-tests"), null, null, new Path("/Project1/bin-tests"), new IClasspathAttribute[] { JavaCore.newClasspathAttribute(IClasspathAttribute.TEST, "true") }));
			addClasspathEntry(project1, JavaCore.newLibraryEntry(new Path(jarPath), null, null, null, new IClasspathAttribute[] { JavaCore.newClasspathAttribute(IClasspathAttribute.TEST, "true") }, false));

			createFolder("/Project1/src/p1");
			createFolder("/Project1/src-tests/p1");
			createFile("/Project1/src/module-info.java",
					"module m1 {\n" +
					"	exports p1;\n" +
					"}");
			createFile("/Project1/src/p1/P1Class.java",
					"package p1;\n" +
					"\n" +
					"public class P1Class {\n"+
					"}\n"
					);
			createFile("/Project1/src/p1/Production1.java",
					"package p1;\n" +
					"\n" +
					"public class Production1 {\n" +
					"	void p1() {\n" +
					"		new P1Class(); // ok\n" +
					"		new T1Class(); // forbidden\n" +
					"	}\n" +
					"}\n" +
					""
					);
			createFile("/Project1/src-tests/p1/T1Class.java",
					"package p1;\n" +
					"\n" +
					"public class T1Class {\n"+
					"}\n"
					);
			createFile("/Project1/src-tests/p1/Test1.java",
					"package p1;\n" +
					"\n" +
					"public class Test1 extends my.test.Test {\n" +
					"	void test1() {\n" +
					"		new P1Class(); // ok\n" +
					"		new T1Class(); // ok\n" +
					"	}\n" +
					"}\n" +
					""
					);
			project1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

			IMarker[] markers = project1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers",
					"T1Class cannot be resolved to a type" +
					"",
					markers);

			project2 = createJava9Project("Project2", new String[] {"src"});
			addClasspathEntry(project2, JavaCore.newProjectEntry(new Path("/Project1"), null, false, new IClasspathAttribute[] { JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true") }, false));
			addClasspathEntry(project2, JavaCore.newSourceEntry(new Path("/Project2/src-tests"), null, null, new Path("/Project2/bin-tests"), new IClasspathAttribute[] { JavaCore.newClasspathAttribute(IClasspathAttribute.TEST, "true") }));
			addClasspathEntry(project2, JavaCore.newLibraryEntry(new Path(jarPath), null, null, null, new IClasspathAttribute[] { JavaCore.newClasspathAttribute(IClasspathAttribute.TEST, "true") }, false));
			createFolder("/Project2/src/p2");
			createFolder("/Project2/src-tests/p2");
			createFile("/Project2/src/module-info.java",
					"module m2 {\n" +
					"	requires m1;\n" +
					"}");
			createFile("/Project2/src/p2/P2Class.java",
					"package p2;\n" +
					"\n" +
					"public class P2Class {\n"+
					"}\n"
					);
			createFile("/Project2/src/p2/Production2.java",
					"package p2;\n" +
					"\n" +
					"import p1.P1Class;\n" +
					"import p1.T1Class;\n" +
					"\n" +
					"public class Production2 {\n" +
					"	void p2() {\n" +
					"		new P1Class(); // ok\n" +
					"		new P2Class(); // ok\n" +
					"		new T1Class(); // forbidden\n" +
					"		new T2Class(); // forbidden\n" +
					"	}\n" +
					"}\n" +
					""
					);
			createFile("/Project2/src-tests/p2/T2Class.java",
					"package p2;\n" +
					"\n" +
					"public class T2Class {\n"+
					"}\n"
					);
			createFile("/Project2/src-tests/p2/Test2.java",
					"package p2;\n" +
					"\n" +
					"import p1.P1Class;\n" +
					"import p1.T1Class;\n" +
					"\n" +
					"public class Test2 extends p1.Test1 {\n" +
					"	void test2() {\n" +
					"		new P1Class(); // ok\n" +
					"		new P2Class(); // ok\n" +
					"		new T1Class(); // ok\n" +
					"		new T2Class(); // ok\n" +
					"	}\n" +
					"}\n" +
					""
					);
			project1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

			// regression test for Bug 569512 - CCE in State.writeBinaryLocations:
			IStatus saveStatus = project1.getProject().getWorkspace().save(true, null);
			if (!saveStatus.isOK() && saveStatus.isMultiStatus())
				throw new AssertionError(saveStatus.getChildren()[0].getException());

			IMarker[] markers2 = project2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers2);
			assertMarkers("Unexpected markers",
					"The import p1.T1Class cannot be resolved\n" +
					"T1Class cannot be resolved to a type\n" +
					"T2Class cannot be resolved to a type",
					markers2);
		} finally {
			if (project1 != null)
				deleteProject(project1);
			if (project2 != null)
				deleteProject(project2);
			new File(jarPath).delete();
		}
	}

	public void testBug531579() throws Exception {
		String outputDirectory = Util.getOutputDirectory();

		String jarPath = outputDirectory + File.separator + "jaxb-api.jar";
		IJavaProject project1 = null;
		try {
			// these types replace inaccessible types from JRE/javax.xml.bind:
			// (not a problem during IDE builds)
			String[] sources = {
				"javax/xml/bind/JAXBContext.java",
				"package javax.xml.bind;\n" +
				"public abstract class JAXBContext {\n" +
				"	public static JAXBContext newInstance( String contextPath )\n" +
				"		throws JAXBException {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n",
				"javax/xml/bind/JAXBException.java",
				"package javax.xml.bind;\n" +
				"public class JAXBException extends Exception {}\n"
			};
			Util.createJar(sources, jarPath, "1.8");

			project1 = createJava9Project("Project1", new String[] {"src"});
			addClasspathEntry(project1, JavaCore.newLibraryEntry(new Path(jarPath), null, null));

			createFolder("/Project1/src/p1");
			createFile("/Project1/src/p1/ImportJAXBType.java",
					"package p1;\n" +
					"\n" +
					"import javax.xml.bind.JAXBContext;\n" +
					"\n" +
					"public class ImportJAXBType {\n" +
					"\n" +
					"	public static void main(String[] args) throws Exception {\n" +
					"		JAXBContext context = JAXBContext.newInstance(\"\");\n" +
					"	}\n" +
					"\n" +
					"}\n"
					);

			project1.getProject().getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

			IMarker[] markers = project1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers",
					"The value of the local variable context is not used",
					markers);
		} finally {
			if (project1 != null)
				deleteProject(project1);
			new File(jarPath).delete();
		}
	}
	public void testBug527569a() throws CoreException {
		if (!isJRE19) return;
		IJavaProject p1 = createJava9Project("Bug527569", "17");
		try {
			createFolder("/Bug527569/src/p1");
			createFile("/Bug527569/src/p1/X.java",
					"package p1;\n" +
					"public class X {\n" +
					"	public java.lang.MatchException getException() {\n" +
					"		return null;\n" +
					"	}\n" +
					"}");

			waitForManualRefresh();
			waitForAutoBuild();
			p1.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
			deleteProject(p1);
		}
	}
	public void testBug527569b() throws CoreException {
		if (!isJRE19) return;
		IJavaProject p1 = createJava9Project("Bug527569", "17");
		try {
			createFolder("/Bug527569/src/p1");
			createFile("/Bug527569/src/p1/X.java",
					"package p1;\n" +
					"public class X {\n" +
					"	public java.lang.MatchException getException() {\n" +
					"		return null;\n" +
					"	}\n" +
					"}");

			waitForManualRefresh();
			waitForAutoBuild();
			p1.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
			deleteProject(p1);
		}
	}
	public void testBug527569c() throws CoreException {
		if (!isJRE19) return;
		IJavaProject p1 = createJava9Project("Bug527569", "17");
		Map<String, String> options = new HashMap<>();
		// Make sure the new options map doesn't reset.
		options.put(CompilerOptions.OPTION_Compliance, "17");
		options.put(CompilerOptions.OPTION_Source, "17");
		options.put(CompilerOptions.OPTION_TargetPlatform, "17");
		options.put(CompilerOptions.OPTION_Release, "enabled");
		p1.setOptions(options);
		try {
			createFolder("/Bug527569/src/p1");
			createFile("/Bug527569/src/p1/X.java",
					"package p1;\n" +
					"public class X {\n" +
					"	public java.lang.MatchException getException() {\n" +
					"		return null;\n" +
					"	}\n" +
					"}");

			waitForManualRefresh();
			waitForAutoBuild();
			p1.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "java.lang.MatchException cannot be resolved to a type", markers);
		} finally {
			deleteProject(p1);
		}
	}
	public void testBug527569d() throws CoreException {
		IJavaProject p1 = createJava9Project("Bug527569", "9");
		try {
			createFolder("/Bug527569/src/p1");
			createFile("/Bug527569/src/p1/X.java",
					"package p1;\n" +
					"public class X {\n" +
					"	public java.lang.Compiler getCompiler() {\n" +
					"		return null;\n" +
					"	}\n" +
					"}");

			waitForManualRefresh();
			waitForAutoBuild();
			p1.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "The type Compiler has been deprecated since version 9 and marked for removal", markers);
		} finally {
			deleteProject(p1);
		}
	}
	public void testBug527569e() throws CoreException {
		if (!isJRE9 || isJRE12) return;
		IJavaProject p1 = createJava9Project("Bug527569", "1.8");
		Map<String, String> options = new HashMap<>();
		// Make sure the new options map doesn't reset.
		options.put(CompilerOptions.OPTION_Compliance, "1.7");
		options.put(CompilerOptions.OPTION_Source, "1.7");
		options.put(CompilerOptions.OPTION_TargetPlatform, "1.7");
		options.put(CompilerOptions.OPTION_Release, "enabled");
		p1.setOptions(options);
		try {
			createFolder("/Bug527569/src/p1");
			createFile("/Bug527569/src/p1/X.java",
					"package p1;\n" +
					"public class X {\n" +
					"	public java.lang.Compiler getCompiler() {\n" +
					"		return null;\n" +
					"	}\n" +
					"}");

			waitForManualRefresh();
			waitForAutoBuild();
			p1.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
			deleteProject(p1);
		}
	}
	public void testBug522601() throws CoreException {
		IJavaProject p1 = createJava9Project("Bug522601", "9");
		try {
			IFile file = createFile("/Bug522601/test.txt", "not a jar");
			IClasspathAttribute modAttr = JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true");
			addLibraryEntry(p1, file.getFullPath(), null, null, null, null, new IClasspathAttribute[] { modAttr }, false);
			p1.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers",
					"Archive for required library: \'test.txt\' in project \'Bug522601\' cannot be read or is not a valid ZIP file\n" +
					"The project cannot be built until build path errors are resolved", markers);
		} finally {
			deleteProject(p1);
		}
	}
	// automatic modules export all their packages
	public void testBug532724() throws CoreException, IOException {
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
					"	requires transitive test;\n" +
					"	exports com.greetings;\n" +
					"}",
					"src/com/greetings/MyWorld.java",
					"package com.greetings;\n" +
					"import org.astro.World;\n"	+
					"public class MyWorld {\n" +
					"	public World name() {\n" +
					"		return null;\n" +
					"	}\n" +
					"}"
			};
			IClasspathAttribute modAttr = new ClasspathAttribute("module", "true");
			IClasspathEntry dep = JavaCore.newLibraryEntry(new Path(getExternalResourcePath(libPath)), null, null, ClasspathEntry.NO_ACCESS_RULES,
					new IClasspathAttribute[] {modAttr},
					false/*not exported*/);
			IJavaProject p2 = setupModuleProject("com.greetings", src, new IClasspathEntry[] { dep });
			p2.setOption(JavaCore.COMPILER_PB_UNSTABLE_AUTO_MODULE_NAME, JavaCore.IGNORE);
			p2.setOption(JavaCore.COMPILER_PB_API_LEAKS, JavaCore.ERROR);

			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
			deleteExternalResource("externalLib");
			this.deleteProject("com.greetings");
		}
	}
	public void testBug534624a() throws CoreException, IOException {
		IJavaProject project = null;
		Hashtable<String, String> options = JavaCore.getOptions();
		try {
			project = setUpJavaProject("bug.test.b534624");
			IClasspathEntry[] rawClasspath = project.getRawClasspath();
			IClasspathEntry jrtEntry = getJRTLibraryEntry();
			for(int i = 0; i < rawClasspath.length; i++) {
				if (rawClasspath[i].getEntryKind() == IClasspathEntry.CPE_CONTAINER)
					rawClasspath[i] = jrtEntry;
			}
			project.setRawClasspath(rawClasspath, null);
			project.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_10);
			project.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_10);
			project.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_10);
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = project.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "Version9 cannot be resolved to a module", markers);
		} finally {
			if (project != null)
				deleteProject(project);
			JavaCore.setOptions(options);
		}
	}
	public void testBug534624b() throws CoreException, IOException {
		IJavaProject project = null;
		Hashtable<String, String> options = JavaCore.getOptions();
		try {
			project = setUpJavaProject("bug.test.b534624");
			IClasspathEntry[] rawClasspath = project.getRawClasspath();
			IClasspathEntry jrtEntry = getJRTLibraryEntry();
			for(int i = 0; i < rawClasspath.length; i++) {
				if (rawClasspath[i].getEntryKind() == IClasspathEntry.CPE_CONTAINER)
					rawClasspath[i] = jrtEntry;
			}
			project.setRawClasspath(rawClasspath, null);
			project.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_9);
			project.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_9);
			project.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_9);
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = project.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "Version10 cannot be resolved to a module", markers);

			project.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_10);
			project.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_10);
			project.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_10);
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			markers = project.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "Version9 cannot be resolved to a module", markers);
		} finally {
			if (project != null)
				deleteProject(project);
			JavaCore.setOptions(options);
		}
	}
	// missing linked jar must not cause NPE
	public void testBug540904() throws CoreException, IOException {
		try {
			String[] src = new String[] {
					"src/test/Test.java",
					"package test;\n" +
					"public class Test {\n" +
					"}"
			};
			IJavaProject p2 = setupModuleProject("Bug540904", src, new IClasspathEntry[] {  });
			IFile file = getFile("/Bug540904/link.jar");
			file.createLink(new Path("MISSING/missing.jar"), IResource.ALLOW_MISSING_LOCAL, null);
			addLibraryEntry(p2, file.getFullPath(), false);
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
			this.deleteProject("Bug540904");
		}
	}
	public void testBug540788() throws Exception {
		try {
			// project common:
			IJavaProject common = createJava9Project("Bug540788.common", new String[] { "src/main/java" });
			createSourceFiles(common,
					new String[] {
						"src/main/java/module-info.java",
						"module org.sheepy.common {\n" +
						"	requires transitive org.eclipse.emf.common;\n" +
						"	requires transitive org.eclipse.emf.ecore;\n" +
						"}\n"
					});
			IFolder libs = createFolder("/Bug540788.common/libs");
			String emfCommonLocation = libs.getLocation()+"/org.eclipse.emf.common.jar";
			Path emfCommonPath = new Path(emfCommonLocation);
			Util.createJar(
					new String[] {
							"src/org/eclipse/emf/common/Foo.java",
							"package org.eclipse.emf.common;\n" +
							"public interface Foo {\n" +
							"}",
					},
					null,
					new HashMap<>(),
					null,
					emfCommonLocation);
			addModularLibraryEntry(common, emfCommonPath, null);

			String ecoreLocation = libs.getLocation()+"/org.eclipse.emf.ecore.jar";
			Path ecorePath = new Path(ecoreLocation);
			Util.createJar(
					new String[] {
						"src/org/eclipse/emf/ecore/EObject.java",
						"package org.eclipse.emf.ecore;\n" +
						"public interface EObject {\n" +
						"}",
					},
					null,
					new HashMap<>(),
					null,
					ecoreLocation);
			addModularLibraryEntry(common, ecorePath, null);
			// project vulkan:
			IJavaProject vulkan = createJava9Project("Bug540788.vulkan", new String[] { "src/main/java" });
			createSourceFiles(vulkan,
					new String[] {
						"src/main/java/module-info.java",
						"module org.sheepy.vulkan {\n" +
						"	requires transitive org.sheepy.common;\n" +
						"	exports org.sheepy.vulkan.model.resource;\n" +
						"}\n",
						"src/main/java/org/sheepy/vulkan/model/resource/Resource.java",
						"package org.sheepy.vulkan.model.resource;\n" +
						"import org.eclipse.emf.ecore.EObject;\n" +
						"public interface Resource extends EObject {\n" +
						"}\n",
						"src/main/java/org/sheepy/vulkan/model/resource/VulkanBuffer.java",
						"package org.sheepy.vulkan.model.resource;\n" +
						"public interface VulkanBuffer extends Resource {\n" +
						"}\n",
					});
			addModularProjectEntry(vulkan, common);
			addModularLibraryEntry(vulkan, emfCommonPath, null);
			addModularLibraryEntry(vulkan, ecorePath, null);
			// project vulkan.demo
			IJavaProject vulkan_demo = createJava9Project("Bug540788.vulkan.demo", new String[] { "src/main/java" });
			createSourceFiles(vulkan_demo,
					new String[] {
						"src/main/java/module-info.java",
						"module org.sheepy.vulkan.demo {\n" +
						"	exports org.sheepy.vulkan.demo.model;\n" +
						"	requires org.sheepy.vulkan;\n" +
						"}\n",
						"src/main/java/org/sheepy/vulkan/demo/model/UniformBuffer.java",
						"package org.sheepy.vulkan.demo.model;\n" +
						"import org.sheepy.vulkan.model.resource.VulkanBuffer;\n" +
						"public interface UniformBuffer extends VulkanBuffer {\n" +
						"}\n",
					});
			addModularProjectEntry(vulkan_demo, vulkan);
			addModularProjectEntry(vulkan_demo, common);
			addModularLibraryEntry(vulkan_demo, emfCommonPath, null);
			addModularLibraryEntry(vulkan_demo, ecorePath, null);

			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = vulkan_demo.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
			deleteProject("Bug540788.common");
			deleteProject("Bug540788.vulkan");
			deleteProject("Bug540788.vulkan.demo");
		}
	}
	public void testBug541015() throws Exception {
		try {
			IJavaProject m1 = createJava9Project("m1", new String[] { "src" });
			createSourceFiles(m1,
					new String[] {
						"src/module-info.java",
						"module m1 { exports org.p1; }\n",
						"src/org/p1/T1.java",
						"package org.p1;\n" +
						"public class T1 {}\n"
					});
			IJavaProject m2 = createJava9Project("m2", new String[] { "src" });
			createSourceFiles(m2,
					new String[] {
						"src/module-info.java",
						"module m2 { exports org.p1; }\n",
						"src/org/p1/T1.java",
						"package org.p1;\n" +
						"public class T1 {}\n"
					});
			IJavaProject m3 = createJava9Project("m3", new String[] { "src" });
			createSourceFiles(m3,
					new String[] {
						"src/module-info.java",
						"module m3 { exports org.p1; }\n",
						"src/org/p1/T1.java",
						"package org.p1;\n" +
						"public class T1 {}\n"
					});
			IJavaProject unnamed = createJava9Project("unnamed", new String[] { "src" });
			String testSource = "package test;\n" +
			"import org.p1.T1;\n" +
			"public class Test {\n" +
			"	T1 t1;\n" +
			"}\n";
			createSourceFiles(unnamed,
					new String[] {
						"src/test/Test.java",
						testSource
					});
			addModularProjectEntry(unnamed, m1);
			addModularProjectEntry(unnamed, m2);
			addModularProjectEntry(unnamed, m3);

			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = unnamed.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers",
					"The package org.p1 is accessible from more than one module: m1, m2, m3\n" +
					"T1 cannot be resolved to a type",
					markers);

			char[] sourceChars = testSource.toCharArray();
			this.problemRequestor.initialize(sourceChars);
			getCompilationUnit("/unnamed/src/test/Test.java").getWorkingCopy(this.wcOwner, null);
			assertProblems(
					"Unexpected problems",
					"----------\n" +
					"1. ERROR in /unnamed/src/test/Test.java (at line 2)\n" +
					"	import org.p1.T1;\n" +
					"	       ^^^^^^\n" +
					"The package org.p1 is accessible from more than one module: m1, m2, m3\n" +
					"----------\n" +
					"2. ERROR in /unnamed/src/test/Test.java (at line 4)\n" +
					"	T1 t1;\n" +
					"	^^\n" +
					"T1 cannot be resolved to a type\n" +
					"----------\n",
					this.problemRequestor);
		} finally {
			deleteProject("m1");
			deleteProject("m2");
			deleteProject("m3");
			deleteProject("unnamed");
		}
	}
	public void testBug536928_comment22() throws CoreException, IOException {
		try {
			IJavaProject project = createJava9Project("ztest", new String[] { "src" });
			createFolder("/ztest/lib");
			Util.createJar(new String[] {
					"javax/xml/transform/Transformer.java",
					"package javax.xml.transform;\n" +
					"public class Transformer {}\n",
					"javax/xml/transform/Result.java",
					"package javax.xml.transform;\n" +
					"public class Result {}\n"
				},
				project.getProject().getLocation().toString() + "/lib/xml-apis.jar",
				"1.8");
			project.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
			IClasspathEntry libraryEntry = JavaCore.newLibraryEntry(new Path("/ztest/lib/xml-apis.jar"), null, null);
			addClasspathEntry(project, libraryEntry, 1); // right after src and before jrt-fs.jar

			String testSource =
					"package com.ztest;\n" +
					"import javax.xml.transform.Transformer;\n" +
					"\n" +
					"public class TestApp {\n" +
					"	Transformer ts;\n" +
					"	javax.xml.transform.Result result;\n" +
					"}\n";
			createFolder("/ztest/src/com/ztest");
			createFile("/ztest/src/com/ztest/TestApp.java", testSource);
			String test2Source =
					"package com.ztest;\n" +
					"import javax.xml.transform.*;\n" +
					"public class Test2 {\n" +
					"	Transformer ts;\n" +
					"}\n";
			createFile("/ztest/src/com/ztest/Test2.java", test2Source);
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = project.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected Markers",
					"The package javax.xml.transform is accessible from more than one module: <unnamed>, java.xml\n" +
					"The package javax.xml.transform is accessible from more than one module: <unnamed>, java.xml\n" +
					"Transformer cannot be resolved to a type\n" +
					"Transformer cannot be resolved to a type\n" +
					"The package javax.xml.transform is accessible from more than one module: <unnamed>, java.xml",
					markers);

			char[] sourceChars = testSource.toCharArray();
			this.problemRequestor.initialize(sourceChars);
			getCompilationUnit("/ztest/src/com/ztest/TestApp.java").getWorkingCopy(this.wcOwner, null);
			assertProblems(
					"Unexpected problems",
					"----------\n" +
					"1. ERROR in /ztest/src/com/ztest/TestApp.java (at line 2)\n" +
					"	import javax.xml.transform.Transformer;\n" +
					"	       ^^^^^^^^^^^^^^^^^^^\n" +
					"The package javax.xml.transform is accessible from more than one module: <unnamed>, java.xml\n" +
					"----------\n" +
					"2. ERROR in /ztest/src/com/ztest/TestApp.java (at line 5)\n" +
					"	Transformer ts;\n" +
					"	^^^^^^^^^^^\n" +
					"Transformer cannot be resolved to a type\n" +
					"----------\n" +
					"3. ERROR in /ztest/src/com/ztest/TestApp.java (at line 6)\n" +
					"	javax.xml.transform.Result result;\n" +
					"	^^^^^^^^^^^^^^^^^^^\n" +
					"The package javax.xml.transform is accessible from more than one module: <unnamed>, java.xml\n" +
					"----------\n",
					this.problemRequestor);

			sourceChars = test2Source.toCharArray();
			this.problemRequestor.initialize(sourceChars);
			getCompilationUnit("/ztest/src/com/ztest/Test2.java").getWorkingCopy(this.wcOwner, null);
			assertProblems(
					"Unexpected problems",
					"----------\n" +
					"1. ERROR in /ztest/src/com/ztest/Test2.java (at line 2)\n" +
					"	import javax.xml.transform.*;\n" +
					"	       ^^^^^^^^^^^^^^^^^^^\n" +
					"The package javax.xml.transform is accessible from more than one module: <unnamed>, java.xml\n" +
					"----------\n" +
					"2. ERROR in /ztest/src/com/ztest/Test2.java (at line 4)\n" +
					"	Transformer ts;\n" +
					"	^^^^^^^^^^^\n" +
					"Transformer cannot be resolved to a type\n" +
					"----------\n",
					this.problemRequestor);
		} finally {
			deleteProject("ztest");
		}
	}
	public void testBug536928_comment22b() throws CoreException, IOException {
		try {
			IJavaProject project = createJava9Project("ztest", new String[] { "src" });
			createFolder("/ztest/lib");
			Util.createJar(new String[] {
					"javax/xml/transform/Transformer.java",
					"package javax.xml.transform;\n" +
					"public class Transformer {}\n",
					"javax/xml/transform/Result.java",
					"package javax.xml.transform;\n" +
					"public class Result {}\n"
				},
				project.getProject().getLocation().toString() + "/lib/xml-apis.jar",
				"1.8");
			project.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
			IClasspathEntry libraryEntry = JavaCore.newLibraryEntry(new Path("/ztest/lib/xml-apis.jar"), null, null);
			addClasspathEntry(project, libraryEntry, 2); // DIFFERENCE HERE: place xml-apis.jar AFTER jrt-fs.jar

			String testSource =
					"package com.ztest;\n" +
					"import javax.xml.transform.Transformer;\n" +
					"\n" +
					"public class TestApp {\n" +
					"	Transformer ts;\n" +
					"	javax.xml.transform.Result result;\n" +
					"}\n";
			createFolder("/ztest/src/com/ztest");
			createFile("/ztest/src/com/ztest/TestApp.java", testSource);
			String test2Source =
					"package com.ztest;\n" +
					"import javax.xml.transform.*;\n" +
					"public class Test2 {\n" +
					"	Transformer ts;\n" +
					"}\n";
			createFile("/ztest/src/com/ztest/Test2.java", test2Source);
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = project.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected Markers",
					"The package javax.xml.transform is accessible from more than one module: <unnamed>, java.xml\n" +
					"The package javax.xml.transform is accessible from more than one module: <unnamed>, java.xml\n" +
					"Transformer cannot be resolved to a type\n" +
					"Transformer cannot be resolved to a type\n" +
					"The package javax.xml.transform is accessible from more than one module: <unnamed>, java.xml",
					markers);

			char[] sourceChars = testSource.toCharArray();
			this.problemRequestor.initialize(sourceChars);
			getCompilationUnit("/ztest/src/com/ztest/TestApp.java").getWorkingCopy(this.wcOwner, null);
			assertProblems(
					"Unexpected problems",
					"----------\n" +
					"1. ERROR in /ztest/src/com/ztest/TestApp.java (at line 2)\n" +
					"	import javax.xml.transform.Transformer;\n" +
					"	       ^^^^^^^^^^^^^^^^^^^\n" +
					"The package javax.xml.transform is accessible from more than one module: <unnamed>, java.xml\n" +
					"----------\n" +
					"2. ERROR in /ztest/src/com/ztest/TestApp.java (at line 5)\n" +
					"	Transformer ts;\n" +
					"	^^^^^^^^^^^\n" +
					"Transformer cannot be resolved to a type\n" +
					"----------\n" +
					"3. ERROR in /ztest/src/com/ztest/TestApp.java (at line 6)\n" +
					"	javax.xml.transform.Result result;\n" +
					"	^^^^^^^^^^^^^^^^^^^\n" +
					"The package javax.xml.transform is accessible from more than one module: <unnamed>, java.xml\n" +
					"----------\n",
					this.problemRequestor);

			sourceChars = test2Source.toCharArray();
			this.problemRequestor.initialize(sourceChars);
			getCompilationUnit("/ztest/src/com/ztest/Test2.java").getWorkingCopy(this.wcOwner, null);
			assertProblems(
					"Unexpected problems",
					"----------\n" +
					"1. ERROR in /ztest/src/com/ztest/Test2.java (at line 2)\n" +
					"	import javax.xml.transform.*;\n" +
					"	       ^^^^^^^^^^^^^^^^^^^\n" +
					"The package javax.xml.transform is accessible from more than one module: <unnamed>, java.xml\n" +
					"----------\n" +
					"2. ERROR in /ztest/src/com/ztest/Test2.java (at line 4)\n" +
					"	Transformer ts;\n" +
					"	^^^^^^^^^^^\n" +
					"Transformer cannot be resolved to a type\n" +
					"----------\n",
					this.problemRequestor);
		} finally {
			deleteProject("ztest");
		}
	}
	public void testBug536928_comment22_limited() throws CoreException, IOException {
		try {
			IClasspathAttribute[] limitModules = {
				JavaCore.newClasspathAttribute(IClasspathAttribute.LIMIT_MODULES, "java.base")
			};
			IJavaProject project = createJava9ProjectWithJREAttributes("ztest", new String[] { "src" }, limitModules);
			createFolder("/ztest/lib");
			Util.createJar(new String[] {
					"javax/xml/transform/Transformer.java",
					"package javax.xml.transform;\n" +
					"public class Transformer {}\n",
					"javax/xml/transform/Result.java",
					"package javax.xml.transform;\n" +
					"public class Result {}\n"
				},
				project.getProject().getLocation().toString() + "/lib/xml-apis.jar",
				"1.8");
			project.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
			IClasspathEntry libraryEntry = JavaCore.newLibraryEntry(new Path("/ztest/lib/xml-apis.jar"), null, null);
			addClasspathEntry(project, libraryEntry, 1); // right after src and before jrt-fs.jar

			String testSource =
					"package com.ztest;\n" +
					"import javax.xml.transform.Transformer;\n" +
					"\n" +
					"public class TestApp {\n" +
					"	Transformer ts;\n" +
					"	javax.xml.transform.Result result;\n" +
					"}\n";
			createFolder("/ztest/src/com/ztest");
			createFile("/ztest/src/com/ztest/TestApp.java", testSource);
			String test2Source =
					"package com.ztest;\n" +
					"import javax.xml.transform.*;\n" +
					"public class Test2 {\n" +
					"	Transformer ts;\n" +
					"}\n";
			createFile("/ztest/src/com/ztest/Test2.java", test2Source);
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();

			char[] sourceChars = testSource.toCharArray();
			this.problemRequestor.initialize(sourceChars);
			getCompilationUnit("/ztest/src/com/ztest/TestApp.java").getWorkingCopy(this.wcOwner, null);
			assertProblems(
					"Unexpected problems",
					"----------\n" +
					"----------\n",
					this.problemRequestor);
			sourceChars = test2Source.toCharArray();
			this.problemRequestor.initialize(sourceChars);
			getCompilationUnit("/ztest/src/com/ztest/Test2.java").getWorkingCopy(this.wcOwner, null);
			assertProblems(
					"Unexpected problems",
					"----------\n" +
					"----------\n",
					this.problemRequestor);
		} finally {
			deleteProject("ztest");
		}
	}
	public void testBug542896() throws CoreException {
		IJavaProject java10Project = createJava10Project("bug", new String[] { "src" });
		try {
			createFolder("/bug/src/test/platform");
			createFile("/bug/src/test/platform/Context.java",
					"package test.platform;\n" +
					"\n" +
					"import java.net.URI;\n" +
					"\n" +
					"public interface Context {\n" +
					"	public URI getURI();\n" +
					"}\n");
			createFile("/bug/src/test/platform/AbstractContext.java",
					"package test.platform;\n" +
					"\n" +
					"import java.net.URI;\n" +
					"import java.util.*;\n" +
					"import test.*;\n" +
					"\n" +
					"public abstract class AbstractContext implements Context {\n" +
					"	Iterable<URI> uris = new ArrayList<URI>();\n" +
					"	Application application;\n" +
					"}\n");
			String testSource =
					"package test;\n" +
					"\n" +
					"import java.io.*;\n" +
					"import java.net.*;\n" +
					"import java.util.*;\n" +
					"\n" +
					"import test.platform.*;\n" +
					"\n" +
					"public interface Application // extends Foo\n" +
					"{\n" +
					"}\n";
			String testPath = "/bug/src/test/Application.java";
			createFile(testPath, testSource);
			// first compile: no error:
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();
			char[] sourceChars = testSource.toCharArray();
			this.problemRequestor.initialize(sourceChars);
			getCompilationUnit(testPath).getWorkingCopy(this.wcOwner, null);
			assertProblems(
					"Unexpected problems",
					"----------\n" +
					"1. WARNING in /bug/src/test/Application.java (at line 3)\n" +
					"	import java.io.*;\n" +
					"	       ^^^^^^^\n" +
					"The import java.io is never used\n" +
					"----------\n" +
					"2. WARNING in /bug/src/test/Application.java (at line 4)\n" +
					"	import java.net.*;\n" +
					"	       ^^^^^^^^\n" +
					"The import java.net is never used\n" +
					"----------\n" +
					"3. WARNING in /bug/src/test/Application.java (at line 5)\n" +
					"	import java.util.*;\n" +
					"	       ^^^^^^^^^\n" +
					"The import java.util is never used\n" +
					"----------\n" +
					"4. WARNING in /bug/src/test/Application.java (at line 7)\n" +
					"	import test.platform.*;\n" +
					"	       ^^^^^^^^^^^^^\n" +
					"The import test.platform is never used\n" +
					"----------\n",
					this.problemRequestor);
			// introduce error:
			String testSourceEdited =
					"package test;\n" +
					"\n" +
					"import java.io.*;\n" +
					"import java.net.*;\n" +
					"import java.util.*;\n" +
					"\n" +
					"import test.platform.*;\n" +
					"\n" +
					"public interface Application extends Foo\n" +
					"{\n" +
					"}\n";
			editFile(testPath, testSourceEdited);
			java10Project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			IMarker[] markers = java10Project.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "Foo cannot be resolved to a type", markers);
		} finally {
			if (java10Project != null)
				deleteProject(java10Project);
		}
	}
	public void testBug543392a() throws Exception {
		bug543392(null);
	}
	public void testBug543392b() throws Exception {
		// put other on the *modulepath*:
		IClasspathAttribute[] attrs = { JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true") };
		bug543392(attrs);
	}
	void bug543392(IClasspathAttribute[] dependencyAttrs) throws Exception {
		IJavaProject other = createJava9Project("other");
		IJavaProject current = createJava9Project("current");
		try {
			createFile("other/src/module-info.java",
					"module other {\n" +
					"	exports other.p;\n" +
					"}\n");
			createFolder("other/src/other/p");
			createFile("other/src/other/p/C.java",
					"package other.p;\n" +
					"public class C {}\n");

			addClasspathEntry(current,
					JavaCore.newProjectEntry(other.getProject().getFullPath(), null, false, dependencyAttrs, false)); // dependency, but ..
			createFile("current/src/module-info.java", "module current {}\n"); // ... no 'requires'!
			createFolder("current/src/current");

			String test1path = "current/src/current/Test1.java";
			String test1source =
					"package current;\n" +
					"import other.p.C;\n" +
					"public class Test1 {\n" +
					"}\n";
			createFile(test1path, test1source);
			String test2path = "current/src/current/Test2.java";
			String test2source =
					"package current;\n" +
					"public class Test2 {\n" +
					"	other.p.C c;\n" +
					"}\n";
			createFile(test2path, test2source);

			getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			IMarker[] markers = current.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers",
					"The type other.p.C is not accessible\n" +
					"The type other.p.C is not accessible",
					markers);

			char[] sourceChars = test1source.toCharArray();
			this.problemRequestor.initialize(sourceChars);
			getCompilationUnit(test1path).getWorkingCopy(this.wcOwner, null);
			assertProblems("unexpected problems",
					"----------\n" +
					"1. ERROR in /current/src/current/Test1.java (at line 2)\n" +
					"	import other.p.C;\n" +
					"	       ^^^^^^^^^\n" +
					"The type other.p.C is not accessible\n" +
					"----------\n",
					this.problemRequestor);
			sourceChars = test2source.toCharArray();
			this.problemRequestor.initialize(sourceChars);
			getCompilationUnit(test2path).getWorkingCopy(this.wcOwner, null);
			assertProblems("unexpected problems",
					"----------\n" +
					"1. ERROR in /current/src/current/Test2.java (at line 3)\n" +
					"	other.p.C c;\n" +
					"	^^^^^^^^^\n" +
					"The type other.p.C is not accessible\n" +
					"----------\n",
					this.problemRequestor);
		} finally {
			deleteProject(other);
			deleteProject(current);
		}
	}
	public void testBug541328() throws Exception {
		IJavaProject pa = createJava9Project("m.a");
		IJavaProject pb = createJava9Project("m.b");
		IJavaProject test = createJava9Project("test");
		try {
			createFolder("m.a/src/a/foo");
			createFile("m.a/src/a/foo/Bar.java", "package a.foo;\n public class Bar {}\n");
			createFile("m.a/src/module-info.java",
					"module m.a {\n" +
					"	exports a.foo to m.b;\n" +
					"}\n");
			createFile("m.b/src/module-info.java",
					"module m.b {\n" +
					"	requires m.a;\n" +
					"	exports b;\n" +
					"}\n");
			createFolder("m.b/src/b");
			createFile("m.b/src/b/Boo.java",
					"package b;\n" +
					"import a.foo.Bar;\n" +
					"public class Boo extends Bar {}\n");
			addModularProjectEntry(pb, pa);

			IClasspathAttribute[] forceExport = {
						JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true"),
						JavaCore.newClasspathAttribute(IClasspathAttribute.ADD_EXPORTS, "m.a/a.foo=ALL-UNNAMED")
					};
			addClasspathEntry(test, JavaCore.newProjectEntry(pa.getPath(), null, false, forceExport, false));
			addModularProjectEntry(test, pb);

			String testSource =
					"import a.foo.Bar;\n" +
					"import b.Boo;\n" +
					"public class Test {\n" +
					"	Bar b = new Boo();\n" +
					"}\n";
			String testPath = "test/src/Test.java";
			createFile(testPath, testSource);
			getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			assertNoErrors();

			this.problemRequestor.initialize(testSource.toCharArray());
			getCompilationUnit(testPath).getWorkingCopy(this.wcOwner, null);
			assertProblems("unexpected problems",
					"----------\n" +
					"----------\n",
					this.problemRequestor);
		} finally {
			deleteProject(pa);
			deleteProject(pb);
			deleteProject(test);
		}
	}
	public void testBug543195() throws CoreException {
		IJavaProject pj1 = createJava9Project("pj1");
		IJavaProject pj2 = createJava9Project("pj2");
		IJavaProject ptest = createJava9Project("ptest");
		try {
			addModularProjectEntry(pj2, pj1);
			addModularProjectEntry(ptest, pj2);

			createFolder("pj1/src/p");
			createFile("pj1/src/p/Missing.java",
					"package p;\n" +
					"public class Missing {\n" +
					"	public void miss() {}\n" +
					"}\n");
			createFile("pj1/src/module-info.java",
					"module pj1 {\n" +
					"	exports p;\n" +
					"}\n");

			createFolder("pj2/src/q");
			createFile("pj2/src/q/API.java",
					"package q;\n" +
					"public class API extends p.Missing {}\n");
			createFile("pj2/src/q/API2.java",
					"package q;\n" +
					"public class API2 extends API {}\n");
			createFile("pj2/src/module-info.java",
					"module pj2 {\n" +
					"	requires pj1;\n" +
					"	exports q;\n" +
					"}\n");
			getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);

			deleteFile("pj1/bin/p/Missing.class");
			pj1.getProject().close(null);

			createFolder("ptest/src/p/r");
			createFile("ptest/src/p/r/P.java", "package p.r;\n public class P {}\n");
			createFolder("ptest/src/t");
			createFile("ptest/src/t/Test1.java",
					"package t;\n" +
					"import q.API2;\n" +
					"public class Test1 {\n" +
					"	void m(API2 a) {\n" +
					"		a.miss();\n" +
					"	}\n" +
					"}\n");
			String test2Path = "ptest/src/t/Test2.java";
			String test2Content =
					"package t;\n" +
					"import p.Missing;\n" +
					"public class Test2 {}\n";
			createFile(test2Path, test2Content);
			ptest.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			IMarker[] markers = ptest.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("unexpected markers",
					"The import p.Missing cannot be resolved\n" +
					"The method miss() is undefined for the type API2",
					markers);

			this.problemRequestor.initialize(test2Content.toCharArray());
			getCompilationUnit(test2Path).getWorkingCopy(this.wcOwner, null);
			assertProblems("unexpected problems",
					"----------\n" +
					"1. ERROR in /ptest/src/t/Test2.java (at line 2)\n" +
					"	import p.Missing;\n" +
					"	       ^^^^^^^^^\n" +
					"The import p.Missing cannot be resolved\n" +
					"----------\n",
					this.problemRequestor);
		} finally {
			deleteProject(pj1);
			deleteProject(pj2);
			deleteProject(ptest);
		}
	}

	public void testBug543701() throws Exception {
		IJavaProject p = createJava9Project("p");
		String outputDirectory = Util.getOutputDirectory();
		try {
			String jar1Path = outputDirectory + File.separator + "lib1.jar";
			Util.createJar(new String[] {
						"javax/xml/transform/Result.java",
						"package javax.xml.transform;\n" +
						"public class Result {}\n"
					}, new HashMap<>(), jar1Path);

			String jar2Path = outputDirectory + File.separator + "lib2.jar";
			Util.createJar(new String[] {
						"p2/C2.java",
						"package p2;\n" +
						"import javax.xml.transform.Result;\n" +
						"public class C2 {\n" +
						"	public void m(Number n) {}\n" +
						"	public void m(Result r) {}\n" + // Result will be ambiguous looking from project 'p', but should not break compilation
						"}\n"
					}, new HashMap<>(), jar2Path);

			addLibraryEntry(p, jar1Path, false);
			addLibraryEntry(p, jar2Path, false);

			createFolder("p/src/pp");
			String testPath = "p/src/pp/Test.java";
			String testSource =
					"package pp;\n" +
					"import p2.C2;\n" +
					"public class Test {\n" +
					"	void test(C2 c2) {\n" +
					"		c2.m(Integer.valueOf(1));\n" +
					"	}\n" +
					"}\n";
			createFile(testPath, testSource);

			p.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			assertNoErrors();

			this.problemRequestor.initialize(testSource.toCharArray());
			getCompilationUnit(testPath).getWorkingCopy(this.wcOwner, null);
			assertProblems("unexpected problems",
					"----------\n" +
					"----------\n",
					this.problemRequestor);
		} finally {
			deleteProject(p);
			// clean up output dir
			File outputDir = new File(outputDirectory);
			if (outputDir.exists())
				Util.flushDirectoryContent(outputDir);
		}
	}

	public void testBug543441() throws Exception {
		// unsuccessful attempt at triggering NPE on null required module
		IJavaProject p = createJava9Project("p");
		String outputDirectory = Util.getOutputDirectory();
		try {
			String jar1Path = outputDirectory + File.separator + "lib1.jar";
			Util.createJar(new String[] {
						"module-info.java",
						"module lib1 {}\n"
					}, jar1Path, "9");

			String jar2Path = outputDirectory + File.separator + "lib2.jar";
			Util.createJar(new String[] {
						"module-info.java",
						"module lib2 {\n" +
						"	requires lib1;\n" + // will be messing when seen from project 'p'
						"	exports p2;\n" +
						"}\n",
						"p2/C2.java",
						"package p2;\n" +
						"public class C2 {}\n"
					},
					null, jar2Path, new String[] { jar1Path }, "9");

			File jar1File = new File(jar1Path);
			jar1File.delete();

			addModularLibraryEntry(p, new Path(jar2Path), null);
			createFile("p/src/module-info.java",
					"module p {\n" +
					"	requires transitive lib2;\n" + // not lib1
					"}\n");
			createFolder("p/src/pkg");
			createFile("p/src/pkg/Test.java",
					"package pkg;\n" +
					"import p2.C2;\n" +
					"public class Test {\n" +
					"	void test(C2 c) {}\n" +
					"}\n");

			p.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			assertNoErrors();

		} finally {
			deleteProject(p);
			File outputDir = new File(outputDirectory);
			if (outputDir.exists())
				Util.flushDirectoryContent(outputDir);
		}
	}

	public void testBug543765() throws CoreException, IOException {
		// failure never seen in this test
		IJavaProject m = createJava9Project("M");
		IJavaProject n = createJava9Project("N");
		IJavaProject x = createJava9Project("X");
		IJavaProject y = createJava9Project("Y");
		String outputDirectory = Util.getOutputDirectory();
		try {
			// ------ W ------
			String wJarLocation = outputDirectory + File.separator + "w-0.0.1-SNAPSHOT.jar";
			IPath wJarPath = new Path(wJarLocation);
			Util.createJar(new String[] {
						"external/W.java",
						"public class W {\n" +
						"	public static void main(String... args) {}\n" +
						"}\n"
					}, wJarLocation, "9");

			// ------ X ------
			addModularLibraryEntry(x, wJarPath, null);
			createFolder("X/src/com/example/x");
			createFile("X/src/com/example/x/X.java",
					"package com.example.x;\n" +
					"public class X {\n" +
					"    public static void main(String[] args) { \n" +
					"        System.out.println(\"X\");\n" +
					"    }\n" +
					"}\n");
			createFile("X/src/module-info.java",
					"open module com.example.x {\n" +
					"    exports com.example.x;\n" +
					"    requires w;\n" +
					"}\n");

			// ------ Y ------
			addModularLibraryEntry(y, wJarPath, null);
			addModularProjectEntry(y, x);
			createFolder("Y/src/com/example/y");
			createFile("Y/src/com/example/y/Y.java",
					"package com.example.y;\n" +
					"public class Y {\n" +
					"    public static void main(String[] args) { \n" +
					"        System.out.println(\"Y\");\n" +
					"    }\n" +
					"}\n");
			createFile("Y/src/module-info.java",
					"open module com.example.y {\n" +
					"    exports com.example.y;\n" +
					"    requires com.example.x;\n" +
					"}\n");

			// ------ N ------
			createFolder("N/src/com/example/n");
			createFile("N/src/com/example/n/N.java",
					"package com.example.n;\n" +
					"public class N {\n" +
					"    public static void main(String[] args) { \n" +
					"        System.out.println(\"N\");\n" +
					"    } \n" +
					"}\n");
			createFile("N/src/module-info.java",
					"open module n {\n" +
					"    exports com.example.n;\n" +
					"}\n");

			// ------ M ------
			// insert new entries before JRE:
			IClasspathEntry[] entries = m.getRawClasspath();
			int length = entries.length;
			System.arraycopy(entries, 0, entries = new IClasspathEntry[length + 4], 4, length);
			entries[0] = entries[4];
			entries[1] = newModularLibraryEntry(wJarPath, null, null);
			entries[2] = newModularProjectEntry(n);
			entries[3] = newModularProjectEntry(x);
			entries[4] = newModularProjectEntry(y);
			m.setRawClasspath(entries, null);

			createFolder("M/src/m");
			String mSource =
					"package m;\n" +
					"import com.example.n.N;\n" +
					"public class M {\n" +
					"    public static void main(String[] args) {\n" +
					"        System.out.println(\"M\");\n" +
					"        N.main(null);\n" +
					"    }\n" +
					"}\n";
			String mPath = "M/src/m/M.java";
			createFile(mPath, mSource);
			createFile("M/src/module-info.java",
					"open module m {\n" +
					"    requires n;\n" +
					"    requires w;\n" +
					"}\n");

			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();

			m.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();

			this.problemRequestor.initialize(mSource.toCharArray());
			getCompilationUnit(mPath).getWorkingCopy(this.wcOwner, null);
			assertProblems("unexpected problems",
					"----------\n" +
					"----------\n",
					this.problemRequestor);
		} finally {
			deleteProject(m);
			deleteProject(n);
			deleteProject(x);
			deleteProject(y);
			File outputDir = new File(outputDirectory);
			if (outputDir.exists())
				Util.flushDirectoryContent(outputDir);
		}
	}

	public void testBug544126() throws CoreException, IOException {
		String outputDirectory = Util.getOutputDirectory();
		IJavaProject p = createJava9Project("p");
		try {
			String jar1Path = outputDirectory + File.separator + "auto1Lib.jar";
			createJar(
					new String[] {
						"org/test/Root.java",
						"package org.test;\n" +
						"public class Root {}\n"
					},
					jar1Path);
			String jar2Path = outputDirectory + File.separator + "auto2Lib.jar";
			createJar(
					new String[] {
						"org/test/ext/Ext.java",
						"package org.test.ext;\n" +
						"public class Ext {}\n"
					},
					jar2Path);
			addModularLibraryEntry(p, new Path(jar1Path), null);
			addModularLibraryEntry(p, new Path(jar2Path), null);
			createFolder("p/src/test");
			String testPath = "p/src/test/Test.java";
			String testSource =
					"package test;\n" +
					"import org.test.Root;\n" +
					"public class Test {\n" +
					"    public static void main(String[] args) { \n" +
					"        System.out.println(new Root());\n" +
					"    }\n" +
					"}\n";
			createFile(testPath, testSource);
			createFile("p/src/module-info.java",
					"module test {\n" +
					"    requires auto1Lib;\n" +
					"    requires auto2Lib;\n" +
					"}\n");
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();

			this.problemRequestor.initialize(testSource.toCharArray());
			getCompilationUnit(testPath).getWorkingCopy(this.wcOwner, null);
			assertProblems("unexpected problems",
					"----------\n" +
					"----------\n",
					this.problemRequestor);
		} finally {
			deleteProject(p);
			File outputDir = new File(outputDirectory);
			if (outputDir.exists())
				Util.flushDirectoryContent(outputDir);
		}
	}

	public void testBug544432() throws CoreException {
		IJavaProject prjA = createJava9Project("A");
		IJavaProject prjB = createJava9Project("B");
		try {
			createFolder("A/src/com/a");
			createFile("A/src/com/a/A.java",
				"package com.a;\n" +
				"\n" +
				"public class A {}\n");
			createFile("A/src/module-info.java",
				"open module com.a {\n" +
				"	exports com.a;\n" +
				"}\n");

			addModularProjectEntry(prjB, prjA);
			createFolder("B/src/com/a/b");
			String bPath = "B/src/com/a/b/B.java";
			String bSource =
				"package com.a.b;\n" +
				"import com.a.A;\n" +
				"public class B {\n" +
				"	\n" +
				"	public static void main(String[] args) {\n" +
				"		A a = new A();\n" +
				"		System.out.println(a);\n" +
				"	}\n" +
				"}\n";
			createFile(bPath, bSource);
			createFile("B/src/module-info.java",
				"open module com.a.b {\n" +
				"	requires com.a;\n" +
				"}\n");
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();

			this.problemRequestor.initialize(bSource.toCharArray());
			getCompilationUnit(bPath).getWorkingCopy(this.wcOwner, null);
			assertProblems("unexpected problems",
					"----------\n" +
					"----------\n",
					this.problemRequestor);
		} finally {
			deleteProject(prjA);
			deleteProject(prjB);
		}
	}

	public void testReleaseOption1() throws Exception {
		Hashtable<String, String> options = JavaCore.getOptions();
		IJavaProject p = createJava9Project("p");
		p.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_11);
		p.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_11);
		p.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_11);
		p.setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
		String outputDirectory = Util.getOutputDirectory();
		try {
			String testSource = "public class X {\n" +
								"}";
			String mPath = "p/src/X.java";
			createFile(mPath,
					testSource);
			p.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			waitForAutoBuild();
			IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"",  markers);

		} finally {
			JavaCore.setOptions(options);
			deleteProject(p);
			File outputDir = new File(outputDirectory);
			if (outputDir.exists())
				Util.flushDirectoryContent(outputDir);
		}
	}
	public void testReleaseOption2() throws Exception {
		if (!isJRE12)
			return;
		Hashtable<String, String> options = JavaCore.getOptions();
		IJavaProject p = createJava9Project("p");
		p.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_6);
		p.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_6);
		p.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_6);
		p.setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
		String outputDirectory = Util.getOutputDirectory();
		try {
			String testSource = "public class X {\n" +
								"	public java.util.stream.Stream<String> emptyStream() {\n" +
								"		return null;\n" +
								"	}\n" +
								"}";
			String mPath = "p/src/X.java";
			createFile(mPath,
					testSource);
			p.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			waitForAutoBuild();
			IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"The project was not built due to \"release 6 is not found in the system\". "
					+ "Fix the problem, then try refreshing this project and building it since it may be inconsistent",  markers);

		} finally {
			JavaCore.setOptions(options);
			deleteProject(p);
			File outputDir = new File(outputDirectory);
			if (outputDir.exists())
				Util.flushDirectoryContent(outputDir);
		}
	}
	public void testReleaseOption3() throws Exception {
		if (isJRE12)
			return;
		Hashtable<String, String> options = JavaCore.getOptions();
		IJavaProject p = createJava9Project("p");
		p.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_11);
		p.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_11);
		p.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_11);
		p.setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
		String outputDirectory = Util.getOutputDirectory();
		try {
			String testSource = "public class X {\n" +
								"	public java.util.stream.Stream<String> emptyStream() {\n" +
								"		return null;\n" +
								"	}\n" +
								"}";
			String mPath = "p/src/X.java";
			createFile(mPath,
					testSource);
			p.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			waitForAutoBuild();
			IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"java.util.stream.Stream cannot be resolved to a type",  markers);

		} finally {
			JavaCore.setOptions(options);
			deleteProject(p);
			File outputDir = new File(outputDirectory);
			if (outputDir.exists())
				Util.flushDirectoryContent(outputDir);
		}
	}
	public void testReleaseOption4() throws Exception {
		Hashtable<String, String> options = JavaCore.getOptions();
		IJavaProject p = createJava9Project("p");
		p.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		p.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		p.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		p.setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
		String outputDirectory = Util.getOutputDirectory();
		try {
			String testSource = "public class X {\n" +
								"	public java.util.stream.Stream<String> emptyStream() {\n" +
								"		return null;\n" +
								"	}\n" +
								"}";
			String mPath = "p/src/X.java";
			createFile(mPath,
					testSource);
			p.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			waitForAutoBuild();
			IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"",  markers);

		} finally {
			JavaCore.setOptions(options);
			deleteProject(p);
			File outputDir = new File(outputDirectory);
			if (outputDir.exists())
				Util.flushDirectoryContent(outputDir);
		}
	}
	public void testReleaseOption5() throws Exception {
		if (!isJRE19) return;
		Hashtable<String, String> options = JavaCore.getOptions();
		IJavaProject p = createJava9Project("p");
		p.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_11);
		p.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_11);
		p.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_11);
		p.setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
		String outputDirectory = Util.getOutputDirectory();
		try {
			String testSource = "public class X {\n" +
								"	public java.lang.MatchException getException() {\n" +
								"		return null;\n" +
								"	}\n" +
								"}";
			String mPath = "p/src/X.java";
			createFile(mPath,
					testSource);
			p.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			waitForAutoBuild();
			IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"java.lang.MatchException cannot be resolved to a type",  markers);

		} finally {
			JavaCore.setOptions(options);
			deleteProject(p);
			File outputDir = new File(outputDirectory);
			if (outputDir.exists())
				Util.flushDirectoryContent(outputDir);
		}
	}
	public void testReleaseOption6() throws Exception {
		if (isJRE20) return; // Effectively disable it for most older versions.
		Hashtable<String, String> options = JavaCore.getOptions();
		IJavaProject p = createJava9Project("p");
		p.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_7);
		p.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);
		p.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_7);
		p.setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
		String outputDirectory = Util.getOutputDirectory();
		try {
			String testSource = "interface I {\n" +
								"  int add(int x, int y);\n" +
								"}\n" +
								"public class X {\n" +
								"  public static void main(String[] args) {\n" +
								"    I i = (x, y) -> {\n" +
								"      return x + y;\n" +
								"    };\n" +
								"  }\n" +
								"}\n";
			String mPath = "p/src/X.java";
			createFile(mPath,
					testSource);
			p.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			waitForAutoBuild();
			IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"Lambda expressions are allowed only at source level 1.8 or above",  markers);

		} finally {
			JavaCore.setOptions(options);
			deleteProject(p);
			File outputDir = new File(outputDirectory);
			if (outputDir.exists())
				Util.flushDirectoryContent(outputDir);
		}
	}
	public void testReleaseOption7() throws Exception {
		if (isJRE12)
			return;
		Hashtable<String, String> options = JavaCore.getOptions();
		IJavaProject p = createJava9Project("p");
		p.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_6);
		p.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_6);
		p.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_6);
		p.setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
		String outputDirectory = Util.getOutputDirectory();
		try {
			String testSource = "import java.io.*;\n" +
								"public class X {\n" +
								"	public static void main(String[] args) {\n" +
								"		try {\n" +
								"			System.out.println();\n" +
								"			Reader r = new FileReader(args[0]);\n" +
								"			r.read();\n" +
								"		} catch(IOException | FileNotFoundException e) {\n" +
								"			e.printStackTrace();\n" +
								"		}\n" +
								"	}\n" +
								"}";
			String mPath = "p/src/X.java";
			createFile(mPath,
					testSource);
			p.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			waitForAutoBuild();
			IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers",
							"Multi-catch parameters are not allowed for source level below 1.7\n" +
							"The exception FileNotFoundException is already caught by the alternative IOException",  markers);

		} finally {
			JavaCore.setOptions(options);
			deleteProject(p);
			File outputDir = new File(outputDirectory);
			if (outputDir.exists())
				Util.flushDirectoryContent(outputDir);
		}
	}
	public void testReleaseOption8() throws Exception {
		Hashtable<String, String> options = JavaCore.getOptions();
		IJavaProject p = createJava9Project("p");
		p.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_9);
		p.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_9);
		p.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_9);
		p.setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
		String outputDirectory = Util.getOutputDirectory();
		try {
			String testSource = "module mod.one { \n" +
								"	requires java.base;\n" +
								"}";
			String mPath = "p/src/module-info.java";
			createFile(mPath,
					testSource);
			p.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			waitForAutoBuild();
			IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"",  markers);

		} finally {
			JavaCore.setOptions(options);
			deleteProject(p);
			File outputDir = new File(outputDirectory);
			if (outputDir.exists())
				Util.flushDirectoryContent(outputDir);
		}
	}
	public void testReleaseOption9() throws Exception {
		if (!isJRE10) return;
		Hashtable<String, String> options = JavaCore.getOptions();
		IJavaProject p = createJava9Project("p");
		p.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_10);
		p.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_10);
		p.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_10);
		p.setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
		String outputDirectory = Util.getOutputDirectory();
		try {
			String testSource = "module mod.one { \n" +
								"	requires java.base;\n" +
								"}";
			String mPath = "p/src/module-info.java";
			createFile(mPath,
					testSource);
			p.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			waitForAutoBuild();
			IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"",  markers);

		} finally {
			JavaCore.setOptions(options);
			deleteProject(p);
			File outputDir = new File(outputDirectory);
			if (outputDir.exists())
				Util.flushDirectoryContent(outputDir);
		}
	}
	public void testReleaseOption10() throws Exception {
		Hashtable<String, String> options = JavaCore.getOptions();
		IJavaProject p = createJava9Project("p");
		p.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		p.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		p.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		p.setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
		String outputDirectory = Util.getOutputDirectory();
		try {
			String testSource = "module mod.one { \n" +
					"	requires java.base;\n" +
					"}";
			String mPath = "p/src/module-info.java";
			createFile(mPath,
					testSource);
			p.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			waitForAutoBuild();
			IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			String expected =
					"Syntax error on token \"module\", package expected\n" +
					"Syntax error on token(s), misplaced construct(s)\n" +
					"Syntax error on token \".\", , expected\n" +
					"Syntax error on token \"}\", delete this token";
			assertMarkers("Unexpected markers",
							expected,  markers);

		} finally {
			JavaCore.setOptions(options);
			deleteProject(p);
			File outputDir = new File(outputDirectory);
			if (outputDir.exists())
				Util.flushDirectoryContent(outputDir);
		}
	}
	public void testReleaseOption11() throws Exception {
		Hashtable<String, String> options = JavaCore.getOptions();
		IJavaProject p = createJava9Project("p");
		p.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		p.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		p.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		p.setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
		String outputDirectory = Util.getOutputDirectory();
		try {
			createFolder("p/src/foo");
			createFile(
					"p/src/foo/Module.java",
					"package foo;\n" +
					"public class Module {}\n");
			createFile(
					"p/src/foo/X.java",
					"package foo;\n" +
					"public class X { \n" +
					"	public Module getModule(String name) {\n" +
					"		return null;\n" +
					"	}\n" +
					"}");
			p.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			waitForAutoBuild();
			IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"",  markers);

		} finally {
			JavaCore.setOptions(options);
			deleteProject(p);
			File outputDir = new File(outputDirectory);
			if (outputDir.exists())
				Util.flushDirectoryContent(outputDir);
		}
	}
	public void testReleaseOption12() throws Exception {
		if (!isJRE16)
			return;
		Hashtable<String, String> options = JavaCore.getOptions();
		IJavaProject p = createJava16Project("p");
		IClasspathEntry[] rawClasspath = p.getRawClasspath();
		IClasspathEntry jrtEntry = getJRTLibraryEntry();
		for(int i = 0; i < rawClasspath.length; i++) {
			if (rawClasspath[i].getEntryKind() == IClasspathEntry.CPE_CONTAINER)
				rawClasspath[i] = jrtEntry;
		}
		p.setRawClasspath(rawClasspath, null);
		p.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_15);
		p.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_15);
		p.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_15);
		p.setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
		String outputDirectory = Util.getOutputDirectory();
		try {
			String testSource = "import javax.lang.model.*;\n" +
								"public class X {\n" +
								"	public static void main(String[] args) {\n" +
								"		SourceVersion version = SourceVersion.RELEASE_16;\n" +
								"	}\n" +
								"}";
			String mPath = "p/src/X.java";
			createFile(mPath,
					testSource);
			p.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			waitForAutoBuild();
			IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"RELEASE_16 cannot be resolved or is not a field",  markers);

		} finally {
			JavaCore.setOptions(options);
			deleteProject(p);
			File outputDir = new File(outputDirectory);
			if (outputDir.exists())
				Util.flushDirectoryContent(outputDir);
		}
	}
	public void testReleaseOption13() throws Exception {
		if (!isJRE12)
			return;
		Hashtable<String, String> options = JavaCore.getOptions();
		IJavaProject p = createJava9Project("p");
		p.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		p.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		p.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		p.setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
		String outputDirectory = Util.getOutputDirectory();
		try {
			String testSource = "\n" +
								"public class X {\n" +
								"	public static void main(String[] args) {\n" +
								"		Integer.toUnsignedString(1, 1);\n" +
								"	}\n" +
								"}";
			String mPath = "p/src/X.java";
			createFile(mPath,
					testSource);
			p.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			waitForAutoBuild();
			IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"",  markers);

		} finally {
			JavaCore.setOptions(options);
			deleteProject(p);
			File outputDir = new File(outputDirectory);
			if (outputDir.exists())
				Util.flushDirectoryContent(outputDir);
		}
	}
	public void testBug547114a() throws CoreException, IOException {
		String outputDirectory = Util.getOutputDirectory();
		String jarPath = outputDirectory + File.separator + "lib.jar";
		try {
			// focus project has no module-info, to trigger path where LE#knownPackages is not empty when processing add-reads
			String[] sources = new String[] {
					"src/org/astro/World.java",
					"package org.astro;\n" +
					"import p.C;\n" +
					"public class World {\n" +
					"	C f;\n" +
					"}\n"
			};
			IJavaProject p = setupModuleProject("org.astro", sources);

			Util.createJar(new String[] {
					"/lib/src/module-info.java",
					"module lib {\n" +
					"	exports p;\n" +
					"}\n",
					"/lib/src/p/C.java",
					"package p;\n" +
					"public class C {}\n",
				},
				jarPath,
				"9");
			addClasspathEntry(p, JavaCore.newLibraryEntry(new Path(jarPath), null, null, null,
					new IClasspathAttribute[] {
						JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true"),
						JavaCore.newClasspathAttribute(IClasspathAttribute.ADD_READS, "lib=missing.module") // problematic directive on jar-dependency
					},
					false));

			p.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			waitForAutoBuild();

			IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			// 1. marker is on the project, second on the "current" CU: World.java.
			assertMarkers("Unexpected markers",
					"The project was not built since its build path has a problem: missing.module cannot be resolved to a module, it is referenced from an add-reads directive. Fix the build path then try building this project\n" +
					"missing.module cannot be resolved to a module, it is referenced from an add-reads directive",
					markers);
		} finally {
			deleteProject("org.astro");
			deleteFile(jarPath);
		}
	}
	public void testBug547114b() throws CoreException, IOException {
		try {
			IJavaProject p = setupModuleProject("org.astro", new String[] {
					"src/module-info.java",
					"module org.astro {\n" +
					"	requires lib;\n" +
					"}\n",
					"src/org/astro/World.java",
					"package org.astro;\n" +
					"import p.C;\n" +
					"public class World {\n" +
					"	C f;\n" +
					"}\n"
			});

			IJavaProject lib = setupModuleProject("lib", new String[] {
					"src/module-info.java",
					"module lib {\n" +
					"	exports p;\n" +
					"}\n",
					"src/p/C.java",
					"package p;\n" +
					"public class C {}\n",
			});
			addClasspathEntry(p, JavaCore.newProjectEntry(lib.getPath(), null, false,
					new IClasspathAttribute[] {
						JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true"),
						JavaCore.newClasspathAttribute(IClasspathAttribute.ADD_READS, "lib=missing.module") // problematic directive on project dependency
					},
					false));

			ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			waitForAutoBuild();

			IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			// 1. marker is on the project, second on the "current" CU: World.java.
			assertMarkers("Unexpected markers",
					"The project was not built since its build path has a problem: missing.module cannot be resolved to a module, it is referenced from an add-reads directive. Fix the build path then try building this project\n" +
					"missing.module cannot be resolved to a module, it is referenced from an add-reads directive",
					markers);
		} finally {
			deleteProject("org.astro");
			deleteProject("lib");
		}
	}

	public void testBug558004() throws CoreException {
		IJavaProject prj = createJava9Project("A");
		try {
			String moduleinfopath = "A/src/module-info.java";
			String moduleinfosrc =
				"/**\n" +
				" * The {@link java.nio.file.FileSystems#newFileSystem FileSystems.newFileSystem(URI.create(\"jrt:/\"))}\n" +
				" */\n" +
				"module modulartest11 {\n" +
				"}\n";
			createFile(moduleinfopath, moduleinfosrc);
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();
			this.problemRequestor.initialize(moduleinfosrc.toCharArray());
			getCompilationUnit("A/src/module-info.java").getWorkingCopy(this.wcOwner, null);
			assertProblems("unexpected problems",
					"----------\n" +
					"----------\n",
					this.problemRequestor);
		} finally {
			deleteProject(prj);
		}
	}

	public void testBug547479() throws CoreException {
		int max = org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE;

		IJavaProject prjA = createJava9Project("A");
		IJavaProject prjB = createJava9Project("B");
		try {
			createFile("A/src/module-info.java",
				"module A {\n" +
				"}\n");

			addModularProjectEntry(prjB, prjA);
			// prepare files to be compiled in two batches à 2 files:
			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = 2;
			// ---1---
			createFolder("B/src/b");
			createFile("B/src/b/Class1.java",
				"package b;\n" +
				"import java.sql.Connection;\n" +
				"public class Class1 {\n" +
				"	Connection connection;\n" +
				"}\n");
			createFile("B/src/b/Class2.java",
				"package b;\n" +
				"import java.sql.Connection;\n" +
				"public class Class2 {\n" +
				"	Connection connection;\n" +
				"}\n");
			// ---2---
			createFile("B/src/module-info.java",
				"module B {\n" +
				"	requires java.sql;\n" +
				"	requires A;\n" +
				"}\n");
			String bPath = "B/src/b/Class3.java";
			String bSource =
				"package b;\n" + // <= this triggered createPackage in an inconsistent state
				"import java.sql.Connection;\n" +
				"public class Class3 {\n" +
				"	Connection connection;\n" +
				"}\n";
			createFile(bPath, bSource);
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();

			this.problemRequestor.initialize(bSource.toCharArray());
			getCompilationUnit(bPath).getWorkingCopy(this.wcOwner, null);
			assertProblems("unexpected problems",
					"----------\n" +
					"----------\n",
					this.problemRequestor);
		} finally {
			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = max;
			deleteProject(prjA);
			deleteProject(prjB);
		}
	}
	public void testBug547181() throws CoreException {
		IJavaProject prjA = createJava9Project("A");
		IJavaProject prjB = createJava9Project("B");
		IJavaProject prjC = createJava9Project("C");
		IJavaProject prjD = createJava9Project("D");
		try {
			createFile("A/src/module-info.java",
				"module A {\n" +
				" exports p1.p2;\n" +
				"}\n");
			createFolder("A/src/p1/p2");
			createFile("A/src/p1/p2/X.java",
					"package p1.p2;\n" +
					"public class X {\n" +
					"}\n");

			addModularProjectEntry(prjB, prjA);
			addModularProjectEntry(prjD, prjA);
			addModularProjectEntry(prjD, prjB);
			addModularProjectEntry(prjD, prjC);

			createFile("B/src/module-info.java",
					"module B {\n" +
						" requires A;\n" +
						" exports p1;\n" +
					"}\n");
			createFolder("B/src/p1");
			createFile("B/src/p1/Y.java",
				"package p1;\n" +
				"import p1.p2.X;\n" +
				"public class Y {\n" +
				"	private void f(X x) {}\n" +
				"}\n");

			createFile("C/src/module-info.java",
					"module C {\n" +
					" exports p1.p2;\n" +
					"}\n");
			createFolder("C/src/p1/p2");
			createFile("C/src/p1/p2/X.java",
					"package p1.p2;\n" +
					"public class X {\n" +
					"}\n");

			createFile("D/src/module-info.java",
					"module D {\n" +
						" requires B;\n" +
						" requires C;\n" +
					"}\n");

			createFolder("D/src/usage");
			createFile("D/src/usage/AAA.java",
					"package usage;\n" +
					"import p1.Y;\n" +
					"public class AAA {\n" +
					" Y y;\n" +
					"}\n");
			createFile("D/src/usage/Usage.java",
					"package usage;\n" +
					"import p1.p2.X;\n" +
					"public class Usage {\n" +
					" X x;\n" +
					"}\n");

			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();

		} finally {
			deleteProject(prjA);
			deleteProject(prjB);
			deleteProject(prjC);
			deleteProject(prjD);
		}
	}
	public void testBug547181Comment104() throws CoreException {

		IJavaProject prjA = createJava9Project("A");
		IJavaProject prjB = createJava9Project("B");
		try {
			// NO module-info.java, so A is accessed as automatic module
			createFolder("A/src/pack/a");

			createFile("A/src/pack/_some_resource_without_extension",
					"dummy content\n");

			addModularProjectEntry(prjB, prjA);
			// ---1---
			createFolder("B/src/pack/b");
			createFile("B/src/pack/b/Usage.java",
				"package pack.b;\n" +
				"public class Usage {\n" +
				"}\n");
			createFile("B/src/module-info.java",
					"module B {\n" +
					" requires A;\n" +
					"}\n");
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			assertNoErrors();
		} finally {
			deleteProject(prjA);
			deleteProject(prjB);
		}
	}
	public void testBug538512_comment9() throws CoreException, IOException {
		try {
			IJavaProject project = createJava9Project("ztest", new String[] { "src" });
			createFolder("/ztest/lib");
			Util.createJar(new String[] {
					"org/xml/sax/Parser.java",
					"package org.xml.sax;\n" +
					"public class Parser {}\n"
				},
				project.getProject().getLocation().toString() + "/lib/xml-apis.jar", // conflicts with module 'java.xml'
				"1.8");
			IClasspathEntry libraryEntry = JavaCore.newLibraryEntry(new Path("/ztest/lib/xml-apis.jar"), null, null);
			addClasspathEntry(project, libraryEntry, 1); // right after src and before jrt-fs.jar
			Util.createJar(new String[] {
					"org/apache/xerces/parsers/SAXParser.java",
					"package org.apache.xerces.parsers;\n" +
					"public abstract class SAXParser implements org.xml.sax.Parser, java.io.Serializable {}\n"
				},
				project.getProject().getLocation().toString() + "/lib/xercesImpl.jar",
				"1.8");
			project.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
			libraryEntry = JavaCore.newLibraryEntry(new Path("/ztest/lib/xercesImpl.jar"), null, null);
			addClasspathEntry(project, libraryEntry, 1); // right after src and before jrt-fs.jar

			String testSource =
					"package com.ztest;\n" +
					"import org.apache.xerces.parsers.SAXParser;\n" +
					"\n" +
					"public class MySAXParser extends SAXParser {\n" +
					"	static final long serialVersionUID = 0;\n" +
					"}\n";
			createFolder("/ztest/src/com/ztest");
			createFile("/ztest/src/com/ztest/MySAXParser.java", testSource);
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = project.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected Markers",
					"",
					markers);

			char[] sourceChars = testSource.toCharArray();
			this.problemRequestor.initialize(sourceChars);
			getCompilationUnit("/ztest/src/com/ztest/MySAXParser.java").getWorkingCopy(this.wcOwner, null);
			assertProblems(
					"Unexpected problems",
					"----------\n" +
					"----------\n",
					this.problemRequestor);

		} finally {
			deleteProject("ztest");
		}
	}
	public void testIssue23() throws CoreException {
		IJavaProject p1 = createJava9Project("Issue23", "18");
		Map<String, String> options = new HashMap<>();
		// Make sure the new options map doesn't reset.
		options.put(CompilerOptions.OPTION_Compliance, "16.8");
		options.put(CompilerOptions.OPTION_Source, "16.8");
		options.put(CompilerOptions.OPTION_TargetPlatform, "16.8");
		options.put(CompilerOptions.OPTION_Release, "enabled");
		p1.setOptions(options);
		try {
			createFolder("/Issue23/src/p1");
			createFile("/Issue23/src/p1/X.java",
					"package p1;\n" +
					"public class X {\n" +
					"	public java.util.stream.Stream<String> emptyStream() {\n" +
					"		return null;\n" +
					"	}\n" +
					"}");

			waitForManualRefresh();
			waitForAutoBuild();
			p1.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "The project was not built due to \"Invalid value for --release argument:16.8\". Fix the problem, then try refreshing this project and building it since it may be inconsistent", markers);
		} finally {
			deleteProject(p1);
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
	// sort by CHAR_START then MESSAGE
	@Override
	protected void sortMarkers(IMarker[] markers) {
		Arrays.sort(markers, Comparator.comparingInt((IMarker a) -> a.getAttribute(IMarker.CHAR_START, 0))
									   .thenComparing((IMarker a) -> a.getAttribute(IMarker.MESSAGE, "")));
	}
}
