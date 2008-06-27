/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class TestFileStore extends FileStore {

	URI uri;

	public TestFileStore(URI uri) {
		this.uri = uri;
	}

	public String[] childNames(int options, IProgressMonitor monitor)
			throws CoreException {
		return null;
	}

	public IFileInfo fetchInfo(int options, IProgressMonitor monitor)
			throws CoreException {
		return new FileInfo();
	}

	public IFileStore getChild(String name) {
		if (name.equals(".project"))
			return new TestFileStore(this.uri);
		return new TestFileStore(null);
	}

	public String getName() {
		return null;
	}

	public IFileStore getParent() {
		return new TestFileStore(this.uri);
	}

	public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException {
		return new TestFileStore(this.uri);
	}

	public InputStream openInputStream(int options, IProgressMonitor monitor)
			throws CoreException {
		return null;
	}

	public OutputStream openOutputStream(int options, IProgressMonitor monitor)
			throws CoreException {
		return new OutputStream() {
			public void write(int b) throws IOException {
			}
		};
	}

	public URI toURI() {
		return this.uri;
	}

}
