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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.JavaModelException;

public class CopyPackageFragmentRootOperation extends JavaModelOperation {
	IPath destination;
	int updateFlags;
	boolean updateClasspath;
	IClasspathEntry sibling;

	public CopyPackageFragmentRootOperation(
		IPackageFragmentRoot root,
		IPath destination,
		int updateFlags,
		boolean updateClasspath,
		IClasspathEntry sibling) {
			
		super(root);
		this.destination = destination;
		this.updateFlags = updateFlags;
		this.updateClasspath = updateClasspath;
		this.sibling = sibling;
	}
	protected void executeOperation() throws JavaModelException {
		
		// copy resource
		IPackageFragmentRoot root = (IPackageFragmentRoot)this.getElementToProcess();
		IClasspathEntry rootEntry = root.getRawClasspathEntry();
		final char[][] exclusionPatterns = ((ClasspathEntry)rootEntry).fullExclusionPatternChars();
		IResource rootResource = root.getResource();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		if (root.getKind() == IPackageFragmentRoot.K_BINARY || exclusionPatterns == null) {
			try {
				rootResource.copy(this.destination, this.updateFlags, fMonitor);
			} catch (CoreException e) {
				throw new JavaModelException(e);
			}
		} else {
			final int sourceSegmentCount = rootEntry.getPath().segmentCount();
			final IFolder destFolder = workspaceRoot.getFolder(this.destination);
			final IPath[] nestedFolders = getNestedFolders(root);
			// TODO: Use IResourceProxyVisitor when bug 30268 is fixed
			IResourceVisitor visitor = new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {
					if (resource.getType() == IResource.FOLDER) {
						IPath path = resource.getFullPath();
						if (prefixesOneOf(path, nestedFolders)) {
							if (equalsOneOf(path, nestedFolders)) {
								// nested source folder
								return false;
							} else {
								// folder containing nested source folder
								IFolder folder = destFolder.getFolder(path.removeFirstSegments(sourceSegmentCount));
								folder.create(updateFlags, true, fMonitor);
								return true;
							}
						} else {
							// subtree doesn't contain any nested source folders
							resource.copy(destination.append(path.removeFirstSegments(sourceSegmentCount)), updateFlags, fMonitor);
							return false;
						}
					} else {
						IPath path = resource.getFullPath();
						resource.copy(destination.append(path.removeFirstSegments(sourceSegmentCount)), updateFlags, fMonitor);
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
		
		// update classpath if needed
		if (this.updateClasspath) {
			updateDestProjectClasspath(rootEntry, workspaceRoot);
		}
	}
	protected void updateDestProjectClasspath(
		IClasspathEntry rootEntry,
		IWorkspaceRoot workspaceRoot)
		throws JavaModelException {
		IProject destProject = workspaceRoot.getProject(this.destination.segment(0));
		IJavaProject jProject = JavaCore.create(destProject);
		IClasspathEntry[] classpath = jProject.getRawClasspath();
		int length = classpath.length;
		int position;
		if (this.sibling == null) {
			// insert at the end
			position = length;
		} else {
			// insert before sibling
			position = -1;
			for (int i = 0; i < length; i++) {
				if (this.sibling.equals(classpath[i])) {
					position = i;
					break;
				}
			}
		}
		if (position == -1) {
			throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_SIBLING, this.sibling.toString()));
		}
		IClasspathEntry[] newClasspath = new IClasspathEntry[length+1];
		if (position != 0) {
			System.arraycopy(classpath, 0, newClasspath, 0, position);
		}
		if (position != length) {
			System.arraycopy(classpath, position, newClasspath, position+1, length-position);
		}
		IClasspathEntry newEntry = copy(rootEntry);
		newClasspath[position] = newEntry;
		jProject.setRawClasspath(newClasspath, fMonitor);
	}
	/*
	 * Copies the given classpath entry replacing its path with the destination path
	 * if it is a source folder or a library.
	 */
	protected IClasspathEntry copy(IClasspathEntry entry) throws JavaModelException {
		switch (entry.getEntryKind()) {
			case IClasspathEntry.CPE_CONTAINER:
				return JavaCore.newContainerEntry(entry.getPath(), entry.isExported());
			case IClasspathEntry.CPE_LIBRARY:
				return JavaCore.newLibraryEntry(this.destination, entry.getSourceAttachmentPath(), entry.getSourceAttachmentRootPath(), entry.isExported());
			case IClasspathEntry.CPE_PROJECT:
				return JavaCore.newProjectEntry(entry.getPath(), entry.isExported());
			case IClasspathEntry.CPE_SOURCE:
				return JavaCore.newSourceEntry(this.destination, entry.getExclusionPatterns(), entry.getOutputLocation());
			case IClasspathEntry.CPE_VARIABLE:
				return JavaCore.newVariableEntry(entry.getPath(), entry.getSourceAttachmentPath(), entry.getSourceAttachmentRootPath(), entry.isExported());
			default:
				throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this.getElementToProcess()));
		}
	}
	public IJavaModelStatus verify() {
		IJavaModelStatus status = super.verify();
		if (!status.isOK()) {
			return status;
		}
		IPackageFragmentRoot root = (IPackageFragmentRoot)getElementToProcess();
		if (root == null || !root.exists()) {
			return new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, root);
		}
			
		if (root.isExternal()) {
			return new JavaModelStatus(IJavaModelStatusConstants.INVALID_RESOURCE_TYPE, root.getPath().toOSString());
		}
			
		if (this.updateClasspath) {
			String destProjectName = this.destination.segment(0);
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(destProjectName);
			if (!JavaProject.hasJavaNature(project)) {
				return new JavaModelStatus(IJavaModelStatusConstants.INVALID_PROJECT, destProjectName);
			}
			
			try {
				IJavaProject destProject = JavaCore.create(project);
				IClasspathEntry[] destClasspath = destProject.getRawClasspath();
				boolean foundSibling = false;
				boolean foundExistingEntry = false;
				for (int i = 0, length = destClasspath.length; i < length; i++) {
					IClasspathEntry entry = destClasspath[i];
					if (entry.equals(this.sibling)) {
						foundSibling = true;
						break;
					}
					if (entry.getPath().equals(this.destination)) {
						foundExistingEntry = true;
					}
				}
				if (this.sibling != null && !foundSibling) {
					return new JavaModelStatus(IJavaModelStatusConstants.INVALID_SIBLING, this.sibling == null ? "null" : this.sibling.toString()); //$NON-NLS-1$
				}
				if (foundExistingEntry) {
					return new JavaModelStatus(IJavaModelStatusConstants.NAME_COLLISION, this.destination.toString());
				}
			} catch (JavaModelException e) {
				return e.getJavaModelStatus();
			}
		}

		return JavaModelStatus.VERIFIED_OK;
	}
}
