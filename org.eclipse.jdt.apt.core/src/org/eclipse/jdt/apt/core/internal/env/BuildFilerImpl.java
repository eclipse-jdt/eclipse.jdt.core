/*******************************************************************************
 * Copyright (c) 2006, 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.env;

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

import com.sun.mirror.apt.Filer;

/**
 * @author wharley
 *
 */
public class BuildFilerImpl extends FilerImpl {

	private boolean _generatedClassFiles = false;
	private final BuildEnv _env;

	public BuildFilerImpl(BuildEnv env) {
		_env = env;
	}

    /**  
     * Creates a new class file, and returns a stream for writing to it. The 
     * file's name and path (relative to the root of all newly created class 
     * files) is based on the name of the type being written. 
     *  
     * @param typeName - canonical (fully qualified) name of the type being written 
     * @return -a stream for writing to the new file 
     */
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
			IOException ioe = new IOException();
			ioe.initCause(e);
			throw ioe;
		}
		_generatedClassFiles = true;
		
		// We do not want to write to disk during reconcile
		if (_env.getPhase() == Phase.RECONCILE) {
			return new NoOpOutputStream();
		}
		
    	GeneratedSourceFolderManager gsfm = _env.getAptProject().getGeneratedSourceFolderManager();
    	IPath path;
    	try 
    	{
    		 path = gsfm.getBinaryOutputLocation();
    	}
    	catch ( Exception e )
    	{
    		// TODO - stop throwing this exception
    		AptPlugin.log(e, "Failure getting the output file"); //$NON-NLS-1$
    		throw new IOException();
    	}
    	
    	path = path.append(typeName.replace('.', File.separatorChar) + ".class"); //$NON-NLS-1$
    	
        IFile file = getEnv().getProject().getFile(path);
        return new BinaryFileOutputStream(file, _env);
    }
	
	public boolean hasGeneratedClassFile(){ return _generatedClassFiles; }

    /**
     * Creates a new text file, and returns a writer for it. The file is 
     * located along with either the newly created source or newly created 
     * binary files. It may be named relative to some package (as are source 
     * and binary files), and from there by an arbitrary pathname. In a loose 
     * sense, the pathname of the new file will be the concatenation of loc, 
     * pkg, and relPath. 
     * 
     * A charset for encoding the file may be provided. If none is given, 
     * the charset used to encode source files (see createSourceFile(String)) will be used. 
     *
     * @param loc - location of the new file
     * @param pkg - package relative to which the file should be named, or the empty string if none
     * @param relPath - final pathname components of the file
     * @param charsetName - the name of the charset to use, or null if none is being explicitly specified 
     * @return - a writer for the new file 
     */
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

	/**
     * Creates a new binary file, and returns a stream for writing to it. The 
     * file is located along with either the newly created source or newly 
     * created binary files. It may be named relative to some package (as 
     * are source and binary files), and from there by an arbitrary pathname. 
     * In a loose sense, the pathname of the new file will be the concatenation 
     * of loc, pkg, and relPath. 
     * 
     * @param loc - location of the new file
     * @param pkg - package relative to which the file should be named, or the empty string if none
     * @param relPath - final pathname components of the file 
     * @return a stream for writing to the new file 
     */
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
        	IOException ioe = new IOException("Invalid file name"); //$NON-NLS-1$
        	ioe.initCause(ce);
        	throw ioe;
        }
	}

}
