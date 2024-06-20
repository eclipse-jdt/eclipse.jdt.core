/*******************************************************************************
 * Copyright (c) 2007, 2018 BEA Systems, Inc.
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
 *******************************************************************************/

package org.eclipse.jdt.internal.apt.pluggable.core.filer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.apt.core.internal.env.BinaryFileOutputStream;
import org.eclipse.jdt.apt.core.internal.util.FileSystemUtil;
import org.eclipse.jdt.internal.apt.pluggable.core.Apt6Plugin;
import org.eclipse.jdt.internal.apt.pluggable.core.dispatch.IdeProcessingEnvImpl;

/**
 * @see BinaryFileOutputStream
 */
public class IdeNonSourceOutputStream  extends ByteArrayOutputStream
{
	private final IdeProcessingEnvImpl _env;
	private final IFile _file;
	private final Collection<IFile> _parentFiles;

	public IdeNonSourceOutputStream(IdeProcessingEnvImpl env, IFile file, Collection<IFile> parentFiles) {
		_env = env;
		_file = file;
		_parentFiles = parentFiles;
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
			saveToDisk(newContent);
		}

		// If there are no parents, we don't need to track dependencies
		if (_parentFiles != null && !_parentFiles.isEmpty()) {
			_env.getAptProject().getGeneratedFileManager(_env.isTestCode()).addGeneratedFileDependency(_parentFiles, _file);
			_env.addNewResource(_file);
		}
	}

	private void saveToDisk(byte[] toSave) throws IOException{
		try {
			FileSystemUtil.makeDerivedParentFolders(_file.getParent());
			_file.write(toSave, true, true, false, null);
		}
		catch (CoreException ce) {
			if (_file.exists()) {
				// Do nothing. This is a case-insensitive file system mismatch,
				// and the underlying platform has saved the contents already.
			}
			else {
				Apt6Plugin.log(ce, "Could not create generated non-Java file " + _file.getName()); //$NON-NLS-1$
				throw new IOException(ce);
			}
		}
	}

}
