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

import org.eclipse.jdt.core.IJavaProject;

/**
 * An event class passed into ICompilationParticipant's notify() method after a build
 * has completed.
 * 
 * @see ICompilationParticipant#notify(CompilationParticipantEvent)
 * @since 3.2
 */
public class PostBuildCompilationEvent extends CompilationParticipantEvent {

	PostBuildCompilationEvent( IJavaProject jp )
	{
		super( jp );
	}
	
	/**
	 * @return an integer flag indicating that this is a post-build event.
	 * @see ICompilationParticipant#POST_BUILD_EVENT
	 * @see CompilationParticipantEvent#getKind()
	 */
	public final int getKind() { return ICompilationParticipant.POST_BUILD_EVENT; }
}
