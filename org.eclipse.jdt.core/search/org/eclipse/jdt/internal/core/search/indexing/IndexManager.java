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
import org.eclipse.jdt.internal.core.search.IndexSelector;
import org.eclipse.jdt.internal.core.search.JavaWorkspaceScope;
import org.eclipse.jdt.internal.core.search.Util;
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
	Map indexes = new HashMap(5);

	/* read write monitors */
	private Map monitors = new HashMap(5);

	/* need to save ? */
	private boolean needToSave = false;
	private static final CRC32 checksumCalculator = new CRC32();
	private IPath javaPluginLocation = null;
	
	/* queue of awaiting index requests
	 * if null, requests are fired right away, otherwise this is done with fireIndexRequests() */

/**
 * Before processing all jobs, need to ensure that the indexes are up to date.
 */
public void activateProcessing() {
	try {
		Thread.currentThread().sleep(10000); // wait 10 seconds so as not to interfere with plugin startup
	} catch (InterruptedException ie) {
	}	
	checkIndexConsistency();
	super.activateProcessing();
}
/**
 * Trigger addition of a resource to an index
 * Note: the actual operation is performed in background
 */
public void add(IFile resource, IPath indexedContainer){
	if (JavaCore.getPlugin() == null || this.workspace == null) return;	
	String extension = resource.getFileExtension();
	if ("java".equals(extension)){ //$NON-NLS-1$
		AddCompilationUnitToIndex job = new AddCompilationUnitToIndex(resource, this, indexedContainer);
		if (this.awaitingJobsCount() < MAX_FILES_IN_MEMORY) {
			job.initializeContents();
		}
		request(job);
	} else if ("class".equals(extension)){ //$NON-NLS-1$
		AddClassFileToIndex job = new AddClassFileToIndex(resource, this, indexedContainer);
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

	if (VERBOSE) System.out.println("STARTING ("+ Thread.currentThread()+") - ensuring consistency"); //$NON-NLS-1$//$NON-NLS-2$

	boolean wasEnabled = isEnabled();	
	try {
		disable();

		if (this.workspace == null) return;

		IProject[] projects = this.workspace.getRoot().getProjects();
		for (int i = 0, max = projects.length; i < max; i++){
			IProject project = projects[i];
			// not only java project, given at startup nature may not have been set yet
			if (project.isOpen()) { 
				indexAll(project);
			}
		}
	} finally {
		if (wasEnabled) enable();
		if (VERBOSE) System.out.println("DONE ("+ Thread.currentThread()+") - ensuring consistency"); //$NON-NLS-1$//$NON-NLS-2$
	}
}
private String computeIndexName(String pathString) {
	byte[] pathBytes = pathString.getBytes();
	checksumCalculator.reset();
	checksumCalculator.update(pathBytes);
	String fileName = Long.toString(checksumCalculator.getValue()) + ".index"; //$NON-NLS-1$
	if (VERBOSE) System.out.println(" index name: " + pathString + " <----> " + fileName); //$NON-NLS-1$ //$NON-NLS-2$
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
}
/**
 * Remove the index from cache for a given project.
 * Passing null as a job family discards them all.
 */
public void discardJobsUntilNextProjectAddition(String jobFamily) {
	boolean wasEnabled = isEnabled();
	try {
		disable();

		// flush and compact awaiting jobs
		int loc = -1;
		boolean foundProjectAddition = false;
		for (int i = jobStart; i <= jobEnd; i++){
			IJob currentJob = awaitingJobs[i];
			awaitingJobs[i] = null;
			boolean discard = jobFamily == null;
			if (!discard && currentJob.belongsTo(jobFamily)){ // might discard
				if (!(foundProjectAddition || (foundProjectAddition = currentJob instanceof IndexAllProject))) {
					discard = true;
				}
			}
			if (discard) {
				currentJob.cancel();
				if (i == jobStart) {
					// wait until current active job has accepted the cancel
					while (thread != null && executing){
						try {
							Thread.currentThread().sleep(50);
						} catch(InterruptedException e){
						}
					}
				}
			} else {
				awaitingJobs[++loc] = currentJob;
			}
		}
		jobStart = 0;
		jobEnd = loc;
	} finally {
		if (wasEnabled)	enable();
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
			if (!mustCreate) return index;
			if (index == null) {
				// New index: add same index for given path and canonical path
				String indexPath = computeIndexName(canonicalPath.toOSString());
				index = IndexFactory.newIndex(indexPath, "Index for " + canonicalPath.toOSString()); //$NON-NLS-1$
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
public ReadWriteMonitor getMonitorFor(IIndex index){
	
	return (ReadWriteMonitor)monitors.get(index);
}
/**
 * Trigger addition of the entire content of a project
 * Note: the actual operation is performed in background 
 */
public void indexAll(IProject project) {
	if (JavaCore.getPlugin() == null || this.workspace == null) return;

	// Also request indexing of binaries on the classpath
	// determine the new children
	try {
		IJavaModel model = JavaModelManager.getJavaModel(this.workspace);
		IJavaProject javaProject = ((JavaModel) model).getJavaProject(project);	
		// only consider immediate libraries - each project will do the same
		IClasspathEntry[] entries = javaProject.getResolvedClasspath(true);	
		for (int i = 0; i < entries.length; i++) {
			IClasspathEntry entry= entries[i];
			if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
				this.indexLibrary(entry.getPath(), project);
			}
		}
	} catch(JavaModelException e){ // cannot retrieve classpath info
	}	
	this.request(new IndexAllProject(project, this));
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
protected void notifyIdle(long idlingTime){
	if (idlingTime > 1000 && needToSave) saveIndexes();
}
/**
 * Name of the background process
 */
public String processName(){
	return Util.bind("process.name"); //$NON-NLS-1$
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
			ReadWriteMonitor monitor = (ReadWriteMonitor)monitors.remove(index);
			index = IndexFactory.newIndex(indexPath, "Index for " + canonicalPath.toOSString()); //$NON-NLS-1$
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
public void remove(String resourceName, IPath indexedContainer){
	request(new RemoveFromIndex(resourceName, indexedContainer, this));
}
/**
 * Removes the index for a given path. 
 * This is a no-op if the index did not exist.
 */
public synchronized void removeIndex(IPath path) {
	IPath canonicalPath = JavaProject.canonicalizedPath(path);
	IIndex index = (IIndex)this.indexes.get(canonicalPath);
	if (index == null) return;
	index.getIndexFile().delete();
	this.indexes.remove(canonicalPath);
	this.monitors.remove(index);
}

/**
 * Flush current state
 */
public void reset(){

	super.reset();
	if (indexes != null){
		indexes = new HashMap(5);
		monitors = new HashMap(5);
	}
	javaPluginLocation = null;
}
/**
 * Commit all index memory changes to disk
 */
public void saveIndexes(){

	ArrayList indexList = new ArrayList();
	synchronized(this){
		for (Iterator iter = indexes.values().iterator(); iter.hasNext();){
			indexList.add(iter.next());
		}
	}

	for (Iterator iter = indexList.iterator(); iter.hasNext();){		
		IIndex index = (IIndex)iter.next();
		if (index == null) continue; // index got deleted since acquired
		ReadWriteMonitor monitor = getMonitorFor(index);
		if (monitor == null) continue; // index got deleted since acquired
		try {
			monitor.enterWrite();
			if (IndexManager.VERBOSE){
				if (index.hasChanged()){
					System.out.println("-> merging index : "+index.getIndexFile()); //$NON-NLS-1$
				}
			}
			try {
				index.save();
			} catch(IOException e){
				if (IndexManager.VERBOSE){
					e.printStackTrace();
				}
				//org.eclipse.jdt.internal.core.Util.log(e);
			}
		} finally {
			monitor.exitWrite();
		}
	}
	needToSave = false;
}

public void shutdown() {
	HashMap keepingIndexesPaths = new HashMap();
	IndexSelector indexSelector = new IndexSelector(new JavaWorkspaceScope(), null, this);
	IIndex[] selectedIndexes = indexSelector.getIndexes();
	for (int i = 0, max = selectedIndexes.length; i < max; i++) {
		String path = selectedIndexes[i].getIndexFile().getAbsolutePath();
		keepingIndexesPaths.put(path, path);
	}
	File indexesDirectory = new File(this.getJavaPluginWorkingLocation().toOSString());
	if (indexesDirectory.isDirectory()) {
		File[] indexesFiles = indexesDirectory.listFiles();
		for (int i = 0, indexesFilesLength = indexesFiles.length; i < indexesFilesLength; i++) {
			if (keepingIndexesPaths.get(indexesFiles[i].getAbsolutePath()) == null) {
				indexesFiles[i].delete();
			}
		}
		
	}
	super.shutdown();		
}

/**
 * Trigger addition of a library to an index
 * Note: the actual operation is performed in background
 */
public void indexLibrary(IPath path, IProject referingProject) {
	if (JavaCore.getPlugin() == null || this.workspace == null) return;

	Object target = JavaModel.getTarget(ResourcesPlugin.getWorkspace().getRoot(), path, true);
	
	IndexRequest request = null;
	if (target instanceof IFile) {
		request = new AddJarFileToIndex((IFile)target, this, referingProject.getName());
	} else if (target instanceof java.io.File) {
		request = new AddJarFileToIndex(path, this, referingProject.getName());
	} else if (target instanceof IFolder) {
		request = new IndexBinaryFolder((IFolder)target, this, referingProject);
	} else {
		return;
	}
	
	// check if the same request is not already in the queue
	for (int i = this.jobEnd; i >= this.jobStart; i--) {
		IJob awaiting = this.awaitingJobs[i];
		if (awaiting != null
			&& request.equals(awaiting) 
			&& (request.timeStamp == ((IndexRequest)awaiting).timeStamp)) {
				
			return;
		}
	}

	this.request(request);
}
}

