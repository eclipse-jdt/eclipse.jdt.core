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

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;

/**
 * An event class passed into ICompilationParticipant's notify() method before a build
 * has completed.
 * 
 * @see ICompilationParticipant#notify(CompilationParticipantEvent)
 * @since 3.2
 */
public class PreBuildCompilationEvent extends CompilationParticipantEvent {
	
	/**
	 * @param files - IFile[] of the files about to be compiled
	 * @param jp - the IJavaProject for the IFiles[] being compiled
	 * @param isFullBuild <code>true</code> to indicate a full build, build after a clean.
	 * @param round the current round number, 0-based. 
	 */
	public PreBuildCompilationEvent( IFile[] files, IJavaProject jp, boolean isFullBuild, int round ) 
	{ 
		super( jp );
		_files = files;
		_isFullBuild = isFullBuild;
		_round = round; 
	}
	
	/**
	 * Within a build, compilation pariticpants will be repeatedly invoked until no
	 * generated files are reported. This indicates the number of times the 
	 * participant has been invoked within the same build. Number is 0-based.
	 * @return the current round number with the build.
	 */
	public int getRound(){ return _round; }
	
	/**
	 * @return -  IFile[] containing the files about to be compiled
	 */
	public IFile[] getFiles()  { return _files; }
	
	/**
	 * @return <code>true</code> iff this the build is going to be a clean/full build.
	 * Return <code>false</code> otherwise.
	 */
	public boolean isFullBuild(){ return _isFullBuild; }
	
	/**
	 * @return an integer flag indicating that this is a pre-build event.
	 * @see ICompilationParticipant#PRE_BUILD_EVENT
	 * @see CompilationParticipantEvent#getKind()
	 */
	public final int getKind() { return ICompilationParticipant.PRE_BUILD_EVENT; }

	private IFile[] _files;
	
	private final boolean _isFullBuild;
	
	private final int _round;
	
}
