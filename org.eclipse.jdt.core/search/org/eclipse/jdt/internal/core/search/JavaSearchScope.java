package org.eclipse.jdt.internal.core.search;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
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

	private boolean includesBinaries = true;
	private boolean includesClasspaths = true;
    
	private IResource fLastCheckedResource;
	private boolean fLastResult;

	/* The paths of the resources in this search scope 
	   (or the classpath entries' paths 
	   if the resources are projects) */
	private IPath[] paths = new IPath[1];
	private int pathsCount = 0;
/**
 * Adds the given resource to this search scope.
 */
public void add(IResource element) {
	super.add(element);

	// clear indexer cache
	fLastCheckedResource = null;

	if (element instanceof IProject) {
		// remember the paths of its classpath entries
		IJavaModel javaModel = JavaModelManager.getJavaModel(element.getWorkspace());
		IJavaProject javaProject = javaModel.getJavaProject(element.getName());
		try {
			IClasspathEntry[] entries = javaProject.getResolvedClasspath(true);
			for (int i = 0, length = entries.length; i < length; i++) {
				IClasspathEntry entry = entries[i];
				this.add(entry.getPath());
			}
		} catch (JavaModelException e) {
		}
	} else {
		this.add(element.getFullPath());
	}
}
/**
 * Adds the given path to this search scope.
 */
private void add(IPath path) {
	if (this.paths.length == this.pathsCount) {
		System.arraycopy(
			this.paths,
			0,
			this.paths = new IPath[this.pathsCount * 2],
			0,
			this.pathsCount);
	}
	this.paths[this.pathsCount++] = path;
}
/* (non-Javadoc)
 * @see IJavaSearchScope#encloses(String)
 */
public boolean encloses(String resourcePathString) {
	int separatorIndex = resourcePathString.indexOf(JAR_FILE_ENTRY_SEPARATOR);
	if (separatorIndex != -1) {
		resourcePathString = resourcePathString.substring(0, separatorIndex);
	}
	IPath resourcePath = new Path(resourcePathString);
	for (int i = 0; i < this.pathsCount; i++){
		if (this.paths[i].isPrefixOf(resourcePath)) {
			return true;
		}
	}
	return false;
}
/**
 * Returns whether this search scope encloses the given resource.
 */
protected boolean encloses(IResource element) {
	boolean encloses = false;
	IPath elementPath = element.getFullPath();
	for (int i = 0; i < this.pathsCount; i++) {
		if (this.paths[i].isPrefixOf(elementPath)) {
			encloses = true;
			break;
		}
	}
	fLastCheckedResource = element;
	fLastResult = encloses;
	return encloses;
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
	try {
		Vector paths = new Vector();
		IJavaModel javaModel = JavaModelManager.getJavaModel(ResourcesPlugin.getWorkspace());
		IWorkspaceRoot root = javaModel.getWorkspace().getRoot();
		for (int i = 0; i < this.elementCount; i++){
			IResource element = this.elements[i];
			IPath path = element.getProject().getFullPath();
			IProject project = element.getProject();
			if (project.exists() && project.isOpen()) {
				if (!paths.contains(path)) paths.add(path);
				if (this.includesClasspaths) {
					IJavaProject javaProject = javaModel.getJavaProject(project.getName());
					IClasspathEntry[] entries = javaProject.getResolvedClasspath(true);
					for (int j = 0; j < entries.length; j++) {
						IClasspathEntry entry = entries[j];
						switch (entry.getEntryKind()) {
							case IClasspathEntry.CPE_PROJECT:
								path = entry.getPath();
								if (!paths.contains(path) && root.getProject(path.lastSegment()).isAccessible()) {
									paths.add(path);
								}
								break;
							case IClasspathEntry.CPE_LIBRARY:
								if (this.includesBinaries) {
									path = entry.getPath();
									if (!paths.contains(path)) paths.add(path);
								}
								break;
						}
					}
				}
			}
		}
		IPath[] result = new IPath[paths.size()];
		paths.copyInto(result);
		return result;
	} catch (JavaModelException e) {
		return new IPath[0];
	}
}
/* (non-Javadoc)
 * @see IJavaSearchScope#includesBinaries()
 */
public boolean includesBinaries() {
	return this.includesBinaries;
}
/* (non-Javadoc)
 * @see IJavaSearchScope#includesClasspaths()
 */
public boolean includesClasspaths() {
	return this.includesClasspaths;
}
/* (non-Javadoc)
 * @see IJavaSearchScope#setIncludesBinaries
 */
public void setIncludesBinaries(boolean includesBinaries) {
	this.includesBinaries = includesBinaries;
}
/* (non-Javadoc)
 * @see IJavaSearchScope#setIncludeClasspaths
 */
public void setIncludesClasspaths(boolean includesClasspaths) {
	this.includesClasspaths = includesClasspaths;
}
}
