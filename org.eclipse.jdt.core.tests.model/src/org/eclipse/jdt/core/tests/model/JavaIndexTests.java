/*******************************************************************************
 * Copyright (c) 2012, 2022 IBM Corporation and others.
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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.CRC32;
import junit.framework.Test;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.index.JavaIndexer;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.UserLibraryClasspathContainer;
import org.eclipse.jdt.internal.core.index.DiskIndex;
import org.eclipse.jdt.internal.core.index.Index;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.osgi.service.prefs.BackingStoreException;

public class JavaIndexTests extends AbstractJavaSearchTests  {

	static {
		// TESTS_NAMES = new String[] {"testPlatformIndexFile"};
	}
	public JavaIndexTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(JavaIndexTests.class);
	}

	// Test that the index file is really generated.
	public void testGenerateIndex() throws IOException {
		String indexFilePath = getExternalResourcePath("Test.index");
		String jarFilePath = getExternalResourcePath("Test.jar");
		try {
			createJar(new String[] {
					"pkg/Test.java",
					"package pkg;\n" +
					"public class Test {\n" +
					"  protected Test(int i) {}\n" +
					"}"}, jarFilePath);

			JavaIndexer.generateIndexForJar(jarFilePath, indexFilePath);
			assertTrue(new File(indexFilePath).exists());
		} finally {
			new File(indexFilePath).delete();
			new File(jarFilePath).delete();
		}
	}

	// Test that the index file and the jar can be deleted after the indexing is done
	// This is to ensure that the files are closed
	public void testDeleteIndexedFile() {
		String indexFilePath = getExternalResourcePath("Test.index");
		String jarFilePath = getExternalResourcePath("Test.jar");
		try {
			createJar(new String[] {
					"pkg/Test.java",
					"package pkg;\n" +
					"public class Test {\n" +
					"  protected Test(int i) {}\n" +
					"}"}, jarFilePath);
			JavaIndexer.generateIndexForJar(jarFilePath, indexFilePath);
			assertTrue("Could not delete the index file", new File(indexFilePath).delete());
			assertTrue("Could not delete the jar file", new File(jarFilePath).delete());
		} catch (IOException e) {
			assertFalse("Test failed", true);
		}
	}

	// Test that search works fine with the index file
	public void testUseIndex() throws CoreException, IOException {
		String indexFilePath = getExternalResourcePath("Test.index");
		String jarFilePath = getExternalResourcePath("Test.jar");
		try {
			createJar(new String[] {
					"pkg/Test.java",
					"package pkg;\n" +
					"public class Test {\n" +
					"  protected Test(int i) {}\n" +
					"}"}, jarFilePath);
			JavaIndexer.generateIndexForJar(jarFilePath, indexFilePath);
			long modified = new File(indexFilePath).lastModified();

			IJavaProject p = createJavaProject("P");
			Path libPath = new Path(jarFilePath);
			IClasspathAttribute attribute = JavaCore.newClasspathAttribute(IClasspathAttribute.INDEX_LOCATION_ATTRIBUTE_NAME, "file:///"+indexFilePath);
			IClasspathEntry entry = JavaCore.newLibraryEntry(libPath, null, null, null, new IClasspathAttribute[]{attribute}, false);
			setClasspath(p, new IClasspathEntry[] {entry});

			waitUntilIndexesReady();

			// Test that specified index file is really used
			java.io.File indexFile = JavaModelManager.getIndexManager().getIndex(libPath, false, false).getIndexFile();
			assertEquals("Specified index file is not being used", indexFilePath,indexFile.toString());

			// Test that search works properly
			search("Test", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults(getExternalPath() + "Test.jar pkg.Test");

			// Ensure that the index file is not modified
			assertEquals(modified, new File(indexFilePath).lastModified());
		} finally {
			deleteProject("P");
			new File(indexFilePath).delete();
			new File(jarFilePath).delete();
		}
	}

	// Test that the same index file is used even after restarting
	public void testUseIndexAfterRestart() throws IOException, CoreException {
		String indexFilePath = getExternalResourcePath("Test.index");
		String jarFilePath = getExternalResourcePath("Test.jar");
		try {
			createJar(new String[] {
					"pkg/Test.java",
					"package pkg;\n" +
					"public class Test {\n" +
					"  protected Test(int i) {}\n" +
					"}"}, jarFilePath);
			JavaIndexer.generateIndexForJar(jarFilePath, indexFilePath);
			long modified = new File(indexFilePath).lastModified();
			IJavaProject p = createJavaProject("P");
			Path libPath = new Path(jarFilePath);
			IClasspathAttribute attribute = JavaCore.newClasspathAttribute(IClasspathAttribute.INDEX_LOCATION_ATTRIBUTE_NAME, "file:///"+indexFilePath);
			IClasspathEntry entry = JavaCore.newLibraryEntry(libPath, null, null, null, new IClasspathAttribute[]{attribute}, false);
			setClasspath(p, new IClasspathEntry[] {entry});
			waitUntilIndexesReady();

			search("Test", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults(getExternalPath() + "Test.jar pkg.Test");

			simulateExitRestart();
			getJavaModel().refreshExternalArchives(null, null);
			waitUntilIndexesReady();

			this.resultCollector = new JavaSearchResultCollector();
			search("Test", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults(getExternalPath() + "Test.jar pkg.Test");

			java.io.File indexFile = JavaModelManager.getIndexManager().getIndex(libPath, false, false).getIndexFile();
			assertEquals(indexFilePath,indexFile.toString());
			// Ensure that the file is not modified
			assertEquals(modified, new File(indexFilePath).lastModified());
		} finally {
			deleteProject("P");
			new File(indexFilePath).delete();
			new File(jarFilePath).delete();
		}
	}

	// Test that the same index file is used even after restarting
	public void testUseIndexInternalJarAfterRestart() throws IOException, CoreException {
		String indexFilePath = getExternalResourcePath("Test.index");
		String jarFilePath = "/P/Test.jar";
		String fullJarPath = getWorkspacePath() + jarFilePath;
		try {
			IJavaProject p = createJavaProject("P");
			createJar(new String[] {
					"pkg/Test.java",
					"package pkg;\n" +
					"public class Test {\n" +
					"  protected Test(int i) {}\n" +
					"}"}, fullJarPath);
			p.getProject().refreshLocal(1, null);
			JavaIndexer.generateIndexForJar(fullJarPath, indexFilePath);
			long modified = new File(indexFilePath).lastModified();
			IPath libPath = new Path(jarFilePath);
			IClasspathAttribute attribute = JavaCore.newClasspathAttribute(IClasspathAttribute.INDEX_LOCATION_ATTRIBUTE_NAME, "file:///"+indexFilePath);
			IClasspathEntry entry = JavaCore.newLibraryEntry(libPath, null, null, null, new IClasspathAttribute[]{attribute}, false);
			setClasspath(p, new IClasspathEntry[] {entry});
			waitUntilIndexesReady();

			search("Test", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults("Test.jar pkg.Test [No source]");
			java.io.File indexFile = JavaModelManager.getIndexManager().getIndex(libPath, false, false).getIndexFile();
			assertEquals(indexFilePath,indexFile.toString());

			simulateExitRestart();
			waitUntilIndexesReady();

			this.resultCollector = new JavaSearchResultCollector();
			search("Test", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults("Test.jar pkg.Test [No source]");

			indexFile = JavaModelManager.getIndexManager().getIndex(libPath, false, false).getIndexFile();
			assertEquals(indexFilePath,indexFile.toString());
			// Ensure that the file is not modified
			assertEquals(modified, new File(indexFilePath).lastModified());
		} finally {
			deleteProject("P");
			new File(indexFilePath).delete();
		}
	}

	// Test that a jar file that gets modified after the index is created doesn't return new changes.
	// This behavior might have to be modified but..
	public void testModifyJarAfterIndex() throws CoreException, IOException {
		String indexFilePath = getExternalResourcePath("Test.index");
		String jarFilePath = getExternalResourcePath("Test.jar");
		try {
			createJar(new String[] {
					"pkg/Test.java",
					"package pkg;\n" +
					"public class Test {\n" +
					"  protected Test(int i) {}\n" +
					"}"}, jarFilePath);
			JavaIndexer.generateIndexForJar(jarFilePath, indexFilePath);
			createJar(new String[] {
					"pkg/Test.java",
					"package pkg;\n" +
					"public class Test {\n" +
					"  protected Test(int i) {}\n" +
					"}",
					"pkg/NewTest.java",
					"package pkg;\n" +
					"public class NewTest {\n" +
					"  protected NewTest(int i) {}\n" +
					"}"}, jarFilePath);
			IJavaProject p = createJavaProject("P");
			Path libPath = new Path(jarFilePath);
			IClasspathAttribute attribute = JavaCore.newClasspathAttribute(IClasspathAttribute.INDEX_LOCATION_ATTRIBUTE_NAME, "file:///"+indexFilePath);
			IClasspathEntry entry = JavaCore.newLibraryEntry(libPath, null, null, null, new IClasspathAttribute[]{attribute}, false);
			setClasspath(p, new IClasspathEntry[] {entry});
			waitUntilIndexesReady();
			search("NewTest", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults("");
		} finally {
			deleteProject("P");
			new File(indexFilePath).delete();
			new File(jarFilePath).delete();
		}
	}

	// test a non-existent index
	public void testNonExistentIndex() throws CoreException, IOException {
		String indexFilePath = getExternalResourcePath("Test.index");
		String jarFilePath = getExternalResourcePath("Test.jar");
		try {
			createJar(new String[] {
					"pkg/Test.java",
					"package pkg;\n" +
					"public class Test {\n" +
					"  protected Test(int i) {}\n" +
					"}"}, jarFilePath);
			IJavaProject p = createJavaProject("P");
			Path libPath = new Path(jarFilePath);
			new File(indexFilePath).delete();
			IClasspathAttribute attribute = JavaCore.newClasspathAttribute(IClasspathAttribute.INDEX_LOCATION_ATTRIBUTE_NAME, "file:///"+indexFilePath);
			IClasspathEntry entry = JavaCore.newLibraryEntry(libPath, null, null, null, new IClasspathAttribute[]{attribute}, false);
			setClasspath(p, new IClasspathEntry[] {entry});
			waitUntilIndexesReady();
			search("Test", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults(getExternalPath() + "Test.jar pkg.Test");

		} finally {
			deleteProject("P");
			new File(indexFilePath).delete();
			new File(jarFilePath).delete();
		}
	}

	// test a non-existent index
	public void testNonExistentIndexRestart() throws CoreException, IOException {
		String indexFilePath = getExternalResourcePath("Test.index");
		String jarFilePath = getExternalResourcePath("Test.jar");
		try {
			createJar(new String[] {
					"pkg/Test.java",
					"package pkg;\n" +
					"public class Test {\n" +
					"  protected Test(int i) {}\n" +
					"}"}, jarFilePath);

			new File(indexFilePath).delete();

			IJavaProject p = createJavaProject("P");
			Path libPath = new Path(jarFilePath);
			IClasspathAttribute attribute = JavaCore.newClasspathAttribute(IClasspathAttribute.INDEX_LOCATION_ATTRIBUTE_NAME, "file:///"+indexFilePath);
			IClasspathEntry entry = JavaCore.newLibraryEntry(libPath, null, null, null, new IClasspathAttribute[]{attribute}, false);
			setClasspath(p, new IClasspathEntry[] {entry});
			waitUntilIndexesReady();

			search("Test", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults(getExternalPath() + "Test.jar pkg.Test");

			java.io.File indexFile = JavaModelManager.getIndexManager().getIndex(libPath, false, false).getIndexFile();
			long modified = indexFile.lastModified();
			assertEquals(modified, indexFile.lastModified());

			simulateExitRestart();
			getJavaModel().refreshExternalArchives(null,null);
			waitUntilIndexesReady();

			this.resultCollector = new JavaSearchResultCollector();
			search("Test", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults(getExternalPath() + "Test.jar pkg.Test");

			indexFile = JavaModelManager.getIndexManager().getIndex(libPath, false, false).getIndexFile();
			// XXX see bug 534548
			// assertEquals("Index File should not have got modified", modified, indexFile.lastModified());
		} finally {
			deleteProject("P");
			new File(indexFilePath).delete();
			new File(jarFilePath).delete();
		}
	}

	// test that if the index is not existent after restart, it should build up a new index
	public void testNonExistentIndexAfterRestart() throws CoreException, IOException {
		String indexFilePath = getExternalResourcePath("Test.index");
		String jarFilePath = getExternalResourcePath("Test.jar");
		try {
			createJar(new String[] {
					"pkg/Test.java",
					"package pkg;\n" +
					"public class Test {\n" +
					"protected Test(int i) {}\n" + "}"
					},jarFilePath);
			JavaIndexer.generateIndexForJar(jarFilePath, indexFilePath);

			IJavaProject p = createJavaProject("P");
			Path libPath = new Path(jarFilePath);
			IClasspathAttribute attribute = JavaCore.newClasspathAttribute(IClasspathAttribute.INDEX_LOCATION_ATTRIBUTE_NAME, "file:///"+indexFilePath);
			IClasspathEntry entry = JavaCore.newLibraryEntry(libPath, null, null, null, new IClasspathAttribute[]{attribute}, false);
			setClasspath(p, new IClasspathEntry[] {entry});
			waitUntilIndexesReady();

			search("Test", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults(getExternalPath() + "Test.jar pkg.Test");

			simulateExitRestart();
			File indexFile = new File(indexFilePath);
			indexFile.delete();
			assertTrue(!indexFile.exists());
			getJavaModel().refreshExternalArchives(null,null);
			waitUntilIndexesReady();

			this.resultCollector = new JavaSearchResultCollector();
			search("Test", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults(getExternalPath() + "Test.jar pkg.Test");

		} finally {
			deleteProject("P");
			new File(indexFilePath).delete();
			new File(jarFilePath).delete();
		}
	}

	// test a non-existent index which becomes existent after restart
	public void testExistentIndexAfterRestart() throws CoreException, IOException {
		String indexFilePath = getExternalResourcePath("Test.index");
		String jarFilePath = getExternalResourcePath("Test.jar");
		try {
			createJar(new String[] {
					"pkg/Test.java",
					"package pkg;\n" +
					"public class Test {\n" +
					"  protected Test(int i) {}\n" +
					"}"}, jarFilePath);

			File indexFile = new File(indexFilePath);
			indexFile.delete();
			assertTrue(!indexFile.exists());

			IJavaProject p = createJavaProject("P");
			Path libPath = new Path(jarFilePath);
			IClasspathAttribute attribute = JavaCore.newClasspathAttribute(IClasspathAttribute.INDEX_LOCATION_ATTRIBUTE_NAME, "file:///"+indexFilePath);
			IClasspathEntry entry = JavaCore.newLibraryEntry(libPath, null, null, null, new IClasspathAttribute[]{attribute}, false);
			setClasspath(p, new IClasspathEntry[] {entry});
			waitUntilIndexesReady();

			search("Test", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults(getExternalPath() + "Test.jar pkg.Test");

			JavaIndexer.generateIndexForJar(jarFilePath, indexFilePath);
			simulateExitRestart();
			getJavaModel().refreshExternalArchives(null,null);
			waitUntilIndexesReady();

			this.resultCollector = new JavaSearchResultCollector();
			search("Test", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults(getExternalPath() + "Test.jar pkg.Test");

			indexFile = JavaModelManager.getIndexManager().getIndex(libPath, false, false).getIndexFile();
			assertEquals(indexFilePath,indexFile.toString());
		} finally {
			deleteProject("P");
			new File(indexFilePath).delete();
			new File(jarFilePath).delete();
		}
	}

	// Test that the index file is not deleted when the project is deleted
	public void testDeleteProject() throws CoreException, IOException {
		String indexFilePath = getExternalResourcePath("Test.index");
		String jarFilePath = getExternalResourcePath("Test.jar");
		try {
			createJar(new String[] {
					"pkg/Test.java",
					"package pkg;\n" +
					"public class Test {\n" +
					"  protected Test(int i) {}\n" +
					"}"}, jarFilePath);
			JavaIndexer.generateIndexForJar(jarFilePath, indexFilePath);
			IJavaProject p = createJavaProject("P");
			createExternalFolder("externalLib");
			Path libPath = new Path(jarFilePath);
			IClasspathAttribute attribute = JavaCore.newClasspathAttribute(IClasspathAttribute.INDEX_LOCATION_ATTRIBUTE_NAME, "file:///"+indexFilePath);
			IClasspathEntry entry = JavaCore.newLibraryEntry(libPath, null, null, null, new IClasspathAttribute[]{attribute}, false);
			setClasspath(p, new IClasspathEntry[] {entry});
			waitUntilIndexesReady();
			deleteProject("P");
			File f = new File(indexFilePath);
			assertTrue(f.exists());
		} finally {
			new File(indexFilePath).delete();
			new File(jarFilePath).delete();
		}
	}



	// Test index file in platform
	public void testPlatformIndexFile() throws CoreException, IOException {
		String indexFilePath = null;
		String jarFilePath = getExternalResourcePath("Test.jar");
		String indexUrl = "platform:/resource/P/Test.index";
		try {
			createJar(new String[] {
					"pkg/Test.java",
					"package pkg;\n" +
					"public class Test {\n" +
					"  protected Test(int i) {}\n" +
					"}"}, jarFilePath);

			IJavaProject p = createJavaProject("P");
			indexFilePath = p.getProject().getLocation().append("Test.index").toFile().getAbsolutePath();
			JavaIndexer.generateIndexForJar(jarFilePath, indexFilePath);
			long modified = new File(indexFilePath).lastModified();

			Path libPath = new Path(jarFilePath);
			IClasspathAttribute attribute = JavaCore.newClasspathAttribute(IClasspathAttribute.INDEX_LOCATION_ATTRIBUTE_NAME, indexUrl);
			IClasspathEntry entry = JavaCore.newLibraryEntry(libPath, null, null, null, new IClasspathAttribute[]{attribute}, false);
			setClasspath(p, new IClasspathEntry[] {entry});
			waitUntilIndexesReady();

			search("Test", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults(getExternalPath() + "Test.jar pkg.Test");

			String indexFileName = JavaModelManager.getIndexManager().getIndex(libPath, false, false).getIndexFile().getName();
			assertEquals(indexFileName, "Test.index");

			simulateExitRestart();
			getJavaModel().refreshExternalArchives(null,null);
			waitUntilIndexesReady();

			this.resultCollector = new JavaSearchResultCollector();
			search("Test", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults(getExternalPath() + "Test.jar pkg.Test");

			indexFileName = JavaModelManager.getIndexManager().getIndex(libPath, false, false).getIndexFile().getName();
			assertEquals(indexFileName, "Test.index");

			assertEquals(modified, new File(indexFilePath).lastModified());
		} finally {
			deleteProject("P");
			if (indexFilePath != null) new File(indexFilePath).delete();
			new File(jarFilePath).delete();
		}
	}


	public void testEditClasspath() throws CoreException, IOException {
		String indexFilePath = getExternalResourcePath("Test.index");
		String jarFilePath = getExternalResourcePath("Test.jar");
		try {
			createJar(new String[] {
					"pkg/Test.java",
					"package pkg;\n" +
					"public class Test {\n" +
					"  protected Test(int i) {}\n" +
					"}"}, jarFilePath);
			JavaIndexer.generateIndexForJar(jarFilePath, indexFilePath);
			File f = new File(indexFilePath);
			long modified = f.lastModified();
			IJavaProject p = this.createJavaProject("P", new String[] {}, "bin");

			String content = new String(
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
					+ "<classpath>\n"
					+ "<classpathentry kind=\"src\" path=\"src a\"/>\n"
					+ "<classpathentry kind=\"src\" path=\"src x\"/>\n"
					+ "<classpathentry kind=\"lib\" path=\""
					+ getExternalJCLPath()
					+ "\"/>\n"
					+ "<classpathentry kind=\"lib\" path=\""
					+ jarFilePath
					+ "\">"
					+ "<attributes>\n"
					+ "	<attribute name=\"index_location\" value=\"file:///"
					+ indexFilePath
					+"\"/>\n"
					+ "</attributes>\n"
					+ "</classpathentry>\n"
					+ "<classpathentry kind=\"output\" path=\"bin\"/>\n"
					+ "</classpath>\n");

			editFile("/P/.classpath", content);
			p.open(null);
			waitUntilIndexesReady();
			search("Test", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults(getExternalPath() + "Test.jar pkg.Test");
			java.io.File indexFile = JavaModelManager.getIndexManager().getIndex(new Path(jarFilePath), false, false).getIndexFile();
			assertEquals(indexFilePath,indexFile.toString());
			f = new File(indexFilePath);
			assertEquals(modified, f.lastModified());
		} finally {
			deleteProject("P");
			new File(indexFilePath).delete();
			new File(jarFilePath).delete();
		}
	}

	// Test changing the classpath
	public void testChangeClasspath() throws CoreException, IOException {
		String indexFilePath = getExternalResourcePath("Test.index");
		String jarFilePath = getExternalResourcePath("Test.jar");
		try {
			createJar(new String[] {
					"pkg/Test.java",
					"package pkg;\n" +
					"public class Test {\n" +
					"  protected Test(int i) {}\n" +
					"}"}, jarFilePath);
			JavaIndexer.generateIndexForJar(jarFilePath, indexFilePath);
			createJar(new String[] {
					"pkg/Test.java",
					"package pkg;\n" +
					"public class Test {\n" +
					"  protected Test(int i) {}\n" +
					"}",
					"pkg/NewTest.java",
					"package pkg;\n" +
					"public class NewTest {\n" +
					"  protected NewTest(int i) {}\n" +
					"}"}, jarFilePath);
			IJavaProject p = createJavaProject("P");
			Path libPath = new Path(jarFilePath);

			IClasspathEntry entry = JavaCore.newLibraryEntry(libPath, null, null, null, null, false);
			setClasspath(p, new IClasspathEntry[] {entry});
			waitUntilIndexesReady();
			search("NewTest", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults(getExternalPath() + "Test.jar pkg.NewTest");

			IClasspathAttribute attribute = JavaCore.newClasspathAttribute(IClasspathAttribute.INDEX_LOCATION_ATTRIBUTE_NAME, "file:///"+indexFilePath);
			entry = JavaCore.newLibraryEntry(libPath, null, null, null, new IClasspathAttribute[]{attribute}, false);
			setClasspath(p, new IClasspathEntry[] {entry});
			waitUntilIndexesReady();
			this.resultCollector = new JavaSearchResultCollector();
			search("NewTest", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults("");

			entry = JavaCore.newLibraryEntry(libPath, null, null, null, null, false);
			setClasspath(p, new IClasspathEntry[] {entry});
			waitUntilIndexesReady();
			this.resultCollector = new JavaSearchResultCollector();
			search("NewTest", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults(getExternalPath() + "Test.jar pkg.NewTest");


		} finally {
			deleteProject("P");
			new File(indexFilePath).delete();
			new File(jarFilePath).delete();
		}
	}

	// Test changing the classpath
	public void testChangeClasspathForInternalJar() throws CoreException, IOException {
		String indexFilePath = getExternalResourcePath("Test.index");
		String jarFilePath = "/P/Test.jar";
		String fullJarPath = getWorkspacePath() + jarFilePath;
		try {
			IJavaProject p = createJavaProject("P");
			createJar(new String[] {
					"pkg/Test.java",
					"package pkg;\n" +
					"public class Test {\n" +
					"  protected Test(int i) {}\n" +
					"}"}, fullJarPath);
			JavaIndexer.generateIndexForJar(fullJarPath, indexFilePath);
			createJar(new String[] {
					"pkg/Test.java",
					"package pkg;\n" +
					"public class Test {\n" +
					"  protected Test(int i) {}\n" +
					"}",
					"pkg/NewTest.java",
					"package pkg;\n" +
					"public class NewTest {\n" +
					"  protected NewTest(int i) {}\n" +
					"}"}, fullJarPath);
			p.getProject().refreshLocal(1, null);
			Path libPath = new Path(jarFilePath);

			IClasspathEntry entry = JavaCore.newLibraryEntry(libPath, null, null, null, null, false);
			setClasspath(p, new IClasspathEntry[] {entry});
			waitUntilIndexesReady();
			search("NewTest", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults("Test.jar pkg.NewTest [No source]");

			IClasspathAttribute attribute = JavaCore.newClasspathAttribute(IClasspathAttribute.INDEX_LOCATION_ATTRIBUTE_NAME, "file:///"+indexFilePath);
			entry = JavaCore.newLibraryEntry(libPath, null, null, null, new IClasspathAttribute[]{attribute}, false);
			setClasspath(p, new IClasspathEntry[] {entry});
			waitUntilIndexesReady();
			this.resultCollector = new JavaSearchResultCollector();
			search("NewTest", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults("");

			entry = JavaCore.newLibraryEntry(libPath, null, null, null, null, false);
			setClasspath(p, new IClasspathEntry[] {entry});
			waitUntilIndexesReady();
			this.resultCollector = new JavaSearchResultCollector();
			search("NewTest", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults("Test.jar pkg.NewTest [No source]");
		} finally {
			deleteProject("P");
			new File(indexFilePath).delete();
		}
	}

	public void testMultipleProjects() throws CoreException, IOException {
		String indexFilePath = getExternalResourcePath("Test.index");
		String jarFilePath = getExternalResourcePath("Test.jar");
		try {
			createJar(new String[] {
					"pkg/Test.java",
					"package pkg;\n" +
					"public class Test {\n" +
					"  protected Test(int i) {}\n" +
					"}"}, jarFilePath);
			JavaIndexer.generateIndexForJar(jarFilePath, indexFilePath);

			IJavaProject p1 = createJavaProject("P1");
			Path libPath = new Path(jarFilePath);
			IClasspathEntry entry = JavaCore.newLibraryEntry(libPath, null, null, null, null, false);
			setClasspath(p1, new IClasspathEntry[] {entry});
			waitUntilIndexesReady();

			IJavaProject p2 = createJavaProject("P2");
			IClasspathAttribute attribute = JavaCore.newClasspathAttribute(IClasspathAttribute.INDEX_LOCATION_ATTRIBUTE_NAME, "file:///"+indexFilePath);
			entry = JavaCore.newLibraryEntry(libPath, null, null, null, new IClasspathAttribute[]{attribute}, false);
			setClasspath(p2, new IClasspathEntry[] {entry});
			waitUntilIndexesReady();

			search("Test", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p1}));
			assertSearchResults(getExternalPath() + "Test.jar pkg.Test");

			File indexFile = JavaModelManager.getIndexManager().getIndex(libPath, false, false).getIndexFile();
			assertEquals(indexFilePath,indexFile.toString());

		} finally {
			deleteProject("P1");
			deleteProject("P2");
			new File(indexFilePath).delete();
			new File(jarFilePath).delete();
		}
	}

	public void setContainerPath(IJavaProject p, IPath jarPath, String indexLocation) throws CoreException, BackingStoreException {
		// Create new user library "SomeUserLibrary"
		ClasspathContainerInitializer initializer= JavaCore.getClasspathContainerInitializer(JavaCore.USER_LIBRARY_CONTAINER_ID);
		String libraryName = "SomeUserLibrary";
		IPath containerPath = new Path(JavaCore.USER_LIBRARY_CONTAINER_ID);
		UserLibraryClasspathContainer containerSuggestion = new UserLibraryClasspathContainer(libraryName);
		initializer.requestClasspathContainerUpdate(containerPath.append(libraryName), null, containerSuggestion);

		// Modify user library
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(JavaCore.PLUGIN_ID);
		String propertyName = JavaModelManager.CP_USERLIBRARY_PREFERENCES_PREFIX+"SomeUserLibrary";
		StringBuilder propertyValue = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<userlibrary systemlibrary=\"false\" version=\"1\">\r\n<archive ");
		//String jarFullPath = getWorkspaceRoot().getLocation().append(jarFile.getFullPath()).toString();
		propertyValue.append(" path=\"" + jarPath + "\">\r\n");
		propertyValue.append(" <attributes>\r\n");
		propertyValue.append("		<attribute name=\"index_location\" value=\"");
		propertyValue.append(indexLocation);
		propertyValue.append("\"/>\r\n</attributes>\r\n");
		propertyValue.append("</archive>\r\n");
		propertyValue.append("</userlibrary>\r\n");
		preferences.put(propertyName, propertyValue.toString());
		preferences.flush();

		IClasspathEntry[] entries = p.getRawClasspath();
		int length = entries.length;
		System.arraycopy(entries, 0, entries = new IClasspathEntry[length+1], 0, length);
		entries[length] = JavaCore.newContainerEntry(containerSuggestion.getPath());
		p.setRawClasspath(entries, null);
	}

	public void testUserLibraryIndex() throws IOException, CoreException, BackingStoreException {
		String indexFilePath = getExternalResourcePath("Test.index");
		String jarFilePath = getExternalResourcePath("Test.jar");
		try {
			createJar(new String[] {
					"pkg/Test.java",
					"package pkg;\n" +
					"public class Test {\n" +
					"  protected Test(int i) {}\n" +
					"}"}, jarFilePath);
			JavaIndexer.generateIndexForJar(jarFilePath, indexFilePath);
			long modified = new File(indexFilePath).lastModified();

			IJavaProject p = createJavaProject("P");

			Path libPath = new Path(jarFilePath);
			setContainerPath(p, libPath, "file:///"+indexFilePath);

			waitUntilIndexesReady();

			// Test that specified index file is really used
			java.io.File indexFile = JavaModelManager.getIndexManager().getIndex(libPath, false, false).getIndexFile();
			assertEquals("Specified index file is not being used", indexFilePath,indexFile.toString());

			// Test that search works properly
			search("Test", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults(getExternalPath() + "Test.jar pkg.Test");
			// Ensure that the index file is not modified
			assertEquals(modified, new File(indexFilePath).lastModified());

			simulateExitRestart();
			getJavaModel().refreshExternalArchives(null,null);
			waitUntilIndexesReady();

			// Test that specified index file is really used
			indexFile = JavaModelManager.getIndexManager().getIndex(libPath, false, false).getIndexFile();
			assertEquals("Specified index file is not being used", indexFilePath,indexFile.toString());

			// Test that search works properly
			this.resultCollector = new JavaSearchResultCollector();
			search("Test", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults(getExternalPath() + "Test.jar pkg.Test");
			// Ensure that the index file is not modified
			assertEquals(modified, new File(indexFilePath).lastModified());
		} finally {
			deleteProject("P");
			new File(indexFilePath).delete();
			new File(jarFilePath).delete();
		}
	}

	// Test that it works if the index file is in the jar file
	public void testIndexInJar() throws IOException, CoreException {
		String indexFilePath = getExternalResourcePath("Test.index");
		String jarFilePath = getExternalResourcePath("Test.jar");
		String indexZipPath =  getExternalResourcePath("TestIndex.zip");
		try {
			createJar(new String[] {
					"pkg/Test.java",
					"package pkg;\n" +
					"public class Test {\n" +
					"  protected Test(int i) {}\n" +
					"}"}, jarFilePath);

			JavaIndexer.generateIndexForJar(jarFilePath, indexFilePath);
			Util.zipFiles(new File[]{new File(indexFilePath)}, indexZipPath);

			IJavaProject p = createJavaProject("P");
			Path libPath = new Path(jarFilePath);
			String url = "jar:file:"+indexZipPath+"!/Test.index";
			IClasspathAttribute attribute = JavaCore.newClasspathAttribute(IClasspathAttribute.INDEX_LOCATION_ATTRIBUTE_NAME, url);
			IClasspathEntry entry = JavaCore.newLibraryEntry(libPath, null, null, null, new IClasspathAttribute[]{attribute}, false);
			setClasspath(p, new IClasspathEntry[] {entry});
			waitUntilIndexesReady();

			IndexManager indexManager = JavaModelManager.getIndexManager();
			Index index = indexManager.getIndex(libPath, false, false);
			assertEquals(url, index.getIndexLocation().getUrl().toString());

			search("Test", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults(getExternalPath() + "Test.jar pkg.Test");

			simulateExitRestart();
			getJavaModel().refreshExternalArchives(null,null);
			waitUntilIndexesReady();

			this.resultCollector = new JavaSearchResultCollector();
			index = indexManager.getIndex(libPath, false, false);
			assertEquals(url, index.getIndexLocation().getUrl().toString());
			search("Test", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults(getExternalPath() + "Test.jar pkg.Test");
		} finally {
			deleteProject("P");
			new File(indexZipPath).delete();
			new File(jarFilePath).delete();
		}
	}

	// Test index file in platform
	public void testPlatformJarIndexFile() throws CoreException, IOException {
		String indexFilePath = null;
		String jarFilePath = getExternalResourcePath("Test.jar");
		String indexUrl = "platform:/resource/ForIndex/Test.index.zip!/Test.index";
		try {
			createJar(new String[] {
					"pkg/Test.java",
					"package pkg;\n" +
					"public class Test {\n" +
					"  protected Test(int i) {}\n" +
					"}"}, jarFilePath);

			IProject indexProj = createProject("ForIndex");
			indexFilePath = indexProj.getProject().getLocation().append("Test.index").toFile().getAbsolutePath();
			JavaIndexer.generateIndexForJar(jarFilePath, indexFilePath);
			Util.zipFiles(new File[]{new File(indexFilePath)}, indexFilePath+".zip");

			IJavaProject p = createJavaProject("P");
			Path libPath = new Path(jarFilePath);
			IClasspathAttribute attribute = JavaCore.newClasspathAttribute(IClasspathAttribute.INDEX_LOCATION_ATTRIBUTE_NAME, indexUrl);
			IClasspathEntry entry = JavaCore.newLibraryEntry(libPath, null, null, null, new IClasspathAttribute[]{attribute}, false);
			setClasspath(p, new IClasspathEntry[] {entry});
			waitUntilIndexesReady();

			search("Test", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults(getExternalPath() + "Test.jar pkg.Test");

			URL url = JavaModelManager.getIndexManager().getIndex(libPath, false, false).getIndexLocation().getUrl();
			assertEquals(indexUrl, url.toString());

			simulateExitRestart();
			getJavaModel().refreshExternalArchives(null,null);
			waitUntilIndexesReady();

			this.resultCollector = new JavaSearchResultCollector();
			search("Test", TYPE, DECLARATIONS, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaElement[]{p}));
			assertSearchResults(getExternalPath() + "Test.jar pkg.Test");

			url = JavaModelManager.getIndexManager().getIndex(libPath, false, false).getIndexLocation().getUrl();
			assertEquals(indexUrl, url.toString());
		} finally {
			deleteProject("P");
			if (indexFilePath != null) {
				new File(indexFilePath).delete();
				new File(indexFilePath+".zip").delete();
			}
			new File(jarFilePath).delete();
			deleteProject("ForIndex");
		}
	}

	// Test shared index location functionality
	public void testSharedIndexLocation() throws CoreException, IOException {
		// Create temporary testing folder
		String sharedIndexDir = Files.createTempDirectory("shared_index").toFile().getCanonicalPath();
		// enable shared index
		ClasspathEntry.setSharedIndexLocation(sharedIndexDir, getClass());
		// path of library must be platform neutral
		String jarFilePath = Path.fromOSString(Paths.get(sharedIndexDir, "Test.jar").toString()).toPortableString();
		// compute index file
		CRC32 checksumCalculator = new CRC32();
		checksumCalculator.update(jarFilePath.getBytes());
		String fileName = Long.toString(checksumCalculator.getValue()) + ".index";
		String indexFilePath = Paths.get(sharedIndexDir, DiskIndex.INDEX_VERSION, fileName).toString();
		try {
			createJar(new String[] {
					"pkg/Test.java",
					"package pkg;\n" +
					"public class Test {\n" +
					"  protected Test(int i) {}\n" +
					"}"}, jarFilePath);

			JavaIndexer.generateIndexForJar(jarFilePath, indexFilePath);
			assertTrue(new File(indexFilePath).exists());
			long modified = new File(indexFilePath).lastModified();

			IJavaProject p = createJavaProject("P");
			Path libPath = new Path(jarFilePath);
			IClasspathEntry entry = JavaCore.newLibraryEntry(libPath, null, null, null, null, false);
			setClasspath(p, new IClasspathEntry[] { entry });

			waitUntilIndexesReady();

			// Test that search works properly
			search("Test", TYPE, DECLARATIONS, EXACT_RULE,
					SearchEngine.createJavaSearchScope(new IJavaElement[] { p }));
			assertSearchResults(Paths.get(sharedIndexDir, "Test.jar").toString() + " pkg.Test");

			// Test that specified index file is really used
			java.io.File indexFile = JavaModelManager.getIndexManager().getIndex(libPath, false, false).getIndexFile();
			assertEquals("Specified index file is not being used", indexFilePath, indexFile.toString());

			// Ensure that the index file is not modified
			assertEquals(modified, new File(indexFilePath).lastModified());
		} finally {
			deleteProject("P");
			new File(indexFilePath).delete();
			new File(jarFilePath).delete();
			new File(sharedIndexDir).delete();
			ClasspathEntry.setSharedIndexLocation(null, getClass());
		}
	}
}
