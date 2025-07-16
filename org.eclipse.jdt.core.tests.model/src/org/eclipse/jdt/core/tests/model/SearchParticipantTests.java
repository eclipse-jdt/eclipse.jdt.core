/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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

import junit.framework.Test;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchDocument;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.tests.model.AbstractJavaSearchTests.JavaSearchResultCollector;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.search.indexing.SourceIndexer;
import org.eclipse.jdt.internal.core.search.processing.JobManager;

/**
 * Tests the search participant supprt.
 */
public class SearchParticipantTests extends ModifyingResourceTests implements IJavaSearchConstants {

	IJavaProject project;
	boolean deleteProject = true;

	public class TestSearchParticipant extends SearchParticipant {

		class WrapperDocument  extends SearchDocument {

			private final SearchDocument document;

			WrapperDocument(SearchDocument document, SearchParticipant participant) {
				super(document.getPath().replaceAll(".test", ".java"), participant);
				this.document = document;
			}

			public byte[] getByteContents() {
				return this.document.getByteContents();
			}

			public char[] getCharContents() {
				return this.document.getCharContents();
			}

			public String getEncoding() {
				return this.document.getEncoding();
			}
		}

		private final SearchParticipant defaultSearchParticipant = SearchEngine.getDefaultSearchParticipant();

		public SearchDocument getDocument(String documentPath) {
			return new TestSearchDocument(documentPath, this);
		}

		public void indexDocument(SearchDocument document, IPath indexLocation) {
			((TestSearchDocument) document).indexingRequested = true;
			document.removeAllIndexEntries();
			String documentPath = document.getPath();
			if (documentPath.endsWith(".test")) {
				new SourceIndexer(document).indexDocument();
			}
		}

		public void locateMatches(SearchDocument[] documents, SearchPattern pattern, IJavaSearchScope scope, SearchRequestor requestor, IProgressMonitor monitor) throws CoreException {
			int length = documents.length;
			SearchDocument[] wrapperDocuments = new SearchDocument[length];
			for (int i = 0; i < length; i++) {
				wrapperDocuments[i] = new WrapperDocument(documents[i], this);
			}
			this.defaultSearchParticipant.locateMatches(wrapperDocuments, pattern, scope, requestor, monitor);
		}

		public IPath[] selectIndexes(SearchPattern query, IJavaSearchScope scope) {
			return new IPath[] {getIndexLocation()};
		}
	}

	public class TestSearchDocument extends SearchDocument {

		public boolean indexingRequested;
		private final String fileSystemPath;

		protected TestSearchDocument(String documentPath, SearchParticipant participant) {
			super(documentPath, participant);
			this.fileSystemPath = getWorkspaceRoot().getFile(new Path(documentPath)).getLocation().toOSString();
		}

		public byte[] getByteContents() {
			String fileContent = Util.fileContent(this.fileSystemPath);
			if (fileContent == null) return null;
			return fileContent.getBytes();
		}

		public char[] getCharContents() {
			String fileContent = Util.fileContent(this.fileSystemPath);
			if (fileContent == null) return null;
			return fileContent.toCharArray();
		}

		public String getEncoding() {
			return null;
		}
	}

