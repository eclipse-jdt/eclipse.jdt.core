/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.File;

import junit.framework.Test;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.core.tests.model.JavaSearchTests.JavaSearchResultCollector;
import org.eclipse.jdt.core.tests.util.Util;

/**
 * Tests the search participant supprt.
 */
public class SearchParticipantTests extends ModifyingResourceTests implements IJavaSearchConstants {
	
	public class TestSearchParticipant extends SearchParticipant {
		
		class WrapperDocument  extends SearchDocument {
			
			private SearchDocument document;
			
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
				
		private SearchParticipant defaultSearchParticipant = SearchEngine.getDefaultSearchParticipant();
		
		public SearchDocument getDocument(String documentPath) {
			return new TestSearchDocument(documentPath, this);
		}

		public void indexDocument(SearchDocument document, IPath indexLocation) {
			((TestSearchDocument) document).indexingRequested = true;
			this.defaultSearchParticipant.indexDocument(new WrapperDocument(document, this), indexLocation);
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
		private String fileSystemPath;

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
		protected char[] getSource(IResource resource, IJavaElement element, ICompilationUnit unit) throws JavaModelException {
			String path = resource.getLocation().toFile().getPath().replaceAll(".java", ".test");
			String fileContent = Util.fileContent(path);
			if (fileContent == null) return null;
			return fileContent.toCharArray();
		}
		protected String getPathString(IResource resource, IJavaElement element) {
			return super.getPathString(resource, element).replaceAll(".java", ".test");
		}
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
		// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
	//	testsNames = new String[] { "testSearch"};
	//	testsNumbers = new int[] { 23, 28, 38 };
	//	testsRange = new int[] { 21, 38 };
	}
	
	public static Test suite() {
		return buildTestSuite(SearchParticipantTests.class);
	}
	
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		deleteFile(getIndexLocation().toFile());
	}
	
	protected void tearDown() throws Exception {
		deleteFile(getIndexLocation().toFile());
		super.tearDown();
	}
	
	public SearchParticipantTests(String name) {
		super(name);
	}

	IPath getIndexLocation() {
		return new Path(getExternalPath() + File.separator + "test.index");
	}
	
	/*
	 * Ensures that scheduleDocumentIndexing(...) triggers a call to indexDocument(...)
	 * (case of document existing on disk)
	 */
	public void testScheduleDocumentIndexing1() throws CoreException {
		try {
			createJavaProject("P");
			createFile("/P/X.test", "");
			TestSearchParticipant participant = new TestSearchParticipant();
			TestSearchDocument document = new TestSearchDocument("/P/X.test", participant);
			participant.scheduleDocumentIndexing(document, getIndexLocation());
			waitUntilIndexesReady();
			assertTrue("Should have requested to index document", document.indexingRequested);
		} finally {
			deleteProject("P");
		}
	}

	/*
	 * Ensures that scheduleDocumentIndexing(...) triggers a call to indexDocument(...)
	 * (case of document that doesn't exist on disk)
	 */
	public void testScheduleDocumentIndexing2() throws CoreException {
		try {
			createJavaProject("P");
			TestSearchParticipant participant = new TestSearchParticipant();
			TestSearchDocument document = new TestSearchDocument("/P/X.test", participant);
			participant.scheduleDocumentIndexing(document, getIndexLocation());
			waitUntilIndexesReady();
			assertTrue("Should have requested to index document", document.indexingRequested);
		} finally {
			deleteProject("P");
		}
	}
	
	/*
	 * Ensures that adding index entries through indexDocument() updates the index file.
	 * TODO (frederic) investigate why this test is failing
	 */
	public synchronized void _testIndexDocument() throws CoreException, InterruptedException {
		try {
			createJavaProject("P");
			TestSearchParticipant participant = new TestSearchParticipant(){
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
			assertTrue("Index file should have been written",  getIndexLocation().toFile().length() > 0);
		} finally {
			deleteProject("P");
		}
	}

	/*
	 * Ensures that a simple search that forwards queries to the default participant works as expected
	 * TODO (frederic) investigate why this test is failing
	 */
	public void _testSearch() throws CoreException {
		try {
			createJavaProject("P");
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
		} finally {
			deleteProject("P");
		}
	}
	
}