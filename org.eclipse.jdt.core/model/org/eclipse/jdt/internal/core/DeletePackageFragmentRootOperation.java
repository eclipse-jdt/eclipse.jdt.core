/*******************************************************************************
 * Copyright (c) 2003 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.*;

public class DeletePackageFragmentRootOperation extends JavaModelOperation {

	int updateFlags;
	boolean updateClasspath;

	public DeletePackageFragmentRootOperation(
		IPackageFragmentRoot root,
		int updateFlags,
		boolean updateClasspath) {
			
		super(root);
		this.updateFlags = updateFlags;
		this.updateClasspath = updateClasspath;
	}

	protected void executeOperation() throws JavaModelException {
		
		IPackageFragmentRoot root = (IPackageFragmentRoot)this.getElementToProcess();
		IClasspathEntry rootEntry = root.getRawClasspathEntry();

		// update classpath if needed
		if (this.updateClasspath) {
			updateReferingProjectClasspaths(rootEntry.getPath());
		}
		
		// delete resource
		final char[][] exclusionPatterns = ((ClasspathEntry)rootEntry).fullExclusionPatternChars();
		IResource rootResource = root.getResource();
		if (rootEntry.getEntryKind() != IClasspathEntry.CPE_SOURCE || exclusionPatterns == null) {
			try {
				rootResource.delete(this.updateFlags, fMonitor);
			} catch (CoreException e) {
				throw new JavaModelException(e);
			}
		} else {
			final IPath[] nestedFolders = getNestedFolders(root);
			// TODO: Use IResourceProxyVisitor when bug 30268 is fixed
			IResourceVisitor visitor = new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {
					if (resource.getType() == IResource.FOLDER) {
						IPath path = resource.getFullPath();
						if (prefixesOneOf(path, nestedFolders)) {
							// equals if nested source folder
							return !equalsOneOf(path, nestedFolders);
						} else {
							// subtree doesn't contain any nested source folders
							resource.delete(updateFlags, fMonitor);
							return false;
						}
					} else {
						resource.delete(updateFlags, fMonitor);
						return false;
					}
				}
			};
			try {
				rootResource.accept(visitor);
			} catch (CoreException e) {
				throw new JavaModelException(e);
			}
		}
		this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE); 
	}


	/*
	 * Deletes the classpath entries equals to the given rootPath from all Java projects.
	 */
	protected void updateReferingProjectClasspaths(IPath rootPath) throws JavaModelException {
		IJavaModel model = this.getJavaModel();
		IJavaProject[] projects = model.getJavaProjects();
		for (int i = 0, length = projects.length; i < length; i++) {
			IJavaProject project = projects[i];
			IClasspathEntry[] classpath = project.getRawClasspath();
			IClasspathEntry[] newClasspath = null;
			int cpLength = classpath.length;
			int newCPIndex = -1;
			for (int j = 0; j < cpLength; j++) {
				IClasspathEntry entry = classpath[j];
				if (rootPath.equals(entry.getPath())) {
					if (newClasspath == null) {
						newClasspath = new IClasspathEntry[cpLength-1];
						System.arraycopy(classpath, 0, newClasspath, 0, j);
						newCPIndex = j;
					}
				} else if (newClasspath != null) {
					newClasspath[newCPIndex++] = entry;
				}
			}
			if (newClasspath != null) {
				if (newCPIndex < newClasspath.length) {
					System.arraycopy(newClasspath, 0, newClasspath = new IClasspathEntry[newCPIndex], 0, newCPIndex);
				}
				project.setRawClasspath(newClasspath, fMonitor);
			}
		}
	}	
	protected IJavaModelStatus verify() {
		IJavaModelStatus status = super.verify();
		if (!status.isOK()) {
			return status;
		}
		IPackageFragmentRoot root = (IPackageFragmentRoot) this.getElementToProcess();
		if (root == null || !root.exists()) {
			return new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, root);
		}
		if (root.isExternal()) {
			return new JavaModelStatus(IJavaModelStatusConstants.INVALID_RESOURCE_TYPE, root.getPath().toOSString());
		}
		return JavaModelStatus.VERIFIED_OK;
	}

}
