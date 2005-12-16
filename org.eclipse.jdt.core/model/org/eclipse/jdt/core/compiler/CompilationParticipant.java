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
 * class to extend when for a client that wants to participate in the compilation process
 */
public abstract class CompilationParticipant {

	/**
	 * Called when the given project is about to be built.
	 * During this call, compilation participants are allowed to
	 * make build configuration changes like modifying the classpath.
	 * This build that is about to begin will honor the changes.
	 *
	 * This method will get called when the participant is interested
	 * in build events, by having returned <code>true</code> for
	 * {@link #doesParticipateInProject}.
	 *
	 * @param project the jave project that is about to be built.
	 */
	public void aboutToBuild(IJavaProject project) {
		// Default No-op
	}

	/**
	 * Called when a compilation event is fired. No compilation configuration
	 * can be change during this call.
	 */
	public CompilationParticipantResult notify( CompilationParticipantEvent e ) {
		return new CompilationParticipantResult();
	}

	/**
	 * During project compilation, this method is called on the compilation
	 * participant to discover if it is interested in this project.<P>
	 *
	 * For efficiency, participants that are not interested in a
	 * given project should return false for that project.<P>
	 *
	 * @return <code>true</code> iff the participant is interested in any subsequent
	 * compilation event. Returning <code>false</code> otherwise
	 */
	public abstract boolean doesParticipateInProject( IJavaProject project );


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
	 * Logical OR of all possible events.
	 */
	public static final int ALL_EVENTS = GENERIC_EVENT | PRE_RECONCILE_EVENT | PRE_BUILD_EVENT | CLEAN_EVENT;

}
