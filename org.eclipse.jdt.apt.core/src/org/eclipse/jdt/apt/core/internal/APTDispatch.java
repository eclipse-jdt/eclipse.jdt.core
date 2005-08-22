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
			final List<AnnotationProcessorFactory> factories,
			final IFile[] files,
			final IJavaProject javaProj,
			final boolean isFullBuild)
	{	
		return runAPT( factories, javaProj, files, null, isFullBuild );
	}
	
	/**
	 * Run annnotation processing.
	 * @param factories the list of annotation processor factories to be run.
	 * @return the set of files that need to be compiled.
	 */
	public static APTResult runAPTDuringReconcile(
			final List<AnnotationProcessorFactory> factories,
			ICompilationUnit compilationUnit, IJavaProject javaProj) 
	{
		return runAPT( factories, javaProj, null, compilationUnit, false /* does not matter*/ );
	}
		
	private static APTResult runAPT(final List<AnnotationProcessorFactory> factories,
			IJavaProject javaProj,
			IFile[] files,
			ICompilationUnit compilationUnit,
			boolean isFullBuild)
	{	

		assert ( files != null && compilationUnit == null ) ||
		       ( files == null && compilationUnit != null ) :
	    	"either compilation unit is null or set of files is, but not both"; //$NON-NLS-1$
	    
		APTDispatchRunnable runnable;
		ISchedulingRule schedulingRule;
		if ( files != null )
		{
			 runnable = new APTDispatchRunnable( files, javaProj, factories, isFullBuild );
			 schedulingRule = javaProj.getResource();
		}
		else
		{
			runnable = new APTDispatchRunnable( compilationUnit, javaProj, factories );
			schedulingRule = null;
		}
		
		try
		{
			IWorkspace w = ResourcesPlugin.getWorkspace();
			w.run( runnable, schedulingRule, IWorkspace.AVOID_UPDATE, null );
		}
		catch( CoreException ce )
		{
			AptPlugin.log(ce, "Could not run APT"); //$NON-NLS-1$
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
			_sourcePathChanged = false;
		}
		APTResult( Set<IFile> newFiles, Set<IFile> deletedFiles, Map<IFile, Set<String>> deps, Map<IFile, List<IProblem>> problems, boolean sourcePathChanged )
		{
			_newFiles = newFiles;
			_newDependencies = deps;
			_deletedFiles = deletedFiles;
			_newProblems = problems;
			_sourcePathChanged = sourcePathChanged;
		}
		
		private final Set<IFile> _newFiles;
		private final Set<IFile> _deletedFiles;
		private final Map<IFile, Set<String>> _newDependencies;
		private final Map<IFile, List<IProblem>> _newProblems;
		private final boolean _sourcePathChanged;
		
		Set<IFile> getNewFiles() { return _newFiles; }
		Set<IFile> getDeletedFiles() { return _deletedFiles; }
		Map<IFile, Set<String>> getNewDependencies() { return _newDependencies; }
		Map<IFile, List<IProblem>> getProblems(){return _newProblems;}
		boolean getSourcePathChanged() { return _sourcePathChanged; }
	}	
}
