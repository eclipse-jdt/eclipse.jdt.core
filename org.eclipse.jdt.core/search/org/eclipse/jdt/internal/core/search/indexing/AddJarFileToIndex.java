package org.eclipse.jdt.internal.core.search.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.internal.core.search.processing.*;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.index.impl.*;

import java.io.*;
import java.util.zip.*;
import java.util.*;

import org.eclipse.jdt.internal.core.Util;

class AddJarFileToIndex implements IJob, IJobConstants {

	IndexManager manager;
	String projectName;
	IFile resource;
	private String toString;
	IPath path;

public AddJarFileToIndex(IFile resource, IndexManager manager, String projectName) {
	this.resource = resource;
	this.path = resource.getFullPath();
	this.manager = manager;
	this.projectName = projectName;
}
public boolean belongsTo(String jobFamily){
	return jobFamily.equals(projectName);
}
public boolean execute() {
	try {
		if (this.resource != null) {
			if (!this.resource.isLocal(IResource.DEPTH_ZERO)) {
				return FAILED;
			}
		}
		IPath indexedPath = this.path;
		// if index already cached, then do not perform any check
		IIndex index = (IIndex) manager.getIndex(indexedPath, false);
		if (index != null) return COMPLETE;
		
		index = manager.getIndex(indexedPath);
		if (index == null) return COMPLETE;
		ReadWriteMonitor monitor = manager.getMonitorFor(index);
		if (monitor == null) return COMPLETE; // index got deleted since acquired
		ZipFile zip = null;
		try {
			// this path will be a relative path to the workspace in case the zipfile in the workspace otherwise it will be a path in the
			// local file system
			Path zipFilePath = null;
			
			monitor.enterWrite(); // ask permission to write
			if (resource != null) {
				IPath location = this.resource.getLocation();
				if (location == null) return FAILED;
				zip = new ZipFile(location.toFile());
				zipFilePath = (Path) this.resource.getFullPath().makeRelative(); // absolute path relative to the workspace
			} else {
				zip = new ZipFile(this.path.toFile());
				zipFilePath = (Path) this.path; // absolute path relative to the local file system
				// make it a canonical path to avoid duplicate entries
				zipFilePath = (Path)JavaProject.canonicalizedPath(zipFilePath);
			}
				
			if (JobManager.VERBOSE)
				System.out.println("INDEX : " + zip.getName()); //$NON-NLS-1$
			long initialTime = System.currentTimeMillis();

			final Hashtable indexedFileNames = new Hashtable(100);
			IQueryResult[] results = index.queryInDocumentNames(""); // all file names //$NON-NLS-1$
			int resultLength = results == null ? 0 : results.length;
			if (resultLength != 0) {
				/* check integrity of the existing index file
				 * if the length is equal to 0, we want to index the whole jar again
				 * If not, then we want to check that there is no missing entry, if
				 * one entry is missing then we 
				 */ 
				for (int i = 0; i < resultLength; i++) {
					String fileName = results[i].getPath();
					indexedFileNames.put(fileName, fileName);
				}
				boolean needToReindex = false;
				for (Enumeration e = zip.entries(); e.hasMoreElements();) {
					// iterate each entry to index it
					ZipEntry ze = (ZipEntry) e.nextElement();
					if (Util.isClassFileName(ze.getName())) {
						JarFileEntryDocument entryDocument = new JarFileEntryDocument(ze, null, zipFilePath);
						if (indexedFileNames.remove(entryDocument.getName()) == null) {
							needToReindex = true;
							break;
						}
					}
				}
				if (!needToReindex && indexedFileNames.size() == 0) {
					return COMPLETE;
				}
			}

			/*
			 * Index the jar for the first time or reindex the jar in case the previous index file has been corrupted
			 */
			if (index != null) {
				// index already existed: recreate it so that we forget about previous entries
				index = manager.recreateIndex(indexedPath);
			}
			for (Enumeration e = zip.entries(); e.hasMoreElements();) {
				// iterate each entry to index it
				ZipEntry ze = (ZipEntry) e.nextElement();
				if (Util.isClassFileName(ze.getName())) {
					InputStream zipInputStream = zip.getInputStream(ze);
					byte classFileBytes[] = new byte[(int) ze.getSize()];
					int length = classFileBytes.length;
					int len = 0;
					int readSize = 0;
					while ((readSize != -1) && (len != length)) {
						readSize = zipInputStream.read(classFileBytes, len, length - len);
						len += readSize;
					}
					zipInputStream.close();
					// Add the name of the file to the index
					index.add(
						new JarFileEntryDocument(ze, classFileBytes, zipFilePath), 
						new BinaryIndexer()); 
				}
			}
			if (JobManager.VERBOSE)
				System.out.println(
					"INDEX : " //$NON-NLS-1$
						+ zip.getName()
						+ " COMPLETE in " //$NON-NLS-1$
						+ (System.currentTimeMillis() - initialTime)
						+ " ms");  //$NON-NLS-1$
		} finally {
			if (zip != null)
				zip.close();
			monitor.exitWrite(); // free write lock
		}
	} catch (IOException e) {
		return FAILED;
	}
	return COMPLETE;
}
/**
 * Insert the method's description here.
 * Creation date: (10/10/00 1:27:18 PM)
 * @return java.lang.String
 */
public String toString() {
	if (toString == null) {
		if (resource != null) {
			IPath location = resource.getLocation();
			if (location == null){
				toString = "indexing "; //$NON-NLS-1$
			} else {
			    toString = "indexing " + location.toFile().toString(); //$NON-NLS-1$
			}
		} else {
		    toString = "indexing " + this.path.toFile().toString(); //$NON-NLS-1$
		}
	}
	return toString;
}

public AddJarFileToIndex(IPath path, IndexManager manager, String projectName) {
	// external JAR scenario - no resource
	this.path = path;
	this.manager = manager;
	this.projectName = projectName;
}
}
