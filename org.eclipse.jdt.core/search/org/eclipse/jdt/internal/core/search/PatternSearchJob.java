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

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.core.index.IIndex;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.jdt.internal.core.search.matching.SearchPattern;
import org.eclipse.jdt.internal.core.search.processing.IJob;
import org.eclipse.jdt.internal.core.search.processing.JobManager;

public class PatternSearchJob implements IJob {

	protected SearchPattern pattern;
	protected IJavaSearchScope scope;
	protected IJavaElement focus;
	protected IIndexSearchRequestor requestor;
	protected IndexManager indexManager;
	protected int detailLevel;
	protected IndexSelector indexSelector;
	protected boolean isPolymorphicSearch;
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
			false,
			detailLevel,
			requestor,
			indexManager);
	}
	public PatternSearchJob(
		SearchPattern pattern,
		IJavaSearchScope scope,
		IJavaElement focus,
		boolean isPolymorphicSearch,
		int detailLevel,
		IIndexSearchRequestor requestor,
		IndexManager indexManager) {

		this.pattern = pattern;
		this.scope = scope;
		this.focus = focus;
		this.isPolymorphicSearch = isPolymorphicSearch;
		this.detailLevel = detailLevel;
		this.requestor = requestor;
		this.indexManager = indexManager;
	}
	public boolean belongsTo(String jobFamily) {
		return true;
	}
	public void cancel() {
	}
	public boolean execute(IProgressMonitor progressMonitor) {

		if (progressMonitor != null && progressMonitor.isCanceled())
			throw new OperationCanceledException();
		boolean isComplete = COMPLETE;
		executionTime = 0;
		if (this.indexSelector == null) {
			this.indexSelector =
				new IndexSelector(this.scope, this.focus, this.isPolymorphicSearch, this.indexManager);
		}
		IIndex[] searchIndexes = this.indexSelector.getIndexes();
		try {
			int max = searchIndexes.length;
			if (progressMonitor != null) {
				progressMonitor.beginTask("", max); //$NON-NLS-1$
			}
			for (int i = 0; i < max; i++) {
				isComplete &= search(searchIndexes[i], progressMonitor);
				if (progressMonitor != null) {
					if (progressMonitor.isCanceled()) {
						throw new OperationCanceledException();
					} else {
						progressMonitor.worked(1);
					}
				}
			}
			if (JobManager.VERBOSE) {
				JobManager.verbose("-> execution time: " + executionTime + "ms - " + this);//$NON-NLS-1$//$NON-NLS-2$
			}
			return isComplete;
		} finally {
			if (progressMonitor != null) {
				progressMonitor.done();
			}
		}
	}
	public boolean isReadyToRun() {
		if (this.indexSelector == null) { // only check once, i.e. as long as this job is used, it will keep the same index picture
			this.indexSelector = new IndexSelector(this.scope, this.focus, this.isPolymorphicSearch, this.indexManager);
			this.indexSelector.getIndexes(); // will only cache answer if all indexes were available originally
		}
		return true;
	}
	public boolean search(IIndex index, IProgressMonitor progressMonitor) {

		if (progressMonitor != null && progressMonitor.isCanceled())
			throw new OperationCanceledException();

//		IIndex inMemIndex = indexManager.peekAtIndex(new Path(((Index)index).toString.substring("Index for ".length()).replace('\\','/')));
//		if (inMemIndex != index) {
//			System.out.println("SANITY CHECK: search job using obsolete index: ["+index+ "] instead of: ["+inMemIndex+"]");
//		}
		
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
					this.indexManager.saveIndex(index);
				} catch (IOException e) {
					return FAILED;
				} finally {
					monitor.exitWriteEnterRead(); // finished writing and reacquire read permission
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
}
