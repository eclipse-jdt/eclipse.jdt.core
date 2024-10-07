/*******************************************************************************
 * Copyright (c) 2006, 2018 BEA Systems, Inc. and others
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
package org.eclipse.jdt.apt.core.internal.env;

import com.sun.mirror.apt.Filer;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.apt.core.env.Phase;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedSourceFolderManager;

/**
 * @author wharley
 */
public class BuildFilerImpl extends FilerImpl {

	private boolean _generatedClassFiles = false;
	private final BuildEnv _env;

	public BuildFilerImpl(BuildEnv env) {
		_env = env;
	}

    @Override
	public OutputStream createClassFile(String typeName) throws IOException
    {
    	if (typeName == null)
    		throw new IllegalArgumentException("Type name cannot be null"); //$NON-NLS-1$
    	if ("".equals(typeName)) //$NON-NLS-1$
    		throw new IllegalArgumentException("Type name cannot be empty"); //$NON-NLS-1$

    	_env.checkValid();
    	try {
			_env.validateTypeName(typeName);
		} catch (CoreException e) {
			throw new IOException(e);
		}
		_generatedClassFiles = true;

		// We do not want to write to disk during reconcile
		if (_env.getPhase() == Phase.RECONCILE) {
			return new NoOpOutputStream();
		}

    	GeneratedSourceFolderManager gsfm = _env.getAptProject().getGeneratedSourceFolderManager(_env.isTestCode());
    	IPath path;
    	try
    	{
    		 path = gsfm.getBinaryOutputLocation();
    	}
    	catch ( Exception e )
    	{
    		// TODO - stop throwing this exception
    		AptPlugin.log(e, "Failure getting the output file"); //$NON-NLS-1$
    		throw new IOException(e);
    	}

    	path = path.append(typeName.replace('.', File.separatorChar) + ".class"); //$NON-NLS-1$

        IFile file = getEnv().getProject().getFile(path);
        return new BinaryFileOutputStream(file, _env);
    }

	public boolean hasGeneratedClassFile(){ return _generatedClassFiles; }

    @Override
	public PrintWriter createTextFile(Filer.Location loc, String pkg, File relPath, String charsetName)
        throws IOException
    {
    	if (relPath == null)
    		throw new IllegalArgumentException("Path cannot be null"); //$NON-NLS-1$
    	if ("".equals(relPath.getPath())) //$NON-NLS-1$
    		throw new IllegalArgumentException("Path cannot be empty"); //$NON-NLS-1$

    	_env.checkValid();

    	// If we're reconciling, we do not want to actually create the text file
    	if (_env.getPhase() == Phase.RECONCILE) {
    		return new NoOpPrintWriter();
    	}


    	IPath path = getOutputFileForLocation( loc, pkg, relPath );
    	IFile file = _env.getProject().getFile(path);
    	validateFile(file);
    	OutputStream binaryOut = new EncodedFileOutputStream(file, _env, charsetName);

    	if (charsetName == null) {
    		return new PrintWriter(binaryOut);
    	}
    	else {
    		OutputStreamWriter outWriter = new OutputStreamWriter(binaryOut, charsetName);
    		return new PrintWriter(outWriter);
    	}
    }

    @Override
	public OutputStream createBinaryFile(Filer.Location loc, String pkg, File relPath)
        throws IOException
    {
    	if (relPath == null)
    		throw new IllegalArgumentException("Path cannot be null"); //$NON-NLS-1$
    	if ("".equals(relPath.getPath())) //$NON-NLS-1$
    		throw new IllegalArgumentException("Path cannot be empty"); //$NON-NLS-1$

    	_env.checkValid();

    	// We do not want to write to disk during reconcile
		if (_env.getPhase() == Phase.RECONCILE) {
			return new NoOpOutputStream();
		}

    	IPath path = getOutputFileForLocation( loc, pkg, relPath );
    	IFile file = _env.getProject().getFile(path);
    	validateFile(file);
    	return new BinaryFileOutputStream(file, _env);
    }

	@Override
	protected AbstractCompilationEnv getEnv() {
		return _env;
	}

    private void validateFile(IFile file) throws IOException
	{
    	IStatus status = file.getWorkspace().validatePath(file.getFullPath().toOSString(), IResource.FILE);
    	if (!status.isOK()) {
        	CoreException ce = new CoreException(status);
        	throw new IOException("Invalid file name", ce); //$NON-NLS-1$
        }
	}

}
