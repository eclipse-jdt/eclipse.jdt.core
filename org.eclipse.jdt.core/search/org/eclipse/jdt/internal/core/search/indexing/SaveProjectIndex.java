/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.search.indexing;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.core.index.IIndex;
import org.eclipse.jdt.internal.core.search.processing.JobManager;

/*
 * Save the index of a project.
 */
public class SaveProjectIndex extends IndexRequest {

	IndexManager manager;
	IPath projectPath;
	
	public SaveProjectIndex(IndexManager manager, IPath projectPath) {
		this.manager = manager;
		this.projectPath = projectPath;
	}
	
	public boolean belongsTo(String jobFamily) {
		return jobFamily.equals(this.projectPath.segment(0));
	}

	public boolean execute(IProgressMonitor progressMonitor) {
		
		if (progressMonitor != null && progressMonitor.isCanceled()) return COMPLETE;
		
		try {
			IIndex index = this.manager.getIndex(this.projectPath, true /*reuse index file*/, false /*don't create if none*/);
			/* ensure no concurrent write access to index */
			if (index == null)
				return COMPLETE;
			ReadWriteMonitor monitor = this.manager.getMonitorFor(index);
			if (monitor == null)
				return COMPLETE; // index got deleted since acquired
			try {
				monitor.enterWrite(); // ask permission to write
				index.save();
			} finally {
				monitor.exitWrite(); // free write lock
			}
		} catch (IOException e) {
			if (JobManager.VERBOSE) {
				JobManager.verbose("-> failed to save index " + this.projectPath + " because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			return FAILED;
		}
		return COMPLETE;
	}
	public String toString() {
		return "saving index for " + this.projectPath; //$NON-NLS-1$
	}	

}
