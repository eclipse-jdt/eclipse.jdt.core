/*******************************************************************************
 * Copyright (c) 2001, 2003 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     IBM Corporation - added support for requesting updates of a particular
 *                       container for generic container operations.
 * 						 - canUpdateClasspathContainer(IPath, IJavaProject)
 * 						 - requestClasspathContainerUpdate(IPath, IJavaProject, IClasspathContainer)
 ******************************************************************************/

package org.eclipse.jdt.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Abstract base implementation of all classpath container initializer.
 * Classpath variable containers are used in conjunction with the
 * "org.eclipse.jdt.core.classpathContainerInitializer" extension point.
 * <p>
 * Clients should subclass this class to implement a specific classpath
 * container initializer. The subclass must have a public 0-argument
 * constructor and a concrete implementation of <code>resolve</code>.
 * <p>
 * Multiple classpath containers can be registered, each of them declares
 * the container ID they can handle, so as to narrow the set of containers they
 * can resolve, i.e. a container initializer is guaranteed to only be activated to 
 * resolve containers which match the ID they registered onto.
 * <p>
 * In case multiple container initializers collide on the same container ID, the first
 * registered one will be invoked.
 * 
 * @see IClasspathEntry
 * @see IClasspathContainer
 * @since 2.0
 */

public abstract class ClasspathContainerInitializer {
	
   /**
     * Creates a new classpath container initializer.
     */
    public ClasspathContainerInitializer() {
    }

    /**
     * Binds a classpath container to a <code>IClasspathContainer</code> for a given project,
     * or silently fails if unable to do so.
     * <p>
     * A container is identified by a container path, which must be formed of two segments.
     * The first segment is used as a unique identifier (which this initializer did register onto), and
     * the second segment can be used as an additional hint when performing the resolution.
     * <p>
     * The initializer is invoked if a container path needs to be resolved for a given project, and no
     * value for it was recorded so far. The implementation of the initializer can set the corresponding 
     * container using <code>JavaCore#setClasspathContainer</code>.
     * <p>
     * @param containerPath a two-segment path (ID/hint) identifying the container that needs 
     * 	to be resolved
     * @param project the Java project in which context the container is to be resolved.
     *    This allows generic containers to be bound with project specific values.
     * 
     * @see JavaCore#getClasspathContainer(IPath, IJavaProject)
     * @see JavaCore#setClasspathContainer(IPath, IJavaProject[], IClasspathContainer[], IProgressMonitor)
     * @see IClasspathContainer
     */
    public abstract void initialize(IPath containerPath, IJavaProject project) throws CoreException;
    
    /**
     * Returns <code>true</code> if this container initializer can be requested to perform updates 
     * on its own container values. If so, then an update request will be performed using
     * <code>ClasspathContainerInitializer#requestClasspathContainerUpdate</code>/
     * <p>
     * @param containerPath - the path of the container which requires to be updated
     * @param project - the project for which the container is to be updated
     * @return boolean - returns <code>true</code> if the container can be updated
     * @since 2.1
     */
    public boolean canUpdateClasspathContainer(IPath containerPath, IJavaProject project) {
    	
		// By default, classpath container initializers do not accept updating containers
    	return false; 
    }

	/**
	 * Request a registered container definition to be updated according to a container suggestion. The container suggestion 
	 * only acts as a place-holder to pass along the information to update the matching container definition(s) held by the 
	 * container initializer. In particular, it is not expected to store the container suggestion as is, but rather adjust 
	 * the actual container definition based on suggested changes.
	 * <p>
	 * IMPORTANT: In reaction to receiving an update request, a container initializer will update the corresponding
	 * container definition (after reconciling changes) at its earliest convenience, using 
	 * <code>JavaCore#setClasspathContainer(IPath, IJavaProject[], IClasspathContainer[], IProgressMonitor)</code>. 
	 * Until it does so, the update will not be reflected in the Java Model.
	 * <p>
	 * In order to anticipate whether the container initializer allows to update its containers, the predicate
	 * <code>JavaCore#canUpdateClasspathContainer</code> should be used.
	 * <p>
	 * @param containerPath - the path of the container which requires to be updated
     * @param project - the project for which the container is to be updated
	 * @param containerSuggestion - a suggestion to update the corresponding container definition
	 * @throws CoreException when <code>JavaCore#setClasspathContainer</code> would throw any.
	 * @see JavaCore#setClasspathContainer(IPath, IJavaProject[], IClasspathContainer[], IProgressMonitor)
	 * @see ClasspathContainerInitializer#canUpdateClasspathContainer(IPath, IJavaProject)
	 * @since 2.1
	 */
    public void requestClasspathContainerUpdate(IPath containerPath, IJavaProject project, IClasspathContainer containerSuggestion) throws CoreException {

		// By default, classpath container initializers do not accept updating containers
    }

	/**
	 * Returns a readable description for a container path. A readable description for a container path can be
	 * used for improving the display of references to container, without actually needing to resolve them.
	 * A good implementation should answer a description consistent with the description of the associated 
	 * target container (see <code>IClasspathContainer.getDescription()</code>).
	 * 
	 * @param containerPath - the path of the container which requires a readable description
	 * @return String - a string description of the container
	 * @since 2.1
	 */    
    public String getDescription(IPath containerPath) {
    	
    	// By default, a container path is the only available description
    	return containerPath.makeRelative().toString();
    }
}

