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

package org.eclipse.jdt.core.compiler;

import java.util.Map;

import org.eclipse.core.resources.IFile;

/**
 * An result class used as a return value from ICompilationParticipant's notify() method 
 * when responding to a pre-build event.
 * 
 * @see ICompilationParticipant#notify(CompilationParticipantEvent)
 * @since 3.2
 */
public class PreBuildCompilationResult extends CompilationParticipantResult {

	/**
	 * Construct a new PreBuildCompilationResult.  
	 * 
	 * @param newFiles  - array of newly generated files that need to be inserted into
	 *                 the compilation loop
	 * @param newDependencyInfo - Map<IFile, Set<String>> where each entry in the map
	 *        is the set of fully-qualified type names which are new dependencies 
	 *        for the IFile.  The JDT will record these new dependencies in its depdency
	 *        matrix. 
	 */
	public PreBuildCompilationResult( IFile[] newFiles, IFile[] deletedFiles, Map newDependencyInfo, Map newProblems )
	{
		_newFiles = newFiles;
		_deletedFiles = deletedFiles;
		_newDependencyInfo = newDependencyInfo;
		_newProblems = newProblems;
	}
	
	/** 
	 * 
	 * @return IFile[] that is the set of new files to be added into the compilation
	 * set.
	 */
	public IFile[] getNewFiles() { return _newFiles; }
	
	/** 
	 * 
	 * @return IFile[] that is the set of files to be deleted from the compilation set
	 */
	public IFile[] getDeletedFiles() { return _deletedFiles; }
		
	/**
	 * 
	 * @return Map<IFile, Set<String>> that maps IFile to the fully-qualified type
	 * names of any new dependencies that need to be added into the JDT's dependency
	 * matrix.
	 */
	public Map     getNewDependencies() { return _newDependencyInfo; }
	
	/**
	 * 
	 * @return Map<IFile, List<IProblem>> that maps the files built to the problems 
	 * encountered during prebuild compilation.
	 */
	public Map 	   getProblems(){ return _newProblems; }
	
	/**
	 * @return an integer flag indicating that this is result for a pre-build event.
	 * @see ICompilationParticipant#PRE_BUILD_EVENT
	 * @see CompilationParticipantEvent#getKind()
	 */
	public final int getKind() { return ICompilationParticipant.PRE_BUILD_EVENT; }
	
	private IFile[] _newFiles;
	private IFile[] _deletedFiles;
	private Map     _newDependencyInfo;
	private Map		_newProblems;

}