	public class TestResultCollector extends JavaSearchResultCollector {
		@Override
		protected char[] getSource(IResource resource, IJavaElement element, ICompilationUnit unit) throws JavaModelException {
			IPath path = resource.getLocation().removeFileExtension().addFileExtension("test");
			String fileContent = Util.fileContent(path.toFile().getPath());
			if (fileContent == null) return null;
			return fileContent.toCharArray();
		}
		@Override
		protected String getPathString(IResource resource, IJavaElement element) {
			return super.getPathString(resource, element).replaceAll(".java", ".test");
		}
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "testSearch"};
	//	TESTS_NUMBERS = new int[] { 23, 28, 38 };
	//	TESTS_RANGE = new int[] { 21, 38 };
	}

	public static Test suite() {
		return buildModelTestSuite(SearchParticipantTests.class);
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		deleteResource(getIndexLocation().toFile());
	}

	@Override
	public void tearDownSuite() throws Exception {
		super.tearDownSuite();
		if (this.project != null) {
			deleteProject("P");
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (this.project == null) {
			this.project = createJavaProject("P");
		}
		this.deleteProject = true;
		if (JobManager.VERBOSE) {
			System.out.println("--------------------------------------------------------------------------------");
			System.out.println("Run test "+getName()+"...");
		}
	}

	@Override
	protected void tearDown() throws Exception {
		// Do not delete specific index file between tests as corresponding still lives in IndexManager cache
		// TODO (frederic) Uncomment when bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=116650 will be fixed
		//deleteFile(getIndexLocation().toFile());
		if (this.deleteProject) {
			deleteProject("P");
			this.project = null;
		}
		super.tearDown();
	}

	public SearchParticipantTests(String name) {
		super(name);
	}

	IPath getIndexLocation() {
		// use a non-canonical path to show that path does not need to be canonical
		return new Path(getExternalPath() + "/dummy/../test.index");
	}

	/*
	 * Ensures that scheduleDocumentIndexing(...) triggers a call to indexDocument(...)
	 * (case of document existing on disk)
	 */
	public void testScheduleDocumentIndexing1() throws CoreException {
		createFile("/P/X.test", "");
		TestSearchParticipant participant = new TestSearchParticipant();
		TestSearchDocument document = new TestSearchDocument("/P/X.test", participant);
		participant.scheduleDocumentIndexing(document, getIndexLocation());
		waitUntilIndexesReady();
		assertTrue("Should have requested to index document", document.indexingRequested);
	}

	/*
	 * Ensures that scheduleDocumentIndexing(...) triggers a call to indexDocument(...)
	 * (case of document that doesn't exist on disk)
	 */
	public void testScheduleDocumentIndexing2() throws CoreException {
		TestSearchParticipant participant = new TestSearchParticipant();
		TestSearchDocument document = new TestSearchDocument("/P/X.test", participant);
		participant.scheduleDocumentIndexing(document, getIndexLocation());
		waitUntilIndexesReady();
		assertTrue("Should have requested to index document", document.indexingRequested);
	}

	/*
	 * Ensures that adding index entries through indexDocument() updates the index file.
	 */
	public synchronized void testIndexDocument01() throws CoreException, InterruptedException {
		createFile(
			"/P/X.test",
			"public class X {\n" +
			"}"
		);
		TestSearchParticipant participant = new TestSearchParticipant();
		TestSearchDocument document = new TestSearchDocument("/P/X.test", participant);
		participant.scheduleDocumentIndexing(document, getIndexLocation());
		waitUntilIndexesReady();
		wait(1100); // wait more than 1000ms so as to allow for the index to be saved on disk
		document = new TestSearchDocument("/P/no", participant);
		participant.scheduleDocumentIndexing(document, getIndexLocation());
		waitUntilIndexesReady();
		assertTrue("Index file should have been written",  getIndexLocation().toFile().length() > 0);

		// remove index file
		participant.removeIndex(getIndexLocation());
		assertFalse("Index file should have been removed",  getIndexLocation().toFile().exists());
	}

	/*
	 * Ensures that adding index entries through indexDocument() updates the index file
	 * and that exit session keeps it.
	 */
	public synchronized void testIndexDocument02() throws CoreException, InterruptedException {
		TestSearchParticipant participant = new TestSearchParticipant(){
			@Override
			public void indexDocument(SearchDocument document, IPath indexLocation) {
				if (!document.getPath().equals("/P/no")) {
					for (int i = 0; i < 1000; i++) {
						document.addIndexEntry(("category" + i).toCharArray(), ("key" + i).toCharArray());
					}
				}
			}
		};
		TestSearchDocument document = new TestSearchDocument("/P/X.test", participant);
		participant.scheduleDocumentIndexing(document, getIndexLocation());
		waitUntilIndexesReady();
		wait(1100); // wait more than 1000ms so as to allow for the index to be saved on disk
		document = new TestSearchDocument("/P/no", participant);
		participant.scheduleDocumentIndexing(document, getIndexLocation());
		waitUntilIndexesReady();
		wait(1100); // wait more than 1000ms so as to allow for the index to be saved on disk
		assertTrue("Index file should have been written",  getIndexLocation().toFile().length() > 0);

		// shutdown
		simulateExit();
		assertTrue("Index file should stay after shutdown",  getIndexLocation().toFile().length() > 0);
		this.deleteProject = false;

		// remove index file
		participant.removeIndex(getIndexLocation());
		assertFalse("Index file should have been removed",  getIndexLocation().toFile().exists());

		// restart
		simulateRestart();
	}

	/*
	 * Ensures that adding index entries through indexDocument() updates the index file.
	 */
	public synchronized void testIndexDocument03() throws CoreException, InterruptedException {
		createFile(
			"/P/X.test",
			"public class X {\n" +
			"}"
		);
		TestSearchParticipant participant = new TestSearchParticipant();
		TestSearchDocument document = new TestSearchDocument("/P/X.test", participant);
		participant.scheduleDocumentIndexing(document, getIndexLocation());
		waitUntilIndexesReady();
		wait(1100); // wait more than 1000ms so as to allow for the index to be saved on disk
		document = new TestSearchDocument("/P/no", participant);
		participant.scheduleDocumentIndexing(document, getIndexLocation());
		waitUntilIndexesReady();
		assertTrue("Index file should have been written",  getIndexLocation().toFile().length() > 0);

		// remove index file
		participant.removeIndex(getIndexLocation());
		assertFalse("Index file should have been removed",  getIndexLocation().toFile().exists());
	}

	/*
	 * Ensures that a simple search that forwards queries to the default participant works as expected
	 */
	public void testSearch() throws CoreException {
		createFile(
			"/P/X.test",
			"public class X {\n" +
			"}"
		);

		// index file
		TestSearchParticipant participant = new TestSearchParticipant();
		TestSearchDocument document = new TestSearchDocument("/P/X.test", participant);
		participant.scheduleDocumentIndexing(document, getIndexLocation());
		waitUntilIndexesReady();

		// search for declaration of X
		SearchPattern pattern = SearchPattern.createPattern("X", IJavaSearchConstants.DECLARATIONS, IJavaSearchConstants.TYPE, SearchPattern.R_EXACT_MATCH);
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		SearchRequestor requestor =  new TestResultCollector();
		new SearchEngine().search(pattern, new SearchParticipant[] {participant}, scope, requestor, null);
		assertSearchResults(
			"X.test X [X]",
			requestor);
	}

	/*
	 * Ensures that a simple search that forwards queries to the default participant works as expected even after restart
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=308402
	 */
	public void testSearchAfterRestart() throws CoreException {
		createFile(
			"/P/X.test",
			"public class X {\n" +
			"}"
		);

		// index file
		TestSearchParticipant participant = new TestSearchParticipant();
		TestSearchDocument document = new TestSearchDocument("/P/X.test", participant);
		participant.scheduleDocumentIndexing(document, getIndexLocation());
		waitUntilIndexesReady();
		try {
			Thread.sleep(5000); // wait for the indexes to go into the disk
		} catch (InterruptedException e) {
			// ignore
		}
		simulateExit();
		simulateRestart();
		waitUntilIndexesReady();

		// search for declaration of X
		SearchPattern pattern = SearchPattern.createPattern("X", IJavaSearchConstants.DECLARATIONS, IJavaSearchConstants.TYPE, SearchPattern.R_EXACT_MATCH);
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		SearchRequestor requestor =  new TestResultCollector();
		new SearchEngine().search(pattern, new SearchParticipant[] {participant}, scope, requestor, null);
		assertSearchResults(
			"X.test X [X]",
			requestor);

	}

	/*
	 * Ensures that a simple search that forwards queries to the default participant works as expected even after restart
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=308402
	 */
	public void testSearchAfterRemoveAndRestart() throws CoreException {
		createFile(
			"/P/X.test",
			"public class X {\n" +
			"}"
		);

		// index file
		TestSearchParticipant participant = new TestSearchParticipant();
		TestSearchDocument document = new TestSearchDocument("/P/X.test", participant);
		participant.scheduleDocumentIndexing(document, getIndexLocation());
		waitUntilIndexesReady();

		// search for declaration of X
		SearchPattern pattern = SearchPattern.createPattern("X", IJavaSearchConstants.DECLARATIONS, IJavaSearchConstants.TYPE, SearchPattern.R_EXACT_MATCH);
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		SearchRequestor requestor =  new TestResultCollector();
		new SearchEngine().search(pattern, new SearchParticipant[] {participant}, scope, requestor, null);

		// remove the index
		participant.removeIndex(getIndexLocation());
		assertSearchResults(
			"X.test X [X]",
			requestor);
		try {
			Thread.sleep(5000); // wait for the indexes to go into the disk
		} catch (InterruptedException e) {
			// ignore
		}
		requestor =  new TestResultCollector();
		new SearchEngine().search(pattern, new SearchParticipant[] {participant}, scope, requestor, null);
		assertSearchResults("", requestor);

		simulateExit();
		simulateRestart();
		waitUntilIndexesReady();
		requestor =  new TestResultCollector();
		new SearchEngine().search(pattern, new SearchParticipant[] {participant}, scope, requestor, null);
		assertSearchResults("", requestor);
	}
}
