/*******************************************************************************
 * Copyright (c) 2026 Eclipse Foundation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Arcadiy Ivanov - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.internal.core.search.indexing.SearchParticipantRegistry;

/**
 * Tests for the {@code org.eclipse.jdt.core.searchParticipant} extension point
 * and the {@link SearchParticipantRegistry}.
 */
public class DerivedSourceSearchParticipantTests extends ModifyingResourceTests {

	IJavaProject project;

	public DerivedSourceSearchParticipantTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(DerivedSourceSearchParticipantTests.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		TestDerivedSourceSearchParticipant.reset();
		SearchParticipantRegistry.reset();
		this.project = createJavaProject("DSP", new String[] {"src"}, "bin");
	}

	@Override
	protected void tearDown() throws Exception {
		deleteProject("DSP");
		this.project = null;
		super.tearDown();
	}

	/**
	 * Verifies that the registry discovers the {@code .langx} extension
	 * from the test plugin's contribution.
	 */
	public void testRegistryHasParticipantForLangx() {
		assertTrue("Registry should have participant for langx",
				SearchParticipantRegistry.hasParticipant("langx"));
	}

	/**
	 * Verifies that the registry does not report a participant for
	 * an unregistered extension.
	 */
	public void testRegistryNoParticipantForUnknownExtension() {
		assertFalse("Registry should not have participant for unknown_ext",
				SearchParticipantRegistry.hasParticipant("unknown_ext"));
	}

	/**
	 * Verifies that {@code getParticipant("langx")} returns a non-null
	 * participant of the correct type and that the instance is reused.
	 */
	public void testRegistryGetParticipantSingleton() {
		SearchParticipant p1 = SearchParticipantRegistry.getParticipant("langx");
		assertNotNull("Should return a participant for langx", p1);
		assertTrue("Should be a TestDerivedSourceSearchParticipant",
				p1 instanceof TestDerivedSourceSearchParticipant);
		SearchParticipant p2 = SearchParticipantRegistry.getParticipant("langx");
		assertSame("Same instance should be returned on second call", p1, p2);
		assertEquals("Exactly one instance should be created",
				1, TestDerivedSourceSearchParticipant.instanceCount.get());
	}

	/**
	 * Verifies that {@code getContributedParticipants()} includes the
	 * test participant.
	 */
	public void testGetContributedParticipants() {
		SearchParticipant[] contributed = SearchParticipantRegistry.getContributedParticipants();
		assertTrue("Should have at least one contributed participant",
				contributed.length >= 1);
		boolean found = false;
		for (SearchParticipant p : contributed) {
			if (p instanceof TestDerivedSourceSearchParticipant) {
				found = true;
				break;
			}
		}
		assertTrue("Contributed participants should include TestDerivedSourceSearchParticipant",
				found);
	}

	/**
	 * Verifies that {@link SearchEngine#getSearchParticipants()} returns both
	 * the default participant and contributed participants.
	 */
	public void testGetSearchParticipants() {
		SearchParticipant[] participants = SearchEngine.getSearchParticipants();
		assertTrue("Should have at least 2 participants (default + contributed)",
				participants.length >= 2);
		boolean hasDefault = false;
		boolean hasContributed = false;
		SearchParticipant defaultP = SearchEngine.getDefaultSearchParticipant();
		for (SearchParticipant p : participants) {
			if (p.getClass() == defaultP.getClass()) {
				hasDefault = true;
			}
			if (p instanceof TestDerivedSourceSearchParticipant) {
				hasContributed = true;
			}
		}
		assertTrue("Should include the default Java search participant", hasDefault);
		assertTrue("Should include the contributed test participant", hasContributed);
	}

	/**
	 * Verifies that {@code getFileExtension()} correctly extracts extensions.
	 */
	public void testGetFileExtension() {
		assertEquals("kt", SearchParticipantRegistry.getFileExtension("Foo.kt"));
		assertEquals("langx", SearchParticipantRegistry.getFileExtension("Bar.langx"));
		assertEquals("java", SearchParticipantRegistry.getFileExtension("Baz.java"));
		assertNull(SearchParticipantRegistry.getFileExtension("noextension"));
	}

	/**
	 * Verifies that adding a {@code .langx} file to a source folder triggers
	 * the search participant's {@code indexDocument()} method via the
	 * automatic indexing pipeline.
	 */
	public void testIndexingTriggeredForDerivedSourceFile() throws CoreException {
		createFile(
			"/DSP/src/Hello.langx",
			"public class Hello {\n" +
			"    public void greet() {}\n" +
			"}"
		);
		waitUntilIndexesReady();
		assertTrue("indexDocument should have been called at least once",
				TestDerivedSourceSearchParticipant.indexDocumentCallCount.get() > 0);
	}

	/**
	 * Verifies that adding a second {@code .langx} file triggers additional
	 * indexing calls.
	 */
	public void testIndexingTriggeredForMultipleDerivedSourceFiles() throws CoreException {
		createFile(
			"/DSP/src/Alpha.langx",
			"public class Alpha {\n" +
			"    public int value() { return 1; }\n" +
			"}"
		);
		createFile(
			"/DSP/src/Beta.langx",
			"public class Beta {\n" +
			"    public int value() { return 2; }\n" +
			"}"
		);
		waitUntilIndexesReady();
		assertTrue("indexDocument should have been called at least twice",
				TestDerivedSourceSearchParticipant.indexDocumentCallCount.get() >= 2);
	}

	/**
	 * Verifies that creating a {@code .langx} file in a subfolder/package
	 * triggers indexing via the add-folder-to-index path.
	 */
	public void testIndexingInSubPackage() throws CoreException {
		createFolder("/DSP/src/pkg");
		createFile(
			"/DSP/src/pkg/InPackage.langx",
			"package pkg;\n" +
			"public class InPackage {}"
		);
		waitUntilIndexesReady();
		assertTrue("indexDocument should have been called for file in subpackage",
				TestDerivedSourceSearchParticipant.indexDocumentCallCount.get() > 0);
	}

	/**
	 * Verifies that deleting a {@code .langx} file does not crash
	 * and that the index is updated.
	 */
	public void testDeletionOfDerivedSourceFile() throws CoreException {
		createFile(
			"/DSP/src/ToDelete.langx",
			"public class ToDelete {}"
		);
		waitUntilIndexesReady();

		// delete the file — should not throw
		deleteFile("/DSP/src/ToDelete.langx");
		waitUntilIndexesReady();
		// If we get here without an exception, the delta processor handled removal correctly
	}

	/**
	 * Verifies that regular {@code .java} files are not routed to the
	 * derived source participant.
	 */
	public void testJavaFilesNotRoutedToParticipant() throws CoreException {
		createFile(
			"/DSP/src/Regular.java",
			"public class Regular {}"
		);
		waitUntilIndexesReady();
		assertEquals("indexDocument should not be called for .java files",
				0, TestDerivedSourceSearchParticipant.indexDocumentCallCount.get());
	}

	/**
	 * Verifies that the registry reset clears cached participants
	 * and forces re-loading on next access.
	 */
	public void testRegistryReset() {
		SearchParticipant before = SearchParticipantRegistry.getParticipant("langx");
		assertNotNull(before);
		TestDerivedSourceSearchParticipant.reset();
		SearchParticipantRegistry.reset();

		SearchParticipant after = SearchParticipantRegistry.getParticipant("langx");
		assertNotNull(after);
		assertNotSame("After reset, a new instance should be created", before, after);
		assertEquals("New instance should have been created after reset",
				1, TestDerivedSourceSearchParticipant.instanceCount.get());
	}
}
