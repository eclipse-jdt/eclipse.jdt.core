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

import org.eclipse.core.runtime.IPath;

/**
 * Abstract base implementation of all classpath container resolver.
 * Classpath variable containers are used in conjunction with the
 * "org.eclipse.jdt.core.classpathContainerResolver" extension point.
 * <p>
 * Clients should subclass this class to implement a specific classpath
 * container resolver. The subclass must have a public 0-argument
 * constructor and a concrete implementation of <code>resolve</code>.
 * <p>
 * Multiple classpath containers can be registered, each of them declares
 * a path prefix to narrow the set of container paths they can handle, i.e.
 * a container resolver is guaranteed to only be requested to resolve container
 * paths which match the prefix they registered onto.
 * <p>
 * This allows to register generic container resolvers for a set of container paths,
 * e.g.
 * A container can denote a generic "JRE" or a more specific "JRE/1.3" and both
 * can be handled by the same container resolver registered for path prefix "JRE".
 * This resolver can then use the rest of the path as a clue for expanding the container
 * into a set of classpath entries.
 * <p>
 * In case multiple container resolvers collide on the same prefixes, the most
 * specific one will be activated, in case of ambiguity, the first registered one
 * will be invoked.
 * </p>
 * @see IClasspathEntry
 * @since 2.0
 */

public abstract class ClasspathContainerResolver {
	
   /**
     * Creates a new classpath container resolver.
     */
    public ClasspathContainerResolver() {
    }

    /**
     * Binds a classpath container to a set of classpath entries for a given project,
     * or returns <code>null</code> if this cannot be done.
     *
     * @param containerPath - the path identifying the classpath container
     *    that needs to be resolved
     * @param project - the Java project in which context the container is to be resolved.
     *    This allows generic containers to be bound with project specific values.
     * 
     * @see JavaCore#updateClasspathContainer     
     */
    public abstract IClasspathEntry[] resolve(IPath containerPath, IJavaProject project);
}

