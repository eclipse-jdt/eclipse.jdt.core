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
    	
    	try {
	    	String contents = _sw.toString();
	        super.close();
	        GeneratedFileManager gfm = GeneratedFileManager.getGeneratedFileManager(_env.getProject());
	        Phase phase = _env.getPhase();
		
	        FileGenerationResult result = null;
	        if ( phase == Phase.RECONCILE )
	        {
	        	ICompilationUnit parentCompilationUnit = _env.getCompilationUnit();
	            result  = gfm.generateFileDuringReconcile( 
	                parentCompilationUnit, _typeName, contents, parentCompilationUnit.getOwner(), null, null );
	        }
	        else if ( phase == Phase.BUILD)	
	        {
				result = gfm.generateFileDuringBuild( 
						_env.getFile(),  _typeName, contents, _env, null /* progress monitor */ );
	        }
	        else
	        {
	            throw new IllegalStateException( "Unexpected phase value: " + phase ); //$NON-NLS-1$
	        }
	        if (result != null) {
	        	_env.addGeneratedFile(result.getFile(), result.isModified());
	        	if (result.hasSourcepathChanged()) {
	        		_env.setSourcePathChanged(true);
	        	}
	        }
    	}
    	catch (CoreException ce) {
    		AptPlugin.log(ce, "Unable to generate type when JavaSourceFilePrintWriter was closed"); //$NON-NLS-1$
    	}
    }
			
	
    private StringWriter _sw;
    private String _typeName;
    private ProcessorEnvImpl _env;
}
