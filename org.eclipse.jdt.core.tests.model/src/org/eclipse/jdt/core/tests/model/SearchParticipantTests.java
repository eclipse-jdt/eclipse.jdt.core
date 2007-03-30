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

import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.core.tests.util.Util;

/**
 * Tests the search participant supprt.
 */
public class SearchParticipantTests extends ModifyingResourceTests implements IJavaSearchConstants {
	
	public class TestSearchParticipant extends SearchParticipant {
		
		public SearchDocument getDocument(String documentPath) {
			return new TestSearchDocument(documentPath, this);
		}

		public void indexDocument(SearchDocument document, IPath indexLocation) {
			((TestSearchDocument) document).indexingRequested = true;
		}

		public void locateMatches(SearchDocument[] documents, SearchPattern pattern, IJavaSearchScope scope, SearchRequestor requestor, IProgressMonitor monitor) throws CoreException {
		}

		public IPath[] selectIndexes(SearchPattern query, IJavaSearchScope scope) {
			return null;
		}
	}
	
	public class TestSearchDocument extends SearchDocument {

		public boolean indexingRequested;

		protected TestSearchDocument(String documentPath, SearchParticipant participant) {
			super(documentPath, participant);
		}

		public byte[] getByteContents() {
			return null;
		}

		public char[] getCharContents() {
			return null;
		}

		public String getEncoding() {
			return null;
		}
	}

	public SearchParticipantTests(String name) {
		super(name);
	}

	public static Test suite() {
		if (false) {
			Suite suite = new Suite(SearchParticipantTests.class.getName());
			suite.addTest(new SearchParticipantTests("test..."));
			return suite;
		}
		return new Suite(SearchParticipantTests.class);
	}
	
	private IPath getIndexLocation() {
		return new Path(EXTERNAL_JAR_DIR_PATH + File.separator + "test.index");
	}

	/**
	 * scheduleDocumentIndexing(...) should trigger a call to indexDocument(...)
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
			Util.delete(getIndexLocation().toFile());
		}
	}

	/**
	 * scheduleDocumentIndexing(...) should trigger a call to indexDocument(...)
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
			Util.delete(getIndexLocation().toFile());
		}
	}

}