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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.CRC32;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.index.IIndex;
import org.eclipse.jdt.internal.core.index.impl.Index;
import org.eclipse.jdt.internal.core.search.IndexSelector;
import org.eclipse.jdt.internal.core.search.JavaWorkspaceScope;
import org.eclipse.jdt.internal.core.search.Util;
import org.eclipse.jdt.internal.core.search.processing.IJob;
import org.eclipse.jdt.internal.core.search.processing.JobManager;
import org.eclipse.jdt.internal.core.util.SimpleLookupTable;

public class IndexManager extends JobManager implements IIndexConstants {
	/* number of file contents in memory */
	public static int MAX_FILES_IN_MEMORY = 0;

	public IWorkspace workspace;
	public SimpleLookupTable indexNames = new SimpleLookupTable();
	private Map indexes = new HashMap(5);

	/* read write monitors */
	private Map monitors = new HashMap(5);

	/* need to save ? */
	private boolean needToSave = false;
	private static final CRC32 checksumCalculator = new CRC32();
	private IPath javaPluginLocation = null;

	/* can only replace a current state if its less than the new one */
	private SimpleLookupTable indexStates = null;
	private File savedIndexNamesFile =
		new File(getJavaPluginWorkingLocation().append("savedIndexNames.txt").toOSString()); //$NON-NLS-1$
	public static Integer SAVED_STATE = new Integer(0);
	public static Integer UPDATING_STATE = new Integer(1);
	public static Integer UNKNOWN_STATE = new Integer(2);
	public static Integer REBUILDING_STATE = new Integer(3);

public synchronized void aboutToUpdateIndex(IPath path, Integer newIndexState) {
	// newIndexState is either UPDATING_STATE or REBUILDING_STATE
	// must tag the index as inconsistent, in case we exit before the update job is started
	String indexName = computeIndexName(path);
	Object state = getIndexStates().get(indexName);
	Integer currentIndexState = state == null ? UNKNOWN_STATE : (Integer) state;
	if (currentIndexState.equals(REBUILDING_STATE)) return; // already rebuilding the index

	int compare = newIndexState.compareTo(currentIndexState);
	if (compare > 0) {
		// so UPDATING_STATE replaces SAVED_STATE and REBUILDING_STATE replaces everything
		updateIndexState(indexName, newIndexState);
	} else if (compare < 0 && this.indexes.get(path) == null) {
		// if already cached index then there is nothing more to do
		rebuildIndex(indexName, path);
	}
}
/**
 * Trigger addition of a resource to an index
 * Note: the actual operation is performed in background
 */
public void addBinary(IFile resource, IPath indexedContainer){
	if (JavaCore.getPlugin() == null) return;	
	AddClassFileToIndex job = new AddClassFileToIndex(resource, indexedContainer, this);
	if (this.awaitingJobsCount() < MAX_FILES_IN_MEMORY) {
		// reduces the chance that the file is open later on, preventing it from being deleted
		if (!job.initializeContents()) return;
	}
	request(job);
}
/**
 * Trigger addition of a resource to an index
 * Note: the actual operation is performed in background
 */
public void addSource(IFile resource, IPath indexedContainer){
	if (JavaCore.getPlugin() == null) return;	
	AddCompilationUnitToIndex job = new AddCompilationUnitToIndex(resource, indexedContainer, this);
	if (this.awaitingJobsCount() < MAX_FILES_IN_MEMORY) {
		// reduces the chance that the file is open later on, preventing it from being deleted
		if (!job.initializeContents()) return;
	}
	request(job);
}
String computeIndexName(IPath path) {
	String name = (String) indexNames.get(path);
	if (name == null) {
		String pathString = path.toOSString();
		checksumCalculator.reset();
		checksumCalculator.update(pathString.getBytes());
		String fileName = Long.toString(checksumCalculator.getValue()) + ".index"; //$NON-NLS-1$
		if (VERBOSE)
			JobManager.verbose("-> index name for " + pathString + " is " + fileName); //$NON-NLS-1$ //$NON-NLS-2$
		name = getJavaPluginWorkingLocation().append(fileName).toOSString();
		indexNames.put(path, name);
	}
	return name;
}
/**
 * Returns the index for a given project, according to the following algorithm:
 * - if index is already in memory: answers this one back
 * - if (reuseExistingFile) then read it and return this index and record it in memory
 * - if (createIfMissing) then create a new empty index and record it in memory
 * 
 * Warning: Does not check whether index is consistent (not being used)
 */
public synchronized IIndex getIndex(IPath path, boolean reuseExistingFile, boolean createIfMissing) {
	// Path is already canonical per construction
	IIndex index = (IIndex) indexes.get(path);
	if (index == null) {
		try {
			String indexName = computeIndexName(path);
			Object state = getIndexStates().get(indexName);
			Integer currentIndexState = state == null ? UNKNOWN_STATE : (Integer) state;
			if (currentIndexState == UNKNOWN_STATE) {
				// should only be reachable for query jobs
				// IF you put an index in the cache, then AddJarFileToIndex fails because it thinks there is nothing to do
				rebuildIndex(indexName, path);
				return null;
			}

			// index isn't cached, consider reusing an existing index file
			if (reuseExistingFile) {
				File indexFile = new File(indexName);
				if (indexFile.exists()) { // check before creating index so as to avoid creating a new empty index if file is missing
					index = new Index(indexName, "Index for " + path.toOSString(), true /*reuse index file*/); //$NON-NLS-1$
					if (index != null) {
						indexes.put(path, index);
						monitors.put(index, new ReadWriteMonitor());
						return index;
					}
				} else if (currentIndexState == SAVED_STATE) {
					rebuildIndex(indexName, path);
					return null;
				}
			} 
			// index wasn't found on disk, consider creating an empty new one
			if (createIfMissing) {
				index = new Index(indexName, "Index for " + path.toOSString(), false /*do not reuse index file*/); //$NON-NLS-1$
				if (index != null) {
					indexes.put(path, index);
					monitors.put(index, new ReadWriteMonitor());
					return index;
				}
			}
		} catch (IOException e) {
			// The file could not be created. Possible reason: the project has been deleted.
			return null;
		}
	}
	//System.out.println(" index name: " + path.toOSString() + " <----> " + index.getIndexFile().getName());	
	return index;
}
private SimpleLookupTable getIndexStates() {
	if (indexStates != null) return indexStates;

	this.indexStates = new SimpleLookupTable();
	char[] savedIndexNames = readIndexState();
	if (savedIndexNames.length > 0) {
		char[][] names = CharOperation.splitOn('\n', savedIndexNames);
		for (int i = 0, l = names.length; i < l; i++)
			this.indexStates.put(new String(names[i]), SAVED_STATE);
	}
	return this.indexStates;
}
private IPath getJavaPluginWorkingLocation() {
	if (this.javaPluginLocation != null) return this.javaPluginLocation;

	return this.javaPluginLocation = JavaCore.getPlugin().getStateLocation();
}
/**
 * Index access is controlled through a read-write monitor so as
 * to ensure there is no concurrent read and write operations
 * (only concurrent reading is allowed).
 */
public ReadWriteMonitor getMonitorFor(IIndex index){
	return (ReadWriteMonitor) monitors.get(index);
}
/**
 * Trigger addition of the entire content of a project
 * Note: the actual operation is performed in background 
 */
public void indexAll(IProject project) {
	if (JavaCore.getPlugin() == null) return;

	// Also request indexing of binaries on the classpath
	// determine the new children
	try {
		JavaModel model = (JavaModel) JavaModelManager.getJavaModelManager().getJavaModel();
		IJavaProject javaProject = model.getJavaProject(project);	
		// only consider immediate libraries - each project will do the same
		// NOTE: force to resolve CP variables before calling indexer - 19303, so that initializers
		// will be run in the current thread.
		IClasspathEntry[] entries = javaProject.getResolvedClasspath(true);	
		for (int i = 0; i < entries.length; i++) {
			IClasspathEntry entry= entries[i];
			if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY)
				this.indexLibrary(entry.getPath(), project);
		}
	} catch(JavaModelException e){ // cannot retrieve classpath info
	}

