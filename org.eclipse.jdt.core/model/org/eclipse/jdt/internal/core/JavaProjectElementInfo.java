package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.core.*;

import java.util.*;

/** 
 * Info for IJavaProject.
 * <p>
 * Note: <code>getChildren()</code> returns all of the <code>IPackageFragmentRoots</code>
 * specified on the classpath for the project.  This can include roots external to the
 * project. See <code>JavaProject#getAllPackageFragmentRoots()</code> and 
 * <code>JavaProject#getPackageFragmentRoots()</code>.  To get only the <code>IPackageFragmentRoots</code>
 * that are internal to the project, use <code>JavaProject#getChildren()</code>.
 */

/* package */ class JavaProjectElementInfo extends OpenableElementInfo {

	/**
	 * The classpath for this project
	 */
	protected IClasspathEntry[] fClasspath;

	/**
	 * The name lookup facility to use with this project.
	 */
	protected NameLookup fNameLookup = null;

	/**
	 * The searchable builder environment facility used
	 * with this project (doubles as the builder environment). 
	 */
	protected SearchableEnvironment fSearchableEnvironment= null;
	
	/**
	 * The output location for this project.
	 */
	protected IPath fOutputLocation = null;

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
	try {
		IClasspathEntry[] classpath = project.getResolvedClasspath(true);
		for (int i = 0; i < classpath.length; i++) {
			IClasspathEntry entry = classpath[i];
			if (projectPath.equals(entry.getPath())) {
				srcIsProject = true;
				break;
			}
		}
		binIsProject = projectPath.equals(project.getOutputLocation());
	} catch (JavaModelException e) {
		// ignore
	}
	
	Object[] nonJavaResources = new IResource[5];
	int nonJavaResourcesCounter = 0;
	try {
		IResource[] members = ((IContainer) project.getUnderlyingResource()).members();
		for (int i = 0, max = members.length; i < max; i++) {
			IResource res = members[i];
			switch (res.getType()) {
				case IResource.FILE :
					// check if this file might be a jar or a zip inside the build path
					IPath resFullPath = res.getFullPath();
					if (project.findPackageFragmentRoot(resFullPath) == null) {
						String extension = res.getFileExtension();
						if (extension != null) extension = extension.toLowerCase();
						// ignore .java file if src == project
						if (srcIsProject && "java".equals(extension)) { //$NON-NLS-1$
							break;
						}
						// ignore .class file if bin == project
						if (binIsProject && "class".equals(extension)) { //$NON-NLS-1$
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
					break;
				case IResource.FOLDER :
					resFullPath = res.getFullPath();
					if (!resFullPath.equals(project.getOutputLocation())
						&& project.findPackageFragmentRoot(resFullPath) == null
						&& project.findPackageFragment(resFullPath) == null) {
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
protected IPath getOutputLocation() {
	return fOutputLocation;
}
/**
 * Returns the classpath for this project
 */
protected IClasspathEntry[] getRawClasspath() {
	return fClasspath;
}
/**
 * @see IJavaProject 
 */
protected SearchableEnvironment getSearchableEnvironment() {
	return fSearchableEnvironment;
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
protected void setOutputLocation(IPath newOutputLocation) {
	fOutputLocation = newOutputLocation;
}
/**
 * Sets the classpath for this project
 */
protected void setRawClasspath(IClasspathEntry[] newClasspath) {
	fClasspath = newClasspath;
}
protected void setSearchableEnvironment(SearchableEnvironment newSearchableEnvironment) {
	fSearchableEnvironment = newSearchableEnvironment;
}
}
