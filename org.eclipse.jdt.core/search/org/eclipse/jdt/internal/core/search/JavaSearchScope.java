package org.eclipse.jdt.internal.core.search;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.Path;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.search.AbstractSearchScope;

import java.util.*;

/**
 * A Java-specific scope for searching relative to one or more java elements.
 */
public class JavaSearchScope implements IJavaSearchScope {

	/* The paths of the resources in this search scope 
	   (or the classpath entries' paths 
	   if the resources are projects) */
	private IPath[] paths = new IPath[1];
	private boolean[] pathWithSubFolders = new boolean[1];
	private int pathsCount = 0;
	
	private IPath[] enclosingProjectsAndJars = new IPath[0];
	
private void addEnclosingProjectOrJar(IPath path) {
	int length = this.enclosingProjectsAndJars.length;
	for (int i = 0; i < length; i++) {
		if (this.enclosingProjectsAndJars[i].equals(path)) return;
	}
	System.arraycopy(
		this.enclosingProjectsAndJars,
		0,
		this.enclosingProjectsAndJars = new IPath[length+1],
		0,
		length);
	this.enclosingProjectsAndJars[length] = path;
}

public void add(IJavaProject javaProject, boolean includesPrereqProjects, Hashtable visitedProjects) throws JavaModelException {
	IProject project = javaProject.getProject();
	if (!project.isAccessible() || visitedProjects.get(project) != null) return;
	visitedProjects.put(project, project);

	this.addEnclosingProjectOrJar(project.getFullPath());

	IWorkspaceRoot root = project.getWorkspace().getRoot();
	IClasspathEntry[] entries = javaProject.getResolvedClasspath(true);
	IJavaModel model = javaProject.getJavaModel();
	for (int i = 0, length = entries.length; i < length; i++) {
		IClasspathEntry entry = entries[i];
		switch (entry.getEntryKind()) {
			case IClasspathEntry.CPE_LIBRARY:
				IPath path = entry.getPath();
				this.add(path, true);
				this.addEnclosingProjectOrJar(path);
				break;
			case IClasspathEntry.CPE_PROJECT:
				if (includesPrereqProjects) {
					this.add(model.getJavaProject(entry.getPath().lastSegment()), true, visitedProjects);
				}
				break;
			case IClasspathEntry.CPE_SOURCE:
				this.add(entry.getPath(), true);
				break;
		}
	}
}
public void add(IJavaElement element) throws JavaModelException {
	IPackageFragmentRoot root = null;
	if (element instanceof IJavaProject) {
		this.add((IJavaProject)element, true, new Hashtable(2));
	} else if (element instanceof IPackageFragmentRoot) {
		root = (IPackageFragmentRoot)element;
		this.add(root.getPath(), true);
	} else if (element instanceof IPackageFragment) {
		root = (IPackageFragmentRoot)element.getParent();
		if (root.isArchive()) {
			this.add(root.getPath().append(new Path(element.getElementName().replace('.', '/'))), false);
		} else {
			IResource resource = element.getUnderlyingResource();
			if (resource != null && resource.isAccessible()) {
				this.add(resource.getFullPath(), false);
			}
		}
	} else {
		IResource resource = element.getUnderlyingResource();
		if (resource != null && resource.isAccessible()) {
			this.add(resource.getFullPath(), true);
		}	
	}
	
	if (root != null) {
		if (root.isArchive()) {
			this.addEnclosingProjectOrJar(root.getPath());
		} else {
			this.addEnclosingProjectOrJar(root.getJavaProject().getProject().getFullPath());
		}
	}
}

/**
 * Adds the given path to this search scope. Remember if subfolders need to be included as well.
 */
private void add(IPath path, boolean withSubFolders) {
	if (this.paths.length == this.pathsCount) {
		System.arraycopy(
			this.paths,
			0,
			this.paths = new IPath[this.pathsCount * 2],
			0,
			this.pathsCount);
		System.arraycopy(
			this.pathWithSubFolders,
			0,
			this.pathWithSubFolders = new boolean[this.pathsCount * 2],
			0,
			this.pathsCount);
	}
	this.paths[this.pathsCount] = path;
	this.pathWithSubFolders[this.pathsCount++] = withSubFolders; 
}

/* (non-Javadoc)
 * @see IJavaSearchScope#encloses(String)
 */
public boolean encloses(String resourcePathString) {
	IPath resourcePath;
	int separatorIndex = resourcePathString.indexOf(JAR_FILE_ENTRY_SEPARATOR);
	if (separatorIndex != -1) {
		resourcePath = 
			new Path(resourcePathString.substring(0, separatorIndex)).
				append(new Path(resourcePathString.substring(separatorIndex+1)));
	} else {
			resourcePath = new Path(resourcePathString);
	}
	return this.encloses(resourcePath);
}

/**
 * Returns whether this search scope encloses the given path.
 */
private boolean encloses(IPath path) {
	for (int i = 0; i < this.pathsCount; i++) {
		if (this.pathWithSubFolders[i]) {
			if (this.paths[i].isPrefixOf(path)) {
				return true;
			}
		} else {
			IPath scopePath = this.paths[i];
			if (scopePath.isPrefixOf(path) 
				&& (scopePath.segmentCount() == path.segmentCount() - 1)) {
				return true;
			}
		}
	}
	return false;
}

/* (non-Javadoc)
 * @see IJavaSearchScope#encloses(IJavaElement)
 */
public boolean encloses(IJavaElement element) {
	return this.encloses(this.fullPath(element));
}

/* (non-Javadoc)
 * @see IJavaSearchScope#enclosingProjectsAndJars()
 */
public IPath[] enclosingProjectsAndJars() {
	return this.enclosingProjectsAndJars;
}
private IPath fullPath(IJavaElement element) {
	if (element instanceof IPackageFragmentRoot) {
		return ((IPackageFragmentRoot)element).getPath();
	} else 	{
		IJavaElement parent = element.getParent();
		IPath parentPath = parent == null ? null : this.fullPath(parent);
		IPath childPath;
		if (element instanceof IPackageFragment) {
			childPath = new Path(element.getElementName().replace('.', '/'));
		} else if (element instanceof IOpenable) {
			childPath = new Path(element.getElementName());
		} else {
			return parentPath;
		}
		return parentPath == null ? childPath : parentPath.append(childPath);
	}
}

/* (non-Javadoc)
 * @see IJavaSearchScope#includesBinaries()
 */
public boolean includesBinaries() {
	return true;
}

/* (non-Javadoc)
 * @see IJavaSearchScope#includesClasspaths()
 */
public boolean includesClasspaths() {
	return true;
}

/* (non-Javadoc)
 * @see IJavaSearchScope#setIncludesBinaries
 */
public void setIncludesBinaries(boolean includesBinaries) {
}

/* (non-Javadoc)
 * @see IJavaSearchScope#setIncludeClasspaths
 */
public void setIncludesClasspaths(boolean includesClasspaths) {
}

}
