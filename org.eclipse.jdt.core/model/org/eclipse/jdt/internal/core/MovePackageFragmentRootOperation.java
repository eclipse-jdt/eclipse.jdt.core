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

public class MovePackageFragmentRootOperation extends CopyPackageFragmentRootOperation {
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

		// update classpath if needed
		if ((this.updateModelFlags & IPackageFragmentRoot.ORIGINATING_PROJECT_CLASSPATH) != 0) {
			updateProjectClasspath(rootEntry.getPath(), root.getJavaProject());
		}
		if ((this.updateModelFlags & IPackageFragmentRoot.OTHER_REFERRING_PROJECTS_CLASSPATH) != 0) {
			updateReferringProjectClasspaths(rootEntry.getPath(), root.getJavaProject());
		}
		if ((this.updateModelFlags & IPackageFragmentRoot.DESTINATION_PROJECT_CLASSPATH) != 0) {
			updateDestProjectClasspath(rootEntry, workspaceRoot);
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
							resource.move(destPath, updateResourceFlags, fMonitor);
							return false;
						}
					} else {
						IPath path = resource.getFullPath();
						IPath destPath = destination.append(path.removeFirstSegments(sourceSegmentCount));
						IResource destRes;
						if ((updateModelFlags & IPackageFragmentRoot.REPLACE) != 0
								&& (destRes = workspaceRoot.findMember(destPath)) != null) {
							destRes.delete(updateResourceFlags, fMonitor);
						}
						resource.move(destPath, updateResourceFlags, fMonitor);
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
	 * Renames the classpath entries equal to the given path in all Java projects.
	 * However if destination is inside project, leave reference as is (this is a rename 
	 * and reference will be updated later on). Otherwise if a source entry refers to this 
	 * path, deletes it (a project cannot refer to an outside source folder)
	 */
	protected void updateReferringProjectClasspaths(IPath rootPath, IJavaProject projectOfRoot) throws JavaModelException {
		IJavaModel model = this.getJavaModel();
		IJavaProject[] projects = model.getJavaProjects();
		for (int i = 0, length = projects.length; i < length; i++) {
			IJavaProject project = projects[i];
			if (project.equals(projectOfRoot)) continue;
			updateProjectClasspath(rootPath, project);
		}
	}
	/*
	 * Renames the classpath entries equal to the given path in the given project.
	 * However if destination is inside project, leave reference as is (this is a rename 
	 * and reference will be updated later on). Otherwise if a source entry refers to this 
	 * path, deletes it (a project cannot refer to an outside source folder)
	 */
	protected void updateProjectClasspath(IPath rootPath, IJavaProject project)
		throws JavaModelException {
		IClasspathEntry[] classpath = project.getRawClasspath();
		IClasspathEntry[] newClasspath = null;
		int cpLength = classpath.length;
		int newCPIndex = -1;
		for (int j = 0; j < cpLength; j++) {
			IClasspathEntry entry = classpath[j];
			if (rootPath.equals(entry.getPath())) {
				if (newClasspath == null) {
					newClasspath = new IClasspathEntry[cpLength];
					System.arraycopy(classpath, 0, newClasspath, 0, j);
					newCPIndex = j;
				}
				if (this.destination.segment(0).equals(project.getElementName())) continue;
				if (entry.getEntryKind() != IClasspathEntry.CPE_SOURCE) { // library entry
					newClasspath[newCPIndex++] = copy(entry);
				} // else source folder is moved to another project: deletes its classpath entry
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
