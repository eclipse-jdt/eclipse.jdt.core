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
package org.eclipse.jdt.internal.core.search.indexing;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.core.index.Index;
import org.eclipse.jdt.internal.core.search.processing.JobManager;
import org.eclipse.jdt.internal.core.util.Util;

class RemoveFolderFromIndex extends IndexRequest {
	IPath folderPath;
	char[][] inclusionPatterns;
	char[][] exclusionPatterns;
	IProject project;

	public RemoveFolderFromIndex(IPath folderPath, char[][] inclusionPatterns, char[][] exclusionPatterns, IProject project, IndexManager manager) {
		super(project.getFullPath(), manager);
		this.folderPath = folderPath;
		this.inclusionPatterns = inclusionPatterns;
		this.exclusionPatterns = exclusionPatterns;
		this.project = project;
	}
	public boolean execute(IProgressMonitor progressMonitor) {

		if (this.isCancelled || progressMonitor != null && progressMonitor.isCanceled()) return true;

		/* ensure no concurrent write access to index */
		Index index = manager.getIndex(this.containerPath, true, /*reuse index file*/ false /*create if none*/);
		if (index == null) return true;
		ReadWriteMonitor monitor = index.monitor;
		if (monitor == null) return true; // index got deleted since acquired

		try {
			monitor.enterRead(); // ask permission to read
			String[] paths = index.queryDocumentNames(this.folderPath.toString());
			// all file names belonging to the folder or its subfolders and that are not excluded (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=32607)
			for (int i = 0, max = paths == null ? 0 : paths.length; i < max; i++) {
				String documentPath = paths[i];
				if (!Util.isExcluded(new Path(documentPath), this.inclusionPatterns, this.exclusionPatterns, false)) {
					manager.remove(documentPath, this.containerPath); // write lock will be acquired by the remove operation
				}
			}
		} catch (IOException e) {
			if (JobManager.VERBOSE) {
				JobManager.verbose("-> failed to remove " + this.folderPath + " from index because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			return false;
		} finally {
			monitor.exitRead(); // free read lock
		}
		return true;
	}
	public String toString() {
		return "removing " + this.folderPath + " from index " + this.containerPath; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
