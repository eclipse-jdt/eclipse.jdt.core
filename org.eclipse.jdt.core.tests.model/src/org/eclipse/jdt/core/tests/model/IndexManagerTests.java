/*******************************************************************************
 * Copyright (c) 2021 Gayan Perera and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Gayan Perera - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.index.EntryResult;
import org.eclipse.jdt.internal.core.index.Index;
import org.eclipse.jdt.internal.core.index.MetaIndex;
import org.eclipse.jdt.internal.core.search.BasicSearchEngine;
import org.eclipse.jdt.internal.core.search.IRestrictedAccessTypeRequestor;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.search.indexing.ReadWriteMonitor;

import junit.framework.Test;

public class IndexManagerTests extends ModifyingResourceTests {
	private static final boolean SKIP_TESTS = Boolean.parseBoolean(System.getProperty("org.eclipse.jdt.disableMetaIndex", "false"));

	static {
//		TESTS_NAMES = new String[] { "testGH1203" };
	}

	private IJavaProject project;
	private IndexManager indexManager;

	public static Test suite() {
		return buildModelTestSuite(IndexManagerTests.class, BYTECODE_DECLARATION_ORDER);
	}

	public IndexManagerTests(String name) {
		super(name);
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.indexManager = JavaModelManager.getIndexManager();
	}

	@Override
	public void tearDownSuite() throws Exception {
		this.indexManager = null;
		super.tearDownSuite();
	}

	@Override
	protected void setUp() throws Exception {
		this.indexDisabledForTest = false;
		super.setUp();
		this.project = createJavaProject("IndexProject", new String[] { "src" }, new String[0], "bin", "1.8");
		addClasspathEntry(this.project, getJRTLibraryEntry());
	}

	@Override
	protected void tearDown() throws Exception {
		deleteProject(this.project);
		super.tearDown();
	}

	public void testAddingNewSourceFile_ShouldUpdate_MetaIndex() throws CoreException {
		if(SKIP_TESTS) return;

		Optional<Set<String>> indexNames = searchInMetaIndex("java.util.ArrayList");
		assertTrue("No meta index", indexNames.isPresent());
		int size = indexNames.get().size();

		createFile("/IndexProject/src/Q1.java", "public class Q1<E> extends java.util.ArrayList<E> {\n" + "}");
		indexNames = searchInMetaIndex("java.util.ArrayList");

		assertTrue("No meta index", indexNames.isPresent());
		assertEquals("Expected number of indexes are not found for ArrayList", size + 1, indexNames.get().size());
	}

	public void testAddingRemoveSourceFile_ShouldUpdate_MetaIndex() throws CoreException {
		if(SKIP_TESTS) return;

		createFile("/IndexProject/src/Q1.java", "public class Q1<E> extends java.util.ArrayList<E> {\n" + "}");

		Optional<Set<String>> indexNames = searchInMetaIndex("java.util.ArrayList");
		assertTrue("No meta index", indexNames.isPresent());
		int size = indexNames.get().size();

		deleteFile("/IndexProject/src/Q1.java");

		indexNames = searchInMetaIndex("java.util.ArrayList");

		assertTrue("No meta index", indexNames.isPresent());
		assertEquals("Expected number of indexes are not found for ArrayList", size - 1, indexNames.get().size());

	}

	public void testUpdateSourceFile_ShouldUpdate_MetaIndex() throws CoreException {
		if(SKIP_TESTS) return;

		Optional<Set<String>> indexNames = searchInMetaIndex("java.util.ArrayList");
		assertTrue("No meta index", indexNames.isPresent());
		int size = indexNames.get().size();

		createFile("/IndexProject/src/Q1.java", "public class Q1<E> {\n" + "}");
		waitUntilIndexesReady();

		changeFile("/IndexProject/src/Q1.java", "public class Q1<E> extends java.util.ArrayList<E> {\n" + "}");

		indexNames = searchInMetaIndex("java.util.ArrayList");

		assertTrue("No meta index", indexNames.isPresent());
		assertEquals("Expected number of indexes are not found for ArrayList", size + 1, indexNames.get().size());
	}

	public void testAddJarFile_ShouldUpdate_MetaIndex() throws CoreException {
		if(SKIP_TESTS) return;

		Optional<Set<String>> indexNames = searchInMetaIndex("binary.Deep");
		assertTrue("No meta index", indexNames.isPresent());

		addLibraryEntry(this.project,
				Paths.get(getSourceWorkspacePath(), "TypeHierarchy", "lib.jar").toFile().getAbsolutePath(), false);

		indexNames = searchInMetaIndex("binary.Deep");

		assertTrue("No meta index", indexNames.isPresent());
		assertEquals("Expected number of indexes are not found for binary.Deep", 1, indexNames.get().size());
	}

	public void testRemoveJarFile_ShouldUpdate_MetaIndex() throws CoreException {
		if(SKIP_TESTS) return;

		String jarPath = Paths.get(getSourceWorkspacePath(), "TypeHierarchy", "lib.jar").toFile().getAbsolutePath();
		addLibraryEntry(this.project, jarPath, false);

		Optional<Set<String>> indexNames = searchInMetaIndex("binary.Deep");
		assertTrue("No meta index", indexNames.isPresent());
		int size = indexNames.get().size();

		removeClasspathEntry(this.project, new Path(jarPath));

		indexNames = searchInMetaIndex("binary.Deep");

		assertTrue("No meta index", indexNames.isPresent());
		assertEquals("Expected number of indexes are not found for binary.Deep", size - 1, indexNames.get().size());
	}

	public void testSearchMetaIndex_ForSourceTypeDeclarations() throws CoreException {
		if (SKIP_TESTS)
			return;

		createFolder("/IndexProject/src/app");
		createFile("/IndexProject/src/app/Q1.java", "package app;\n public class Q1 {\n" + "}");

		Optional<Set<String>> indexNames = searchInMetaIndex("app.Q1");
		assertTrue("No meta index", indexNames.isPresent());
		assertEquals("No results found", 1, indexNames.get().size());
	}

	public void testGH1203() throws Exception {
		waitUntilIndexesReady();

		addLibrary(this.project, "lib.jar", "lib.zip",
				new String[] {
					"test/XY.java",
					"""
					package test;
					public class XY {}
					"""
				},
				"1.8");

		// Wait a little bit to be sure file system is aware of zip file creation
		try {
			Thread.sleep(1000);
		}
		catch (InterruptedException ie) {
			// skip
		}

		String packageFolder = "/" + this.project.getElementName() + "/src/test";
		createFolder(packageFolder);
		String source =
				"""
				package test;
				public class ZXX {
				}
				""";
		boolean indexDisabled = this.indexDisabledForTest;
		this.indexDisabledForTest = true; // avoid waitForIndex() call from ModifyingResourceTests.createFile(String, InputStream)
		createFile(packageFolder+"/ZXX.java", source);
		this.indexDisabledForTest = indexDisabled;
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getCompilationUnit(packageFolder+"/XX.java");

		IPath prjPath = this.project.getProject().getLocation();
		String jarPath = prjPath+"/lib.jar";
		createJar(new String[] {
			"test/ZXY.java",
			"package test;\n" +
			"public class ZXY {}\n",
			"test/ZXZ.java",
			"package test;\n" +
			"public class ZXZ {}\n",
		}, jarPath);
		this.project.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);

		try {
			Thread.sleep(1000);
		}
		catch (InterruptedException ie) {
			// skip
		}

		removeLibrary(this.project, "lib.jar", "lib.zip");

		Thread.sleep(2000);

		this.workingCopies[1] = getWorkingCopy(packageFolder+"/ZX0.java",
				"""
				package test;
				public class ZX0 {}
				""");
		searchTypesExpecting("ZX", new HashSet<>(Arrays.asList("ZXX", "ZX0")));
	}

	public void searchTypesExpecting(String namePrefix, Set<String> names) throws JavaModelException {
		IRestrictedAccessTypeRequestor typeRequestor = new IRestrictedAccessTypeRequestor() {
			@Override
			public void acceptType(int modifiers, char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames,
					String path, AccessRestriction access) {
				if (!names.remove(String.valueOf(simpleTypeName))) {
					fail("unexpected type "+String.valueOf(simpleTypeName));
				}
			}
		};
		try {
			new BasicSearchEngine(this.workingCopies).searchAllTypeNames(
				null,
				SearchPattern.R_EXACT_MATCH,
				namePrefix.toCharArray(),
				SearchPattern.R_PREFIX_MATCH,
				IJavaSearchConstants.TYPE,
				BasicSearchEngine.createJavaSearchScope(false, new IJavaElement[] {this.project}),
				false,
				typeRequestor,
				IJavaSearchConstants.CANCEL_IF_NOT_READY_TO_SEARCH,
				null);
		} catch (OperationCanceledException e) {
			e.printStackTrace();
		}
		if (!names.isEmpty()) {
			fail("Types not found: "+String.join(", ", names));
		}
	}

	private void changeFile(String path, String content) {
		IFile file = getFile(path);
		if (!file.exists()) {
			throw new AssertionError("File expected at path " + path);
		}

		try (ByteArrayInputStream stream = new ByteArrayInputStream(content.getBytes())) {
			file.setContents(stream, IResource.FORCE, new NullProgressMonitor());
		} catch (IOException | CoreException e) {
			e.printStackTrace();
			throw new AssertionError("Failed to update file " + e.getMessage());
		}
	}

	private Optional<Set<String>> searchInMetaIndex(String indexQualifier) {
		waitUntilIndexesReady();
		Optional<MetaIndex> index = null;
		try {
			index = this.indexManager.getMetaIndex();
			if (!index.isPresent()) {
				return Optional.empty();
			}

			ReadWriteMonitor monitor = index.get().getMonitor();
			if (monitor == null) {
				return Optional.empty();
			}
			try {
				monitor.enterRead();
				index.get().startQuery();
				List<EntryResult> results = new ArrayList<>(2);
				results.addAll(safeList(index.get()
						.query(new char[][] { IIndexConstants.META_INDEX_QUALIFIED_TYPE_QUALIFIER_REF,
								IIndexConstants.META_INDEX_QUALIFIED_SUPER_TYPE_QUALIFIER_REF }, indexQualifier.toCharArray(),
								SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE)));

				results.addAll(safeList(index.get()
						.query(new char[][] { IIndexConstants.META_INDEX_SIMPLE_SUPER_TYPE_QUALIFIER_REF,
								IIndexConstants.META_INDEX_SIMPLE_TYPE_QUALIFIER_REF },
								CharOperation.lastSegment(indexQualifier.toCharArray(), '.'),
								SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE)));

				final Index i = index.map(MetaIndex::getIndex).get();
				return Optional.of(results.stream().flatMap(r -> {
					try {
						return Stream.of(r.getDocumentNames(i));
					} catch (IOException e) {
						return Stream.empty();
					}
				}).collect(Collectors.toSet()));
			} finally {
				index.get().stopQuery();
				monitor.exitRead();
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new AssertionError(e.getMessage());
		}

	}

	private List<EntryResult> safeList(EntryResult[] result) {
		return result == null ? Collections.emptyList() : Arrays.asList(result);
	}
}
