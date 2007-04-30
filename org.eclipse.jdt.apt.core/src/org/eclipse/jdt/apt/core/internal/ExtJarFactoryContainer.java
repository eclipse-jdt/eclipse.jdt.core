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
