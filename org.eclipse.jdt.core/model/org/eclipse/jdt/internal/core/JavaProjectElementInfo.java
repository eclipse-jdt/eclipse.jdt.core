/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jdt.internal.core.util.HashtableOfArrayToObject;

/** 
 * Info for IJavaProject.
 * <p>
 * Note: <code>getChildren()</code> returns all of the <code>IPackageFragmentRoots</code>
 * specified on the classpath for the project.  This can include roots external to the
 * project. See <code>JavaProject#getAllPackageFragmentRoots()</code> and 
 * <code>JavaProject#getPackageFragmentRoots()</code>.  To get only the <code>IPackageFragmentRoots</code>
 * that are internal to the project, use <code>JavaProject#getChildren()</code>.
 */

/* package */
class JavaProjectElementInfo extends OpenableElementInfo {

	static class ProjectCache {
		ProjectCache(IPackageFragmentRoot[] allPkgFragmentRootsCache, HashtableOfArrayToObject allPkgFragmentsCache, Map pathToResolvedEntries) {
			this.allPkgFragmentRootsCache = allPkgFragmentRootsCache;
			this.allPkgFragmentsCache = allPkgFragmentsCache;
			this.pathToResolvedEntries = pathToResolvedEntries;
		}
		
		/*
		 * A cache of all package fragment roots of this project.
		 */
		public IPackageFragmentRoot[] allPkgFragmentRootsCache;
		
		/*
		 * A cache of all package fragments in this project.
		 * (a map from String[] (the package name) to IPackageFragmentRoot[] (the package fragment roots that contain a package fragment with this name)
		 */
		public HashtableOfArrayToObject allPkgFragmentsCache;
	
		public Map pathToResolvedEntries;		
	}
	
	/**
	 * A array with all the non-java resources contained by this PackageFragment
	 */
	private Object[] nonJavaResources;
	
	ProjectCache projectCache;
	
	/**
	 * Create and initialize a new instance of the receiver
	 */
	public JavaProjectElementInfo() {
		this.nonJavaResources = null;
	}
	
	/**
	 * Compute the non-java resources contained in this java project.
	 */
	private Object[] computeNonJavaResources(JavaProject project) {
		
		// determine if src == project and/or if bin == project
		IPath projectPath = project.getProject().getFullPath();
		boolean srcIsProject = false;
		boolean binIsProject = false;
		char[][] inclusionPatterns = null;
		char[][] exclusionPatterns = null;
		IClasspathEntry[] classpath = null;
		IPath projectOutput = null;
		try {
			classpath = project.getResolvedClasspath(true/*ignoreUnresolvedEntry*/, false/*don't generateMarkerOnError*/, false/*don't returnResolutionInProgress*/);
			for (int i = 0; i < classpath.length; i++) {
				IClasspathEntry entry = classpath[i];
				if (projectPath.equals(entry.getPath())) {
					srcIsProject = true;
					inclusionPatterns = ((ClasspathEntry)entry).fullInclusionPatternChars();
					exclusionPatterns = ((ClasspathEntry)entry).fullExclusionPatternChars();
					break;
				}
			}
			projectOutput = project.getOutputLocation();
			binIsProject = projectPath.equals(projectOutput);
		} catch (JavaModelException e) {
			// ignore
		}

		Object[] resources = new IResource[5];
		int resourcesCounter = 0;
		try {
			IResource[] members = ((IContainer) project.getResource()).members();
			for (int i = 0, max = members.length; i < max; i++) {
				IResource res = members[i];
				switch (res.getType()) {
					case IResource.FILE :
						IPath resFullPath = res.getFullPath();
						String resName = res.getName();
						
						// ignore a jar file on the classpath
						if (org.eclipse.jdt.internal.compiler.util.Util.isArchiveFileName(resName) && this.isClasspathEntryOrOutputLocation(resFullPath, classpath, projectOutput)) {
							break;
						}
						// ignore .java file if src == project
						if (srcIsProject 
							&& Util.isValidCompilationUnitName(resName)
							&& !Util.isExcluded(res, inclusionPatterns, exclusionPatterns)) {
							break;
						}
						// ignore .class file if bin == project
						if (binIsProject && Util.isValidClassFileName(resName)) {
							break;
						}
						// else add non java resource
						if (resources.length == resourcesCounter) {
							// resize
							System.arraycopy(
								resources,
								0,
								(resources = new IResource[resourcesCounter * 2]),
								0,
								resourcesCounter);
						}
						resources[resourcesCounter++] = res;
						break;
					case IResource.FOLDER :
						resFullPath = res.getFullPath();
						
						// ignore non-excluded folders on the classpath or that correspond to an output location
						if ((srcIsProject && !Util.isExcluded(res, inclusionPatterns, exclusionPatterns) && Util.isValidFolderNameForPackage(res.getName()))
								|| this.isClasspathEntryOrOutputLocation(resFullPath, classpath, projectOutput)) {
							break;
						}
						// else add non java resource
						if (resources.length == resourcesCounter) {
							// resize
							System.arraycopy(
								resources,
								0,
								(resources = new IResource[resourcesCounter * 2]),
								0,
								resourcesCounter);
						}
						resources[resourcesCounter++] = res;
				}
			}
			if (resources.length != resourcesCounter) {
				System.arraycopy(
					resources,
					0,
					(resources = new IResource[resourcesCounter]),
					0,
					resourcesCounter);
			}
		} catch (CoreException e) {
			resources = NO_NON_JAVA_RESOURCES;
			resourcesCounter = 0;
		}
		return resources;
	}
	
