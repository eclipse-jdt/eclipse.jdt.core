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

package org.eclipse.jdt.core;

import org.eclipse.core.runtime.IPath;

/** 
 * Interface of a classpath container.
 * A classpath container provides a way to indirectly reference a set of classpath entries through
 * a classpath entry of kind <code>CPE_CONTAINER</code>. Typically, a classpath container can
 * be used to describe a complex library composed of multiple JARs, projects or classpath variables,
 * considering also that containers can map to different set of entries on each project, i.e. several 
 * projects can reference the same generic container path, but have each of them actually bound 
 * to a different container object.
 * <p>
 * The set of entries associated with a classpath container may contain any of the following:
 * <ul>
 * <li> library entries (<code>CPE_LIBRARY</code>) </li>
 * <li> project entries (<code>CPE_PROJECT</code>) </li>
 * <li> variable entries (<code>CPE_VARIABLE</code>), note that these are not automatically resolved </li>
 * </ul>
 * In particular, a classpath container cannot reference further classpath containers.
 * <p>
 * Classpath container values are persisted locally to the workspace, but are not preserved from a 
 * session to another. It is thus highly recommended to register a <code>ClasspathContainerInitializer</code> 
 * for each referenced container (through the extension point "org.eclipse.jdt.core.ClasspathContainerInitializer").
 * <p>
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
	 * Answers the container path identifying this container.
	 * A container path is a 2-segments path, formed by an ID segment followed with an
	 * extra segment which can be used as an additional hint for resolving to this container.
	 * <p>
	 * The container ID is also used to identify a<code>ClasspathContainerInitializer</code>
	 * registered on the extension point "org.eclipse.jdt.core.classpathContainerInitializer", which can
	 * be invoked if needing to resolve the container before it is explicitely set.
	 * <p>
	 * @return IPath - the container path that is associated with this container
	 */	
    IPath getPath();
}

