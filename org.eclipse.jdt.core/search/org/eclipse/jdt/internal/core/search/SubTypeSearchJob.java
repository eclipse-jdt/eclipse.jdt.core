package org.eclipse.jdt.internal.core.search;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.search.indexing.*;
import org.eclipse.jdt.internal.core.search.matching.*;
import org.eclipse.jdt.internal.core.search.processing.*;
import org.eclipse.jdt.internal.core.index.impl.*;

import java.io.*;
import java.util.*;

public class SubTypeSearchJob extends PatternSearchJob {

	Hashtable inputs = new Hashtable(5);
	public SubTypeSearchJob(
		SearchPattern pattern,
		IJavaSearchScope scope,
		int detailLevel,
		IIndexSearchRequestor requestor,
		IndexManager indexManager,
		IProgressMonitor progressMonitor) {
		super(pattern, scope, detailLevel, requestor, indexManager, progressMonitor);
	}

	public SubTypeSearchJob(
		SearchPattern pattern,
		IJavaSearchScope scope,
		IJavaElement focus,
		int detailLevel,
		IIndexSearchRequestor requestor,
		org.eclipse.jdt.internal.core.search.indexing.IndexManager indexManager,
		IProgressMonitor progressMonitor) {
		super(
			pattern,
			scope,
			focus,
			detailLevel,
			requestor,
			indexManager,
			progressMonitor);
	}

	public void closeAll() {

		Enumeration openedInputs = inputs.elements();
		while (openedInputs.hasMoreElements()) {
			IndexInput input = (IndexInput) openedInputs.nextElement();
			try {
				input.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * execute method comment.
	 */
	public boolean search(IIndex index) {

		if (progressMonitor != null && progressMonitor.isCanceled())
			throw new OperationCanceledException();

		if (index == null)
			return COMPLETE;
		ReadWriteMonitor monitor = indexManager.getMonitorFor(index);
		if (monitor == null)
			return COMPLETE; // index got deleted since acquired
		try {
			monitor.enterRead(); // ask permission to read

			/* if index has changed, commit these before querying */
			if (index.hasChanged()) {
				try {
					monitor.exitRead(); // free read lock
					monitor.enterWrite(); // ask permission to write
					if (IndexManager.VERBOSE)
						System.out.println("-> merging index : " + index.getIndexFile());
					index.save();
				} catch (IOException e) {
					return FAILED;
				} finally {
					monitor.exitWrite(); // finished writing
					monitor.enterRead(); // reaquire read permission
				}
			}
			long start = System.currentTimeMillis();

			IndexInput input;
			if ((input = (IndexInput) inputs.get(index)) == null) {
				input = new BlocksIndexInput(index.getIndexFile());
				input.open();
				inputs.put(index, input);
				//System.out.println("Acquiring INPUT for "+index);
			}
			pattern.findIndexMatches(
				input,
				requestor,
				detailLevel,
				progressMonitor,
				this.scope);
			executionTime += System.currentTimeMillis() - start;
			return COMPLETE;
		} catch (IOException e) {
			return FAILED;
		} finally {
			monitor.exitRead(); // finished reading
		}
	}

}
