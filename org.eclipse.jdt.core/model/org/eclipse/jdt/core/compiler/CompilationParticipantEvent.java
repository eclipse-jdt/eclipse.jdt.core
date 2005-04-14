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

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Generic base class for an event passed into ICompilationParticipant's notify() method.  
 * This is subclassed to provide event-specific return information.
 * 
 * @see ICompilationParticipant#notify(CompilationParticipantEvent)
 * @since 3.2
 */
public  class CompilationParticipantEvent {

	/** 
	 * Construct a new CompilationParticipantEvent for the specified IJavaProject
	 * @param jp The IJavaProject that this compilation event is occurring on
	 */
	public CompilationParticipantEvent( IJavaProject jp)
	{
		_javaProject = jp;
	}

	/**
	 * @return an integer flag indicating the kind of event.  One of the *_EVENT 
	 * constants in ICompilationParticipant
	 * @see ICompilationParticipant#GENERIC_EVENT
	 */
	public int           getKind()              { return ICompilationParticipant.GENERIC_EVENT; }
	
	/**
	 * @return the IProject for this event
	 */
	public IProject      getProject()           { return _javaProject.getProject(); }

	/**
	 * @return the IJavaProject for this event
	 */
	public IJavaProject  getJavaProject()       { return _javaProject; }
	
	private IJavaProject _javaProject;
}
