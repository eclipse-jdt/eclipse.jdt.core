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

package org.eclipse.jdt.apt.core.internal;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

/**
 * Annotation processor factory container based on a jar file 
 * within the workspace.
 */
public class WkspJarFactoryContainer extends JarFactoryContainer {

	private String _id;
	private File _jarFile;

	/**
	 * Construct a workspace-jar container from an IPath representing
	 * the jar file's location in the workspace.  We treat the jar
	 * file as a physical file rather than as an IResource, which
	 * means that we don't cooperate with the Eclipse framework: for
	 * instance, we don't get notified if the jar changes.
	 * @param jar an IPath representing a jar file in the workspace;
	 * the path is relative to the workspace root.
	 */
	public WkspJarFactoryContainer(IPath jar) {
		_id = jar.toString();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource res = root.findMember(_id);
		_jarFile = null;
		if (null != res && res instanceof IFile) {
			_jarFile = res.getLocation().toFile();
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
