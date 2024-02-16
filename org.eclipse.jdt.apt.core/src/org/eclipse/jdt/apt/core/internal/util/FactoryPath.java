/*******************************************************************************
 * Copyright (c) 2005, 2015 BEA Systems, Inc.
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
 *   het@google.com - Bug 423254 - There is no way to tell if a project's factory path is different from the workspace default
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
 * The factory path is an ordered {@code Map<FactoryContainer, FactoryPath.Attributes>}.
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
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Attributes))
				return false;
			Attributes oA = (Attributes)o;
			return (_enabled == oA._enabled) && (_runInBatchMode == oA._runInBatchMode );
		}
		@Override
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
	 * The factory path. Stored in reverse order.
	 */
	private final Map<FactoryContainer, Attributes> _path = Collections.synchronizedMap(
			new LinkedHashMap<FactoryContainer, Attributes>());

	@Override
	public void addExternalJar(File jar) {
		FactoryContainer fc = FactoryPathUtil.newExtJarFactoryContainer(jar);
		Attributes a = new Attributes(true, false);
		internalAdd(fc, a);
	}

	@Override
	public void removeExternalJar(File jar) {
		FactoryContainer fc = FactoryPathUtil.newExtJarFactoryContainer(jar);
		_path.remove(fc);
	}

	@Override
	public void addVarJar(IPath jarPath) {
		FactoryContainer fc = FactoryPathUtil.newVarJarFactoryContainer(jarPath);
		Attributes a = new Attributes(true, false);
		internalAdd(fc, a);
	}

	@Override
	public void removeVarJar(IPath jarPath) {
		FactoryContainer fc = FactoryPathUtil.newVarJarFactoryContainer(jarPath);
		_path.remove(fc);
	}

	@Override
	public void addWkspJar(IPath jarPath) {
		FactoryContainer fc = FactoryPathUtil.newWkspJarFactoryContainer(jarPath);
		Attributes a = new Attributes(true, false);
		internalAdd(fc, a);
	}

	@Override
	public void removeWkspJar(IPath jarPath) {
		FactoryContainer fc = FactoryPathUtil.newWkspJarFactoryContainer(jarPath);
		_path.remove(fc);
	}

	@Override
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

	@Override
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
			for (Entry<FactoryContainer, Attributes> entry : getReversed(map.entrySet())) {
				_path.put(entry.getKey(), entry.getValue());
			}
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
			_path.put(fc, a);
		}
	}

	public Map<FactoryContainer, Attributes> getEnabledContainers() {
		Map<FactoryContainer, Attributes> map = new LinkedHashMap<>();
		synchronized(_path) {
			for (Map.Entry<FactoryContainer, Attributes> entry : getReversed(_path.entrySet())) {
				Attributes attr = entry.getValue();
				if (attr.isEnabled()) {
					Attributes attrClone = new Attributes(attr);
					map.put(entry.getKey(), attrClone);
				}
			}
		}
		return map;
	}

	private static <T> List<T> getReversed(Collection<T> collection) {
		ArrayList<T> result = new ArrayList<>(collection);
		Collections.reverse(result);
		return result;
	}

	/**
	 * @return a copy of the path
	 */
	public Map<FactoryContainer, Attributes> getAllContainers() {
		Map<FactoryContainer, Attributes> map = new LinkedHashMap<>(_path.size());
		synchronized (_path) {
			for (Map.Entry<FactoryContainer, Attributes> entry : getReversed(_path.entrySet())) {
				map.put(entry.getKey(), new Attributes(entry.getValue()));
			}
		}
		return map;
	}

	/**
	 * A word of warning: this equals() method does not canonicalize factory
	 * paths before comparing factory path entries. It's possible that two
	 * factory paths actually refer to the same jar files and this method would
	 * return that the paths are not equal.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof FactoryPath)) {
			return false;
		}
		FactoryPath other = (FactoryPath) o;
		return _path.equals(other._path);
	}
}
