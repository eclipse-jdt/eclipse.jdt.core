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

import java.io.*;
import java.util.*;
import java.util.zip.CRC32;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.index.Index;
import org.eclipse.jdt.internal.core.search.JavaWorkspaceScope;
import org.eclipse.jdt.internal.core.search.PatternSearchJob;
import org.eclipse.jdt.internal.core.search.processing.IJob;
import org.eclipse.jdt.internal.core.search.processing.JobManager;
import org.eclipse.jdt.internal.core.util.SimpleLookupTable;
import org.eclipse.jdt.internal.core.util.Util;

public class IndexManager extends JobManager implements IIndexConstants {

	public SimpleLookupTable indexNames = new SimpleLookupTable();
	/*
	 * key = an IPath, value = an Index
	 */
	private Map indexes = new HashMap(5);

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
public void addBinary(IFile resource, IPath indexPath) {
	if (JavaCore.getPlugin() == null) return;	
	SearchParticipant participant = SearchEngine.getDefaultSearchParticipant();
	SearchDocument document = participant.getDocument(resource.getFullPath().toString());
	participant.scheduleDocumentIndexing(document, indexPath);
}
/**
 * Trigger addition of a resource to an index
 * Note: the actual operation is performed in background
 */
public void addSource(IFile resource, IPath indexPath) {
	if (JavaCore.getPlugin() == null) return;	
	SearchParticipant participant = SearchEngine.getDefaultSearchParticipant();
	SearchDocument document = participant.getDocument(resource.getFullPath().toString());
	participant.scheduleDocumentIndexing(document, indexPath);
}
/*
 * Removes unused indexes from disk.
 */
public void cleanUpIndexes() {
	SimpleLookupTable knownPaths = new SimpleLookupTable();
	IJavaSearchScope scope = new JavaWorkspaceScope();
	PatternSearchJob job = new PatternSearchJob(null, SearchEngine.getDefaultSearchParticipant(), scope, null);
	Index[] selectedIndexes = job.getIndexes(null);
	for (int j = 0, max = selectedIndexes.length; j < max; j++) {
		String path = selectedIndexes[j].getIndexFile().getAbsolutePath();
		knownPaths.put(path, path);
	}

	if (indexStates != null) {
		Object[] keys = indexStates.keyTable;
		for (int i = 0, l = keys.length; i < l; i++) {
			String key = (String) keys[i];
			if (key != null && !knownPaths.containsKey(key))
				updateIndexState(key, null);
		}
	}

	File indexesDirectory = new File(getJavaPluginWorkingLocation().toOSString());
	if (indexesDirectory.isDirectory()) {
		File[] indexesFiles = indexesDirectory.listFiles();
		if (indexesFiles != null) {
			for (int i = 0, indexesFilesLength = indexesFiles.length; i < indexesFilesLength; i++) {
				String fileName = indexesFiles[i].getAbsolutePath();
				if (!knownPaths.containsKey(fileName) && fileName.toLowerCase().endsWith(".index")) { //$NON-NLS-1$
					if (VERBOSE)
						Util.verbose("Deleting index file " + indexesFiles[i]); //$NON-NLS-1$
					indexesFiles[i].delete();
				}
			}
		}
	}
}
String computeIndexName(IPath path) {
	String name = (String) indexNames.get(path);
	if (name == null) {
		String pathString = path.toOSString();
		checksumCalculator.reset();
		checksumCalculator.update(pathString.getBytes());
		String fileName = Long.toString(checksumCalculator.getValue()) + ".index"; //$NON-NLS-1$
		if (VERBOSE)
			Util.verbose("-> index name for " + pathString + " is " + fileName); //$NON-NLS-1$ //$NON-NLS-2$
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
public synchronized Index getIndex(IPath path, boolean reuseExistingFile, boolean createIfMissing) {
	// Path is already canonical per construction
	Index index = (Index) indexes.get(path);
	if (index == null) {
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
				try {
					index = new Index(indexName, "Index for " + path.toOSString(), true /*reuse index file*/); //$NON-NLS-1$
					indexes.put(path, index);
					return index;
				} catch (IOException e) {
					// failed to read the existing file or its no longer compatible
					if (currentIndexState != REBUILDING_STATE) { // rebuild index if existing file is corrupt, unless the index is already being rebuilt
						if (VERBOSE)
							Util.verbose("-> cannot reuse existing index: "+indexName+" path: "+path.toOSString()); //$NON-NLS-1$ //$NON-NLS-2$
						rebuildIndex(indexName, path);
						return null;
					} else {
						index = null; // will fall thru to createIfMissing & create a empty index for the rebuild all job to populate
					}
				}
			}
			if (currentIndexState == SAVED_STATE) { // rebuild index if existing file is missing
				rebuildIndex(indexName, path);
				return null;
			}
		} 
		// index wasn't found on disk, consider creating an empty new one
		if (createIfMissing) {
			try {
				if (VERBOSE)
					Util.verbose("-> create empty index: "+indexName+" path: "+path.toOSString()); //$NON-NLS-1$ //$NON-NLS-2$
				index = new Index(indexName, "Index for " + path.toOSString(), false /*do not reuse index file*/); //$NON-NLS-1$
				indexes.put(path, index);
				return index;
			} catch (IOException e) {
				if (VERBOSE)
					Util.verbose("-> unable to create empty index: "+indexName+" path: "+path.toOSString()); //$NON-NLS-1$ //$NON-NLS-2$
				// The file could not be created. Possible reason: the project has been deleted.
				return null;
			}
		}
	}
	//System.out.println(" index name: " + path.toOSString() + " <----> " + index.getIndexFile().getName());	
	return index;
}
public synchronized Index getIndexForUpdate(IPath path, boolean reuseExistingFile, boolean createIfMissing) {
	String indexName = computeIndexName(path);
	if (getIndexStates().get(indexName) == REBUILDING_STATE)
		return getIndex(path, reuseExistingFile, createIfMissing);

	return null; // abort the job since the index has been removed from the REBUILDING_STATE
}
private SimpleLookupTable getIndexStates() {
	if (indexStates != null) return indexStates;

	this.indexStates = new SimpleLookupTable();
	char[] savedIndexNames = readIndexState();
	if (savedIndexNames.length > 0) {
		char[][] names = CharOperation.splitOn('\n', savedIndexNames);
		for (int i = 0, l = names.length; i < l; i++) {
			char[] name = names[i];
			if (name.length > 0)
				this.indexStates.put(new String(name), SAVED_STATE);
		}
	}
	return this.indexStates;
}
private IPath getJavaPluginWorkingLocation() {
	if (this.javaPluginLocation != null) return this.javaPluginLocation;

	return this.javaPluginLocation = JavaCore.getPlugin().getStateLocation();
}
public void indexDocument(SearchDocument searchDocument, SearchParticipant searchParticipant, Index index, IPath indexPath) throws IOException {
	try {
		((InternalSearchDocument) searchDocument).index = index;
		searchParticipant.indexDocument(searchDocument, indexPath);
	} finally {
		((InternalSearchDocument) searchDocument).index = null;
	}
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
		JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
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
	for (int i = this.jobEnd; i > this.jobStart; i--) // NB: don't check job at jobStart, as it may have already started (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=32488)
		if (request.equals(this.awaitingJobs[i])) return;
	this.request(request);
}
/**
 * Trigger addition of a library to an index
 * Note: the actual operation is performed in background
 */
public void indexLibrary(IPath path, IProject requestingProject) {
	// requestingProject is no longer used to cancel jobs but leave it here just in case
	if (JavaCore.getPlugin() == null) return;

	Object target = JavaModel.getTarget(ResourcesPlugin.getWorkspace().getRoot(), path, true);
	IndexRequest request = null;
	if (target instanceof IFile) {
		request = new AddJarFileToIndex((IFile)target, this);
	} else if (target instanceof java.io.File) {
		request = new AddJarFileToIndex(path, this);
	} else if (target instanceof IFolder) {
		request = new IndexBinaryFolder((IFolder)target, this);
	} else {
		return;
	}

	// check if the same request is not already in the queue
	for (int i = this.jobEnd; i > this.jobStart; i--)  // NB: don't check job at jobStart, as it may have already started (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=32488)
		if (request.equals(this.awaitingJobs[i])) return;
	this.request(request);
}
/**
 * Index the content of the given source folder.
 */
public void indexSourceFolder(JavaProject javaProject, IPath sourceFolder, char[][] inclusionPatterns, char[][] exclusionPatterns) {
	IProject project = javaProject.getProject();
	if (this.jobEnd > this.jobStart) {
		// check if a job to index the project is not already in the queue
		IndexRequest request = new IndexAllProject(project, this);
		for (int i = this.jobEnd; i > this.jobStart; i--) // NB: don't check job at jobStart, as it may have already started (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=32488)
			if (request.equals(this.awaitingJobs[i])) return;
	}

	this.request(new AddFolderToIndex(sourceFolder, project, inclusionPatterns, exclusionPatterns, this));
}
public void jobWasCancelled(IPath path) {
	Object o = this.indexes.get(path);
	if (o instanceof Index) {
		((Index) o).monitor = null;
		this.indexes.remove(path);
	}
	updateIndexState(computeIndexName(path), UNKNOWN_STATE);
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
/*
 * For debug purpose
 */
public Index peekAtIndex(IPath path) {
	return (Index) indexes.get(path);
}
/**
 * Name of the background process
 */
public String processName(){
	return Util.bind("process.name"); //$NON-NLS-1$
}
private void rebuildIndex(String indexName, IPath path) {
	IWorkspace workspace = ResourcesPlugin.getWorkspace();
	if (workspace == null) return;
	Object target = JavaModel.getTarget(workspace.getRoot(), path, true);
	if (target == null) return;

	if (VERBOSE)
		Util.verbose("-> request to rebuild index: "+indexName+" path: "+path.toOSString()); //$NON-NLS-1$ //$NON-NLS-2$

	updateIndexState(indexName, REBUILDING_STATE);
	IndexRequest request = null;
	if (target instanceof IProject) {
		IProject p = (IProject) target;
		if (JavaProject.hasJavaNature(p))
			request = new IndexAllProject(p, this);
	} else if (target instanceof IFolder) {
		request = new IndexBinaryFolder((IFolder) target, this);
	} else if (target instanceof IFile) {
		request = new AddJarFileToIndex((IFile) target, this);
	} else if (target instanceof java.io.File) {
		request = new AddJarFileToIndex(path, this);
	}
	if (request != null)
		request(request);
}
/**
 * Recreates the index for a given path, keeping the same read-write monitor.
 * Returns the new empty index or null if it didn't exist before.
 * Warning: Does not check whether index is consistent (not being used)
 */
public synchronized Index recreateIndex(IPath path) {
	// only called to over write an existing cached index...
	try {
		Index index = (Index) this.indexes.get(path);
		ReadWriteMonitor monitor = index == null ? null : index.monitor;

		// Path is already canonical
		String indexPath = computeIndexName(path);
		if (VERBOSE)
			Util.verbose("-> recreating index: "+indexPath+" for path: "+path.toOSString()); //$NON-NLS-1$ //$NON-NLS-2$
		index = new Index(indexPath, "Index for " + path.toOSString(), false /*reuse index file*/); //$NON-NLS-1$
		indexes.put(path, index);
		index.monitor = monitor;
		return index;
	} catch (IOException e) {
		// The file could not be created. Possible reason: the project has been deleted.
		if (VERBOSE) {
			Util.verbose("-> failed to recreate index for path: "+path.toOSString()); //$NON-NLS-1$ //$NON-NLS-2$
			e.printStackTrace();
		}
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
		Util.verbose("removing index " + path); //$NON-NLS-1$
	String indexName = computeIndexName(path);
	File indexFile = new File(indexName);
	if (indexFile.exists())
		indexFile.delete();
	Object o = this.indexes.get(path);
	if (o instanceof Index)
		((Index) o).monitor = null;
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
public void removeSourceFolderFromIndex(JavaProject javaProject, IPath sourceFolder, char[][] inclusionPatterns, char[][] exclusionPatterns) {
	IProject project = javaProject.getProject();
	if (this.jobEnd > this.jobStart) {
		// check if a job to index the project is not already in the queue
		IndexRequest request = new IndexAllProject(project, this);
		for (int i = this.jobEnd; i > this.jobStart; i--) // NB: don't check job at jobStart, as it may have already started (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=32488)
			if (request.equals(this.awaitingJobs[i])) return;
	}

	this.request(new RemoveFolderFromIndex(sourceFolder, inclusionPatterns, exclusionPatterns, project, this));
}
/**
 * Flush current state
 */
public synchronized void reset() {
	super.reset();
	if (this.indexes != null) {
		this.indexes = new HashMap(5);
		this.indexStates = null;
	}
	this.indexNames = new SimpleLookupTable();
	this.javaPluginLocation = null;
}
public void saveIndex(Index index) throws IOException {
	// must have permission to write from the write monitor
	if (index.hasChanged()) {
		if (VERBOSE)
			Util.verbose("-> saving index " + index.getIndexFile()); //$NON-NLS-1$
		index.save();
	}
	String indexName = index.getIndexFile().getPath();
	if (this.jobEnd > this.jobStart) {
		Object indexPath = indexNames.keyForValue(indexName);
		if (indexPath != null) {
			for (int i = this.jobEnd; i > this.jobStart; i--) { // skip the current job
				IJob job = this.awaitingJobs[i];
				if (job instanceof IndexRequest)
					if (((IndexRequest) job).containerPath.equals(indexPath)) return;
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
			if (o instanceof Index)
				toSave.add(o);
		}
	}

	boolean allSaved = true;
	for (int i = 0, length = toSave.size(); i < length; i++) {
		Index index = (Index) toSave.get(i);
		ReadWriteMonitor monitor = index.monitor;
		if (monitor == null) continue; // index got deleted since acquired
		try {
			// take read lock before checking if index has changed
			// don't take write lock yet since it can cause a deadlock (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=50571)
			monitor.enterRead(); 
			if (index.hasChanged()) {
				if (monitor.exitReadEnterWrite()) {
					try {
						saveIndex(index);
					} catch(IOException e) {
						if (VERBOSE) {
							Util.verbose("-> got the following exception while saving:", System.err); //$NON-NLS-1$
							e.printStackTrace();
						}
						allSaved = false;
					} finally {
						monitor.exitWriteEnterRead();
					}
				} else {
					allSaved = false;
				}
			}
		} finally {
			monitor.exitRead();
		}
	}
	this.needToSave = !allSaved;
}
public void scheduleDocumentIndexing(final SearchDocument searchDocument, final IPath indexPath, final SearchParticipant searchParticipant) {
	request(new IndexRequest(indexPath, this) {
		public boolean execute(IProgressMonitor progressMonitor) {
			if (this.isCancelled || progressMonitor != null && progressMonitor.isCanceled()) return true;
			
			/* ensure no concurrent write access to index */
			Index index = getIndex(indexPath, true, /*reuse index file*/ true /*create if none*/);
			if (index == null) return true;
			ReadWriteMonitor monitor = index.monitor;
			if (monitor == null) return true; // index got deleted since acquired
			
			try {
				monitor.enterWrite(); // ask permission to write
				indexDocument(searchDocument, searchParticipant, index, indexPath);
			} catch (IOException e) {
				if (JobManager.VERBOSE) {
					Util.verbose("-> failed to index " + searchDocument.getPath() + " because of the following exception:", System.err); //$NON-NLS-1$ //$NON-NLS-2$
					e.printStackTrace();
				}
				return false;
			} finally {
				monitor.exitWrite(); // free write lock
			}
			return true;
		}
		public String toString() {
			return "indexing " + searchDocument.getPath(); //$NON-NLS-1$
		}
	});
}

public String toString() {
	StringBuffer buffer = new StringBuffer(10);
	buffer.append(super.toString());
	buffer.append("In-memory indexes:\n"); //$NON-NLS-1$
	int count = 0;
	for (Iterator iter = this.indexes.values().iterator(); iter.hasNext();) {
		buffer.append(++count).append(" - ").append(iter.next().toString()).append('\n'); //$NON-NLS-1$
	}
	return buffer.toString();
}

private char[] readIndexState() {
	try {
		return org.eclipse.jdt.internal.compiler.util.Util.getFileCharContent(savedIndexNamesFile, null);
	} catch (IOException ignored) {
		if (VERBOSE)
			Util.verbose("Failed to read saved index file names"); //$NON-NLS-1$
		return new char[0];
	}
}
private synchronized void updateIndexState(String indexName, Integer indexState) {
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
		Object[] keys = indexStates.keyTable;
		Object[] states = indexStates.valueTable;
		for (int i = 0, l = states.length; i < l; i++) {
			if (states[i] == SAVED_STATE) {
				writer.write((String) keys[i]);
				writer.write('\n');
			}
		}
	} catch (IOException ignored) {
		if (VERBOSE)
			Util.verbose("Failed to write saved index file names", System.err); //$NON-NLS-1$
	} finally {
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
	if (VERBOSE) {
		String state = "?"; //$NON-NLS-1$
		if (indexState == SAVED_STATE) state = "SAVED"; //$NON-NLS-1$
		else if (indexState == UPDATING_STATE) state = "UPDATING"; //$NON-NLS-1$
		else if (indexState == UNKNOWN_STATE) state = "UNKNOWN"; //$NON-NLS-1$
		else if (indexState == REBUILDING_STATE) state = "REBUILDING"; //$NON-NLS-1$
		Util.verbose("-> index state updated to: " + state + " for: "+indexName); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
}
