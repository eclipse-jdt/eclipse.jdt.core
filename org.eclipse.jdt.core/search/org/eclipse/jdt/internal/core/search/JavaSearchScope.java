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
 * A Java-specific scope for searching relative to one or more projects.
 * The scope can be configured to follow the respective classpath of 
 * in-scope projects, and to not search binaries. By default, both classpaths 
 * and binaries are included. 
 */
public class JavaSearchScope extends AbstractSearchScope implements IJavaSearchScope {

	private IResource fLastCheckedResource;
	private boolean fLastResult;

	/* The paths of the resources in this search scope 
	   (or the classpath entries' paths 
	   if the resources are projects) */
	private IPath[] paths = new IPath[1];
	private boolean[] pathWithSubFolders = new boolean[1];
	private int pathsCount = 0;
	
	private IPath[] enclosingProjectsAndJars = new IPath[0];
	
/**
 * Adds the given resource to this search scope.
 * Remember if subfolders need to be included as well.
 */
public void add(IResource element, boolean withSubFolders) {
	// clear indexer cache
	fLastCheckedResource = null;
	
	if (!element.isAccessible()) return;
	
	super.add(element);
	this.add(element.getFullPath(), withSubFolders);	
}
/**
 * Adds the given resource to this search scope.
 * Remember if subfolders need to be included as well.
 */
public void add(IResource element) {
	this.add(element, true);
}
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

public void add(IJavaProject project, boolean includesPrereqProjects) throws JavaModelException {

	this.addEnclosingProjectOrJar(project.getProject().getFullPath());

	IWorkspaceRoot root = project.getUnderlyingResource().getWorkspace().getRoot();
	IClasspathEntry[] entries = project.getResolvedClasspath(true);
	IJavaModel model = project.getJavaModel();
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
					this.add(model.getJavaProject(entry.getPath().lastSegment()), true);
				}
				break;
			case IClasspathEntry.CPE_SOURCE:
				path = entry.getPath();
				if (path.segmentCount() == 1) {
					// project is source
					this.add(root.getProject(path.lastSegment()));
				} else {
					// regular source folder
					this.add(root.getFolder(path));
				}
				break;
		}
	}
}
public void add(IJavaElement element) throws JavaModelException {
	IPackageFragmentRoot root = null;
	if (element instanceof IJavaProject) {
		IJavaProject project = (IJavaProject)element;
		this.add(project, true);
	} else if (element instanceof IPackageFragmentRoot) {
		root = (IPackageFragmentRoot)element;
		if (root.isArchive()) {
			this.add(root.getPath(), false);
		} else {
			IJavaElement[] children = root.getChildren();
			for (int i = 0, length = children.length; i < length; i++) {
				this.add(children[i].getUnderlyingResource(), false);
			}
		}
	} else if (element instanceof IPackageFragment) {
		root = (IPackageFragmentRoot)element.getParent();
		if (root.isArchive()) {
			this.add(root.getPath(), false);
		} else {
			this.add(element.getUnderlyingResource(), false);
		}
	} else {
		this.add(element.getUnderlyingResource());
		
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
 * Returns whether this search scope encloses the given resource.
 */
protected boolean encloses(IResource element) {
	IPath elementPath = element.getFullPath();
	boolean encloses = this.encloses(elementPath);
	fLastCheckedResource = element;
	fLastResult = encloses;
	return encloses;
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
	try {
		IResource resource = element.getUnderlyingResource();
		if (resource == null) {
			// case of a binary in an external jar
			return true;
		} else if (resource.equals(fLastCheckedResource)) {
			return fLastResult;
		}
		return encloses(resource);
	} catch (JavaModelException e) {
		return false;
	}
}

/* (non-Javadoc)
 * @see IJavaSearchScope#enclosingProjectsAndJars()
 */
public IPath[] enclosingProjectsAndJars() {
	return this.enclosingProjectsAndJars;
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
