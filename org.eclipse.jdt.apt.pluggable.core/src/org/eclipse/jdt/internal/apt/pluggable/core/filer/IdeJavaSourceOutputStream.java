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
 *
 *******************************************************************************/

package org.eclipse.jdt.internal.apt.pluggable.core.filer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.apt.core.env.Phase;
import org.eclipse.jdt.apt.core.internal.generatedfile.FileGenerationResult;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedFileManager;
import org.eclipse.jdt.internal.apt.pluggable.core.Apt6Plugin;
import org.eclipse.jdt.internal.apt.pluggable.core.dispatch.IdeProcessingEnvImpl;

/**
 * OutputStream used by the IdeFilerImpl to generate Java source files.
 * @since 3.3
 */
public class IdeJavaSourceOutputStream extends ByteArrayOutputStream {

	private final IdeProcessingEnvImpl _env;
	private final CharSequence _name;
	private final Collection<IFile> _parentFiles;
	private boolean _closed = false;

	public IdeJavaSourceOutputStream(IdeProcessingEnvImpl env, CharSequence name,
			Collection<IFile> parentFiles)
	{
		_env = env;
		_name = name;
		_parentFiles = parentFiles;
	}

	/* (non-Javadoc)
	 * @see java.io.ByteArrayOutputStream#close()
	 */
	@Override
	public void close() throws IOException {
		synchronized(this) {
			if (_closed) {
				return;
			}
			_closed = true;
		}
		try {
			GeneratedFileManager gfm = _env.getAptProject().getGeneratedFileManager(_env.isTestCode());
			Phase phase = _env.getPhase();

			FileGenerationResult result = null;
			if ( phase == Phase.RECONCILE )
			{
				//TODO - implement reconcile
			}
			else if ( phase == Phase.BUILD)	{
				String charset = _env.getJavaProject().getProject().getDefaultCharset();
				result = gfm.generateFileDuringBuild(
						_parentFiles,  _name.toString(), charset == null ? this.toString() : this.toString(charset),
						_env.currentProcessorSupportsRTTG(), null /* progress monitor */ );
			}
			if (result != null) {
				_env.addNewUnit(result);
			}
		}
		catch (CoreException ce) {
			Apt6Plugin.log(ce, "Unable to generate type when IdeJavaSourceOutputStream was closed"); //$NON-NLS-1$
		}
	}

}
