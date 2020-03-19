/*******************************************************************************
 * Copyright (c) 2005, 2018 BEA Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.env;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.apt.core.env.Phase;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.generatedfile.FileGenerationResult;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedFileManager;
import org.eclipse.jdt.core.ICompilationUnit;

public class JavaSourceFilePrintWriter extends PrintWriter {

	private final StringWriter _sw;
    private final String _typeName;
    private final AbstractCompilationEnv _env;

    /**
     * @throws CoreException if type name is not valid
     */
	public JavaSourceFilePrintWriter( String typeName, StringWriter sw, AbstractCompilationEnv env )
    	throws CoreException
    {
        super( sw );
        _sw = sw;
        _typeName = typeName;
        _env = env;
		_env.validateTypeName(typeName);
    }

    @Override
	public void close()
    {
    	try {
	    	String contents = _sw.toString();
	        super.close();
	        GeneratedFileManager gfm = _env.getAptProject().getGeneratedFileManager(_env.isTestCode());
	        Phase phase = _env.getPhase();

	        FileGenerationResult result = null;
	        if ( phase == Phase.RECONCILE && _env.currentProcessorSupportsRTTG() )
	        {
	        	ReconcileEnv reconcileEnv = (ReconcileEnv)_env;
	        	ICompilationUnit parentCompilationUnit = reconcileEnv.getCompilationUnit();
	            result  = gfm.generateFileDuringReconcile(
	                parentCompilationUnit, _typeName, contents );
	            // Need to call ReconcileContext.resetAst() for this to be effective;
	            // that will happen in ReconcileEnv.close().
	        }
	        else if ( phase == Phase.BUILD)	{
		        result = gfm.generateFileDuringBuild(
						Collections.singletonList(_env.getFile()),  _typeName, contents,
						_env.currentProcessorSupportsRTTG(), null /* progress monitor */ );
	        }
	        if (result != null) {
	        	_env.addGeneratedSourceFile(result.getFile(), result.isModified());
	        }
    	}
    	catch (CoreException ce) {
    		AptPlugin.log(ce, "Unable to generate type when JavaSourceFilePrintWriter was closed"); //$NON-NLS-1$
    	}
    }
}
