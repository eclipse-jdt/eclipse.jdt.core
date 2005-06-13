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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.apt.core.internal.APTDispatch.APTResult;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedFileManager;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.CompilationParticipantEvent;
import org.eclipse.jdt.core.compiler.CompilationParticipantResult;
import org.eclipse.jdt.core.compiler.ICompilationParticipant;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.compiler.PostReconcileCompilationEvent;
import org.eclipse.jdt.core.compiler.PostReconcileCompilationResult;
import org.eclipse.jdt.core.compiler.PreBuildCompilationEvent;
import org.eclipse.jdt.core.compiler.PreBuildCompilationResult;

import com.sun.mirror.apt.AnnotationProcessorFactory;


public class AptCompilationParticipant implements ICompilationParticipant
{
	private static AptCompilationParticipant INSTANCE;
	
	public static AptCompilationParticipant getInstance() {
		return INSTANCE;
	}
	
	/**
	 * This class is constructed indirectly, by registering an extension to the 
	 * org.eclipse.jdt.core.compilationParticipants extension point.
	 */
	public AptCompilationParticipant()
	{
        _factoryLoader = AnnotationProcessorFactoryLoader.getLoader();
        INSTANCE = this;
	}

	public CompilationParticipantResult notify( CompilationParticipantEvent cpe )
	{	
		if (!AptConfig.isEnabled())
			return GENERIC_COMPILATION_RESULT;
		
		if ( cpe == null )
			return GENERIC_COMPILATION_RESULT;

		else if ( cpe.getKind() == ICompilationParticipant.PRE_BUILD_EVENT )
			return preBuildNotify( (PreBuildCompilationEvent) cpe );
		
		else if ( cpe.getKind() == ICompilationParticipant.POST_RECONCILE_EVENT )
			return postReconcileNotify( (PostReconcileCompilationEvent) cpe );
		else if ( cpe.getKind() == ICompilationParticipant.CLEAN_EVENT )
			return cleanNotify( cpe );
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
		HashSet<IFile> deletedFiles = new HashSet<IFile>();
		HashMap<IFile, Set<String>> newDependencies = new HashMap<IFile, Set<String>>();
		HashMap<IFile, List<IProblem>> problems = new HashMap<IFile, List<IProblem>>(4);
		List<AnnotationProcessorFactory> factories = _factoryLoader.getFactoriesForProject( javaProject.getProject() );
		for ( int i = 0; i < buildFiles.length; i++ )
		{
			APTResult result = APTDispatch.runAPTDuringBuild( 
					factories, 
					buildFiles[i], 
					javaProject );
			newFiles.addAll( result.getNewFiles() );			
			deletedFiles.addAll( result.getDeletedFiles() );
			newDependencies.put( buildFiles[i], result.getNewDependencies() );	
			mergeMaps(result.getProblems(), problems);
		}
		
		// for apt, new files will always trump deleted files
		for ( IFile df : deletedFiles )
			if ( newFiles.contains( df ) )
				deletedFiles.remove( df );

		return new PreBuildCompilationResult( newFiles.toArray( new IFile[ newFiles.size() ] ), deletedFiles.toArray( new IFile[ deletedFiles.size() ] ), newDependencies, problems ); 
	}
	
	/** 
	 *   Given a Map which maps from a key to a value, where key is an arbitrary 
	 *   type, and where value is a Collection, mergeMaps will ensure that for a key 
	 *   k with value v in source, all of the elements in the Collection v will be 
	 *   moved into the Collection v' corresponding to key k in the destination Map. 
	 * 
	 * @param source - The source map from some key to a Collection.
	 * @param destination - The destination map from some key to a Collection
	 */
	private static void mergeMaps( Map source, Map destination ) {
		if( source == null || destination == null ) return;
		Iterator keys = source.keySet().iterator();
		while( keys.hasNext() ) {
			Object key = keys.next();
			Object val = destination.get( key );
			if ( val != null ) {
				Collection c = (Collection) val;
				c.addAll( (Collection)source.get( key ) );
			}
			else {
				destination.put( key, source.get( key ) );
			}
		}
	}
	
	private CompilationParticipantResult postReconcileNotify( PostReconcileCompilationEvent prce )
	{
		IProblem[] problems = null;
		
		try
		{
			org.eclipse.jdt.core.ICompilationUnit cu = prce.getCompilationUnit();
			IJavaProject javaProject = prce.getJavaProject();
			
			// these are null sometimes.  Not sure why...
			if ( cu == null || javaProject == null  )
				return GENERIC_COMPILATION_RESULT;
			
			List<AnnotationProcessorFactory> factories = _factoryLoader.getFactoriesForProject( javaProject.getProject() );
			APTResult result = APTDispatch.runAPTDuringReconcile( factories, cu, javaProject );
			Map<IFile, List<IProblem>> allproblems = result.getProblems();			
			
			final List<IProblem> problemList = allproblems.get((IFile)cu.getResource());
			if( problemList != null && !problemList.isEmpty())
				problems = problemList.toArray(new IProblem[problemList.size()]);	
		}
		catch ( Throwable t )
		{
			t.printStackTrace();
		}	
		return new PostReconcileCompilationResult(problems);
	}

	private CompilationParticipantResult cleanNotify( CompilationParticipantEvent cpe )
	{		
		IProject p = cpe.getJavaProject().getProject();
		GeneratedFileManager gfm = GeneratedFileManager.getGeneratedFileManager( p );
		gfm.projectClean( true );
		
		return GENERIC_COMPILATION_RESULT;
	}
	
	public boolean doesParticipateInProject(IJavaProject project) {
		List<AnnotationProcessorFactory> factories = _factoryLoader.getFactoriesForProject( project.getProject() );
		if (factories.size() == 0)
			return false;
		
		//TODO: use config to decide which projects we support
		return true;
	}

    private AnnotationProcessorFactoryLoader _factoryLoader;
    private final static String DOT_JAVA = ".java";
	
	private final static PreBuildCompilationResult EMPTY_PRE_BUILD_COMPILATION_RESULT = 
		new PreBuildCompilationResult( new IFile[0], new IFile[0], Collections.emptyMap(), Collections.emptyMap() );

	private final static CompilationParticipantResult GENERIC_COMPILATION_RESULT = 
		new CompilationParticipantResult();

}
