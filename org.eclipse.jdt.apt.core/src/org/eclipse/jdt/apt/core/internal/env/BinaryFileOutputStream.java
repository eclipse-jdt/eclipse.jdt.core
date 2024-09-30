/*******************************************************************************
 * Copyright (c) 2005, 2018 BEA Systems, Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.env;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.apt.core.internal.util.FileSystemUtil;

/**
 * Wrap output operations, caching them in memory,
 * then writing them out at the end if the content
 * is different than what is on disk
 */
public class BinaryFileOutputStream extends ByteArrayOutputStream {

	protected final IFile _file;
	private final BuildEnv _env;

	public BinaryFileOutputStream(IFile file, BuildEnv env) {
		_file = file;
		_env = env;
	}

	@Override
	public void close() throws IOException {
		super.close();

		byte[] newContent = toByteArray();
		boolean contentsChanged = true;
		try {
			// Only write the contents if the data is different
			byte[] oldContent = _file.readAllBytes();
			if (Arrays.equals(newContent, oldContent)) {
				contentsChanged = false;
			}
		} catch (CoreException ce) {
			// Ignore -- couldn't read the old data, so assume it's different
			contentsChanged = true;
		}
		if (contentsChanged) {
			FileSystemUtil.saveToDisk(_file, newContent);
		}

		IFile parentFile = _env.getFile();
		if (parentFile != null) {
			_env.getAptProject().getGeneratedFileManager(_env.isTestCode()).addGeneratedFileDependency(Collections.singleton(parentFile), _file);
			_env.addGeneratedNonSourceFile(_file);
		}
	}
}
