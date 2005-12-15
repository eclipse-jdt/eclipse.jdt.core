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

package org.eclipse.jdt.apt.core.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.apt.core.internal.APTDispatch.APTResult;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedFileManager;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.CompilationParticipantEvent;
import org.eclipse.jdt.core.compiler.CompilationParticipantResult;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.compiler.PreBuildCompilationEvent;
import org.eclipse.jdt.core.compiler.PreBuildCompilationResult;
import org.eclipse.jdt.core.compiler.PreReconcileCompilationEvent;
import org.eclipse.jdt.core.compiler.PreReconcileCompilationResult;

import com.sun.mirror.apt.AnnotationProcessorFactory;

/**
 * A singleton object, created by callback through the
 * org.eclipse.jdt.core.compilationParticipants extension point.
 */
public class AptCompilationParticipant extends CompilationParticipant
{
	/** 
	 * Batch factories that claimed some annotation in a previous round of APT processing.
	 * This currently only apply to the build case since are only generating types during build
	 * and hence cause APT rounding.
	 * The set is an order preserving. The order is determined by their first invocation.
	 */
	private Set<AnnotationProcessorFactory> _previousRoundsBatchFactories = new LinkedHashSet<AnnotationProcessorFactory>();
	private static AptCompilationParticipant INSTANCE;
	
	public static AptCompilationParticipant getInstance() {
		return INSTANCE;
	}
	
	/**
	 * This class is constructed indirectly, by registering an extension to the 
	 * org.eclipse.jdt.core.compilationParticipants extension point.  Other
	 * clients should NOT construct this object.
	 */
	public AptCompilationParticipant()
	{
		INSTANCE = this;
	}

	public CompilationParticipantResult notify( CompilationParticipantEvent cpe )
	{	
        // We need to clean even if we have been disabled. This allows
		// us to remove our generated source files if we get disabled
        if ( cpe.getKind() == CompilationParticipant.CLEAN_EVENT ) {
            return cleanNotify( cpe );
        }
        else if (!AptConfig.isEnabled(cpe.getJavaProject())) {
			return GENERIC_COMPILATION_RESULT;
        }
        else if ( cpe == null ) {
			return GENERIC_COMPILATION_RESULT;
		}
		else if ( cpe.getKind() == CompilationParticipant.PRE_BUILD_EVENT ) {
			return preBuildNotify( (PreBuildCompilationEvent) cpe );
		}
		else if ( cpe.getKind() == CompilationParticipant.PRE_RECONCILE_EVENT ) {
			return preReconcileNotify( (PreReconcileCompilationEvent) cpe );
		}
		else {
			return GENERIC_COMPILATION_RESULT;
		}
	}
		
