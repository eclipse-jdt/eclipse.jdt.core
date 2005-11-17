/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *    mkaufman@bea.com
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.IProblem;

import com.sun.mirror.apt.AnnotationProcessorFactory;

/**
 * Dispatch APT. 
 * @author tyeung
 *
 */
public class APTDispatch 
{	
	public static APTResult runAPTDuringBuild(
			final Map<AnnotationProcessorFactory, FactoryPath.Attributes> factories,
			final Set<AnnotationProcessorFactory> previousRoundsBatchFactories,
			final IFile[] files,
			final IJavaProject javaProj,
			final boolean isFullBuild)
	{	
		return runAPT( factories, previousRoundsBatchFactories, javaProj, files, null, isFullBuild );
	}
	
	/**
	 * Run annnotation processing.
	 * @param factories the list of annotation processor factories to be run.
	 * @return the set of files that need to be compiled.
	 */
	public static APTResult runAPTDuringReconcile(
			final Map<AnnotationProcessorFactory, FactoryPath.Attributes> factories,
			final ICompilationUnit compilationUnit, 
			final IJavaProject javaProj) 
	{
		return runAPT( factories, Collections.<AnnotationProcessorFactory>emptySet(), javaProj, null, compilationUnit, false /* does not matter*/ );
	}
		
	/**
	 * If files is null, we are reconciling. If compilation unit is null, we are building
	 */
	private static APTResult runAPT(
			Map<AnnotationProcessorFactory, FactoryPath.Attributes> factories,
			Set<AnnotationProcessorFactory> previousRoundsFactories,
			IJavaProject javaProj,
			IFile[] files,
			ICompilationUnit compilationUnit,
			boolean isFullBuild)
	{	

		assert ( files != null && compilationUnit == null ) ||
		       ( files == null && compilationUnit != null ) :
	    	"either compilation unit is null or set of files is, but not both"; //$NON-NLS-1$
		       
		boolean building = files != null;
	    
		APTDispatchRunnable runnable;
		ISchedulingRule schedulingRule;
		if ( building )
		{
			// If we're building, types can be generated, so we
			// want to run this as an atomic workspace operation
			 runnable = new APTDispatchRunnable( files, javaProj, factories, previousRoundsFactories, isFullBuild );
			 schedulingRule = javaProj.getResource();
			 IWorkspace workspace = ResourcesPlugin.getWorkspace();
			 try {
				 workspace.run(runnable, schedulingRule, IWorkspace.AVOID_UPDATE, null);
			 }
			 catch (CoreException ce) {
				 AptPlugin.log(ce, "Could not run APT"); //$NON-NLS-1$
			 }
		}
		else
		{
			// Reconciling, so we do not want to run this as an atomic workspace
			// operation. If we do, it is easy to have locking issues when someone
			// calls a reconcile from within a workspace lock
			runnable = new APTDispatchRunnable( compilationUnit, javaProj, factories );
			runnable.run(null);
		}
			
		return runnable.getResult();
	}


	public static class APTResult
	{
		/**
		 * For creating an empty result. i.e. no file changes, no new dependencies and not
		 * new problems.
		 */
		APTResult()
		{
			_newFiles = Collections.emptySet();
			_deletedFiles = Collections.emptySet();
			_newDependencies = Collections.emptyMap();
			_newProblems = Collections.emptyMap();
			_dispatchedBatchFactories = Collections.emptySet();
			_sourcePathChanged = false;
			_hasGeneratedTypes = false;
		}
		APTResult( 
				Set<IFile> newFiles, 
				Set<IFile> deletedFiles,
				Set<AnnotationProcessorFactory> dispatchedBatchFactories,
				Map<IFile, Set<String>> deps, 
				Map<IFile, List<IProblem>> problems, 
				boolean sourcePathChanged,
				boolean hasGeneratedTypes)
		{
			_newFiles = newFiles;
			_newDependencies = deps;
			_deletedFiles = deletedFiles;
			_newProblems = problems;
			_dispatchedBatchFactories = dispatchedBatchFactories;
			_sourcePathChanged = sourcePathChanged;
			_hasGeneratedTypes = hasGeneratedTypes;
		}
		
		private final Set<IFile> _newFiles;
		private final Set<IFile> _deletedFiles;
		private final Map<IFile, Set<String>> _newDependencies;
		private final Map<IFile, List<IProblem>> _newProblems;
		private final Set<AnnotationProcessorFactory> _dispatchedBatchFactories;
		private boolean _sourcePathChanged;
		private boolean _hasGeneratedTypes;
		private boolean _mutable = true;
		
		Set<IFile> getNewFiles() { return Collections.unmodifiableSet(_newFiles); }
		Set<IFile> getDeletedFiles() { return Collections.unmodifiableSet(_deletedFiles); }
		Set<AnnotationProcessorFactory> getDispatchedBatchFactory(){ return Collections.unmodifiableSet(_dispatchedBatchFactories); }
		Map<IFile, Set<String>> getNewDependencies() { return Collections.unmodifiableMap(_newDependencies); }
		void removeDependenciesFrom(IFile file){
			mutate();
			_newDependencies.remove(file);
		}
		
		Map<IFile, List<IProblem>> getProblems(){return Collections.unmodifiableMap(_newProblems);}
		void removeProblemsFrom(IFile file){
			mutate();
			_newProblems.remove(file);
		}
		
		boolean getSourcePathChanged() { return _sourcePathChanged; }
		boolean hasGeneratedTypes(){ return _hasGeneratedTypes; }
		
		void setReadOnly(){
			_mutable = true;
		}
		
		private void mutate(){ 
			if( !_mutable )
				throw new IllegalStateException("modifications not allowed"); //$NON-NLS-1$
		}
		
		void merge(APTResult otherResult){
			mutate();
			_newFiles.addAll(otherResult._newFiles);
			_deletedFiles.addAll(otherResult._deletedFiles);
			_dispatchedBatchFactories.addAll(otherResult._dispatchedBatchFactories);
			mergeMaps(_newDependencies, otherResult._newDependencies);
			mergeMaps(_newProblems, otherResult._newProblems);
			_sourcePathChanged |= otherResult._sourcePathChanged;
			_hasGeneratedTypes |= otherResult._hasGeneratedTypes;
		}
		
		/**
		 * This method assumes that the values of the two maps are of compatible type. 
		 * If not, {@link ClassCastException} will be thrown. If the values of the maps are not collections 
		 * and the keys collide, then {@link IllegalStateException} will be thrown.
		 * @param destination 
		 * @param source moving everything into <code>destination</code
		 *
		 */
		private void mergeMaps(final Map destination, final Map source )
		{		
			for( Object o : source.entrySet() )
			{
				final Map.Entry entry = (Map.Entry)o;
				final Object destValue = destination.get(entry.getKey());
				if( destValue == null )
					destination.put( entry.getKey(), entry.getValue() );
				else{
					if( destValue instanceof Collection )
					{
						final Collection destCollection = (Collection)destination;
						// A ClassCastException would occur if entry.getValue() doesn't return
						// a collection. 
						final Collection sourceCollection = (Collection)entry.getValue();
						destCollection.addAll(sourceCollection);
					}
					else
						throw new IllegalStateException("keys collided"); //$NON-NLS-1$
				}
			}
		}		
	}
}
