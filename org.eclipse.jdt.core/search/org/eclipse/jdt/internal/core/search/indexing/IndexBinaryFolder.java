package org.eclipse.jdt.internal.core.search.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.internal.core.search.processing.*;
import org.eclipse.jdt.internal.core.index.impl.*;

import java.io.*;
import java.util.*;

public class IndexBinaryFolder implements IJob {
	IFolder folder;
	IndexManager manager;
	IProject project;
	public IndexBinaryFolder(
		IFolder folder,
		IndexManager manager,
		IProject project) {
		this.folder = folder;
		this.manager = manager;
		this.project = project;
	}
	public boolean belongsTo(String jobFamily) {
		return jobFamily.equals(this.project.getName());
	}
	/**
	 * Ensure consistency of a folder index. Need to walk all nested resources,
	 * and discover resources which have either been changed, added or deleted
	 * since the index was produced.
	 */
	public boolean execute() {

		if (!this.folder.isAccessible())
			return COMPLETE; // nothing to do

		IIndex index = manager.getIndex(this.folder.getFullPath());
		if (index == null)
			return COMPLETE;
		ReadWriteMonitor monitor = manager.getMonitorFor(index);
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
						System.out.println("-> merging index : " + index.getIndexFile()); //$NON-NLS-1$
					index.save();
				} catch (IOException e) {
					return FAILED;
				} finally {
					monitor.exitWrite(); // finished writing
					monitor.enterRead(); // reacquire read permission
				}
			}
			final String OK = "OK"; //$NON-NLS-1$
			final String DELETED = "DELETED"; //$NON-NLS-1$
			final long indexLastModified = index.getIndexFile().lastModified();

			final Hashtable indexedFileNames = new Hashtable(100);
			IQueryResult[] results = index.queryInDocumentNames("");// all file names //$NON-NLS-1$
			for (int i = 0, max = results == null ? 0 : results.length; i < max; i++) {
				String fileName = results[i].getPath();
				indexedFileNames.put(fileName, DELETED);
			}
			this.folder.accept(new IResourceVisitor() {
				public boolean visit(IResource resource) {
					if (resource.getType() == IResource.FILE) {
						String extension = resource.getFileExtension();
						if ((extension != null)
							&& extension.equalsIgnoreCase("class")) { //$NON-NLS-1$
							IPath path = resource.getLocation();
							if (path != null) {
								File resourceFile = path.toFile();
								String name = new IFileDocument((IFile) resource).getName();
								if (indexedFileNames.get(name) == null) {
									indexedFileNames.put(name, resource);
								} else {
									indexedFileNames.put(
										name,
										resourceFile.lastModified() > indexLastModified
											? (Object) resource
											: (Object) OK);
								}
							}
						}
						return false;
					}
					return true;
				}
			});
			Enumeration names = indexedFileNames.keys();
			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				Object value = indexedFileNames.get(name);
				if (value instanceof IFile) {
					manager.add((IFile) value, this.folder);
				} else if (value == DELETED) {
					manager.remove(name, this.project);
				}
			}
		} catch (CoreException e) {
			return FAILED;
		} catch (IOException e) {
			return FAILED;
		} finally {
			monitor.exitRead(); // free read lock
		}
		return COMPLETE;
	}
	public String toString() {
		return "indexing binary folder " + this.folder.getFullPath(); //$NON-NLS-1$
	}
}