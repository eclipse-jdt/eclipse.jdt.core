package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.*;

import java.io.*;
import java.util.zip.*;

/**
 * A jar entry that represents a non-java resource found in a JAR.
 *
 * @see IStorage
 */
public class JarEntryFile extends PlatformObject implements IStorage {
	private String entryName;
	private String zipName;
	private IPath path;
	
	public JarEntryFile(String entryName, String zipName){
		this.entryName = entryName;
		this.zipName = zipName;
		this.path = new Path(this.entryName);
	}
public InputStream getContents() throws CoreException {

	try {
		ZipFile zipFile = new ZipFile(this.zipName); 
		ZipEntry zipEntry = zipFile.getEntry(this.entryName);
		if (zipEntry == null){
			throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_PATH, this.entryName));
		}
		return zipFile.getInputStream(zipEntry);
	} catch (IOException e){
		throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
	}
}
/**
 * @see IStorage#getFullPath
 */
public IPath getFullPath() {
	return this.path;
}
/**
 * @see IStorage#getName
 */
public String getName() {
	return this.path.lastSegment();
}
/**
 * @see IStorage#isReadOnly()
 */
public boolean isReadOnly() {
	return true;
}
/**
 * @see IStorage#isReadOnly()
 */
public String toString() {
	return "JarEntryFile["+this.zipName+"::"+this.entryName+"]";
}
}
