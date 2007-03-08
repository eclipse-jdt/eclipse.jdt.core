/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.internal.core.util.Util;

public class JarEntryDirectory extends PlatformObject implements IJarEntryResource {
	private Object parent;
	private IPath path;
	private IJarEntryResource[] children;
	
	public JarEntryDirectory(IPath parentRelativePath) {
		this.path = parentRelativePath;
	}
	
	public JarEntryDirectory clone(Object newParent) {
		JarEntryDirectory dir = new JarEntryDirectory(this.path);
		dir.setParent(newParent);
		int length = this.children.length;
		if (length > 0) {
			IJarEntryResource[] newChildren = new IJarEntryResource[length];
			for (int i = 0; i < length; i++) {
				IJarEntryResource child = this.children[i];
				if (child instanceof JarEntryFile)
					newChildren[i] = ((JarEntryFile) child).clone(dir);
				else
					newChildren[i] = ((JarEntryDirectory) child).clone(dir);
			}
			dir.setChildren(newChildren);
		}
		return dir;
	}
	
	public boolean equals(Object obj) {
		if (! (obj instanceof JarEntryDirectory))
			return false;
		JarEntryDirectory other = (JarEntryDirectory) obj;
		return this.parent.equals(other.parent) && this.path.equals(other.path);
	}

	public IJarEntryResource[] getChildren() {
		return this.children;
	}

	public InputStream getContents() throws CoreException {
		return new ByteArrayInputStream(new byte[0]);
	}

	public IPath getFullPath() {
		return this.path;
	}

	public String getName() {
		return this.path.lastSegment();
	}

	public Object getParent() {
		return this.parent;
	}
	
	public int hashCode() {
		return Util.combineHashCodes(this.path.hashCode(), this.parent.hashCode());
	}
	
	public boolean isFile() {
		return false;
	}

	public boolean isReadOnly() {
		return true;
	}

	public void setChildren(IJarEntryResource[] children) {
		this.children = children;
	}

	public void setParent(Object parent) {
		this.parent = parent;
	}
	
	public String toString() {
		return "JarEntryDirectory["+this.path+"]"; //$NON-NLS-1$ //$NON-NLS-2$ 
	}
}
