/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.env;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedFileManager;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;


public class JavaSourceFilePrintWriter extends PrintWriter {

    public JavaSourceFilePrintWriter( String typeName, StringWriter sw, ProcessorEnvImpl env, String charsetName )
    {
        super( sw );
        _sw = sw;
        _typeName = typeName;
        _env = env;
        _charsetName = charsetName;
    }
	
    public void close()
    {
        try
        {
            String contents = _sw.toString();
            super.close();
            GeneratedFileManager gfm = GeneratedFileManager.getGeneratedFileManager();
            ProcessorEnvImpl.Phase phase = _env.getPhase();
		
            if ( phase == ProcessorEnvImpl.Phase.RECONCILE )
            {
            	ICompilationUnit parentCompilationUnit = _env.getCompilationUnit();
                ICompilationUnit cu  = gfm.generateFileDuringReconcile( 
                    parentCompilationUnit, _typeName, contents, DefaultWorkingCopyOwner.PRIMARY, null, null );
				_env.addGeneratedFile( (IFile)cu.getResource() );
            }
            else if ( phase == ProcessorEnvImpl.Phase.BUILD)	
            {
				IFile f = gfm.generateFileDuringBuild( _env.getFile(), _env.getProject(), _typeName, contents, null /* progress monitor */, _charsetName );
				_env.addGeneratedFile( f );
            }
            else
            {
                assert false : "Unexpected phase value: " + phase ;
            }
        }
        catch ( JavaModelException jme )
        {
            // TODO:  handle this exception in a nicer way.
            jme.printStackTrace();
            throw new RuntimeException( jme );
        }
        catch ( CoreException ce )
        {
            // TODO:  handle this exception
            ce.printStackTrace();
            throw new RuntimeException( ce );
        }
        catch( UnsupportedEncodingException use )
        {
        	// TODO: handle this exception
        	throw new RuntimeException( use );
        }
    }
			
	
    private StringWriter _sw;
    private String _typeName;
    private ProcessorEnvImpl _env;
    private String _charsetName;
	
}
