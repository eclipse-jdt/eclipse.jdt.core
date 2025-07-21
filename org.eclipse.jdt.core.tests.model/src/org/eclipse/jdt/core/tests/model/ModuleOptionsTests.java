/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
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
import junit.framework.Test;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.ClasspathAttribute;
import org.eclipse.jdt.internal.core.builder.ClasspathJrt;

public class ModuleOptionsTests extends ModifyingResourceTests {

	public ModuleOptionsTests(String name) {
		super(name);
	}

	static {
//		 TESTS_NAMES = new String[] { "testAddReads" };
	}

	public static Test suite() {
		return buildModelTestSuite(ModuleOptionsTests.class, BYTECODE_DECLARATION_ORDER);
	}
	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		ClasspathJrt.resetCaches();
	}
	@Override
	public void tearDownSuite() throws Exception {
		super.tearDownSuite();
	}
	// testing auto rebuild after change of limit-modules
	public void testLimitModules3() throws CoreException, IOException {
		if (!isJRE9) return;
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

			IClasspathAttribute[] newLimits = {
					JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true"),
					JavaCore.newClasspathAttribute(IClasspathAttribute.LIMIT_MODULES, "java.base,java.sql") // no more awt etc
			};
			setJRECPAttributes(project, newLimits);

			getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);

			markers = project.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			Arrays.sort(markers, (a,b) -> a.getAttribute(IMarker.CHAR_START, 0) - b.getAttribute(IMarker.CHAR_START, 0));
			assertMarkers("Unexpected markers",
					"java.awt cannot be resolved to a type\n" +
					"java.awt cannot be resolved to a type\n" +
					"java.desktop cannot be resolved to a module\n" +
					"java.datatransfer cannot be resolved to a module",
					markers);
		} finally {
			this.deleteProject("org.astro");
		}
	}
	public void testAddExports() throws CoreException, IOException {
		if (!isJRE9) return;
		try {
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
				"public class Test2 {\n" +
				"	java.awt.Window window;\n" +
				"}\n",
				"src2/org/astro/Test3.java",
				"package org.astro;\n" +
				"class Test3 {\n" +
				"	java.awt.datatransfer.Clipboard clippy;\n" +
				"}\n"
			};
			IJavaProject project = setupModuleProject("org.astro", new String[] {"src", "src2"}, sources, null);

			sources = new String[] {
					"src/module-info.java",
					"module test {\n" +
					"	requires org.astro;\n" +
					"}\n",
					"src/test/Test.java",
					"package test;\n" +
					"class Test {\n" +
					"	org.astro.Test2 t;\n" +
					"}\n"
				};
			IClasspathAttribute[] attributes = {
					JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true"),
					JavaCore.newClasspathAttribute(IClasspathAttribute.ADD_EXPORTS, "org.astro/org.astro=test")
			};
			IClasspathEntry cp = JavaCore.newProjectEntry(project.getPath(), null, false, attributes, false);
			IJavaProject p2 = setupModuleProject("test", sources, new IClasspathEntry[] {cp});
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"",
					markers);

			IClasspathAttribute[] newAttrs = {
					JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true")
			};
			setCPAttributes(p2, newAttrs, cp);

			getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);

			markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"The type org.astro.Test2 is not accessible",
					markers);
		} finally {
			this.deleteProject("org.astro");
			this.deleteProject("test");
		}
	}

	public void testAddExports_JRE() throws CoreException, IOException {
		if (!isJRE9) return;
		try {
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
				"public class Test2 {\n" +
				"	int test(jdk.internal.misc.Unsafe unsafe) {\n" +
				"		return unsafe.addressSize();\n" +
				"	}" +
				"}\n",
			};
			IClasspathAttribute[] attrs = new IClasspathAttribute[] {
					JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true"),
					JavaCore.newClasspathAttribute(IClasspathAttribute.ADD_EXPORTS, "java.base/jdk.internal.misc=org.astro")
			};
			IJavaProject project = createJava9ProjectWithJREAttributes("org.astro", new String[] {"src"}, attrs);
			createSourceFiles(project, sources);
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = project.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"",
					markers);

			IClasspathAttribute[] newAttrs = {
					JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true")
			};
			setJRECPAttributes(project, newAttrs);

			getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);

			markers = project.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"The type jdk.internal.misc.Unsafe is not accessible",
					markers);
		} finally {
			this.deleteProject("org.astro");
		}
	}
	public void testAddExports_multi() throws CoreException, IOException {
		if (!isJRE9) return;
		try {
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
				"public class Test2 {\n" +
				"	java.awt.Window window;\n" +
				"}\n",
				"src2/org/astro/sub/Test3.java",
				"package org.astro.sub;\n" +
				"public class Test3 {\n" +
				"	java.awt.datatransfer.Clipboard clippy;\n" +
				"}\n"
			};
			IJavaProject project = setupModuleProject("org.astro", new String[] {"src", "src2"}, sources, null);

			sources = new String[] {
					"src/module-info.java",
					"module test {\n" +
					"	requires org.astro;\n" +
					"}\n",
					"src/test/Test.java",
					"package test;\n" +
					"class Test {\n" +
					"	org.astro.Test2 t;\n" +
					"	org.astro.sub.Test3 t3;\n" +
					"}\n"
				};
			IClasspathAttribute[] attributes = {
					JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true"),
					JavaCore.newClasspathAttribute(IClasspathAttribute.ADD_EXPORTS, "org.astro/org.astro=test")
			};
			IClasspathEntry cp = JavaCore.newProjectEntry(project.getPath(), null, false, attributes, false);
			IJavaProject p2 = setupModuleProject("test", sources, new IClasspathEntry[] {cp});
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"The type org.astro.sub.Test3 is not accessible",
					markers);

			IClasspathAttribute[] newAttrs = {
					JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true"),
					JavaCore.newClasspathAttribute(IClasspathAttribute.ADD_EXPORTS, "org.astro/org.astro=test:org.astro/org.astro.sub=test")
			};
			setCPAttributes(p2, newAttrs, cp);

			getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);

			markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"",
					markers);
		} finally {
			this.deleteProject("org.astro");
			this.deleteProject("test");
		}
	}
	public void testAddExports_classFolder() throws CoreException, IOException {
		if (!isJRE9) return;
		try {
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
				"public class Test2 {\n" +
				"	java.awt.Window window;\n" +
				"}\n",
				"src2/org/astro/sub/Test3.java",
				"package org.astro.sub;\n" +
				"public class Test3 {\n" +
				"	java.awt.datatransfer.Clipboard clippy;\n" +
				"}\n"
			};
			IJavaProject project = setupModuleProject("org.astro", new String[] {"src", "src2"}, sources, null);

			sources = new String[] {
					"src/module-info.java",
					"module test {\n" +
					"	requires org.astro;\n" +
					"}\n",
					"src/test/Test.java",
					"package test;\n" +
					"class Test {\n" +
					"	org.astro.Test2 t;\n" +
					"	org.astro.sub.Test3 t3;\n" +
					"}\n"
				};
			IClasspathAttribute[] attributes = {
					JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true"),
					JavaCore.newClasspathAttribute(IClasspathAttribute.ADD_EXPORTS, "org.astro/org.astro=test")
			};
			IClasspathEntry cp = JavaCore.newLibraryEntry(project.getProject().findMember("bin").getFullPath(), null,
					null, null, attributes, false);
			IJavaProject p2 = setupModuleProject("test", sources, new IClasspathEntry[] {cp});
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"The type org.astro.sub.Test3 is not accessible",
					markers);

			IClasspathAttribute[] newAttrs = {
					JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true"),
					JavaCore.newClasspathAttribute(IClasspathAttribute.ADD_EXPORTS, "org.astro/org.astro=test:org.astro/org.astro.sub=test")
			};
			setCPAttributes(p2, newAttrs, cp);

			getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);

			markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"",
					markers);
		} finally {
			this.deleteProject("org.astro");
			this.deleteProject("test");
		}
	}
	public void testAddReads() throws CoreException, IOException {
		if (!isJRE9) return;
		String libPath = "externalLib/mod.one.jar";
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
			IJavaProject p = setupModuleProject("org.astro", sources);

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
					"public class C implements org.astro.World {\n" +
					"	public String name() {\n" +
					"		return \"C\";\n" +
					"	}\n" +
					"}\n"
			};
			IJavaProject p1 = setupModuleProject("mod.one", src1);
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			// jar-up without the required class, should be supplied by the other module
			deleteFile("/mod.one/src/org/astro/World.java");
			deleteFile("/mod.one/bin/org/astro/World.class");
			File rootDir = new File(p1.getProject().findMember("bin").getLocation().toString());
			Util.zip(rootDir, getExternalResourcePath(libPath));

			String[] src2 = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires mod.one;\n" +
				"}",
				"src/com/greetings/MyTest.java",
				"package com.greetings;\n" +
				"public class MyTest extends one.p.C {}"
			};
			IClasspathEntry dep1 = JavaCore.newProjectEntry(p.getPath(), null, false,
															new IClasspathAttribute[] {new ClasspathAttribute("module", "true")},
															false/*not exported*/);
			IClasspathEntry dep2 = JavaCore.newLibraryEntry(new Path(getExternalResourcePath(libPath)), null, null, null,
															new IClasspathAttribute[] {
																	new ClasspathAttribute("module", "true"),
																	new ClasspathAttribute(IClasspathAttribute.ADD_READS, "mod.one=org.astro")
															},
															false/*not exported*/);
			IJavaProject p2 = setupModuleProject("com.greetings", src2, new IClasspathEntry[] { dep1, dep2 });
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",	"",  markers);
			IClasspathAttribute[] attrs = new IClasspathAttribute[] {
					JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true")
			};
			setCPAttributes(p2, attrs, dep2);
			getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
			markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"The project was not built since its build path is incomplete. Cannot find the class file for org.astro.World. Fix the build path then try building this project\n" +
					"The type org.astro.World cannot be resolved. It is indirectly referenced from required type one.p.C",
					markers);
		} finally {
			deleteExternalResource(libPath);
			deleteProject("mod.one");
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void testAddReads2() throws CoreException, IOException {
		if (!isJRE9) return;
		String libPath = "externalLib/mod.one.jar";
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
			IJavaProject p = setupModuleProject("org.astro", sources);

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
					"public class C implements org.astro.World {\n" +
					"	public String name() {\n" +
					"		return \"C\";\n" +
					"	}\n" +
					"}\n"
			};
			IJavaProject p1 = setupModuleProject("mod.one", src1);
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			// jar-up without the required class, should be supplied by the other module
			deleteFile("/mod.one/src/org/astro/World.java");
			deleteFile("/mod.one/bin/org/astro/World.class");
			File rootDir = new File(p1.getProject().findMember("bin").getLocation().toString());
			Util.zip(rootDir, getExternalResourcePath(libPath));

			String[] src2 = new String[] {
				"src/module-info.java",
				"module com.greetings {\n" +
				"	requires mod.one;\n" +
				"}",
				"src/com/greetings/MyTest.java",
				"package com.greetings;\n" +
				"public class MyTest extends one.p.C {}"
			};
			IClasspathEntry dep1 = JavaCore.newProjectEntry(p.getPath(), null, false,
															new IClasspathAttribute[] {new ClasspathAttribute("module", "true")},
															false/*not exported*/);
			IClasspathEntry dep2 = JavaCore.newLibraryEntry(new Path(getExternalResourcePath(libPath)), null, null, null,
															new IClasspathAttribute[] {
																	new ClasspathAttribute("module", "true"),
															},
															false/*not exported*/);
			IJavaProject p2 = setupModuleProject("com.greetings", src2, new IClasspathEntry[] { dep1, dep2 });
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"The project was not built since its build path is incomplete. Cannot find the class file for org.astro.World. Fix the build path then try building this project\n" +
					"The type org.astro.World cannot be resolved. It is indirectly referenced from required type one.p.C",
					markers);
			IClasspathAttribute[] attrs = new IClasspathAttribute[] {
					JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true"),
					JavaCore.newClasspathAttribute(IClasspathAttribute.ADD_READS, "mod.one=org.astro")
			};
			setCPAttributes(p2, attrs, dep2);
			getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
			markers = p2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",	"",  markers);
		} finally {
			deleteExternalResource(libPath);
			deleteProject("mod.one");
			deleteProject("org.astro");
			deleteProject("com.greetings");
		}
	}
	public void _testPatchModule() throws CoreException, IOException {
		if (!isJRE9) return;
		try {
			String[] sources = {
				"src/module-info.java",
				"module org.astro {\n" +
				"	requires java.base;\n" +
				"	requires java.desktop;\n" +
				"	requires java.datatransfer;\n" +
				"	requires java.sql;\n" +
				"	exports org.astro;\n" +
				"}\n",
				"src/org/astro/Test2.java",
				"package org.astro;\n" +
				"public class Test2 {\n" +
				"	java.awt.Window window;\n" +
				"}\n"
			};
			IJavaProject project = setupModuleProject("org.astro", sources);

			sources = new String[] {
					"src/code/Code.java",
					"package code;\n" +
					"class Code {\n" +
					"}\n",
					"src2/org/astro/Galaxy.java",
					"package org.astro;\n" +
					"public class Galaxy { }\n"
				};
			IClasspathAttribute[] attributes = {
					JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true"),
			};
			IClasspathEntry cp = JavaCore.newProjectEntry(project.getPath(), null, false, attributes, false);
			IJavaProject p2 = setupModuleProject("patch", new String[] {"src", "src2"}, sources, null);
			attributes = new IClasspathAttribute[] {
					JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true"),
					JavaCore.newClasspathAttribute(IClasspathAttribute.PATCH_MODULE, "org.astro")
			};
			IClasspathEntry cp2 = JavaCore.newProjectEntry(p2.getPath(), null, false, attributes, false);
			sources = new String[] {
					"src/module-info.java",
					"module test {\n" +
					"	requires org.astro;\n" +
					"}\n",
					"src/test/Test.java",
					"package test;\n" +
					"class Test {\n" +
					"	org.astro.World w = null;\n" +
					"	org.astro.Galaxy g = null;\n" +
					"}\n",
			};
			IJavaProject p3 = setupModuleProject("test", sources, new IClasspathEntry[] {cp, cp2});
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p3.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"",
					markers);
		} finally {
			this.deleteProject("org.astro");
			this.deleteProject("patch");
			this.deleteProject("test");
		}

	}
	public void testPatchModule() throws CoreException, IOException {
		if (!isJRE9) return;
		try {
			IClasspathAttribute[] attributes = {
					JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true")
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
			IMarker[] markers = patchProject.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"The type jdk.internal.misc.Unsafe is not accessible\n" +
					"Signal cannot be resolved to a type",
					markers);

			attributes = new IClasspathAttribute[] {
				JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true"),
				JavaCore.newClasspathAttribute(IClasspathAttribute.PATCH_MODULE, "java.base")
			};
			setJRECPAttributes(patchProject, attributes);

			getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);

			markers = patchProject.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"",
					markers);
		} finally {
			this.deleteProject("org.astro.patch");
		}
	}
	public void testPatchModule2() throws CoreException, IOException {
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

			attributes = new IClasspathAttribute[] {
				JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true"),
			};
			setJRECPAttributes(patchProject, attributes);

			getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);

			IMarker[] markers = patchProject.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"The type jdk.internal.misc.Unsafe is not accessible\n" +
					"Signal cannot be resolved to a type",
					markers);
		} finally {
			this.deleteProject("org.astro.patch");
		}
	}
	private void setCPAttributes(IJavaProject javaProject, IClasspathAttribute[] attributes, IClasspathEntry entryToReplace) throws JavaModelException {
		IClasspathEntry[] oldClasspath= javaProject.getRawClasspath();
		int nEntries= oldClasspath.length;
		IClasspathEntry[] newEntries= Arrays.copyOf(oldClasspath, nEntries);
		for (int i = 0; i < newEntries.length; i++) {
			if (newEntries[i].getPath().equals(entryToReplace.getPath())) {
				switch(entryToReplace.getEntryKind()) {
				case IClasspathEntry.CPE_PROJECT:
					newEntries[i] = JavaCore.newProjectEntry(entryToReplace.getPath(), entryToReplace.getAccessRules(), entryToReplace.combineAccessRules(), attributes, entryToReplace.isExported());
					break;
				case IClasspathEntry.CPE_LIBRARY:
					newEntries[i] = JavaCore.newLibraryEntry(entryToReplace.getPath(), newEntries[i].getSourceAttachmentPath(), null, null, attributes, false);
					break;
					default:
						// not supported
						break;
				}
				break;
			}
		}
		javaProject.setRawClasspath(newEntries, null);
	}
	private void setJRECPAttributes(IJavaProject javaProject, IClasspathAttribute[] attributes) throws JavaModelException {
		IClasspathEntry[] oldClasspath= javaProject.getRawClasspath();
		int nEntries= oldClasspath.length;
		IClasspathEntry[] newEntries= Arrays.copyOf(oldClasspath, nEntries);
		for (int i = 0; i < newEntries.length; i++) {
			if (newEntries[i].getPath().equals(getJRE9Path())) {
				newEntries[i] = JavaCore.newLibraryEntry(getJRE9Path(), newEntries[i].getSourceAttachmentPath(), null, null, attributes, false);
				break;
			}
		}
		javaProject.setRawClasspath(newEntries, null);
	}
}
