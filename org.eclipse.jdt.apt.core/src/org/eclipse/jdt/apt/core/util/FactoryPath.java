/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.*;
import org.eclipse.jdt.apt.core.internal.util.FactoryPathUtil;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Provides access to the annotation processor factory path for a Java project.
 * This class should not be instantiated or subclassed.
 */
public final class FactoryPath {
	
	// private c'tor to prevent instantiation
	private FactoryPath() {
	}
	
	/**
	 * Add factory containers to the list for a project.  If the container
	 * is already in the project's list, it will remain but will take on
	 * the new value of the 'enabled' attribute.
	 * The resulting list will be saved to the appropriate settings file.
	 * If there is an error accessing the file an exception will be thrown.
	 * @param jproj a project, or null for the workspace list.
	 * @param adds a map of factory containers to add to the list.  The value
	 * indicates whether the container's factories are to be enabled.
	 */
	public static synchronized void addContainers(
			IJavaProject jproj, Map<FactoryContainer, Boolean> adds) 
			throws IOException, CoreException {
		Map<FactoryContainer, Boolean> existing = FactoryPathUtil.getAllContainers(jproj);
		existing.putAll(adds);
		setContainers(jproj, existing);
	}

	/**
	 * Returns all containers for the provided project, including disabled ones.
	 * @param jproj The java project in question, or null for the workspace
	 * @return an ordered map, where the key is the container and the value 
	 * indicates whether the container is enabled.
	 */
	public static synchronized Map<FactoryContainer, Boolean> getAllContainers(IJavaProject jproj) {
		return FactoryPathUtil.getAllContainers(jproj);
	}

	/**
	 * Get a factory path corresponding to the default values: if jproj is
	 * non-null, return the current workspace factory path; if jproj is null,
	 * return the default list of plugin factories.
	 */
	public static Map<FactoryContainer, Boolean> getDefaultFactoryPath(IJavaProject jproj) {
		return FactoryPathUtil.getDefaultFactoryPath(jproj);
	}

	/**
	 * Get the factory containers for this project. If no project-level configuration
	 * is set, the workspace config will be returned. Any disabled containers
	 * will not be returned.
	 * 
	 * @param jproj The java project in question. 
	 * @return an ordered list of all enabled factory containers.
	 */
	public static synchronized List<FactoryContainer> getEnabledContainers(IJavaProject jproj) {
		// this map is ordered.
		Map<FactoryContainer, Boolean> containers = FactoryPathUtil.getAllContainers(jproj);
		List<FactoryContainer> result = new ArrayList<FactoryContainer>(containers.size());
		for (Map.Entry<FactoryContainer, Boolean> entry : containers.entrySet()) {
			if (entry.getValue()) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	/**
	 * Has an explicit factory path been set for the specified project, or
	 * is it just defaulting to the workspace settings? 
	 * @param project
	 * @return true if there is a project-specific factory path.
	 */
	public static boolean hasProjectSpecificFactoryPath(IJavaProject jproj) {
		if (null == jproj) {
			// say no, even if workspace-level factory path does exist. 
			return false;
		}
		return FactoryPathUtil.doesFactoryPathFileExist(jproj);
	}
	
	/**
	 * Create a factory container based on an external jar file (not in the 
	 * workspace).
	 * @param jar a java.io.File representing the jar file.
	 */
	public static FactoryContainer newExtJarFactoryContainer(File jar) {
		return new ExtJarFactoryContainer(jar);
	}
	
	/**
	 * Create a factory container based on a jar file in the workspace.
	 * @param an Eclipse IPath representing the jar file; the path is
	 * relative to the workspace root.
	 */
	public static FactoryContainer newWkspJarFactoryContainer(IPath jar) {
		return new WkspJarFactoryContainer(jar);
	}
	
	/**
	 * Create a factory container based on an Eclipse plugin.  The plugin
	 * is assumed to extend org.eclipse.jdt.apt.annotationProcessorFactory.
	 * @param pluginId the fully qualified id of the plugin, e.g.,
	 * "com.example.annotations"
	 */
	public static FactoryContainer newPluginFactoryContainer(String pluginId) {
		return new PluginFactoryContainer(pluginId);
	}
	
	/**
	 * Create a factory container based on an external jar file specified 
	 * by a classpath variable (and possibly a path relative to that variable).
	 * @param jar an Eclipse IPath representing the jar file; the first
	 * segment of the path is assumed to be the variable name.
	 */
	public static FactoryContainer newVarJarFactoryContainer(IPath jar) {
		return new VarJarFactoryContainer(jar);
	}

	/**
	 * Remove a processor factory container from the list for a project.  
	 * The resulting list will be saved to the appropriate settings file.
	 * If there is an error accessing the file an exception will be thrown.
	 * @param jproj a project, or null for the workspace list.
	 * @param container a factory container.
	 */
	public static synchronized void removeContainer(
			IJavaProject jproj, FactoryContainer container) 
			throws IOException, CoreException {
		Map<FactoryContainer, Boolean> existing = FactoryPathUtil.getAllContainers(jproj);
		existing.remove(container);
		setContainers(jproj, existing);
	}

	/**
	 * Set or reset the factory containers for a given project or the workspace.
	 * @param jproj the java project, or null for the workspace
	 * @param containers an ordered map whose key is a factory container and
	 * whose value indicates whether the container's factories are enabled;
	 * or null, to restore defaults.
	 */
	public static synchronized void setContainers(IJavaProject jproj, Map<FactoryContainer, Boolean> containers) 
	throws IOException, CoreException 
	{
		FactoryPathUtil.saveFactoryPathFile(jproj, containers);
		// The factory path isn't saved to the Eclipse preference store,
		// so we can't rely on the ChangeListener mechanism.
		AnnotationProcessorFactoryLoader.getLoader().reset();
	}

}
