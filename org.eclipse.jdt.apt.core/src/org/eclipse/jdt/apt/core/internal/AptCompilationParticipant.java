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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.compiler.ICompilationParticipantResult;
import org.eclipse.jdt.core.compiler.ReconcileContext;

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
	private int _buildRound = 0;
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
	
	public boolean isAnnotationProcessor(){
		return true;
	}
	
	public void processAnnotations(ICompilationParticipantResult[] filesWithAnnotations, boolean isBatchBuild) {
		if( filesWithAnnotations == null || filesWithAnnotations.length == 0 )
			return;
		final IProject project = filesWithAnnotations[0].getFile().getProject();
		final IJavaProject javaProject = JavaCore.create(project);
		// Don't dispatch on pre-1.5 project. They cannot legally have annotations
		String javaVersion = javaProject.getOption("org.eclipse.jdt.core.compiler.source", true); //$NON-NLS-1$		
		// Check for 1.3 or 1.4, as we don't want this to break in the future when 1.6
		// is a possibility
		if ("1.3".equals(javaVersion) || "1.4".equals(javaVersion)) { //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}			
		
		try {
			if (isBatchBuild && _buildRound == 0 ) {
				AnnotationProcessorFactoryLoader.getLoader().resetBatchProcessors(javaProject);
				_previousRoundsBatchFactories.clear();
			}
		
			Map<AnnotationProcessorFactory, FactoryPath.Attributes> factories =
				AnnotationProcessorFactoryLoader.getLoader().getFactoriesAndAttributesForProject(javaProject);
			
			AptProject aptProject = AptPlugin.getAptProject(javaProject);
			Set<AnnotationProcessorFactory> dispatchedBatchFactories = 
				APTDispatchRunnable.runAPTDuringBuild(filesWithAnnotations, aptProject, factories, _previousRoundsBatchFactories, isBatchBuild);
			_previousRoundsBatchFactories.addAll(dispatchedBatchFactories);
		}
		finally {
			if (isBatchBuild) {
				// In order to keep from locking jars, we explicitly close any batch-based
				// classloaders we opened
				AnnotationProcessorFactoryLoader.getLoader().closeBatchClassLoader();
			}
			_buildRound ++;
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
	
	public void reconcile(ReconcileContext context){
		
		try
		{	
			final ICompilationUnit workingCopy = context.getWorkingCopy();
			if( workingCopy == null ) 
				return;
			IJavaProject javaProject = workingCopy.getJavaProject();			
			if( javaProject == null )
				return;
			AptProject aptProject = AptPlugin.getAptProject(javaProject);
			
			Map<AnnotationProcessorFactory, FactoryPath.Attributes> factories = 
				AnnotationProcessorFactoryLoader.getLoader().getFactoriesAndAttributesForProject( javaProject );
			APTDispatchRunnable.runAPTDuringReconcile(context, aptProject, factories);
		}
		catch ( Throwable t )
		{
			AptPlugin.log(t, "Failure processing");  //$NON-NLS-1$
		}	
	}
	
	public void cleanStarting(IJavaProject javaProject){
		IProject p = javaProject.getProject();
		
		AptPlugin.getAptProject(javaProject).projectClean( true );
		try{
			// clear out all markers during a clean.
			IMarker[] markers = p.findMarkers(AptPlugin.APT_BATCH_PROCESSOR_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			if( markers != null ){
				for( IMarker marker : markers )
					marker.delete();
			}
		}
		catch(CoreException e){
			AptPlugin.log(e, "Unable to delete batch annotation processor markers"); //$NON-NLS-1$
		}
	}
	
	/**
	 * Does APT have anything to do for this project?
	 * Even if there are no processors on the factory path, apt may still
	 * be involved during a clean.
	 */
	public boolean isActive(IJavaProject project){
		return AptConfig.isEnabled(project);
	}
	
	public int aboutToBuild(IJavaProject project) {
		if (AptConfig.isEnabled(project)) {
			// setup the classpath and make sure the generated source folder is on disk.
		AptPlugin.getAptProject(project).compilationStarted();
		}		
		_buildRound = 0; // reset
		// TODO: (wharley) if the factory path is different we need a full build
		return CompilationParticipant.READY_FOR_BUILD;
	}
    
    private final static String DOT_JAVA = ".java"; //$NON-NLS-1$
}
