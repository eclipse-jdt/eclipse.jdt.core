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
package org.eclipse.jdt.internal.core.search;

import java.util.ArrayList;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.index.IIndex;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;

/**
 * Selects the indexes that correspond to projects in a given search scope
 * and that are dependent on a given focus element.
 */
public class IndexSelector {
	IJavaSearchScope searchScope;
	IJavaElement focus;
	IndexManager indexManager;
	IPath[] indexKeys; // cache of the keys for looking index up
	boolean isPolymorphicSearch;
public IndexSelector(
	IJavaSearchScope searchScope,
	IJavaElement focus,
	boolean isPolymorphicSearch,
	IndexManager indexManager) {
	this.searchScope = searchScope;
	this.focus = focus;
	this.indexManager = indexManager;
	this.isPolymorphicSearch = isPolymorphicSearch;
}
/**
 * Returns whether elements of the given project or jar can see the given focus (an IJavaProject or
 * a JarPackageFragmentRot) either because the focus is part of the project or the jar, or because it is 
 * accessible throught the project's classpath
 */
public static boolean canSeeFocus(IJavaElement focus, boolean isPolymorphicSearch, IPath projectOrJarPath) {
	try {
		IJavaModel model = focus.getJavaModel();
		IJavaProject project = getJavaProject(projectOrJarPath, model);
		if (project == null) {
			// projectOrJarPath is a jar
			// it can see the focus only if it is on the classpath of a project that can see the focus
			IJavaProject[] allProjects = model.getJavaProjects();
			for (int i = 0, length = allProjects.length; i < length; i++) {
				IJavaProject otherProject = allProjects[i];
				IClasspathEntry[] entries = otherProject.getResolvedClasspath(true);
				for (int j = 0, length2 = entries.length; j < length2; j++) {
					IClasspathEntry entry = entries[j];
					if ((entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) 
						&& entry.getPath().equals(projectOrJarPath)) {
							if (canSeeFocus(focus, isPolymorphicSearch, otherProject.getPath())) {
								return true;
							}
					}
				}
			}
			return false;
		} else {
			// projectOrJarPath is a project
			JavaProject focusProject = focus instanceof JarPackageFragmentRoot ? (JavaProject)focus.getParent() : (JavaProject)focus;
			if (isPolymorphicSearch) {
				// look for refering project
				IClasspathEntry[] entries = focusProject.getExpandedClasspath(true);
				for (int i = 0, length = entries.length; i < length; i++) {
					IClasspathEntry entry = entries[i];
					if ((entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) 
						&& entry.getPath().equals(projectOrJarPath)) {
							return true;
					}
				}
			}
			if (focus instanceof JarPackageFragmentRoot) {
				// focus is part of a jar
				IPath focusPath = focus.getPath();
				IClasspathEntry[] entries = ((JavaProject)project).getExpandedClasspath(true);
				for (int i = 0, length = entries.length; i < length; i++) {
					IClasspathEntry entry = entries[i];
					if ((entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) 
						&& entry.getPath().equals(focusPath)) {
							return true;
					}
				}
				return false;
			} else {
				// focus is part of a project
				if (focus.equals(project)) {
					return true;
				} else {
					// look for dependent projects
					IPath focusPath = focusProject.getProject().getFullPath();
					IClasspathEntry[] entries = ((JavaProject)project).getExpandedClasspath(true);
					for (int i = 0, length = entries.length; i < length; i++) {
						IClasspathEntry entry = entries[i];
						if ((entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) 
							&& entry.getPath().equals(focusPath)) {
								return true;
						}
					}
					return false;
				}
			}
		}
	} catch (JavaModelException e) {
		return false;
	}
}
/*
 *  Compute the list of paths which are keying index files.
 */
private void initializeIndexKeys() {
	
	ArrayList requiredIndexKeys = new ArrayList();
	IPath[] projectsAndJars = this.searchScope.enclosingProjectsAndJars();
	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	IJavaElement projectOrJarFocus = this.focus == null ? null : getProjectOrJar(this.focus);
	for (int i = 0; i < projectsAndJars.length; i++) {
		IPath location;
		IPath path = projectsAndJars[i];
		if ((!root.getProject(path.lastSegment()).exists()) // if project does not exist
			&& path.segmentCount() > 1
			&& ((location = root.getFile(path).getLocation()) == null
				|| !new java.io.File(location.toOSString()).exists()) // and internal jar file does not exist
			&& !new java.io.File(path.toOSString()).exists()) { // and external jar file does not exist
				continue;
		}
		if (projectOrJarFocus == null || canSeeFocus(projectOrJarFocus, this.isPolymorphicSearch, path)) {
			if (requiredIndexKeys.indexOf(path) == -1) {
				requiredIndexKeys.add(path);
			}
		}
	}
	this.indexKeys = new IPath[requiredIndexKeys.size()];
	requiredIndexKeys.toArray(this.indexKeys);
}
public IIndex[] getIndexes() {
	if (this.indexKeys == null) {
		this.initializeIndexKeys(); 
	}
	// acquire the in-memory indexes on the fly
	int length = this.indexKeys.length;
	IIndex[] indexes = new IIndex[length];
	int count = 0;
	for (int i = 0; i < length; i++){
		// may trigger some index recreation work
		IIndex index = indexManager.getIndex(indexKeys[i], true /*reuse index file*/, false /*do not create if none*/);
		if (index != null) indexes[count++] = index; // only consider indexes which are ready yet
	}
	if (count != length) {
		System.arraycopy(indexes, 0, indexes=new IIndex[count], 0, count);
	}
	return indexes;
}
/**
 * Returns the java project that corresponds to the given path.
 * Returns null if the path doesn't correspond to a project.
 */
private static IJavaProject getJavaProject(IPath path, IJavaModel model) {
	IJavaProject project = model.getJavaProject(path.lastSegment());
	if (project.exists()) {
		return project;
	} else {
		return null;
	}
}
public static IJavaElement getProjectOrJar(IJavaElement element) {
	while (!(element instanceof IJavaProject) && !(element instanceof JarPackageFragmentRoot)) {
		element = element.getParent();
	}
	return element;
}
}
