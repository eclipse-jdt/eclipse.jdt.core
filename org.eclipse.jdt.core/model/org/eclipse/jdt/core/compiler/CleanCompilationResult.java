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
 * An result class returned from an CompilationParticipant's notify() method in 
 * response to a clean event;
 * 
 * @see CompilationParticipant#notify(CompilationParticipantEvent)
 * @since 3.2
 */
public class CleanCompilationResult extends CompilationParticipantResult {

	/**
	 * @return an integer flag indicating that this is result for a post-build event.
	 * @see CompilationParticipant#CLEAN_EVENT
	 * @see CompilationParticipantResult#getKind()
	 */
	public int getKind() { return CompilationParticipant.CLEAN_EVENT; }
	
}
