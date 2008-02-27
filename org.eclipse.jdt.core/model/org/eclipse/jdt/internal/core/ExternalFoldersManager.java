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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.util.Util;

public class ExternalFoldersManager {
	private static final boolean DEBUG = false;
	private static final String EXTERNAL_PROJECT_NAME = ".org.eclipse.jdt.core.external.folders"; //$NON-NLS-1$
	private static final String LINKED_FOLDER_NAME = ".link"; //$NON-NLS-1$
	private HashMap folders;
	private int counter = 0;
	
	public static boolean isExternal(IPath resourcePath) {
		return EXTERNAL_PROJECT_NAME.equals(resourcePath.segment(0));
	}

	public IFolder addFolder(IPath externalFolderPath) {
		return addFolder(externalFolderPath, getExternalFoldersProject());
	}

	private synchronized IFolder addFolder(IPath externalFolderPath, IProject externalFoldersProject) {
		HashMap knownFolders = getFolders();
		Object existing = knownFolders.get(externalFolderPath);
		if (existing != null) {
			return (IFolder) existing;
		}
		IFolder result;
		do {
			result = externalFoldersProject.getFolder(LINKED_FOLDER_NAME + this.counter++);
		} while (result.exists());
		knownFolders.put(externalFolderPath, result);
		return result;
	}
	
	public IFolder createLinkFolder(IPath externalFolderPath, IProgressMonitor monitor) throws CoreException {
		IProject externalFoldersProject = createExternalFoldersProject(monitor); // run outside synchronized as this can create a resource
		IFolder result = addFolder(externalFolderPath, externalFoldersProject);
		if (!result.exists())
			result.createLink(externalFolderPath, IResource.ALLOW_MISSING_LOCAL, monitor);
		else
			result.refreshLocal(IResource.DEPTH_INFINITE,  monitor);
		return result;
	}
	
	public synchronized void cleanUp(IProgressMonitor monitor) throws CoreException {
		DeltaProcessingState state = JavaModelManager.getDeltaState();
		HashMap roots = state.roots;
		HashMap sourceAttachments = state.sourceAttachments;
		if (roots == null && sourceAttachments == null)
			return;
		HashMap knownFolders = getFolders();
		Iterator iterator = knownFolders.keySet().iterator();
		while (iterator.hasNext()) {
			IPath path = (IPath) iterator.next();
			if ((roots != null && !roots.containsKey(path))
					&& (sourceAttachments != null && !sourceAttachments.containsKey(path))) {
				IFolder folder = (IFolder) knownFolders.get(path);
				if (folder != null)
					folder.delete(true, monitor);
			}
		}
		IProject project = getExternalFoldersProject();
		if (project.isAccessible() && project.members().length == 1/*remaining member is .project*/)
			project.delete(true, monitor);
	}
	
	public IProject getExternalFoldersProject() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(EXTERNAL_PROJECT_NAME);
	}
	private IProject createExternalFoldersProject(IProgressMonitor monitor) {
		IProject project = getExternalFoldersProject();
		if (!project.isAccessible()) {
			try {
				if (!project.exists()) {
					IProjectDescription desc = project.getWorkspace().newProjectDescription(project.getName());
					IPath stateLocation = JavaCore.getPlugin().getStateLocation();
					desc.setLocation(stateLocation.append(EXTERNAL_PROJECT_NAME));
					project.create(DEBUG ? null : desc, DEBUG ? IResource.NONE : IResource.HIDDEN, monitor);
				}
				try {
					project.open(monitor);
				} catch (CoreException e1) {
					// .project or folder on disk have been deleted, recreate them
					IPath stateLocation = DEBUG ? ResourcesPlugin.getWorkspace().getRoot().getLocation() : JavaCore.getPlugin().getStateLocation();
					IPath projectPath = stateLocation.append(EXTERNAL_PROJECT_NAME);
					projectPath.toFile().mkdirs();
				    FileOutputStream output = new FileOutputStream(projectPath.append(".project").toOSString()); //$NON-NLS-1$
				    try {
				        output.write((
				        		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + //$NON-NLS-1$
				        		"<projectDescription>\n" + //$NON-NLS-1$
				        		"	<name>" + EXTERNAL_PROJECT_NAME + "</name>\n" + //$NON-NLS-1$ //$NON-NLS-2$
				        		"	<comment></comment>\n" + //$NON-NLS-1$
				        		"	<projects>\n" + //$NON-NLS-1$
				        		"	</projects>\n" + //$NON-NLS-1$
				        		"	<buildSpec>\n" + //$NON-NLS-1$
				        		"	</buildSpec>\n" + //$NON-NLS-1$
				        		"	<natures>\n" + //$NON-NLS-1$
				        		"	</natures>\n" + //$NON-NLS-1$
				        		"</projectDescription>").getBytes()); //$NON-NLS-1$
				    } finally {
				        output.close();
				    }
					project.open(null);
				}
			} catch (CoreException e) {
				Util.log(e, "Problem creating hidden project for external folders"); //$NON-NLS-1$
				return project;
			} catch (IOException e) {
				Util.log(e, "Problem creating hidden project for external folders"); //$NON-NLS-1$
				return project;
			}
		}
		return project;
	}
	
	public synchronized IFolder getFolder(IPath externalFolderPath) {
		return (IFolder) getFolders().get(externalFolderPath);
	}
	
	private HashMap getFolders() {
		if (this.folders == null) {
			this.folders = new HashMap();
			IProject project = getExternalFoldersProject();
			if (project.isAccessible()) {
				try {
					IResource[] members = project.members();
					for (int i = 0, length = members.length; i < length; i++) {
						IResource member = members[i];
						if (member.getType() == IResource.FOLDER && member.isLinked() && member.getName().startsWith(LINKED_FOLDER_NAME)) {
							IPath externalFolderPath = member.getLocation();
							this.folders.put(externalFolderPath, member);
						}
					}
				} catch (CoreException e) {
					Util.log(e, "Exception while initializing external folders"); //$NON-NLS-1$
				}
			}
		}
		return this.folders;
	}
	
	public synchronized IFolder removeFolder(IPath externalFolderPath) {
		return (IFolder) getFolders().remove(externalFolderPath);
	}

}
