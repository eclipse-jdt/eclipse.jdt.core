/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaModelException;

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

	/**
	 * The name lookup facility to use with this project.
	 */
	protected NameLookup fNameLookup = null;

	/**
	 * The searchable builder environment facility used
	 * with this project (doubles as the builder environment). 
	 */
	protected SearchableEnvironment fSearchableEnvironment = null;

	/**
	 * A array with all the non-java resources contained by this PackageFragment
	 */
	private Object[] fNonJavaResources;

	/**
	 * Create and initialize a new instance of the receiver
	 */
	public JavaProjectElementInfo() {
		fNonJavaResources = null;
	}
	
	/**
	 * Compute the non-java resources contained in this java project.
	 */
	private Object[] computeNonJavaResources(JavaProject project) {
		
		// determine if src == project and/or if bin == project
		IPath projectPath = project.getProject().getFullPath();
		boolean srcIsProject = false;
		boolean binIsProject = false;
		char[][] exclusionPatterns = null;
		IClasspathEntry[] classpath = null;
		IPath projectOutput = null;
		try {
			classpath = project.getResolvedClasspath(true/*ignore unresolved variable*/);
			for (int i = 0; i < classpath.length; i++) {
				IClasspathEntry entry = classpath[i];
				if (projectPath.equals(entry.getPath())) {
					srcIsProject = true;
					exclusionPatterns = ((ClasspathEntry)entry).fullExclusionPatternChars();
					break;
				}
			}
			projectOutput = project.getOutputLocation();
			binIsProject = projectPath.equals(projectOutput);
		} catch (JavaModelException e) {
			// ignore
		}

		Object[] nonJavaResources = new IResource[5];
		int nonJavaResourcesCounter = 0;
		try {
			IResource[] members = ((IContainer) project.getResource()).members();
			for (int i = 0, max = members.length; i < max; i++) {
				IResource res = members[i];
				switch (res.getType()) {
					case IResource.FILE :
						IPath resFullPath = res.getFullPath();
						String resName = res.getName();
						
						// ignore a jar file on the classpath
						if (Util.isArchiveFileName(resName) && this.isClasspathEntryOrOutputLocation(resFullPath, classpath, projectOutput)) {
							break;
						}
						// ignore .java file if src == project
						if (srcIsProject 
							&& Util.isValidCompilationUnitName(resName)
							&& !Util.isExcluded(res, exclusionPatterns)) {
							break;
						}
						// ignore .class file if bin == project
						if (binIsProject && Util.isValidClassFileName(resName)) {
							break;
						}
						// else add non java resource
						if (nonJavaResources.length == nonJavaResourcesCounter) {
							// resize
							System.arraycopy(
								nonJavaResources,
								0,
								(nonJavaResources = new IResource[nonJavaResourcesCounter * 2]),
								0,
								nonJavaResourcesCounter);
						}
						nonJavaResources[nonJavaResourcesCounter++] = res;
						break;
					case IResource.FOLDER :
						resFullPath = res.getFullPath();
						
						// ignore non-excluded folders on the classpath or that correspond to an output location
						if ((srcIsProject && !Util.isExcluded(res, exclusionPatterns))
								|| this.isClasspathEntryOrOutputLocation(resFullPath, classpath, projectOutput)) {
							break;
						}
						// else add non java resource
						if (nonJavaResources.length == nonJavaResourcesCounter) {
							// resize
							System.arraycopy(
								nonJavaResources,
								0,
								(nonJavaResources = new IResource[nonJavaResourcesCounter * 2]),
								0,
								nonJavaResourcesCounter);
						}
						nonJavaResources[nonJavaResourcesCounter++] = res;
				}
			}
			if (nonJavaResources.length != nonJavaResourcesCounter) {
				System.arraycopy(
					nonJavaResources,
					0,
					(nonJavaResources = new IResource[nonJavaResourcesCounter]),
					0,
					nonJavaResourcesCounter);
			}
		} catch (CoreException e) {
			nonJavaResources = NO_NON_JAVA_RESOURCES;
			nonJavaResourcesCounter = 0;
		}
		return nonJavaResources;
	}
	
	/**
	 * @see IJavaProject
	 */
	protected NameLookup getNameLookup() {

		return fNameLookup;
	}
	
	/**
	 * Returns an array of non-java resources contained in the receiver.
	 */
	Object[] getNonJavaResources(JavaProject project) {

		Object[] nonJavaResources = fNonJavaResources;
		if (nonJavaResources == null) {
			nonJavaResources = computeNonJavaResources(project);
			fNonJavaResources = nonJavaResources;
		}
		return nonJavaResources;
	}
	
	/**
	 * @see IJavaProject 
	 */
	protected SearchableEnvironment getSearchableEnvironment() {

		return fSearchableEnvironment;
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
	
	protected void setNameLookup(NameLookup newNameLookup) {

		fNameLookup = newNameLookup;

		// Reinitialize the searchable name environment since it caches
		// the name lookup.
		fSearchableEnvironment = null;
	}
	
	/**
	 * Set the fNonJavaResources to res value
	 */
	synchronized void setNonJavaResources(Object[] resources) {

		fNonJavaResources = resources;
	}
	
	protected void setSearchableEnvironment(SearchableEnvironment newSearchableEnvironment) {

		fSearchableEnvironment = newSearchableEnvironment;
	}
}