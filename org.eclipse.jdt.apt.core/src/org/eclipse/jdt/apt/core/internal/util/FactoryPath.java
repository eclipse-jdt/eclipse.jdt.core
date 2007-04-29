/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.util;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.FactoryPluginManager;
import org.eclipse.jdt.apt.core.util.IFactoryPath;

/**
 * Provides access to the annotation processor factory path for a Java project.
 * This class should not be instantiated or subclassed.
 * 
 * The factory path is an ordered Map<FactoryContainer, FactoryPath.Attributes>.
 * Containers are things like jar files or plugins, that contain one or more
 * annotation processor factories.  In the context of a particular project,
 * processors are given precedence according to the order of their container on
 * the factory path; and they are executed according to the container's attributes
 * on the factory path.  
 */
public class FactoryPath implements IFactoryPath {
	
	/**
	 * Attributes of entries on the factory path.  These belong here,
	 * rather than on FactoryContainer itself, because the same container
	 * might have different attributes in different projects - e.g., it
	 * might be enabled in one project and disabled in another.
	 */
	public static class Attributes {
		/** Should this container's processors be executed? */
		private boolean _enabled;
		/** Should this container's processors execute in Sun apt compatibility mode? (Slow and limiting!) */
		private boolean _runInBatchMode;
		
		// CONSTRUCTORS
		public Attributes(boolean enabled, boolean runInBatchMode) {
			_enabled = enabled;
			_runInBatchMode = runInBatchMode;
		}
		public Attributes(Attributes attr) {
			_enabled = attr._enabled;
			_runInBatchMode = attr._runInBatchMode;
		}
		
