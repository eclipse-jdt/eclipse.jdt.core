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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.core.Util;
import org.eclipse.jdt.internal.core.index.IIndex;
import org.eclipse.jdt.internal.core.index.IQueryResult;
import org.eclipse.jdt.internal.core.index.impl.IFileDocument;
import org.eclipse.jdt.internal.core.search.processing.JobManager;
import org.eclipse.jdt.internal.core.util.SimpleLookupTable;

public class IndexBinaryFolder extends IndexRequest {
	IFolder folder;
	IProject project;

	public IndexBinaryFolder(IFolder folder, IndexManager manager, IProject project) {
		super(folder.getFullPath(), manager);
		this.folder = folder;
		this.project = project;
	}
	public boolean belongsTo(String jobFamily) {
		return this.project != null && jobFamily.equals(this.project.getName());
	}
	public boolean equals(Object o) {
		if (o instanceof IndexBinaryFolder)
			return this.folder.equals(((IndexBinaryFolder) o).folder);
		return false;
	}
	/**
	 * Ensure consistency of a folder index. Need to walk all nested resources,
	 * and discover resources which have either been changed, added or deleted
	 * since the index was produced.
	 */
	public boolean execute(IProgressMonitor progressMonitor) {

		if (progressMonitor != null && progressMonitor.isCanceled()) return true;
		if (!this.folder.isAccessible()) return true; // nothing to do

		IIndex index = this.manager.getIndex(this.indexPath, true, /*reuse index file*/ true /*create if none*/);
		if (index == null) return true;
		ReadWriteMonitor monitor = this.manager.getMonitorFor(index);
		if (monitor == null) return true; // index got deleted since acquired

		try {
			monitor.enterRead(); // ask permission to read
			saveIfNecessary(index, monitor);

			IQueryResult[] results = index.queryInDocumentNames(""); // all file names //$NON-NLS-1$
			int max = results == null ? 0 : results.length;
			final SimpleLookupTable indexedFileNames = new SimpleLookupTable(max == 0 ? 33 : max + 11);
			final String OK = "OK"; //$NON-NLS-1$
			final String DELETED = "DELETED"; //$NON-NLS-1$
			if (max == 0) {
// KJ : Release next week
//				this.folder.accept(new IResourceProxyVisitor() {
//					public boolean visit(IResourceProxy proxy) {
//						if (isCancelled) return false;
//						if (proxy.getType() == IResource.FILE) {
//							if (Util.isClassFileName(proxy.getName())) {
//								IResource resource = proxy.requestResource();
//								if (resource.getLocation() != null) {
				this.folder.accept(new IResourceVisitor() {
					public boolean visit(IResource resource) {
// KJ : what should happen if an index job is cancelled? 2 others below...
						if (isCancelled) return false;
						if (resource.getType() == IResource.FILE) {
							if (Util.isClassFileName(resource.getName()) && resource.getLocation() != null) {
								String name = new IFileDocument((IFile) resource).getName();
								indexedFileNames.put(name, resource);
							}
							return false;
						}
						return true;
					}
				});
			} else {
				for (int i = 0; i < max; i++)
					indexedFileNames.put(results[i].getPath(), DELETED);

				final long indexLastModified = index.getIndexFile().lastModified();
//				this.folder.accept(new IResourceProxyVisitor() {
//					public boolean visit(IResourceProxy proxy) {
//						if (isCancelled) return false;
//						if (proxy.getType() == IResource.FILE) {
//							if (Util.isClassFileName(proxy.getName())) {
//								IResource resource = proxy.requestResource();
				this.folder.accept(new IResourceVisitor() {
					public boolean visit(IResource resource) {
						if (isCancelled) return false;
						if (resource.getType() == IResource.FILE) {
							if (Util.isClassFileName(resource.getName())) {
								IPath path = resource.getLocation();
								if (path != null) {
									String name = new IFileDocument((IFile) resource).getName();
									indexedFileNames.put(name,
										indexedFileNames.get(name) == null || indexLastModified < path.toFile().lastModified()
											? (Object) resource
											: (Object) OK);
								}
							}
							return false;
						}
						return true;
					}
				});
			}

			Object[] names = indexedFileNames.keyTable;
			Object[] values = indexedFileNames.valueTable;
			boolean shouldSave = false;
			for (int i = 0, length = names.length; i < length; i++) {
				String name = (String) names[i];
				if (name != null) {
					if (this.isCancelled) return false;

					Object value = values[i];
					if (value != OK) {
						shouldSave = true;
						if (value == DELETED)
							this.manager.remove(name, this.indexPath);
						else
							this.manager.addBinary((IFile) value, this.indexPath);
					}
				}
			}

			// request to save index when all class files have been indexed
			if (shouldSave)
				this.manager.request(new SaveIndex(this.indexPath, this.manager));

		} catch (CoreException e) {
			if (JobManager.VERBOSE) {
				JobManager.verbose("-> failed to index " + this.folder + " because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			this.manager.removeIndex(this.indexPath);
			return false;
		} catch (IOException e) {
			if (JobManager.VERBOSE) {
				JobManager.verbose("-> failed to index " + this.folder + " because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			this.manager.removeIndex(this.indexPath);
			return false;
		} finally {
			monitor.exitRead(); // free read lock
		}
		return true;
	}
	public int hashCode() {
		return this.folder.hashCode();
	}
	protected Integer updatedIndexState() {
		return IndexManager.REBUILDING_STATE;
	}
	public String toString() {
		return "indexing binary folder " + this.folder.getFullPath(); //$NON-NLS-1$
	}
}