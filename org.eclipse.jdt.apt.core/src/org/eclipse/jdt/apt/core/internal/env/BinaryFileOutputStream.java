/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *    
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.env;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.apt.core.internal.util.FileSystemUtil;

/**
 * Wrap output operations, caching them in memory,
 * then writing them out at the end if the content
 * is different than what is on disk
 */
public class BinaryFileOutputStream extends ByteArrayOutputStream {

	private final IFile _file;
	
	public BinaryFileOutputStream(IFile file) {
		_file = file;
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		InputStream contents = new ByteArrayInputStream(toByteArray());
		if (!_file.exists()) {
			saveToDisk(contents, true);
			return;
		}
		boolean needToWriteData = true;
		try {
			// Only write the contents if the data is different
			InputStream in = new ByteArrayInputStream(toByteArray());
			InputStream oldData = new BufferedInputStream(_file.getContents());
			if (FileSystemUtil.compareStreams(in, oldData)) {
				needToWriteData = false;
			}
		}
		catch (CoreException ce) {
			// Ignore -- couldn't read the old data, so assume it's different
		}
		if (needToWriteData) {
			contents.reset();
			saveToDisk(contents, false);
		}
	}
	
	private void saveToDisk(InputStream toSave, boolean create) throws IOException{
		try {
			FileSystemUtil.makeDerivedParentFolders(_file.getParent());
			if (create) {
				_file.create(toSave, true, null);
			}
			else {
				_file.setContents(toSave, true, false, null);
			}
		}
		catch (CoreException ce) {
			AptPlugin.log(ce, "Could not create generated file"); //$NON-NLS-1$
			throw new IOException(ce.getMessage());
		}
	}

}
