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
 * Interface to extend when for a client that wants to participate in the compilation process
 */
public interface ICompilationParticipant {
	
	/**
	 * Called when a compilation event is fired
	 */
	public CompilationParticipantResult notify( CompilationParticipantEvent e );

	/**
	 * During project compilation, this method is called on the compilation
	 * participant to discover if it is interested in this project.<P>
	 * 
	 * For efficiency, participants that are not interested in a 
	 * given project should return false for that project.<P>
	 */
	public boolean doesParticipateInProject( IJavaProject project );
	
	
	/** 
	 * a flag indicating a generic event 
	 */
	public static final int GENERIC_EVENT        = 1;
	
	/** 
	 * a flag indicating an event fired before reconcile 
	 */
	public static final int PRE_RECONCILE_EVENT = 2;
	
	/** 
	 * a flag indicating an event fired before a build 
	 */
	public static final int PRE_BUILD_EVENT      = 4;
	
	/** 
	 * a flag indicating an event fired before a clean operation 
	 */
	public static final int CLEAN_EVENT          = 8;
	
	/**
	 * a flag indicating an event fired when there is a catastrophic build 
	 * failure, such as a broken classpath.
	 */
	public static final int BROKEN_CLASSPATH_BUILD_FAILURE_EVENT = 16;
	
}
