/*******************************************************************************
 * Copyright (c) 2005, 2008 BEA Systems, Inc.
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

package org.eclipse.jdt.apt.core.internal;

import java.io.File;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

/**
 * Annotation processor factory container based on a jar file
 * within the workspace.
 */
public class WkspJarFactoryContainer extends JarFactoryContainer {

	private final String _id;
	private final File _jarFile; // A java.io.File, not guaranteed to exist.

	/**
	 * Construct a workspace-jar container from an IPath representing
	 * the jar file's location in the workspace.  We will construct
	 * the container even if the file does not exist.
	 * @param jar an IPath representing a jar file in the workspace;
	 * the path is relative to the workspace root.
	 */
	public WkspJarFactoryContainer(IPath jar) {
		_id = jar.toString();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource res = root.findMember(_id);
		if (null == res) {
			// The file evidently doesn't exist on disk.  Do our best to
			// construct a java.io.File for it anyway.
			_jarFile = root.getLocation().append(jar).toFile();

		}
		else if (res.getType() == IResource.FILE) {
			_jarFile = res.getLocation().toFile();
		}
		else {
			_jarFile = null;
		}
	}

	@Override
	public FactoryType getType() {
		return FactoryType.WKSPJAR;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.apt.core.internal.JarFactoryContainer#getJarFile()
	 */
	@Override
	public File getJarFile() {
		return _jarFile;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.apt.core.FactoryContainer#getId()
	 */
	@Override
	public String getId() {
		return _id;
	}
}
