package org.eclipse.jdt.internal.core.search.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.internal.core.search.processing.*;
import org.eclipse.jdt.internal.core.index.impl.*;

import java.io.*;
import java.util.*;

public class IndexAllProject implements IJob, IResourceVisitor {
	IProject project;
	IndexManager manager;
	Hashtable indexedFileNames;
	final String OK = "OK"; //$NON-NLS-1$
	final String DELETED = "DELETED"; //$NON-NLS-1$
	long indexLastModified;
	public IndexAllProject(IProject project, IndexManager manager) {
		this.project = project;
		this.manager = manager;
	}
	public boolean belongsTo(String jobFamily) {
		return jobFamily.equals(project.getName());
	}
	/**
	 * Ensure consistency of a project index. Need to walk all nested resources,
	 * and discover resources which have either been changed, added or deleted
	 * since the index was produced.
	 */
	public boolean execute() {

		if (!project.isOpen())
			return COMPLETE; // nothing to do

		IIndex index = manager.getIndex(project.getFullPath());
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
			this.indexLastModified = index.getIndexFile().lastModified();

			this.indexedFileNames = new Hashtable(100);
			IQueryResult[] results = index.queryInDocumentNames(""); // all file names //$NON-NLS-1$
			for (int i = 0, max = results == null ? 0 : results.length; i < max; i++) {
				String fileName = results[i].getPath();
				this.indexedFileNames.put(fileName, DELETED);
			}
			JavaCore javaCore = JavaCore.getJavaCore();
			IJavaProject javaProject = javaCore.create(this.project);
			IClasspathEntry[] entries = javaProject.getResolvedClasspath(true);
			IWorkspaceRoot root = this.project.getWorkspace().getRoot();
			for (int i = 0, length = entries.length; i < length; i++) {
				IClasspathEntry entry = entries[i];
				// Index only the project's source folders.
				// Indexing of libraries is done in a separate job
				if ((entry.getEntryKind() == IClasspathEntry.CPE_SOURCE)) {
					IPath entryPath = entry.getPath();
					IResource sourceFolder = root.findMember(entryPath);
					if (sourceFolder != null) {
						sourceFolder.accept(this);
					}
				}
			}
			
			Enumeration names = indexedFileNames.keys();
			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				Object value = indexedFileNames.get(name);
				if (value instanceof IFile) {
					manager.add((IFile) value, this.project);
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
		return "indexing project " + project.getFullPath(); //$NON-NLS-1$
	}
	public boolean visit(IResource resource) {
		if (resource.getType() == IResource.FILE) {
			String extension = resource.getFileExtension();
			if ((extension != null) && extension.equalsIgnoreCase("java")) { //$NON-NLS-1$
				IPath path = resource.getLocation();
				if (path != null) {
					File resourceFile = path.toFile();
					String name = new IFileDocument((IFile) resource).getName();
					if (this.indexedFileNames.get(name) == null) {
						this.indexedFileNames.put(name, resource);
					} else {
						this.indexedFileNames.put(
							name,
							resourceFile.lastModified() > this.indexLastModified
								? (Object) resource
								: (Object) OK);
					}
				}
			}
			return false;
		}
		return true;
	}
	
}