/*******************************************************************************
 * Copyright (c) 2007, 2008 BEA Systems, Inc. and others
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