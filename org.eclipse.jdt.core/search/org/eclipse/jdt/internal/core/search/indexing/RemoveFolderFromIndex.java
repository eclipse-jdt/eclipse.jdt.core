package org.eclipse.jdt.internal.core.search.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.internal.core.search.processing.*;

import org.eclipse.core.resources.*;

import java.io.*;

class RemoveFolderFromIndex implements IJob {
	String folderPath;
	IPath indexedContainer;
	IndexManager manager;
	public RemoveFolderFromIndex(
		String folderPath,
		IPath indexedContainer,
		IndexManager manager) {
		this.folderPath = folderPath;
		this.indexedContainer = indexedContainer;
		this.manager = manager;
	}
	public boolean belongsTo(String jobFamily) {
		return jobFamily.equals(indexedContainer.segment(0));
	}
	public boolean execute(IProgressMonitor progressMonitor) {
		
		if (progressMonitor != null && progressMonitor.isCanceled()) return COMPLETE;
		
		try {
			IIndex index = manager.getIndex(this.indexedContainer);
			if (index == null)
				return COMPLETE;

			/* ensure no concurrent write access to index */
			ReadWriteMonitor monitor = manager.getMonitorFor(index);
			if (monitor == null)
				return COMPLETE; // index got deleted since acquired
			try {
				monitor.enterRead(); // ask permission to read
				IQueryResult[] results = index.queryInDocumentNames(this.folderPath); // all file names beonlonging to the folder or its subfolders
				for (int i = 0, max = results == null ? 0 : results.length; i < max; i++) {
					String fileName = results[i].getPath();
					manager.remove(fileName, this.indexedContainer); // write lock will be acquired by the remove operation
				}
			} finally {
				monitor.exitRead(); // free read lock
			}
		} catch (IOException e) {
			return FAILED;
		}
		return COMPLETE;
	}
	public String toString() {
		return "removing from index " + this.folderPath; //$NON-NLS-1$
	}
	/*
	 * @see IJob#cancel()
	 */
	public void cancel() {
	}

}
