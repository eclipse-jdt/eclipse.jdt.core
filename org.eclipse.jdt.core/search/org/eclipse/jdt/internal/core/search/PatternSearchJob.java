package org.eclipse.jdt.internal.core.search;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.search.indexing.*;
import org.eclipse.jdt.internal.core.search.matching.*;
import org.eclipse.jdt.internal.core.search.processing.*;

import java.io.*;

public class PatternSearchJob implements IJob {

	protected SearchPattern pattern;
	protected IJavaSearchScope scope;
	protected IJavaElement focus;
	protected IIndexSearchRequestor requestor;
	protected IndexManager indexManager;
	protected int detailLevel;
	protected IndexSelector indexSelector;
	protected long executionTime = 0;
	
	public PatternSearchJob(
		SearchPattern pattern,
		IJavaSearchScope scope,
		int detailLevel,
		IIndexSearchRequestor requestor,
		IndexManager indexManager) {

		this(
			pattern,
			scope,
			null,
			detailLevel,
			requestor,
			indexManager);
	}

	public PatternSearchJob(
		SearchPattern pattern,
		IJavaSearchScope scope,
		IJavaElement focus,
		int detailLevel,
		IIndexSearchRequestor requestor,
		IndexManager indexManager) {

		this.pattern = pattern;
		this.scope = scope;
		this.focus = focus;
		this.detailLevel = detailLevel;
		this.requestor = requestor;
		this.indexManager = indexManager;
	}

	public boolean belongsTo(String jobFamily) {
		return true;
	}

	/**
	 * execute method comment.
	 */
	public boolean execute(IProgressMonitor progressMonitor) {

		if (progressMonitor != null && progressMonitor.isCanceled())
			throw new OperationCanceledException();
		boolean isComplete = COMPLETE;
		executionTime = 0;
		if (this.indexSelector == null) {
			this.indexSelector =
				new IndexSelector(this.scope, this.focus, this.indexManager);
		}
		IIndex[] searchIndexes = this.indexSelector.getIndexes();
		for (int i = 0, max = searchIndexes.length; i < max; i++) {
			isComplete &= search(searchIndexes[i], progressMonitor);
		}
		if (JobManager.VERBOSE) {
			System.out.println(
				"-> execution time: " + executionTime + " ms. for : " + this);//$NON-NLS-1$//$NON-NLS-2$
			//$NON-NLS-2$ //$NON-NLS-1$
		}
		return isComplete;
	}

	/**
	 * execute method comment.
	 */
	public boolean search(IIndex index, IProgressMonitor progressMonitor) {

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
						System.out.println("-> merging index : " + index.getIndexFile());//$NON-NLS-1$
					//$NON-NLS-1$
					index.save();
				} catch (IOException e) {
					return FAILED;
				} finally {
					monitor.exitWrite(); // finished writing
					monitor.enterRead(); // reaquire read permission
				}
			}
			long start = System.currentTimeMillis();
			pattern.findIndexMatches(
				index,
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

	public String toString() {
		return "searching " + pattern.toString(); //$NON-NLS-1$
	}
	/*
	 * @see IJob#cancel()
	 */
	public void cancel() {
	}

}