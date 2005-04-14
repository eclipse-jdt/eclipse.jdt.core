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
 * A generic result object returned from ICompilationParticipant#notify.  This is subclassed
 * to provide event-specific return information.
 * 
 * @see ICompilationParticipant#notify(CompilationParticipantEvent)
 * @since 3.2
 */
public class CompilationParticipantResult {

	/**
	 * @return an integer flag indicating the kind of event this is a result for.  
	 * One of the *_EVENT constants in ICompilationParticipant.
	 * @see ICompilationParticipant#GENERIC_EVENT
	 */
	public int getKind() { return ICompilationParticipant.GENERIC_EVENT; }
	
}
