/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core;

import java.util.ArrayList;

import org.eclipse.jdt.internal.core.DefaultCompilationUnitOwner;
import org.eclipse.jdt.internal.core.util.PerThreadObject;

/**
 * The owner of an <code>ICompilationUnit</code> handle. An owner is used to identify
 * a compilation unit. Two compilation unit handles created on the same <code>IFile</code>
 * but with two different owners are considered non-equal.
 * <p>
 * A <code>CompilationUnitOwner</code> is also used to create the buffer of a
 * compilation unit when it becomes a working copy.
 * </p>
 * <p>
 * When an <code>ICompilationUnit</code> in working copy mode is opened, or when it
 * is reconciled, problems are reported using the <code>IProblemRequestor</code>
 * provided by this <code>CompilationUnitOwner</code>.
 * </p>
 * <p>
 * Clients creating <code>ICompilationUnit</code> handles are expecting to provide a
 * <code>CompilationUnitOwner</code> using the <code>run(Runnable)</code> 
 * protocol.
 * </p>
 * 
 * @see ICompilationUnit#becomeWorkingCopy
 * @see ICompilationUnit#discardWorkingCopy
 * @since 3.0
 */
public abstract class CompilationUnitOwner {
	/*
	 * A per thread stack (ArrayList) of CompilationUnitOwners
	 */
	static PerThreadObject owners = new PerThreadObject();
	
	/**
	 * Returns the current <code>CompilationUnitOwner</code>.
	 * This is the inner most <code>CompilationUnitOwner</code> running
	 * a given <code>Runnable</code> using the <code>run(Runnable)</code>
	 * protocol.
	 * <p>
	 * If the caller of this method is not running inside the dynamic scope of
	 * a <code>run(Runnable)</code> method, then a default 
	 * <code>CompilationUnitOwner</code> is returned.
	 * </p>
	 * 
	 * @return CompilationUnitOwner the current compilation unit owner
	 */
	public static CompilationUnitOwner getCurrentOwner() {
		ArrayList stack = (ArrayList)owners.getCurrent();
		if (stack == null) {
			return DefaultCompilationUnitOwner.PRIMARY;
		}
		return (CompilationUnitOwner)stack.get(stack.size()-1);
	}
	
	/**
	 * Creates a buffer for the given working copy.
	 * The new buffer will be initialized with the contents of the underlying file
	 * if and only if it was not already initialized by the compilation owner (a buffer is uninitialized if 
	 * its content is <code>null</code>).
	 * 
	 * @param workingCopy the working copy of the buffer
	 * @return IBuffer the created buffer for the given working copy
	 * @see IBuffer
	 */
	public abstract IBuffer createBuffer(ICompilationUnit workingCopy);
	
	/**
	 * Returns the <code>IProblemRequestor</code> used to report problem
	 * when opening a working copy with this owner, or when reconciling such
	 * a working copy.
	 * 
	 * @return IProblemRequestor the problem requestor for this owner
	 */
	public abstract IProblemRequestor getProblemRequestor();

}
