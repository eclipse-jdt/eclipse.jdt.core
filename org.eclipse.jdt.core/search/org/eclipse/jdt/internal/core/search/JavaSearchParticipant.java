/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchDocument;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.search.indexing.BinaryIndexer;
import org.eclipse.jdt.internal.core.search.indexing.SourceIndexer;
import org.eclipse.jdt.internal.core.search.matching.MatchLocator;
import org.eclipse.jdt.core.search.SearchParticipant;

/**
 * A search participant describes a particular extension to a generic search mechanism, allowing thus to 
 * perform combined search actions which will involve all required participants
 * 
 * A search scope defines which participants are involved. 
 * 
 * A search participant is responsible for holding index files, and selecting the appropriate ones to feed to
 * index queries. It also can map a document path to an actual document (note that documents could live outside
 * the workspace or no exist yet, and thus aren't just resources).
 */
public class JavaSearchParticipant extends SearchParticipant {
	
	public class WorkingCopyDocument extends JavaSearchDocument {
		public ICompilationUnit workingCopy;
		WorkingCopyDocument(ICompilationUnit workingCopy) {
			super(workingCopy.getPath().toString(), JavaSearchParticipant.this);
			this.charContents = ((CompilationUnit)workingCopy).getContents();
			this.workingCopy = workingCopy;
		}
		public String toString() {
			return "WorkingCopyDocument for " + getPath(); //$NON-NLS-1$
		}
	}
	
	IndexSelector indexSelector;
	
	/*
	 * A table from path (String) to working copy document (WorkingCopopyDocument)
	 */
	ICompilationUnit[] workingCopies;
	
	public JavaSearchParticipant(ICompilationUnit[] workingCopies) {
		this.workingCopies = workingCopies;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.search.SearchParticipant#beginSearching()
	 */
	public void beginSearching() {
		this.indexSelector = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.search.SearchParticipant#doneSearching()
	 */
	public void doneSearching() {
		this.indexSelector = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.search.SearchParticipant#getName()
	 */
	public String getDescription() {
		return "Java"; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.search.SearchParticipant#getDocument(String)
	 */
	public SearchDocument getDocument(String documentPath) {
		return new JavaSearchDocument(documentPath, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.search.SearchParticipant#indexDocument(SearchDocument)
	 */
	public void indexDocument(SearchDocument document, String indexPath) {
		String documentPath = document.getPath();
		if (org.eclipse.jdt.internal.compiler.util.Util.isJavaFileName(documentPath)) {
			new SourceIndexer(document, indexPath).indexDocument();
		} else if (org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(documentPath)) {
			new BinaryIndexer(document, indexPath).indexDocument();
		}
	}
	
	/* (non-Javadoc)
	 * @see SearchParticipant#locateMatches(SearchDocument[], SearchPattern, IJavaSearchScope, SearchRequestor, IProgressMonitor)
	 */
	public void locateMatches(SearchDocument[] indexMatches, SearchPattern pattern,
			IJavaSearchScope scope, SearchRequestor requestor, IProgressMonitor monitor) throws CoreException {
		
		MatchLocator matchLocator = 
			new MatchLocator(
				pattern, 
				requestor, 
				scope,
				this.workingCopies,
				monitor == null ? null : new SubProgressMonitor(monitor, 95)
		);

		// working copies take precedence over corresponding compilation units
		HashMap workingCopyDocuments = workingCopiesThatCanSeeFocus(pattern.focus, pattern.isPolymorphicSearch());
		SearchDocument[] matches = null;
		int length = indexMatches.length;
		for (int i = 0; i < length; i++) {
			SearchDocument searchDocument = indexMatches[i];
			if (searchDocument.getParticipant() == this) {
				SearchDocument workingCopyDocument = (SearchDocument) workingCopyDocuments.remove(searchDocument.getPath());
				if (workingCopyDocument != null) {
					if (matches == null) {
						System.arraycopy(indexMatches, 0, matches = new SearchDocument[length], 0, length);
					}
					matches[i] = workingCopyDocument;
				}
			}
		}
		if (matches == null) { // no working copy
			matches = indexMatches;
		}
		int remainingWorkingCopiesSize = workingCopyDocuments.size();
		if (remainingWorkingCopiesSize != 0) {
			System.arraycopy(matches, 0, matches = new SearchDocument[length+remainingWorkingCopiesSize], 0, length);
			Iterator iterator = workingCopyDocuments.values().iterator();
			int index = length;
			while (iterator.hasNext()) {
				matches[index++] = (SearchDocument) iterator.next();
			}
		}

		/* eliminating false matches and locating them */
		if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
		matchLocator.locateMatches(matches);
		

		if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
		
		matchLocator.locatePackageDeclarations(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.search.SearchParticipant#selectIndexes(org.eclipse.jdt.core.search.SearchQuery, org.eclipse.jdt.core.search.SearchContext)
	 */
	public IPath[] selectIndexes(
		SearchPattern pattern,
		IJavaSearchScope scope) {
		
		if (this.indexSelector == null) {
			this.indexSelector = new IndexSelector(scope, pattern);
		}
		return this.indexSelector.getIndexKeys();
	}
	
	/*
	 * Returns the working copies that can see the given focus.
	 */
	private HashMap workingCopiesThatCanSeeFocus(IJavaElement focus, boolean isPolymorphicSearch) {
		if (this.workingCopies == null) return new HashMap();
		if (focus != null) {
			while (!(focus instanceof IJavaProject) && !(focus instanceof JarPackageFragmentRoot)) {
				focus = focus.getParent();
			}
		}
		HashMap result = new HashMap();
		for (int i=0, length = this.workingCopies.length; i<length; i++) {
			ICompilationUnit workingCopy = this.workingCopies[i];
			IPath projectOrJar = IndexSelector.getProjectOrJar(workingCopy).getPath();
			if (focus == null || IndexSelector.canSeeFocus(focus, isPolymorphicSearch, projectOrJar)) {
				result.put(
					workingCopy.getPath().toString(),
					new WorkingCopyDocument(workingCopy)
				);
			}
		}
		return result;
	}

}