		// SUPPORT
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Attributes))
				return false;
			Attributes oA = (Attributes)o;
			return (_enabled == oA._enabled) && (_runInBatchMode == oA._runInBatchMode );
		}
		public int hashCode() {
			return (_enabled ? 1 : 0) + (_runInBatchMode ? 2 : 0);
		}
		
		
		// GETTERS
		public boolean isEnabled() {
			return _enabled;
		}
		public boolean runInBatchMode() {
			return _runInBatchMode;
		}

		// SETTERS
		public void setEnabled(boolean enabled) {
			_enabled = enabled;
		}
		public void setRunInBatchMode(boolean runInBatchMode) {
			_runInBatchMode = runInBatchMode;
		}
	}
	
	/**
	 * The factory path.  
	 */
	private final Map<FactoryContainer, Attributes> _path = Collections.synchronizedMap(
			new LinkedHashMap<FactoryContainer, Attributes>());
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.apt.core.util.IFactoryPath#addExternalJar(java.io.File)
	 */
	public void addExternalJar(File jar) {
		FactoryContainer fc = FactoryPathUtil.newExtJarFactoryContainer(jar);
		Attributes a = new Attributes(true, false);
		internalAdd(fc, a);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.apt.core.util.IFactoryPath#removeExternalJar(java.io.File)
	 */
	public void removeExternalJar(File jar) {
		FactoryContainer fc = FactoryPathUtil.newExtJarFactoryContainer(jar);
		_path.remove(fc);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.apt.core.util.IFactoryPath#addVarJar(org.eclipse.core.runtime.IPath)
	 */
	public void addVarJar(IPath jarPath) {
		FactoryContainer fc = FactoryPathUtil.newVarJarFactoryContainer(jarPath);
		Attributes a = new Attributes(true, false);
		internalAdd(fc, a);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.apt.core.util.IFactoryPath#removeVarJar(org.eclipse.core.runtime.IPath)
	 */
	public void removeVarJar(IPath jarPath) {
		FactoryContainer fc = FactoryPathUtil.newVarJarFactoryContainer(jarPath);
		_path.remove(fc);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.apt.core.util.IFactoryPath#addWkspJar(org.eclipse.core.runtime.IPath)
	 */
	public void addWkspJar(IPath jarPath) {
		FactoryContainer fc = FactoryPathUtil.newWkspJarFactoryContainer(jarPath);
		Attributes a = new Attributes(true, false);
		internalAdd(fc, a);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.apt.core.util.IFactoryPath#removeWkspJar(org.eclipse.core.runtime.IPath)
	 */
	public void removeWkspJar(IPath jarPath) {
		FactoryContainer fc = FactoryPathUtil.newWkspJarFactoryContainer(jarPath);
		_path.remove(fc);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.apt.core.util.IFactoryPath#enablePlugin(java.lang.String)
	 */
	public void enablePlugin(String pluginId) throws CoreException {
		FactoryContainer fc = FactoryPluginManager.getPluginFactoryContainer(pluginId);
		Attributes a = _path.get(fc);
		if (a == null) {
			Status status = AptPlugin.createWarningStatus(new IllegalArgumentException(), 
					"Specified plugin was not found, so it could not be added to the annotation processor factory path: " + pluginId);  //$NON-NLS-1$
			throw new CoreException(status);
		}
		a.setEnabled(true);
		internalAdd(fc, a);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.apt.core.util.IFactoryPath#disablePlugin(java.lang.String)
	 */
	public void disablePlugin(String pluginId) {
		FactoryContainer fc = FactoryPluginManager.getPluginFactoryContainer(pluginId);
		Attributes a = _path.get(fc);
		if (a != null) {
			a.setEnabled(false);
		}
	}

	/**
	 * Add a single factory container to the head of the FactoryPath, 
	 * and save the new path to the appropriate settings file.  
	 * If the container specified is already in the project's list in 
	 * some other FactoryPathEntry, the existing entry will be removed 
	 * before the new one is added.
	 * @param fc must not be null.
	 */
	public void addEntryToHead(FactoryContainer fc, boolean enabled, boolean runInBatchMode) {
		Attributes a = new Attributes(enabled, runInBatchMode);
		internalAdd(fc, a);
	}
	
	/**
	 * Set the factory path based on the contents of an ordered map.
	 * @param map should be an ordered map, such as LinkedHashMap; should contain no
	 * nulls; and should contain no duplicate FactoryContainers.
	 */
	public void setContainers(Map<FactoryContainer, Attributes> map) {
		synchronized(_path) {
			_path.clear();
			_path.putAll(map);
		}
	}
	
	/**
	 * Add a factory container, and attributes, to the head of the list.
	 * If it already existed in the list, remove the old instance before
	 * adding the new one.
	 * <p>
	 * @param fc must not be null
	 * @param a must not be null
	 */
	private void internalAdd(FactoryContainer fc, Attributes a) {
		synchronized(_path) {
			_path.remove(fc);
			// LinkedHashMap doesn't have any way to add to the head,
			// so we're forced to do two copies.  Make the new map
			// large enough that we don't have to rehash midway through the putAll().
			Map<FactoryContainer, Attributes> newPath = 
				new LinkedHashMap<FactoryContainer, Attributes>(1 + 4*(_path.size() + 1)/3);
			newPath.put(fc, a);
			newPath.putAll(_path);
			_path.clear();
			_path.putAll(newPath);
		}
	}

	public Map<FactoryContainer, Attributes> getEnabledContainers() {
		Map<FactoryContainer, Attributes> map = new LinkedHashMap<FactoryContainer, Attributes>();
		synchronized(_path) {
			for (Map.Entry<FactoryContainer, Attributes> entry : _path.entrySet()) {
				Attributes attr = entry.getValue();
				if (attr.isEnabled()) {
					Attributes attrClone = new Attributes(attr);
					map.put(entry.getKey(), attrClone);
				}
			}
		}
		return map;
	}

	/**
	 * @return a copy of the path
	 */
	public Map<FactoryContainer, Attributes> getAllContainers() {
		Map<FactoryContainer, Attributes> map = new LinkedHashMap<FactoryContainer, Attributes>(_path.size());
		synchronized(_path) {
			for( Map.Entry<FactoryContainer, Attributes> entry : _path.entrySet() ){
				map.put( entry.getKey(), new Attributes(entry.getValue()) );
			}
		}
		return map;
	}

}
