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

/**
 * The owner of an <code>ICompilationUnit</code> handle in working copy mode. 
 * An owner is used to identify a working copy and to create its buffer.
 * 
 * @see ICompilationUnit#becomeWorkingCopy
 * @see ICompilationUnit#discardWorkingCopy
 * @since 3.0
 */
public abstract class WorkingCopyOwner {
	
	public IBufferFactory factory;

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
		// TODO: change to use a org.eclipse.text buffer
		return this.factory.createBuffer(workingCopy);
	}

}
