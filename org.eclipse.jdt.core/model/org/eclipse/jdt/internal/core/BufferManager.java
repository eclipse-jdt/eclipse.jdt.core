package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.Util;
import java.io.*;
import java.util.Enumeration;

/**
 * The buffer manager manages the set of open buffers.
 * It implements an LRU cache of buffers.
 */
public class BufferManager {

	/**
	 * LRU cache of buffers. The key and value for an entry
	 * in the table is the identical buffer.
	 */
	protected OverflowingLRUCache fOpenBuffers = new BufferCache(20);
	protected static BufferManager fgDefaultBufferManager;
/**
 * Creates a new buffer manager.
 */
public BufferManager() {
}
/**
 * Adds a buffer to the table of open buffers.
 */
protected void addBuffer(IBuffer buffer) {
	fOpenBuffers.put(buffer.getOwner(), buffer);
}
/**
 * Returns the open buffer associated with the given owner,
 * or <code>null</code> if the owner does not have an open
 * buffer associated with it.
 */
public IBuffer getBuffer(IOpenable owner) {
	return (IBuffer)fOpenBuffers.get(owner);
}
/**
 * Returns the default buffer manager.
 * TBD: There shouldn't be a global buffer manager.
 * It should be a registered manager with the workspace.
 */
public synchronized static BufferManager getDefaultBufferManager() {
	if (fgDefaultBufferManager == null) {
		fgDefaultBufferManager = new BufferManager();
	}
	return fgDefaultBufferManager;
}
/**
 * Returns an enumeration of all open buffers.
 * <p> 
 * The <code>Enumeration</code> answered is thread safe.
 *
 * @see OverflowLRUCache
 * @return Enumeration of IBuffer
 */
public Enumeration getOpenBuffers() {
	synchronized (fOpenBuffers) {
		fOpenBuffers.shrink();
		return fOpenBuffers.elements();
	}
}
/**
 * Opens a buffer with the given contents, not associated with any resource,
 * assigned to the specified owner.
 *
 * @exception IllegalArgumentException if contents or owner is <code>null</code>
 */
public IBuffer openBuffer(char[] contents, IProgressMonitor progress, IOpenable owner, boolean readOnly) throws IllegalArgumentException {
	if (contents == null || owner == null) {
		throw new IllegalArgumentException();
	}
	Buffer buffer = new Buffer(this, contents, owner, readOnly);
	addBuffer(buffer);
	return buffer;
}
/**
 * Opens a buffer on the current contents of the specified file,
 * assigned to the specified owner.
 */
public IBuffer openBuffer(IFile file, IProgressMonitor progress, IOpenable owner, boolean readOnly) throws JavaModelException {
	if (file == null || owner == null) {
		throw new IllegalArgumentException();
	}
	char[] contents = Util.getResourceContentsAsCharArray(file);
	Buffer buffer = new Buffer(this, file, contents, owner, readOnly);
	addBuffer(buffer);
	return buffer;
}
/**
 * Removes a buffer from the table of open buffers.
 */
protected void removeBuffer(IBuffer buffer) {
	fOpenBuffers.remove(buffer.getOwner());
}
/**
 * Returns the given String as a byte array. This is centralized here in case
 * we need to do special conversion.
 */
public static byte[] stringToBytes(String s) {

	return s.getBytes();
	
}
}
