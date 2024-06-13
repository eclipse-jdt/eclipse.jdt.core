/*******************************************************************************
 * Copyright (c) 2006, 2007 BEA Systems, Inc.
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

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

/**
 * Sets the encoding for the IFile on close of the stream
 */
public class EncodedFileOutputStream extends BinaryFileOutputStream {

	private final String _charsetName;

	public EncodedFileOutputStream(IFile file, BuildEnv env, String charsetName) {
		super(file, env);
		_charsetName = charsetName;
	}

	@Override
	public void close() throws IOException {
		super.close();
		if (_charsetName != null) {

			// Need to check for source control on the resources encoding file
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=190268
			IWorkspace ws = ResourcesPlugin.getWorkspace();

			// Yuck -- we need to hardcode the location of the prefs file for file encoding
			IFile resourceFile = _file.getProject().getFile(".settings/org.eclipse.core.resources.prefs"); //$NON-NLS-1$
			IStatus result = ws.validateEdit(new IFile[]{resourceFile}, IWorkspace.VALIDATE_PROMPT);
			if (result.getSeverity() == IStatus.CANCEL) {
				// User cancelled the checkout. Don't try to edit the encoding for the file
				return;
			}
			try {
				String defaultCharset = _file.getCharset();
				if (!_charsetName.equalsIgnoreCase(defaultCharset)) {
					_file.setCharset(_charsetName, null);
				}
			}
			catch (CoreException ce) {
				throw new IOException("Could not set charset: " + _charsetName, ce); //$NON-NLS-1$
			}
		}
	}
}
