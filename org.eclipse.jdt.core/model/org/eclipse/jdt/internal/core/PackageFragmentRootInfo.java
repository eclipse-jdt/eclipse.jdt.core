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
package org.eclipse.jdt.internal.core;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

/**
 * The element info for <code>PackageFragmentRoot</code>s.
 */
class PackageFragmentRootInfo extends OpenableElementInfo {

	/**
	 * The SourceMapper for this JAR (or <code>null</code> if
	 * this JAR does not have source attached).
	 */
	protected SourceMapper sourceMapper = null;

	/**
	 * The kind of the root associated with this info.
	 * Valid kinds are: <ul>
	 * <li><code>IPackageFragmentRoot.K_SOURCE</code>
	 * <li><code>IPackageFragmentRoot.K_BINARY</code></ul>
	 */
	protected int fRootKind= IPackageFragmentRoot.K_SOURCE;

	/**
	 * A array with all the non-java resources contained by this PackageFragment
	 */
	protected Object[] fNonJavaResources;
/**
 * Create and initialize a new instance of the receiver
 */
public PackageFragmentRootInfo() {
	fNonJavaResources = null;
}
/**
 * Starting at this folder, create non-java resources for this package fragment root 
 * and add them to the non-java resources collection.
 * 
 * @exception JavaModelException  The resource associated with this package fragment does not exist
 */
static Object[] computeFolderNonJavaResources(JavaProject project, IContainer folder, char[][] exclusionPatterns) throws JavaModelException {
	Object[] nonJavaResources = new IResource[5];
	int nonJavaResourcesCounter = 0;
	try {
		IClasspathEntry[] classpath = project.getResolvedClasspath(true/*ignore unresolved variable*/);
		IResource[] members = folder.members();
		nextResource: for (int i = 0, max = members.length; i < max; i++) {
			IResource member = members[i];
			switch (member.getType()) {
				case IResource.FILE :
					String fileName = member.getName();
					
					// ignore .java files that are not excluded
					if (Util.isValidCompilationUnitName(fileName) && !Util.isExcluded(member, exclusionPatterns)) 
						continue nextResource;
					// ignore .class files
					if (Util.isValidClassFileName(fileName)) 
						continue nextResource;
					// ignore .zip or .jar file on classpath
					if (Util.isArchiveFileName(fileName) && isClasspathEntry(member.getFullPath(), classpath)) 
						continue nextResource;
					break;

				case IResource.FOLDER :
					// ignore valid packages or excluded folders that correspond to a nested pkg fragment root
					if (Util.isValidFolderNameForPackage(member.getName())
							&& (!Util.isExcluded(member, exclusionPatterns) 
								|| isClasspathEntry(member.getFullPath(), classpath)))
						continue nextResource;
					break;
			}
			if (nonJavaResources.length == nonJavaResourcesCounter) {
				// resize
				System.arraycopy(nonJavaResources, 0, (nonJavaResources = new IResource[nonJavaResourcesCounter * 2]), 0, nonJavaResourcesCounter);
			}
			nonJavaResources[nonJavaResourcesCounter++] = member;

		}
		if (nonJavaResources.length != nonJavaResourcesCounter) {
			System.arraycopy(nonJavaResources, 0, (nonJavaResources = new IResource[nonJavaResourcesCounter]), 0, nonJavaResourcesCounter);
		}
		return nonJavaResources;
	} catch (CoreException e) {
		throw new JavaModelException(e);
	}
}
/**
 * Compute the non-package resources of this package fragment root.
 * 
 * @exception JavaModelException  The resource associated with this package fragment root does not exist
 */
private Object[] computeNonJavaResources(IJavaProject project, IResource underlyingResource, PackageFragmentRoot handle) {
	Object[] nonJavaResources = NO_NON_JAVA_RESOURCES;
	try {
		// the underlying resource may be a folder or a project (in the case that the project folder
		// is actually the package fragment root)
		if (underlyingResource.getType() == IResource.FOLDER || underlyingResource.getType() == IResource.PROJECT) {
			nonJavaResources = 
				computeFolderNonJavaResources(
					(JavaProject)project, 
					(IContainer) underlyingResource,  
					handle.fullExclusionPatternChars());
		}
	} catch (JavaModelException e) {
	}
	return nonJavaResources;
}
/**
 * Returns an array of non-java resources contained in the receiver.
 */
synchronized Object[] getNonJavaResources(IJavaProject project, IResource underlyingResource, PackageFragmentRoot handle) {
	Object[] nonJavaResources = fNonJavaResources;
	if (nonJavaResources == null) {
		nonJavaResources = this.computeNonJavaResources(project, underlyingResource, handle);
		fNonJavaResources = nonJavaResources;
	}
	return nonJavaResources;
}
/**
 * Returns the kind of this root.
 */
public int getRootKind() {
	return fRootKind;
}
/**
 * Retuns the SourceMapper for this root, or <code>null</code>
 * if this root does not have attached source.
 */
protected SourceMapper getSourceMapper() {
	return this.sourceMapper;
}
private static boolean isClasspathEntry(IPath path, IClasspathEntry[] resolvedClasspath) {
	for (int i = 0, length = resolvedClasspath.length; i < length; i++) {
		IClasspathEntry entry = resolvedClasspath[i];
		if (entry.getPath().equals(path)) {
			return true;
		}
	}
	return false;
}
/**
 * Set the fNonJavaResources to res value
 */
synchronized void setNonJavaResources(Object[] resources) {
	fNonJavaResources = resources;
}
/**
 * Sets the kind of this root.
 */
protected void setRootKind(int newRootKind) {
	fRootKind = newRootKind;
}
/**
 * Sets the SourceMapper for this root.
 */
protected void setSourceMapper(SourceMapper mapper) {
	this.sourceMapper= mapper;
}
}
