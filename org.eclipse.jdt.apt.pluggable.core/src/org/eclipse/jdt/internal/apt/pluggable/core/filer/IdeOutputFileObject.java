/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.apt.pluggable.core.filer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.Collection;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.internal.apt.pluggable.core.dispatch.IdeProcessingEnvImpl;

/**
 * Implementation of FileObject for generating resource files in the IDE.  
 * This is used for files that are neither class files nor Java source files.
 * TODO: at present this represents a write-only (generated) file.  When we
 * implement the openResource() calls in Filer, we may want to move some of
 * these methods into a base class.
 * @see IdeOutputJavaFileObject
 */
public class IdeOutputFileObject implements FileObject
{
	private final IdeProcessingEnvImpl _env;
	private final IFile _file;
	private final Collection<IFile> _parentFiles;


	/**
	 * Create a new IdeOutputFileObject for writing.  The file will not actually be written until the Writer or OutputStream is closed.
	 * @param env among other roles, the ProcessingEnvironment tracks what files have been generated in a given build.
	 * @param location must be an output location (see {@link Location#isOutputLocation()}).
	 * @param pkg
	 * @param relativeName
	 * @param parentFiles
	 * @see javax.tools.StandardLocation
	 */
	public IdeOutputFileObject(IdeProcessingEnvImpl env, IFile file, Set<IFile> parentFiles) {
		_env = env;
		_file = file;
		_parentFiles = parentFiles;
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#delete()
	 */
	@Override
	public boolean delete()
	{
		throw new UnsupportedOperationException("Deleting a generated file is not permitted");
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#getCharContent(boolean)
	 */
	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException
	{
		throw new UnsupportedOperationException("Reading a generated file is not permitted");
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#getLastModified()
	 */
	@Override
	public long getLastModified()
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#getName()
	 */
	@Override
	public String getName()
	{
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#openInputStream()
	 */
	@Override
	public InputStream openInputStream() throws IOException
	{
		throw new UnsupportedOperationException("Reading a generated file is not permitted");
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#openOutputStream()
	 */
	@Override
	public OutputStream openOutputStream() throws IOException
	{
		return new IdeNonSourceOutputStream(_env, _file, _parentFiles);
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#openReader(boolean)
	 */
	@Override
	public Reader openReader(boolean ignoreEncodingErrors) throws IOException
	{
		throw new UnsupportedOperationException("Reading a generated file is not permitted");
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#openWriter()
	 */
	@Override
	public Writer openWriter() throws IOException
	{
		return new PrintWriter(openOutputStream());
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#toUri()
	 */
	@Override
	public URI toUri()
	{
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
