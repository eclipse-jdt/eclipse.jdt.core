/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core;

import org.eclipse.jdt.internal.core.BufferManager;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;

/**
 * The owner of an <code>ICompilationUnit</code> handle in working copy mode. 
 * An owner is used to identify a working copy and to create its buffer.
 * <p>
 * Clients should subclass this class to instanciate a working copy owner that is specific to their need and that
 * they can pass in to various APIs (e.g. <code>IType.resolveType(String, WorkingCopyOwner)</code>.
 * Clients can also override the default implementation of <code>createBuffer(ICompilationUnit)</code>.
 * </p>
 * 
 * @see ICompilationUnit#becomeWorkingCopy(IProblemRequestor, org.eclipse.core.runtime.IProgressMonitor)
 * @see ICompilationUnit#discardWorkingCopy()
 * @see ICompilationUnit#getWorkingCopy(org.eclipse.core.runtime.IProgressMonitor)
 * @since 3.0
 */
public abstract class WorkingCopyOwner {
	
	/**
	 * Sets the buffer provider of the primary working copy owner. Note that even if the
	 * buffer provider is a working copy owner, only its <code>createBuffer(ICompilationUnit)</code>
	 * method is used by the primary working copy owner. It doesn't replace the internal primary 
	 * working owner.
 	 * <p>
	 * This method is for internal use by the jdt-related plug-ins.
	 * Clients outside of the jdt should not reference this method.
	 * </p>
	 * 
	 * @param primaryBufferProvider the primary buffer provider
	 */
	public static void setPrimaryBufferProvider(WorkingCopyOwner primaryBufferProvider) {
		DefaultWorkingCopyOwner.PRIMARY.primaryBufferProvider = primaryBufferProvider;
	}
	
	/**
	 * Creates a buffer for the given working copy.
	 * The new buffer will be initialized with the contents of the underlying file
	 * if and only if it was not already initialized by the compilation owner (a buffer is 
	 * uninitialized if its content is <code>null</code>).
	 * 
	 * @param workingCopy the working copy of the buffer
	 * @return IBuffer the created buffer for the given working copy
	 * @see IBuffer
	 */
	public IBuffer createBuffer(ICompilationUnit workingCopy) {

		return BufferManager.getDefaultBufferManager().createBuffer(workingCopy);
	}

}