	// check if the same request is not already in the queue
	IndexRequest request = new IndexAllProject(project, this);
	for (int i = this.jobEnd; i >= this.jobStart; i--)
		if (request.equals(this.awaitingJobs[i])) return;
	this.request(request);
}
/**
 * Trigger addition of a library to an index
 * Note: the actual operation is performed in background
 */
public void indexLibrary(IPath path, IProject referingProject) {
	if (JavaCore.getPlugin() == null) return;

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
	for (int i = this.jobEnd; i >= this.jobStart; i--)
		if (request.equals(this.awaitingJobs[i])) return;
	this.request(request);
}
/**
 * Index the content of the given source folder.
 */
public void indexSourceFolder(JavaProject javaProject, IPath sourceFolder, final char[][] exclusionPattern) {
	IProject project = javaProject.getProject();
	final IPath container = project.getFullPath();
	IContainer folder = container.equals(sourceFolder)
		? (IContainer) project
		 : (IContainer) ResourcesPlugin.getWorkspace().getRoot().getFolder(sourceFolder);
	try {
		folder.accept(new IResourceVisitor() {
			/*
			 * @see IResourceVisitor#visit(IResource)
			 */
			public boolean visit(IResource resource) throws CoreException {
				if (resource instanceof IFile) {
					if (org.eclipse.jdt.internal.core.Util.isJavaFileName(resource.getName()) 
							&& !org.eclipse.jdt.internal.core.Util.isExcluded(resource, exclusionPattern)) {
						addSource((IFile)resource, container);
					}
					return false;
				}
				return true;
			}
		});
	} catch (CoreException e) {
		// Folder does not exist.
		// It will be indexed only when DeltaProcessor detects its addition
	}
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
private void rebuildIndex(String indexName, IPath path) {
	Object target = JavaModel.getTarget(ResourcesPlugin.getWorkspace().getRoot(), path, true);
	if (target == null) return;

	updateIndexState(indexName, REBUILDING_STATE);
	IndexRequest request = null;
	if (target instanceof IProject) {
		IProject p = (IProject) target;
		if (JavaProject.hasJavaNature(p))
			request = new IndexAllProject(p, this);
	} else if (target instanceof IFolder) {
		IFolder folder = (IFolder) target;
		request = new IndexBinaryFolder(folder, this, folder.getProject());
	} else if (target instanceof IFile) {
		request = new AddJarFileToIndex((IFile) target, this, ""); //$NON-NLS-1$
	} else if (target instanceof java.io.File) {
		request = new AddJarFileToIndex(path, this, ""); //$NON-NLS-1$
	}
	if (request != null)
		request(request);
}
/**
 * Recreates the index for a given path, keeping the same read-write monitor.
 * Returns the new empty index or null if it didn't exist before.
 * Warning: Does not check whether index is consistent (not being used)
 */
public synchronized IIndex recreateIndex(IPath path) {
	// only called to over write an existing cached index...
	try {
		IIndex index = (IIndex) this.indexes.get(path);
		ReadWriteMonitor monitor = (ReadWriteMonitor) this.monitors.remove(index);

		// Path is already canonical
		String indexPath = computeIndexName(path);
		index = new Index(indexPath, "Index for " + path.toOSString(), true /*reuse index file*/); //$NON-NLS-1$
		index.empty();
		indexes.put(path, index);
		monitors.put(index, monitor);
		return index;
	} catch (IOException e) {
		// The file could not be created. Possible reason: the project has been deleted.
		return null;
	}
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
	if (VERBOSE)
		JobManager.verbose("removing index " + path); //$NON-NLS-1$
	String indexName = computeIndexName(path);
	File indexFile = new File(indexName);
	if (indexFile.exists())
		indexFile.delete();
	Object o = this.indexes.get(path);
	if (o instanceof IIndex)
		this.monitors.remove(o);
	this.indexes.remove(path);
	updateIndexState(indexName, null);
}
/**
 * Removes all indexes whose paths start with (or are equal to) the given path. 
 */
public synchronized void removeIndexFamily(IPath path) {
	// only finds cached index files... shutdown removes all non-cached index files
	ArrayList toRemove = null;
	Iterator iterator = this.indexes.keySet().iterator();
	while (iterator.hasNext()) {
		IPath indexPath = (IPath) iterator.next();
		if (path.isPrefixOf(indexPath)) {
			if (toRemove == null)
				toRemove = new ArrayList();
			toRemove.add(indexPath);
		}
	}
	if (toRemove != null)
		for (int i = 0, length = toRemove.size(); i < length; i++)
			this.removeIndex((IPath) toRemove.get(i));
}
/**
 * Remove the content of the given source folder from the index.
 */
public void removeSourceFolderFromIndex(JavaProject javaProject, IPath sourceFolder) {
	this.request(new RemoveFolderFromIndex(sourceFolder.toString(), javaProject.getProject().getFullPath(), this));
}
/**
 * Flush current state
 */
public void reset() {
	super.reset();
	if (this.indexes != null) {
		this.indexes = new HashMap(5);
		this.monitors = new HashMap(5);
		this.indexStates = null;
	}
	this.indexNames = new SimpleLookupTable();
	this.javaPluginLocation = null;
}
public void saveIndex(IIndex index) throws IOException {
	// must have permission to write from the write monitor
	if (index.hasChanged()) {
		if (VERBOSE)
			JobManager.verbose("-> merging index " + index.getIndexFile()); //$NON-NLS-1$
		index.save();
	}
	String indexName = index.getIndexFile().getPath();
	if (this.jobEnd > this.jobStart) {
		Object indexPath = indexNames.keyForValue(indexName);
		if (indexPath != null) {
			for (int i = this.jobEnd; i > this.jobStart; i--) { // skip the current job
				IJob job = this.awaitingJobs[i];
				if (job instanceof IndexRequest)
					if (((IndexRequest) job).indexPath.equals(indexPath)) return;
			}
		}
	}
	updateIndexState(indexName, SAVED_STATE);
}
/**
 * Commit all index memory changes to disk
 */
public void saveIndexes() {
	// only save cached indexes... the rest were not modified
	ArrayList toSave = new ArrayList();
	synchronized(this) {
		for (Iterator iter = this.indexes.values().iterator(); iter.hasNext();) {
			Object o = iter.next();
			if (o instanceof IIndex)
				toSave.add(o);
		}
	}

	for (int i = 0, length = toSave.size(); i < length; i++) {
		IIndex index = (IIndex) toSave.get(i);
		ReadWriteMonitor monitor = getMonitorFor(index);
		if (monitor == null) continue; // index got deleted since acquired
		try {
			monitor.enterWrite();
			try {
				saveIndex(index);
			} catch(IOException e){
				if (VERBOSE) {
					JobManager.verbose("-> got the following exception while merging:"); //$NON-NLS-1$
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
	if (VERBOSE)
		JobManager.verbose("Shutdown"); //$NON-NLS-1$

	IndexSelector indexSelector = new IndexSelector(new JavaWorkspaceScope(), null, this);
	IIndex[] selectedIndexes = indexSelector.getIndexes();
	SimpleLookupTable knownPaths = new SimpleLookupTable();
	for (int i = 0, max = selectedIndexes.length; i < max; i++) {
		String path = selectedIndexes[i].getIndexFile().getAbsolutePath();
		knownPaths.put(path, path);
	}

	File indexesDirectory = new File(getJavaPluginWorkingLocation().toOSString());
	if (indexesDirectory.isDirectory()) {
		File[] indexesFiles = indexesDirectory.listFiles();
		for (int i = 0, indexesFilesLength = indexesFiles.length; i < indexesFilesLength; i++) {
			String fileName = indexesFiles[i].getAbsolutePath();
			if (!knownPaths.containsKey(fileName) && fileName.toLowerCase().endsWith(".index")) { //$NON-NLS-1$
				if (VERBOSE)
					JobManager.verbose("Deleting index file " + indexesFiles[i]); //$NON-NLS-1$
				indexesFiles[i].delete();
			}
		}
		
	}

	super.shutdown();		
}
private char[] readIndexState() {
	try {
		return org.eclipse.jdt.internal.compiler.util.Util.getFileCharContent(savedIndexNamesFile, null);
	} catch (IOException ignored) {
		if (VERBOSE)
			JobManager.verbose("Failed to read saved index file names"); //$NON-NLS-1$
		return new char[0];
	}
}
private void updateIndexState(String indexName, Integer indexState) {
	getIndexStates(); // ensure the states are initialized
	if (indexState != null) {
		if (indexState.equals(indexStates.get(indexName))) return; // not changed
		indexStates.put(indexName, indexState);
	} else {
		if (!indexStates.containsKey(indexName)) return; // did not exist anyway
		indexStates.removeKey(indexName);
	}

	BufferedWriter writer = null;
	try {
		writer = new BufferedWriter(new FileWriter(savedIndexNamesFile));
		Object[] indexNames = indexStates.keyTable;
		Object[] states = indexStates.valueTable;
		for (int i = 0, l = states.length; i < l; i++) {
			if (states[i] == SAVED_STATE) {
				writer.write((String) indexNames[i]);
				if (i > 0)
					writer.write('\n');
			}
		}
	} catch (IOException ignored) {
		if (VERBOSE)
			JobManager.verbose("Failed to write saved index file names"); //$NON-NLS-1$
	} finally {
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {}
		}
	}
	if (VERBOSE)
		JobManager.verbose("Saved indexes are now : " + new String(readIndexState())); //$NON-NLS-1$
}
}