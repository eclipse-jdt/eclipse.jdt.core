/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.search.indexing;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.core.index.IIndex;
import org.eclipse.jdt.internal.core.search.processing.IJob;

public abstract class IndexRequest implements IJob {
	protected boolean isCancelled = false;
	protected IPath indexPath;
	protected IndexManager manager;

	public IndexRequest(IPath indexPath, IndexManager manager) {
		this.indexPath = indexPath;
		this.manager = manager;
	}
	public boolean belongsTo(String jobFamily) {
		return jobFamily.equals(this.indexPath.segment(0));
	}
	public void cancel() {
		this.isCancelled = true;
	}
	public boolean isReadyToRun() {
		// tag the index as inconsistent
		this.manager.aboutToUpdateIndex(indexPath, updatedIndexState());
		return true;
	}
	protected void saveIfNecessary(IIndex index, ReadWriteMonitor monitor) throws IOException {
		/* if index has changed, commit these before querying */
		if (index.hasChanged()) {
			try {
				monitor.exitRead(); // free read lock
				monitor.enterWrite(); // ask permission to write
				this.manager.saveIndex(index);
			} finally {
				monitor.exitWriteEnterRead(); // finished writing and reacquire read permission
			}
		}
	}
	protected Integer updatedIndexState() {
		return IndexManager.UPDATING_STATE;
	}
}