/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.apt.pluggable.core.filer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.internal.apt.pluggable.core.dispatch.IdeProcessingEnvImpl;

/**
 * Implementation of FileObject for generating resource files in the IDE.
 * This is used for files that are neither class files nor Java source files.
 * @see IdeOutputJavaFileObject
 */
public class IdeOutputClassFileObject extends IdeOutputFileObject implements JavaFileObject {

	private final IdeProcessingEnvImpl _env;
	private final String _name;
	private final IFile _file;

	public IdeOutputClassFileObject(IdeProcessingEnvImpl env, IFile file, String name) {
		_env = env;
		_name = name;
		_file = file;
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#getName()
	 */
	@Override
	public String getName()
	{
		return _name;
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#openOutputStream()
	 */
	@Override
	public OutputStream openOutputStream() throws IOException
	{
		return new IdeClassOutputStream(_env,_file);
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
	public URI toUri() {
		return _file.getLocationURI();
	}

	@Override
	public Kind getKind() {
		return Kind.CLASS;
	}

	@Override
	public boolean isNameCompatible(String simpleName, Kind kind) {
		//TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public NestingKind getNestingKind() {
		//TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Modifier getAccessLevel() {
		//TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
