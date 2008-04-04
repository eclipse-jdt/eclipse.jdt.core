/*******************************************************************************
 * Copyright (c) 2005, 2008 BEA Systems, Inc.
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

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;

/**
 * Annotation processor factory container based on a jar file
 * outside of the workspace, referenced by a classpath variable.
 */
public class VarJarFactoryContainer extends JarFactoryContainer {
	
	private final String _id;
	private final File _jarFile;

	/**
	 * @param jarPath
	 */
	public VarJarFactoryContainer(IPath jarPath) {
		_id = jarPath.toString();
		IPath resolved = JavaCore.getResolvedVariablePath(jarPath);
		if (null != resolved) {
			_jarFile = resolved.toFile();
		}
		else {
			_jarFile = null;
		}
	}

	@Override
	public FactoryType getType() {
		return FactoryType.VARJAR;
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
