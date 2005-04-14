/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *    
 *******************************************************************************/

// TODO: this class gets constructed and called from JDT Core.  So it needs to
// be 1.4-compliant, and to contain the "switch" to disable the rest of the code
// if we are running on 1.4 and/or if tools.jar is unavailable. - WHarley 3/05

package org.eclipse.jdt.apt.core.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.CompilationParticipantEvent;
import org.eclipse.jdt.core.compiler.CompilationParticipantResult;
import org.eclipse.jdt.core.compiler.ICompilationParticipant;
import org.eclipse.jdt.core.compiler.PostReconcileCompilationEvent;
import org.eclipse.jdt.core.compiler.PostReconcileCompilationResult;
import org.eclipse.jdt.core.compiler.PreBuildCompilationEvent;
import org.eclipse.jdt.core.compiler.PreBuildCompilationResult;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

import com.sun.mirror.apt.AnnotationProcessorFactory;


public class BuildListener implements ICompilationParticipant
{
	/**
	 * This class is constructed indirectly, by registering an extension to the 
	 * org.eclipse.jdt.core.compilationParticipants extension point.
	 */
	public BuildListener()
	{
        _factoryLoader = new AnnotationProcessorFactoryLoader();
		_factoryLoader.loadFactoriesFromPlugins();
        _factories = _factoryLoader.getFactories();
	}
	
    private String getUnitName( ICompilationUnit sourceUnit )
    {
    	char[] mainTypeName = sourceUnit.getMainTypeName();
    	StringBuffer sb = new StringBuffer( mainTypeName.length + DOT_JAVA.length() );
    	sb.append( mainTypeName );
    	sb.append( DOT_JAVA );
    	return sb.toString();
    }

	public CompilationParticipantResult notify( CompilationParticipantEvent cpe )
	{	
		if ( cpe == null )
			return GENERIC_COMPILATION_RESULT;

		else if ( cpe instanceof PreBuildCompilationEvent )
			return preBuildNotify( (PreBuildCompilationEvent) cpe );
		
		else if ( cpe instanceof PostReconcileCompilationEvent )
			return postReconcileNotify( (PostReconcileCompilationEvent) cpe );

		else 
			return GENERIC_COMPILATION_RESULT;		
	}
		
	private CompilationParticipantResult preBuildNotify( PreBuildCompilationEvent pbce )
	{		
		if ( pbce == null )
			return EMPTY_PRE_BUILD_COMPILATION_RESULT;

		IFile[] buildFiles = pbce.getFiles();
		IJavaProject javaProject = pbce.getJavaProject();
		
		if ( buildFiles == null || buildFiles.length == 0 )
			return EMPTY_PRE_BUILD_COMPILATION_RESULT;

		HashSet<IFile> newFiles = new HashSet<IFile>();
		for ( int i = 0; i < buildFiles.length; i++ )
		{
			Set<IFile> files = APTDispatch.runAPTDuringBuild( 
					_factories, 
					buildFiles[i], 
					javaProject );
			newFiles.addAll( files );
		}
		
		return new PreBuildCompilationResult( newFiles.toArray( new IFile[ newFiles.size() ] ), Collections.emptyMap() ); 
	}
	
	private CompilationParticipantResult postReconcileNotify( PostReconcileCompilationEvent prce )
	{
		try
		{
			org.eclipse.jdt.core.ICompilationUnit cu = prce.getCompilationUnit();
			CompilationUnit ast = prce.getAst();	
			IJavaProject javaProject = prce.getJavaProject();
			
			// these are null sometimes.  Not sure why...
			if ( ast == null || cu == null || javaProject == null  )
				return GENERIC_COMPILATION_RESULT;
			
			APTDispatch.runAPTDuringReconcile( _factories, ast, cu, javaProject );
		}
		catch ( Throwable t )
		{
			t.printStackTrace();
		}	
		return new PostReconcileCompilationResult();
	}

    private List<AnnotationProcessorFactory> _factories;
    private AnnotationProcessorFactoryLoader _factoryLoader;
    private final static String DOT_JAVA = ".java";
	
	private final static PreBuildCompilationResult EMPTY_PRE_BUILD_COMPILATION_RESULT = 
		new PreBuildCompilationResult( new IFile[0], Collections.emptyMap() );

	private final static CompilationParticipantResult GENERIC_COMPILATION_RESULT = 
		new CompilationParticipantResult();
}
