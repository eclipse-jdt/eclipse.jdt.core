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
	 */
	public PreBuildCompilationEvent( IFile[] files, IJavaProject jp ) 
	{ 
		super( jp );
		_files = files;
	}
	
	/**
	 * @return -  IFile[] containing the files about to be compiled
	 */
	public IFile[] getFiles()  { return _files; }
	
	/**
	 * @return an integer flag indicating that this is a pre-build event.
	 * @see ICompilationParticipant#PRE_BUILD_EVENT
	 * @see CompilationParticipantEvent#getKind()
	 */
	public final int getKind() { return ICompilationParticipant.PRE_BUILD_EVENT; }

	private IFile[] _files;
}
