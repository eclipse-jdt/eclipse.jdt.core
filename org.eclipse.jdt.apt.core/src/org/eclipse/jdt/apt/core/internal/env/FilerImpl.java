/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.env;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedSourceFolderManager;
import org.eclipse.jdt.apt.core.internal.util.FileSystemUtil;
import org.eclipse.jdt.core.JavaModelException;

import com.sun.mirror.apt.Filer;


public abstract class FilerImpl implements Filer {

    abstract protected AbstractCompilationEnv getEnv();
	
    /**
     * Creates a new source file and returns a writer for it. The file's name 
     * and path (relative to the root of all newly created source files) is 
     * based on the type to be declared in that file. If more than one type is 
     * being declared, the name of the principal top-level type (the public 
     * one, for example) should be used. 
     * 
     * Character set used is the default character set for the platform
     * 
     * @param typeName - canonical (fully qualified) name of the principal type being declared in this file 
     */
    public PrintWriter createSourceFile(String typeName) throws IOException 
    {
    	if (typeName == null)
    		throw new IllegalArgumentException("Type name cannot be null"); //$NON-NLS-1$
    	if ("".equals(typeName)) //$NON-NLS-1$
    		throw new IllegalArgumentException("Type name cannot be empty"); //$NON-NLS-1$
    	
    	getEnv().checkValid();
    	
    	PrintWriter pw;
        try {
			pw = new JavaSourceFilePrintWriter( typeName, new StringWriter(), getEnv() );
		} catch (CoreException e) {
			IOException ioe = new IOException();
			ioe.initCause(e);
			throw ioe;
		} 
		return pw;
    }


    /**
     * Return a project-relative path
     */
    protected IPath getOutputFileForLocation( Filer.Location loc, String pkg, File relPath )
    	throws IOException
    {
    	GeneratedSourceFolderManager gsfm = getEnv().getAptProject().getGeneratedSourceFolderManager();
    	IPath path = null;
    	if ( loc == Filer.Location.CLASS_TREE )
    	{
    		try 
    		{
    			path = gsfm.getBinaryOutputLocation();
    		}
    		catch ( JavaModelException e )
    		{
    			AptPlugin.log(e, "Failure getting the output file"); //$NON-NLS-1$
    			throw new IOException();
    		}
    	}
    	else if ( loc == Filer.Location.SOURCE_TREE ) {
    		path = gsfm.getFolder().getProjectRelativePath();
    	}
    	
        if( pkg != null )
            path = path.append(pkg.replace('.', File.separatorChar) );

        path = path.append(relPath.getPath() );
    	
        // Create the parent folder (need an absolute path temporarily)
        IPath absolutePath = getEnv().getProject().getLocation().append(path);
        File parentFile = absolutePath.toFile().getParentFile();
        FileSystemUtil.mkdirs( parentFile );
        
    	return path;
    }
    
    
}
