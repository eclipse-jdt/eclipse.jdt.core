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
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * An event class passed into ICompilationParticipant's notify() method after a reconcile
 * has completed.
 * 
 *  @see ICompilationParticipant#notify(CompilationParticipantEvent)
 *  @since 3.2
 */
public class PostReconcileCompilationEvent extends CompilationParticipantEvent {

	/**
	 * constructs a new PostReconcileCompilationEvent
	 * 
	 * @param cu - the ICompilationUnit that was just reconciled
	 * @param ast - the AST for for ICompilationUnit that was just reconciled
	 * @param jp - the java project for the ICompilationUnit that was reconciled
	 */
	public PostReconcileCompilationEvent( ICompilationUnit cu, CompilationUnit ast, IJavaProject jp )
	{
		super( jp );
		_compilationUnit = cu;
		_ast = ast;
	}
	
	/** @return the AST for the ICompilationUnit that was just reconciled */
	public CompilationUnit getAst() { return _ast; }
	
	/** @return the ICompilationUnit that was just reconciled */
	public ICompilationUnit getCompilationUnit() { return _compilationUnit; }
	
	/**
	 * @return an integer flag indicating that this is a post-reconcile event.
	 * @see ICompilationParticipant#POST_RECONCILE_EVENT
	 * @see CompilationParticipantEvent#getKind()
	 */
	public final int getKind() { return ICompilationParticipant.POST_RECONCILE_EVENT; }

	private ICompilationUnit _compilationUnit;
	private CompilationUnit _ast;
	
}