	private CompilationParticipantResult preBuildNotify( PreBuildCompilationEvent pbce )
	{		
		if ( pbce == null )
			return EMPTY_PRE_BUILD_COMPILATION_RESULT;

		IFile[] buildFiles = pbce.getFiles();
		IJavaProject javaProject = pbce.getJavaProject();
		
		if ( buildFiles == null || buildFiles.length == 0 )
			return EMPTY_PRE_BUILD_COMPILATION_RESULT;
		
		// Don't dispatch on pre-1.5 project. They cannot legally have annotations
		String javaVersion = javaProject.getOption("org.eclipse.jdt.core.compiler.source", true); //$NON-NLS-1$
		
		// Check for 1.3 or 1.4, as we don't want this to break in the future when 1.6
		// is a possibility
		if ("1.3".equals(javaVersion) || "1.4".equals(javaVersion)) { //$NON-NLS-1$ //$NON-NLS-2$
			return EMPTY_PRE_BUILD_COMPILATION_RESULT;
		}			
	
		// If we're in batch mode, we need to reset the classloaders
		// for the batch processors before we begin
		boolean isFullBuild = pbce.isFullBuild();
		try {
			if (isFullBuild && pbce.getRound() == 0) {
				AnnotationProcessorFactoryLoader.getLoader().resetBatchProcessors(pbce.getJavaProject());
				_previousRoundsBatchFactories.clear();
			}
			
			Map<AnnotationProcessorFactory, FactoryPath.Attributes> factories =
				AnnotationProcessorFactoryLoader.getLoader().getFactoriesAndAttributesForProject(javaProject);
			
			APTResult result = APTDispatch.runAPTDuringBuild(factories, _previousRoundsBatchFactories, buildFiles, javaProject, isFullBuild);
			Set<IFile> newFiles = result.getNewFiles();			
			Set<IFile> deletedFiles = new HashSet<IFile>();
			_previousRoundsBatchFactories.addAll(result.getDispatchedBatchFactory());
			
			// for apt, new files will always trump deleted files
			for ( IFile df : result.getDeletedFiles() ){
				if ( !newFiles.contains( df ) ){
					deletedFiles.add(df);
				}
			}
	
			return new PreBuildCompilationResult( 
					newFiles.toArray( new IFile[ newFiles.size() ] ), 
					deletedFiles.toArray( new IFile[ deletedFiles.size() ] ), 
					result.getNewDependencies(), 
					result.getProblems());
		}
		finally {
			if (isFullBuild) {
				// In order to keep from locking jars, we explicitly close any batch-based
				// classloaders we opened
				AnnotationProcessorFactoryLoader.getLoader().closeBatchClassLoader();
			}
		}
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
	
	private CompilationParticipantResult preReconcileNotify( PreReconcileCompilationEvent prce )
	{
		IProblem[] problems = null;
		
		try
		{
			org.eclipse.jdt.core.ICompilationUnit cu = prce.getCompilationUnit();
			IJavaProject javaProject = prce.getJavaProject();
			
			// these are null sometimes.  Not sure why...
			if ( cu == null || javaProject == null  )
				return GENERIC_COMPILATION_RESULT;
			
			Map<AnnotationProcessorFactory, FactoryPath.Attributes> factories = 
				AnnotationProcessorFactoryLoader.getLoader().getFactoriesAndAttributesForProject( javaProject );
			APTResult result = APTDispatch.runAPTDuringReconcile( factories, cu, javaProject );
			Map<IFile, List<IProblem>> allproblems = result.getProblems();			
			
			final List<IProblem> problemList = allproblems.get(cu.getResource());
			if( problemList != null && !problemList.isEmpty())
				problems = problemList.toArray(new IProblem[problemList.size()]);	
		}
		catch ( Throwable t )
		{
			AptPlugin.log(t, "Failure processing"); //$NON-NLS-1$
		}	
		return new PreReconcileCompilationResult(problems);
	}

	private CompilationParticipantResult cleanNotify( CompilationParticipantEvent cpe )
	{
		IJavaProject javaProject = cpe.getJavaProject();
		IProject p = javaProject.getProject();
		
		GeneratedFileManager gfm = AptPlugin.getAptProject(javaProject).getGeneratedFileManager();
		gfm.projectClean( true );
		try{
			// clear out all markers during a clean.
			IMarker[] markers = p.findMarkers(AptPlugin.APT_PROCESSOR_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			if( markers != null ){
				for( IMarker marker : markers )
					marker.delete();
			}
		}
		catch(CoreException e){
			AptPlugin.log(e, "Unable to delete annotation processor problem markers"); //$NON-NLS-1$
		}
		
		return GENERIC_COMPILATION_RESULT;
	}	
	
	/**
	 * Does APT have anything to do for this project?
	 * Even if there are no processors on the factory path, apt may still
	 * be involved during a clean.
	 */
	public boolean doesParticipateInProject(IJavaProject project){
		return AptConfig.isEnabled(project);
	}
	
	public void aboutToBuild(IJavaProject project) {
		if (!AptConfig.isEnabled(project)) {
			return;
		}		
		// setup the classpath and make sure the generated source folder is on disk.
		GeneratedFileManager manager = AptPlugin.getAptProject(project).getGeneratedFileManager();
		manager.compilationStarted();
	}
    
    private final static String DOT_JAVA = ".java"; //$NON-NLS-1$
	
	private final static PreBuildCompilationResult EMPTY_PRE_BUILD_COMPILATION_RESULT = 
		new PreBuildCompilationResult( new IFile[0], new IFile[0], Collections.emptyMap(), Collections.emptyMap() );
		
	private final static CompilationParticipantResult GENERIC_COMPILATION_RESULT = 
		new CompilationParticipantResult();

}
