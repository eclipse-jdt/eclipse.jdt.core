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

/** TOFIX
 * Abstract base implementation of all classpath container initializer.
 * Classpath variable containers are used in conjunction with the
 * "org.eclipse.jdt.core.classpathContainerInitializer" extension point.
 * <p>
 * Clients should subclass this class to implement a specific classpath
 * container initializer. The subclass must have a public 0-argument
 * constructor and a concrete implementation of <code>initialize</code>.
 * <p>
 * Multiple classpath containers can be registered, each of them declares
 * a path prefix to narrow the set of container paths they can handle, i.e.
 * a container initializer is guaranteed to only be requested to resolve container
 * paths which match the prefix they registered onto.
 * <p>
 * This allows to register generic container initializers for a set of container paths,
 * e.g.
 * A container can denote a generic "JRE" or a more specific "JRE/1.3" and both
 * can be handled by the same container initializer registered for path prefix "JRE".
 * This initializer can then use the rest of the path as a clue for expanding the container
 * into a set of classpath entries.
 * <p>
 * In case multiple container initializers collide on the same prefixes, the most
 * specific one will be activated, in case of ambiguity, the first registered one
 * will be invoked.
 * </p>
 * @see IClasspathEntry
 * @since 2.0
 */

public interface IClasspathContainer {
	
	/**
	 * Kind for a container mapping to an application library
	 */
	int K_APPLICATION = 1;

	/**
	 * Kind for a container mapping to a system library
	 */
	int K_SYSTEM = 2;

	/**
	 * Answers the set of classpath entries this container is mapping to.
	 * <p>
	 * The set of entries associated with a classpath container may contain any of the following:
	 * <ul>
	 * <li> source entries (<code>CPE_SOURCE</code>) </li>
	 * <li> library entries (<code>CPE_LIBRARY</code>) </li>
	 * <li> project entries (<code>CPE_PROJECT</code>) </li>
	 * <li> variable entries (<code>CPE_VARIABLE</code>), note that these are not automatically resolved </li>
	 * </ul>
	 * A classpath container cannot reference further classpath containers.
	 * 
	 * @return IClasspathEntry[] - the classpath entries this container represents
	 * @see IClasspathEntry
	 */	
    IClasspathEntry[] getClasspathEntries();

	/**
	 * Answers a readable description of this container
	 * 
	 * @return String - a string description of the container
	 */	
    String getDescription();

	/**
	 * Answers the kind of this container. Can be either:
	 * <ul>
	 * <li><code>K_APPLICATION</code> if this container maps to an application library</li>
	 * <li><code>K_SYSTEM</code> if this container maps to a system library</li>
	 * </ul>
	 * Typically, system containers should be placed first on a build path.
	 */	
    int getKind();

	/**
	 * Answers the container path this container maps to.
	 * A container path is a segment path, formed by an ID segment followed with a clue segment.
	 * This container ID is used in conjunction with the  clue for resolving to this container.
	 * Note that the container ID may also identify a<code>ClasspathContainerInitializer</code>
	 * registered on the extension point "org.eclipse.jdt.core.classpathContainerInitializer", which can
	 * be invoked if needing to resolve the container before it is explicitely set.
	 * 
	 * @return IPath - the container path that is associated with this container
	 */	
    IPath getPath();
}

