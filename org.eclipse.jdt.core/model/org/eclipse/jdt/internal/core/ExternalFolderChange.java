/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.util.Util;

public class ExternalFolderChange {
	
	private JavaProject project;
	private IClasspathEntry[] oldResolvedClasspath;
	
	public ExternalFolderChange(JavaProject project, IClasspathEntry[] oldResolvedClasspath) {
		this.project = project;
		this.oldResolvedClasspath = oldResolvedClasspath;
	}
	
	private HashSet getExternalFolders(IClasspathEntry[] classpath) {
		HashSet folders = new HashSet();
		for (int i = 0; i < classpath.length; i++) {
			IClasspathEntry entry = classpath[i];
			if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
				addExternalFolder(entry.getPath(), folders);
				addExternalFolder(entry.getSourceAttachmentPath(), folders);
			}
		}
		return folders;
	}

	private void addExternalFolder(IPath path, HashSet folders) {
		if (path == null || Util.isArchiveFileName(path.lastSegment())) 
			return;
		Object target = JavaModel.getTarget(path, false/*don't check resource existence*/);
		if (target instanceof File) {
			if (!((File) target).isFile()) {
				folders.add(path);
			}
		} else if (target instanceof IFolder && ExternalFoldersManager.isExternal(((IFolder) target).getFullPath())) {
			folders.add(path);
		}
	}

	/*
	 * Update external folders
	 */
	public void updateExternalFoldersIfNecessary(IProgressMonitor monitor) throws JavaModelException {
		HashSet oldFolders = this.oldResolvedClasspath == null ? new HashSet() : getExternalFolders(this.oldResolvedClasspath);
		IClasspathEntry[] newResolvedClasspath = this.project.getResolvedClasspath();
		HashSet newFolders = getExternalFolders(newResolvedClasspath);
		ExternalFoldersManager foldersManager = JavaModelManager.getExternalManager();
		Iterator iterator = newFolders.iterator();
		while (iterator.hasNext()) {
			Object folderPath = iterator.next();
			if (!oldFolders.remove(folderPath)) {
				try {
					foldersManager.createLinkFolder((IPath) folderPath, monitor);
				} catch (CoreException e) {
					throw new JavaModelException(e);
				}
			}
		}
		// removal of linked folders is done during save
	}
	public String toString() {
		return "ExternalFolderChange: " + this.project.getElementName(); //$NON-NLS-1$
	}
}
