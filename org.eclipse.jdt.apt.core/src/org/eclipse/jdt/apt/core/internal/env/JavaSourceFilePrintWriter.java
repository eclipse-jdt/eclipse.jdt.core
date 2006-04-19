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
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.generatedfile.FileGenerationResult;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedFileManager;

// note: only works in BUILD phase.
public class JavaSourceFilePrintWriter extends PrintWriter {

	private final StringWriter _sw;
    private final String _typeName;
    private final BuildEnv _env;
	
    public JavaSourceFilePrintWriter( String typeName, StringWriter sw, BuildEnv env )
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
	        GeneratedFileManager gfm = _env.getAptProject().getGeneratedFileManager();
	        FileGenerationResult result = gfm.generateFileDuringBuild( 
					_env.getFile(),  _typeName, contents, null /* progress monitor */ );
	        if (result != null) {
	        	_env.addGeneratedSourceFile(result.getFile(), result.isModified());
	        }
    	}
    	catch (CoreException ce) {
    		AptPlugin.log(ce, "Unable to generate type when JavaSourceFilePrintWriter was closed"); //$NON-NLS-1$
    	}
    }
}
