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

package org.eclipse.jdt.apt.core.internal;

import java.io.File;

/**
 * Annotation processor factory container based on a jar file
 * outside the workspace, referenced by absolute path.
 */
public class ExtJarFactoryContainer extends JarFactoryContainer {
	private String _id;
	private File _jarFile;

	/**
	 * @param jar must not be null
	 */
	public ExtJarFactoryContainer(File jar) {
		_jarFile = jar.getAbsoluteFile();
		_id = _jarFile.getPath(); // id of ExtJar is the absolute path
	}

	@Override
	public FactoryType getType() {
		return FactoryType.EXTJAR;
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
