/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation and others.
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
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.search.indexing.IndexRequest;

public class ResolveTests9 extends AbstractJavaModelTests {
	private static final int MODULE = 1;
	private static final int WITHOUT_TEST = 2;
	private static final int TEST = 4;

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
	@Override
	public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
		return super.getWorkingCopy(path, source, this.wcOwner);
	}
	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();

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

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.wcOwner = new WorkingCopyOwner(){};
	}
	@Override
	public void tearDownSuite() throws Exception {
		deleteProject("Resolve");

		super.tearDownSuite();
	}

	@Override
	protected void tearDown() throws Exception {
		if (this.wc != null) {
			this.wc.discardWorkingCopy();
		}
		super.tearDown();
	}

	void addProjectEntry(IJavaProject thisProject, IJavaProject otherProject, int flags) throws JavaModelException {
		addClasspathEntry(thisProject,
				JavaCore.newProjectEntry(otherProject.getPath(), null, false, attributes(flags), false));
	}
	IClasspathAttribute[] attributes(int flags) {
		List<IClasspathAttribute> attrs = new ArrayList<>();
		if ((flags & MODULE) != 0)
			attrs.add(JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true"));
		if ((flags & WITHOUT_TEST) != 0)
			attrs.add(JavaCore.newClasspathAttribute(IClasspathAttribute.WITHOUT_TEST_CODE, "true"));
		if ((flags & TEST) != 0)
			attrs.add(JavaCore.newClasspathAttribute(IClasspathAttribute.TEST, "true"));
		return attrs.toArray(new IClasspathAttribute[attrs.size()]);
	}
	void addTestSrc(IJavaProject prj) throws CoreException {
		IPath path = prj.getProject().getFullPath();
		IClasspathEntry testSrc = JavaCore.newSourceEntry(path.append(new Path("src-test")),
				null, null, path.append(new Path("bin-test")),
				new IClasspathAttribute[] {JavaCore.newClasspathAttribute(IClasspathAttribute.TEST, "true")});
		addClasspathEntry(prj, testSrc);
		createFolder(prj.getElementName() + "/src-test");
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
					"package test;\n" +
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
	public void testModuleInfo_serviceInterface_almost_OK() throws CoreException {
		IFile modInfo = null;
		IFolder testFolder = null;
		IndexManager indexManager = JavaModelManager.getIndexManager();
		try {
			// block the index manager to ensure it's not ready when code select is performed:
			indexManager.request(new IndexRequest(new Path("blocker"), indexManager) {
				@Override
				public boolean execute(IProgressMonitor progress) {
					try {
						while (!this.isCancelled)
							Thread.sleep(1000);
					} catch (InterruptedException e) { /* ignore */ }
					return false;
				}
			});
			testFolder = createFolder("/Resolve/src/test");
			createFile(
					"/Resolve/src/test/ITest.java",
					"public interface ITest {}\n"); // missing package declaration
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

			// still, if we get here before indexes are ready,
			// then OperationCanceledException will send us into SearchableEnvironment.findTypes(String, ISearchRequestor, int),
			// which succeeds:
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
			indexManager.discardJobs("blocker");
		}
	}
	public void testModuleInfo_serviceInterface_NOK() throws CoreException {
		IFile modInfo = null;
		IFolder testFolder = null;
		try {
			testFolder = createFolder("/Resolve/src/test");
			createFile(
					"/Resolve/src/test/ITest.java",
					"public interface ITest {}\n"); // missing package declaration
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

			waitUntilIndexesReady();
			// even after indexes are built we meanwhile (after bug 540541) find the selected type
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
			IOrdinaryClassFile classFile = type.getClassFile();
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
			assertMarkers("Unexpected markers",
					"The package p1.p2 conflicts with a package accessible from another module: mod\n" +
					"The package p1.p2 is accessible from more than one module: <unnamed>, mod\n" +
					"The package p1.p2 is accessible from more than one module: <unnamed>, mod",
					markers);

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

	public void testBug537934() throws Exception {
		if (!isJRE9) {
			System.err.println("Test "+getName()+" requires a JRE 9");
			return;
		}
		IJavaProject gui = null;
		IJavaProject model = null;
		IJavaProject type = null;
		IJavaProject logg = null;
		try {
			// ---- module log:
			//      - has log4j on the module path
			logg = createJava9ProjectWithJREAttributes("com.igorion.log", new String[] {"src"}, attributes(MODULE));
			String jarAbsPath = logg.getProject().getLocation()+"/log4j.jar";
			createJar(new String[] {
					"log4j/Dummy.java",
					"package log4j;\n" +
					"public class Dummy {}\n"
				},
				jarAbsPath);
			addLibraryEntry(logg, new Path(jarAbsPath), null, null, null, null, attributes(MODULE), false);
			createFolder("com.igorion.log/src/com/igorion/log");
			createFile("com.igorion.log/src/com/igorion/log/ILog.java",
					"package com.igorion.log;\n public interface ILog {}\n");
			createFile("com.igorion.log/src/module-info.java",
					"module com.igorion.log {\n" +
					"	requires log4j;\n" +
					"	exports com.igorion.log;\n" +
					"}\n");
			logg.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = logg.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("markers in com.igorion.log",
					"Name of automatic module \'log4j\' is unstable, it is derived from the module\'s file name.",
					markers);

			// ---- module type:
			//      - has test sources
			type = createJava9Project("com.igorion.type");
			createFolder("com.igorion.type/src/com/igorion/type");
			createFile("com.igorion.type/src/com/igorion/type/IOther.java",
					"package com.igorion.type;\n public interface IOther {}\n");
			createFile("com.igorion.type/src/module-info.java",
					"module com.igorion.type {\n" +
					"	exports com.igorion.type;\n" +
					"}\n");
			addTestSrc(type);
			type.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			markers = type.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("markers in com.igorion.type", "", markers);

			// ---- module model:
			//      - has test sources
			//      - has log4j on the module path
			//      - has modules log & type on the module path without_test_code
			model = createJava9ProjectWithJREAttributes("com.igorion.model", new String[] {"src"}, attributes(MODULE));
			createFolder("com.igorion.model/src/com/igorion/model/define");
			createFile("com.igorion.model/src/com/igorion/model/IModel.java",
					"package com.igorion.model;\n public interface IModel {}\n");
			createFile("com.igorion.model/src/com/igorion/model/define/Model.java",
					"package com.igorion.model.define;\n" +
					"import com.igorion.model.IModel;\n" +
					"import java.util.Optional;\n" +
					"public class Model {\n" +
					"	public static synchronized Optional<IModel> instance() { return Optional.empty(); }\n" +
					"}\n");
			createFile("com.igorion.model/src/module-info.java",
					"module com.igorion.model {\n" +
					"	requires com.igorion.log;\n" +
					"	exports com.igorion.model;\n" +
					"	exports com.igorion.model.define;\n" +
					"}\n");
			addTestSrc(model);
			addLibraryEntry(model, new Path(jarAbsPath), null, null, null, null, attributes(MODULE), false);
			addProjectEntry(model, logg, MODULE|WITHOUT_TEST);
			addProjectEntry(model, type, MODULE|WITHOUT_TEST);
			model.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			markers = model.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("markers in com.igorion.model", "", markers);

			// ---- module gui:
			//      - has log4j on the module path for test code
			//      - has modules type, model, log on the module path without_test_code (order is significant)
			gui = createJava9ProjectWithJREAttributes("com.igorion.gui", new String[] {"src"}, attributes(MODULE));
			addTestSrc(gui);
			createFolder("com.igorion.gui/src/com/igorion/gui");
			String source =
					"package com.igorion.gui;\n" +
					"import com.igorion.model.IModel;\n" +
					"import com.igorion.model.define.Model;\n" +
					"import java.util.Optional;\n" +
					"public class Reproduce {\n" +
					"	static void meth() {\n" +
					"		Optional<IModel> oModel = Model.instance();\n" +
					"		if (oModel.isPresent())\n" +
					"			oModel.get();\n" +
					"	}\n" +
					"}\n";
			createFile("com.igorion.gui/src/com/igorion/gui/Reproduce.java", source);
			createFile("com.igorion.gui/src/module-info.java",
					"module com.igorion.gui {\n" +
					"	requires com.igorion.type;\n" +
					"	requires com.igorion.model;\n" +
					"}\n");
			addLibraryEntry(gui, new Path(jarAbsPath), null, null, null, null, attributes(MODULE|TEST), false);
			addProjectEntry(gui, type, MODULE|WITHOUT_TEST);
			addProjectEntry(gui, model, MODULE|WITHOUT_TEST);
			addProjectEntry(gui, logg, MODULE|WITHOUT_TEST);
			gui.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			markers = gui.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("markers in com.igorion.gui", "", markers);

			// test that selection finds a fully resolved type Optional<IModel>:
			ICompilationUnit unit = getCompilationUnit("com.igorion.gui/src/com/igorion/gui/Reproduce.java");
			int start = source.indexOf("get(");
			IJavaElement[] selected = unit.codeSelect(start, 3);
			assertElementsEqual(
					"Unexpected elements",
					"get() [in Optional [in Optional.class [in java.util [in <module:java.base>]]]]",
					selected);
		} finally {
			deleteProject(gui);
			deleteProject(model);
			deleteProject(type);
			deleteProject(logg);
		}
	}
	public void testModuleCaching() throws Exception {
		if (!isJRE9 || isJRE11) {
			System.err.println("Test "+getName()+" requires a JRE [9,11)");
			return;
		}
		IJavaProject prj = createJava9Project("caching");
		try {
			IFile file = createFile("caching/src/module-info.java",
					"module caching {\n" +
					"	requires java.xml.bind;\n" +
					"}\n");
			createFolder("caching/src/p");
			createFile("caching/src/p/X.java",
					"package p;\n" +
					"@javax.xml.bind.annotation.XmlRootElement\n" +
					"public class X {}\n");
			prj.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = prj.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("unexpected markers",
					"The module java.xml.bind has been deprecated since version 9 and marked for removal",  markers);

			IModuleDescription module = prj.getModuleDescription();
			assertTrue("module exists", module.exists());
			assertEquals("module name", "caching", module.getElementName());

			// move module-info away ...
			createFolder("caching/away");
			file.move(new Path("/caching/away/module-info.java"), false, null);
			IModuleDescription mod2 = prj.getModuleDescription();
			assertNull("no longer a module", mod2);

			// ... and observe javax.xml.bind unobservable:
			prj.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			markers = prj.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			assertMarkers("unexpected markers",
					"javax.xml.bind cannot be resolved to a type", markers);
		} finally {
			deleteProject(prj);
		}
	}
}
