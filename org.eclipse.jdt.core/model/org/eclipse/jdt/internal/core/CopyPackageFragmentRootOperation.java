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
	int updateResourceFlags;
	int updateModelFlags;
	IClasspathEntry sibling;

	public CopyPackageFragmentRootOperation(
		IPackageFragmentRoot root,
		IPath destination,
		int updateResourceFlags,
		int updateModelFlags,
		IClasspathEntry sibling) {
			
		super(root);
		this.destination = destination;
		this.updateResourceFlags = updateResourceFlags;
		this.updateModelFlags = updateModelFlags;
		this.sibling = sibling;
	}
	protected void executeOperation() throws JavaModelException {
		
		IPackageFragmentRoot root = (IPackageFragmentRoot)this.getElementToProcess();
		IClasspathEntry rootEntry = root.getRawClasspathEntry();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		// copy resource
		if (!root.isExternal() && (this.updateModelFlags & IPackageFragmentRoot.NO_RESOURCE_MODIFICATION) == 0) {
			copyResource(root, rootEntry, workspaceRoot);
		}
		
		// update classpath if needed
		if ((this.updateModelFlags & IPackageFragmentRoot.DESTINATION_PROJECT_CLASSPATH) != 0) {
			addEntryToClasspath(rootEntry, workspaceRoot);
		}
	}
	protected void copyResource(
		IPackageFragmentRoot root,
		IClasspathEntry rootEntry,
		final IWorkspaceRoot workspaceRoot)
		throws JavaModelException {
		final char[][] exclusionPatterns = ((ClasspathEntry)rootEntry).fullExclusionPatternChars();
		IResource rootResource = root.getResource();
		if (root.getKind() == IPackageFragmentRoot.K_BINARY || exclusionPatterns == null) {
			try {
				IResource destRes;
				if ((this.updateModelFlags & IPackageFragmentRoot.REPLACE) != 0) {
					if (rootEntry.getPath().equals(this.destination)) return;
					if ((destRes = workspaceRoot.findMember(this.destination)) != null) {
						destRes.delete(this.updateResourceFlags, fMonitor);
					}
				}
				rootResource.copy(this.destination, this.updateResourceFlags, fMonitor);
			} catch (CoreException e) {
				throw new JavaModelException(e);
			}
		} else {
			final int sourceSegmentCount = rootEntry.getPath().segmentCount();
			final IFolder destFolder = workspaceRoot.getFolder(this.destination);
			final IPath[] nestedFolders = getNestedFolders(root);
			IResourceProxyVisitor visitor = new IResourceProxyVisitor() {
				public boolean visit(IResourceProxy proxy) throws CoreException {
					if (proxy.getType() == IResource.FOLDER) {
						IPath path = proxy.requestFullPath();
						if (prefixesOneOf(path, nestedFolders)) {
							if (equalsOneOf(path, nestedFolders)) {
								// nested source folder
								return false;
							} else {
								// folder containing nested source folder
								IFolder folder = destFolder.getFolder(path.removeFirstSegments(sourceSegmentCount));
								if ((updateModelFlags & IPackageFragmentRoot.REPLACE) != 0
										&& folder.exists()) {
									return true;
								}
								folder.create(updateResourceFlags, true, fMonitor);
								return true;
							}
						} else {
							// subtree doesn't contain any nested source folders
							IPath destPath = destination.append(path.removeFirstSegments(sourceSegmentCount));
							IResource destRes;
							if ((updateModelFlags & IPackageFragmentRoot.REPLACE) != 0
									&& (destRes = workspaceRoot.findMember(destPath)) != null) {
								destRes.delete(updateResourceFlags, fMonitor);
							}
							proxy.requestResource().copy(destPath, updateResourceFlags, fMonitor);
							return false;
						}
					} else {
						IPath path = proxy.requestFullPath();
						IPath destPath = destination.append(path.removeFirstSegments(sourceSegmentCount));
						IResource destRes;
						if ((updateModelFlags & IPackageFragmentRoot.REPLACE) != 0
								&& (destRes = workspaceRoot.findMember(destPath)) != null) {
							destRes.delete(updateResourceFlags, fMonitor);
						}
						proxy.requestResource().copy(destPath, updateResourceFlags, fMonitor);
						return false;
					}
				}
			};
			try {
				rootResource.accept(visitor, IResource.NONE);
			} catch (CoreException e) {
				throw new JavaModelException(e);
			}
		}
		this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE); 
	}
	protected void addEntryToClasspath(IClasspathEntry rootEntry, IWorkspaceRoot workspaceRoot) throws JavaModelException {
		
		IProject destProject = workspaceRoot.getProject(this.destination.segment(0));
		IJavaProject jProject = JavaCore.create(destProject);
		IClasspathEntry[] classpath = jProject.getRawClasspath();
		int length = classpath.length;
		IClasspathEntry[] newClasspath;
		
		// case of existing entry and REPLACE was specified
		if ((this.updateModelFlags & IPackageFragmentRoot.REPLACE) != 0) {
			// find existing entry
			for (int i = 0; i < length; i++) {
				if (this.destination.equals(classpath[i].getPath())) {
					newClasspath = new IClasspathEntry[length];
					System.arraycopy(classpath, 0, newClasspath, 0, length);
					newClasspath[i] = copy(rootEntry);
					jProject.setRawClasspath(newClasspath, fMonitor);
					return;
				}
			}
		} 
		
		// other cases
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
		newClasspath = new IClasspathEntry[length+1];
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

		if ((this.updateModelFlags & IPackageFragmentRoot.DESTINATION_PROJECT_CLASSPATH) != 0) {
			String destProjectName = this.destination.segment(0);
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(destProjectName);
			if (JavaProject.hasJavaNature(project)) {
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
					if (foundExistingEntry && (this.updateModelFlags & IPackageFragmentRoot.REPLACE) == 0) {
						return new JavaModelStatus(
							IJavaModelStatusConstants.NAME_COLLISION, 
							Util.bind("status.nameCollision", this.destination.toString())); //$NON-NLS-1$
					}
				} catch (JavaModelException e) {
					return e.getJavaModelStatus();
				}
			}
		}

		return JavaModelStatus.VERIFIED_OK;
	}
}
