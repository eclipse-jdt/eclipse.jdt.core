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
		int updateFlags,
		boolean updateClasspath,
		IClasspathEntry sibling) {
			
		super(
			root,
			destination,
			updateFlags,
			updateClasspath,
			sibling);
	}
	protected void executeOperation() throws JavaModelException {
		
		IPackageFragmentRoot root = (IPackageFragmentRoot)this.getElementToProcess();
		IClasspathEntry rootEntry = root.getRawClasspathEntry();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		// update classpath if needed
		if (this.updateClasspath) {
			updateDestProjectClasspath(rootEntry, workspaceRoot);
			updateReferingProjectClasspaths(rootEntry.getPath());
		}
		
		// move resource
		final char[][] exclusionPatterns = ((ClasspathEntry)rootEntry).fullExclusionPatternChars();
		IResource rootResource = root.getResource();
		if (rootEntry.getEntryKind() != IClasspathEntry.CPE_SOURCE || exclusionPatterns == null) {
			try {
				rootResource.move(this.destination, this.updateFlags, fMonitor);
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
							resource.move(destination.append(path.removeFirstSegments(sourceSegmentCount)), updateFlags, fMonitor);
							return false;
						}
					} else {
						IPath path = resource.getFullPath();
						resource.move(destination.append(path.removeFirstSegments(sourceSegmentCount)), updateFlags, fMonitor);
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
	 */
	protected void updateReferingProjectClasspaths(IPath rootPath) throws JavaModelException {
		IJavaModel model = this.getJavaModel();
		IJavaProject[] projects = model.getJavaProjects();
		for (int i = 0, length = projects.length; i < length; i++) {
			IJavaProject project = projects[i];
			IClasspathEntry[] classpath = project.getRawClasspath();
			IClasspathEntry[] newClasspath = null;
			int cpLength = classpath.length;
			for (int j = 0; j < cpLength; j++) {
				IClasspathEntry entry = classpath[j];
				if (rootPath.equals(entry.getPath())) {
					if (newClasspath == null) {
						newClasspath = new IClasspathEntry[cpLength];
						System.arraycopy(classpath, 0, newClasspath, 0, cpLength);
					}
					newClasspath[j] = copy(entry);
				}
			}
			if (newClasspath != null) {
				project.setRawClasspath(newClasspath, fMonitor);
			}
		}
	}
}
