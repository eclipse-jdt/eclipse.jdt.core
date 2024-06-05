/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.util;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * A minimal API for manipulating the annotation processor factory path.
 * The factory path is an ordered list of containers, each of which has
 * certain attributes, e.g., isEnabled, runInBatchMode.  Containers on
 * the path do not necessarily contain implementations of any particular
 * service; in particular, a path may have both Java 5 and Java 6 annotation
 * processors, as well as entries that contain supporting classes or
 * resources.
 * <p>
 * Clients should not implement this interface.
 */
public interface IFactoryPath {

	/**
	 * Add an external jar file (not in the workspace) to the head of the
	 * factory path.  If the jar is already on the path, move it to the
	 * head.  The jar will be added with default attributes, e.g.,
	 * isEnabled=true, runInBatchMode=false.
	 * @param jar a java.io.File representing the jar file.  Must not be null.
	 */
	public void addExternalJar(File jar);

	/**
	 * Remove an external jar file from the factory path.
	 * If the jar isn't on the path, do nothing.
	 * @param jar must not be null.
	 */
	public void removeExternalJar(File jar);

	/**
	 * Add a jar file in the workspace to the head of the
	 * factory path.  If the jar is already on the path, move it to the
	 * head.  The jar will be added with default attributes, e.g.,
	 * isEnabled=true, runInBatchMode=false.
	 * @param jarPath an Eclipse IPath representing the jar file; the path is
	 * relative to the workspace root.  Must not be null.
	 */
	public void addWkspJar(IPath jarPath);

	/**
	 * Remove a workspace-relative jar from the factory path.
	 * If the jar isn't on the path, do nothing.
	 * @param jarPath an Eclipse IPath representing the jar file; the path is
	 * relative to the workspace root.  Must not be null.
	 */
	public void removeWkspJar(IPath jarPath);

	/**
	 * Add a jar file in the workspace, specified with a classpath variable,
	 * to the head of the factory path.  If the jar is already on the path,
	 * move it to the head.  The jar will be added with default attributes,
	 * e.g., isEnabled=true, runInBatchMode=false.
	 * @param jarPath an Eclipse IPath representing the jar file; the first
	 * segment of the path is assumed to be the variable name.  Must not
	 * be null.
	 */
	public void addVarJar(IPath jarPath);

	/**
	 * Remove from the factory path a jar file whose name is based on a
	 * classpath variable.  If the jar isn't on the path, do nothing.
	 * @param jarPath an Eclipse IPath representing the jar file; the first
	 * segment of the path is assumed to be the variable name.  Must not
	 * be null.
	 */
	public void removeVarJar(IPath jarPath);

	/**
	 * Enable a plugin on the factory path, and move it to the head of the
	 * path.
	 * @param pluginId the unique id of the processor plugin, e.g.,
	 * "org.example.myProcessors"
	 * @throws CoreException if the plugin is not installed.
	 */
	public void enablePlugin(String pluginId) throws CoreException;

	/**
	 * Disable a plugin on the factory path.
	 * If the plugin is not installed, do nothing.
	 * @param pluginId the unique id of the processor plugin, e.g.,
	 * "org.example.myProcessors"
	 */
	public void disablePlugin(String pluginId);
}
