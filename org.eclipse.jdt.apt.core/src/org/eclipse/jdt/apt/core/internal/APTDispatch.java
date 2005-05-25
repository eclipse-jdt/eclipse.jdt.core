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

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;

import com.sun.mirror.apt.AnnotationProcessorFactory;

/**
 * Dispatch APT. 
 * @author tyeung
 *
 */
public class APTDispatch 
{	
	public static APTResult runAPTDuringBuild(
			final List<AnnotationProcessorFactory> factories, final IFile file,
			final IJavaProject javaProj) 
	{
		return runAPT( factories, javaProj, file, null );
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
		return runAPT( factories, javaProj, null, compilationUnit );
	}
		
	private static APTResult runAPT(final List<AnnotationProcessorFactory> factories,
			IJavaProject javaProj, IFile file, 
			ICompilationUnit compilationUnit )
	{
		
		AptDispatchRunnable runnable;
		if ( file != null )
			 runnable = new AptDispatchRunnable( file, javaProj, factories );
		else
			runnable = new AptDispatchRunnable( compilationUnit, javaProj, factories );
		
		IWorkspace w = ResourcesPlugin.getWorkspace();
		try
		{
			w.run( runnable, null );
		}
		catch( CoreException ce )
		{
			// TODO:  deal with this exception
			ce.printStackTrace();
		}
			
		return runnable.getResult();
	}


	public static class APTResult
	{
		APTResult( Set<IFile> newFiles, Set<IFile> deletedFiles, Set<String> deps )
		{
			_newFiles = newFiles;
			_newDependencies = deps;
			_deletedFiles = deletedFiles;
		}
		
		private Set<IFile> _newFiles;
		private Set<IFile> _deletedFiles;
		private Set<String> _newDependencies;
		
		Set<IFile> getNewFiles() { return _newFiles; }
		Set<IFile> getDeletedFiles() { return _deletedFiles; }
		Set<String> getNewDependencies() { return _newDependencies; }
	}

	
}
