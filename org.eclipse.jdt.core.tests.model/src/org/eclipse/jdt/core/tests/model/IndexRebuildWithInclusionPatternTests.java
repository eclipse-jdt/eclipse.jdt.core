/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.TreeSet;
import junit.framework.Test;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;

/**
 * Tests that an index that is rebuilt from an existing index file keeps the types of a source folder with an
 * inclusion pattern below it (for example <code>p/</code> or <code>p/*.java</code>), as a new index does.
 */
public class IndexRebuildWithInclusionPatternTests extends ModifyingResourceTests {

	private static final String PROJECT = "IdxInclusion";

	private static final Set<String> ALL_TYPES = Set.of("p.A", "p.B", "p.C");

	private IJavaProject project;

	private IndexManager indexManager;

	public static Test suite() {
		return buildModelTestSuite(IndexRebuildWithInclusionPatternTests.class, BYTECODE_DECLARATION_ORDER);
	}

	public IndexRebuildWithInclusionPatternTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		this.indexDisabledForTest = false;
		super.setUp();
		this.indexManager = JavaModelManager.getIndexManager();
		this.project = createJavaProject(PROJECT, new String[] { "src" }, new String[0], "bin");
		createFolder("/" + PROJECT + "/src/p");
		createFile("/" + PROJECT + "/src/p/A.java", "package p;\npublic class A {}\n");
		createFile("/" + PROJECT + "/src/p/B.java", "package p;\npublic class B {}\n");
		createFile("/" + PROJECT + "/src/p/C.java", "package p;\npublic class C {}\n");
		settleAndSave();
	}

	@Override
	protected void tearDown() throws Exception {
		enableIndexer();
		deleteProject(PROJECT);
		this.indexManager = null;
		super.tearDown();
	}

	/**
	 * A reuse rebuild (project open or import) must not drop the types of a source folder whose inclusion
	 * pattern is a folder below it.
	 */
	public void testReuseRebuild_folderInclusionPattern_keepsTypes() throws Exception {
		setSourceEntry(new IPath[] { new Path("p/") }, new IPath[0]);
		settleAndSave();
		assertTypes("index after the classpath change", ALL_TYPES);

		// Same job as the rebuild of an UNKNOWN index that has an index file.
		this.indexManager.indexAll(this.project.getProject());
		settleAndSave();

		assertTypes("index rebuilt from the existing index file", ALL_TYPES);

		simulateIndexManagerRestart();
		settle();
		assertTypes("same index after a restart", ALL_TYPES);
	}

	/**
	 * A queued edit that is discarded at the end of a session leaves the index in state UNKNOWN. The next session
	 * rebuilds it from the existing index file and must keep the types of a folder inclusion pattern.
	 */
	public void testRestartAfterQueuedEdit_folderInclusionPattern_keepsTypes() throws Exception {
		setSourceEntry(new IPath[] { new Path("p/") }, new IPath[0]);
		settleAndSave();
		assertTypes("index after the classpath change", ALL_TYPES);

		disableIndexer(); // keeps the edit job queued, so that it is still pending when the session ends
		changeFile("A.java", "package p;\npublic class A { int f; }\n"); // queued: the index state becomes UPDATING
		this.indexManager.discardJobs(PROJECT); // as JobManager.shutdown() does with pending jobs
		simulateIndexManagerRestart(); // only SAVED indexes are known after a restart: this one is UNKNOWN
		enableIndexer();
		settleAndSave(); // the first search rebuilds it from the existing file

		assertTypes("index rebuilt after a shutdown with a queued edit", ALL_TYPES);
	}

	/**
	 * A package that the folder inclusion pattern does not cover stays out of the index after a reuse rebuild, while
	 * the covered package stays in.
	 */
	public void testReuseRebuild_folderInclusionPattern_stillExcludesOtherPackage() throws Exception {
		createFolder("/" + PROJECT + "/src/q");
		createFile("/" + PROJECT + "/src/q/Q.java", "package q;\npublic class Q {}\n");
		setSourceEntry(new IPath[] { new Path("p/") }, new IPath[0]);
		settleAndSave();
		assertTypes("index after the classpath change", ALL_TYPES);
		assertTypesAbsent("index after the classpath change", Set.of("q.Q"));

		this.indexManager.indexAll(this.project.getProject());
		settleAndSave();

		assertTypes("index rebuilt from the existing index file", ALL_TYPES);
		assertTypesAbsent("index rebuilt from the existing index file", Set.of("q.Q"));
	}

	/**
	 * Control: exclusion patterns take the same folder rule in the fresh and in the reuse walk.
	 */
	public void testReuseRebuild_exclusionPattern_keepsExcludingFolder() throws Exception {
		createFolder("/" + PROJECT + "/src/q");
		createFile("/" + PROJECT + "/src/q/Q.java", "package q;\npublic class Q {}\n");
		setSourceEntry(new IPath[0], new IPath[] { new Path("q/") });
		settleAndSave();
		assertTypes("index after the classpath change", ALL_TYPES);
		assertTypesAbsent("index after the classpath change", Set.of("q.Q"));

		this.indexManager.indexAll(this.project.getProject());
		settleAndSave();

		assertTypes("index rebuilt from the existing index file", ALL_TYPES);
		assertTypesAbsent("index rebuilt from the existing index file", Set.of("q.Q"));
	}

	/**
	 * A reuse rebuild must also keep the types of a source folder whose inclusion pattern is a file pattern below it.
	 */
	public void testReuseRebuild_fileInclusionPatternBelowSourceFolder_keepsTypes() throws Exception {
		setSourceEntry(new IPath[] { new Path("p/*.java") }, new IPath[0]);
		settleAndSave();
		assertTypes("index after the classpath change", ALL_TYPES);

		this.indexManager.indexAll(this.project.getProject());
		settleAndSave();

		assertTypes("index rebuilt from the existing index file", ALL_TYPES);
	}

	/**
	 * Control: the inclusion pattern <code>**&#47;*.java</code> matches the source folder itself and always worked.
	 */
	public void testReuseRebuild_fileInclusionPattern_keepsTypes() throws Exception {
		setSourceEntry(new IPath[] { new Path("**/*.java") }, new IPath[0]);
		settleAndSave();
		assertTypes("index after the classpath change", ALL_TYPES);

		this.indexManager.indexAll(this.project.getProject());
		settleAndSave();

		assertTypes("index rebuilt from the existing index file", ALL_TYPES);
	}

	// --- helpers ---

	private void setSourceEntry(IPath[] inclusionPatterns, IPath[] exclusionPatterns) throws CoreException {
		IClasspathEntry source = JavaCore.newSourceEntry(new Path("/" + PROJECT + "/src"), inclusionPatterns,
				exclusionPatterns, null);
		this.project.setRawClasspath(new IClasspathEntry[] { source }, new Path("/" + PROJECT + "/bin"), null);
	}

	private void changeFile(String name, String content) throws CoreException {
		IFile file = getFile("/" + PROJECT + "/src/p/" + name);
		file.setContents(content.getBytes(), IResource.FORCE, new NullProgressMonitor());
	}

	/** Waits until the indexer is idle. Twice, since the first search may only request a rebuild. */
	private void settle() {
		waitUntilIndexesReady();
		this.indexManager.waitForIndex(true, null);
		waitUntilIndexesReady();
	}

	private void settleAndSave() {
		settle();
		this.indexManager.saveIndexes();
	}

	/**
	 * Drops all cached indexes and reads the index states from disk, as a restart does. The queue is drained first
	 * without a search (a search would rebuild an index in state UNKNOWN). The pending write of
	 * {@code savedIndexNames.txt} is awaited, since a read while a write is pending (which a real start never sees)
	 * drops the first index name.
	 */
	private void simulateIndexManagerRestart() throws Exception {
		this.indexManager.waitForIndex(true, null);
		assertEquals("no queued index job expected before the restart: " + this.indexManager, 0,
				this.indexManager.awaitingJobsCount());
		Object nameRegistry = field(this.indexManager, "nameRegistry");
		Job writeJob = (Job) field(nameRegistry, "writeJob");
		// join() alone is not enough: it returns at once while the write job is not scheduled yet. The wait ends as
		// soon as the write is done (a condition, not a fixed delay); the loop bound only avoids a hang on a defect.
		for (int i = 0; i < 200 && field(nameRegistry, "pendingWrite") != null; i++) {
			writeJob.join(50, null);
		}
		assertNull("pending write of the saved index names", field(nameRegistry, "pendingWrite"));
		this.indexManager.reset();
	}

	private static Object field(Object target, String name) throws Exception {
		Class<?> type = target.getClass();
		while (type != null) {
			try {
				Field field = type.getDeclaredField(name);
				field.setAccessible(true);
				return field.get(target);
			} catch (NoSuchFieldException e) {
				type = type.getSuperclass();
			}
		}
		throw new NoSuchFieldException(name);
	}

	private Set<String> findTypes() throws CoreException {
		Set<String> found = new TreeSet<>();
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { this.project },
				IJavaSearchScope.SOURCES);
		new SearchEngine().searchAllTypeNames(null, SearchPattern.R_PATTERN_MATCH, "*".toCharArray(),
				SearchPattern.R_PATTERN_MATCH, IJavaSearchConstants.TYPE, scope, new TypeNameRequestor() {
					@Override
					public void acceptType(int modifiers, char[] packageName, char[] simpleTypeName,
							char[][] enclosingTypeNames, String path) {
						found.add(new String(packageName) + "." + new String(simpleTypeName));
					}
				}, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
		return found;
	}

	private void assertTypesAbsent(String situation, Set<String> unexpected) throws CoreException {
		Set<String> found = findTypes();
		Set<String> present = new TreeSet<>(unexpected);
		present.retainAll(found);
		assertTrue(situation + ": types that must not be indexed: " + present + " (found " + found + ")",
				present.isEmpty());
	}

	private void assertTypes(String situation, Set<String> expected) throws CoreException {
		Set<String> found = findTypes();
		Set<String> missing = new TreeSet<>(expected);
		missing.removeAll(found);
		assertTrue(situation + ": types missing from the index: " + missing + " (found " + found + ")",
				missing.isEmpty());
	}
}
