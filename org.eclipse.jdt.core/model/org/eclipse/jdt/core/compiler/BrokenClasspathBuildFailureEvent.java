/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *******************************************************************************/


package org.eclipse.jdt.core.compiler;

import org.eclipse.jdt.core.IJavaProject;

/**
 * An event class passed into ICompilationParticipant's notify() method when a build
 * has a catastrophic error that prevents the build from even running (e.g., a 
 * classpath error).
 * 
 *  @see ICompilationParticipant#notify(CompilationParticipantEvent)
 *  @since 3.2
 */
public class BrokenClasspathBuildFailureEvent extends CompilationParticipantEvent {

	public BrokenClasspathBuildFailureEvent( IJavaProject jp )
	{
		super( jp );
	}
	
	/**
	* @return an integer flag indicating the kind of event.  In this case, 
	* BROKEN_CLASSPATH_BUILD_FAILURE_EVENT
	* @see ICompilationParticipant#BROKEN_CLASSPATH_BUILD_FAILURE_EVENT
	*/
	public int           getKind()              { return ICompilationParticipant.BROKEN_CLASSPATH_BUILD_FAILURE_EVENT; }
}
