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

/**
 * Interface to extend when for a client that wants to participate in the compilation process
 */
public interface ICompilationParticipant {
	
	public CompilationParticipantResult notify( CompilationParticipantEvent e );

	
	/** a flag indicating a generic event */
	public static final int GENERIC_EVENT        = 1;
	/** a flag indicating an event fired after reconcile */
	public static final int POST_RECONCILE_EVENT = 2;
	/** a flag indicating an event fired before a build */
	public static final int PRE_BUILD_EVENT      = 4;
	/** a flag indicating an event fired after a build */
	public static final int POST_BUILD_EVENT     = 8;
	/** a flag indicating an event fired before a clean operation */
	public static final int CLEAN_EVENT          = 16;
}
