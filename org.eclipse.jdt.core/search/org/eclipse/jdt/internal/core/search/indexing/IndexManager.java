package org.eclipse.jdt.internal.core.search.indexing;

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
import org.eclipse.jdt.internal.core.search.processing.*;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.index.impl.*;

import org.eclipse.jdt.core.JavaCore;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class IndexManager extends JobManager implements IIndexConstants {
	/* number of file contents in memory */
	public static int MAX_FILES_IN_MEMORY = 0;

	public IWorkspace workspace;

	/* indexes */
	Hashtable indexes = new Hashtable(5);

	/* read write monitors */
	private Hashtable monitors = new Hashtable(5);

	/* need to save ? */
	private boolean needToSave = false;
	private static final CRC32 checksumCalculator = new CRC32();
	private IPath javaPluginLocation = null;
	/**
	 * Before processing all jobs, need to ensure that the indexes are up to date.
	 */
	public void activateProcessing() {
		try {
			Thread.currentThread().sleep(10000);
			// wait 10 seconds so as not to interfere with plugin startup
		} catch (InterruptedException ie) {
		}
		checkIndexConsistency();
	}

	/**
	 * Trigger addition of a resource to an index
	 * Note: the actual operation is performed in background
	 */
	public void add(IFile resource) {
		if (JavaCore.getPlugin() == null || this.workspace == null)
			return;
		String extension = resource.getFileExtension();
		if ("java".equals(extension)) {
			AddCompilationUnitToIndex job = new AddCompilationUnitToIndex(resource, this);
			if (this.awaitingJobsCount() < MAX_FILES_IN_MEMORY) {
				job.initializeContents();
			}
			request(job);
		} else
			if ("class".equals(extension)) {
				AddClassFileToIndex job = new AddClassFileToIndex(resource, this);
				if (this.awaitingJobsCount() < MAX_FILES_IN_MEMORY) {
					job.initializeContents();
				}
				request(job);
			}
	}

	/**
	 * Ensures that indexes are up to date with workbench content. Typically
	 * it is invoked in background when activate the job processing.
	 */
	public void checkIndexConsistency() {

		if (VERBOSE)
			System.out.println("STARTING - ensuring consistency");

		boolean wasEnabled = isEnabled();
		try {
			disable();

			if (this.workspace == null)
				return;
			IProject[] projects = this.workspace.getRoot().getProjects();
			for (int i = 0, max = projects.length; i < max; i++) {
				IProject project = projects[i];
				// not only java project, given at startup nature may not have been set yet
				if (project.isOpen()) {
					indexAll(project);
				}
			}
		} finally {
			if (wasEnabled)
				enable();
			if (VERBOSE)
				System.out.println("DONE - ensuring consistency");
		}
	}

	private String computeIndexName(String pathString) {
		byte[] pathBytes = pathString.getBytes();
		checksumCalculator.reset();
		checksumCalculator.update(pathBytes);
		String fileName = Long.toString(checksumCalculator.getValue()) + ".index";
		if (VERBOSE)
			System.out.println(" index name: " + pathString + " <----> " + fileName);
		IPath indexPath = getJavaPluginWorkingLocation();
		String indexDirectory = indexPath.toOSString();
		if (indexDirectory.endsWith(File.separator)) {
			return indexDirectory + fileName;
		} else {
			return indexDirectory + File.separator + fileName;
		}
	}

	/**
	 * About to delete a project.
	 */
	public void deleting(IProject project) {
		discardJobsUntilNextProjectAddition(project.getName());

		IPath path = project.getFullPath();
		IIndex index = (IIndex) indexes.get(path);
		if (index != null) {
			indexes.remove(path);
			monitors.remove(index);
		}
	}

	/**
	 * Remove the index from cache for a given project.
	 * Passing null as a job family discards them all.
	 */
	public void discardJobsUntilNextProjectAddition(String jobFamily) {
		boolean wasEnabled = isEnabled();
		try {
			disable();

			// wait until current job has completed
			while (thread != null && executing) {
				try {
					Thread.currentThread().sleep(50);
				} catch (InterruptedException e) {
				}
			}

			// flush and compact awaiting jobs
			int loc = -1;
			boolean foundProjectAddition = false;
			for (int i = jobStart; i <= jobEnd; i++) {
				IJob currentJob = awaitingJobs[i];
				awaitingJobs[i] = null;
				if (jobFamily == null)
					continue; // discard
				if (currentJob.belongsTo(jobFamily)) { // might discard
					if (!(foundProjectAddition
						|| (foundProjectAddition = currentJob instanceof IndexAllProject)))
						continue; // discard
				}
				awaitingJobs[++loc] = currentJob;
			}
			jobStart = 0;
			jobEnd = loc;
		} finally {
			if (wasEnabled)
				enable();
		}
	}

	/**
	 * Returns the index for a given project, if none then create an empty one.
	 * Note: if there is an existing index file already, it will be reused. 
	 * Warning: Does not check whether index is consistent (not being used)
	 */
	public IIndex getIndex(IPath path) {
		return this.getIndex(path, true);
	}

	/**
	 * Returns the index for a given project, if none and asked for then create an empty one.
	 * Note: if there is an existing index file already, it will be reused. 
	 * Warning: Does not check whether index is consistent (not being used)
	 */
	public synchronized IIndex getIndex(IPath path, boolean mustCreate) {
		IIndex index = (IIndex) indexes.get(path);
		if (index == null) {
			try {
				// Compute canonical path
				IPath canonicalPath = JavaProject.canonicalizedPath(path);
				index = (IIndex) indexes.get(canonicalPath);
				if (!mustCreate)
					return index;
				if (index == null) {
					// New index: add same index for given path and canonical path
					String indexPath = computeIndexName(canonicalPath.toOSString());
					index =
						IndexFactory.newIndex(indexPath, "Index for " + canonicalPath.toOSString());
					indexes.put(canonicalPath, index);
					indexes.put(path, index);
					monitors.put(index, new ReadWriteMonitor());
				} else {
					// Index existed for canonical path, add it for given path
					indexes.put(path, index);
				}
			} catch (IOException e) {
				// The file could not be created. Possible reason: the project has been deleted.
				return null;
			}
		}
		//System.out.println(" index name: " + path.toOSString() + " <----> " + index.getIndexFile().getName());	
		return index;
	}

	private IPath getJavaPluginWorkingLocation() {
		if (javaPluginLocation == null) {
			javaPluginLocation = JavaCore.getPlugin().getStateLocation();
		}
		return javaPluginLocation;
	}

	/**
	 * Index access is controlled through a read-write monitor so as
	 * to ensure there is no concurrent read and write operations
	 * (only concurrent reading is allowed).
	 */
	public ReadWriteMonitor getMonitorFor(IIndex index) {

		return (ReadWriteMonitor) monitors.get(index);
	}

	/**
	 * Trigger addition of the entire content of a project
	 * Note: the actual operation is performed in background 
	 */
	public void indexAll(IProject project) {
		if (JavaCore.getPlugin() == null || this.workspace == null)
			return;

		// Also request indexing of binaries on the classpath
		// determine the new children
		try {
			IJavaModel model = JavaModelManager.getJavaModel(this.workspace);
			IJavaProject javaProject = ((JavaModel) model).getJavaProject(project);
			IClasspathEntry[] entries = javaProject.getResolvedClasspath(true);
			for (int i = 0; i < entries.length; i++) {
				IClasspathEntry entry = entries[i];
				IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots(entry);
				for (int j = 0; j < roots.length; j++) {
					IPackageFragmentRoot root = roots[j];
					if (root.exists()) {
						if (root.isArchive()) {
							IResource rsc = root.getUnderlyingResource();
							if (rsc == null) {
								indexJarFile(root, project.getName());
							} else {
								indexJarFile((IFile) rsc, project.getName());
							}
						}
					}
				}
			}
		} catch (JavaModelException e) { // cannot retrieve classpath info
		}
		request(new IndexAllProject(project, this));
	}

	/**
	 * Trigger addition of a resource to an index
	 * Note: the actual operation is performed in background
	 */
	public void indexJarFile(IFile resource, String projectName) {
		if (JavaCore.getPlugin() == null || this.workspace == null)
			return;
		request(new AddJarFileToIndex(resource, this, projectName));
	}

	/**
	 * Trigger addition of a resource to an index
	 * Note: the actual operation is performed in background
	 */
	public void indexJarFile(IPackageFragmentRoot root, String projectName) {
		if (JavaCore.getPlugin() == null || this.workspace == null)
			return;
		// we want to request a indexing only if this index doesn't already exist	
		request(new AddJarFileToIndex(root, this, projectName));
	}

	/**
	 * Advance to the next available job, once the current one has been completed.
	 * Note: clients awaiting until the job count is zero are still waiting at this point.
	 */
	protected synchronized void moveToNextJob() {

		// remember that one job was executed, and we will need to save indexes at some point
		needToSave = true;
		super.moveToNextJob();
	}

	/**
	 * No more job awaiting.
	 */
	protected void notifyIdle(long idlingTime) {
		if (idlingTime > 1000 && needToSave)
			saveIndexes();
	}

	/**
	 * Name of the background process
	 */
	public String processName() {
		return "Java indexing: " + IndexManager.class.getName();
	}

	/**
	 * Recreates the index for a given path, keeping the same read-write monitor.
	 * Returns the new empty index or null if it didn't exist before.
	 * Warning: Does not check whether index is consistent (not being used)
	 */
	public synchronized IIndex recreateIndex(IPath path) {
		IIndex index = (IIndex) indexes.get(path);
		if (index != null) {
			try {
				// Compute canonical path
				IPath canonicalPath = JavaProject.canonicalizedPath(path);
				// Add same index for given path and canonical path
				String indexPath = computeIndexName(canonicalPath.toOSString());
				ReadWriteMonitor monitor = (ReadWriteMonitor) monitors.remove(index);
				index =
					IndexFactory.newIndex(indexPath, "Index for " + canonicalPath.toOSString());
				index.empty();
				indexes.put(canonicalPath, index);
				indexes.put(path, index);
				monitors.put(index, monitor);
			} catch (IOException e) {
				// The file could not be created. Possible reason: the project has been deleted.
				return null;
			}
		}
		//System.out.println(" index name: " + path.toOSString() + " <----> " + index.getIndexFile().getName());	
		return index;
	}

	/**
	 * Trigger removal of a resource to an index
	 * Note: the actual operation is performed in background
	 */
	public void remove(String resourceName, IProject project) {
		request(new RemoveFromIndex(resourceName, project, this));
	}

	/**
	 * Flush current state
	 */
	public void reset() {

		super.reset();
		if (indexes != null) {
			indexes = new Hashtable(5);
			monitors = new Hashtable(5);
		}
		javaPluginLocation = null;
	}

	/**
	 * Commit all index memory changes to disk
	 */
	public void saveIndexes() {
		Enumeration indexList = indexes.elements();
		while (indexList.hasMoreElements()) {
			try {
				IIndex index = (IIndex) indexList.nextElement();
				if (index == null)
					continue; // index got deleted since acquired
				ReadWriteMonitor monitor = getMonitorFor(index);
				if (monitor == null)
					continue; // index got deleted since acquired
				try {
					monitor.enterWrite();
					if (IndexManager.VERBOSE)
						System.out.println("-> merging index : " + index.getIndexFile());
					index.save();
				} finally {
					monitor.exitWrite();
				}
			} catch (IOException e) {
				// Index file has been deleted
			}
		}
		needToSave = false;
	}

}
