package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.core.*;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * The buffer manager manages the set of open buffers.
 */
public interface IBufferManager {

/**
 * Returns the open buffer associated with the given owner,
 * or <code>null</code> if the owner does not have an open
 * buffer associated with it.
 */
IBuffer getBuffer(IOpenable owner);
	/**
	 * Returns an enumeration of all open buffers.
	 * 
	 * @return Enumeration of IBuffer
	 */
	public Enumeration getOpenBuffers();
	/**
	 * Opens a buffer with the given contents, not associated with any resource,
	 * assigned to the specified owner.
	 *
	 * @exception IllegalArgumentException if contents or owner is <code>null</code>
	 */
	public IBuffer openBuffer(char[] contents, IProgressMonitor progress, IOpenable owner, boolean readOnly) throws IllegalArgumentException;
/**
 * Opens a buffer on the current contents of the specified file,
 * assigned to the specified owner.
 */
public IBuffer openBuffer(IFile file, IProgressMonitor progress, IOpenable owner, boolean readOnly) throws JavaModelException;
}
