package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.core.*;

import java.io.*;
import java.util.Enumeration;

/**
 * The buffer manager manages the set of open buffers.
 * It implements an LRU cache of buffers.
 */
public class BufferManager implements IBufferFactory {

	protected static BufferManager DEFAULT_BUFFER_MANAGER;

	/**
	 * LRU cache of buffers. The key and value for an entry
	 * in the table is the identical buffer.
	 */
	protected OverflowingLRUCache openBuffers = new BufferCache(60);

/**
 * Creates a new buffer manager.
 */
public BufferManager() {
}
/**
 * Adds a buffer to the table of open buffers.
 */
protected void addBuffer(IBuffer buffer) {
	openBuffers.put(buffer.getOwner(), buffer);
}
/**
 * @see IBufferFactory#createBuffer(IOpenable)
 */
public IBuffer createBuffer(IOpenable owner) {
	IJavaElement element = (IJavaElement)owner;
	IResource resource = element.getResource();
	return 
		new Buffer(
			resource instanceof IFile ? (IFile)resource : null, 
			owner, 
			element.isReadOnly());
}

/**
 * Returns the open buffer associated with the given owner,
 * or <code>null</code> if the owner does not have an open
 * buffer associated with it.
 */
public IBuffer getBuffer(IOpenable owner) {
	return (IBuffer)openBuffers.get(owner);
}
/**
 * Returns the default buffer manager.
 */
public synchronized static BufferManager getDefaultBufferManager() {
	if (DEFAULT_BUFFER_MANAGER == null) {
		DEFAULT_BUFFER_MANAGER = new BufferManager();
	}
	return DEFAULT_BUFFER_MANAGER;
}
/**
 * Returns the default buffer factory.
 */
public IBufferFactory getDefaultBufferFactory() {
	return this;
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
	synchronized (openBuffers) {
		openBuffers.shrink();
		return openBuffers.elements();
	}
}


/**
 * Removes a buffer from the table of open buffers.
 */
protected void removeBuffer(IBuffer buffer) {
	openBuffers.remove(buffer.getOwner());
}
/**
 * Returns the given String as a byte array. This is centralized here in case
 * we need to do special conversion.
 */
public static byte[] stringToBytes(String s) {

	return s.getBytes();
	
}
}
