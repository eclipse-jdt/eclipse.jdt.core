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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import junit.framework.Test;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.index.EntryResult;
import org.eclipse.jdt.internal.core.index.Index;
import org.eclipse.jdt.internal.core.index.MetaIndex;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.search.indexing.ReadWriteMonitor;

public class IndexManagerTests extends ModifyingResourceTests {
	private static final boolean SKIP_TESTS = Boolean.parseBoolean(System.getProperty("org.eclipse.jdt.disableMetaIndex", "false"));

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
		this.project = createJavaProject("IndexProject", new String[] { "src" }, new String[0], "bin", CompilerOptions.getFirstSupportedJavaVersion());
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

	private void changeFile(String path, String content) {
		IFile file = getFile(path);
		if (!file.exists()) {
			throw new AssertionError("File expected at path " + path);
		}

		try {
			file.setContents(content.getBytes(), IResource.FORCE, new NullProgressMonitor());
		} catch (CoreException e) {
			throw new AssertionError("Failed to update file " + e.getMessage(), e);
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
