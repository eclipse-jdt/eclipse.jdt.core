package org.eclipse.jdt.internal.core.search.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;

import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.internal.core.search.processing.*;
import org.eclipse.jdt.internal.core.index.impl.*;

import java.io.*;

import org.eclipse.core.runtime.IPath;

class AddClassFileToIndex implements IJob, IJobConstants {
	IFile resource;
	IndexManager manager;
	byte[] contents;
public AddClassFileToIndex(IFile resource, IndexManager manager){
	this.resource = resource;
	this.manager = manager;
}
public boolean belongsTo(String jobFamily){
	return jobFamily.equals(resource.getProject().getName());
}
public boolean execute(){
	try {
		IProject project = resource.getProject();
		IIndex index = manager.getIndex(project.getFullPath());
		if (!resource.isLocal(IResource.DEPTH_ZERO)){
			return FAILED;
		}
		/* ensure no concurrent write access to index */
		if (index == null) return COMPLETE;		
		ReadWriteMonitor monitor = manager.getMonitorFor(index);
		if (monitor == null) return COMPLETE; // index got deleted since acquired
		try {
			monitor.enterWrite(); // ask permission to write
			byte[] contents = this.getContents();
			if (contents == null) return FAILED;
			index.add(new IFileDocument(resource, contents), new BinaryIndexer());
		} finally {
			monitor.exitWrite(); // free write lock
		}
	} catch (IOException e){
		return FAILED;
	}
	return COMPLETE;
}
private byte[] getContents() {
	if (this.contents == null) this.initializeContents();
	return this.contents;
}
public void initializeContents() {
	if (!resource.isLocal(IResource.DEPTH_ZERO)) {
		return;
	} else {
		try {
			IPath location = resource.getLocation();
			if (location != null){;
				this.contents = Util.getFileByteContent(resource.getLocation().toFile());
			}
		} catch (IOException e) {
		}
	}
}
public String toString(){
	return "indexing " + resource.getName();
}
}
