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

public class IndexAllProject implements IJob, IJobConstants {
	IProject project;
	IndexManager manager;
public IndexAllProject(IProject project, IndexManager manager){
	this.project = project;
	this.manager = manager;	
}
public boolean belongsTo(String jobFamily){
	return jobFamily.equals(project.getName());
}
/**
 * Ensure consistency of a project index. Need to walk all nested resources,
 * and discover resources which have either been changed, added or deleted
 * since the index was produced.
 */
public boolean execute(){

	if (!project.isOpen()) return COMPLETE; // nothing to do
	
	IIndex index = manager.getIndex(project.getFullPath());
	if (index == null) return COMPLETE;
	ReadWriteMonitor monitor = manager.getMonitorFor(index);
	if (monitor == null) return COMPLETE; // index got deleted since acquired
	try {
		monitor.enterRead(); // ask permission to read

		/* if index has changed, commit these before querying */
		if (index.hasChanged()){
			try {
				monitor.exitRead(); // free read lock
				monitor.enterWrite(); // ask permission to write
				if (IndexManager.VERBOSE) System.out.println("-> merging index : "/*nonNLS*/+index.getIndexFile());
				index.save();
			} catch(IOException e){
				return FAILED;
			} finally {
				monitor.exitWrite(); // finished writing
				monitor.enterRead(); // reacquire read permission
			}
		}
		final String OK = "OK"/*nonNLS*/;
		final String DELETED = "DELETED"/*nonNLS*/;	
		final long indexLastModified = index.getIndexFile().lastModified();

		final Hashtable indexedFileNames = new Hashtable(100);
		IQueryResult[] results = index.queryInDocumentNames(""/*nonNLS*/); // all file names
		for (int i = 0, max = results == null ? 0 : results.length; i < max; i++){
			String fileName = results[i].getPath();
			indexedFileNames.put(fileName, DELETED);
		}
		project.accept(new IResourceVisitor(){
			public boolean visit(IResource resource) {
				if (resource.getType() == IResource.FILE) {
					String extension = resource.getFileExtension();
					if ((extension != null) && extension.equalsIgnoreCase("java"/*nonNLS*/)) {
						IPath path = resource.getLocation();
						if (path != null){
							File resourceFile = path.toFile();
							String name = new IFileDocument((IFile)resource).getName();
							if (indexedFileNames.get(name) == null){
								indexedFileNames.put(name, resource);
							} else {
								indexedFileNames.put(
									name, 
									resourceFile.lastModified() > indexLastModified
										? (Object)resource
										: (Object)OK);
							}
						}
					}
					return false;
				}
				return true;
			}});
		Enumeration names = indexedFileNames.keys();
		while (names.hasMoreElements()){
			String name = (String)names.nextElement();
			Object value = indexedFileNames.get(name);
			if (value instanceof IFile){
				manager.add((IFile)value);
			} else if (value == DELETED){
				manager.remove(name, project);
			}
		}
	} catch (CoreException e){
		return FAILED;
	} catch (IOException e){
		return FAILED;
	} finally {
		monitor.exitRead(); // free read lock
	}
	return COMPLETE;
}
public String toString(){
	return "indexing project "/*nonNLS*/ + project.getName();
}
}
