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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.apt.core.env.Phase;
import org.eclipse.jdt.apt.core.internal.generatedfile.FileGenerationResult;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedFileManager;
import org.eclipse.jdt.core.ICompilationUnit;


public class JavaSourceFilePrintWriter extends PrintWriter {

    public JavaSourceFilePrintWriter( String typeName, StringWriter sw, ProcessorEnvImpl env )
    {
        super( sw );
        _sw = sw;
        _typeName = typeName;
        _env = env;
    }
	
    public void close()
    {
    	String contents = _sw.toString();
        super.close();
        GeneratedFileManager gfm = GeneratedFileManager.getGeneratedFileManager(_env.getProject());
        Phase phase = _env.getPhase();
	
        if ( phase == Phase.RECONCILE )
        {
        	ICompilationUnit parentCompilationUnit = _env.getCompilationUnit();
            FileGenerationResult result  = gfm.generateFileDuringReconcile( 
                parentCompilationUnit, _typeName, contents, parentCompilationUnit.getOwner(), null, null );
			if ( result != null )
				_env.addGeneratedFile(result.getFile(), result.isModified());
        }
        else if ( phase == Phase.BUILD)	
        {
        	try {
				FileGenerationResult result = gfm.generateFileDuringBuild( _env.getFile(),  _typeName, contents, null /* progress monitor */ );
				_env.addGeneratedFile( result.getFile(), result.isModified());
				
				// don't set to false, we don't want to overwrite a previous iteration setting it to true
				if ( result.getSourcePathChanged() )
					_env.setSourcePathChanged( true );
        	}
        	catch (CoreException ce) {
        		AptPlugin.log(ce, "Failure generating file"); //$NON-NLS-1$
        	}
        }
        else
        {
            assert false : "Unexpected phase value: " + phase ; //$NON-NLS-1$
        }
    }
			
	
    private StringWriter _sw;
    private String _typeName;
    private ProcessorEnvImpl _env;
}
