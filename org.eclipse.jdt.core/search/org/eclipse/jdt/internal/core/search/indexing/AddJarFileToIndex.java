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
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.index.IIndex;
import org.eclipse.jdt.internal.core.index.IQueryResult;
import org.eclipse.jdt.internal.core.index.impl.JarFileEntryDocument;
import org.eclipse.jdt.internal.core.search.processing.JobManager;
import org.eclipse.jdt.internal.core.util.SimpleLookupTable;

class AddJarFileToIndex extends IndexRequest {
	IFile resource;
	String projectName;

	public AddJarFileToIndex(IFile resource, IndexManager manager, String projectName) {
		super(resource.getFullPath(), manager);
		this.resource = resource;
		this.projectName = projectName;
	}
	public AddJarFileToIndex(IPath indexPath, IndexManager manager, String projectName) {
		// external JAR scenario - no resource
		super(indexPath, manager);
		this.projectName = projectName;
	}
	public boolean belongsTo(String jobFamily) {
		// used to remove pending jobs because the project was deleted... not to delete index files
		// can be found either by project name or JAR path name
		return jobFamily.equals(projectName) || this.indexPath.toString().equals(jobFamily);
	}
	public boolean equals(Object o) {
		if (o instanceof AddJarFileToIndex) {
			if (this.resource != null)
				return this.resource.equals(((AddJarFileToIndex) o).resource);
			if (this.indexPath != null)
				return this.indexPath.equals(((AddJarFileToIndex) o).indexPath);
		}
		return false;
	}
	public int hashCode() {
		if (this.resource != null)
			return this.resource.hashCode();
		if (this.indexPath != null)
			return this.indexPath.hashCode();
		return -1;
	}
	public boolean execute(IProgressMonitor progressMonitor) {

		if (progressMonitor != null && progressMonitor.isCanceled()) return true;

		try {
			// if index is already cached, then do not perform any check
			// MUST reset the IndexManager if a jar file is changed
			IIndex index = (IIndex) manager.getIndex(this.indexPath, false, /*do not reuse index file*/ false /*do not create if none*/);
			if (index != null) {
				if (JobManager.VERBOSE)
					JobManager.verbose("-> no indexing required (index already exists) for " + this.indexPath); //$NON-NLS-1$
				return true;
			}

			index = manager.getIndex(this.indexPath, true, /*reuse index file*/ true /*create if none*/);
			if (index == null) {
				if (JobManager.VERBOSE)
					JobManager.verbose("-> index could not be created for " + this.indexPath); //$NON-NLS-1$
				return true;
			}
			ReadWriteMonitor monitor = manager.getMonitorFor(index);
			if (monitor == null) {
				if (JobManager.VERBOSE)
					JobManager.verbose("-> index for " + this.indexPath + " just got deleted"); //$NON-NLS-1$//$NON-NLS-2$
				return true; // index got deleted since acquired
			}
			ZipFile zip = null;
			try {
				// this path will be a relative path to the workspace in case the zipfile in the workspace otherwise it will be a path in the
				// local file system
				Path zipFilePath = null;

				monitor.enterWrite(); // ask permission to write
				if (resource != null) {
					IPath location = this.resource.getLocation();
					if (location == null) return false;
					if (JavaModelManager.ZIP_ACCESS_VERBOSE)
						System.out.println("(" + Thread.currentThread() + ") [AddJarFileToIndex.execute()] Creating ZipFile on " + location); //$NON-NLS-1$	//$NON-NLS-2$
					zip = new ZipFile(location.toFile());
					zipFilePath = (Path) this.resource.getFullPath().makeRelative();
					// absolute path relative to the workspace
				} else {
					if (JavaModelManager.ZIP_ACCESS_VERBOSE)
						System.out.println("(" + Thread.currentThread() + ") [AddJarFileToIndex.execute()] Creating ZipFile on " + this.indexPath); //$NON-NLS-1$	//$NON-NLS-2$
					zip = new ZipFile(this.indexPath.toFile());
					zipFilePath = (Path) this.indexPath;
					// path is already canonical since coming from a library classpath entry
				}

				if (JobManager.VERBOSE)
					JobManager.verbose("-> indexing " + zip.getName()); //$NON-NLS-1$
				long initialTime = System.currentTimeMillis();

				IQueryResult[] results = index.queryInDocumentNames(""); // all file names //$NON-NLS-1$
				int max = results == null ? 0 : results.length;
				if (max != 0) {
					/* check integrity of the existing index file
					 * if the length is equal to 0, we want to index the whole jar again
					 * If not, then we want to check that there is no missing entry, if
					 * one entry is missing then we recreate the index
					 */
					String EXISTS = "OK"; //$NON-NLS-1$
					String DELETED = "DELETED"; //$NON-NLS-1$
					SimpleLookupTable indexedFileNames = new SimpleLookupTable(max == 0 ? 33 : max + 11);
					for (int i = 0; i < max; i++)
						indexedFileNames.put(results[i].getPath(), DELETED);
					for (Enumeration e = zip.entries(); e.hasMoreElements();) {
						// iterate each entry to index it
						ZipEntry ze = (ZipEntry) e.nextElement();
						if (Util.isClassFileName(ze.getName())) {
							JarFileEntryDocument entryDocument = new JarFileEntryDocument(ze, null, zipFilePath);
							indexedFileNames.put(entryDocument.getName(), EXISTS);
						}
					}
					boolean needToReindex = indexedFileNames.elementSize != max; // a new file was added
					if (!needToReindex) {
						Object[] valueTable = indexedFileNames.valueTable;
						for (int i = 0, l = valueTable.length; i < l; i++) {
							if (valueTable[i] == DELETED) {
								needToReindex = true; // a file was deleted so re-index
								break;
							}
						}
						if (!needToReindex) {
							if (JobManager.VERBOSE)
								JobManager.verbose("-> no indexing required (index is consistent with library) for " //$NON-NLS-1$
								+ zip.getName() + " (" //$NON-NLS-1$
								+ (System.currentTimeMillis() - initialTime) + "ms)"); //$NON-NLS-1$
							return true;
						}
					}
				}

				/*
				 * Index the jar for the first time or reindex the jar in case the previous index file has been corrupted
				 */
				if (index != null) // index already existed: recreate it so that we forget about previous entries
					index = manager.recreateIndex(this.indexPath);
				for (Enumeration e = zip.entries(); e.hasMoreElements();) {
					if (this.isCancelled) {
// KJ : what should happen if an index job is cancelled?
						if (JobManager.VERBOSE)
							JobManager.verbose("-> indexing of " + zip.getName() + " has been cancelled"); //$NON-NLS-1$ //$NON-NLS-2$
						return false;
					}

					// iterate each entry to index it
					ZipEntry ze = (ZipEntry) e.nextElement();
					if (Util.isClassFileName(ze.getName())) {
						byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getZipEntryByteContent(ze, zip);
						// Add the name of the file to the index
						index.add(
							new JarFileEntryDocument(ze, classFileBytes, zipFilePath),
							new BinaryIndexer(true));
					}
				}
				this.manager.saveIndex(index);
				if (JobManager.VERBOSE)
					JobManager.verbose("-> done indexing of " //$NON-NLS-1$
						+ zip.getName() + " (" //$NON-NLS-1$
						+ (System.currentTimeMillis() - initialTime) + "ms)"); //$NON-NLS-1$
			} finally {
				if (zip != null) {
					if (JavaModelManager.ZIP_ACCESS_VERBOSE)
						System.out.println("(" + Thread.currentThread() + ") [AddJarFileToIndex.execute()] Closing ZipFile " + zip); //$NON-NLS-1$	//$NON-NLS-2$
					zip.close();
				}
				monitor.exitWrite(); // free write lock
			}
		} catch (IOException e) {
			if (JobManager.VERBOSE) {
				JobManager.verbose("-> failed to index " + this.indexPath + " because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			manager.removeIndex(this.indexPath);
			return false;
		}
		return true;
	}
	protected Integer updatedIndexState() {
		return IndexManager.REBUILDING_STATE;
	}
	public String toString() {
		return "indexing " + this.indexPath.toString(); //$NON-NLS-1$
	}
}