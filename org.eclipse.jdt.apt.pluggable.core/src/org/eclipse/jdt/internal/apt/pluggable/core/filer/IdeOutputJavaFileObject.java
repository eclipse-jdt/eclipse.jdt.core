/*******************************************************************************
 * Copyright (c) 2007, 2018 BEA Systems, Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.internal.apt.pluggable.core.filer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Collection;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.internal.apt.pluggable.core.dispatch.IdeProcessingEnvImpl;

/**
 * Implementation of JavaFileObject used for Java 6 annotation processing within the IDE.
 * This object is used only for writing source and class files.
 *
 * @since 3.3
 */
public class IdeOutputJavaFileObject extends IdeOutputFileObject implements JavaFileObject {

	private final IdeProcessingEnvImpl _env;
	private final CharSequence _name;
	private final Collection<IFile> _parentFiles;

	public IdeOutputJavaFileObject(IdeProcessingEnvImpl env, CharSequence name, Collection<IFile> parentFiles) {
		_env = env;
		_parentFiles = parentFiles;
		_name = name;
	}

	@Override
	public Modifier getAccessLevel() {
		//TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see javax.tools.JavaFileObject#getKind()
	 */
	@Override
	public Kind getKind() {
		//TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#getName()
	 */
	@Override
	public String getName() {
		return _name.toString();
	}

	@Override
	public NestingKind getNestingKind() {
		//TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public boolean isNameCompatible(String simpleName, Kind kind) {
		//TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#openOutputStream()
	 */
	@Override
	public OutputStream openOutputStream() throws IOException {
		return new IdeJavaSourceOutputStream(_env, _name, _parentFiles);
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#openWriter()
	 */
	@Override
	public Writer openWriter() throws IOException {
		return new PrintWriter(openOutputStream());
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#toUri()
	 * The file does not exist until its writer is closed, so the URI we are
	 * constructing here does not point to a real resource.
	 */
	@Override
	public URI toUri() {
		IFile file = _env.getAptProject().getGeneratedFileManager(_env.isTestCode()).getIFileForTypeName(_name.toString());
		return file.getLocationURI();
	}

}
