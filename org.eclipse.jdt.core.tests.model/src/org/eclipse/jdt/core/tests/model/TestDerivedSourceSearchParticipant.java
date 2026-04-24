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

import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchDocument;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.search.indexing.SourceIndexer;

/**
 * A test search participant for {@code .langx} files. Delegates indexing to
 * the default Java source indexer (treating {@code .langx} content as Java syntax
 * for testing purposes).
 */
public class TestDerivedSourceSearchParticipant extends SearchParticipant {

	public static final AtomicInteger indexDocumentCallCount = new AtomicInteger(0);
	public static final AtomicInteger instanceCount = new AtomicInteger(0);

	public TestDerivedSourceSearchParticipant() {
		instanceCount.incrementAndGet();
	}

	@Override
	public SearchDocument getDocument(String documentPath) {
		return new TestDerivedSearchDocument(documentPath, this);
	}

	@Override
	public void indexDocument(SearchDocument document, IPath indexLocation) {
		indexDocumentCallCount.incrementAndGet();
		document.removeAllIndexEntries();
		// delegate to the Java source indexer for testing
		new SourceIndexer(document).indexDocument();
	}

	@Override
	public void locateMatches(SearchDocument[] documents, SearchPattern pattern,
			IJavaSearchScope scope, SearchRequestor requestor,
			IProgressMonitor monitor) throws CoreException {
		// no-op for now — index population is what we test
	}

	@Override
	public IPath[] selectIndexes(SearchPattern query, IJavaSearchScope scope) {
		return new IPath[0];
	}

	public static void reset() {
		indexDocumentCallCount.set(0);
		instanceCount.set(0);
	}
}