	ProjectCache getProjectCache(JavaProject project) {
		ProjectCache cache = this.projectCache;
		if (cache == null) {
			IPackageFragmentRoot[] roots;
			Map reverseMap = new HashMap(3);
			try {
				roots = project.getAllPackageFragmentRoots(reverseMap);
			} catch (JavaModelException e) {
				// project does not exist: cannot happen since this is the info of the project
				roots = new IPackageFragmentRoot[0];
				reverseMap.clear();
			}
			HashtableOfArrayToObject fragmentsCache = new HashtableOfArrayToObject();
			for (int i = 0, length = roots.length; i < length; i++) {
				IPackageFragmentRoot root = roots[i];
				IJavaElement[] frags = null;
				try {
					frags = root.getChildren();
				} catch (JavaModelException e) {
					// root doesn't exist: ignore
					continue;
				}
				for (int j = 0, length2 = frags.length; j < length2; j++) {
					PackageFragment fragment= (PackageFragment) frags[j];
					String[] pkgName = fragment.names;
					Object existing = fragmentsCache.get(pkgName);
					if (existing == null) {
						fragmentsCache.put(pkgName, root);
					} else {
						if (existing instanceof PackageFragmentRoot) {
							fragmentsCache.put(pkgName, new IPackageFragmentRoot[] {(PackageFragmentRoot) existing, root});
						} else {
							IPackageFragmentRoot[] entry= (IPackageFragmentRoot[]) existing;
							IPackageFragmentRoot[] copy= new IPackageFragmentRoot[entry.length + 1];
							System.arraycopy(entry, 0, copy, 0, entry.length);
							copy[entry.length]= root;
							fragmentsCache.put(pkgName, copy);
						}
					}
				}
			}
			cache = new ProjectCache(roots, fragmentsCache, reverseMap);
			this.projectCache = cache;
		}
		return cache;
	}
	
	/**
	 * Returns an array of non-java resources contained in the receiver.
	 */
	Object[] getNonJavaResources(JavaProject project) {

		if (this.nonJavaResources == null) {
			this.nonJavaResources = computeNonJavaResources(project);
		}
		return this.nonJavaResources;
	}

	/*
	 * Returns whether the given path is a classpath entry or an output location.
	 */
	private boolean isClasspathEntryOrOutputLocation(IPath path, IClasspathEntry[] resolvedClasspath, IPath projectOutput) {
		if (projectOutput.equals(path)) return true;
		for (int i = 0, length = resolvedClasspath.length; i < length; i++) {
			IClasspathEntry entry = resolvedClasspath[i];
			if (entry.getPath().equals(path)) {
				return true;
			}
			IPath output;
			if ((output = entry.getOutputLocation()) != null && output.equals(path)) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * Creates a new name lookup for this project info. 
	 * The given project is assumed to be the handle of this info.
	 * This name lookup first looks in the given working copies.
	 */
	NameLookup newNameLookup(JavaProject project, ICompilationUnit[] workingCopies) {
		ProjectCache cache = getProjectCache(project);
		return new NameLookup(cache.allPkgFragmentRootsCache, cache.allPkgFragmentsCache, workingCopies, cache.pathToResolvedEntries);
	}
	
	/*
	 * Reset the package fragment roots and package fragment caches
	 */
	void resetCaches() {
		this.projectCache = null;
	}
	
	/**
	 * Set the fNonJavaResources to res value
	 */
	void setNonJavaResources(Object[] resources) {

		this.nonJavaResources = resources;
	}
	
}
