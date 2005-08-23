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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;

/**
 * An event class passed into ICompilationParticipant's notify() method before a reconcile
 * has begun.
 * 
 *  @see ICompilationParticipant#notify(CompilationParticipantEvent)
 *  @since 3.2
 */
public class PreReconcileCompilationEvent extends CompilationParticipantEvent {

	/**
	 * constructs a new PreReconcileCompilationEvent
	 * 
	 * @param cu - the ICompilationUnit that is about to be reconciled
	 * @param jp - the java project for the ICompilationUnit that will be reconciled
	 */
	public PreReconcileCompilationEvent( ICompilationUnit cu, IJavaProject jp )
	{
		super( jp );
		_compilationUnit = cu;
	}
	
	
	/** @return the ICompilationUnit that will be reconciled */
	public ICompilationUnit getCompilationUnit() { return _compilationUnit; }
	
	/**
	 * @return an integer flag indicating that this is a pre-reconcile event.
	 * @see ICompilationParticipant#PRE_RECONCILE_EVENT
	 * @see CompilationParticipantEvent#getKind()
	 */
	public final int getKind() { return ICompilationParticipant.PRE_RECONCILE_EVENT; }

	private ICompilationUnit _compilationUnit;
	
}
