/*******************************************************************************
 * Copyright (c) 2006 BEA Systems, Inc. 
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

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * Sets the encoding for the IFile on close of the stream
 */
public class EncodedFileOutputStream extends BinaryFileOutputStream {

	private final String _charsetName;
	
	public EncodedFileOutputStream(IFile file, ProcessorEnvImpl env, String charsetName) {
		super(file, env);
		_charsetName = charsetName;
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		if (_charsetName != null) {
			try {
				_file.setCharset(_charsetName, null);
			}
			catch (CoreException ce) {
				IOException ioe = new IOException("Could not set charset: " + _charsetName); //$NON-NLS-1$
				ioe.initCause(ce);
				throw ioe;
			}
		}
	}
}
