/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.JavaModelException;

/**
 * A jar entry that represents a non-java file found in a JAR.
 *
 * @see IStorage
 */
public class JarEntryFile extends PlatformObject implements IJarEntryResource {
	private static final IJarEntryResource[] NO_CHILDREN = new IJarEntryResource[0];
	private Object parent;
	private String entryName;
	private String zipName;
	private IPath path;
	
public JarEntryFile(String entryName, String zipName, IPath parentRelativePath) {
	this.entryName = entryName;
	this.zipName = zipName;
	this.path = parentRelativePath;
}

public JarEntryFile clone(Object newParent) {
	JarEntryFile file = new JarEntryFile(this.entryName, this.zipName, this.path);
	file.setParent(newParent);
	return file;
}
	
public InputStream getContents() throws CoreException {

	try {
		if (JavaModelManager.ZIP_ACCESS_VERBOSE) {
			System.out.println("(" + Thread.currentThread() + ") [JarEntryFile.getContents()] Creating ZipFile on " + this.zipName); //$NON-NLS-1$	//$NON-NLS-2$
		}
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
public IJarEntryResource[] getChildren() {
	return NO_CHILDREN;
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
public Object getParent() {
	return this.parent;
}
public boolean isFile() {
	return true;
}
/**
 * @see IStorage#isReadOnly()
 */
public boolean isReadOnly() {
	return true;
}
public void setParent(Object parent) {
	this.parent = parent;
}
/**
 * @see IStorage#isReadOnly()
 */
public String toString() {
	return "JarEntryFile["+this.zipName+"::"+this.entryName+"]"; //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-1$
}
}
