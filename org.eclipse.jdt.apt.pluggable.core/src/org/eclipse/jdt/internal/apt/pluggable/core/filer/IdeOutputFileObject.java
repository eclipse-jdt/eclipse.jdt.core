/*******************************************************************************
 * Copyright (c) 2007, 2008 BEA Systems, Inc. and others 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.internal.apt.pluggable.core.filer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.tools.FileObject;

public abstract class IdeOutputFileObject implements FileObject {

	@Override
	public boolean delete() {
		throw new IllegalStateException("Deleting a file is not permitted from within an annotation processor");
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
		throw new IllegalStateException("Generated files are write-only");
	}

	@Override
	public long getLastModified() {
		//TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public InputStream openInputStream() throws IOException {
		throw new IllegalStateException("Opening an input stream on a generated file is not permitted");
	}

	@Override
	public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
		throw new IllegalStateException("Opening a reader on a generated file is not permitted");
	}

}