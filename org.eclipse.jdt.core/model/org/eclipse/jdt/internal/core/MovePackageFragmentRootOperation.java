/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.*;

public class MovePackageFragmentRootOperation extends CopyPackageFragmentRootOperation {
	/*
	 * Renames the classpath entries equal to the given path in the given project.
	 * If an entry with the destination path already existed, remove it.
	 */
	protected void renameEntryInClasspath(IPath rootPath, IJavaProject project) throws JavaModelException {
			
		IClasspathEntry[] classpath = project.getRawClasspath();
		IClasspathEntry[] newClasspath = null;
		int cpLength = classpath.length;
		int newCPIndex = -1;

		for (int i = 0; i < cpLength; i++) {
			IClasspathEntry entry = classpath[i];
			IPath entryPath = entry.getPath();
			if (rootPath.equals(entryPath)) {
				// rename entry
				if (newClasspath == null) {
					newClasspath = new IClasspathEntry[cpLength];
					System.arraycopy(classpath, 0, newClasspath, 0, i);
					newCPIndex = i;
				}
				newClasspath[newCPIndex++] = copy(entry);
			} else if (this.destination.equals(entryPath)) {
				// remove entry equals to destination
				if (newClasspath == null) {
					newClasspath = new IClasspathEntry[cpLength];
					System.arraycopy(classpath, 0, newClasspath, 0, i);
					newCPIndex = i;
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

	public MovePackageFragmentRootOperation(
		IPackageFragmentRoot root,
		IPath destination,
		int updateResourceFlags,
		int updateModelFlags,
		IClasspathEntry sibling) {
			
		super(
			root,
			destination,
			updateResourceFlags,
			updateModelFlags,
			sibling);
	}
	protected void executeOperation() throws JavaModelException {
		
		IPackageFragmentRoot root = (IPackageFragmentRoot)this.getElementToProcess();
		IClasspathEntry rootEntry = root.getRawClasspathEntry();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		
		// move resource
		if (!root.isExternal() && (this.updateModelFlags & IPackageFragmentRoot.NO_RESOURCE_MODIFICATION) == 0) {
			moveResource(root, rootEntry, workspaceRoot);
		}
		
		// update refering projects classpath excluding orignating project
		IJavaProject originatingProject = root.getJavaProject();
		if ((this.updateModelFlags & IPackageFragmentRoot.OTHER_REFERRING_PROJECTS_CLASSPATH) != 0) {
			updateReferringProjectClasspaths(rootEntry.getPath(), originatingProject);
		}

		boolean isRename = this.destination.segment(0).equals(originatingProject.getElementName());
		boolean updateOriginating = (this.updateModelFlags & IPackageFragmentRoot.ORIGINATING_PROJECT_CLASSPATH) != 0;
		boolean updateDestination = (this.updateModelFlags & IPackageFragmentRoot.DESTINATION_PROJECT_CLASSPATH) != 0;

		// update originating classpath
		if (updateOriginating) {
			if (isRename && updateDestination) {
				renameEntryInClasspath(rootEntry.getPath(), originatingProject);
			} else {
				removeEntryFromClasspath(rootEntry.getPath(), originatingProject);
			}
		}
		
		// update destination classpath
		if (updateDestination) {
			if (!isRename || !updateOriginating) {
				addEntryToClasspath(rootEntry, workspaceRoot);
			}  // else reference has been updated when updating originating project classpath
		}
	}
	protected void moveResource(
		IPackageFragmentRoot root,
		IClasspathEntry rootEntry,
		final IWorkspaceRoot workspaceRoot)
		throws JavaModelException {
			
		final char[][] exclusionPatterns = ((ClasspathEntry)rootEntry).fullExclusionPatternChars();
		IResource rootResource = root.getResource();
		if (rootEntry.getEntryKind() != IClasspathEntry.CPE_SOURCE || exclusionPatterns == null) {
			try {
				IResource destRes;
				if ((this.updateModelFlags & IPackageFragmentRoot.REPLACE) != 0
						&& (destRes = workspaceRoot.findMember(this.destination)) != null) {
					destRes.delete(this.updateResourceFlags, fMonitor);
				}
				rootResource.move(this.destination, this.updateResourceFlags, fMonitor);
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
							proxy.requestResource().move(destPath, updateResourceFlags, fMonitor);
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
						proxy.requestResource().move(destPath, updateResourceFlags, fMonitor);
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
	/*
	 * Renames the classpath entries equal to the given path in all Java projects.
	 */
	protected void updateReferringProjectClasspaths(IPath rootPath, IJavaProject projectOfRoot) throws JavaModelException {
		IJavaModel model = this.getJavaModel();
		IJavaProject[] projects = model.getJavaProjects();
		for (int i = 0, length = projects.length; i < length; i++) {
			IJavaProject project = projects[i];
			if (project.equals(projectOfRoot)) continue;
			renameEntryInClasspath(rootPath, project);
		}
	}
	/*
	 * Removes the classpath entry equal to the given path from the given project's classpath.
	 */
	protected void removeEntryFromClasspath(IPath rootPath, IJavaProject project) throws JavaModelException {
		
		IClasspathEntry[] classpath = project.getRawClasspath();
		IClasspathEntry[] newClasspath = null;
		int cpLength = classpath.length;
		int newCPIndex = -1;
		
		for (int i = 0; i < cpLength; i++) {
			IClasspathEntry entry = classpath[i];
			if (rootPath.equals(entry.getPath())) {
				if (newClasspath == null) {
					newClasspath = new IClasspathEntry[cpLength];
					System.arraycopy(classpath, 0, newClasspath, 0, i);
					newCPIndex = i;
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
