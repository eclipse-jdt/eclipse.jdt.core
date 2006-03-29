/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;

/**
 *
 */
public class UserLibraryClasspathContainer implements IClasspathContainer {
	
	private String name;
	
	public UserLibraryClasspathContainer(String libName) {
		this.name= libName;
	}
	
	private UserLibrary getUserLibrary() {
		return UserLibraryManager.getUserLibrary(this.name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IClasspathContainer#getClasspathEntries()
	 */
	public IClasspathEntry[] getClasspathEntries() {
		UserLibrary library= getUserLibrary();
		if (library != null) {
			return library.getEntries();
		}
		return new IClasspathEntry[0];
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IClasspathContainer#getDescription()
	 */
	public String getDescription() {
		return this.name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IClasspathContainer#getKind()
	 */
	public int getKind() {
		UserLibrary library= getUserLibrary();
		if (library != null && library.isSystemLibrary()) {
			return K_SYSTEM;
		}
		return K_APPLICATION;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IClasspathContainer#getPath()
	 */
	public IPath getPath() {
		return new Path(JavaCore.USER_LIBRARY_CONTAINER_ID).append(this.name);
	}
}
