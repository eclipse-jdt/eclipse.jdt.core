/*******************************************************************************
 * Copyright (c) 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jdt.core;

import org.eclipse.core.runtime.CoreException;

/**TOFIX
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
 * </p>
 * @see IClasspathEntry
 * @seeIClasspathContainer
 * 
 * @since 2.0
 */

public abstract class ClasspathContainerInitializer {
	
   /**
     * Creates a new classpath container initializer.
     */
    public ClasspathContainerInitializer() {
    }

    /**
     * Binds a classpath container to a <code>IClasspathContainer</code> for a given project.
     * The resolved container can be queried for the set of classpath entries the container maps to.
     * It may returns <code>null</code> if this resolver was unable to resolve the container ID.
     * <p>
     * A container is identified primarily by a containerID (which this resolver did register onto), and the
     * container may also define a clue that can be used by the resolver to perform.
     * <p>
     * @param containerID - the name identifying the classpath container that needs 
     * 	to be resolved
     * @param containerClue - an extra String which can be used by the resolver for binding
     * 	a particular containerID.
     * @param project - the Java project in which context the container is to be resolved.
     *    This allows generic containers to be bound with project specific values.
     * 
     * @see JavaCore#classpathContainerChanged
     * @see IClasspathContainer
     */
    public abstract void initialize(String containerID, String containerClue, IJavaProject project) throws CoreException;
}

