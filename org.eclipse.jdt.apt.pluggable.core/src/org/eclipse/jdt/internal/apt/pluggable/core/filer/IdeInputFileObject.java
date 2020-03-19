/*******************************************************************************
 * Copyright (c) 2008 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.internal.apt.pluggable.core.filer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;

import javax.tools.FileObject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Implementation of a FileObject returned by Filer.getResource().
 * @since 3.4
 */
public class IdeInputFileObject implements FileObject {

	private final IFile _file;

	public IdeInputFileObject(IFile file) {
		_file = file;
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#delete()
	 */
	@Override
	public boolean delete() {
		throw new IllegalStateException("An annotation processor is not permitted to delete resources");
	}

	/**
	 * @see javax.tools.FileObject#getCharContent(boolean)
	 */
	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors)
			throws IOException {
		try {
			char[] chars = Util.getResourceContentsAsCharArray(this._file);
			return new String(chars);
		} catch (CoreException e) {
			throw new IOException(e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#getLastModified()
	 */
	@Override
	public long getLastModified() {
		return _file.getModificationStamp();
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#getName()
	 */
	@Override
	public String getName() {
		return _file.getProjectRelativePath().toString();
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#openInputStream()
	 */
	@Override
	public InputStream openInputStream() throws IOException {
		try {
			return _file.getContents();
		} catch (CoreException e) {
			throw new IOException(e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#openOutputStream()
	 */
	@Override
	public OutputStream openOutputStream() throws IOException {
		throw new IllegalStateException("Writing to a non-generated file is not permitted");
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#openReader(boolean)
	 */
	@Override
	public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
		return new BufferedReader(new InputStreamReader(openInputStream()));
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#openWriter()
	 */
	@Override
	public Writer openWriter() throws IOException {
		throw new IllegalStateException("Writing to a non-generated file is not permitted");
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#toUri()
	 */
	@Override
	public URI toUri() {
		return _file.getLocationURI();
	}

}
